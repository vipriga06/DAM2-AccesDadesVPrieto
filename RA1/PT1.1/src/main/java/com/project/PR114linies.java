package com.project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PR114linies {

    // Mètode que el test espera
    public static void generarNumerosAleatoris(String camiFitxer) throws IOException {
        Path path = Path.of(camiFitxer);

        // Creem la carpeta "data" si no existeix
        Files.createDirectories(path.getParent());

        Random random = new Random();
        List<String> linies = new ArrayList<>();

        // Generem exactament 10 números entre 0 i 99
        for (int i = 0; i < 10; i++) {
            linies.add(String.valueOf(random.nextInt(100)));
        }

        // Escrivim les línies en UTF-8, sense salt extra al final
        Files.write(path, linies, StandardCharsets.UTF_8);
    }

    // Main per executar manualment
    public static void main(String[] args) {
        String camiFitxer = System.getProperty("user.dir") + "/data/numeros.txt";
        try {
            generarNumerosAleatoris(camiFitxer);
            System.out.println("Fitxer generat correctament a: " + camiFitxer);
        } catch (IOException e) {
            System.err.println("Error en escriure el fitxer: " + e.getMessage());
        }
    }
}
