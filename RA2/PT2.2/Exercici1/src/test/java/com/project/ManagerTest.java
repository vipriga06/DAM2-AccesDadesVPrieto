package com.project;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests simples per a la classe Manager (JPA)
 * Valida les operacions CRUD bàsiques
 */
public class ManagerTest {

    @BeforeEach
    public void setUp() {
        // Inicialitza l'EntityManagerFactory abans de cada test
        Manager.createSessionFactory();
    }

    /**
     * Test per verificar que es pot crear una ciutat
     */
    @Test
    public void testAddCiutat() {
        try {
            Ciutat ciutat = Manager.addCiutat("Barcelona", "Espanya", 1600000);
            assertNotNull(ciutat, "La ciutat no pot ser nula");
            assertEquals("Barcelona", ciutat.getNom(), "El nom de la ciutat no coincideix");
            assertEquals("Espanya", ciutat.getPais(), "El país no coincideix");
            assertEquals(1600000, ciutat.getPoblacio(), "La població no coincideix");
        } finally {
            Manager.close();
        }
    }

    /**
     * Test per verificar que es pot crear un ciutadà
     */
    @Test
    public void testAddCiutada() {
        try {
            Ciutada ciutada = Manager.addCiutada("Joan", "Puig", 30);
            assertNotNull(ciutada, "El ciutadà no pot ser nul");
            assertEquals("Joan", ciutada.getNom(), "El nom del ciutadà no coincideix");
            assertEquals("Puig", ciutada.getCognom(), "El cognom del ciutadà no coincideix");
            assertEquals(30, ciutada.getEdat(), "L'edat del ciutadà no coincideix");
        } finally {
            Manager.close();
        }
    }

    /**
     * Test per verificar que es poden llistar ciutats
     */
    @Test
    public void testListCiutats() {
        try {
            Manager.addCiutat("Madrid", "Espanya", 3000000);
            Manager.addCiutat("Valencia", "Espanya", 1600000);

            List<Ciutat> ciutats = Manager.listCollection(Ciutat.class, "");
            assertNotNull(ciutats, "La llista de ciutats no pot ser nula");
            assertFalse(ciutats.isEmpty(), "La llista de ciutats no pot estar buida");
            assertTrue(ciutats.size() >= 2, "Hauria d'haver almenys 2 ciutats");
        } finally {
            Manager.close();
        }
    }

    /**
     * Test per verificar que es poden llistar ciutadans
     */
    @Test
    public void testListCiutadans() {
        try {
            Manager.addCiutada("Anna", "García", 25);
            Manager.addCiutada("Pere", "López", 35);

            List<Ciutada> ciutadans = Manager.listCollection(Ciutada.class, "");
            assertNotNull(ciutadans, "La llista de ciutadans no pot ser nula");
            assertFalse(ciutadans.isEmpty(), "La llista de ciutadans no pot estar buida");
            assertTrue(ciutadans.size() >= 2, "Hauria d'haver almenys 2 ciutadans");
        } finally {
            Manager.close();
        }
    }

    /**
     * Test per verificar que es pot actualitzar una ciutat
     */
    @Test
    public void testUpdateCiutat() {
        try {
            Ciutat ciutat = Manager.addCiutat("Tarragona", "Espanya", 130000);
            long id = ciutat.getCiutatId();

            Manager.updateCiutat(id, "Tarragona Updated", "Espanya", 135000, null);

            List<Ciutat> ciutats = Manager.listCollection(Ciutat.class, "");
            Ciutat ciudatUpdated = ciutats.stream()
                .filter(c -> c.getCiutatId() == id)
                .findFirst()
                .orElse(null);
            assertNotNull(ciudatUpdated, "La ciutat actualitzada no pot ser nula");
            assertEquals("Tarragona Updated", ciudatUpdated.getNom(), "El nom de la ciutat no ha estat actualitzat");
        } finally {
            Manager.close();
        }
    }

    /**
     * Test per verificar que es pot actualitzar un ciutadà
     */
    @Test
    public void testUpdateCiutada() {
        try {
            Ciutada ciutada = Manager.addCiutada("Miguel", "Sánchez", 40);
            long id = ciutada.getCiutadaId();

            Manager.updateCiutada(id, "Miguel Updated", "Sánchez", 41);

            List<Ciutada> ciutadans = Manager.listCollection(Ciutada.class, "");
            Ciutada ciudadanoUpdated = ciutadans.stream()
                .filter(c -> c.getCiutadaId() == id)
                .findFirst()
                .orElse(null);
            assertNotNull(ciudadanoUpdated, "El ciutadà actualitzat no pot ser nul");
            assertEquals("Miguel Updated", ciudadanoUpdated.getNom(), "El nom del ciutadà no ha estat actualitzat");
        } finally {
            Manager.close();
        }
    }

    /**
     * Test per verificar que es pot eliminar un ciutadà
     */
    @Test
    public void testDeleteCiutada() {
        try {
            Ciutada ciutada = Manager.addCiutada("Laura", "Fernández", 32);
            long id = ciutada.getCiutadaId();

            Manager.delete(Ciutada.class, id);

            List<Ciutada> ciutadans = Manager.listCollection(Ciutada.class, "");
            assertFalse(ciutadans.stream().anyMatch(c -> c.getCiutadaId() == id), 
                "El ciutadà no hauria d'existir després de ser eliminat");
        } finally {
            Manager.close();
        }
    }

    /**
     * Test per verificar que es pot eliminar una ciutat
     */
    @Test
    public void testDeleteCiutat() {
        try {
            Ciutat ciutat = Manager.addCiutat("Lleida", "Espanya", 150000);
            long id = ciutat.getCiutatId();

            Manager.delete(Ciutat.class, id);

            List<Ciutat> ciutats = Manager.listCollection(Ciutat.class, "");
            assertFalse(ciutats.stream().anyMatch(c -> c.getCiutatId() == id), 
                "La ciutat no hauria d'existir després de ser eliminada");
        } finally {
            Manager.close();
        }
    }
}
