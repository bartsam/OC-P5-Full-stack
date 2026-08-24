# P6-Full-Stack-reseau-dev

## Front

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 14.1.3.

Don't forget to install your node_modules before starting (`npm install`).

### Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

### Where to start

As you may have seen if you already started the app, a simple home page containing a logo, a title and a button is available. If you take a look at its code (in the `home.component.html`) you will see that an external UI library is already configured in the project.

This library is `@angular/material`, it's one of the most famous in the angular ecosystem. As you can see on their docs (https://material.angular.io/), it contains a lot of highly customizable components that will help you design your interfaces quickly.

Note: I recommend to use material however it's not mandatory, if you prefer you can get rid of it.

Good luck!

## Back

### Tech stack

- Java 21
- Spring Boot 4.1.1
- MySQL 8.4 (Docker Compose)
- Maven Wrapper (`./mvnw`)

### Prerequisites

- JDK 21 installed
- Docker Desktop running

### Backend quick start

1. Go to the backend folder:

```bash
cd back
```

2. Create your local environment file:

```bash
cp .env.example .env
```

3. Update `.env` values:

- `DB_HOST=localhost`
- `DB_PORT=3306`
- `DB_NAME=...`
- `DB_USER=...`
- `DB_PASSWORD=...`
- `DB_ROOT_PASSWORD=...`
- `JWT_SECRET=...`

4. Start the backend:

```bash
./mvnw clean spring-boot:run
```

### Run tests

```bash
./mvnw clean test
```

Tests use Testcontainers (`mysql:8.4`) and do not require a manually running local MySQL.
