package com.securitesociale.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Prescription {

    private int numPrescription;
    private LocalDate datePrescription;
    private String type;
    private String contenu;
    private int numConsultation;
    private String codeMedicament;
    private String nomMedicament;
    private String posologie;
    private String dosage;
    private String typeExamen;
    private String motifMedical;
    private Integer numSpecialiste;
    private String nomSpecialiste;
    private LocalDateTime createdAt;

    public Prescription() {}

    public int getNumPrescription() { return numPrescription; }
    public void setNumPrescription(int v) { this.numPrescription = v; }

    public LocalDate getDatePrescription() { return datePrescription; }
    public void setDatePrescription(LocalDate v) { this.datePrescription = v; }

    public String getType() { return type; }
    public void setType(String v) { this.type = v; }

    public String getContenu() { return contenu; }
    public void setContenu(String v) { this.contenu = v; }

    public int getNumConsultation() { return numConsultation; }
    public void setNumConsultation(int v) { this.numConsultation = v; }

    public String getCodeMedicament() { return codeMedicament; }
    public void setCodeMedicament(String v) { this.codeMedicament = v; }

    public String getNomMedicament() { return nomMedicament; }
    public void setNomMedicament(String v) { this.nomMedicament = v; }

    public String getPosologie() { return posologie; }
    public void setPosologie(String v) { this.posologie = v; }

    public String getDosage() { return dosage; }
    public void setDosage(String v) { this.dosage = v; }

    public String getTypeExamen() { return typeExamen; }
    public void setTypeExamen(String v) { this.typeExamen = v; }

    public String getMotifMedical() { return motifMedical; }
    public void setMotifMedical(String v) { this.motifMedical = v; }

    public Integer getNumSpecialiste() { return numSpecialiste; }
    public void setNumSpecialiste(Integer v) { this.numSpecialiste = v; }

    public String getNomSpecialiste() { return nomSpecialiste; }
    public void setNomSpecialiste(String v) { this.nomSpecialiste = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
