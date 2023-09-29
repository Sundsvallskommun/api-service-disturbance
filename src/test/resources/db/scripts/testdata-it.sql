INSERT INTO disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted, created, updated) VALUES

	-- ReadDisturbanceTest.test1
	-- UpdateDisturbanceTest.test7
	(2, 'COMMUNICATION', 'Description', 'disturbance-2', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198', '2021-09-24 09:05:48.298'),
	
	-- ReadDisturbanceTest.test2
	-- ReadDisturbanceTest.test3
	-- ReadDisturbanceTest.test4
	(3, 'ELECTRICITY', 'Description', 'disturbance-3', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, null, null),
	(4, 'COMMUNICATION', 'Description', 'disturbance-4', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'CLOSED', 'Title', 0, null, null),
	
	-- UpdateDisturbanceTest.test1
	(5, 'ELECTRICITY', 'Description', 'disturbance-5', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, null, null),

	-- UpdateDisturbanceTest.test2
	(6, 'ELECTRICITY', 'Description', 'disturbance-6', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, null, null),
	
	-- UpdateDisturbanceTest.test3
	(7, 'ELECTRICITY', 'Description', 'disturbance-7', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, null, null),
	
	-- UpdateDisturbanceTest.test4
	(8, 'ELECTRICITY', 'Description', 'disturbance-8', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, null, null),
	
	-- DeleteDisturbanceTest.test1
	(9, 'ELECTRICITY', 'Description', 'disturbance-9', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, null, null),
	
	-- DeleteDisturbanceTest.test2
	(10, 'ELECTRICITY', 'Description', 'disturbance-10', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'CLOSED', 'Title', 0, null, null),

	(11, 'COMMUNICATION', 'Description', 'disturbance-11', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, null, null),

	-- UpdateDisturbanceTest.test5
	(12, 'ELECTRICITY', 'Description', 'disturbance-12', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'PLANNED', 'Title', 0, null, null),
	
	-- UpdateDisturbanceTest.test6
	(13, 'ELECTRICITY', 'Description', 'disturbance-13', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, null, null);

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
	('00000001-0000-1000-8000-00805f9b34fb', 'Streetname 11', 'facility-11', 'coordinate-11', 12),
	('00000002-0000-1000-8000-00805f9b34fb', 'Streetname 22', 'facility-22', 'coordinate-22', 12),
	('00000003-0000-1000-8000-00805f9b34fb', 'Streetname 33', 'facility-33', 'coordinate-33', 12),
	('b6d929c8-fac3-4ac6-8b15-d255bad864df', 'Streetname 11', 'facility-11', 'coordinate-11', 13),
	('affected-1', 'Streetname 11', 'facility-11', 'coordinate-11', 9),
	('affected-2', 'Streetname 22', 'facility-22', 'coordinate-22', 9),
	('affected-3', 'Streetname 33', 'facility-33', 'coordinate-33', 9),
	('affected-1', 'Streetname 11', 'facility-11', 'coordinate-11', 9),
	('affected-2', 'Streetname 22', 'facility-22', 'coordinate-22', 9),
	('affected-3', 'Streetname 33', 'facility-33', 'coordinate-33', 9),
	('affected-1', 'Streetname 11', 'facility-11', 'coordinate-11', 11),
	('affected-2', 'Streetname 22', 'facility-22', 'coordinate-22', 11),
	('affected-3', 'Streetname 33', 'facility-33', 'coordinate-33', 11);

INSERT INTO subscription (id, party_id, created, updated) VALUES
	(1, '44f40c52-f550-4fee-860d-eda9c591d6a3', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(2, 'c1236ca-4c44-11ec-81d3-0242ac130003', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(3, '49a974ea-9137-419b-bcb9-ad74c81a1d7f', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(4, 'fbfbd90c-4c47-11ec-81d3-0242ac130003', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(5, '257f6aa0-4c48-11ec-81d3-0242ac130003', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(6, '00000001-0000-1000-8000-00805f9b34fb', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(7, '00000003-0000-1000-8000-00805f9b34fb', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(8, 'b6d929c8-fac3-4ac6-8b15-d255bad864df', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(9, 'eeca0a46-3b1d-11ec-8d3d-0242ac130003', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(10, 'eeca0c8a-3b1d-11ec-8d3d-0242ac130003', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824'),
	(11, 'eeca0d7a-3b1d-11ec-8d3d-0242ac130003', '2023-09-26 16:06:33.220', '2023-09-27 11:21:24.824');
	