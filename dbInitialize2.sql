
-- user = root
-- password = 1234
use COMP4111;

DROP TABLE IF EXISTS bookdb;
CREATE TABLE bookdb (
                       id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                       title VARCHAR(255),
                       author VARCHAR(255),
                       publisher VARCHAR(255),
                       year INT,
                       available BOOLEAN NOT NULL DEFAULT 1,
                       PRIMARY KEY (id),
                       UNIQUE (title)
);

DROP TABLE IF EXISTS userdb;
create table userdb
(
	id int unsigned auto_increment
		primary key,
	username varchar(255) not null,
	password varchar(255) not null,
	token varchar(255) null,
	constraint userdb_token_uindex
		unique (token)
)
charset=utf8;

drop procedure if exists test;
create procedure test()
begin
    DECLARE i INT DEFAULT 1;
    WHILE i <= 10000 DO
            INSERT into userdb (username, password) VALUES (CONCAT('user', LPAD(i, 5, '0')), CONCAT('pass', LPAD(i, 5, '0')));
            SET i = i + 1;
        END WHILE;
end;

call test;
drop procedure if exists test;

drop table if exists transactiondb;
create table transactiondb
(
	id int auto_increment
		primary key,
	transaction_id int not null,
	body varchar(255) default '' not null,
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