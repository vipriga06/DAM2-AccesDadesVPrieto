const fs = require('fs');
const path = require('path');
const readline = require('readline');
const dotenv = require('dotenv');
const he = require('he');
const { MongoClient } = require('mongodb');
const winston = require('winston');

dotenv.config({ path: path.join(__dirname, '..', '.env') });

const TOP_N = Number(process.env.TOP_N || 10000);
const PROJECT_ROOT = path.resolve(__dirname, '..');
const DATA_DIR = process.env.DATA_DIR_PATH
  ? path.resolve(PROJECT_ROOT, process.env.DATA_DIR_PATH)
  : path.resolve(__dirname, '../../data');
const XML_INPUT_FILE = process.env.STACKEXCHANGE_XML_PATH
  ? path.resolve(PROJECT_ROOT, process.env.STACKEXCHANGE_XML_PATH)
  : path.join(DATA_DIR, 'Posts.xml');
const LOG_DIR = process.env.LOG_FILE_PATH
  ? path.resolve(PROJECT_ROOT, process.env.LOG_FILE_PATH)
  : path.join(DATA_DIR, 'logs');
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://root:password@localhost:27017/';
const MONGODB_DB_NAME = process.env.MONGODB_DB_NAME || 'stackexchange_db';
const MONGODB_COLLECTION = process.env.MONGODB_COLLECTION || 'questions';

function ensureDir(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
}

function createLogger() {
  ensureDir(LOG_DIR);
  return winston.createLogger({
    level: process.env.LOG_LEVEL || 'info',
    format: winston.format.combine(
      winston.format.timestamp(),
      winston.format.printf(({ timestamp, level, message }) => `[${timestamp}] ${level.toUpperCase()} ${message}`)
    ),
    transports: [
      new winston.transports.Console(),
      new winston.transports.File({ filename: path.join(LOG_DIR, 'exercici1.log') })
    ]
  });
}

class MinHeap {
  constructor() {
    this.data = [];
  }

  size() {
    return this.data.length;
  }

  peek() {
    return this.data[0];
  }

  push(item) {
    this.data.push(item);
    this.bubbleUp(this.data.length - 1);
  }

  replaceRoot(item) {
    this.data[0] = item;
    this.bubbleDown(0);
  }

  bubbleUp(index) {
    while (index > 0) {
      const parent = Math.floor((index - 1) / 2);
      if (this.data[parent].viewCount <= this.data[index].viewCount) {
        break;
      }
      [this.data[parent], this.data[index]] = [this.data[index], this.data[parent]];
      index = parent;
    }
  }

  bubbleDown(index) {
    const lastIndex = this.data.length - 1;
    while (true) {
      let smallest = index;
      const left = index * 2 + 1;
      const right = index * 2 + 2;

      if (left <= lastIndex && this.data[left].viewCount < this.data[smallest].viewCount) {
        smallest = left;
      }
      if (right <= lastIndex && this.data[right].viewCount < this.data[smallest].viewCount) {
        smallest = right;
      }
      if (smallest === index) {
        break;
      }
      [this.data[index], this.data[smallest]] = [this.data[smallest], this.data[index]];
      index = smallest;
    }
  }
}

function parseXmlAttributes(rowLine) {
  const attrs = {};
  const regex = /([A-Za-z0-9_:-]+)="([\s\S]*?)"/g;
  let match = regex.exec(rowLine);
  while (match !== null) {
    attrs[match[1]] = he.decode(match[2]);
    match = regex.exec(rowLine);
  }
  return attrs;
}

function buildQuestion(attrs) {
  return {
    Id: attrs.Id || '',
    PostTypeId: attrs.PostTypeId || '',
    AcceptedAnswerId: attrs.AcceptedAnswerId || '',
    CreationDate: attrs.CreationDate || '',
    Score: attrs.Score || '',
    ViewCount: attrs.ViewCount || '0',
    Body: attrs.Body || '',
    OwnerUserId: attrs.OwnerUserId || '',
    LastActivityDate: attrs.LastActivityDate || '',
    Title: attrs.Title || '',
    Tags: attrs.Tags || '',
    AnswerCount: attrs.AnswerCount || '0',
    CommentCount: attrs.CommentCount || '0',
    ContentLicense: attrs.ContentLicense || ''
  };
}

async function extractTopQuestionsFromXml(logger) {
  if (!fs.existsSync(XML_INPUT_FILE)) {
    throw new Error(`No existe el XML de entrada: ${XML_INPUT_FILE}`);
  }

  const heap = new MinHeap();
  const stream = fs.createReadStream(XML_INPUT_FILE, { encoding: 'utf8' });
  const rl = readline.createInterface({ input: stream, crlfDelay: Infinity });

  let totalRows = 0;
  let validQuestions = 0;

  for await (const line of rl) {
    const trimmed = line.trim();
    if (!trimmed.startsWith('<row ')) {
      continue;
    }

    totalRows += 1;
    const attrs = parseXmlAttributes(trimmed);
    if (attrs.PostTypeId !== '1') {
      continue;
    }

    const viewCount = Number.parseInt(attrs.ViewCount || '0', 10);
    if (!Number.isFinite(viewCount)) {
      continue;
    }

    validQuestions += 1;
    const doc = { question: buildQuestion(attrs) };
    const item = { viewCount, doc };

    if (heap.size() < TOP_N) {
      heap.push(item);
    } else if (viewCount > heap.peek().viewCount) {
      heap.replaceRoot(item);
    }

    if (totalRows % 200000 === 0) {
      logger.info(`Procesadas ${totalRows} filas del XML...`);
    }
  }

  logger.info(`Filas XML leidas: ${totalRows}`);
  logger.info(`Preguntas validas detectadas: ${validQuestions}`);

  return heap.data
    .sort((a, b) => b.viewCount - a.viewCount)
    .map((x) => x.doc);
}

async function saveToMongo(docs, logger) {
  const client = new MongoClient(MONGODB_URI);
  await client.connect();
  logger.info(`Conectado a MongoDB: ${MONGODB_URI}`);

  try {
    const collection = client.db(MONGODB_DB_NAME).collection(MONGODB_COLLECTION);
    await collection.deleteMany({});

    if (docs.length === 0) {
      logger.warn('No hay documentos para insertar.');
      return;
    }

    const result = await collection.insertMany(docs, { ordered: false });
    logger.info(`Insertados ${result.insertedCount} documentos en ${MONGODB_DB_NAME}.${MONGODB_COLLECTION}`);
  } finally {
    await client.close();
    logger.info('Conexion MongoDB cerrada.');
  }
}

async function main() {
  const logger = createLogger();

  logger.info('Inicio exercici1');
  logger.info(`XML de entrada: ${XML_INPUT_FILE}`);
  logger.info(`TOP_N: ${TOP_N}`);

  const topQuestions = await extractTopQuestionsFromXml(logger);
  logger.info(`Preguntas candidatas para insercion: ${topQuestions.length}`);

  await saveToMongo(topQuestions, logger);
  logger.info('Fin exercici1');
}

main().catch((error) => {
  const fallbackLogDir = path.join(path.resolve(__dirname, '../../data'), 'logs');
  ensureDir(fallbackLogDir);
  fs.appendFileSync(path.join(fallbackLogDir, 'exercici1.log'), `${new Date().toISOString()} ERROR ${error.stack}\n`);
  console.error('Error en exercici1:', error.message);
  process.exitCode = 1;
});
