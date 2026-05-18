package com.securitesociale.model;

import java.time.LocalDate;

public class Assure {

    private int       numAssure;
    private String    nom;
    private String    prenom;
    private String    email;
    private LocalDate dateNaissance;
    private String    sexe;
    private String    numCompteBancaire;
    private int       numMedecinTraitant;   // FK
    private String    nomMedecinTraitant;   // champ calculé pour affichage

    public Assure() {}

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int      getNumAssure()     { return numAssure; }
    public void     setNumAssure(int v){ this.numAssure = v; }

    public String   getNom()           { return nom; }
    public void     setNom(String v)   { this.nom = v; }

    public String   getPrenom()        { return prenom; }
    public void     setPrenom(String v){ this.prenom = v; }

    public String   getEmail()         { return email; }
    public void     setEmail(String v) { this.email = v; }

    public LocalDate getDateNaissance()          { return dateNaissance; }
    public void      setDateNaissance(LocalDate v) { this.dateNaissance = v; }

    public String   getSexe()          { return sexe; }
    public void     setSexe(String v)  { this.sexe = v; }

    public String   getNumCompteBancaire()       { return numCompteBancaire; }
    public void     setNumCompteBancaire(String v){ this.numCompteBancaire = v; }

    public int      getNumMedecinTraitant()         { return numMedecinTraitant; }
    public void     setNumMedecinTraitant(int v)    { this.numMedecinTraitant = v; }

    public String   getNomMedecinTraitant()          { return nomMedecinTraitant; }
    public void     setNomMedecinTraitant(String v)  { this.nomMedecinTraitant = v; }

    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() { return getNomComplet(); }
}
