package com.securitesociale.model;

public enum TypeMedecin {
    GENERALISTE("Médecin Généraliste"),
    SPECIALISTE("Médecin Spécialiste");

    private final String libelle;

    TypeMedecin(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
