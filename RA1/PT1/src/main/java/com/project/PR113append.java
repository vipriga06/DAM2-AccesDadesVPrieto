package com.project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class PR113append {

    public static void afegirFrases(String camiFitxer) throws IOException {
        // Creem el directori "data" si no existeix
        File carpeta = new File(System.getProperty("user.dir") + "/data");
        if (!carpeta.exists()) {
            carpeta.mkdir();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(camiFitxer, true), StandardCharsets.UTF_8))) {

            writer.write("I can only show you the door");
            writer.newLine();
            writer.write("You're the one that has to walk through it");
            writer.newLine();
            writer.newLine(); // línia en blanc final
        }
    }
    public static void main(String[] args) {
        String camiFitxer = System.getProperty("user.dir") + "/data/frasesMatrix.txt";
        try {
            afegirFrases(camiFitxer);
            System.out.println("Frases afegides correctament a: " + camiFitxer);
        } catch (IOException e) {
            System.err.println("Error en escriure el fitxer: " + e.getMessage());
        }
    }
}
