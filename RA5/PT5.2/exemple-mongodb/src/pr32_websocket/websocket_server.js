const fs = require('fs');
const path = require('path');
const { randomUUID } = require('crypto');
const WebSocket = require('ws');
const dotenv = require('dotenv');
const winston = require('winston');
const { MongoClient } = require('mongodb');

dotenv.config({ path: path.join(__dirname, '..', '..', '.env') });

const PROJECT_ROOT = path.resolve(__dirname, '..', '..');
const LOG_DIR = process.env.LOG_FILE_PATH
  ? path.resolve(PROJECT_ROOT, process.env.LOG_FILE_PATH)
  : path.resolve(PROJECT_ROOT, '../data/logs');
const WS_HOST = process.env.WS_HOST || '127.0.0.1';
const WS_PORT = Number(process.env.WS_PORT || 8082);
const INACTIVITY_TIMEOUT_MS = Number(process.env.INACTIVITY_TIMEOUT_MS || 10000);
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://root:password@localhost:27017/';
const MONGODB_DB_NAME = process.env.MONGODB_DB_NAME || 'pr32_game_db';
const MONGODB_COLLECTION = process.env.MONGODB_COLLECTION || 'movements';

function ensureDir(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
}

function createLogger() {
  ensureDir(LOG_DIR);
  const logFile = path.join(LOG_DIR, 'pr32_websocket_server.log');

  return winston.createLogger({
    level: process.env.LOG_LEVEL || 'info',
    format: winston.format.combine(
      winston.format.timestamp(),
      winston.format.printf(({ timestamp, level, message }) => `[${timestamp}] ${level.toUpperCase()} ${message}`)
    ),
    transports: [
      new winston.transports.Console(),
      new winston.transports.File({ filename: logFile })
    ]
  });
}

function isValidMoveMessage(payload) {
  if (!payload || typeof payload !== 'object') {
    return false;
  }

  return payload.type === 'move'
    && typeof payload.playerId === 'string'
    && Number.isFinite(payload.x)
    && Number.isFinite(payload.y);
}

function computeDistance(start, end) {
  const dx = end.x - start.x;
  const dy = end.y - start.y;
  return Math.sqrt((dx * dx) + (dy * dy));
}

async function startServer() {
  const logger = createLogger();
  const mongoClient = new MongoClient(MONGODB_URI);

  await mongoClient.connect();
  logger.info(`Connected to MongoDB ${MONGODB_URI}`);

  const movementsCollection = mongoClient.db(MONGODB_DB_NAME).collection(MONGODB_COLLECTION);
  const wss = new WebSocket.Server({ host: WS_HOST, port: WS_PORT });

  logger.info(`WebSocket server listening on ws://${WS_HOST}:${WS_PORT}`);

  wss.on('connection', (ws, req) => {
    const clientId = randomUUID();
    const clientIp = req.socket.remoteAddress || 'unknown';

    let currentSession = null;
    let inactivityTimer = null;

    const clearTimer = () => {
      if (inactivityTimer) {
        clearTimeout(inactivityTimer);
        inactivityTimer = null;
      }
    };

    const finishSession = () => {
      if (!currentSession) {
        return;
      }

      const distance = computeDistance(currentSession.start, currentSession.end);
      const finished = {
        type: 'session_ended',
        sessionId: currentSession.sessionId,
        playerId: currentSession.playerId,
        start: currentSession.start,
        end: currentSession.end,
        straightDistance: distance,
        inactivityMs: INACTIVITY_TIMEOUT_MS
      };

      ws.send(JSON.stringify(finished));
      logger.info(
        `Session ended sessionId=${finished.sessionId} playerId=${finished.playerId} distance=${distance.toFixed(4)}`
      );

      currentSession = null;
      clearTimer();
    };

    const resetInactivity = () => {
      clearTimer();
      inactivityTimer = setTimeout(() => {
        finishSession();
      }, INACTIVITY_TIMEOUT_MS);
    };

    logger.info(`Client connected clientId=${clientId} ip=${clientIp}`);

    ws.on('message', async (rawMessage) => {
      try {
        const payload = JSON.parse(rawMessage.toString());

        if (!isValidMoveMessage(payload)) {
          logger.warn(`Invalid message from clientId=${clientId}`);
          ws.send(JSON.stringify({ type: 'error', message: 'Invalid move payload' }));
          return;
        }

        if (!currentSession || currentSession.playerId !== payload.playerId) {
          currentSession = {
            sessionId: randomUUID(),
            playerId: payload.playerId,
            start: { x: payload.x, y: payload.y },
            end: { x: payload.x, y: payload.y }
          };

          logger.info(
            `Session started sessionId=${currentSession.sessionId} playerId=${currentSession.playerId}`
          );
        }

        currentSession.end = { x: payload.x, y: payload.y };

        const movementDoc = {
          sessionId: currentSession.sessionId,
          playerId: payload.playerId,
          x: payload.x,
          y: payload.y,
          direction: payload.direction || null,
          createdAt: new Date()
        };

        await movementsCollection.insertOne(movementDoc);

        ws.send(JSON.stringify({
          type: 'move_saved',
          sessionId: currentSession.sessionId,
          x: payload.x,
          y: payload.y,
          direction: payload.direction || null
        }));

        logger.info(
          `Move saved sessionId=${movementDoc.sessionId} playerId=${movementDoc.playerId} x=${movementDoc.x} y=${movementDoc.y}`
        );

        resetInactivity();
      } catch (error) {
        logger.error(`Error processing message clientId=${clientId} ${error.message}`);
        ws.send(JSON.stringify({ type: 'error', message: 'Server processing error' }));
      }
    });

    ws.on('close', () => {
      finishSession();
      logger.info(`Client disconnected clientId=${clientId}`);
    });

    ws.on('error', (error) => {
      logger.error(`WebSocket error clientId=${clientId} ${error.message}`);
    });
  });

  const gracefulShutdown = async () => {
    logger.info('Shutting down WebSocket server');
    wss.close();
    await mongoClient.close();
    logger.info('MongoDB connection closed');
    process.exit(0);
  };

  process.on('SIGINT', gracefulShutdown);
  process.on('SIGTERM', gracefulShutdown);
}

startServer().catch((error) => {
  console.error('Fatal server error:', error);
  process.exit(1);
});
