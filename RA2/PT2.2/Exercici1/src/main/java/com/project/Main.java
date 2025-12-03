package com.project;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Main {
   public static void main(String[] args) {
       String basePath = System.getProperty("user.dir") + "/data/";
       File dir = new File(basePath);
       if (!dir.exists()) {
           if (!dir.mkdirs()) {
               System.out.println("Error creating 'data' folder");
           }
       }

        Manager.createSessionFactory();

        Ciutat refCiutat1 = Manager.addCiutat("Vancouver", "Canada", 98661);
        Ciutat refCiutat2 = Manager.addCiutat("Växjö", "Suècia", 35220);
        Ciutat refCiutat3 = Manager.addCiutat("Kyoto", "Japó", 5200461);

        Ciutada refCiutada1 = Manager.addCiutada("Tony", "Happy", 20);
        Ciutada refCiutada2 = Manager.addCiutada("Monica", "Mouse", 22);
        Ciutada refCiutada3 = Manager.addCiutada("Eirika", "Erjo", 44);
        Ciutada refCiutada4 = Manager.addCiutada("Ven", "Enrison", 48);
        Ciutada refCiutada5 = Manager.addCiutada("Akira", "Akiko", 62);
        Ciutada refCiutada6 = Manager.addCiutada("Masako", "Kubo", 66);

        System.out.println("Punt 1: Després de la creació inicial d'elements");
        System.out.println(Manager.collectionToString(Ciutat.class, Manager.listCollection(Ciutat.class, "")));
        System.out.println(Manager.collectionToString(Ciutada.class, Manager.listCollection(Ciutada.class, "")));

        Set<Ciutada> ciutadansCity1 = new HashSet<Ciutada>();
        ciutadansCity1.add(refCiutada1);
        ciutadansCity1.add(refCiutada2);
        ciutadansCity1.add(refCiutada3);

        Manager.updateCiutat(refCiutat1.getCiutatId(), refCiutat1.getNom(), refCiutat1.getPais(), refCiutat1.getPoblacio(), ciutadansCity1);

        Set<Ciutada> ciutadansCity2 = new HashSet<Ciutada>();
        ciutadansCity2.add(refCiutada4);
        ciutadansCity2.add(refCiutada5);

        Manager.updateCiutat(refCiutat2.getCiutatId(), refCiutat2.getNom(), refCiutat2.getPais(), refCiutat2.getPoblacio(), ciutadansCity2);

        System.out.println("Punt 2: Després d'actualitzar ciutats");
        System.out.println(Manager.collectionToString(Ciutat.class, Manager.listCollection(Ciutat.class, "")));
        System.out.println(Manager.collectionToString(Ciutada.class, Manager.listCollection(Ciutada.class, "")));

        Manager.updateCiutat(refCiutat1.getCiutatId(), "Vancouver Updated", refCiutat1.getPais(), refCiutat1.getPoblacio(), ciutadansCity1);
        Manager.updateCiutat(refCiutat2.getCiutatId(), "Växjö Updated", refCiutat2.getPais(), refCiutat2.getPoblacio(), ciutadansCity2);

        Manager.updateCiutada(refCiutada1.getCiutadaId(), "Tony Updated", refCiutada1.getCognom(), refCiutada1.getEdat());
        Manager.updateCiutada(refCiutada4.getCiutadaId(), "Ven Updated", refCiutada4.getCognom(), refCiutada4.getEdat());

        System.out.println("Punt 3: Després d'actualització de noms");
        System.out.println(Manager.collectionToString(Ciutat.class, Manager.listCollection(Ciutat.class, "")));
        System.out.println(Manager.collectionToString(Ciutada.class, Manager.listCollection(Ciutada.class, "")));

        Manager.delete(Ciutat.class, refCiutat3.getCiutatId());
        Manager.delete(Ciutada.class, refCiutada6.getCiutadaId());

        System.out.println("Punt 4: després d'esborrat");
        System.out.println(Manager.collectionToString(Ciutat.class, Manager.listCollection(Ciutat.class, "")));
        System.out.println(Manager.collectionToString(Ciutada.class, Manager.listCollection(Ciutada.class, "")));

        System.out.println("Punt 5: Recuperació de ciutadans d'una ciutat específica");
        Ciutat ciutat = Manager.getCiutatWithCiutadans(refCiutat1.getCiutatId());
        if (ciutat != null) {
            System.out.println("Ciutadans de la ciutat '" + ciutat.getNom() + "':");
            Set<Ciutada> ciutadans = ciutat.getCiutadans();
            if (ciutadans != null && !ciutadans.isEmpty()) {
                for (Ciutada ciutada : ciutadans) {
                    System.out.println("- " + ciutada.getNom() + " " + ciutada.getCognom());
                }
            } else {
                System.out.println("La ciutat no té ciutadans");
            }
        } else {
            System.out.println("No s'ha trobat la ciutat");
        }

        Manager.close();
   }
}