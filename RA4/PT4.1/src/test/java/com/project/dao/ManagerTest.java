package com.project.dao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.project.domain.Autor;
import com.project.domain.Biblioteca;
import com.project.domain.Exemplar;
import com.project.domain.Llibre;
import com.project.domain.Persona;
import com.project.domain.Prestec;


class ManagerTest {

    private static final LocalDate DATA_ACTUAL = LocalDate.now();
    private static final LocalDate DATA_FUTURA = LocalDate.now().plusDays(14);

    @BeforeAll
    static void initializeDatabase() {
        Manager.createSessionFactory();
    }

    @AfterAll
    static void closeDatabase() {
        Manager.close();
    }

    // --------------------------- AUTOR ---------------------------
    @Nested
    @DisplayName("Autor")
    class AutorTests {
        @Test
        void addAutor_nomValid() {
            Autor autor = Manager.addAutor("Gabriel Garcia Marquez");
            assertThat(autor).isNotNull();
            assertThat(autor.getAutorId()).isNotNull();
            assertThat(autor.getNom()).isEqualTo("Gabriel Garcia Marquez");
        }
    }

    // --------------------------- LLIBRE ---------------------------
    @Nested
    @DisplayName("Llibre")
    class LlibreTests {
        @Test
        void addLlibre_valorsBasics() {
            Llibre l = Manager.addLlibre("978-TEST-PT41-1","El Quixot","Planeta",1605);
            assertThat(l).isNotNull();
            assertThat(l.getIsbn()).isEqualTo("978-TEST-PT41-1");
        }

        @Test
        void addLlibre_senseAny() {
            Llibre l = Manager.addLlibre("978-TEST-PT41-2","Sense Any","Editorial",null);
            assertThat(l).isNotNull();
            assertThat(l.getAnyPublicacio()).isNull();
        }
    }

    // --------------------------- BIBLIOTECA ---------------------------
    @Nested
    @DisplayName("Biblioteca")
    class BibliotecaTests {
        @Test
        void addBiblioteca_ok() {
            Biblioteca b = Manager.addBiblioteca("Biblio PT4.1","Barcelona","Adreca","933","info@pt41.com");
            assertThat(b).isNotNull();
            assertThat(b.getBibliotecaId()).isNotNull();
        }
    }

    // --------------------------- PERSONA ---------------------------
    @Nested
    @DisplayName("Persona")
    class PersonaTests {
        @Test
        void addPersona_ok() {
            Persona p = Manager.addPersona("12345678Z","Usuari PT41","933-000-000","user@pt41.com");
            assertThat(p).isNotNull();
            assertThat(p.getPersonaId()).isNotNull();
        }
    }

    // --------------------------- EXEMPLAR ---------------------------
    @Nested
    @DisplayName("Exemplar")
    class ExemplarTests {
        private Llibre llibre;
        private Biblioteca biblio;
        private long counter = System.currentTimeMillis();

        @BeforeEach
        void setUp() {
            counter++;
            llibre = Manager.addLlibre("978-PT41-EXE-"+counter,"Llibre Exe","Ed",2024);
            biblio = Manager.addBiblioteca("Biblio Exe "+counter,"Ciutat","Adreca","900","exe"+counter+"@biblio.com");
        }

        @Test
        void addExemplar_ok() {
            String codi = "EXE-PT41-"+counter;
            Exemplar e = Manager.addExemplar(codi,llibre,biblio);
            assertThat(e).isNotNull();
            assertThat(e.getCodiBarres()).isEqualTo(codi);
            assertThat(e.isDisponible()).isTrue();
        }
    }

    // --------------------------- PRESTEC ---------------------------
    @Nested
    @DisplayName("Prestec")
    class PrestecTests {
        private Llibre llibre;
        private Biblioteca biblio;
        private Exemplar exemplar;
        private Persona persona;
        private long counter = System.currentTimeMillis();

        @BeforeEach
        void setUp() {
            counter++;
            llibre = Manager.addLlibre("978-PT41-PR-"+counter,"Llibre Prestec","Ed",2023);
            biblio = Manager.addBiblioteca("Biblio Prestec "+counter,"Ciutat","Adreca","901","prest"+counter+"@biblio.com");
            exemplar = Manager.addExemplar("EXE-PT41-PR-"+counter,llibre,biblio);
            persona = Manager.addPersona("9999999"+(counter%10)+"X","Usuari Prestec","934","prestec"+counter+"@example.com");
        }

        @Test
        void addPrestec_disponible_ok() {
            Prestec p = Manager.addPrestec(exemplar, persona, DATA_ACTUAL, DATA_FUTURA);
            assertThat(p).isNotNull();
            assertThat(p.isActiu()).isTrue();
            assertThat(p.getExemplar().isDisponible()).isFalse();
        }

        @Test
        void addPrestec_noDisponible_retornaNull() {
            Manager.addPrestec(exemplar, persona, DATA_ACTUAL, DATA_FUTURA);
            Persona p2 = Manager.addPersona("8888888"+(counter%10)+"Y","Altre","935","altre"+counter+"@example.com");
            Prestec p = Manager.addPrestec(exemplar, p2, DATA_ACTUAL, DATA_FUTURA);
            assertThat(p).isNull();
        }

        @Test
        void registrarRetorn_actiu_ok() {
            Prestec p = Manager.addPrestec(exemplar, persona, DATA_ACTUAL, DATA_FUTURA);
            Manager.registrarRetornPrestec(p.getPrestecId(), DATA_ACTUAL.plusDays(7));
            assertThat(p.getPrestecId()).isNotNull();
        }
    }

    // --------------------------- CONSULTES HQL ---------------------------
    @Nested
    @DisplayName("Consultes HQL")
    class ConsultesHQLTests {
        @Test
        void findLlibresAmbAutors_noRevienta() {
            List<Llibre> res = Manager.findLlibresAmbAutors();
            assertThat(res).isNotNull();
        }

        @Test
        void findLlibresEnPrestec_noRevienta() {
            List<Object[]> res = Manager.findLlibresEnPrestec();
            assertThat(res).isNotNull();
            if (!res.isEmpty()) assertThat(res.get(0)).hasSize(2);
        }

        @Test
        void findLlibresAmbBiblioteques_noRevienta() {
            List<Object[]> res = Manager.findLlibresAmbBiblioteques();
            assertThat(res).isNotNull();
            if (!res.isEmpty()) assertThat(res.get(0)).hasSize(2);
        }
    }

    // --------------------------- HELPERS ---------------------------
    @Nested
    @DisplayName("Helpers")
    class HelpersTests {
        @Test
        void listCollection_autors() {
            Manager.addAutor("Autor Helper");
            Collection<Autor> res = Manager.listCollection(Autor.class);
            assertThat(res).isNotNull();
        }

        @Test
        void collectionToString_buit() {
            String res = Manager.collectionToString(Autor.class, new HashSet<>());
            assertThat(res).contains("Cap Autor trobat");
        }
    }
}
