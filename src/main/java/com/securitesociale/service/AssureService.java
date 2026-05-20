package com.securitesociale.service;

import com.securitesociale.dao.AssureDAO;
import com.securitesociale.dao.LogDAO;
import com.securitesociale.model.Assure;
import com.securitesociale.util.SessionManager;

import java.util.List;

public class AssureService {

    private final AssureDAO assureDAO = new AssureDAO();

    public List<Assure> findAll() {
        return assureDAO.findAll();
    }

    public Assure save(Assure a) {
        assureDAO.save(a);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "CREATE_ASSURE",
            "Assuré créé : " + a.getNomComplet() + " (ID: " + a.getNumAssure() + ")"
        );
        return a;
    }

    public Assure update(Assure a) {
        assureDAO.update(a);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "UPDATE_ASSURE",
            "Assuré modifié : " + a.getNomComplet() + " (ID: " + a.getNumAssure() + ")"
        );
        return a;
    }

    public void delete(int numAssure, String nomComplet) {
        assureDAO.delete(numAssure);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "DELETE_ASSURE",
            "Assuré supprimé : " + nomComplet + " (ID: " + numAssure + ")"
        );
    }

    public int count() {
        return assureDAO.count();
    }
}
