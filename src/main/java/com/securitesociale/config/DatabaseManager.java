package com.securitesociale.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {

    private static HikariDataSource dataSource;

    static {
        try (InputStream is = DatabaseManager.class.getResourceAsStream("/application.properties")) {
            Properties props = new Properties();
            props.load(is);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.max-size", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.min-idle", "2")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.connection-timeout", "30000")));
            config.setIdleTimeout(600_000);
            config.setMaxLifetime(1_800_000);
            config.setAutoCommit(true);

            dataSource = new HikariDataSource(config);
            System.out.println("[DB] Connexion PostgreSQL établie");
        } catch (Exception e) {
            throw new RuntimeException("Impossible de se connecter à la base de données.\n" +
                    "Vérifiez application.properties et que PostgreSQL est démarré.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DB] Pool de connexions fermé");
        }
    }

    // ── Initialisation du schéma ──────────────────────────────────────────────

    public static void initSchema() {
        String[] ddl = {
            // Agents SS
            """
            CREATE TABLE IF NOT EXISTS agents_ss (
                id           SERIAL PRIMARY KEY,
                login        VARCHAR(50)  UNIQUE NOT NULL,
                mot_de_passe VARCHAR(255) NOT NULL,
                created_at   TIMESTAMP DEFAULT NOW()
            )
            """,
            // Médecins
            """
            CREATE TABLE IF NOT EXISTS medecins (
                num_medecin    SERIAL PRIMARY KEY,
                nom            VARCHAR(100) NOT NULL,
                prenom         VARCHAR(100) NOT NULL,
                email          VARCHAR(150),
                date_naissance DATE,
                sexe           VARCHAR(10),
                login          VARCHAR(50) UNIQUE NOT NULL,
                mot_de_passe   VARCHAR(255) NOT NULL,
                type_medecin   VARCHAR(20) CHECK (type_medecin IN ('GENERALISTE','SPECIALISTE')),
                type_formation VARCHAR(100),
                nom_specialite VARCHAR(100),
                actif          BOOLEAN DEFAULT TRUE,
                created_at     TIMESTAMP DEFAULT NOW()
            )
            """,
            // Assurés
            """
            CREATE TABLE IF NOT EXISTS assures (
                num_assure            SERIAL PRIMARY KEY,
                nom                   VARCHAR(100) NOT NULL,
                prenom                VARCHAR(100) NOT NULL,
                email                 VARCHAR(150),
                date_naissance        DATE,
                sexe                  VARCHAR(10),
                num_compte_bancaire   VARCHAR(50),
                num_medecin_traitant  INT REFERENCES medecins(num_medecin),
                created_at            TIMESTAMP DEFAULT NOW()
            )
            """,
            // Consultations
            """
            CREATE TABLE IF NOT EXISTS consultations (
                num_consultation SERIAL PRIMARY KEY,
                date_consult     DATE NOT NULL DEFAULT CURRENT_DATE,
                motif            TEXT,
                diagnostic       TEXT,
                num_medecin      INT NOT NULL REFERENCES medecins(num_medecin),
                num_assure       INT NOT NULL REFERENCES assures(num_assure),
                created_at       TIMESTAMP DEFAULT NOW()
            )
            """,
            // Feuilles de maladie
            """
            CREATE TABLE IF NOT EXISTS feuilles_maladie (
                num_feuille      SERIAL PRIMARY KEY,
                date_emission    DATE NOT NULL DEFAULT CURRENT_DATE,
                statut           VARCHAR(20) DEFAULT 'EN_ATTENTE',
                num_consultation INT NOT NULL REFERENCES consultations(num_consultation),
                created_at       TIMESTAMP DEFAULT NOW()
            )
            """,
            // Prescriptions
            """
            CREATE TABLE IF NOT EXISTS prescriptions (
                num_prescription SERIAL PRIMARY KEY,
                date_prescription DATE NOT NULL DEFAULT CURRENT_DATE,
                type             VARCHAR(30) NOT NULL,
                contenu          TEXT,
                num_consultation INT NOT NULL REFERENCES consultations(num_consultation),
                code_medicament  VARCHAR(50),
                nom_medicament   VARCHAR(100),
                posologie        VARCHAR(200),
                type_examen      VARCHAR(100),
                motif_medical    TEXT,
                num_specialiste  INT REFERENCES medecins(num_medecin),
                created_at       TIMESTAMP DEFAULT NOW()
            )
            """,
            // Remboursements
            """
            CREATE TABLE IF NOT EXISTS remboursements (
                num_remboursement SERIAL PRIMARY KEY,
                nature            VARCHAR(100),
                taux              NUMERIC(5,2)  NOT NULL,
                montant           NUMERIC(10,2) NOT NULL,
                mode_reglement    VARCHAR(20) CHECK (mode_reglement IN ('VIREMENT','CASH')),
                statut            VARCHAR(20) DEFAULT 'EN_ATTENTE',
                num_feuille       INT NOT NULL REFERENCES feuilles_maladie(num_feuille),
                created_at        TIMESTAMP DEFAULT NOW()
            )
            """,
            // Journal d'audit (Exigence 3.5 — Traçabilité)
            """
            CREATE TABLE IF NOT EXISTS logs (
                id          SERIAL PRIMARY KEY,
                utilisateur VARCHAR(100),
                action      VARCHAR(100),
                details     TEXT,
                ip_address  VARCHAR(50),
                created_at  TIMESTAMP DEFAULT NOW()
            )
            """
        };

        // Migrations — garantit la présence de toutes les colonnes même si la table
        // a été créée par une version antérieure du schéma.
        String[] migrations = {
            "ALTER TABLE IF EXISTS medecins ADD COLUMN IF NOT EXISTS type_medecin   VARCHAR(20)",
            "ALTER TABLE IF EXISTS medecins ADD COLUMN IF NOT EXISTS type_formation  VARCHAR(100)",
            "ALTER TABLE IF EXISTS medecins ADD COLUMN IF NOT EXISTS nom_specialite  VARCHAR(100)",
            "ALTER TABLE IF EXISTS medecins ADD COLUMN IF NOT EXISTS actif           BOOLEAN DEFAULT TRUE",
            "ALTER TABLE IF EXISTS assures  ADD COLUMN IF NOT EXISTS actif           BOOLEAN DEFAULT TRUE",
            // Colonnes Module 3 — feuilles_maladie
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS version               INT DEFAULT 1",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS temperature            NUMERIC(4,1)",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS tension_arterielle     VARCHAR(20)",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS poids                  NUMERIC(5,2)",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS taille                 NUMERIC(5,2)",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS frequence_cardiaque    INT",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS frequence_respiratoire INT",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS saturation_oxygene     NUMERIC(4,1)",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS antecedents            TEXT",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS symptomes              TEXT",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS diagnostic             TEXT",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS traitement_prescrit    TEXT",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS observations           TEXT",
            "ALTER TABLE IF EXISTS feuilles_maladie ADD COLUMN IF NOT EXISTS num_medecin            INT REFERENCES medecins(num_medecin)",
            // Prescriptions — champ dosage
            "ALTER TABLE IF EXISTS prescriptions ADD COLUMN IF NOT EXISTS dosage VARCHAR(100)",
            // Remboursements — colonnes Module 4
            "ALTER TABLE IF EXISTS remboursements ADD COLUMN IF NOT EXISTS date_remboursement TIMESTAMP DEFAULT NOW()",
            "ALTER TABLE IF EXISTS remboursements ADD COLUMN IF NOT EXISTS agent_login        VARCHAR(100)",
            // Table versions feuilles — Module 3
            """
            CREATE TABLE IF NOT EXISTS feuilles_maladie_versions (
                id          SERIAL PRIMARY KEY,
                num_feuille INT NOT NULL REFERENCES feuilles_maladie(num_feuille),
                version     INT NOT NULL,
                snapshot    JSONB NOT NULL,
                modifie_par INT REFERENCES medecins(num_medecin),
                modifie_le  TIMESTAMP DEFAULT NOW()
            )
            """
        };

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            for (String sql : ddl) {
                st.execute(sql);
            }
            for (String sql : migrations) {
                st.execute(sql);
            }
            System.out.println("[DB] Schéma initialisé avec succès");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du schéma", e);
        }
    }
}
