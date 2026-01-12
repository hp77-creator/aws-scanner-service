-- Create findings table
CREATE TABLE findings (
    id BIGSERIAL PRIMARY KEY,
    job_id UUID NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    key VARCHAR(1024) NOT NULL,
    detector VARCHAR(50) NOT NULL,
    masked_match VARCHAR(500),
    context TEXT,
    byte_offset INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_findings_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE
);

-- Create indexes for efficient queries
CREATE INDEX idx_findings_job_id ON findings(job_id);
CREATE INDEX idx_findings_bucket ON findings(bucket);
CREATE INDEX idx_findings_key ON findings(key);
CREATE INDEX idx_findings_detector ON findings(detector);
CREATE INDEX idx_findings_created_at ON findings(created_at DESC);

-- Create unique index for deduplication (idempotency)
CREATE UNIQUE INDEX idx_findings_dedupe ON findings(bucket, key, detector, byte_offset);
