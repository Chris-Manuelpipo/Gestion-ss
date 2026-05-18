-- ═══════════════════════════════════════════════════════════════
--  Script d'initialisation — Gestion Sécurité Sociale
--  PostgreSQL — ENSPY 2026
--  Usage : psql -U postgres -d securite_sociale -f init.sql
-- ═══════════════════════════════════════════════════════════════

-- Créer la base si elle n'existe pas (exécuter en dehors de la DB)
-- CREATE DATABASE securite_sociale;

-- Agents de sécurité sociale
CREATE TABLE IF NOT EXISTS agents_ss (
    id           SERIAL PRIMARY KEY,
    login        VARCHAR(50)  UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,          -- BCrypt hash
    created_at   TIMESTAMP DEFAULT NOW()
);

-- Médecins (généralistes et spécialistes)
CREATE TABLE IF NOT EXISTS medecins (
    num_medecin    SERIAL PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL,
    email          VARCHAR(150),
    date_naissance DATE,
    sexe           VARCHAR(10),
    login          VARCHAR(50)  UNIQUE NOT NULL,
    mot_de_passe   VARCHAR(255) NOT NULL,         -- BCrypt hash
    type_medecin   VARCHAR(20)  CHECK (type_medecin IN ('GENERALISTE','SPECIALISTE')),
    type_formation VARCHAR(100),                  -- Généraliste uniquement
    nom_specialite VARCHAR(100),                  -- Spécialiste uniquement
    actif          BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT NOW()
);

-- Assurés
CREATE TABLE IF NOT EXISTS assures (
    num_assure            SERIAL PRIMARY KEY,
    nom                   VARCHAR(100) NOT NULL,
    prenom                VARCHAR(100) NOT NULL,
    email                 VARCHAR(150),
    date_naissance        DATE,
    sexe                  VARCHAR(10),
    num_compte_bancaire   VARCHAR(50),
    num_medecin_traitant  INT REFERENCES medecins(num_medecin) ON DELETE SET NULL,
    created_at            TIMESTAMP DEFAULT NOW()
);

-- Consultations
CREATE TABLE IF NOT EXISTS consultations (
    num_consultation SERIAL PRIMARY KEY,
    date_consult     DATE NOT NULL DEFAULT CURRENT_DATE,
    motif            TEXT,
    diagnostic       TEXT,
    num_medecin      INT NOT NULL REFERENCES medecins(num_medecin),
    num_assure       INT NOT NULL REFERENCES assures(num_assure),
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Feuilles de maladie
CREATE TABLE IF NOT EXISTS feuilles_maladie (
    num_feuille      SERIAL PRIMARY KEY,
    date_emission    DATE NOT NULL DEFAULT CURRENT_DATE,
    statut           VARCHAR(20) DEFAULT 'EN_ATTENTE'
                     CHECK (statut IN ('EN_ATTENTE','VALIDEE','REJETEE')),
    num_consultation INT NOT NULL REFERENCES consultations(num_consultation),
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Prescriptions (médicaments ou consultation spécialiste)
CREATE TABLE IF NOT EXISTS prescriptions (
    num_prescription  SERIAL PRIMARY KEY,
    date_prescription DATE NOT NULL DEFAULT CURRENT_DATE,
    type              VARCHAR(30) NOT NULL
                      CHECK (type IN ('MEDICAMENT','CONSULTATION_SPECIALISTE')),
    contenu           TEXT,
    num_consultation  INT NOT NULL REFERENCES consultations(num_consultation),
    -- Prescription médicament
    code_medicament   VARCHAR(50),
    nom_medicament    VARCHAR(100),
    posologie         VARCHAR(200),
    -- Prescription consultation spécialiste
    type_examen       VARCHAR(100),
    motif_medical     TEXT,
    num_specialiste   INT REFERENCES medecins(num_medecin),
    created_at        TIMESTAMP DEFAULT NOW()
);

-- Remboursements
CREATE TABLE IF NOT EXISTS remboursements (
    num_remboursement SERIAL PRIMARY KEY,
    nature            VARCHAR(100),
    taux              NUMERIC(5,2)  NOT NULL,
    montant           NUMERIC(10,2) NOT NULL,
    mode_reglement    VARCHAR(20)
                      CHECK (mode_reglement IN ('VIREMENT','CASH')),
    statut            VARCHAR(20) DEFAULT 'EN_ATTENTE'
                      CHECK (statut IN ('EN_ATTENTE','EFFECTUE')),
    num_feuille       INT NOT NULL REFERENCES feuilles_maladie(num_feuille),
    created_at        TIMESTAMP DEFAULT NOW()
);

-- Journal d'audit — Exigence 3.5 Traçabilité
CREATE TABLE IF NOT EXISTS logs (
    id          SERIAL PRIMARY KEY,
    utilisateur VARCHAR(100),
    action      VARCHAR(100),    -- ex: LOGIN_SUCCESS, CREATE_ASSURE, etc.
    details     TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- ── Index de performance ──────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_assures_nom         ON assures(nom, prenom);
CREATE INDEX IF NOT EXISTS idx_medecins_login      ON medecins(login);
CREATE INDEX IF NOT EXISTS idx_consultations_date  ON consultations(date_consult);
CREATE INDEX IF NOT EXISTS idx_remboursements_stat ON remboursements(statut);
CREATE INDEX IF NOT EXISTS idx_logs_created_at     ON logs(created_at);

-- ── Note ─────────────────────────────────────────────────────────────
-- Le compte agent SS (admin / Admin@2026) est créé automatiquement
-- au premier démarrage de l'application (App.java → initializeDatabase).
