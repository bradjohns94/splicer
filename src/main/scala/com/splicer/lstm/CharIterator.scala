package com.splicer.lstm

import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

import com.splicer.nlp.Encoder
import com.splicer.twitter.Aggregator

class CharIterator(agg: Aggregator, params: Params)
  extends DataSetIterator {

  var tweetIndex = 0
  var aggIndex = 0

  /* Remove all tweets of length < batch size */
  agg.removeWhere( (tweet: String) =>
      tweet.length <= params.getBatchSize
  )

  def asyncSupported: Boolean = true

  def batch: Int = params.getBatchSize

  def hasNext: Boolean = aggIndex < agg.length && agg(aggIndex).length - tweetIndex > batch

  def next: DataSet = next(batch)

  def next(num: Int): DataSet = {
    def getNextChars(n: Int): String
      = (agg(aggIndex).length - tweetIndex) match {
        case diff if diff > n => {
          tweetIndex += n
          agg(aggIndex).slice(tweetIndex - n, tweetIndex)
        }
        case diff if diff <= n => {
          var str = agg(aggIndex).slice(tweetIndex, agg(aggIndex).length)
          aggIndex += 1
          tweetIndex = n - diff
          str + agg(aggIndex).slice(0, tweetIndex)
        }
    }

    val fullString = getNextChars(num + 1)
    val inputs = Encoder.encode(
      fullString.slice(0, fullString.length - 1),
      params.getCharOrder
    )
    val outputs = Encoder.encode(
      fullString.slice(1, fullString.length),
      params.getCharOrder
    )

    new DataSet(inputs, outputs)
  }

  def reset: Unit = {
    tweetIndex = 0
    aggIndex = 0
  }

  def resetSupported: Boolean = true

  def numExamples: Int = totalExamples

  def totalExamples: Int = agg.foldLeft(0) { (sum: Int, tweet: String) =>
    if (tweet.length > 1) /* Ignore 1 character tweets */
      sum + tweet.length - 1
    else
      sum
  }

  def totalOutcomes: Int = params.getOutputSize

  def inputColumns: Int = params.getInputSize

  /* XXX not implemented */
  def getLabels: java.util.List[String] = null
  def getPreProcessor: DataSetPreProcessor = null
  def setPreProcessor(preProcessor: DataSetPreProcessor): Unit = {}
  def cursor: Int = 0
}
