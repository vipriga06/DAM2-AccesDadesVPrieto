package com.project;

import java.util.List;

import com.project.excepcions.IOFitxerExcepcio;
import com.project.utilitats.UtilsCSV;

public class PR123mainTreballadors {
    private String filePath = System.getProperty("user.dir") + "/data/PR123treballadors.csv";
    private java.util.Scanner scanner = new java.util.Scanner(System.in);

    // Getters i setters per a filePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void iniciar() {
        boolean sortir = false;

        while (!sortir) {
            try {
                // Mostrar menú
                mostrarMenu();

                // Llegir opció de l'usuari
                int opcio = Integer.parseInt(scanner.nextLine());

                switch (opcio) {
                    case 1 -> mostrarTreballadors();
                    case 2 -> modificarTreballadorInteractiu();
                    case 3 -> {
                        System.out.println("Sortint...");
                        sortir = true;
                    }
                    default -> System.out.println("Opció no vàlida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Si us plau, introdueix un número vàlid.");
            } catch (IOFitxerExcepcio e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\nMenú de Gestió de Treballadors");
        System.out.println("1. Mostra tots els treballadors");
        System.out.println("2. Modificar dades d'un treballador");
        System.out.println("3. Sortir");
        System.out.print("Selecciona una opció: ");
    }

    public void mostrarTreballadors() throws IOFitxerExcepcio {
        List<String> treballadors = llegirFitxerCSV();
        treballadors.forEach(System.out::println);
    }

    public void modificarTreballadorInteractiu() throws IOFitxerExcepcio {
        System.out.print("\nIntrodueix l'ID del treballador que vols modificar: ");
        String id = scanner.nextLine();

        System.out.print("Quina dada vols modificar (Nom, Cognom, Departament, Salari)? ");
        String columna = scanner.nextLine();

        System.out.print("Introdueix el nou valor per a " + columna + ": ");
        String nouValor = scanner.nextLine();

        modificarTreballador(id, columna, nouValor);
    }

    public void modificarTreballador(String id, String columna, String nouValor) throws IOFitxerExcepcio {
        List<String> treballadors = llegirFitxerCSV();
        if (treballadors.isEmpty()) return;

        // Obtenir l'índex de la columna
        String[] headers = UtilsCSV.obtenirArrayLinia(treballadors.get(0));
        int indexColumna = -1;
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(columna)) {
                indexColumna = i;
                break;
            }
        }
        if (indexColumna == -1) {
            throw new IOFitxerExcepcio("Columna no trobada: " + columna);
        }

        // Buscar línia amb l'ID corresponent i modificar-la
        boolean trobat = false;
        for (int i = 1; i < treballadors.size(); i++) {
            String[] dades = UtilsCSV.obtenirArrayLinia(treballadors.get(i));
            if (dades[0].equals(id)) {
                dades[indexColumna] = nouValor;
                treballadors.set(i, String.join(",", dades));
                trobat = true;
                break;
            }
        }

        if (!trobat) {
            throw new IOFitxerExcepcio("Treballador amb Id " + id + " no trobat.");
        }

        // Escriure de nou el fitxer CSV
        escriureFitxerCSV(treballadors);
    }

    private List<String> llegirFitxerCSV() throws IOFitxerExcepcio {
        List<String> treballadorsCSV = UtilsCSV.llegir(filePath);
        if (treballadorsCSV == null) {
            throw new IOFitxerExcepcio("Error en llegir el fitxer.");
        }
        return treballadorsCSV;
    }

    private void escriureFitxerCSV(List<String> treballadorsCSV) throws IOFitxerExcepcio {
        try {
            UtilsCSV.escriure(filePath, treballadorsCSV);
        } catch (Exception e) {
            throw new IOFitxerExcepcio("Error en escriure el fitxer.", e);
        }
    }

    public static void main(String[] args) {
        PR123mainTreballadors programa = new PR123mainTreballadors();
        programa.iniciar();
    }
}
