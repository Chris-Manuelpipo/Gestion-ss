package com.securitesociale.model;

import java.time.LocalDate;

public class Medecin {

    private int        numMedecin;
    private String     nom;
    private String     prenom;
    private String     email;
    private LocalDate  dateNaissance;
    private String     sexe;
    private String     login;
    private String     motDePasse;
    private TypeMedecin typeMedecin;
    private String     typeFormation;   // Généraliste
    private String     nomSpecialite;   // Spécialiste
    private boolean    actif = true;

    public Medecin() {}

    public Medecin(String nom, String prenom, String login, String motDePasse, TypeMedecin type) {
        this.nom = nom; this.prenom = prenom;
        this.login = login; this.motDePasse = motDePasse;
        this.typeMedecin = type;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int         getNumMedecin()     { return numMedecin; }
    public void        setNumMedecin(int n){ this.numMedecin = n; }

    public String      getNom()            { return nom; }
    public void        setNom(String v)    { this.nom = v; }

    public String      getPrenom()         { return prenom; }
    public void        setPrenom(String v) { this.prenom = v; }

    public String      getEmail()          { return email; }
    public void        setEmail(String v)  { this.email = v; }

    public LocalDate   getDateNaissance()        { return dateNaissance; }
    public void        setDateNaissance(LocalDate v) { this.dateNaissance = v; }

    public String      getSexe()           { return sexe; }
    public void        setSexe(String v)   { this.sexe = v; }

    public String      getLogin()          { return login; }
    public void        setLogin(String v)  { this.login = v; }

    public String      getMotDePasse()     { return motDePasse; }
    public void        setMotDePasse(String v) { this.motDePasse = v; }

    public TypeMedecin getTypeMedecin()    { return typeMedecin; }
    public void        setTypeMedecin(TypeMedecin v) { this.typeMedecin = v; }

    public String      getTypeFormation()  { return typeFormation; }
    public void        setTypeFormation(String v) { this.typeFormation = v; }

    public String      getNomSpecialite()  { return nomSpecialite; }
    public void        setNomSpecialite(String v) { this.nomSpecialite = v; }

    public boolean     isActif()           { return actif; }
    public void        setActif(boolean v) { this.actif = v; }

    public String getNomComplet() {
        return "Dr " + prenom + " " + nom;
    }

    @Override
    public String toString() { return getNomComplet(); }
}
