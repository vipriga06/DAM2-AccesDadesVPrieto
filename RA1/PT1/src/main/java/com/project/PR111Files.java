package com.project;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PR111Files {

    public static void main(String[] args) {
        String camiFitxer = System.getProperty("user.dir") + "/data/pr111";
        gestionarArxius(camiFitxer);
    }

    public static void gestionarArxius(String camiFitxer) {
        Path carpeta = Paths.get(camiFitxer, "myFiles");

        try {
            if (Files.notExists(carpeta)) {
                Files.createDirectories(carpeta);
                System.out.println("Carpeta creada correctament: " + carpeta);
            }

            Path file1 = carpeta.resolve("file1.txt");
            if (Files.notExists(file1)) {
                Files.createFile(file1);
                System.out.println("Arxiu creat: " + file1.getFileName());
            }

            Path file2 = carpeta.resolve("file2.txt");
            Path renamedFile = carpeta.resolve("renamedFile.txt");
            if (Files.notExists(file2) && Files.notExists(renamedFile)) {
                Files.createFile(file2);
                System.out.println("Arxiu creat: " + file2.getFileName());
            }

            if (Files.exists(file2) && Files.notExists(renamedFile)) {
                Files.move(file2, renamedFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Arxiu " + file2.getFileName() + " renombrat a " + renamedFile.getFileName());
            }

            mostrarArxius(carpeta);

            if (Files.exists(file1)) {
                Files.delete(file1);
                System.out.println("Arxiu eliminat: " + file1.getFileName());
            }

            mostrarArxius(carpeta);

        } catch (IOException e) {
            System.out.println("S'ha produït un error: " + e.getMessage());
        }
    }

    private static void mostrarArxius(Path carpeta) throws IOException {
        System.out.println("Els arxius de la carpeta són:");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(carpeta)) {
            boolean buida = true;
            for (Path path : stream) {
                System.out.println("- " + path.getFileName());
                buida = false;
            }
            if (buida) {
                System.out.println("(La carpeta està buida)");
            }
        }
    }
}
