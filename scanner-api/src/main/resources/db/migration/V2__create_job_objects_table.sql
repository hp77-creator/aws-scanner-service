-- Create job_objects table
CREATE TABLE job_objects (
    job_id UUID NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    key VARCHAR(1024) NOT NULL,
    etag VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('QUEUED', 'PROCESSING', 'SUCCEEDED', 'FAILED')),
    last_error TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (job_id, bucket, key, etag),
    CONSTRAINT fk_job_objects_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE
);

-- Create indexes for efficient queries
CREATE INDEX idx_job_objects_job_id ON job_objects(job_id);
CREATE INDEX idx_job_objects_status ON job_objects(status);
CREATE INDEX idx_job_objects_job_status ON job_objects(job_id, status);
