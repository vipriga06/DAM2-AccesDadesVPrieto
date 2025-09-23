package com.project;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PR110ReadFile {

    public static void main(String[] args) {
        String camiFitxer = System.getProperty("user.dir") + "/data/GestioTasques.java";
        llegirIMostrarFitxer(camiFitxer);  // Només cridem a la funció amb la ruta del fitxer
    }

    // Funció que llegeix el fitxer i mostra les línies amb numeració
    public static void llegirIMostrarFitxer(String camiFitxer) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(camiFitxer), StandardCharsets.UTF_8))) {
            String linia;
            int num_linia = 0;
            while ((linia = br.readLine()) != null) {
                System.out.println(num_linia + ": " + linia);
                num_linia++;
            }
        } catch (IOException e) {
            System.out.println("Error llegint el fitxer: " + e.getMessage());
        }

    }
}
