# Gestion Sécurité Sociale

Application desktop JavaFX pour la gestion des prestations de sécurité sociale — **ENSPY Yaoundé**.

## Fonctionnalités

- **Assurés** — Gestion complète des bénéficiaires (immatriculation, modification, historique)
- **Médecins** — Gestion des praticiens généralistes et spécialistes
- **Consultations** — Prise en charge des visites médicales
- **Feuilles de maladie** — Certificats médicaux avec paramètres cliniques et versioning
- **Prescriptions** — Prescriptions médicamenteuses et orientations spécialisées
- **Remboursements** — Gestion des remboursements avec workflow de validation
- **Journal d'audit** — Traçabilité intégrale de toutes les actions utilisateurs
- **Tableau de bord** — Indicateurs clés adaptés à chaque rôle

## Rôles

| Rôle | Accès |
|---|---|
| **Agent SS** (Admin) | Gère assurés, médecins, remboursements ; consulte les logs |
| **Médecin** | Gère ses patients, consultations, prescriptions et feuilles de maladie |

## Stack technique

| Technologie | Version |
|---|---|
| Java | 21 LTS |
| JavaFX | 21 |
| PostgreSQL | (driver 42.7.3) |
| HikariCP | 5.1.0 (pool de connexions) |
| BCrypt | 0.10.2 (hachage mots de passe) |
| Maven | (build, packaging fat JAR) |

## Prérequis

- **Java 21+**
- **Maven 3.8+**
- **PostgreSQL** accessible sur `localhost:5432`

## Installation

```bash
# 1. Créer la base de données
psql -U postgres -c "CREATE DATABASE securite_sociale;"

# 2. Configurer la connexion (fichier application.properties, valeurs par défaut)
#    db.url=jdbc:postgresql://localhost:5432/securite_sociale
#    db.username=postgres
#    db.password=postgres

# 3. Lancer l'application
mvn clean javafx:run
```

Les tables sont créées automatiquement au premier démarrage.

## Compte administrateur par défaut

| Login | Mot de passe |
|---|---|
| `admin` | `Admin@2026` |

## Packaging

```bash
mvn clean package
java -jar target/gestion-ss-1.0.0.jar
```

## Architecture

```
App.java (point d'entrée)
  └─ Contrôleurs (couche présentation, FXML)
       └─ Services (logique métier + audit)
            └─ DAOs (accès aux données, JDBC)
                 └─ DatabaseManager (HikariCP → PostgreSQL)
```
