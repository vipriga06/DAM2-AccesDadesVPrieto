package com.project.dao;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.project.domain.Autor;
import com.project.domain.Biblioteca;
import com.project.domain.Exemplar;
import com.project.domain.Llibre;
import com.project.domain.Persona;
import com.project.domain.Prestec;

/**
 * Classe Manager - Capa d'accés a dades (DAO)
 * 
 * Aquesta classe centralitza totes les operacions de persistència amb Hibernate.
 * Segueix el patró DAO (Data Access Object) per separar la lògica de negoci
 * de l'accés a la base de dades.
 * 
 * PATRÓ GENERAL DELS MÈTODES:
 * 1. Obrir sessió amb factory.openSession()
 * 2. Iniciar transacció amb session.beginTransaction()
 * 3. Realitzar operacions (persist, merge, find, createQuery...)
 * 4. Confirmar amb tx.commit() o desfer amb tx.rollback() si hi ha error
 * 5. Tancar sessió al finally
 */
public class Manager {

    // SessionFactory és thread-safe i es crea una sola vegada per aplicació
    private static SessionFactory factory;

    /**
     * Inicialitza la SessionFactory llegint la configuració de hibernate.properties.
     * S'ha de cridar una sola vegada a l'inici de l'aplicació.
     */
    public static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            Properties properties = new Properties();
            
            // Carreguem el fitxer hibernate.properties des de resources/
            try (InputStream input = Manager.class.getClassLoader().getResourceAsStream("hibernate.properties")) {
                if (input == null) throw new IOException("No s'ha trobat hibernate.properties");
                properties.load(input);
                configuration.addProperties(properties);
            }
            
