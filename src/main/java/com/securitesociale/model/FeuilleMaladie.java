package com.securitesociale.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FeuilleMaladie {

    private int numFeuille;
    private LocalDate dateEmission;
    private String statut;
    private int numConsultation;
    private LocalDateTime createdAt;

    private int version;
    private BigDecimal temperature;
    private String tensionArterielle;
    private BigDecimal poids;
    private BigDecimal taille;
    private Integer frequenceCardiaque;
    private Integer frequenceRespiratoire;
    private BigDecimal saturationOxygene;
    private String antecedents;
    private String symptomes;
    private String diagnostic;
    private String traitementPrescrit;
    private String observations;
    private Integer numMedecin;

    private String nomAssure;
    private String motif;
    private LocalDate dateConsult;

    public FeuilleMaladie() {}

    public int getNumFeuille() { return numFeuille; }
    public void setNumFeuille(int v) { this.numFeuille = v; }

    public LocalDate getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDate v) { this.dateEmission = v; }

    public String getStatut() { return statut; }
    public void setStatut(String v) { this.statut = v; }

    public int getNumConsultation() { return numConsultation; }
    public void setNumConsultation(int v) { this.numConsultation = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public int getVersion() { return version; }
    public void setVersion(int v) { this.version = v; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal v) { this.temperature = v; }

    public String getTensionArterielle() { return tensionArterielle; }
    public void setTensionArterielle(String v) { this.tensionArterielle = v; }

    public BigDecimal getPoids() { return poids; }
    public void setPoids(BigDecimal v) { this.poids = v; }

    public BigDecimal getTaille() { return taille; }
    public void setTaille(BigDecimal v) { this.taille = v; }

    public Integer getFrequenceCardiaque() { return frequenceCardiaque; }
    public void setFrequenceCardiaque(Integer v) { this.frequenceCardiaque = v; }

    public Integer getFrequenceRespiratoire() { return frequenceRespiratoire; }
    public void setFrequenceRespiratoire(Integer v) { this.frequenceRespiratoire = v; }

    public BigDecimal getSaturationOxygene() { return saturationOxygene; }
    public void setSaturationOxygene(BigDecimal v) { this.saturationOxygene = v; }

    public String getAntecedents() { return antecedents; }
    public void setAntecedents(String v) { this.antecedents = v; }

    public String getSymptomes() { return symptomes; }
    public void setSymptomes(String v) { this.symptomes = v; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String v) { this.diagnostic = v; }

    public String getTraitementPrescrit() { return traitementPrescrit; }
    public void setTraitementPrescrit(String v) { this.traitementPrescrit = v; }

    public String getObservations() { return observations; }
    public void setObservations(String v) { this.observations = v; }

    public Integer getNumMedecin() { return numMedecin; }
    public void setNumMedecin(Integer v) { this.numMedecin = v; }

    public String getNomAssure() { return nomAssure; }
    public void setNomAssure(String v) { this.nomAssure = v; }

    public String getMotif() { return motif; }
    public void setMotif(String v) { this.motif = v; }

    public LocalDate getDateConsult() { return dateConsult; }
    public void setDateConsult(LocalDate v) { this.dateConsult = v; }
}
