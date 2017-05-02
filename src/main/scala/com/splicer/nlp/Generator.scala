package com.splicer.nlp

import java.util.Random

import org.nd4j.linalg.api.ndarray.INDArray

import com.splicer.lstm.{CharPredictor, Params}
import com.splicer.twitter.Aggregator

class Generator(agg: Aggregator, params: Params, lstm: CharPredictor) {
  /* Get the words which start before the LSTM input size of a random tweet */
  def getRandomStart: String = {
    val rand = new Random()
    val validTweets = agg.filter(_.length > params.getExampleSize)
    //val tweet = validTweets(rand.nextInt(validTweets.length))
    val tweet = validTweets(0)
    tweet.substring(0, params.getExampleSize) +
    tweet.substring(params.getExampleSize, tweet.length).split(" ")(0) + " "
  }

  val start = getRandomStart
  var output = start

  def next: Array[Double] = {
    val tail = output.substring(output.length - params.getExampleSize, output.length)
    val inputs = Encoder.encode(tail, params.getCharOrder)
    val res = lstm.run(inputs).data.asDouble
    output += Encoder.decode(res, params.getCharOrder)
    res
  }

  def getStart: String = start
}
