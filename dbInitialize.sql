
-- user = root
-- password = 1234
drop database COMP4111;
create database COMP4111;

use COMP4111;

create table bookdb
(
	id int auto_increment
		primary key,
	title varchar(100) not null,
	author varchar(100) not null,
	publisher varchar(100) not null,
	year int not null,
	available tinyint(1) default 1 not null,
	constraint bookdb_Title_uindex
		unique (title)
);

create table userdb
(
	id int unsigned auto_increment
		primary key,
	username varchar(100) not null,
	password varchar(100) not null,
	token varchar(100) null,
	constraint userdb_token_uindex
		unique (token)
)
charset=utf8;

create procedure test(a int)
begin
    declare i int default 1;
    while i <=a DO
        INSERT INTO userdb SET username = CONCAT('user',lpad(i,3,0)),password = CONCAT('pass',lpad(i,3,0));
        set i = i + 1;
    end while;
end;

call test(100);
drop procedure if exists test;

drop table if exists transactiondb;
create table transactiondb
(
	id int auto_increment
		primary key,
	transaction_id int not null,
	body varchar(100) default '' not null,
	available tinyint(1) default 1 not null,
	constraint transactiondb_transaction_id_uindex
		unique (transaction_id)
);

insert into transactiondb set transaction_id = 4234;
insert into transactiondb set transaction_id = 6234;
insert into transactiondb set transaction_id = 3421;
insert into transactiondb set transaction_id = 4432123;
insert into transactiondb set transaction_id = 51441;
insert into transactiondb set transaction_id = 34132;
insert into transactiondb set transaction_id = 87557;
insert into transactiondb set transaction_id = 904302;
insert into transactiondb set transaction_id = 32400;
insert into transactiondb set transaction_id = 109092;
insert into transactiondb set transaction_id = 88888;
insert into transactiondb set transaction_id = 6666;
