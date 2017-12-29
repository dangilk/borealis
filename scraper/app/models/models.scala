package models

case class Game(id: String, name: String, yearPublished: Int,
imageUrl: String, subType: String, minPlayers: Int, maxPlayers: Int,
minPlaytime: Int, maxPlaytime: Int, playingTime: Int, numOwned: Int,
ratingCount: Int, averageRating: Double, bayesAverageRating: Double,
stdDevRating: Double, medianRating: Double)

case class User(id: String, name: String)

case class UserCollection(id: Int, userId: String, gameId: String,
numPlays: Int, own: Boolean, prevOwned: Boolean, forTrade: Boolean,
want: Boolean, wantToPlay: Boolean, wantToBuy: Boolean,
wishList: Boolean, wishListPriority: Int, preOrdered: Boolean,
lastModified: String, userRating: Double)

case class GlobalSetting(key: String, value: String)
