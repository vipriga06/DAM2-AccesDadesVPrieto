package com.project;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class PR124main {

    private static final int ID_SIZE = 4;
    private static final int NAME_MAX_BYTES = 40;
    private static final int GRADE_SIZE = 4;
    private static final int RECORD_SIZE = ID_SIZE + NAME_MAX_BYTES + GRADE_SIZE;

    private static final int NAME_POS = ID_SIZE;
    private static final int GRADE_POS = NAME_POS + NAME_MAX_BYTES;

    private String filePath;
    private Scanner scanner = new Scanner(System.in);

    public PR124main() {
        this.filePath = System.getProperty("user.dir") + "/data/PR124estudiants.dat";
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        PR124main gestor = new PR124main();
        boolean sortir = false;
        while (!sortir) {
            try {
                gestor.mostrarMenu();
                int opcio = gestor.getOpcioMenu();
                switch (opcio) {
                    case 1 -> gestor.llistarEstudiants();
                    case 2 -> gestor.afegirEstudiant();
                    case 3 -> gestor.consultarNota();
                    case 4 -> gestor.actualitzarNota();
                    case 5 -> sortir = true;
                    default -> System.out.println("Opció no vàlida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Si us plau, introdueix un número vàlid.");
            } catch (IOException e) {
                System.out.println("Error en la manipulació del fitxer: " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\nMenú de Gestió d'Estudiants");
        System.out.println("1. Llistar estudiants");
        System.out.println("2. Afegir nou estudiant");
        System.out.println("3. Consultar nota d'un estudiant");
        System.out.println("4. Actualitzar nota d'un estudiant");
        System.out.println("5. Sortir");
        System.out.print("Selecciona una opció: ");
    }

    private int getOpcioMenu() {
        return Integer.parseInt(scanner.nextLine());
    }

    public void llistarEstudiants() throws IOException {
        llistarEstudiantsFitxer();
    }

    public void afegirEstudiant() throws IOException {
        int registre = demanarRegistre();
        String nom = demanarNom();
        float nota = demanarNota();
        afegirEstudiantFitxer(registre, nom, nota);
    }

    public void consultarNota() throws IOException {
        int registre = demanarRegistre();
        consultarNotaFitxer(registre);
    }

    public void actualitzarNota() throws IOException {
        int registre = demanarRegistre();
        float novaNota = demanarNota();
        actualitzarNotaFitxer(registre, novaNota);
    }

    private int demanarRegistre() {
        System.out.print("Introdueix el número de registre (enter positiu): ");
        int registre = Integer.parseInt(scanner.nextLine());
        if (registre < 0) throw new IllegalArgumentException("El número de registre ha de ser positiu.");
        return registre;
    }

    private String demanarNom() {
        System.out.print("Introdueix el nom (màxim 20 caràcters): ");
        return scanner.nextLine();
    }

    private float demanarNota() {
        System.out.print("Introdueix la nota (0-10): ");
        float nota = Float.parseFloat(scanner.nextLine());
        if (nota < 0 || nota > 10) throw new IllegalArgumentException("Nota fora de rang.");
        return nota;
    }

    private long trobarPosicioRegistre(RandomAccessFile raf, int registreBuscat) throws IOException {
        long numRecords = raf.length() / RECORD_SIZE;
        for (int i = 0; i < numRecords; i++) {
            raf.seek(i * RECORD_SIZE);
            int id = raf.readInt();
            if (id == registreBuscat) return i * RECORD_SIZE;
        }
        return -1;
    }

    public void llistarEstudiantsFitxer() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long numRecords = raf.length() / RECORD_SIZE;
            for (int i = 0; i < numRecords; i++) {
                raf.seek(i * RECORD_SIZE);
                int id = raf.readInt();
                String nom = llegirNom(raf);
                float nota = raf.readFloat();
                System.out.printf(java.util.Locale.US, "Registre: %d, Nom: %s, Nota: %.1f%n", id, nom, nota);
            }
        }
    }

    public void afegirEstudiantFitxer(int registre, String nom, float nota) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            raf.seek(raf.length()); // Afegir al final
            raf.writeInt(registre);
            escriureNom(raf, nom);
            raf.writeFloat(nota);
        }
    }

    public void consultarNotaFitxer(int registre) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No s'ha trobat l'estudiant amb registre: " + registre);
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long pos = trobarPosicioRegistre(raf, registre);
            if (pos == -1) {
                System.out.println("No s'ha trobat l'estudiant amb registre: " + registre);
                return;
            }
            raf.seek(pos + NAME_POS);
            String nom = llegirNom(raf);
            raf.seek(pos + GRADE_POS);
            float nota = raf.readFloat();
            System.out.printf("Registre: %d, Nom: %s, Nota: %.1f%n", registre, nom, nota);
        }
    }

    public void actualitzarNotaFitxer(int registre, float novaNota) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long pos = trobarPosicioRegistre(raf, registre);
            if (pos == -1) return;
            raf.seek(pos + GRADE_POS);
            raf.writeFloat(novaNota);
        }
    }

    private String llegirNom(RandomAccessFile raf) throws IOException {
        byte[] bytes = new byte[NAME_MAX_BYTES];
        raf.readFully(bytes);
        int len = 0;
        for (int i = 0; i < NAME_MAX_BYTES; i++) if (bytes[i] == 0) break; else len++;
        return new String(bytes, 0, len, StandardCharsets.UTF_8);
    }

    private void escriureNom(RandomAccessFile raf, String nom) throws IOException {
        byte[] nomBytes = nom.getBytes(StandardCharsets.UTF_8);
        if (nomBytes.length > NAME_MAX_BYTES) {
            raf.write(nomBytes, 0, NAME_MAX_BYTES); // Truncar si és massa llarg
        } else {
            raf.write(nomBytes);
            raf.write(new byte[NAME_MAX_BYTES - nomBytes.length]); // Padding amb zeros
        }
    }
}
