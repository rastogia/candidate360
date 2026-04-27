CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(200) NOT NULL, email VARCHAR(200) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL, role VARCHAR(30) NOT NULL DEFAULT 'RECRUITER',
  active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP DEFAULT NOW());
CREATE TABLE candidates (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(200) NOT NULL,
  email VARCHAR(200), phone VARCHAR(50), location VARCHAR(200),
  cv_file_path TEXT, cv_file_name VARCHAR(255), cv_parsed_json TEXT,
  created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());
CREATE TABLE candidate_handles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
  source VARCHAR(50) NOT NULL, handle VARCHAR(255) NOT NULL, profile_url TEXT,
  discovery_method VARCHAR(30) NOT NULL DEFAULT 'AUTO_DISCOVERED',
  confirmed BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMP DEFAULT NOW());
CREATE TABLE raw_source_data (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
  source VARCHAR(50) NOT NULL, json_payload TEXT,
  fetched_at TIMESTAMP DEFAULT NOW(), is_stale BOOLEAN DEFAULT FALSE, error_message TEXT);
CREATE TABLE job_roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(200) NOT NULL,
  description TEXT, created_by UUID REFERENCES users(id),
  active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP DEFAULT NOW());
CREATE TABLE role_weights (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  job_role_id UUID NOT NULL REFERENCES job_roles(id) ON DELETE CASCADE,
  dimension VARCHAR(50) NOT NULL, weight INTEGER NOT NULL CHECK (weight >= 0 AND weight <= 100));
CREATE TABLE profiles_360 (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  candidate_id UUID NOT NULL REFERENCES candidates(id),
  job_role_id UUID NOT NULL REFERENCES job_roles(id),
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING', enriched_json TEXT,
  refreshed_at TIMESTAMP, created_at TIMESTAMP DEFAULT NOW());
CREATE TABLE profile_scores (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  profile_id UUID NOT NULL REFERENCES profiles_360(id) ON DELETE CASCADE,
  dimension VARCHAR(50), score INTEGER, label VARCHAR(30), weight_used INTEGER,
  composite_score INTEGER, composite_label VARCHAR(30), data_coverage_pct INTEGER,
  sources_available TEXT, sources_missing TEXT, scored_at TIMESTAMP DEFAULT NOW());
CREATE TABLE red_flags (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  profile_id UUID REFERENCES profiles_360(id) ON DELETE CASCADE,
  flag_type VARCHAR(100) NOT NULL, description TEXT NOT NULL,
  severity VARCHAR(20) NOT NULL, source VARCHAR(50), created_at TIMESTAMP DEFAULT NOW());
CREATE TABLE recruiter_notes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  profile_id UUID NOT NULL REFERENCES profiles_360(id) ON DELETE CASCADE,
  recruiter_id UUID NOT NULL REFERENCES users(id), note_text TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW());
