package com.project;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ciutada")
public class Ciutada implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ciutada_id")
	private Long ciutadaId;

	@Column(name = "nom", nullable = false, length = 100)
	private String nom;

	@Column(name = "cognom", nullable = false, length = 100)
	private String cognom;

	@Column(name = "edat", nullable = false)
	private int edat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ciutat_id")
	private Ciutat ciutat;

	public Ciutada() {
		// Required by JPA
	}

	public Ciutada(String nom, String cognom, int edat) {
		this.nom = nom;
		this.cognom = cognom;
		this.edat = edat;
	}

	public Long getCiutadaId() {
		return ciutadaId;
	}

	public void setCiutadaId(Long ciutadaId) {
		this.ciutadaId = ciutadaId;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getCognom() {
		return cognom;
	}

	public void setCognom(String cognom) {
		this.cognom = cognom;
	}

	public int getEdat() {
		return edat;
	}

	public void setEdat(int edat) {
		this.edat = edat;
	}

	public Ciutat getCiutat() {
		return ciutat;
	}

	public void setCiutat(Ciutat ciutat) {
		this.ciutat = ciutat;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Ciutada ciutada = (Ciutada) o;
		return Objects.equals(ciutadaId, ciutada.ciutadaId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ciutadaId);
	}

	@Override
	public String toString() {
		return "Ciutada{" +
			"ciutadaId=" + ciutadaId +
			", nom='" + nom + '\'' +
			", cognom='" + cognom + '\'' +
			", edat=" + edat +
			'}';
	}
}
