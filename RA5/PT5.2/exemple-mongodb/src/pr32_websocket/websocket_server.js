const fs = require('fs');
const path = require('path');
const { randomUUID } = require('crypto');
const WebSocket = require('ws');
const winston = require('winston');
const LokiTransport = require('winston-loki');
const { MongoClient } = require('mongodb');
const dotenv = require('dotenv');

dotenv.config({ path: path.join(__dirname, '..', '..', '.env') });

const PROJECT_ROOT = path.resolve(__dirname, '..', '..');

function parseNumber(value, defaultValue) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : defaultValue;
}

function getConfig() {
  return {
    wsHost: process.env.WS_HOST || '127.0.0.1',
    wsPort: parseNumber(process.env.WS_PORT, 8082),
    inactivityTimeoutMs: parseNumber(process.env.INACTIVITY_TIMEOUT_MS, 10000),
    mongodbUri: process.env.MONGODB_URI || 'mongodb://root:password@localhost:27017/',
    mongodbDbName: process.env.MONGODB_DB_NAME || 'pr32_game_db',
    mongodbCollection: process.env.MONGODB_COLLECTION || 'movements',
    logLevel: process.env.LOG_LEVEL || 'info',
    logDir: process.env.LOG_FILE_PATH
      ? path.resolve(PROJECT_ROOT, process.env.LOG_FILE_PATH)
      : path.resolve(PROJECT_ROOT, '../data/logs'),
    lokiEnabled: String(process.env.LOKI_ENABLED || 'false').toLowerCase() === 'true',
    lokiUrl: process.env.LOKI_URL || 'http://localhost:3100',
    lokiJob: process.env.LOKI_JOB || 'pr32-websocket'
  };
}

function ensureDirectory(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
}

function createLogger(config) {
  ensureDirectory(config.logDir);
  const logFilePath = path.join(config.logDir, 'pr32_websocket_server.log');

  const transports = [
    new winston.transports.Console(),
    new winston.transports.File({ filename: logFilePath })
  ];

  if (config.lokiEnabled) {
    transports.push(
      new LokiTransport({
        host: config.lokiUrl,
        labels: { app: 'pr32-websocket-server', job: config.lokiJob },
        json: true,
        batching: true,
        interval: 5
      })
    );
  }

  return winston.createLogger({
    level: config.logLevel,
    format: winston.format.combine(
      winston.format.timestamp(),
      winston.format.printf(({ timestamp, level, message }) => `[${timestamp}] ${level.toUpperCase()} ${message}`)
    ),
    transports
  });
}

function isValidMoveMessage(payload) {
  return Boolean(
    payload
      && typeof payload === 'object'
      && payload.type === 'move'
      && typeof payload.playerId === 'string'
      && Number.isFinite(payload.x)
      && Number.isFinite(payload.y)
      && typeof payload.direction === 'string'
  );
}

function distanceInStraightLine(start, end) {
  const dx = end.x - start.x;
  const dy = end.y - start.y;
  return Math.sqrt((dx * dx) + (dy * dy));
}

function createSessionFromMove(move) {
  return {
    sessionId: randomUUID(),
    playerId: move.playerId,
    start: { x: move.x, y: move.y },
    end: { x: move.x, y: move.y },
    moveCount: 0
  };
}

function createMoveDocument(session, move) {
  session.moveCount += 1;

  return {
    sessionId: session.sessionId,
    playerId: session.playerId,
    moveIndex: session.moveCount,
    x: move.x,
    y: move.y,
    direction: move.direction,
    createdAt: new Date()
  };
}

function sendJson(ws, payload) {
  if (ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(payload));
  }
}

function parseJsonSafe(rawMessage) {
  try {
    return { ok: true, value: JSON.parse(rawMessage.toString()) };
  } catch (error) {
    return { ok: false, error };
  }
}

