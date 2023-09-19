    create table affected (
        id bigint not null auto_increment,
        parent_id bigint not null,
        reference varchar(512),
        coordinates varchar(255),
        facility_id varchar(255),
        party_id varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table disturbance (
        deleted bit,
        created datetime(6),
        id bigint not null auto_increment,
        planned_start_date datetime(6),
        planned_stop_date datetime(6),
        updated datetime(6),
        description varchar(8192) not null,
        category varchar(255) not null,
        disturbance_id varchar(255) not null,
        status varchar(255) not null,
        title varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table disturbance_feedback (
        created datetime(6),
        id bigint not null auto_increment,
        category varchar(255) not null,
        disturbance_id varchar(255) not null,
        party_id varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table disturbance_feedback_history (
        created datetime(6),
        id bigint not null auto_increment,
        category varchar(255) not null,
        disturbance_id varchar(255) not null,
        party_id varchar(255) not null,
        status varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table feedback (
        created datetime(6),
        id bigint not null auto_increment,
        party_id varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table schema_history (
        applied datetime(6) not null,
        comment varchar(8192) not null,
        schema_version varchar(255) not null,
        primary key (schema_version)
    ) engine=InnoDB;

    create index party_id_index 
       on affected (party_id);

    create index disturbance_id_index 
       on disturbance (disturbance_id);

    create index category_index 
       on disturbance (category);

    create index party_id_index 
       on feedback (party_id);

    alter table if exists affected 
       add constraint fk_affected_parent_id_disturbance_id 
       foreign key (parent_id) 
       references disturbance (id);
