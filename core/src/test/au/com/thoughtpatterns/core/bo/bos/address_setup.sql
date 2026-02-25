create table test_address (
    id bigint primary key,
    person_id bigint,
    street_number varchar(20),
    street_name varchar(20),
    country varchar(20),
    audit_id bigint
);

insert into test_address ( id, person_id, street_number, street_name, country, audit_id ) values ( 1, 1, '13', 'Jorge St', 'Australia', 1 );
insert into test_address ( id, person_id, street_number, street_name, country, audit_id ) values ( 2, 1, '13a', 'Bendy St', 'New Zealand', 1 );
