package com.project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;


public class PR115cp {

    // Mètode públic estàtic que simula la comanda cp
    public static void copiarArxiu(String rutaOrigen, String rutaDesti) throws IOException {
        Path origen = Path.of(rutaOrigen);
        Path desti = Path.of(rutaDesti);

        if (!Files.exists(origen) || !Files.isRegularFile(origen)) {
            throw new IOException("L'arxiu d'origen no existeix o no és un fitxer de text.");
        }
        // el desti es la ruta de la carpeta on es vol copiar el fitxer
        if (Files.isDirectory(desti)) {
            desti = desti.resolve(origen.getFileName());
        }

        if (Files.exists(desti)) {
            Files.delete(desti);
        }

        List<String> linies = Files.readAllLines(origen, StandardCharsets.UTF_8);
        Files.write(desti, linies, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        String origen;
        String desti;


        if (args.length == 2) {
            origen = args[0];
            desti = args[1];
        } else {
            System.out.print("Introdueix la ruta del fitxer origen: ");
            origen = sc.nextLine();
            System.out.print("Introdueix la ruta del fitxer destí: ");
            desti = sc.nextLine();
        }

        try {
             
            copiarArxiu(origen, desti);
            System.out.println("Còpia realitzada correctament de " + origen + " a " + desti);
        } catch (IOException e) {
            System.err.println("Error en copiar l'arxiu: " + e.getMessage());
        }
        sc.close(); 
    }
}
