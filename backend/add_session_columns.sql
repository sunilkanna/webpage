-- Add session tracking columns to appointments table
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS session_start_time TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS session_end_time TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS session_duration_minutes INT DEFAULT 0;
