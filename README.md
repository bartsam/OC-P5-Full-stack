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
