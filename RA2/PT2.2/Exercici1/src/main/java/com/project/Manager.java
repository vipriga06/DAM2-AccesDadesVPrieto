package com.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

public final class Manager {

	private static final String PERSISTENCE_UNIT = "CiutatsPU";
	private static EntityManagerFactory entityManagerFactory;

	private Manager() {
	}

	public static synchronized void createSessionFactory() {
		if (entityManagerFactory != null) {
			return;
		}
		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, loadOverrides());
	}

	public static synchronized void close() {
		if (entityManagerFactory != null) {
			entityManagerFactory.close();
			entityManagerFactory = null;
		}
	}

	private static EntityManagerFactory getEntityManagerFactory() {
		if (entityManagerFactory == null) {
			throw new IllegalStateException("Cal inicialitzar Hibernate cridant createSessionFactory() abans d'usar Manager");
		}
		return entityManagerFactory;
	}

	private static Properties loadOverrides() {
		Properties properties = new Properties();
		try (InputStream input = Manager.class.getClassLoader().getResourceAsStream("hibernate.properties")) {
			if (input != null) {
				properties.load(input);
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Error llegint hibernate.properties", ex);
		}
		return properties;
	}

	public static Ciutat addCiutat(String nom, String pais, int poblacio) {
		Objects.requireNonNull(nom, "El nom de la ciutat és obligatori");
		Objects.requireNonNull(pais, "El país de la ciutat és obligatori");
		return execute(entityManager -> {
			Ciutat ciutat = new Ciutat(nom, pais, poblacio);
			entityManager.persist(ciutat);
			return ciutat;
		});
	}

	public static Ciutada addCiutada(String nom, String cognom, int edat) {
		Objects.requireNonNull(nom, "El nom del ciutadà és obligatori");
		Objects.requireNonNull(cognom, "El cognom del ciutadà és obligatori");
		return execute(entityManager -> {
			Ciutada ciutada = new Ciutada(nom, cognom, edat);
			entityManager.persist(ciutada);
			return ciutada;
		});
	}

	public static Ciutat updateCiutat(long ciutatId, String nom, String pais, int poblacio, Set<Ciutada> ciutadans) {
		return execute(entityManager -> {
			Ciutat ciutat = entityManager.find(Ciutat.class, ciutatId);
			if (ciutat == null) {
				return null;
			}
			ciutat.setNom(nom);
			ciutat.setPais(pais);
			ciutat.setPoblacio(poblacio);

			if (ciutadans == null || ciutadans.isEmpty()) {
				ciutat.clearCiutadans();
			} else {
				Set<Ciutada> managed = new LinkedHashSet<>();
				for (Ciutada ciutada : ciutadans) {
					if (ciutada == null || ciutada.getCiutadaId() == null) {
						continue;
					}
					Ciutada attached = entityManager.find(Ciutada.class, ciutada.getCiutadaId());
					if (attached != null) {
						managed.add(attached);
					}
				}
				ciutat.assignCiutadans(managed);
			}

			return ciutat;
		});
	}

	public static Ciutada updateCiutada(long ciutadaId, String nom, String cognom, int edat) {
		return execute(entityManager -> {
			Ciutada ciutada = entityManager.find(Ciutada.class, ciutadaId);
			if (ciutada == null) {
				return null;
			}
			ciutada.setNom(nom);
			ciutada.setCognom(cognom);
			ciutada.setEdat(edat);
			return ciutada;
		});
	}

	public static <T> List<T> listCollection(Class<T> clazz, String orderByProperty) {
		String alias = "e";
		StringBuilder jpql = new StringBuilder("select ").append(alias).append(" from ")
			.append(clazz.getSimpleName()).append(' ').append(alias);
		if (orderByProperty != null && !orderByProperty.isBlank()) {
			jpql.append(" order by ").append(alias).append('.').append(orderByProperty);
		}
		return execute(entityManager -> {
			TypedQuery<T> query = entityManager.createQuery(jpql.toString(), clazz);
			return query.getResultList();
		});
	}

	public static String collectionToString(Class<?> clazz, List<?> elements) {
		if (elements == null || elements.isEmpty()) {
			return "No hi ha elements per a " + clazz.getSimpleName();
		}
		StringJoiner joiner = new StringJoiner(System.lineSeparator());
		elements.forEach(element -> joiner.add(Objects.toString(element)));
		return joiner.toString();
	}

	public static void delete(Class<?> clazz, long id) {
		execute(entityManager -> {
			Object entity = entityManager.find(clazz, id);
			if (entity != null) {
				entityManager.remove(entity);
			}
			return null;
		});
	}

	public static Ciutat getCiutatWithCiutadans(long ciutatId) {
		return execute(entityManager -> {
			Ciutat ciutat = entityManager.find(Ciutat.class, ciutatId);
			if (ciutat != null) {
				ciutat.getCiutadans().size();
			}
			return ciutat;
		});
	}

	public static List<Ciutada> listCiutadansDeCiutat(long ciutatId) {
		return execute(entityManager -> entityManager.createQuery(
				"select c from Ciutada c where c.ciutat.ciutatId = :ciutatId order by c.ciutadaId",
				Ciutada.class)
			.setParameter("ciutatId", ciutatId)
			.getResultList());
	}

	private static <R> R execute(Function<EntityManager, R> action) {
		Objects.requireNonNull(action, "L'acció no pot ser nul·la");
		EntityManagerFactory factory = getEntityManagerFactory();
		EntityTransaction transaction = null;
		try (EntityManager entityManager = factory.createEntityManager()) {
			transaction = entityManager.getTransaction();
			transaction.begin();
			R result = action.apply(entityManager);
			transaction.commit();
			return result;
		} catch (RuntimeException ex) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw ex;
		}
	}
}
