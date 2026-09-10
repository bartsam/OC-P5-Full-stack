# MDD - Monde de Dev

Application MVP permettant aux utilisateurs de créer un compte, de gérer leur profil et de s’abonner à des thèmes liés au monde du développement afin de consulter, commenter ou publier des articles sur ces sujets.

Le projet est compose de deux applications :

- `front/` : application cliente Angular.
- `back/` : API REST Spring Boot securisée par JWT.

## Technologies

### Frontend

- Angular 22
- Angular Material
- TypeScript et SCSS
- Vitest

### Backend

- Java 21
- Spring Boot 4
- Spring Security et JWT
- Spring Data JPA
- MySQL 8.4
- Maven
- Testcontainers

## Prerequis

- Node.js dans une version compatible avec Angular 22 et npm
- JDK 21
- Docker Desktop, lance pour la base MySQL et les tests d'integration

## Demarrer le projet en developpement

Les deux applications doivent etre lancees dans deux terminaux distincts.

### 1. Demarrer le backend

Depuis le dossier `back`, creer le fichier d'environnement local :

```bash
cd back
cp .env.example .env
```

Renseigner les valeurs de `back/.env` :

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=mdd
DB_USER=mdd_user
DB_PASSWORD=change_me
DB_ROOT_PASSWORD=change_me_root
JWT_SECRET=change_this_to_a_long_random_secret
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Puis lancer l'API :

```bash
./mvnw spring-boot:run
```

Spring Boot demarre automatiquement le conteneur MySQL defini dans `back/compose.yaml` lorsque Docker Desktop est disponible. L'API est accessible sur `http://localhost:8080`.

La documentation OpenAPI est disponible sur `http://localhost:8080/swagger-ui/index.html`.
La collection Postman est disponible dans [docs/MDD.postman_collection.json](docs/MDD.postman_collection.json). Elle utilise les variables `baseUrl` et `token` pour tester les endpoints.

### 2. Demarrer le frontend

Depuis le dossier `front` :

```bash
cd front
npm install
npm start
```

L'application est accessible sur `http://localhost:4200`.

En developpement, le proxy Angular redirige les requetes vers `http://localhost:8080/api`.

## Fonctionnalites actuelles

- Inscription d'un utilisateur
- Connexion par email ou nom d'utilisateur
- Authentification par jeton JWT
- Consultation et modification du profil utilisateur
- Protection des routes necessitant une authentification

## Tests et qualite

### Frontend

```bash
cd front
npm test
npm run lint
```

### Backend

```bash
cd back
./mvnw test
```

Les tests d'integration backend utilisent Testcontainers et necessitent Docker Desktop. Le rapport de couverture JaCoCo est genere dans `back/target/site/jacoco/`.

### Tests E2E avec Cypress

Docker Desktop doit être démarré. Chaque commande utilise une base MySQL temporaire `mdd_e2e`, un back Spring en profil
`e2e` sur le port `8081` et un front sur le port `4201`. La base de développement n’est jamais utilisée et les services
sont arrêtés et supprimés à la fin de l'exécution ou en cas d'échec.

```bash
npm run e2e:coverage   # execution dans Chrome avec rapport de couverture
npm run e2e:open       # interface Cypress interactive dans Chrome
```

`npm run e2e:coverage` construit un front instrumenté, puis génère le rapport HTML dans
`front/coverage/cypress/index.html`.

`npm run e2e:open` lance l'environnement E2E et l'interface Cypress sur le même port
`4201`, sans collecte de couverture. Fermer Cypress arrête ensuite le back et supprime
la base temporaire.

## Structure du projet

```text
.
|- front/                 # Application Angular
|  |- src/app/core/       # Authentification, gardes et interceptors
|  |- src/app/features/   # Fonctionnalites metier
|  |- src/app/shared/     # Composants et services partages
|
|- back/                  # API Spring Boot
   |- src/main/java/      # Controllers, services, DTO, securite et persistance
   |- src/test/java/      # Tests unitaires et d'integration
   |- compose.yaml        # Service MySQL pour le developpement
```

## Variables d'environnement backend

Le fichier `back/.env` est local et ne doit pas etre versionne. Le modele `back/.env.example` liste les variables attendues :

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` : connexion MySQL
- `DB_ROOT_PASSWORD` : mot de passe administrateur MySQL pour Docker Compose
- `JWT_SECRET` : cle secrete de signature des jetons JWT
- `CORS_ALLOWED_ORIGINS` : origine autorisee pour le frontend, par exemple `http://localhost:4200`
- `E2E_DB_NAME`, `E2E_DB_USER`, `E2E_DB_PASSWORD`, `E2E_DB_ROOT_PASSWORD` : base MySQL temporaire E2E
- `E2E_JWT_SECRET` : cle JWT exclusivement utilisee par le profil E2E
- `E2E_CORS_ALLOWED_ORIGINS` : origine du frontend E2E, `http://localhost:4201`
