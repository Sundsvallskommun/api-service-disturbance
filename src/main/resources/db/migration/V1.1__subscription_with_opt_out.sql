create table opt_out_settings
(
    id              bigint       not null auto_increment,
    subscription_id bigint,
    category        varchar(255) not null,
    primary key (id)
) engine = InnoDB;

create table opt_out_settings_key_values
(
    opt_out_settings_id bigint       not null,
    opt_outs            varchar(255),
    opt_outs_key        varchar(255) not null,
    primary key (opt_out_settings_id, opt_outs_key)
) engine = InnoDB;

create table subscription
(
    id       bigint       not null auto_increment,
    party_id varchar(255) not null,
    primary key (id)
) engine = InnoDB;

create index party_id_index
    on subscription (party_id);

alter table if exists opt_out_settings
    add constraint fk_opt_out_settings_subscription_id
        foreign key (subscription_id)
            references subscription (id);

alter table if exists opt_out_settings_key_values
    add constraint fk_opt_out_settings_opt_out_values
        foreign key (opt_out_settings_id)
            references opt_out_settings (id);