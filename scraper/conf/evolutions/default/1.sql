# sql schema

# --- !Ups

CREATE TABLE IF NOT EXISTS USER_COLLECTIONS(
		id INT NOT NULL PRIMARY KEY,
		userId INT NOT NULL,
		userName VARCHAR(200) NOT NULL,
		gameName VARCHAR(1000) NOT NULL,
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
		lastModified VARCHAR(100),
		userRating DOUBLE);

CREATE TABLE IF NOT EXISTS CURRENT_FORUMLISTID(id INT NOT NULL PRIMARY KEY, forumId INT NOT NULL);

CREATE TABLE IF NOT EXISTS USER_RATINGS(userName VARCHAR(200) NOT NULL PRIMARY KEY, userId INT NOT NULL, ratingsJson LONGTEXT);

CREATE TABLE IF NOT EXISTS GAME_METADATA(
		id INT NOT NULL PRIMARY KEY,
		gameName VARCHAR(1000) NOT NULL,
		yearPublished INT,
		image VARCHAR(1000) NOT NULL,
		subType VARCHAR(100),
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
    
# --- !Downs

DROP TABLE USER_COLLECTIONS;
DROP TABLE CURRENT_FORUMLISTID;
DROP TABLE USER_RATINGS;
DROP TABLE GAME_METADATA;
