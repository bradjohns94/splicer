package com.splicer.twitter

import collection.mutable.ArrayBuffer

class Aggregator(api: API, users: List[String], tweetsPerUser: Int = 20)
  extends ArrayBuffer[String] {
  /* Populate with tweets from user list */
  users.foreach{user => this ++= api.getTweets(user, tweetsPerUser)}

  /* Define our metadata variables */
  var counts: Map[Char, Int] = _
  var avgLength: Double = 0.0
  var avgWords: Double = 0.0

  def calculateMetadata: Unit = {
    counts = this.flatten.groupBy(_.toChar).map{c => (c._1, c._2.length)}
    avgLength = this.flatten.length.toDouble / this.length.toDouble
    avgWords = this.map{str => str.split(" ").length}.sum.toDouble / this.length.toDouble
  }

  calculateMetadata

  /* Remove all tweets which meet the specified condition */
  def removeWhere(fn: (String) => Boolean): Unit = this.find(fn) match {
    case Some(tweet) => {
      this.remove(this.indexOf(tweet))
      removeWhere(fn)
    }
    case None => calculateMetadata
  }

  /* Functions to access metadata */
  def getCounts: Map[Char, Int] = counts
  def getAvgTweetLength: Double = avgLength
  def getAvgTweetWords: Double = avgWords
  def getAvgWordLength: Double = avgLength / avgWords
}
