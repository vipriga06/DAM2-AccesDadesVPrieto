package com.project;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ciutat")
public class Ciutat implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ciutat_id")
	private Long ciutatId;

	@Column(name = "nom", nullable = false, length = 100)
	private String nom;

	@Column(name = "pais", nullable = false, length = 100)
	private String pais;

	@Column(name = "poblacio", nullable = false)
	private int poblacio;

	@OneToMany(
		mappedBy = "ciutat",
		cascade = {CascadeType.PERSIST, CascadeType.MERGE},
		orphanRemoval = false,
		fetch = FetchType.LAZY
	)
	private Set<Ciutada> ciutadans = new LinkedHashSet<>();

	public Ciutat() {
	}

	public Ciutat(String nom, String pais, int poblacio) {
		this.nom = nom;
		this.pais = pais;
		this.poblacio = poblacio;
	}

	public Long getCiutatId() {
		return ciutatId;
	}

	public void setCiutatId(Long ciutatId) {
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

	public void assignCiutadans(Set<Ciutada> nousCiutadans) {
		ciutadans.clear();
		if (nousCiutadans == null) {
			return;
		}
		for (Ciutada ciutada : nousCiutadans) {
			addCiutada(ciutada);
		}
	}

	public void addCiutada(Ciutada ciutada) {
		if (ciutada == null) {
			return;
		}
		ciutadans.add(ciutada);
		ciutada.setCiutat(this);
	}

	public void clearCiutadans() {
		for (Ciutada ciutada : ciutadans) {
			ciutada.setCiutat(null);
		}
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
		return Objects.equals(ciutatId, ciutat.ciutatId);
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
