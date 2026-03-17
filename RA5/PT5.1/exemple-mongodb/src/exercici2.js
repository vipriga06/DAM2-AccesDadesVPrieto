const fs = require('fs');
const path = require('path');
const dotenv = require('dotenv');
const PDFDocument = require('pdfkit');
const { MongoClient } = require('mongodb');

dotenv.config({ path: path.join(__dirname, '..', '.env') });

const PROJECT_ROOT = path.resolve(__dirname, '..');
const DATA_DIR = process.env.DATA_DIR_PATH
  ? path.resolve(PROJECT_ROOT, process.env.DATA_DIR_PATH)
  : path.resolve(__dirname, '../../data');
const OUT_DIR = path.join(DATA_DIR, 'out');

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://root:password@localhost:27017/';
const MONGODB_DB_NAME = process.env.MONGODB_DB_NAME || 'stackexchange_db';
const MONGODB_COLLECTION = process.env.MONGODB_COLLECTION || 'questions';

const TITLE_WORDS = ['pug', 'wig', 'yak', 'nap', 'jig', 'mug', 'zap', 'gag', 'oaf', 'elf'];

function ensureDir(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
}

function createWordRegex(words) {
  const escaped = words.map((w) => w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'));
  return new RegExp(`\\b(${escaped.join('|')})\\b`, 'i');
}

function writePdf(filePath, reportTitle, titles) {
  return new Promise((resolve, reject) => {
    const doc = new PDFDocument({ margin: 50 });
    const stream = fs.createWriteStream(filePath);

    stream.on('finish', resolve);
    stream.on('error', reject);
    doc.on('error', reject);

    doc.pipe(stream);
    doc.fontSize(18).text(reportTitle, { underline: true });
    doc.moveDown(0.8);
    doc.fontSize(11).text(`Total resultados: ${titles.length}`);
    doc.moveDown(0.8);

    if (titles.length === 0) {
      doc.fontSize(11).text('Sin resultados.');
    } else {
      titles.forEach((title, idx) => {
        doc.fontSize(10).text(`${idx + 1}. ${title}`);
      });
    }

    doc.end();
  });
}

async function main() {
  ensureDir(OUT_DIR);

  const client = new MongoClient(MONGODB_URI);
  await client.connect();
  console.log(`Conectado a MongoDB: ${MONGODB_URI}`);

  try {
    const collection = client.db(MONGODB_DB_NAME).collection(MONGODB_COLLECTION);

    const avgResult = await collection
      .aggregate([
        {
          $group: {
            _id: null,
            avgViewCount: { $avg: { $toDouble: '$question.ViewCount' } }
          }
        }
      ])
      .toArray();

    const avgViewCount = avgResult[0]?.avgViewCount ?? 0;

    const q1Docs = await collection
      .find({
        $expr: {
          $gt: [{ $toDouble: '$question.ViewCount' }, avgViewCount]
        }
      })
      .project({ _id: 0, 'question.Title': 1 })
      .toArray();

    const q1Titles = q1Docs.map((d) => d.question?.Title).filter(Boolean);
    console.log(`Consulta 1 -> resultados: ${q1Titles.length}`);

    const wordRegex = createWordRegex(TITLE_WORDS);
    const q2Docs = await collection
      .find({ 'question.Title': { $regex: wordRegex } })
      .project({ _id: 0, 'question.Title': 1 })
      .toArray();

    const q2Titles = q2Docs.map((d) => d.question?.Title).filter(Boolean);
    console.log(`Consulta 2 -> resultados: ${q2Titles.length}`);

    await writePdf(path.join(OUT_DIR, 'informe1.pdf'), 'Informe 1: ViewCount superior a la media', q1Titles);
    await writePdf(path.join(OUT_DIR, 'informe2.pdf'), 'Informe 2: Titulos con palabras clave', q2Titles);

    console.log(`PDF generado: ${path.join(OUT_DIR, 'informe1.pdf')}`);
    console.log(`PDF generado: ${path.join(OUT_DIR, 'informe2.pdf')}`);
  } finally {
    await client.close();
    console.log('Conexion MongoDB cerrada.');
  }
}

main().catch((error) => {
  console.error('Error en exercici2:', error.message);
  process.exitCode = 1;
});
