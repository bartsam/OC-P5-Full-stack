-- Compte réservé aux scénarios E2E qui vérifient une authentification réelle.
-- Mot de passe : Password123!
INSERT INTO topics (name, description) VALUES
('Java', 'Tout sur l''écosystème Java : Spring Boot, Jakarta EE, performances, bonnes pratiques et architecture.'),
('JavaScript', 'Actualités et tutoriels JS : frameworks front, Node.js, patterns asynchrones et outillage moderne.'),
('DevOps & Cloud', 'CI/CD, Docker, Kubernetes, AWS/GCP et bonnes pratiques pour déployer et scaler vos applications.'),
('Data & IA', 'Bases de données, data engineering, machine learning et intégration d''IA dans les projets logiciels.'),
('Mobile', 'Développement natif et cross-platform (iOS, Android, React Native, Flutter) et UX mobile.'),
('Cybersécurité', 'Sécurité des applications, authentification, OWASP, cryptographie et bonnes pratiques en prod.')
ON DUPLICATE KEY UPDATE name=name;
INSERT INTO users (username, email, password, created_at, updated_at)
VALUES (
  'user-test',
  'user@test.com',
  '$2y$10$iQfmdw5gcBwWWiaM/6FABuGZl5ccVFE71Y5vSmtMN4eOJNp1vZa5S',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
