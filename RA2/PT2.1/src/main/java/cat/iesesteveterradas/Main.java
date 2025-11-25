package cat.iesesteveterradas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Classe principal que executa l'aplicació PR210Honor.
 */
public class Main {

    public static void main(String[] args) {
        PR210Honor.main(args);
    }

    /**
     * Retorna el path al fitxer de bones pràctiques.
     */
    public static Path obtenirPathFitxer() {
        return Paths.get(System.getProperty("user.dir"), "data", "bones_practiques_programacio.txt");
    }

    /**
     * Llegeix el contingut d'un fitxer i retorna una llista de línies.
     */
    public static List<String> readFileContent(Path filePath) throws IOException {
        return Files.readAllLines(filePath);
    }
}
