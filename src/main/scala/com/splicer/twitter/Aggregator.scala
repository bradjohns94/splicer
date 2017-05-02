package com.splicer.twitter

import collection.mutable.ArrayBuffer

class Aggregator(api: API, users: List[String], tweetsPerUser: Int = 20)
  extends ArrayBuffer[String] {
  /* Populate with tweets from user list */
  users.foreach{user => this ++= api.getTweets(user, tweetsPerUser)}

  /* Get some tweet metadata */
  val counts = this.flatten.groupBy(_.toChar).map{c => (c._1, c._2.length)}
  val avgLength = this.flatten.length.toDouble / this.length.toDouble
  val avgWords = this.map{str => str.split(" ").length}.sum.toDouble / this.length.toDouble

  /* Functions to access metadata */
  def getCounts: Map[Char, Int] = counts
  def getAvgTweetLength: Double = avgLength
  def getAvgTweetWords: Double = avgWords
  def getAvgWordLength: Double = avgLength / avgWords
}
