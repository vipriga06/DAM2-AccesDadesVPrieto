package com.project.exemples;

import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Headers;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExempleServidor {

    public static void main(String[] args) {
        // Configuració del servidor Undertow
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost") // Escoltar al port 8080
                // Ús de BlockingHandler per permetre I/O bloquejant
                .setHandler(new BlockingHandler(exchange -> {
                    // Permetre només peticions POST
                    if (exchange.getRequestMethod().toString().equalsIgnoreCase("POST")) {
                        // Establir l'encapçalament de la resposta
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                        // Llegir el contingut del cos de la petició
                        try {
                            // Llegir tot el cos de la petició
                            String requestBody = new String(exchange.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            System.out.println("Rebut JSON: " + requestBody);

                            // NOVA PART: Desa la petició rebuda com a fitxer JSON
                            try {
                                // Crea el directori si no existeix
                                File dir = new File("data/requests");
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                // Nom únic amb data i hora
                                String filename = "request_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")) + ".json";
                                File file = new File(dir, filename);
                                try (FileWriter fw = new FileWriter(file)) {
                                    fw.write(requestBody);
                                }
                                System.out.println("Petició desada a " + file.getAbsolutePath());
                            } catch (IOException ex) {
                                System.out.println("No s'ha pogut desar la petició a fitxer: " + ex.getMessage());
                            }
                            // Fi de la NOVA PART

                            // Convertir el JSON rebut en un objecte JsonObject
                            try (JsonReader jsonReader = Json.createReader(new StringReader(requestBody))) {
                                JsonObject personaJson = jsonReader.readObject();

                                // Recuperar les dades del JSON
                                String nom = personaJson.getString("nom");
                                int anyNaixement = personaJson.getInt("anyNaixement");

                                // Calcular l'any en què la persona complirà 120 anys
                                int any120 = anyNaixement + 120;

                                // Crear la resposta JSON
                                JsonObject responseJson = Json.createObjectBuilder()
                                        .add("nom", nom)
                                        .add("anyComplira120", any120)
                                        .build();

                                // Convertir la resposta JsonObject en String
                                StringWriter stringWriter = new StringWriter();
                                try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                                    jsonWriter.write(responseJson);
                                }
                                String resposta = stringWriter.toString();

                                // Enviar la resposta al client
                                exchange.getResponseSender().send(resposta);
                            }
                        } catch (Exception e) {
                            // En cas d'error, enviar un missatge d'error al client
                            exchange.setStatusCode(500);
                            exchange.getResponseSender().send("{\"error\": \"Error processant la petició.\"}");
                            e.printStackTrace();
                        }
                    } else {
                        // Si no és una petició POST, indicar que el mètode no està permès
                        exchange.setStatusCode(405); // Mètode no permès
                        exchange.getResponseSender().send("{\"error\": \"Només es permeten peticions POST.\"}");
                    }
                }))
                .build();

        // Arrencar el servidor
        server.start();
        System.out.println("Servidor en execució a http://localhost:8080");
    }
}