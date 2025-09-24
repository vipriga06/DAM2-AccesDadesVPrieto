package com.project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PR113sobreescriu {

    public static void main(String[] args) {
        String camiFitxer = System.getProperty("user.dir") + "/data/frasesMatrix.txt";
        escriureFrases(camiFitxer);
    }

    public static void escriureFrases(String camiFitxer) {
        String[] frases = {
            "I can only show you the door",
            "You're the one that has to walk through it"
        };

        try {
            // Escriure frases amb salt de línia, però sense newline extra al final
            StringBuilder sb = new StringBuilder();
            for (String frase : frases) {
                sb.append(frase).append("\n");
            }
            sb.append(""); // última línia buida sense newline després

            Files.writeString(Path.of(camiFitxer), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error escrivint al fitxer: " + e.getMessage());
        }
    }
}
