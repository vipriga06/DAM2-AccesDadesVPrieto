package com.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public final class Manager {

	private static SessionFactory sessionFactory;
	private static ServiceRegistry serviceRegistry;

	private Manager() {
		// Utility class
	}

	public static synchronized void createSessionFactory() {
		if (sessionFactory != null) {
			return;
		}

		try {
			Properties properties = loadHibernateProperties();
			Configuration configuration = new Configuration();
			configuration.setProperties(properties);
			configuration.addResource("com/project/Ciutat.hbm.xml");
			configuration.addResource("com/project/Ciutada.hbm.xml");

			StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties());

			serviceRegistry = registryBuilder.build();
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (Exception ex) {
			destroyRegistry();
			throw new IllegalStateException("No s'ha pogut inicialitzar Hibernate", ex);
		}
	}

	private static Properties loadHibernateProperties() throws IOException {
		Properties props = new Properties();
		try (InputStream input = Manager.class.getClassLoader().getResourceAsStream("hibernate.properties")) {
			if (input == null) {
				throw new IOException("No s'ha trobat el fitxer hibernate.properties al classpath");
			}
			props.load(input);
		}
		return props;
	}

	private static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			throw new IllegalStateException("La SessionFactory no està inicialitzada. Crida createSessionFactory() abans.");
		}
		return sessionFactory;
	}

	public static synchronized void close() {
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
		}
		destroyRegistry();
	}

	private static void destroyRegistry() {
		if (serviceRegistry != null) {
			StandardServiceRegistryBuilder.destroy(serviceRegistry);
			serviceRegistry = null;
		}
	}

	public static Ciutat addCiutat(String nom, String pais, int poblacio) {
		Objects.requireNonNull(nom, "El nom de la ciutat és obligatori");
		Objects.requireNonNull(pais, "El país és obligatori");
		return execute(session -> {
			Ciutat ciutat = new Ciutat(nom, pais, poblacio);
			session.persist(ciutat);
			return ciutat;
		});
	}

	public static void updateCiutat(long ciutatId, String nom, String pais, int poblacio) {
		execute(session -> {
			Ciutat ciutat = session.get(Ciutat.class, ciutatId);
			if (ciutat != null) {
				ciutat.setNom(nom);
				ciutat.setPais(pais);
				ciutat.setPoblacio(poblacio);
				session.merge(ciutat);
			}
			return null;
		});
	}

	public static Ciutada addCiutada(String nom, String cognom, int edat) {
		Objects.requireNonNull(nom, "El nom del ciutadà és obligatori");
		Objects.requireNonNull(cognom, "El cognom del ciutadà és obligatori");
		return execute(session -> {
			Ciutada ciutada = new Ciutada(nom, cognom, edat);
			session.persist(ciutada);
			return ciutada;
		});
	}

	public static void updateCiutada(long ciutadaId, String nom, String cognom, int edat) {
		execute(session -> {
			Ciutada ciutada = session.get(Ciutada.class, ciutadaId);
			if (ciutada != null) {
				ciutada.setNom(nom);
				ciutada.setCognom(cognom);
				ciutada.setEdat(edat);
				session.merge(ciutada);
			}
			return null;
		});
	}

	public static void assignCiutadaToCiutat(long ciutadaId, long ciutatId) {
		execute(session -> {
			Ciutada ciutada = session.get(Ciutada.class, ciutadaId);
			if (ciutada != null) {
				ciutada.setCiutatId(ciutatId);
				session.merge(ciutada);
			}
			return null;
		});
	}

	public static List<Ciutat> listCiutats() {
		return listCollection(Ciutat.class, "ciutatId");
	}

	public static List<Ciutada> listCiutadans() {
		return listCollection(Ciutada.class, "ciutadaId");
	}

	public static List<Ciutada> listCiutadansByCiutat(long ciutatId) {
		return execute(session -> session.createQuery(
				"from Ciutada where ciutatId = :ciutatId order by ciutadaId",
				Ciutada.class)
			.setParameter("ciutatId", ciutatId)
			.getResultList());
	}

	public static Ciutat getCiutatWithCiutadans(long ciutatId) {
		return execute(session -> {
			Ciutat ciutat = session.get(Ciutat.class, ciutatId);
			if (ciutat != null) {
				List<Ciutada> ciutadans = session.createQuery(
					"from Ciutada where ciutatId = :ciutatId order by ciutadaId",
					Ciutada.class)
					.setParameter("ciutatId", ciutatId)
					.getResultList();
				ciutat.setCiutadans(new LinkedHashSet<>(ciutadans));
			}
			return ciutat;
		});
	}

	public static <T> List<T> listCollection(Class<T> clazz, String orderByProperty) {
		StringBuilder hql = new StringBuilder("from ").append(clazz.getSimpleName());
		if (orderByProperty != null && !orderByProperty.isBlank()) {
			hql.append(" order by ").append(orderByProperty);
		}

		return execute(session -> session.createQuery(hql.toString(), clazz).getResultList());
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
		execute(session -> {
			Object entity = session.get(clazz, id);
			if (entity != null) {
				session.remove(entity);
			}
			return null;
		});
	}

	private static <R> R execute(Function<Session, R> action) {
		Objects.requireNonNull(action, "L'acció no pot ser nul·la");
		SessionFactory factory = getSessionFactory();
		Transaction tx = null;
		try (Session session = factory.openSession()) {
			tx = session.beginTransaction();
			R result = action.apply(session);
			tx.commit();
			return result;
		} catch (RuntimeException ex) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw ex;
		}
	}
}
