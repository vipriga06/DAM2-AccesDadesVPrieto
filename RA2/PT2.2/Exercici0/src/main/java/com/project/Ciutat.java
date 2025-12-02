package com.project;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Ciutat implements Serializable {

	private static final long serialVersionUID = 1L;

	private long ciutatId;
	private String nom;
	private String pais;
	private int poblacio;

	// Relació lògica (no persistent) per tenir els ciutadans vinculats en memòria
	private transient Set<Ciutada> ciutadans = new LinkedHashSet<>();

	public Ciutat() {
		// Required by Hibernate
	}

	public Ciutat(String nom, String pais, int poblacio) {
		this.nom = nom;
		this.pais = pais;
		this.poblacio = poblacio;
	}

	public long getCiutatId() {
		return ciutatId;
	}

	public void setCiutatId(long ciutatId) {
		this.ciutatId = ciutatId;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public int getPoblacio() {
		return poblacio;
	}

	public void setPoblacio(int poblacio) {
		this.poblacio = poblacio;
	}

	public Set<Ciutada> getCiutadans() {
		return Collections.unmodifiableSet(ciutadans);
	}

	public void setCiutadans(Set<Ciutada> ciutadans) {
		if (ciutadans == null) {
			this.ciutadans = new LinkedHashSet<>();
		} else {
			this.ciutadans = new LinkedHashSet<>(ciutadans);
		}
	}

	public void addCiutada(Ciutada ciutada) {
		if (ciutada != null) {
			ciutadans.add(ciutada);
		}
	}

	public void clearCiutadans() {
		ciutadans.clear();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Ciutat ciutat = (Ciutat) o;
		return ciutatId == ciutat.ciutatId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ciutatId);
	}

	@Override
	public String toString() {
		return "Ciutat{" +
			"ciutatId=" + ciutatId +
			", nom='" + nom + '\'' +
			", pais='" + pais + '\'' +
			", poblacio=" + poblacio +
			'}';
	}
}
