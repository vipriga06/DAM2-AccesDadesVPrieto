package com.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class PR115cp {

    /**
     * Copia un fitxer des de rutaOrigen a rutaDesti.
     * Si el fitxer origen no existeix o no és un fitxer regular, no fa res.
     */
    public static void copiarArxiu(String rutaOrigen, String rutaDesti) throws IOException {
        Path origen = Path.of(rutaOrigen);
        Path desti = Path.of(rutaDesti);

        // Si el fitxer origen no existeix o no és un fitxer regular, simplement sortir
        if (!Files.exists(origen) || !Files.isRegularFile(origen)) {
            System.err.println("Advertència: l'arxiu d'origen no existeix o no és un fitxer regular. No es copiarà res.");
            return;
        }

        // Comprovació si el fitxer destí existeix
        if (Files.exists(desti)) {
            System.out.println("Advertència: l'arxiu de destinació existeix i serà sobreescrit.");
        }

        // Còpia amb reemplaçament si ja existeix
        Files.copy(origen, desti, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void main(String[] args) {
        String origen;
        String desti;

        if (args.length == 2) {
            origen = args[0];
            desti = args[1];
        } else {
            try (Scanner sc = new Scanner(System.in)) {
                System.out.print("Introdueix la ruta del fitxer origen: ");
                origen = sc.nextLine().trim();
                System.out.print("Introdueix la ruta del fitxer destí: ");
                desti = sc.nextLine().trim();
            }
        }

        try {
            copiarArxiu(origen, desti);
            System.out.println("Còpia realitzada correctament de " + origen + " a " + desti);
        } catch (IOException e) {
            System.err.println("Error en copiar l'arxiu: " + e.getMessage());
        }
    }
}
