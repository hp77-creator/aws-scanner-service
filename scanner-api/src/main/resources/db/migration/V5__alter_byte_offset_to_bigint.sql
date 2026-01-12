-- Alter byte_offset column type from INTEGER to BIGINT
-- This fixes the schema mismatch between JPA entity (Long) and database (INTEGER)
-- BIGINT is required to support large files (>2GB) where byte offsets can exceed INTEGER range

ALTER TABLE findings 
ALTER COLUMN byte_offset TYPE BIGINT;
