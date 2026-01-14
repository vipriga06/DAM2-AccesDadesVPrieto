const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const SentimentAnalysis = sequelize.define('SentimentAnalysis', {
    id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true
    },
    text: {
        type: DataTypes.TEXT,
        allowNull: false,
        validate: {
            notEmpty: true,
            len: [1, 5000]
        }
    },
    sentiment: {
        type: DataTypes.ENUM('positive', 'negative', 'neutral'),
        allowNull: false
    },
    score: {
        type: DataTypes.FLOAT,
        allowNull: false,
        validate: {
            min: -1,
            max: 1
        }
    },
    confidence: {
        type: DataTypes.ENUM('low', 'medium', 'high'),
        allowNull: false
    },
    keywords: {
        type: DataTypes.JSON,
        allowNull: true,
        defaultValue: []
    },
    analysis: {
        type: DataTypes.TEXT,
        allowNull: true
    },
    language: {
        type: DataTypes.STRING,
        defaultValue: 'ca',
        allowNull: false
    },
    model: {
        type: DataTypes.STRING,
        defaultValue: process.env.CHAT_API_OLLAMA_MODEL,
        allowNull: false
    },
    createdAt: {
        type: DataTypes.DATE,
        defaultValue: DataTypes.NOW
    },
    updatedAt: {
        type: DataTypes.DATE,
        defaultValue: DataTypes.NOW
    }
});

module.exports = SentimentAnalysis;
