-- Create jobs table
CREATE TABLE jobs (
    job_id UUID PRIMARY KEY,
    bucket VARCHAR(255) NOT NULL,
    prefix VARCHAR(1024),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster lookups
CREATE INDEX idx_jobs_created_at ON jobs(created_at DESC);
CREATE INDEX idx_jobs_bucket ON jobs(bucket);
