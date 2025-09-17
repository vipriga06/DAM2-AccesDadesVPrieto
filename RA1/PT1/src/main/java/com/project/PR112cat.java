package com.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;   

public class PR112cat {

    public static void main(String[] args) {
        
        if (args == null || args.length == 0) {
            args = new String[1];
        }

        String input;
        try ( // Demana al usuari que introdueixi la ruta del fitxer com a paràmetre
            Scanner scanner = new Scanner(System.in)) {
            System.out.print("Introdueix la ruta del fitxer: ");
            input = scanner.nextLine();
            scanner.close();
        }
        
        // Agrega el input(ruta) demanada en l'array "args"
        args[0] = input;
        
        // Comprovar que s'ha proporcionat una ruta com a paràmetre
        if (args.length == 0) {
            System.out.println("No s'ha proporcionat cap ruta d'arxiu.");
            return;
        }

        // Obtenir la ruta del fitxer des dels paràmetres
        String rutaArxiu = args[0];
        mostrarContingutArxiu(rutaArxiu);
    }

    // Funció per mostrar el contingut de l'arxiu o el missatge d'error corresponent
    public static void mostrarContingutArxiu(String rutaArxiu) {
        Path path = Paths.get(rutaArxiu);

        

        try {
            if (!path.isAbsolute()) {
                Path absolutePath = path.toAbsolutePath();
                System.out.println("S'ha convertit la ruta relativa a absoluta: " + absolutePath);
                path = absolutePath;
            }
            // Primer, comprova si és una carpeta
            if (Files.isDirectory(path)) {
                System.out.println("El path no correspon a un arxiu, sinó a una carpeta.");
            }
            // Després, comprova si l'arxiu existeix
            else if (Files.exists(path)) {
                // Llavors, llegeix tot el contigut del fitxer i el mostra per consola
                Files.lines(path).forEach(System.out::println);
            }
            // si no existeix llença un error
            else {
                System.out.println("El fitxer no existeix o no és accessible.");
            }
        } catch (IOException e) {
            System.out.println("Error a l'lectura del fitxer: " + e.getMessage());
        }
    }
}
