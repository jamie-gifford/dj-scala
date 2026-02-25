create table test_audit (
    id bigint primary key,
    txn_user varchar(20),
    txn_timestamp timestamp,
    txn_name varchar(20)
);

insert into test_audit ( id, txn_name ) values ( 1, 'setup' );
