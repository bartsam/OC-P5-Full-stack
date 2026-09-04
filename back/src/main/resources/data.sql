-- Initial topic creation for the MDD MVP

INSERT INTO topics (name, description) VALUES
('Java', 'Tout sur l''écosystème Java : Spring Boot, Jakarta EE, performances, bonnes pratiques et architecture.'),
('JavaScript', 'Actualités et tutoriels JS : frameworks front, Node.js, patterns asynchrones et outillage moderne.'),
('DevOps & Cloud', 'CI/CD, Docker, Kubernetes, AWS/GCP et bonnes pratiques pour déployer et scaler vos applications.'),
('Data & IA', 'Bases de données, data engineering, machine learning et intégration d''IA dans les projets logiciels.'),
('Mobile', 'Développement natif et cross-platform (iOS, Android, React Native, Flutter) et UX mobile.'),
('Cybersécurité', 'Sécurité des applications, authentification, OWASP, cryptographie et bonnes pratiques en prod.')
ON DUPLICATE KEY UPDATE name=name;