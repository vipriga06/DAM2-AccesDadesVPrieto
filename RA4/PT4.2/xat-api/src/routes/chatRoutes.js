const express = require('express');
const router = express.Router();
const { registerPrompt, getConversation, listOllamaModels, analyzeSentiment } = require('../controllers/chatController');

/**
 * @swagger
 * /api/chat/prompt:
 *   post:
 *     summary: Crear un nou prompt o afegir-lo a una conversa existent
 *     tags: [Prompts]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               conversationId:
 *                 type: string
 *                 format: uuid
 *                 description: ID de la conversa (opcional)
 *               prompt:
 *                 type: string
 *                 description: Text del prompt
 *                 default: Diga'm el nom d'una bona cançó composada en els darrers 10 anys amb cantant i canço no massivament coneguts
 *               model:
 *                 type: string
 *                 description: Model d'Ollama a utilitzar
 *                 default: qwen2.5vl:7b
 *               stream:
 *                 type: boolean
 *                 description: Indica si la resposta ha de ser en streaming
 *                 default: false
 *     responses:
 *       201:
 *         description: Prompt registrat correctament
 *       400:
 *         description: Dades invàlides
 *       404:
 *         description: Conversa no trobada
 */
router.post('/prompt', registerPrompt);

/**
 * @swagger
 * /api/chat/conversation/{id}:
 *   get:
 *     summary: Obtenir una conversa per ID
 *     tags: [Conversations]
 *     parameters:
 *       - in: path
 *         name: id
 *         schema:
 *           type: string
 *         required: true
 *         description: UUID de la conversa
 *     responses:
 *       200:
 *         description: Conversa trobada
 *       404:
 *         description: Conversa no trobada
 */
router.get('/conversation/:id', getConversation);

/**
 * @swagger
 * /api/chat/models:
 *   get:
 *     summary: Llistar models disponibles a Ollama
 *     tags: [Chat]
 *     responses:
 *       200:
 *         description: Llista de models disponibles
 *       500:
 *         description: Error al recuperar models
 */
router.get('/models', listOllamaModels);

/**
 * @swagger
 * /api/chat/sentiment-analysis:
 *   post:
 *     summary: Realitzar anàlisi de sentiment d'un text
 *     tags: [Sentiment Analysis]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               text:
 *                 type: string
 *                 description: Text a analitzar (obligatori, màx 5000 caràcters)
 *                 example: "Aquest producte és fantàstic! Estic molt content amb la compra."
 *               language:
 *                 type: string
 *                 description: Idioma del text (ca/es/en)
 *                 default: ca
 *                 example: ca
 *               model:
 *                 type: string
 *                 description: Model d'Ollama a utilitzar
 *                 default: qwen2.5vl:7b
 *                 example: qwen2.5vl:7b
 *             required:
 *               - text
 *     responses:
 *       201:
 *         description: Anàlisi de sentiment completat correctament
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 id:
 *                   type: string
 *                   format: uuid
 *                 text:
 *                   type: string
 *                 sentiment:
 *                   type: string
 *                   enum: [positive, negative, neutral]
 *                 score:
 *                   type: number
 *                   minimum: -1
 *                   maximum: 1
 *                 confidence:
 *                   type: string
 *                   enum: [low, medium, high]
 *                 keywords:
 *                   type: array
 *                   items:
 *                     type: string
 *                 analysis:
 *                   type: string
 *                 language:
 *                   type: string
 *                 model:
 *                   type: string
 *                 createdAt:
 *                   type: string
 *                   format: date-time
 *       400:
 *         description: Dades invàlides (text buit o massa llarg)
 *       500:
 *         description: Error processant l'anàlisi de sentiment
 */
router.post('/sentiment-analysis', analyzeSentiment);

module.exports = router;
