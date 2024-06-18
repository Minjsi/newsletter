CREATE USER `read`@`%` IDENTIFIED WITH mysql_native_password BY '1234';
GRANT Select, Show Databases, Show View ON *.* TO `read`@`%`;

CREATE USER `write`@`%` IDENTIFIED WITH mysql_native_password BY '1234';
GRANT Alter, Alter Routine, Create, Create Routine, Create Temporary Tables, Create User, Create View, Delete, Drop, Event, Execute, File, Grant Option, Index, Insert, Lock Tables, Process, References, Reload, Replication Client, Replication Slave, Select, Show Databases, Show View, Shutdown, Super, Trigger, Update ON *.* TO `write`@`%`;

CREATE DATABASE `example_db` CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_bin';

USE `example_db`;

DROP TABLE IF EXISTS `example`;

CREATE TABLE `example`
(
    `id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `keyword`  varchar(50) COLLATE utf8mb4_bin  DEFAULT NULL,
    `data`     varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
    `reg_date` timestamp(6)        NOT NULL     DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO example (keyword, data, reg_date)
VALUES ('example1', 'Hello', now(6));
INSERT INTO example (keyword, data, reg_date)
VALUES ('example2', 'World', now(6));
INSERT INTO example (keyword, data, reg_date)
VALUES ('example3', 'foo', now(6));
INSERT INTO example (keyword, data, reg_date)
VALUES ('example4', 'bar', now(6));
