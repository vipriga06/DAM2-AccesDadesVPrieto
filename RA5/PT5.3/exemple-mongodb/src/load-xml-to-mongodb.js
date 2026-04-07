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

/**
 * Llegeix i analitza un fitxer XML de forma asíncrona.
 * @param {string} filePath - Ruta absoluta o relativa de l'arxiu XML.
 * @returns {Promise<Object>} Objecte JavaScript resultant de parsejar l'XML.
 */
async function parseXMLFile(filePath) {
  try {
    const xmlData = await fs.readFile(filePath, 'utf-8');

    const parser = new xml2js.Parser({ 
      explicitArray: false,
      mergeAttrs: true
    });

    return await parser.parseStringPromise(xmlData);
  } catch (error) {
    console.error(`Error llegint o analitzant el fitxer XML a ${filePath}:`, error);
    throw error;
  }
}

/**
 * Processa i transforma les dades parsejades del XML a l'esquema de MongoDB.
 * @param {Object} data - Dades crues obtingudes de xml2js.
 * @returns {Array<Object>} Llista de youtubers llista per a ser inserida.
 */
function processYoutuberData(data) {
  if (!data?.youtubers?.youtuber) {
    return [];
  }

  const youtubers = normalizeToArray(data.youtubers.youtuber);
  
  return youtubers.map(youtuber => {
    const categories = normalizeToArray(youtuber.categories?.category);
    const videos = normalizeToArray(youtuber.videos?.video);
    
    const processedVideos = videos.map(video => ({
      videoId: video.id,
      title: video.title,
      duration: video.duration,
      views: parseInteger(video.views),
      uploadDate: new Date(video.uploadDate),
      likes: parseInteger(video.likes),
      comments: parseInteger(video.comments)
    }));
    
    return {
      youtuberId: youtuber.id,
      channel: youtuber.channel,
      name: youtuber.name, 
      subscribers: parseInteger(youtuber.subscribers),
      joinDate: new Date(youtuber.joinDate),
      categories: categories,
      videos: processedVideos
    };
  });
}

/**
 * Funció principal que coordina la càrrega de dades a MongoDB.
 */
async function loadDataToMongoDB() {
  const { mongoUri, databaseName, collectionName, xmlFilePath } = getAppConfig();
  const client = new MongoClient(mongoUri);
  
  try {
    await client.connect();
    console.log('✅ Connectat correctament a MongoDB');
    
    const database = client.db(databaseName);
    const collection = database.collection(collectionName);
    
    console.log('📄 Llegint el fitxer XML...');
    const xmlData = await parseXMLFile(xmlFilePath);
    
    console.log('⚙️ Processant les dades...');
    const youtubers = processYoutuberData(xmlData);
    
    if (youtubers.length === 0) {
      console.log('⚠️ No s\'han trobat dades vàlides per inserir.');
      return;
    }
    
    console.log('🔐 Garantint índex únic sobre youtuberId...');
    await collection.createIndex({ youtuberId: 1 }, { unique: true });

    console.log('💾 Inserint o actualitzant dades a MongoDB amb upsert...');
    const bulkOperations = youtubers.map((youtuber) => ({
      updateOne: {
        filter: { youtuberId: youtuber.youtuberId },
        update: { $set: youtuber },
        upsert: true
      }
    }));

    const result = await collection.bulkWrite(bulkOperations, { ordered: false });

    console.log(
      `🎉 ${result.upsertedCount} documents creats, ${result.modifiedCount} documents actualitzats i ${result.matchedCount} documents trobats.`
    );

    return result;
  } finally {
    await client.close();
    console.log('🔌 Connexió a MongoDB tancada');
  }
}

// Bona pràctica: només executar si l'arxiu es crida directament, per permetre ser testejar
if (require.main === module) {
  loadDataToMongoDB().catch((error) => {
    console.error('❌ Error general carregant les dades a MongoDB:', error);
    process.exitCode = 1;
  });
}

module.exports = {
  getAppConfig,
  loadDataToMongoDB,
  parseXMLFile,
  processYoutuberData
};