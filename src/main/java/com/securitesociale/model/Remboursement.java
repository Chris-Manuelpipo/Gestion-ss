package com.securitesociale.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Remboursement {

    private int numRemboursement;
    private String nature;
    private BigDecimal taux;
    private BigDecimal montant;
    private String modeReglement;
    private String statut;
    private int numFeuille;
    private LocalDate dateRemboursement;
    private String agentLogin;
    private LocalDateTime createdAt;

    public Remboursement() {}

    public int getNumRemboursement() { return numRemboursement; }
    public void setNumRemboursement(int v) { this.numRemboursement = v; }

    public String getNature() { return nature; }
    public void setNature(String v) { this.nature = v; }

    public BigDecimal getTaux() { return taux; }
    public void setTaux(BigDecimal v) { this.taux = v; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal v) { this.montant = v; }

    public String getModeReglement() { return modeReglement; }
    public void setModeReglement(String v) { this.modeReglement = v; }

    public String getStatut() { return statut; }
    public void setStatut(String v) { this.statut = v; }

    public int getNumFeuille() { return numFeuille; }
    public void setNumFeuille(int v) { this.numFeuille = v; }

    public LocalDate getDateRemboursement() { return dateRemboursement; }
    public void setDateRemboursement(LocalDate v) { this.dateRemboursement = v; }

    public String getAgentLogin() { return agentLogin; }
    public void setAgentLogin(String v) { this.agentLogin = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