            // Registrem totes les entitats que Hibernate ha de gestionar
            configuration.addAnnotatedClass(Biblioteca.class);
            configuration.addAnnotatedClass(Llibre.class);
            configuration.addAnnotatedClass(Autor.class);
            configuration.addAnnotatedClass(Exemplar.class);
            configuration.addAnnotatedClass(Persona.class);
            configuration.addAnnotatedClass(Prestec.class);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("Error inicialitzant Hibernate: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Tanca la SessionFactory i allibera recursos.
     * S'ha de cridar al finalitzar l'aplicació.
     */
    public static void close() {
        if (factory != null) factory.close();
    }

    // =================================================================================
    // MÈTODES A IMPLEMENTAR PER L'ALUMNE
    // =================================================================================

    /**
     * Crea i persisteix un nou autor a la base de dades.
     * 
     * @param nom Nom complet de l'autor
     * @return L'objecte Autor persistit (amb ID generat) o null si hi ha error
     */
    public static Autor addAutor(String nom) {
        Session session = factory.openSession();
        Transaction tx = null;
        Autor autor = null;
        try {
            tx = session.beginTransaction();
            autor = new Autor(nom);
            session.persist(autor);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return autor;
    }

    /**
     * Crea i persisteix un nou llibre a la base de dades.
     * 
     * @param isbn Codi ISBN del llibre (identificador únic del llibre, no de l'exemplar)
     * @param titol Títol del llibre
     * @param editorial Nom de l'editorial
     * @param anyPublicacio Any de publicació (pot ser null)
     * @return L'objecte Llibre persistit o null si hi ha error
     */
    public static Llibre addLlibre(String isbn, String titol, String editorial, Integer anyPublicacio) {
        Session session = factory.openSession();
        Transaction tx = null;
        Llibre llibre = null;
        try {
            tx = session.beginTransaction();
            llibre = new Llibre(isbn,titol,editorial,anyPublicacio);
            session.persist(llibre);            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return llibre;
    }

    /**
     * Actualitza un autor existent i li assigna una col·lecció de llibres.
     * 
     * IMPORTANT - RELACIONS MANY-TO-MANY:
     * En JPA, quan tens una relació bidireccional M:N, només un costat és el "propietari"
     * (el que té @JoinTable). L'altre costat és l'invers (té mappedBy).
     * Per persistir la relació, S'HA DE MODIFICAR EL COSTAT PROPIETARI.
     * 
     * En aquest projecte: Llibre és el propietari (té @JoinTable), Autor és l'invers.
     * 
     * @param autorId ID de l'autor a actualitzar
     * @param nom Nou nom de l'autor
     * @param llibres Conjunt de llibres a vincular amb l'autor
     */
    public static void updateAutor(Long autorId, String nom, Set<Llibre> llibres) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Autor autorDB = session.find(Autor.class, autorId);
            if (autorDB != null) {
                autorDB.setNom(nom);
                for (Llibre llibre : llibres) {
                    Llibre llibreDB = session.find(Llibre.class, llibre.getLlibreId());
                    if (llibreDB != null) {
                        llibreDB.getAutors().add(autorDB);
                    }
                }
                session.merge(autorDB); 
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    /**
     * Crea i persisteix una nova biblioteca.
     * 
     * @param nom Nom de la biblioteca
     * @param ciutat Ciutat on es troba
     * @param adreca Adreça completa
     * @param telefon Telèfon de contacte
     * @param email Correu electrònic
     * @return L'objecte Biblioteca persistit o null si hi ha error
     */
    public static Biblioteca addBiblioteca(String nom, String ciutat, String adreca, String telefon, String email) {
        Session session = factory.openSession();
        Transaction tx = null;
        Biblioteca biblio = null;
        try {
            tx = session.beginTransaction();
            biblio = new Biblioteca(nom,ciutat,adreca,telefon,email);
            session.persist(biblio);            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return biblio;
    }

    /**
     * Crea i persisteix un nou exemplar físic d'un llibre en una biblioteca.
     * Un exemplar representa una còpia física concreta d'un llibre.
     * 
     * @param codiBarres Codi de barres únic de l'exemplar
     * @param llibre Llibre del qual és còpia (relació ManyToOne)
     * @param biblioteca Biblioteca on es troba (relació ManyToOne)
     * @return L'objecte Exemplar persistit o null si hi ha error
     */
    public static Exemplar addExemplar(String codiBarres, Llibre llibre, Biblioteca biblioteca) {
        Session session = factory.openSession();
        Transaction tx = null;
        Exemplar exemplar = null;
        try {
            tx = session.beginTransaction();
            exemplar = new Exemplar(codiBarres,llibre,biblioteca);
            session.persist(exemplar);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return exemplar;
    }

    /**
     * Crea i persisteix una nova persona (usuari de la biblioteca).
     * 
     * @param dni Document Nacional d'Identitat
     * @param nom Nom complet de la persona
     * @param telefon Telèfon de contacte
     * @param email Correu electrònic
     * @return L'objecte Persona persistit o null si hi ha error
     */
    public static Persona addPersona(String dni, String nom, String telefon, String email) {
        Session session = factory.openSession();
        Transaction tx = null;
        Persona persona = null;
        try {
            tx = session.beginTransaction();
            persona = new Persona(dni,nom,telefon,email);
            session.persist(persona);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return persona;
    }

    /**
     * Crea un nou préstec si l'exemplar està disponible.
     * 
     * LÒGICA DE NEGOCI:
     * - Un exemplar només es pot prestar si està disponible (disponible = true)
     * - En crear el préstec, l'exemplar passa a no disponible
     * - El préstec es crea amb actiu = true
     * 
     * @param exemplar Exemplar a prestar
     * @param persona Persona que rep el préstec
     * @param dataPrestec Data d'inici del préstec
     * @param dataRetornPrevista Data prevista de retorn
     * @return L'objecte Prestec creat o null si l'exemplar no està disponible o hi ha error
     */
    public static Prestec addPrestec(Exemplar exemplar, Persona persona, LocalDate dataPrestec, LocalDate dataRetornPrevista) {
        Session session = factory.openSession();
        Transaction tx = null;
        Prestec prestec = null;
        try {
            tx = session.beginTransaction();
            Exemplar exemplarDB = session.find(Exemplar.class, exemplar.getExemplarId());
            if (exemplarDB != null && exemplarDB.isDisponible()) {
                prestec = new Prestec(exemplarDB, persona, dataPrestec, dataRetornPrevista);
                exemplarDB.setDisponible(false);
                session.persist(prestec);
                session.merge(exemplarDB);
            } else {
                System.out.println("Exemplar no disponible o no existeix.");
            }

            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return prestec;
    }

    /**
     * Registra el retorn d'un préstec actiu.
     * 
     * LÒGICA DE NEGOCI:
     * - Només es pot retornar un préstec que estigui actiu
     * - En retornar, el préstec passa a actiu = false
     * - L'exemplar associat torna a estar disponible
     * 
     * @param prestecId ID del préstec a retornar
     * @param dataReal Data real de retorn (pot ser diferent de la prevista)
     */
    public static void registrarRetornPrestec(Long prestecId, LocalDate dataReal) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Prestec prestec = session.find(Prestec.class, prestecId);
            if (prestec != null && prestec.isActiu()) {
                prestec.setDataRetornReal(dataReal);
                prestec.setActiu(false);
                Exemplar exemplar = prestec.getExemplar();
                exemplar.setDisponible(true);
                System.out.println("Préstec retornat correctament.");
            } else {
                System.out.println("Préstec no existeix o no està actiu.");
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // =================================================================================
    // CONSULTES HQL - A IMPLEMENTAR PER L'ALUMNE
    // =================================================================================

    /**
     * Retorna tots els llibres amb els seus autors carregats (evitant LazyInitializationException).
     * 
     * PISTES HQL:
     * - Usa JOIN FETCH per carregar la relació en una sola consulta
     * - Usa DISTINCT per evitar duplicats (un llibre amb 2 autors apareixeria 2 cops sense DISTINCT)
     * - Exemple estructura: "SELECT DISTINCT l FROM Llibre l JOIN FETCH l.autors"
     * 
     * @return Llista de llibres amb autors carregats
     */
    public static List<Llibre> findLlibresAmbAutors() {
        try (Session session = factory.openSession()) {
            String hql = "SELECT DISTINCT l FROM Llibre l JOIN FETCH l.autors";
            return session.createQuery(hql, Llibre.class).list();
            }
    }

    /**
     * Retorna informació dels llibres que actualment estan en préstec (préstecs actius).
     * 
     * PISTES HQL:
     * - Has de fer JOIN entre Prestec, Exemplar i Llibre
     * - Filtrar per p.actiu = true
     * - Retornar camps concrets: SELECT l.titol, p.persona.nom FROM ...
     * 
     * @return Llista de Object[] on cada fila conté [títol del llibre, nom de la persona]
     */
    public static List<Object[]> findLlibresEnPrestec() {
        String hql = "SELECT l.titol, p.nom FROM Prestec pr " +
             "JOIN pr.exemplar e " +
             "JOIN e.llibre l " +
             "JOIN pr.persona p " +
             "WHERE pr.actiu = true";
        try (Session session = factory.openSession()) {
            return session.createQuery(hql, Object[].class).list();
        }
    }

    /**
     * Retorna informació de quins llibres estan a quines biblioteques (via Exemplar).
     * 
     * PISTES HQL:
     * - L'entitat Exemplar és la que connecta Llibre amb Biblioteca
     * - SELECT e.llibre.titol, e.biblioteca.nom FROM Exemplar e
     * 
     * @return Llista de Object[] on cada fila conté [títol del llibre, nom de la biblioteca]
     */
    public static List<Object[]> findLlibresAmbBiblioteques() {
        try (Session session = factory.openSession()) {
            String hql = "SELECT e.llibre.titol, e.biblioteca.nom FROM Exemplar e";
            return session.createQuery(hql, Object[].class).list();
        }
    }

    // =================================================================================
    // MÈTODES AUXILIARS (Helpers) - NO CAL MODIFICAR
    // =================================================================================

    /**
     * Retorna tots els objectes d'una entitat.
     * Mètode genèric que funciona amb qualsevol classe anotada amb @Entity.
     */
    public static <T> Collection<T> listCollection(Class<T> clazz) {
        try (Session session = factory.openSession()) {
            List<T> results = session.createQuery("FROM " + clazz.getSimpleName(), clazz).list();
            // Force initialization of lazy proxies before closing session
            for (T entity : results) {
                org.hibernate.Hibernate.initialize(entity);
            }
            return results;
        }
    }

    /**
     * Converteix una col·lecció d'objectes a String per mostrar per consola.
     */
    public static String collectionToString(Class<?> clazz, Collection<?> collection) {
        if (collection == null || collection.isEmpty()) return "   [Cap " + clazz.getSimpleName() + " trobat]";
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) sb.append("   - ").append(obj.toString()).append("\n");
        return sb.toString();
    }

    /**
     * Formata resultats de consultes que retornen Object[] (múltiples columnes).
     */
    public static String formatMultipleResult(List<Object[]> results) {
        if (results == null || results.isEmpty()) return "   [Sense resultats]";
        StringBuilder sb = new StringBuilder();
        for (Object[] row : results) {
            sb.append("   - ");
            for (int i = 0; i < row.length; i++) {
                sb.append(row[i]);
                if (i < row.length - 1) sb.append(" | ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}