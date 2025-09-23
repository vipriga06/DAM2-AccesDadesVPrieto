package com.project;

public class PR113sobreescriu {

    public static void main(String[] args) {
        // Definir el camí del fitxer dins del directori "data"
        String camiFitxer = System.getProperty("user.dir") + "/data/frasesMatrix.txt";

        // Crida al mètode que escriu les frases sobreescrivint el fitxer
        escriureFrases(camiFitxer);
    }

    // Mètode que escriu les frases sobreescrivint el fitxer amb UTF-8 i línia en blanc final
    public static void escriureFrases(String camiFitxer) {
        String[] frases = {
            "Matrix és a tot arreu.",
            "Segueix el conill blanc.",
            "Pastilla vermella o blava?",
            "Benvingut al món real.",
            "No hi ha cullera."
        };

        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(
                java.nio.file.Paths.get(camiFitxer),
                java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {

            for (String frase : frases) {
                writer.write(frase);
                writer.newLine();
            }
            writer.newLine(); // línia en blanc final

        } catch (java.io.IOException e) {
            System.err.println("Error escrivint al fitxer: " + e.getMessage());
        }
    }
}
