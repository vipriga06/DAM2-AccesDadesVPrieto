// Importem la versió de promeses de 'fs' per no bloquejar l'Event Loop
const fs = require('fs').promises;
const path = require('path');
const { MongoClient } = require('mongodb');
const xml2js = require('xml2js');
require('dotenv').config({ path: path.join(__dirname, '..', '.env') });

const REQUIRED_ENV_VARS = [
  'MONGODB_URI',
  'MONGODB_DB_NAME',
  'MONGODB_COLLECTION_NAME',
  'XML_FILE_PATH'
];

const LOG = {
  info: (message) => console.log(`[INFO] ${message}`),
  warn: (message) => console.log(`[WARN] ${message}`),
  error: (message, error) => console.error(`[ERROR] ${message}`, error)
};

function normalizeToArray(value) {
  if (value === undefined || value === null || value === '') {
    return [];
  }

  return Array.isArray(value) ? value : [value];
}

function parseInteger(value) {
  const parsedValue = Number.parseInt(value, 10);
  return Number.isNaN(parsedValue) ? 0 : parsedValue;
}

function resolveConfiguredPath(filePath) {
  return path.resolve(__dirname, '..', filePath);
}

function getAppConfig() {
  const missingVariables = REQUIRED_ENV_VARS.filter((variableName) => !process.env[variableName]);

  if (missingVariables.length > 0) {
    throw new Error(`Falten variables d'entorn obligatòries: ${missingVariables.join(', ')}`);
  }

  return {
    mongoUri: process.env.MONGODB_URI,
    databaseName: process.env.MONGODB_DB_NAME,
    collectionName: process.env.MONGODB_COLLECTION_NAME,
    xmlFilePath: resolveConfiguredPath(process.env.XML_FILE_PATH)
  };
}

async function parseXMLFile(filePath) {
  try {
    const xmlData = await fs.readFile(filePath, 'utf-8');

    const parser = new xml2js.Parser({ 
      explicitArray: false,
      mergeAttrs: true
    });

    return parser.parseStringPromise(xmlData);
  } catch (error) {
    LOG.error(`Error llegint o analitzant el fitxer XML a ${filePath}:`, error);
    throw error;
  }
}

function mapVideo(video) {
  return {
    videoId: video.id,
    title: video.title,
    duration: video.duration,
    views: parseInteger(video.views),
    uploadDate: new Date(video.uploadDate),
    likes: parseInteger(video.likes),
    comments: parseInteger(video.comments)
  };
}

function mapYoutuber(youtuber) {
  const categories = normalizeToArray(youtuber.categories?.category);
  const videos = normalizeToArray(youtuber.videos?.video).map(mapVideo);

  return {
    youtuberId: youtuber.id,
    channel: youtuber.channel,
    name: youtuber.name,
    subscribers: parseInteger(youtuber.subscribers),
    joinDate: new Date(youtuber.joinDate),
    categories,
    videos
  };
}

function processYoutuberData(data) {
  if (!data?.youtubers?.youtuber) {
    return [];
  }

  const youtubers = normalizeToArray(data.youtubers.youtuber);

  return youtubers.map(mapYoutuber);
}

function buildUpsertOperations(youtubers) {
  return youtubers.map((youtuber) => ({
    updateOne: {
      filter: { youtuberId: youtuber.youtuberId },
      update: { $set: youtuber },
      upsert: true
    }
  }));
}

async function saveYoutubers(collection, youtubers) {
  if (youtubers.length === 0) {
    LOG.warn('No s\'han trobat dades vàlides per inserir.');
    return null;
  }

  LOG.info('Garantint índex únic sobre youtuberId...');
  await collection.createIndex({ youtuberId: 1 }, { unique: true });

  LOG.info('Inserint o actualitzant dades a MongoDB amb upsert...');
  const bulkOperations = buildUpsertOperations(youtubers);
  return collection.bulkWrite(bulkOperations, { ordered: false });
}

function printResult(result) {
  if (!result) {
    return;
  }

  LOG.info(
    `${result.upsertedCount} documents creats, ${result.modifiedCount} documents actualitzats i ${result.matchedCount} documents trobats.`
  );
}

async function loadDataToMongoDB() {
  const { mongoUri, databaseName, collectionName, xmlFilePath } = getAppConfig();
  const client = new MongoClient(mongoUri);
  
  try {
    await client.connect();
    LOG.info('Connectat correctament a MongoDB');
    
    const database = client.db(databaseName);
    const collection = database.collection(collectionName);
    
    LOG.info('Llegint el fitxer XML...');
    const xmlData = await parseXMLFile(xmlFilePath);
    
    LOG.info('Processant les dades...');
    const youtubers = processYoutuberData(xmlData);
    const result = await saveYoutubers(collection, youtubers);
    printResult(result);

    return result;
  } finally {
    await client.close();
    LOG.info('Connexió a MongoDB tancada');
  }
}

if (require.main === module) {
  loadDataToMongoDB().catch((error) => {
    LOG.error('Error carregant les dades a MongoDB:', error);
    process.exitCode = 1;
  });
}

module.exports = {
  getAppConfig,
  loadDataToMongoDB,
  parseXMLFile,
  processYoutuberData
};