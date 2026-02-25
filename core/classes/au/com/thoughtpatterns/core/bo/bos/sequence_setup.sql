create sequence main_sequence as bigint start with 1000;
create table dual_main_sequence ( fake bigint );
insert into dual_main_sequence values ( 1 );
