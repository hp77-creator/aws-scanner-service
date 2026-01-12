-- Rename detector column to detector_type
-- This fixes the schema mismatch between JPA entity (@Column(name = "detector_type")) and database (detector)

ALTER TABLE findings 
RENAME COLUMN detector TO detector_type;
