package com.securitesociale.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Consultation {

    private int numConsultation;
    private LocalDate dateConsult;
    private String motif;
    private String diagnostic;
    private int numMedecin;
    private int numAssure;
    private String nomMedecin;
    private String nomAssure;
    private LocalDateTime createdAt;

    public Consultation() {}

    public int getNumConsultation() { return numConsultation; }
    public void setNumConsultation(int v) { this.numConsultation = v; }

    public LocalDate getDateConsult() { return dateConsult; }
    public void setDateConsult(LocalDate v) { this.dateConsult = v; }

    public String getMotif() { return motif; }
    public void setMotif(String v) { this.motif = v; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String v) { this.diagnostic = v; }

    public int getNumMedecin() { return numMedecin; }
    public void setNumMedecin(int v) { this.numMedecin = v; }

    public int getNumAssure() { return numAssure; }
    public void setNumAssure(int v) { this.numAssure = v; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String v) { this.nomMedecin = v; }

    public String getNomAssure() { return nomAssure; }
    public void setNomAssure(String v) { this.nomAssure = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
