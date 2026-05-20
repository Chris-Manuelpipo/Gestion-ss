package com.securitesociale.service;

import com.securitesociale.dao.LogDAO;
import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.model.Medecin;
import com.securitesociale.util.SessionManager;

import java.util.List;

public class MedecinService {

    private final MedecinDAO medecinDAO = new MedecinDAO();

    public List<Medecin> findAll() {
        return medecinDAO.findAll();
    }

    public Medecin save(Medecin m) {
        medecinDAO.save(m);
        return m;
    }

    public Medecin update(Medecin m) {
        medecinDAO.update(m);
        return m;
    }

    public void delete(int numMedecin, String nomComplet) {
        medecinDAO.delete(numMedecin);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "ARCHIVE_MEDECIN",
            "Médecin archivé : " + nomComplet + " (ID: " + numMedecin + ")"
        );
    }

    public void updatePassword(int numMedecin, String hashedPassword) {
        medecinDAO.updatePassword(numMedecin, hashedPassword);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "CHANGE_PASSWORD",
            "Mot de passe changé pour le médecin ID: " + numMedecin
        );
    }

    public int count() { return medecinDAO.count(); }
    public int countGeneralistes() { return medecinDAO.countGeneralistes(); }
    public int countSpecialistes() { return medecinDAO.countSpecialistes(); }
    public boolean loginExists(String login) { return medecinDAO.loginExists(login); }
    public List<Medecin> findAllGeneralistes() { return medecinDAO.findAllGeneralistes(); }
}
