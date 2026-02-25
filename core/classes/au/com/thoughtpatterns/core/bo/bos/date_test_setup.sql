create table test_date (
    id bigint primary key,
	start_date date,
	end_date date,
	payment_cents int,
	audit_id bigint
);

insert into test_date ( id, start_date, end_date, payment_cents ) values ( 1, '2000-01-02', '2007-08-09', 99 );
