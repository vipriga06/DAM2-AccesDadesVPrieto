package com.project;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.project.excepcions.IOFitxerExcepcio;

public class PR120mainPersonesHashmap {
    private static String filePath = System.getProperty("user.dir") + "/data/PR120persones.dat";

    public static void main(String[] args) {
        HashMap<String, Integer> persones = new HashMap<>();
        persones.put("Anna", 25);
        persones.put("Bernat", 30);
        persones.put("Carla", 22);
        persones.put("David", 35);
        persones.put("Elena", 28);

        try {
            escriurePersones(persones);
            llegirPersones();
        } catch (IOFitxerExcepcio e) {
            System.err.println("Error en treballar amb el fitxer: " + e.getMessage());
        }
    }

    // Getter per a filePath
    public static String getFilePath() {
        return filePath;
    }

    // Setter per a filePath
    public static void setFilePath(String newFilePath) {
        filePath = newFilePath;
    }

    // Mètode per escriure les persones al fitxer
    public static void escriurePersones(HashMap<String, Integer> persones) throws IOFitxerExcepcio {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(persones);
        } catch (IOException e) {
            throw new IOFitxerExcepcio("Error en escriure les persones al fitxer", e);
        }
    }

    // Mètode per llegir les persones des del fitxer
    @SuppressWarnings("unchecked")
    public static void llegirPersones() throws IOFitxerExcepcio {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            HashMap<String, Integer> persones = (HashMap<String, Integer>) ois.readObject();
            for (Map.Entry<String, Integer> entry : persones.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + " anys");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IOFitxerExcepcio("Error en llegir les persones del fitxer", e);
        }
    }
}
