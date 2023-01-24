-- Rename column
ALTER TABLE affected CHANGE COLUMN connection_point facility_id varchar(255);

-- Necessary line in order to document the change. 
INSERT INTO schema_history (schema_version,comment,applied) VALUES ('004','Renamed column connection_point to facility_id', NOW());