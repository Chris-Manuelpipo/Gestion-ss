package com.securitesociale.service;

import com.securitesociale.dao.ConsultationDAO;
import com.securitesociale.dao.FeuilleMaladieDAO;
import com.securitesociale.dao.LogDAO;
import com.securitesociale.dao.PrescriptionDAO;
import com.securitesociale.model.Consultation;
import com.securitesociale.model.FeuilleMaladie;
import com.securitesociale.model.Prescription;
import com.securitesociale.util.SessionManager;

import java.util.List;

public class ConsultationService {

    private final ConsultationDAO consultationDAO = new ConsultationDAO();
    private final FeuilleMaladieDAO feuilleMaladieDAO = new FeuilleMaladieDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    public List<Consultation> findAll() {
        return consultationDAO.findAll();
    }

    public List<Consultation> findByMedecin(int numMedecin) {
        return consultationDAO.findByMedecin(numMedecin);
    }

    public List<Consultation> findByAssure(int numAssure) {
        return consultationDAO.findByAssure(numAssure);
    }

    public Consultation findById(int id) {
        return consultationDAO.findById(id);
    }

    public Consultation save(Consultation c) {
        consultationDAO.save(c);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "CREATE_CONSULTATION",
            "Consultation créée N°" + c.getNumConsultation()
                + " pour assuré ID: " + c.getNumAssure()
        );
        return c;
    }

    public Consultation update(Consultation c) {
        consultationDAO.update(c);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "UPDATE_CONSULTATION",
            "Consultation modifiée N°" + c.getNumConsultation()
        );
        return c;
    }

    public void delete(int id) {
        consultationDAO.delete(id);
        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "DELETE_CONSULTATION",
            "Consultation supprimée N°" + id
        );
    }

    public int count() { return consultationDAO.count(); }
    public int countByMedecin(int numMedecin) { return consultationDAO.countByMedecin(numMedecin); }

    public List<FeuilleMaladie> findFeuillesByConsultation(int numConsultation) {
        return feuilleMaladieDAO.findByConsultation(numConsultation);
    }

    public FeuilleMaladie saveFeuille(FeuilleMaladie f) {
        feuilleMaladieDAO.save(f);
        return f;
    }

    public void updateFeuilleStatut(int numFeuille, String statut) {
        feuilleMaladieDAO.updateStatut(numFeuille, statut);
    }

    public List<Prescription> findPrescriptionsByConsultation(int numConsultation) {
        return prescriptionDAO.findByConsultation(numConsultation);
    }

    public Prescription savePrescription(Prescription p) {
        prescriptionDAO.save(p);
        return p;
    }

    public void deletePrescription(int id) {
        prescriptionDAO.delete(id);
    }
}
