INSERT INTO disturbance(id, municipality_id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted, created, updated) VALUES
	(2, '2281', 'COMMUNICATION', 'Description', 'disturbance-2', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(3, '2281', 'ELECTRICITY', 'Description', 'disturbance-3', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(4, '2281', 'COMMUNICATION', 'Description', 'disturbance-4', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'CLOSED', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(5, '2281', 'ELECTRICITY', 'Description', 'disturbance-5', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(6, '2281', 'ELECTRICITY', 'Description', 'disturbance-6', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(7, '2281', 'ELECTRICITY', 'Description', 'disturbance-7', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(8, '2281', 'ELECTRICITY', 'Description', 'disturbance-8', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(9, '2281', 'ELECTRICITY', 'Description', 'disturbance-9', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(10, '2281', 'ELECTRICITY', 'Description', 'disturbance-10', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'CLOSED', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(11, '2281', 'COMMUNICATION', 'Description', 'disturbance-11', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(12, '2281', 'ELECTRICITY', 'Description', 'disturbance-12', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'PLANNED', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(13, '2281', 'ELECTRICITY', 'Description', 'disturbance-13', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	(14, '2281', 'WATER', 'Description', 'disturbance-14', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, DATE_ADD(NOW(), INTERVAL -25 MONTH), NOW()),
	(15, '2281', 'WATER', 'Description', 'disturbance-15', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'CLOSED', 'Title', 0, DATE_ADD(NOW(), INTERVAL -25 MONTH), NOW());

INSERT INTO affected (party_id, reference, facility_id, coordinates, parent_id) VALUES
	('0d64beb2-3aea-11ec-8d3d-0242ac130003', 'Streetname 11', 'facility-11', 'coordinate-11', 2),
	('0d64c132-3aea-11ec-8d3d-0242ac130003', 'Streetname 22', 'facility-22', 'coordinate-22', 2),
	('0d64c42a-3aea-11ec-8d3d-0242ac130003', 'Streetname 33', 'facility-33', 'coordinate-33', 2),
	('c76ae496-3aed-11ec-8d3d-0242ac130003', 'Streetname 11', 'facility-11', 'coordinate-11', 3),
	('c76ae496-3aed-11ec-8d3d-0242ac130003', 'Streetname 11', 'facility-11', 'coordinate-11', 4),
	('00000001-0000-1000-8000-00805f9b34fb', 'Streetname 11', 'facility-11', 'coordinate-11', 5),
	('00000002-0000-1000-8000-00805f9b34fb', 'Streetname 22', 'facility-22', 'coordinate-22', 5),
	('00000003-0000-1000-8000-00805f9b34fb', 'Streetname 33', 'facility-33', 'coordinate-33', 5),
	('00000001-0000-1000-8000-00805f9b34fb', 'Streetname 11', 'facility-11', 'coordinate-11', 6),
	('00000002-0000-1000-8000-00805f9b34fb', 'Streetname 22', 'facility-22', 'coordinate-22', 6),
	('00000003-0000-1000-8000-00805f9b34fb', 'Streetname 33', 'facility-33', 'coordinate-33', 6),
	('eeca0a46-3b1d-11ec-8d3d-0242ac130003', 'Streetname 11', 'facility-11', 'coordinate-11', 7),
	('eeca0c8a-3b1d-11ec-8d3d-0242ac130003', 'Streetname 22', 'facility-22', 'coordinate-22', 7),
	('eeca0d7a-3b1d-11ec-8d3d-0242ac130003', 'Streetname 33', 'facility-33', 'coordinate-33', 7),
	('00000001-0000-1000-8000-00805f9b34fb', 'Streetname 11', 'facility-11', 'coordinate-11', 8),
	('00000002-0000-1000-8000-00805f9b34fb', 'Streetname 22', 'facility-22', 'coordinate-22', 8),
	('00000003-0000-1000-8000-00805f9b34fb', 'Streetname 33', 'facility-33', 'coordinate-33', 8),
	('affected-1', 'Streetname 11', 'facility-11', 'coordinate-11', 9),
	('affected-2', 'Streetname 22', 'facility-22', 'coordinate-22', 9),
	('affected-3', 'Streetname 33', 'facility-33', 'coordinate-33', 9),
	('affected-1', 'Streetname 11', 'facility-11', 'coordinate-11', 9),
	('affected-2', 'Streetname 22', 'facility-22', 'coordinate-22', 9),
	('affected-3', 'Streetname 33', 'facility-33', 'coordinate-33', 9),
	('affected-1', 'Streetname 11', 'facility-11', 'coordinate-11', 11),
	('affected-2', 'Streetname 22', 'facility-22', 'coordinate-22', 11),
	('affected-3', 'Streetname 33', 'facility-33', 'coordinate-33', 11),
	('00000001-0000-1000-8000-00805f9b34fb', 'Streetname 11', 'facility-11', 'coordinate-11', 12),
	('00000002-0000-1000-8000-00805f9b34fb', 'Streetname 22', 'facility-22', 'coordinate-22', 12),
	('00000003-0000-1000-8000-00805f9b34fb', 'Streetname 33', 'facility-33', 'coordinate-33', 12),
	('b6d929c8-fac3-4ac6-8b15-d255bad864df', 'Streetname 11', 'facility-11', 'coordinate-11', 13),
	('25eaeed8-0c9c-49ed-bace-5d60c3d7380b', 'Streetname 11', 'facility-11', 'coordinate-11', 14),
	('15696203-01e6-4357-a850-bd61660cd737', 'Streetname 22', 'facility-22', 'coordinate-22', 14),
	('59ebfdb3-d4df-42ad-bc64-90d261360a48', 'Streetname 33', 'facility-33', 'coordinate-33', 15);
	
INSERT INTO subscription (id, municipality_id, party_id) VALUES
	(1, '2281', '0d64beb2-3aea-11ec-8d3d-0242ac130003');

INSERT INTO opt_out_settings (id, subscription_id, category) VALUES
	(1, 1, 'ELECTRICITY');
	
INSERT INTO opt_out_settings_key_values (opt_out_settings_id, opt_outs, opt_outs_key) VALUES
	(1, "facility-11", 'facilityId');

