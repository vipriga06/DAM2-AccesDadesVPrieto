package com.project;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
   public static void main(String[] args) {
        ensureDataFolder();
        Manager.createSessionFactory();

        try {
            Ciutat ciutat1 = Manager.addCiutat("Barcelona", "Espanya", 8020);
            Ciutat ciutat2 = Manager.addCiutat("Vancouver", "Canadà", 98661);
            Ciutat ciutat3 = Manager.addCiutat("Kyoto", "Japó", 5200461);

            List<Ciutada> ciutadans = Arrays.asList(
                Manager.addCiutada("Jordi", "Serra", 34),
                Manager.addCiutada("Maria", "Reverter", 30),
                Manager.addCiutada("Tony", "Happy", 20),
                Manager.addCiutada("Monica", "Mouse", 22),
                Manager.addCiutada("Akira", "Sato", 44),
                Manager.addCiutada("Keiko", "Tanaka", 40)
            );

            assignCitizensToCity(ciutat1, ciutadans.subList(0, 2));
            assignCitizensToCity(ciutat2, ciutadans.subList(2, 4));
            assignCitizensToCity(ciutat3, ciutadans.subList(4, 6));

            printCiutatsAmbCiutadans("Ciutats i ciutadans inicials");

            removeSecondCitizenFromEachCity();
            printCiutatsAmbCiutadans("Després d'esborrar el segon ciutadà de cada ciutat");

            deleteSecondCityIfExists();

            printCollection("Ciutats restants", Ciutat.class, Manager.listCiutats());
            printCollection("Ciutadans restants", Ciutada.class, Manager.listCiutadans());
        } finally {
            Manager.close();
        }
    }

    private static void ensureDataFolder() {
        String basePath = System.getProperty("user.dir") + "/data/";
        File dir = new File(basePath);
        if (!dir.exists() && !dir.mkdirs()) {
            System.out.println("Error creant la carpeta 'data'");
        }
    }

    private static void assignCitizensToCity(Ciutat ciutat, List<Ciutada> ciutadans) {
        for (Ciutada ciutada : ciutadans) {
            Manager.assignCiutadaToCiutat(ciutada.getCiutadaId(), ciutat.getCiutatId());
        }
    }

    private static void removeSecondCitizenFromEachCity() {
        List<Ciutat> ciutats = Manager.listCiutats();
        for (Ciutat ciutat : ciutats) {
            List<Ciutada> ciutadans = Manager.listCiutadansByCiutat(ciutat.getCiutatId());
            if (ciutadans.size() >= 2) {
                Ciutada segon = ciutadans.get(1);
                Manager.delete(Ciutada.class, segon.getCiutadaId());
            }
        }
    }

    private static void deleteSecondCityIfExists() {
        List<Ciutat> ciutats = Manager.listCiutats();
        if (ciutats.size() >= 2) {
            Manager.delete(Ciutat.class, ciutats.get(1).getCiutatId());
        }
    }

    private static void printCiutatsAmbCiutadans(String title) {
        System.out.println("\n" + title);
        List<Ciutat> ciutats = Manager.listCiutats();
        if (ciutats.isEmpty()) {
            System.out.println("(No hi ha ciutats registrades)");
            return;
        }
        for (Ciutat ciutat : ciutats) {
            System.out.println("- " + ciutat.getNom() + " (id=" + ciutat.getCiutatId() + ")");
            List<Ciutada> ciutadans = Manager.listCiutadansByCiutat(ciutat.getCiutatId());
            if (ciutadans.isEmpty()) {
                System.out.println("  Sense ciutadans associats");
                continue;
            }
            for (Ciutada ciutada : ciutadans) {
                System.out.println("  * " + ciutada.getNom() + " " + ciutada.getCognom() +
                    " (id=" + ciutada.getCiutadaId() + ")");
            }
        }
    }

    private static void printCollection(String title, Class<?> clazz, List<?> elements) {
        System.out.println("\n" + title);
        System.out.println(Manager.collectionToString(clazz, elements));
    }
}