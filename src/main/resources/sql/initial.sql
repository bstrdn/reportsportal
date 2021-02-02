drop table gate_users;

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