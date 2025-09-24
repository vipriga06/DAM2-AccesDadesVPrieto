package com.project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PR113append {

    public static void main(String[] args) {
        String camiFitxer = System.getProperty("user.dir") + "/data/frasesMatrix.txt";
        try {
            afegirFrases(camiFitxer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mètode que afegeix exactament dues frases al final
    // i garanteix només una línia en blanc final
    public static void afegirFrases(String camiFitxer) throws IOException {
        List<String> frases = Arrays.asList(
                "I can only show you the door",
                "You're the one that has to walk through it"
        );

        Path path = Path.of(camiFitxer);
        List<String> liniesExistents = new ArrayList<>();

        // Llegir el fitxer si existeix
        if (Files.exists(path)) {
            liniesExistents = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

            // Eliminar **totes** les línies buides finals
            while (!liniesExistents.isEmpty() && liniesExistents.get(liniesExistents.size() - 1).isEmpty()) {
                liniesExistents.remove(liniesExistents.size() - 1);
            }
        }

        // Afegir les noves frases
        liniesExistents.addAll(frases);

        // Combinar tot en un sol contingut amb salts de línia
        StringBuilder contingut = new StringBuilder();
        for (String linia : liniesExistents) {
            contingut.append(linia).append("\n");
        }
        contingut.append("\n"); // només una línia en blanc final

        // Escriure tot al fitxer
        Files.writeString(path, contingut.toString(), StandardCharsets.UTF_8);
    }
}
