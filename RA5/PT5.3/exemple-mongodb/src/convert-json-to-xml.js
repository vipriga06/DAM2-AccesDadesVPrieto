const fs = require('fs').promises;
const path = require('path');
const xml2js = require('xml2js');

require('dotenv').config({ path: path.join(__dirname, '..', '.env') });

const JSON_FILE_PATH = process.env.JSON_FILE_PATH || '../data/youtubers.json';
const XML_FILE_PATH = process.env.XML_FILE_PATH || '../data/youtubers.xml';

const LOG = {
  info: (message) => console.log(`[INFO] ${message}`),
  warn: (message) => console.log(`[WARN] ${message}`),
  error: (message, error) => console.error(`[ERROR] ${message}`, error)
};

function resolveFromProject(filePath) {
  return path.resolve(__dirname, '..', filePath);
}

function normalizeToArray(value) {
  if (value === undefined || value === null || value === '') {
    return [];
  }

  return Array.isArray(value) ? value : [value];
}

function toXmlYoutuber(youtuber) {
  const categories = normalizeToArray(youtuber.categories).map((category) => String(category));
  const videos = normalizeToArray(youtuber.videos).map((video) => ({
    $: { id: String(video.id ?? '') },
    title: String(video.title ?? ''),
    duration: String(video.duration ?? ''),
    views: String(video.views ?? 0),
    uploadDate: String(video.uploadDate ?? ''),
    likes: String(video.likes ?? 0),
    comments: String(video.comments ?? 0)
  }));

  return {
    $: { id: String(youtuber.id ?? '') },
    channel: String(youtuber.channel ?? ''),
    name: String(youtuber.name ?? ''),
    subscribers: String(youtuber.subscribers ?? 0),
    joinDate: String(youtuber.joinDate ?? ''),
    categories: {
      category: categories
    },
    videos: {
      video: videos
    }
  };
}

function buildXmlObjectFromJson(parsedJson) {
  const youtubers = normalizeToArray(parsedJson?.youtubers);

  if (youtubers.length === 0) {
    throw new Error('El JSON no conté una llista vàlida a la clau "youtubers".');
  }

  return {
    youtubers: {
      youtuber: youtubers.map(toXmlYoutuber)
    }
  };
}

async function readJsonFile(filePath) {
  const rawData = await fs.readFile(filePath, 'utf-8');
  return JSON.parse(rawData);
}

async function writeXmlFile(filePath, xmlObject) {
  const builder = new xml2js.Builder({
    headless: false,
    renderOpts: { pretty: true, indent: '  ', newline: '\n' }
  });

  const xmlString = builder.buildObject(xmlObject);
  await fs.writeFile(filePath, xmlString, 'utf-8');
}

async function convertJsonToXml() {
  const jsonPath = resolveFromProject(JSON_FILE_PATH);
  const xmlPath = resolveFromProject(XML_FILE_PATH);

  try {
    LOG.info(`Llegint JSON des de ${jsonPath}...`);
    const parsedJson = await readJsonFile(jsonPath);

    LOG.info('Transformant JSON a estructura XML...');
    const xmlObject = buildXmlObjectFromJson(parsedJson);

    LOG.info(`Escrivint XML a ${xmlPath}...`);
    await writeXmlFile(xmlPath, xmlObject);

    LOG.info('Conversió JSON -> XML finalitzada correctament.');
  } catch (error) {
    LOG.error('Error durant la conversió JSON -> XML:', error);
    process.exitCode = 1;
  }
}

if (require.main === module) {
  convertJsonToXml();
}

module.exports = {
  convertJsonToXml,
  buildXmlObjectFromJson,
  toXmlYoutuber,
  normalizeToArray
};
