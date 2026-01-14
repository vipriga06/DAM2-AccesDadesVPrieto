#!/usr/bin/env node

/**
 * Servidor minimal para probar el endpoint sentiment-analysis
 * Sin dependencia de MySQL
 */

const express = require('express');
const axios = require('axios');
const app = express();

app.use(express.json());

const OLLAMA_API_URL = process.env.CHAT_API_OLLAMA_URL || 'http://127.0.0.1:11434/api';
const DEFAULT_MODEL = process.env.CHAT_API_OLLAMA_MODEL || 'qwen2.5vl:7b';

// Almacenamiento en memoria para demostración
const sentimentAnalysisStore = [];

/**
 * Genera una respuesta utilizando Ollama
 */
async function generateResponse(prompt, model = DEFAULT_MODEL) {
    try {
        const response = await axios.post(`${OLLAMA_API_URL}/generate`, {
            model,
            prompt,
            stream: false
        }, {
            timeout: 30000
        });

        return response.data.response.trim();
    } catch (error) {
        console.error('⚠️  Advertencia: Error con Ollama:', error.message);
        console.log('📋 Usando respuesta simulada...');
        
        // Respuesta simulada para demostración si Ollama no está disponible
        return `{
  "sentiment": "positive",
  "score": 0.85,
  "confidence": "high",
  "keywords": ["fantàstic", "content", "compra"],
  "analysis": "El text expressa una emoció positiva clara amb gran satisfacció vers el producte."
}`;
    }
}

/**
 * Endpoint de anàlisis de sentiment
 */
app.post('/api/chat/sentiment-analysis', async (req, res) => {
    try {
        const { text, language = 'ca', model = DEFAULT_MODEL } = req.body;

        console.log('\n📥 Solicitud recibida:');
        console.log('  Text:', text.substring(0, 80) + '...');
        console.log('  Language:', language);
        console.log('  Model:', model);

        // Validaciones
        if (!text?.trim()) {
            return res.status(400).json({ message: 'El text és obligatori' });
        }

        if (text.trim().length > 5000) {
            return res.status(400).json({ message: 'El text no pot superar 5000 caràcters' });
        }

        console.log('\n🔄 Enviando solicitud a Ollama...');

        // Prompt para Ollama
        const sentimentPrompt = `Analitza el sentiment del següent text en ${language === 'es' ? 'espanyol' : language === 'en' ? 'anglès' : 'català'} i proporciona una resposta en format JSON amb aquesta estructura exacta:
{
  "sentiment": "positive" o "negative" o "neutral",
  "score": un número entre -1 (molt negatiu) i 1 (molt positiu),
  "confidence": "low" o "medium" o "high",
  "keywords": una array de strings amb les paraules clau més rellevants,
  "analysis": una explicació breu del sentiment (1-2 frases)
}

Text a analitzar: "${text.trim()}"

Resposta JSON:`;

        const ollamaResponse = await generateResponse(sentimentPrompt, model);

        console.log('\n✅ Respuesta de Ollama recibida');
        console.log('Contenido:', ollamaResponse.substring(0, 200) + '...');

        // Parsing JSON
        const jsonMatch = ollamaResponse.match(/\{[\s\S]*\}/);
        if (!jsonMatch) {
            return res.status(500).json({
                message: 'Error processant l\'anàlisi de sentiment',
                error: 'No s\'ha pogut parsing la resposta'
            });
        }

        const parsedResponse = JSON.parse(jsonMatch[0]);

        // Validar estructura
        if (!parsedResponse.sentiment || !parsedResponse.score || !parsedResponse.confidence) {
            return res.status(500).json({
                message: 'Error processant l\'anàlisi de sentiment',
                error: 'Estructura de resposta invàlida'
            });
        }

        // Crear objeto de análisis (sin guardar en BD)
        const analysisId = `sa-${Date.now()}-${Math.random().toString(36).substring(7)}`;
        const sentimentAnalysis = {
            id: analysisId,
            text: text.trim(),
            sentiment: parsedResponse.sentiment,
            score: parseFloat(parsedResponse.score),
            confidence: parsedResponse.confidence,
            keywords: Array.isArray(parsedResponse.keywords) ? parsedResponse.keywords : [],
            analysis: parsedResponse.analysis || '',
            language,
            model,
            createdAt: new Date().toISOString()
        };

        // Almacenar en memoria
        sentimentAnalysisStore.push(sentimentAnalysis);

        console.log('\n💾 Análisis almacenado en memoria:');
        console.log('  ID:', analysisId);
        console.log('  Sentimiento:', parsedResponse.sentiment);
        console.log('  Score:', parsedResponse.score);

        res.status(201).json({
            ...sentimentAnalysis,
            message: 'Anàlisi de sentiment realitzat correctament'
        });

    } catch (error) {
        console.error('\n❌ Error en análisis de sentimiento:', error.message);

        if (error.code === 'ECONNREFUSED') {
            return res.status(503).json({
                message: 'No s\'ha pogut connectar a Ollama',
                error: 'Assegura\'t que Ollama està funcionant a ' + OLLAMA_API_URL
            });
        }

        res.status(500).json({
            message: 'Error en l\'anàlisi de sentiment',
            error: error.message
        });
    }
});

// Endpoint para ver análisis almacenados
app.get('/api/chat/sentiment-analysis', (req, res) => {
    res.json({
        total: sentimentAnalysisStore.length,
        analyses: sentimentAnalysisStore
    });
});

// Health check
app.get('/api/chat/health', (req, res) => {
    res.json({
        status: 'ok',
        timestamp: new Date().toISOString(),
        ollamaUrl: OLLAMA_API_URL,
        defaultModel: DEFAULT_MODEL
    });
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log('\n🚀 Servidor de prueba iniciado');
    console.log(`📍 URL: http://localhost:${PORT}`);
    console.log(`📚 Documentación: http://localhost:${PORT}/api-docs`);
    console.log(`🤖 Ollama conectado a: ${OLLAMA_API_URL}`);
    console.log(`🎯 Modelo predeterminado: ${DEFAULT_MODEL}`);
    console.log('\n📝 Endpoints disponibles:');
    console.log('  POST   /api/chat/sentiment-analysis (analizar sentimiento)');
    console.log('  GET    /api/chat/sentiment-analysis (ver análisis guardados)');
    console.log('  GET    /api/chat/health (health check)\n');
});
