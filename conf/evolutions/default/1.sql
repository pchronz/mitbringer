# --- !Ups
CREATE TABLE usar (
	username VARCHAR(32) NOT NULL PRIMARY KEY,
	password VARCHAR(64) NOT NULL,
	email VARCHAR(64) NOT NULL UNIQUE
);

CREATE SEQUENCE offer_id_seq;
CREATE TABLE offer (
    id INTEGER NOT NULL DEFAULT nextval('offer_id_seq') PRIMARY KEY,
	name VARCHAR(256) NOT NULL UNIQUE,
	kCal INTEGER NOT NULL,
	protein FLOAT NOT NULL,
	fat FLOAT NOT NULL,
	carbs FLOAT NOT NULL,
    username VARCHAR(32) REFERENCES usar(username)
);


# --- !Downs
DROP TABLE usar;
DROP TABLE offer;


