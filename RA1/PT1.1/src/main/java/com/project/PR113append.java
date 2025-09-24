package com.project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PR113append {

    public static void afegirFrases(String camiFitxer) throws IOException {
        List<String> frases = Arrays.asList(
                "I can only show you the door",
                "You're the one that has to walk through it"
        );

        Path path = Path.of(camiFitxer);
        List<String> liniesExistents = new ArrayList<>();

        if (Files.exists(path)) {
            liniesExistents = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

            // Eliminar totes les línies buides finals
            while (!liniesExistents.isEmpty() && liniesExistents.get(liniesExistents.size() - 1).isEmpty()) {
                liniesExistents.remove(liniesExistents.size() - 1);
            }
        }

        // Afegim només les frases noves
        liniesExistents.addAll(frases);

        // Afegim una única línia en blanc final
        liniesExistents.add(""); // només una línia en blanc

        // Escriure tot al fitxer
        Files.write(path, liniesExistents, StandardCharsets.UTF_8);
    }
}
