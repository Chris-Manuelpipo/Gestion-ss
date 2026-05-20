package com.securitesociale.service;

import com.securitesociale.dao.PrescriptionDAO;
import com.securitesociale.model.Prescription;

import java.util.List;

public class PrescriptionService {

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    public List<Prescription> findAll() {
        return prescriptionDAO.findAll();
    }

    public List<Prescription> findByConsultation(int numConsultation) {
        return prescriptionDAO.findByConsultation(numConsultation);
    }

    public Prescription save(Prescription p) {
        return prescriptionDAO.save(p);
    }

    public void delete(int id) {
        prescriptionDAO.delete(id);
    }
}
