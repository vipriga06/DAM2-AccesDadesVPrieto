// Importacions
const fs = require('fs').promises;
const path = require('path');
require('dotenv').config();
const sharp = require('sharp');

// Constants
const IMAGES_SUBFOLDER = 'imatges/animals';
const IMAGE_TYPES = ['.jpg', '.jpeg', '.png', '.gif'];
const OLLAMA_URL = process.env.CHAT_API_OLLAMA_URL;
const OLLAMA_MODEL = process.env.CHAT_API_OLLAMA_MODEL_VISION;
const DELAY_MS = 2000; // 2 segundos entre imágenes para no saturar

// Funció per dormir
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// Funció per convertir imatge a Base64 amb compressió AGRESIVA
async function imageToBase64(imagePath) {
    try {
        const compressedBuffer = await sharp(imagePath)
            .resize(300, 200, { fit: 'inside', withoutEnlargement: true })
            .jpeg({ quality: 30 })
            .toBuffer();
        
        return Buffer.from(compressedBuffer).toString('base64');
    } catch (error) {
        console.error(`Error: ${imagePath} - ${error.message}`);
        return null;
    }
}

// Funció per consultar Ollama
async function queryOllama(base64Image, prompt) {
    try {
        const response = await fetch(`${OLLAMA_URL}/api/generate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                model: OLLAMA_MODEL,
                prompt: prompt,
                images: [base64Image],
                stream: false
            })
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        
        const data = await response.json();
        if (!data || !data.response) throw new Error('Sin respuesta válida');
        
        return data.response;
    } catch (error) {
        console.error('Error Ollama:', error.message);
        return null;
    }
}

// Funció per parsear JSON - elimina blocs markdown i caracteres problemàtics
function parseAnimalAnalysis(response) {
    try {
        let cleaned = response.trim();
        
        // Eliminar bloques de markdown ```json ... ```
        cleaned = cleaned.replace(/^[\s\n]*```json\s*/i, '');
        cleaned = cleaned.replace(/\s*```[\s\n]*$/i, '');
        cleaned = cleaned.replace(/^[\s\n]*```\s*/i, '');
        
        // Eliminar caracteres de control problemáticos
        cleaned = cleaned.replace(/[\x00-\x1F\x7F]/g, '');
        
        const parsed = JSON.parse(cleaned);
        return parsed;
    } catch (e) {
        console.error('Error parseando JSON:', e.message);
        return { analisi_raw: response, nota: 'Error al parsear' };
    }
}

// MAIN
async function main() {
    try {
        const dataPath = process.env.DATA_PATH;
        if (!dataPath || !OLLAMA_URL || !OLLAMA_MODEL) {
            throw new Error('Variables de entorno faltantes');
        }

        const imagesFolderPath = path.join(__dirname, dataPath, IMAGES_SUBFOLDER);
        await fs.access(imagesFolderPath);

        const resultado = { analisis: [] };
        const animalDirectories = await fs.readdir(imagesFolderPath);

        console.log(`Procesando máx 2 directorios, 2 imágenes cada uno...\n`);

        // Procesar máximo 2 directorios
        for (let dirIdx = 0; dirIdx < Math.min(2, animalDirectories.length); dirIdx++) {
            const animalDir = animalDirectories[dirIdx];
            const animalDirPath = path.join(imagesFolderPath, animalDir);

            const stats = await fs.stat(animalDirPath);
            if (!stats.isDirectory()) continue;

            const imageFiles = await fs.readdir(animalDirPath);
            
            // Máximo 2 imágenes por directorio
            const imagesToProcess = [];
            for (const file of imageFiles) {
                if (imagesToProcess.length >= 2) break;
                
                const ext = path.extname(file).toLowerCase();
                if (IMAGE_TYPES.includes(ext)) {
                    imagesToProcess.push({
                        path: path.join(animalDirPath, file),
                        name: file
                    });
                }
            }

            console.log(`[${dirIdx + 1}/2] ${animalDir} - ${imagesToProcess.length} imágenes`);

            // Procesar imágenes secuencialmente
            for (const img of imagesToProcess) {
                await delay(DELAY_MS);
                
                console.log(`  Procesando: ${img.name}`);
                const base64 = await imageToBase64(img.path);

                if (base64) {
                    const prompt = `Analitza la imatge i proporciona un anàlisis detallat de l'animal en format JSON puro:
{
    "nom_comu": "nom comú",
    "nom_cientific": "nom científic",
    "taxonomia": {
        "classe": "mamífer/au/rèptil/amfibi/peix",
        "ordre": "ordre",
        "familia": "família"
    },
    "habitat": {
        "tipus": ["hàbitats"],
        "regioGeografica": ["regions"],
        "clima": ["climes"]
    },
    "dieta": {
        "tipus": "carnívor/herbívor/omnívor",
        "aliments_principals": ["aliments"]
    },
    "caracteristiques_fisiques": {
        "mida": {
            "altura_mitjana_cm": "cm",
            "pes_mitja_kg": "kg"
        },
        "colors_predominants": ["colors"],
        "trets_distintius": ["trets"]
    },
    "estat_conservacio": {
        "classificacio_IUCN": "estat",
        "amenaces_principals": ["amenaces"]
    }
}`;

                    const response = await queryOllama(base64, prompt);
                    
                    if (response) {
                        resultado.analisis.push({
                            imatge: { nom_fitxer: img.name },
                            analisi: parseAnimalAnalysis(response)
                        });
                        console.log(`  ✓ Completado\n`);
                    } else {
                        console.log(`  ✗ Error\n`);
                    }
                }
            }
        }

        // Guardar resultados
        const outputPath = path.join(__dirname, dataPath, 'exercici3_resposta.json');
        await fs.writeFile(outputPath, JSON.stringify(resultado, null, 2));
        
        // Limpiar archivo: parsear y reparsear para eliminar analisi_raw
        const savedContent = await fs.readFile(outputPath, 'utf-8');
        const savedData = JSON.parse(savedContent);
        
        const cleaned = {
            analisis: savedData.analisis.map(item => {
                if (item.analisi && item.analisi.analisi_raw) {
                    // Si tiene analisi_raw, intentar parsear nuevamente
                    try {
                        let jsonStr = item.analisi.analisi_raw;
                        jsonStr = jsonStr.replace(/^[\s\n]*```json\s*/i, '');
                        jsonStr = jsonStr.replace(/\s*```[\s\n]*$/i, '');
                        jsonStr = jsonStr.replace(/^[\s\n]*```\s*/i, '');
                        jsonStr = jsonStr.replace(/[\x00-\x1F\x7F]/g, '');
                        const parsed = JSON.parse(jsonStr);
                        return { imatge: item.imatge, analisi: parsed };
                    } catch (e) {
                        // Dejar como está si no se puede parsear
                        return item;
                    }
                }
                return item;
            })
        };
        
        // Sobrescribir con versión limpia
        await fs.writeFile(outputPath, JSON.stringify(cleaned, null, 2));
        console.log(`✓ Guardado y limpiado: ${outputPath}`);

    } catch (error) {
        console.error('Error:', error.message);
    }
}

main();
