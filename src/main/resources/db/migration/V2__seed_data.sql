-- password = Admin@123 (bcrypt)
INSERT INTO users (name,email,password,role) VALUES
  ('Admin User','admin@company.com','$2a$12$w9FVb.8r6QEjGFezJ2VoLOVT5Y5zYqGm3v8Qz9r1fBnHxQkPgJHLG','ADMIN'),
  ('Recruiter One','recruiter@company.com','$2a$12$w9FVb.8r6QEjGFezJ2VoLOVT5Y5zYqGm3v8Qz9r1fBnHxQkPgJHLG','RECRUITER');
INSERT INTO job_roles (name,description) VALUES
  ('Senior Java Developer','Backend Java/Spring expert'),
  ('Full Stack Engineer','Java backend + React/Angular frontend'),
  ('DevOps Engineer','CI/CD, Docker, K8s, cloud infrastructure');
INSERT INTO role_weights (job_role_id,dimension,weight)
  SELECT id,'TECHNICAL',60 FROM job_roles WHERE name='Senior Java Developer' UNION ALL
  SELECT id,'SOCIAL',20 FROM job_roles WHERE name='Senior Java Developer' UNION ALL
  SELECT id,'BEHAVIOURAL',20 FROM job_roles WHERE name='Senior Java Developer';
