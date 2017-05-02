package com.splicer.twitter

import collection.JavaConverters._
import collection.mutable.Buffer

import com.typesafe.config.ConfigFactory
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{Paging, Status, TwitterFactory}

class API(configFile: String, formatters: List[(String) => String]) {
  val config = ConfigFactory.load(configFile)
  val builder = new ConfigurationBuilder()
    .setDebugEnabled(true)
    .setOAuthConsumerKey(config.getString("twitter.consumer.key"))
    .setOAuthConsumerSecret(config.getString("twitter.consumer.secret"))
    .setOAuthAccessToken(config.getString("twitter.access.token"))
    .setOAuthAccessTokenSecret(config.getString("twitter.access.secret"))
  val apiRef = new TwitterFactory(builder.build()).getInstance()

  def getTweets(user: String, num: Int = 20): Buffer[String] = {
    def formatTweet(tweet: String): String = {
      var res = tweet
      formatters.foreach{fn => res = fn(res)}
      res
    }
    val paging = new Paging(1, num)
    apiRef.getUserTimeline(user, paging)
      .asInstanceOf[java.util.List[Status]]
      .asScala
      .map{tweet => formatTweet(tweet.getText)}
  }
}
