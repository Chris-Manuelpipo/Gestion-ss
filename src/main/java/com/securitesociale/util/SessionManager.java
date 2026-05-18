package com.securitesociale.util;

import com.securitesociale.model.Medecin;
import com.securitesociale.model.Role;

/**
 * Singleton — conserve l'état de la session courante.
 */
public class SessionManager {

    private static SessionManager instance;

    private Role      role;
    private String    loginUtilisateur;
    private Medecin   medecinConnecte;   // null si Agent SS

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    // ── Connexion ─────────────────────────────────────────────────────────────

    public void connecterAgentSS(String login) {
        this.role              = Role.AGENT_SS;
        this.loginUtilisateur  = login;
        this.medecinConnecte   = null;
    }

    public void connecterMedecin(Medecin medecin) {
        this.role              = Role.MEDECIN;
        this.loginUtilisateur  = medecin.getLogin();
        this.medecinConnecte   = medecin;
    }

    public void deconnecter() {
        this.role             = null;
        this.loginUtilisateur = null;
        this.medecinConnecte  = null;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Role    getRole()              { return role; }
    public String  getLoginUtilisateur()  { return loginUtilisateur; }
    public Medecin getMedecinConnecte()   { return medecinConnecte; }
    public boolean isAgentSS()            { return role == Role.AGENT_SS; }
    public boolean isMedecin()            { return role == Role.MEDECIN; }
    public boolean isConnecte()           { return role != null; }

    public String getNomAffichage() {
        if (role == Role.AGENT_SS) return "Agent SS";
        if (medecinConnecte != null)
            return "Dr " + medecinConnecte.getPrenom() + " " + medecinConnecte.getNom();
        return loginUtilisateur;
    }

    public String getRoleAffichage() {
        if (role == Role.AGENT_SS) return "Administrateur";
        if (medecinConnecte != null)
            return medecinConnecte.getTypeMedecin() != null
                    ? medecinConnecte.getTypeMedecin().getLibelle()
                    : "Médecin";
        return "";
    }
}
