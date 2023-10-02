
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

    create table opt_out_settings (
        id bigint not null auto_increment,
        subscription_id bigint,
        category varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table opt_out_settings_key_values (
        opt_out_settings_id bigint not null,
        opt_outs varchar(255),
        opt_outs_key varchar(255) not null,
        primary key (opt_out_settings_id, opt_outs_key)
    ) engine=InnoDB;

    create table subscription (
        created datetime(6),
        id bigint not null auto_increment,
        updated datetime(6),
        party_id varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create index party_id_index 
       on affected (party_id);

    create index disturbance_id_index 
       on disturbance (disturbance_id);

    create index category_index 
       on disturbance (category);

    create index party_id_index 
       on subscription (party_id);

    alter table if exists affected 
       add constraint fk_affected_parent_id_disturbance_id 
       foreign key (parent_id) 
       references disturbance (id);

    alter table if exists opt_out_settings 
       add constraint fk_opt_out_settings_subscription_id 
       foreign key (subscription_id) 
       references subscription (id);

    alter table if exists opt_out_settings_key_values 
       add constraint fk_opt_out_settings_key_values_opt_out_settings_id 
       foreign key (opt_out_settings_id) 
       references opt_out_settings (id);
