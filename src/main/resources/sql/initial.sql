drop table gate_users;
drop table gate_events;
CREATE SEQUENCE global_seq START WITH 100000;


create table gate_users
(
    "userptr" int not null,
    "number" varchar,
    "lastname" varchar,
    "firstname" varchar,
    "fathername" varchar,
    "groupptr" varchar,
    "lastused" varchar,
    "lastusedrdrname" varchar,
    "lastusedrdrptr" varchar,
    "lastusedevent" varchar,
    "details1" varchar,
    "details2" varchar,
    "details3" varchar,
    "details4" varchar,
    "details5" varchar
);

create unique index gate_users_userptr_uindex
    on gate_users ("userptr");

alter table gate_users
    add constraint gate_users_pk
        primary key ("userptr");






create table logs
(
    id     serial not null
        constraint logs_pk
            primary key,
    fio    varchar,
    action varchar,
    date   timestamp default now()
);

alter table logs
    owner to "user";

create unique index logs_id_uindex
    on logs (id);







create table gate_events
(
    id INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
    datetime TIMESTAMP,
    eventtype INTEGER,
    eventcode INTEGER,
    devptr INTEGER,
    rdrptr INTEGER,
    userptr INTEGER,
    operatorid INTEGER,
    alarmstatus INTEGER,
    unit VARCHAR,
    message VARCHAR,
    name VARCHAR
);

create unique index gate_events_id_uindex
    on gate_events ("id");

alter table gate_events
    add constraint gate_events_pk
        primary key ("id");