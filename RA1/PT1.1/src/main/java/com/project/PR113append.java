package com.project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PR113append {

    public static void main(String[] args) {
        String camiFitxer = System.getProperty("user.dir") + "/data/frasesMatrix.txt";
        try {
            afegirFrases(camiFitxer);
            afegirFrases(camiFitxer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   public static void afegirFrases(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write("I can only show you the door");
            writer.newLine();
            writer.write("You're the one that has to walk through it");
            writer.newLine();
        }
    }
}
