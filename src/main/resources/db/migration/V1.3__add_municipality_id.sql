alter table if exists disturbance
   add column municipality_id varchar(255) AFTER id;
   
alter table if exists subscription
   add column municipality_id varchar(255) AFTER id;
   
create index municipality_id_index 
   on disturbance (municipality_id);
    
create index municipality_id_index 
   on subscription (municipality_id);
   