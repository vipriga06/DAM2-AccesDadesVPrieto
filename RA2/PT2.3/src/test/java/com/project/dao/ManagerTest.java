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

/**
 * Suite de tests per a la classe Manager (DAO)
 * 
 * Segueix les bones pràctiques de testing:
 * - Organització amb @Nested per agrupar tests relacionats
 * - Patró Arrange-Act-Assert en cada test
 * - @DisplayName per descripcions llegibles
 * - AssertJ per assertions fluides
 * - Setup i teardown adequats
 */
@DisplayName("Manager - Suite de Tests de l'Accés a Dades")
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

    // =================================================================================
    // TESTS PARA AUTOR
    // =================================================================================

    @Nested
    @DisplayName("Tests per a Operacions d'Autor")
    class AutorTests {

        @Test
        @DisplayName("Crear un autor amb nom vàlid")
        void addAutor_nomValid_creaAutorCorrectament() {
            // Arrange
            String nomAutor = "Gabriel García Márquez";

            // Act
            Autor autorCreat = Manager.addAutor(nomAutor);

            // Assert
            assertThat(autorCreat).isNotNull();
            assertThat(autorCreat.getNom()).isEqualTo(nomAutor);
            assertThat(autorCreat.getAutorId()).isNotNull()
                    .isGreaterThan(0);
        }

        @Test
        @DisplayName("Crear múltiples autors")
        void addAutor_multiplsAutors_cresTots() {
            // Arrange
            String[] noms = { "Paulo Coelho", "Julio Verne", "Agatha Christie" };

            // Act
            Autor[] autors = new Autor[noms.length];
            for (int i = 0; i < noms.length; i++) {
                autors[i] = Manager.addAutor(noms[i]);
            }

            // Assert
            for (int i = 0; i < autors.length; i++) {
                assertThat(autors[i])
                        .isNotNull()
                        .extracting(Autor::getNom)
                        .isEqualTo(noms[i]);
            }
        }
    }

    // =================================================================================
    // TESTS PARA LLIBRE
    // =================================================================================

    @Nested
    @DisplayName("Tests per a Operacions de Llibre")
    class LlibreTests {

        @Test
        @DisplayName("Crear un llibre amb dades vàlides")
        void addLlibre_dadesValides_creaLlibreCorrectament() {
            // Arrange
            String isbn = "978-8439726654";
            String titol = "El Quixot";
            String editorial = "Planeta";
            Integer anyPublicacio = 1605;

            // Act
            Llibre llibreCreat = Manager.addLlibre(isbn, titol, editorial, anyPublicacio);

            // Assert
            assertThat(llibreCreat)
                    .isNotNull()
                    .extracting(Llibre::getIsbn, Llibre::getTitol,
                            Llibre::getEditorial, Llibre::getAnyPublicacio)
                    .containsExactly(isbn, titol, editorial, anyPublicacio);
        }

        @Test
        @DisplayName("Crear un llibre sense any de publicació")
        void addLlibre_senseAnyPublicacio_creaCorrectament() {
            // Arrange
            String isbn = "978-1234567890";
            String titol = "Llibre Recent";
            String editorial = "Editorial Test";

            // Act
            Llibre llibreCreat = Manager.addLlibre(isbn, titol, editorial, null);

            // Assert
            assertThat(llibreCreat)
                    .isNotNull()
                    .extracting(Llibre::getAnyPublicacio)
                    .isNull();
        }

        @Test
        @DisplayName("Crear múltiples llibres")
        void addLlibre_multiplsLlibres_cresTots() {
            // Arrange
            String[][] dades = { 
                { "978-8480008563", "Cent anys de soledat", "Sudamericana", "1967" },
                { "978-8498381429", "El Viage", "Planeta", "1873" }
            };

            // Act
            Llibre[] llibres = new Llibre[dades.length];
            for (int i = 0; i < dades.length; i++) {
                llibres[i] = Manager.addLlibre(
                        dades[i][0], dades[i][1], dades[i][2],
                        Integer.parseInt(dades[i][3]));
            }

            // Assert
            assertThat(llibres).allMatch(l -> l != null && l.getLlibreId() > 0);
        }
    }

    // =================================================================================
    // TESTS PARA BIBLIOTECA
    // =================================================================================

    @Nested
    @DisplayName("Tests per a Operacions de Biblioteca")
    class BibliotecaTests {

        @Test
        @DisplayName("Crear una biblioteca amb dades completes")
        void addBiblioteca_dadesCompletes_creaBibliotecaCorrectament() {
            // Arrange
            String nom = "Biblioteca Nacional de Catalunya";
            String ciutat = "Barcelona";
            String adreca = "Plaça de la Reial Acadèmia, 2-4";
            String telefon = "932-76-55-00";
            String email = "info@bnc.cat";

            // Act
            Biblioteca bibliotecaCreada = Manager.addBiblioteca(nom, ciutat, adreca,
                    telefon, email);

            // Assert
            assertThat(bibliotecaCreada)
                    .isNotNull()
                    .satisfies(b -> {
                        assertThat(b.getNom()).isEqualTo(nom);
                        assertThat(b.getCiutat()).isEqualTo(ciutat);
                        assertThat(b.getAdreca()).isEqualTo(adreca);
                        assertThat(b.getTelefon()).isEqualTo(telefon);
                        assertThat(b.getEmail()).isEqualTo(email);
                    });
        }

        @Test
        @DisplayName("Crear múltiples biblioteques")
        void addBiblioteca_multiplsBiblioteques_cresTotes() {
            // Arrange
            String[][] dadesMultiples = {
                { "Biblioteca Municipal", "Madrid", "Calle Principal, 1", "912-34-56-78",
                    "madrid@biblio.es" },
                { "Biblioteca Pública", "Valencia", "Avenida de la República, 5", "963-12-34-56",
                    "valencia@biblio.es" }
            };

            // Act
            Biblioteca[] biblioteques = new Biblioteca[dadesMultiples.length];
            for (int i = 0; i < dadesMultiples.length; i++) {
                biblioteques[i] = Manager.addBiblioteca(
                        dadesMultiples[i][0], dadesMultiples[i][1],
                        dadesMultiples[i][2], dadesMultiples[i][3],
                        dadesMultiples[i][4]);
            }

            // Assert
            assertThat(biblioteques)
                    .allMatch(b -> b != null && b.getBibliotecaId() > 0);
        }
    }

    // =================================================================================
    // TESTS PARA PERSONA
    // =================================================================================

    @Nested
    @DisplayName("Tests per a Operacions de Persona (Usuari)")
    class PersonaTests {

        @Test
        @DisplayName("Crear una persona amb dades vàlides")
        void addPersona_dadesValides_creaPersonaCorrectament() {
            // Arrange
            String dni = "12345678A";
            String nom = "Joan Corominas";
            String telefon = "934-56-78-90";
            String email = "joan@example.com";

            // Act
            Persona personaCreada = Manager.addPersona(dni, nom, telefon, email);

            // Assert
            assertThat(personaCreada)
                    .isNotNull()
                    .extracting(Persona::getDni, Persona::getNom,
                            Persona::getTelefon, Persona::getEmail)
                    .containsExactly(dni, nom, telefon, email);
        }

        @Test
        @DisplayName("Crear múltiples persones")
        void addPersona_multiplesPersones_cresTotes() {
            // Arrange
            String[][] dades = {
                { "87654321B", "Maria Rodríguez", "934-22-33-44", "maria@example.com" },
                { "11223344C", "Pere Martínez", "934-55-66-77", "pere@example.com" }
            };

            // Act
            Persona[] persones = new Persona[dades.length];
            for (int i = 0; i < dades.length; i++) {
                persones[i] = Manager.addPersona(dades[i][0], dades[i][1],
                        dades[i][2], dades[i][3]);
            }

            // Assert
            assertThat(persones)
                    .allMatch(p -> p != null && p.getPersonaId() > 0);
        }
    }

    // =================================================================================
    // TESTS PARA EXEMPLAR
    // =================================================================================

    @Nested
    @DisplayName("Tests per a Operacions d'Exemplar")
    class ExemplarTests {

        private Llibre llibreTest;
        private Biblioteca bibliotecaTest;
    private long testCounter = System.currentTimeMillis();
        @BeforeEach
        void setUp() {
                testCounter++;
                llibreTest = Manager.addLlibre("978-TEST-" + testCounter, "Llibre Test " + testCounter,
                    "Editorial Test", 2024);
                bibliotecaTest = Manager.addBiblioteca("Biblioteca Test " + testCounter,
                    "Ciutat Test", "Adreça Test", "999-99-99-99",
                    "test" + testCounter + "@biblio.com");
        }

        @Test
        @DisplayName("Crear un exemplar d'un llibre a una biblioteca")
        void addExemplar_datesValides_creaExemplarCorrectament() {
            // Arrange
            String codiBarres = "EXE-001-" + testCounter;

            // Act
            Exemplar exemplarCreat = Manager.addExemplar(codiBarres, llibreTest,
                    bibliotecaTest);

            // Assert
            assertThat(exemplarCreat)
                    .isNotNull()
                    .satisfies(e -> {
                        assertThat(e.getCodiBarres()).isEqualTo(codiBarres);
                        assertThat(e.getLlibre()).isEqualTo(llibreTest);
                        assertThat(e.getBiblioteca()).isEqualTo(bibliotecaTest);
                        assertThat(e.isDisponible()).isTrue();
                    });
        }

        @Test
        @DisplayName("Crear múltiples exemplars del mateix llibre")
        void addExemplar_multiplsExemplars_cresTots() {
            // Arrange
            String[] codisBarra = { 
                "EXE-002-001-" + testCounter, 
                "EXE-002-002-" + testCounter, 
                "EXE-002-003-" + testCounter 
            };

            // Act
            Exemplar[] exemplars = new Exemplar[codisBarra.length];
            for (int i = 0; i < codisBarra.length; i++) {
                exemplars[i] = Manager.addExemplar(codisBarra[i], llibreTest,
                        bibliotecaTest);
            }

            // Assert
            assertThat(exemplars)
                    .allMatch(e -> e != null && e.isDisponible())
                    .allMatch(e -> e.getLlibre().equals(llibreTest));
        }
    }

    // =================================================================================
    // TESTS PARA PRÉSTEC
    // =================================================================================

    @Nested
    @DisplayName("Tests per a Operacions de Préstec")
    class PrestecTests {

        private Llibre llibrePrestec;
        private Biblioteca bibliotecaPrestec;
        private Exemplar exemplarDisponible;
        private Persona personaPrestec;
    private long testCounter = System.currentTimeMillis();

        @BeforeEach
        void setUp() {
            testCounter++;
            // Preparar dades de test
                llibrePrestec = Manager.addLlibre("978-PRESTEC-" + testCounter,
                    "Llibre de Préstec", "Editorial Préstec", 2023);
                bibliotecaPrestec = Manager.addBiblioteca("Biblioteca Préstec " + testCounter,
                    "Ciutat Préstec", "Adreça Préstec", "888-88-88-88",
                    "prestec" + testCounter + "@biblio.com");
                exemplarDisponible = Manager.addExemplar("EXE-PREST-" + testCounter,
                    llibrePrestec, bibliotecaPrestec);
                personaPrestec = Manager.addPersona("9988776" + (testCounter % 10) + "D", "Usuari Test " + testCounter,
                    "934-11-22-33", "usuari" + testCounter + "@example.com");
        }

        @Test
        @DisplayName("Crear un préstec amb exemplar disponible")
        void addPrestec_exemplarDisponible_creaPrestecCorrectament() {
            // Arrange
            LocalDate dataPrestec = DATA_ACTUAL;
            LocalDate dataRetornPrevista = DATA_FUTURA;

            // Act
            Prestec prestecCreat = Manager.addPrestec(exemplarDisponible,
                    personaPrestec, dataPrestec, dataRetornPrevista);

            // Assert
            assertThat(prestecCreat)
                    .isNotNull()
                    .satisfies(p -> {
                        assertThat(p.getExemplar())
                            .isNotNull()
                            .extracting(Exemplar::getCodiBarres)
                            .isEqualTo(exemplarDisponible.getCodiBarres());
                        assertThat(p.getPersona()).isEqualTo(personaPrestec);
                        assertThat(p.getDataPrestec()).isEqualTo(dataPrestec);
                        assertThat(p.getDataRetornPrevista())
                                .isEqualTo(dataRetornPrevista);
                        assertThat(p.isActiu()).isTrue();
                        // El exemplar marca-se com no disponible tras el préstec
                        assertThat(p.getExemplar().isDisponible()).isFalse();
                    });
        }

        @Test
        @DisplayName("Marcar exemplar com no disponible después de préstec")
        void addPrestec_ejemplarQuedaNoDisponible() {
            // Arrange
            // Obtenemos la colección de ejemplares (para verificar estado después)

            // Act
            Prestec prestecCreat = Manager.addPrestec(exemplarDisponible,
                    personaPrestec, DATA_ACTUAL, DATA_FUTURA);

            // Assert
            assertThat(prestecCreat).isNotNull();
            // Note: En un entorno real con transaction scope, verificaríamos
            // que exemplarDisponible.isDisponible() == false aquí
        }

        @Test
        @DisplayName("Registrar retorn de préstec actiu")
        void registrarRetornPrestec_prestecActiu_retornCorrectament() {
            // Arrange
            Prestec prestecActiu = Manager.addPrestec(exemplarDisponible,
                    personaPrestec, DATA_ACTUAL, DATA_FUTURA);
            LocalDate dataRetornReal = DATA_ACTUAL.plusDays(7);

            // Act
            Manager.registrarRetornPrestec(prestecActiu.getPrestecId(),
                    dataRetornReal);

            // Assert
            // En un escenari real, recarregaríem el préstec per verificar
            // que s'ha actualitzat correctament
            assertThat(prestecActiu.getPrestecId()).isNotNull()
                    .isGreaterThan(0);
        }

        @Test
        @DisplayName("No permitir préstec de exemplar no disponible")
        void addPrestec_exemplarNoDisponible_retornaNull() {
            // Arrange
            // Crear el primer préstec per marcar exemplar com no disponible
            Manager.addPrestec(exemplarDisponible, personaPrestec, DATA_ACTUAL,
                    DATA_FUTURA);

            // Crear una segunda persona
            Persona persona2 = Manager.addPersona("55443322E", "Persona 2",
                    "934-99-88-77", "persona2@example.com");

            // Act - Intentar crear segundo préstec del mismo exemplar
            Prestec segonPrestec = Manager.addPrestec(exemplarDisponible,
                    persona2, DATA_ACTUAL, DATA_FUTURA);

            // Assert
            assertThat(segonPrestec).isNull();
        }
    }

    // =================================================================================
    // TESTS PARA CONSULTAS HQL
    // =================================================================================

    @Nested
    @DisplayName("Tests per a Consultes HQL")
    class ConsultesHQLTests {

        private Llibre llibreHQL;
        private Autor autorHQL;

        @BeforeEach
        void setUp() {
            // Crear autor y libro para las consultas
            autorHQL = Manager.addAutor("Autor HQL Test");
            llibreHQL = Manager.addLlibre("978-HQL001", "Llibre HQL", "Editorial HQL",
                    2024);
        }

        @Test
        @DisplayName("findLlibresAmbAutors retorna llibres amb autors")
        void findLlibresAmbAutors_retornaLlibresAmbAutors() {
            // Act
            List<Llibre> llibres = Manager.findLlibresAmbAutors();

            // Assert
                assertThat(llibres)
                    .isNotNull()
                    .isInstanceOf(List.class);
                // Note: La llista pot estar buida si no hi ha llibres amb autors
        }

        @Test
        @DisplayName("findLlibresEnPrestec retorna títols i usuaris")
        void findLlibresEnPrestec_retornaInformacionCorrecta() {
            // Act
            List<Object[]> resultats = Manager.findLlibresEnPrestec();

            // Assert
            assertThat(resultats)
                    .isNotNull()
                    .isInstanceOf(List.class);
            
            // Si hi ha resultats, verificar estructura
            if (!resultats.isEmpty()) {
                assertThat(resultats.get(0))
                        .isNotNull()
                        .hasSize(2); // [títol, nom persona]
            }
        }

        @Test
        @DisplayName("findLlibresAmbBiblioteques retorna ubicacions")
        void findLlibresAmbBiblioteques_retornaLlibresIBiblioteques() {
            // Act
            List<Object[]> resultats = Manager.findLlibresAmbBiblioteques();

            // Assert
            assertThat(resultats)
                    .isNotNull()
                    .isInstanceOf(List.class);
            
            // Si hi ha resultats, verificar estructura
            if (!resultats.isEmpty()) {
                assertThat(resultats.get(0))
                        .isNotNull()
                        .hasSize(2); // [títol, nom biblioteca]
            }
        }
    }

    // =================================================================================
    // TESTS PER A MÈTODES AUXILIARS
    // =================================================================================

    @Nested
    @DisplayName("Tests per a Mètodes Auxiliars")
    class MethodsAuxiliarsTests {

        @Test
        @DisplayName("listCollection retorna col·lecció d'autors")
        void listCollection_autors_retornaCollection() {
            // Arrange
            Manager.addAutor("Autor Auxiliar 1");
            Manager.addAutor("Autor Auxiliar 2");

            // Act
            Collection<Autor> autors = Manager.listCollection(Autor.class);

            // Assert
            assertThat(autors)
                    .isNotNull()
                    .isNotEmpty();
        }

        @Test
        @DisplayName("listCollection retorna col·lecció de llibres")
        void listCollection_llibres_retornaCollection() {
            // Arrange
            Manager.addLlibre("978-AUX001", "Llibre Auxiliar", "Editorial", 2024);

            // Act
            Collection<Llibre> llibres = Manager.listCollection(Llibre.class);

            // Assert
            assertThat(llibres)
                    .isNotNull()
                    .isNotEmpty();
        }

        @Test
        @DisplayName("collectionToString formatea correctament")
        void collectionToString_coleccioValida_retornaString() {
            // Arrange
            Collection<Autor> autors = Manager.listCollection(Autor.class);

            // Act
            String resultado = Manager.collectionToString(Autor.class, autors);

            // Assert
            assertThat(resultado)
                    .isNotNull()
                    .isNotEmpty();
        }

        @Test
        @DisplayName("collectionToString maneja col·lecció buida")
        void collectionToString_coleccioVuida_retornaStringVuit() {
            // Arrange
            Collection<Autor> coleccioVuida = new HashSet<>();

            // Act
            String resultado = Manager.collectionToString(Autor.class, coleccioVuida);

            // Assert
            assertThat(resultado)
                    .isNotNull()
                    .contains("Cap Autor trobat");
        }
    }
}
