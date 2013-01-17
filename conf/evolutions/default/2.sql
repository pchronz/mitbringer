# --- !Ups
CREATE TABLE usar_unconfirmed (
	username VARCHAR(32) NOT NULL PRIMARY KEY,
	password VARCHAR(128) NOT NULL,
	email VARCHAR(64) NOT NULL UNIQUE,
    confirmationKey VARCHAR(32) NOT NULL
);

# --- !Downs
DROP TABLE usar_unconfirmed;


