package com.project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PR113sobreescriu {

    public static void main(String[] args) {
        // Definir el camí del fitxer dins del directori "data"
        String camiFitxer = System.getProperty("user.dir") + "/data/frasesMatrix.txt";
        escriureFrases(camiFitxer);
    }

    // Mètode que sobreescriu el fitxer amb exactament dues frases
    // i una única línia en blanc final
    public static void escriureFrases(String camiFitxer) {
        String[] frases = {
                "I can only show you the door",
                "You're the one that has to walk through it"
        };

        try {
            // Combinar les frases en un contingut únic amb salts de línia
            StringBuilder contingut = new StringBuilder();
            for (String frase : frases) {
                contingut.append(frase).append("\n");
            }
            contingut.append("\n"); // només una línia en blanc final

            // Escriure tot el contingut al fitxer, sobrescrivint
            Files.writeString(Path.of(camiFitxer), contingut.toString(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            System.err.println("Error escrivint al fitxer: " + e.getMessage());
        }
    }
}
