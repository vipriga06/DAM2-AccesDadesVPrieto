package com.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.project.excepcions.IOFitxerExcepcio;
import com.project.objectes.PR122persona;

class PR122mainTest {

    @TempDir
    File directoriTemporal;

    @Test
    void testSerialitzarIDeserialitzarPersones() throws IOException, IOFitxerExcepcio {
        // Preparació: Fitxer temporal
        File fitxerTemporal = new File(directoriTemporal, "PR122persones.dat");

        // Crear llista de persones
        List<PR122persona> persones = new ArrayList<>();
        persones.add(new PR122persona("Maria", "López", 36));
        persones.add(new PR122persona("Gustavo", "Ponts", 63));

        // Canviar la ruta per utilitzar el fitxer temporal amb el setter
        String filePathAnterior = PR122main.getFilePath();
        PR122main.setFilePath(fitxerTemporal.getAbsolutePath());

        // Serialitzar i deserialitzar
        PR122main.serialitzarPersones(persones);
        List<PR122persona> deserialitzades = PR122main.deserialitzarPersones();

        // Comprovar les dades deserialitzades
        assertEquals(persones.size(), deserialitzades.size());
        assertEquals(persones.get(0).toString(), deserialitzades.get(0).toString());
        assertEquals(persones.get(1).toString(), deserialitzades.get(1).toString());

        // Restaurar la ruta original
        PR122main.setFilePath(filePathAnterior);
    }

    @Test
    void testDeserialitzarFitxerInexistent() {
        // Preparació: Fitxer que no existeix
        File fitxerInexistent = new File(directoriTemporal, "PR122persones.dat");

        // Assegura que el fitxer no existeix
        assertFalse(fitxerInexistent.exists());

        // Canviar la ruta per utilitzar el fitxer temporal amb el setter
        String filePathAnterior = PR122main.getFilePath();
        PR122main.setFilePath(fitxerInexistent.getAbsolutePath());

        // Verificar que es llença l'excepció
        IOFitxerExcepcio excepcio = assertThrows(IOFitxerExcepcio.class, () -> {
            PR122main.deserialitzarPersones();
        });

        // Comprovar que el missatge d'error és adequat
        assertTrue(excepcio.getMessage().contains("Fitxer no trobat"));

        // Restaurar la ruta original
        PR122main.setFilePath(filePathAnterior);
    }
}
