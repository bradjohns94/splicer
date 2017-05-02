package com.splicer.twitter

import twitter4j.Status

object Processor {
  val mentionRegex = """@[^\s]+""".r
  val specialCharacterRegex = """[^\s@\w\d]+""".r
  val urlRegex = """(?:https?:\/\/)?(?:[\.\/\d\w])+\.(?:[\.\/\d\w])+""".r
  val whitespaceRegex = """(?:\s\s+|^\s+|\s+$)""".r

  def removeMentions(tweet: String): String
    = mentionRegex.replaceAllIn(tweet, "")

  def removeSpecialCharacters(tweet: String): String
    = specialCharacterRegex.replaceAllIn(tweet, "")

  def removeUrls(tweet: String): String
    = urlRegex.replaceAllIn(tweet, "")

  def removeExcessWhitespace(tweet: String): String
    = whitespaceRegex.replaceAllIn(tweet, "")

  def makeLowerCase(tweet: String): String
    = tweet.toLowerCase
}
