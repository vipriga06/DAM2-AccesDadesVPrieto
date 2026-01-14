#!/usr/bin/env node

/**
 * Test directo del endpoint sentiment-analysis
 * Sin necesidad de MySQL ni servidor corriendo
 */

const axios = require('axios');

const BASE_URL = 'http://127.0.0.1:3000';

async function testSentimentAnalysis() {
    try {
        console.log('🚀 Iniciando prueba del endpoint sentiment-analysis...\n');

        const testCases = [
            {
                name: 'Test 1: Sentimiento Positivo',
                data: {
                    text: 'Aquest producte és fantàstic! Estic molt content amb la compra.',
                    language: 'ca'
                }
            },
            {
                name: 'Test 2: Sentimiento Negativo',
                data: {
                    text: 'Això és horrible. Totalment decepcionant i mal funcionament.',
                    language: 'ca'
                }
            },
            {
                name: 'Test 3: Sentimiento Neutral',
                data: {
                    text: 'El producte té les característiques que es van prometre.',
                    language: 'ca'
                }
            }
        ];

        for (const test of testCases) {
            console.log(`\n${test.name}`);
            console.log('━'.repeat(60));
            console.log('📤 Enviando:', JSON.stringify(test.data, null, 2));

            try {
                const response = await axios.post(
                    `${BASE_URL}/api/chat/sentiment-analysis`,
                    test.data,
                    { timeout: 60000 }
                );

                console.log('\n✅ Respuesta exitosa (HTTP 201):');
                console.log(JSON.stringify(response.data, null, 2));
            } catch (error) {
                if (error.response) {
                    console.log(`\n❌ Error HTTP ${error.response.status}:`);
                    console.log(JSON.stringify(error.response.data, null, 2));
                } else if (error.code === 'ECONNREFUSED') {
                    console.log('\n❌ No se pudo conectar al servidor (ECONNREFUSED)');
                    console.log('   Asegúrate de que el servidor esté corriendo en puerto 3000');
                } else {
                    console.log(`\n❌ Error: ${error.message}`);
                }
            }
        }

    } catch (error) {
        console.error('Error fatal:', error.message);
        process.exit(1);
    }
}

testSentimentAnalysis();
