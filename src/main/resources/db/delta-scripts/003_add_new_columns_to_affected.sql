-- Add new columns
ALTER TABLE affected ADD COLUMN connection_point varchar(255);
ALTER TABLE affected ADD COLUMN coordinates varchar(255);

-- Necessary line in order to document the change. 
INSERT INTO schema_history (schema_version,comment,applied) VALUES ('003','Add columns connection_point and coordinates to affected-table', NOW());