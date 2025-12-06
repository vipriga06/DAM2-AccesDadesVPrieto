package com.project.utils;

import java.util.Scanner;

/**
 * Clase DatabaseSelector para elegir el tipo de base de datos (SQLite o MySQL)
 * Proporciona un menú interactivo al usuario
 */
public class DatabaseSelector {

    /**
     * Muestra un menú para que el usuario seleccione la base de datos
     * @return "sqlite" para SQLite o "mysql" para MySQL
     */
    public static String selectDatabase() {
        Scanner scanner = new Scanner(System.in);
        String selection;

        while (true) {
            System.out.println("\n=== Selecciona la Base de Dades ===");
            System.out.println("1. SQLite (Base de dades local)");
            System.out.println("2. MySQL (Base de dades remota)");
            System.out.print("Selecciona una opció (1 o 2): ");

            selection = scanner.nextLine().trim();

            if ("1".equals(selection)) {
                System.out.println("✓ SQLite seleccionat");
                return "sqlite";
            } else if ("2".equals(selection)) {
                System.out.println("✓ MySQL seleccionat");
                return "mysql";
            } else {
                System.out.println("✗ Opció invàlida. Si us plau, tria 1 o 2.");
            }
        }
    }

    /**
     * Retorna la configuració de Hibernate baseada en la BD seleccionada
     * @param databaseType "sqlite" o "mysql"
     * @return String amb el nom del fitxer de configuració
     */
    public static String getHibernateConfigFile(String databaseType) {
        if ("mysql".equalsIgnoreCase(databaseType)) {
            return "hibernate-mysql.properties";
        }
        return "hibernate.properties"; // SQLite per defecte
    }

    /**
     * Valida que la BD seleccionada sigui vàlida
     * @param databaseType "sqlite" o "mysql"
     * @return true si és vàlida
     */
    public static boolean isValidDatabase(String databaseType) {
        return "sqlite".equalsIgnoreCase(databaseType) || "mysql".equalsIgnoreCase(databaseType);
    }
}
