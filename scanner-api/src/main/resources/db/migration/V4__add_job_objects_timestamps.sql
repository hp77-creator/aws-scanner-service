-- Add timestamp columns to job_objects table for better tracking
ALTER TABLE job_objects 
    ADD COLUMN queued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN started_at TIMESTAMP,
    ADD COLUMN completed_at TIMESTAMP;

-- Update existing records to set queued_at from updated_at
UPDATE job_objects SET queued_at = updated_at WHERE queued_at IS NULL;

-- Add index for queued_at for efficient queries
CREATE INDEX idx_job_objects_queued_at ON job_objects(queued_at DESC);
