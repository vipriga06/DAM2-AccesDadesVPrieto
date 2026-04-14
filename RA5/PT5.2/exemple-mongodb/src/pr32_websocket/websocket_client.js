const readline = require('readline');
const path = require('path');
const WebSocket = require('ws');
const dotenv = require('dotenv');

dotenv.config({ path: path.join(__dirname, '..', '..', '.env') });

const WS_HOST = process.env.WS_HOST || '127.0.0.1';
const WS_PORT = Number(process.env.WS_PORT || 8082);
const PLAYER_ID = process.env.PLAYER_ID || 'player-1';

const DIRECTIONS = {
  up: { dx: 0, dy: 1 },
  down: { dx: 0, dy: -1 },
  left: { dx: -1, dy: 0 },
  right: { dx: 1, dy: 0 }
};

const state = { x: 0, y: 0 };

function sendMove(ws, directionName) {
  const direction = DIRECTIONS[directionName];
  if (!direction) {
    return;
  }

  state.x += direction.dx;
  state.y += direction.dy;

  const payload = {
    type: 'move',
    playerId: PLAYER_ID,
    x: state.x,
    y: state.y,
    direction: directionName
  };

  ws.send(JSON.stringify(payload));
  process.stdout.write(`Moviment enviat ${directionName} -> (${state.x}, ${state.y})\n`);
}

function runDemo(ws) {
  const sequence = ['up', 'up', 'right', 'right', 'down'];
  let index = 0;

  const timer = setInterval(() => {
    if (index >= sequence.length) {
      clearInterval(timer);
      process.stdout.write('Demo finalitzada. Esperant timeout d\'inactivitat...\n');
      return;
    }

    sendMove(ws, sequence[index]);
    index += 1;
  }, 400);
}

function startInteractive(ws) {
  process.stdout.write('Mou-te amb les fletxes. Prem q per sortir.\n');

  readline.emitKeypressEvents(process.stdin);
  if (process.stdin.isTTY) {
    process.stdin.setRawMode(true);
  }

  process.stdin.on('keypress', (_, key) => {
    if (!key) {
      return;
    }

    if (key.name === 'q' || (key.ctrl && key.name === 'c')) {
      ws.close();
      process.exit(0);
    }

    if (DIRECTIONS[key.name]) {
      sendMove(ws, key.name);
    }
  });
}

function startClient() {
  const ws = new WebSocket(`ws://${WS_HOST}:${WS_PORT}`);
  const isDemo = process.argv.includes('--demo');

  ws.on('open', () => {
    process.stdout.write(`Connectat a ws://${WS_HOST}:${WS_PORT} com ${PLAYER_ID}\n`);

    if (isDemo) {
      runDemo(ws);
      return;
    }

    startInteractive(ws);
  });

  ws.on('message', (raw) => {
    try {
      const payload = JSON.parse(raw.toString());

      if (payload.type === 'move_saved') {
        process.stdout.write(
          `Servidor: moviment guardat (sessionId=${payload.sessionId}, moveIndex=${payload.moveIndex})\n`
        );
      } else if (payload.type === 'session_ended') {
        process.stdout.write(
          `Servidor: partida finalitzada (${payload.reason}). Distancia recta: ${payload.straightDistance.toFixed(4)}\n`
        );

        if (isDemo) {
          ws.close();
        }
      } else if (payload.type === 'error') {
        process.stdout.write(`Servidor: error -> ${payload.message}\n`);
      } else {
        process.stdout.write(`Servidor: ${raw.toString()}\n`);
      }
    } catch {
      process.stdout.write(`Servidor: ${raw.toString()}\n`);
    }
  });

  ws.on('close', () => {
    process.stdout.write('Connexio tancada\n');

    if (isDemo) {
      process.exit(0);
    }
  });

  ws.on('error', (error) => {
    process.stderr.write(`Error WebSocket client: ${error.message}\n`);
  });
}

startClient();
