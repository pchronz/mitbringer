# --- !Ups
CREATE TABLE usar (
	username VARCHAR(32) NOT NULL PRIMARY KEY,
	password VARCHAR(64) NOT NULL,
	email VARCHAR(64) NOT NULL UNIQUE
);

CREATE SEQUENCE offer_id_seq;
CREATE TABLE offer (
    id INTEGER NOT NULL DEFAULT nextval('offer_id_seq') PRIMARY KEY,
	origin VARCHAR(256) NOT NULL,
	destination VARCHAR(256) NOT NULL,
    date LONG NOT NULL,
    price FLOAT,
    isDriver VARCHAR(1) NOT NULL,
    username VARCHAR(32) REFERENCES usar(username)
);

CREATE SEQUENCE message_id_seq;
CREATE TABLE message (
  id INTEGER NOT NULL DEFAULT nextval('message_id_seq') PRIMARY KEY,
  originUser VARCHAR(32) NOT NULL REFERENCES usar(username),
  destinationUser VARCHAR(32) NOT NULL REFERENCES usar(username),
  date LONG NOT NULL,
  offer INTEGER NOT NULL REFERENCES offer(id),
  state VARCHAR(16) NOT NULL,
  content TEXT NOT NULL
);


# --- !Downs
DROP TABLE usar;
DROP TABLE offer;
DROP TABLE message;


