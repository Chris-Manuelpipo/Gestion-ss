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

public class FeuilleMaladieService {

    private final FeuilleMaladieDAO feuilleDAO = new FeuilleMaladieDAO();
    private final ConsultationDAO consultationDAO = new ConsultationDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    public List<FeuilleMaladie> findAll() {
        return feuilleDAO.findAll();
    }

    public List<FeuilleMaladie> findByMedecin(int numMedecin) {
        return feuilleDAO.findByMedecin(numMedecin);
    }

    public FeuilleMaladie findById(int numFeuille) {
        return feuilleDAO.findById(numFeuille);
    }

    public List<Prescription> findPrescriptions(int numConsultation) {
        return prescriptionDAO.findByConsultation(numConsultation);
    }

    public FeuilleMaladie saveWithConsultation(FeuilleMaladie feuille, Consultation consultation, List<Prescription> prescriptions) {
        Consultation saved = consultationDAO.save(consultation);
        int numConsultation = saved.getNumConsultation();

        feuille.setNumConsultation(numConsultation);
        FeuilleMaladie savedFeuille = feuilleDAO.save(feuille);

        if (prescriptions != null) {
            for (Prescription p : prescriptions) {
                p.setNumConsultation(numConsultation);
                prescriptionDAO.save(p);
            }
        }

        LogDAO.insert(
            SessionManager.getInstance().getLoginUtilisateur(),
            "CREATE_FEUILLE_MALADIE",
            "Feuille de maladie N°" + savedFeuille.getNumFeuille()
                + " créée pour consultation N°" + numConsultation
                + " | " + (prescriptions != null ? prescriptions.size() : 0) + " prescription(s)"
        );

        return savedFeuille;
    }
}