async function runServer() {
  const config = getConfig();
  const logger = createLogger(config);
  const mongoClient = new MongoClient(config.mongodbUri);

  await mongoClient.connect();
  logger.info(`MongoDB connectat a ${config.mongodbUri}`);

  const movesCollection = mongoClient
    .db(config.mongodbDbName)
    .collection(config.mongodbCollection);

  const webSocketServer = new WebSocket.Server({ host: config.wsHost, port: config.wsPort });
  logger.info(`Servidor WebSocket escoltant a ws://${config.wsHost}:${config.wsPort}`);

  webSocketServer.on('connection', (ws, req) => {
    const clientId = randomUUID();
    const clientIp = req.socket.remoteAddress || 'unknown';

    let session = null;
    let inactivityTimer = null;

    const clearInactivityTimer = () => {
      if (inactivityTimer) {
        clearTimeout(inactivityTimer);
        inactivityTimer = null;
      }
    };

    const finishCurrentSession = (reason) => {
      if (!session) {
        return;
      }

      const straightDistance = distanceInStraightLine(session.start, session.end);

      sendJson(ws, {
        type: 'session_ended',
        reason,
        sessionId: session.sessionId,
        playerId: session.playerId,
        start: session.start,
        end: session.end,
        movesRegistered: session.moveCount,
        straightDistance,
        inactivityMs: config.inactivityTimeoutMs
      });

      logger.info(
        `Partida finalitzada sessionId=${session.sessionId} playerId=${session.playerId} motiu=${reason} distancia=${straightDistance.toFixed(4)}`
      );

      session = null;
      clearInactivityTimer();
    };

    const resetInactivityTimer = () => {
      clearInactivityTimer();
      inactivityTimer = setTimeout(() => {
        finishCurrentSession('inactivity_timeout');
      }, config.inactivityTimeoutMs);
    };

    logger.info(`Client connectat clientId=${clientId} ip=${clientIp}`);

    ws.on('message', async (rawMessage) => {
      try {
        const parsed = parseJsonSafe(rawMessage);
        if (!parsed.ok) {
          logger.warn(`JSON invalid rebut des de clientId=${clientId}`);
          sendJson(ws, { type: 'error', message: 'Invalid JSON format' });
          return;
        }

        const payload = parsed.value;

        if (!isValidMoveMessage(payload)) {
          logger.warn(`Missatge invalid rebut des de clientId=${clientId}`);
          sendJson(ws, { type: 'error', message: 'Invalid move payload' });
          return;
        }

        if (!session || session.playerId !== payload.playerId) {
          session = createSessionFromMove(payload);
          logger.info(`Partida iniciada sessionId=${session.sessionId} playerId=${session.playerId}`);
        }

        session.end = { x: payload.x, y: payload.y };
        const moveDocument = createMoveDocument(session, payload);
        await movesCollection.insertOne(moveDocument);

        sendJson(ws, {
          type: 'move_saved',
          sessionId: session.sessionId,
          moveIndex: moveDocument.moveIndex,
          x: payload.x,
          y: payload.y,
          direction: payload.direction
        });

        logger.info(
          `Moviment guardat sessionId=${session.sessionId} moveIndex=${moveDocument.moveIndex} x=${payload.x} y=${payload.y} direction=${payload.direction}`
        );

        resetInactivityTimer();
      } catch (error) {
        logger.error(`Error processant missatge clientId=${clientId} ${error.message}`);
        sendJson(ws, { type: 'error', message: 'Server processing error' });
      }
    });

    ws.on('close', (code, reasonBuffer) => {
      const reason = reasonBuffer && reasonBuffer.length > 0 ? reasonBuffer.toString() : 'no-reason';
      finishCurrentSession('client_disconnected');
      logger.info(`Client desconnectat clientId=${clientId} code=${code} reason=${reason}`);
    });

    ws.on('error', (error) => {
      logger.error(`WebSocket error clientId=${clientId} ${error.message}`);
    });
  });

  async function gracefulShutdown() {
    logger.info('Apagant servidor WebSocket');
    webSocketServer.close();
    await mongoClient.close();
    logger.info('Connexio MongoDB tancada');
    process.exit(0);
  }

  process.on('SIGINT', gracefulShutdown);
  process.on('SIGTERM', gracefulShutdown);
}

runServer().catch((error) => {
  console.error('Error fatal del servidor:', error);
  process.exit(1);
});
