package com.securitesociale.service;

import com.securitesociale.dao.LogDAO;
import com.securitesociale.dao.RemboursementDAO;
import com.securitesociale.model.Remboursement;
import com.securitesociale.util.SessionManager;

import java.util.List;

public class RemboursementService {

    private final RemboursementDAO remboursementDAO = new RemboursementDAO();

    public List<Remboursement> findAll() {
        return remboursementDAO.findAll();
    }

    public List<Remboursement> findByStatut(String statut) {
        return remboursementDAO.findByStatut(statut);
    }

    public Remboursement findById(int id) {
        return remboursementDAO.findById(id);
    }

    public Remboursement save(Remboursement r) {
        remboursementDAO.save(r);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "CREATE_REMBOURSEMENT",
            "Remboursement créé N°" + r.getNumRemboursement()
                + " — " + r.getNature() + " (" + r.getMontant() + " FCFA)"
        );
        return r;
    }

    public void valider(int numRemboursement) {
        String agent = SessionManager.getInstance().getLoginUtilisateur();
        remboursementDAO.updateStatut(numRemboursement, "EFFECTUE", agent);
        LogDAO.insert(
            agent,
            "VALIDER_REMBOURSEMENT",
            "Remboursement validé N°" + numRemboursement
        );
    }

    public void rejeter(int numRemboursement) {
        String agent = SessionManager.getInstance().getLoginUtilisateur();
        remboursementDAO.updateStatut(numRemboursement, "REJETEE", agent);
        LogDAO.insert(
            agent,
            "REJETER_REMBOURSEMENT",
            "Remboursement rejeté N°" + numRemboursement
        );
    }

    public void delete(int id) {
        remboursementDAO.delete(id);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "DELETE_REMBOURSEMENT",
            "Remboursement supprimé N°" + id
        );
    }

    public int count() { return remboursementDAO.count(); }
    public int countEnAttente() { return remboursementDAO.countEnAttente(); }
}
