package cat.iesesteveterradas;

import cat.iesesteveterradas.utils.UtilsSQLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


public class PR210Honor {

    private static final Logger logger = LoggerFactory.getLogger(PR210Honor.class);
    private static final String DB_PATH = System.getProperty("user.dir") + "/data/forhonor.db";

    public static void main(String[] args) {
        logger.info("Iniciant aplicació PR210Honor...");

        try {
            // Verificar si la BD existeix, si no, inicialitzar-la
            File dbFile = new File(DB_PATH);
            if (!dbFile.exists()) {
                logger.info("La base de dades no existeix. Creant-la...");
                initDatabase();
            } else {
                logger.info("Base de dades trobada a {}", DB_PATH);
            }

            // Menú principal
            try (Connection conn = UtilsSQLite.connect(DB_PATH);
                 Scanner scanner = new Scanner(System.in)) {
                
                boolean running = true;
                while (running) {
                    mostrarMenu();
                    System.out.print("Selecciona una opció: ");
                    
                    if (!scanner.hasNextInt()) {
                        scanner.nextLine();
                        System.out.println("Si us plau, introdueix un número vàlid.\n");
                        continue;
                    }
                    
                    int opcio = scanner.nextInt();
                    scanner.nextLine();

                    switch (opcio) {
                        case 1 -> mostrarTaula(conn, scanner);
                        case 2 -> mostrarPersonatgesPerFaccio(conn, scanner);
                        case 3 -> mostrarMillorAtacantPerFaccio(conn, scanner);
                        case 4 -> mostrarMillorDefensorPerFaccio(conn, scanner);
                        case 5 -> {
                            System.out.println("Sortint de l'aplicació. Adéu!");
                            logger.info("Aplicació tancada per l'usuari.");
                            running = false;
                        }
                        default -> System.out.println("Opció no vàlida. Si us plau, tria entre 1 i 5.\n");
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Error de base de dades: {}", e.getMessage(), e);
            System.out.println("Error en la base de dades: " + e.getMessage());
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n===== FOR HONOR - GESTIÓ DE BD =====");
        System.out.println("1. Mostrar una taula");
        System.out.println("2. Mostrar personatges per facció");
        System.out.println("3. Mostrar el millor atacant per facció");
        System.out.println("4. Mostrar el millor defensor per facció");
        System.out.println("5. Sortir");
        System.out.println("====================================");
    }

    public static void initDatabase() throws SQLException {
        logger.info("Inicialitzant base de dades...");
        
        // Assegurar que existeix el directori data
        File dataDir = new File(System.getProperty("user.dir") + "/data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            logger.debug("Directori 'data' creat.");
        }

        try (Connection conn = UtilsSQLite.connect(DB_PATH)) {
            // Crear taula Faccio
            String createFaccio = """
                CREATE TABLE IF NOT EXISTS Faccio (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom VARCHAR(15) NOT NULL,
                    resum VARCHAR(500)
                );
                """;
            UtilsSQLite.queryUpdate(conn, createFaccio);
            logger.info("Taula Faccio creada correctament.");

            // Crear taula Personatge
            String createPersonatge = """
                CREATE TABLE IF NOT EXISTS Personatge (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom VARCHAR(15) NOT NULL,
                    atac REAL,
                    defensa REAL,
                    idFaccio INTEGER,
                    FOREIGN KEY (idFaccio) REFERENCES Faccio(id)
                );
                """;
            UtilsSQLite.queryUpdate(conn, createPersonatge);
            logger.info("Taula Personatge creada correctament.");

            // Inserir dades de faccions
            logger.info("Inserint dades de faccions...");
            UtilsSQLite.queryUpdatePS(conn, 
                "INSERT INTO Faccio (nom, resum) VALUES (?, ?)",
                "Cavallers", "Though seen as a single group, the Knights are hardly unified. There are many Legions in Ashfeld, the most prominent being The Iron Legion.");
            UtilsSQLite.queryUpdatePS(conn,
                "INSERT INTO Faccio (nom, resum) VALUES (?, ?)",
                "Vikings", "The Vikings are a loose coalition of hundreds of clans and tribes, the most powerful being The Warborn.");
            UtilsSQLite.queryUpdatePS(conn,
                "INSERT INTO Faccio (nom, resum) VALUES (?, ?)",
                "Samurais", "The Samurai are the most unified of the three factions, though this does not say much as the Daimyos were often battling each other for dominance.");

            // Inserir dades de personatges
            logger.info("Inserint dades de personatges...");
            // Cavallers (idFaccio = 1)
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Warden", 1.0, 3.0, 1);
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Conqueror", 2.0, 2.0, 1);
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Peacekeep", 2.0, 3.0, 1);
            
            // Vikings (idFaccio = 2)
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Raider", 3.0, 3.0, 2);
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Warlord", 2.0, 2.0, 2);
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Berserker", 1.0, 1.0, 2);
            
            // Samurais (idFaccio = 3)
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Kensei", 3.0, 2.0, 3);
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Shugoki", 2.0, 1.0, 3);
            UtilsSQLite.queryUpdatePS(conn, "INSERT INTO Personatge (nom, atac, defensa, idFaccio) VALUES (?, ?, ?, ?)", "Orochi", 3.0, 2.0, 3);

            logger.info("Base de dades inicialitzada correctament amb totes les dades.");
        }
    }

    private static void mostrarTaula(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nQuina taula vols visualitzar?");
        System.out.println("1. Faccio");
        System.out.println("2. Personatge");
        System.out.print("Selecciona: ");
        
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            System.out.println("Opció no vàlida.");
            return;
        }
        
        int opcio = scanner.nextInt();
        scanner.nextLine();

        switch (opcio) {
            case 1 -> {
                System.out.println("\n=== TAULA FACCIO ===");
                System.out.printf("%-5s | %-15s | %s%n", "ID", "NOM", "RESUM");
                System.out.println("-".repeat(80));
                
                try (ResultSet rs = UtilsSQLite.querySelect(conn, "SELECT * FROM Faccio ORDER BY id")) {
                    while (rs.next()) {
                        String resum = rs.getString("resum");
                        if (resum != null && resum.length() > 50) {
                            resum = resum.substring(0, 50) + "...";
                        }
                        System.out.printf("%-5d | %-15s | %s%n", 
                            rs.getInt("id"), 
                            rs.getString("nom"), 
                            resum);
                    }
                }
            }
            case 2 -> {
                System.out.println("\n=== TAULA PERSONATGE ===");
                System.out.printf("%-5s | %-15s | %-8s | %-8s | %-10s%n", "ID", "NOM", "ATAC", "DEFENSA", "ID_FACCIO");
                System.out.println("-".repeat(60));
                
                try (ResultSet rs = UtilsSQLite.querySelect(conn, "SELECT * FROM Personatge ORDER BY id")) {
                    while (rs.next()) {
                        System.out.printf("%-5d | %-15s | %-8.1f | %-8.1f | %-10d%n",
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getDouble("atac"),
                            rs.getDouble("defensa"),
                            rs.getInt("idFaccio"));
                    }
                }
            }
            default -> System.out.println("Opció no vàlida.");
        }
    }

    private static int seleccionarFaccio(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nFaccions disponibles:");
        try (ResultSet rs = UtilsSQLite.querySelect(conn, "SELECT id, nom FROM Faccio ORDER BY id")) {
            while (rs.next()) {
                System.out.printf("  %d. %s%n", rs.getInt("id"), rs.getString("nom"));
            }
        }
        
        System.out.print("Selecciona una facció: ");
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            return -1;
        }
        
        int idFaccio = scanner.nextInt();
        scanner.nextLine();
        
        // Verificar que la facció existeix
        try (ResultSet rs = UtilsSQLite.querySelectPS(conn, "SELECT nom FROM Faccio WHERE id = ?", idFaccio)) {
            if (!rs.next()) {
                System.out.println("Facció no trobada.");
                return -1;
            }
        }
        
        return idFaccio;
    }

    private static void mostrarPersonatgesPerFaccio(Connection conn, Scanner scanner) throws SQLException {
        int idFaccio = seleccionarFaccio(conn, scanner);
        if (idFaccio == -1) return;

        String sql = """
            SELECT p.id, p.nom, p.atac, p.defensa, f.nom AS nomFaccio
            FROM Personatge p
            JOIN Faccio f ON p.idFaccio = f.id
            WHERE p.idFaccio = ?
            ORDER BY p.nom
            """;

        try (ResultSet rs = UtilsSQLite.querySelectPS(conn, sql, idFaccio)) {
            String nomFaccio = null;
            boolean first = true;
            
            while (rs.next()) {
                if (first) {
                    nomFaccio = rs.getString("nomFaccio");
                    System.out.printf("%n=== PERSONATGES DE LA FACCIÓ: %s ===%n", nomFaccio);
                    System.out.printf("%-5s | %-15s | %-8s | %-8s%n", "ID", "NOM", "ATAC", "DEFENSA");
                    System.out.println("-".repeat(45));
                    first = false;
                }
                
                System.out.printf("%-5d | %-15s | %-8.1f | %-8.1f%n",
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("atac"),
                    rs.getDouble("defensa"));
            }
            
            if (first) {
                System.out.println("No s'han trobat personatges per aquesta facció.");
            }
        }
    }

    private static void mostrarMillorAtacantPerFaccio(Connection conn, Scanner scanner) throws SQLException {
        int idFaccio = seleccionarFaccio(conn, scanner);
        if (idFaccio == -1) return;

        String sql = """
            SELECT p.nom, p.atac, p.defensa, f.nom AS nomFaccio
            FROM Personatge p
            JOIN Faccio f ON p.idFaccio = f.id
            WHERE p.idFaccio = ?
            ORDER BY p.atac DESC
            LIMIT 1
            """;

        try (ResultSet rs = UtilsSQLite.querySelectPS(conn, sql, idFaccio)) {
            if (rs.next()) {
                String nomFaccio = rs.getString("nomFaccio");
                String nomPersonatge = rs.getString("nom");
                double atac = rs.getDouble("atac");
                double defensa = rs.getDouble("defensa");

                System.out.printf("%n=== MILLOR ATACANT DE LA FACCIÓ: %s ===%n", nomFaccio);
                System.out.printf("Personatge: %s%n", nomPersonatge);
                System.out.printf("Atac: %.1f%n", atac);
                System.out.printf("Defensa: %.1f%n", defensa);
            } else {
                System.out.println("No s'han trobat personatges per aquesta facció.");
            }
        }
    }

    private static void mostrarMillorDefensorPerFaccio(Connection conn, Scanner scanner) throws SQLException {
        int idFaccio = seleccionarFaccio(conn, scanner);
        if (idFaccio == -1) return;

        String sql = """
            SELECT p.nom, p.atac, p.defensa, f.nom AS nomFaccio
            FROM Personatge p
            JOIN Faccio f ON p.idFaccio = f.id
            WHERE p.idFaccio = ?
            ORDER BY p.defensa DESC
            LIMIT 1
            """;

        try (ResultSet rs = UtilsSQLite.querySelectPS(conn, sql, idFaccio)) {
            if (rs.next()) {
                String nomFaccio = rs.getString("nomFaccio");
                String nomPersonatge = rs.getString("nom");
                double atac = rs.getDouble("atac");
                double defensa = rs.getDouble("defensa");

                System.out.printf("%n=== MILLOR DEFENSOR DE LA FACCIÓ: %s ===%n", nomFaccio);
                System.out.printf("Personatge: %s%n", nomPersonatge);
                System.out.printf("Atac: %.1f%n", atac);
                System.out.printf("Defensa: %.1f%n", defensa);
            } else {
                System.out.println("No s'han trobat personatges per aquesta facció.");
            }
        }
    }
}
