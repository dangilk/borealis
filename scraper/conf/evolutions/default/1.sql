# sql schema

# --- !Ups
CREATE TABLE IF NOT EXISTS USER(
  id VARCHAR NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS GAME(
		id VARCHAR NOT NULL PRIMARY KEY,
		name VARCHAR NOT NULL,
		yearPublished INT,
		image VARCHAR NOT NULL,
		subType VARCHAR,
	  minPlayers INT,
		maxPlayers INT,
		minPlaytime INT,
		maxPlaytime INT,
		playingTime INT,
		numOwned INT,
		ratingCount INT,
		averageRating DOUBLE,
		bayesAverageRating DOUBLE,
		stdDevRating DOUBLE,
		medianRating DOUBLE);
    
CREATE TABLE IF NOT EXISTS USER_COLLECTION(
		id INT NOT NULL PRIMARY KEY,
		userId VARCHAR NOT NULL,
    gameId VARCHAR NOT NULL,
		numPlays INT,
		own BOOL,
		prevOwned BOOL,
		forTrade BOOL,
		want BOOL,
	  wantToPlay BOOL,
		wantToBuy BOOL,
		wishList BOOL,
		wishListPriority INT,
		preOrdered BOOL,
		lastModified VARCHAR,
		userRating DOUBLE,
    FOREIGN KEY (userId) REFERENCES User(id),
    FOREIGN KEY (gameId) REFERENCES Game(id));

CREATE TABLE IF NOT EXISTS GLOBAL_SETTING(key VARCHAR NOT NULL PRIMARY KEY, val VARCHAR NOT NULL);

# --- !Downs

DROP TABLE USER_COLLECTION;
DROP TABLE GLOBAL_SETTING;
DROP TABLE GAME;
DROP TABLE USER;
