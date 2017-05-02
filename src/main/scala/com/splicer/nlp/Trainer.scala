package com.splicer.nlp

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet

import com.splicer.lstm.{CharPredictor, Params}
import com.splicer.twitter.Aggregator

class Trainer(agg: Aggregator, params: Params, lstm: CharPredictor, numThreads: Int = 10) {
  /* Spawn n threads to handle a subset of the aggregator */
  val tweetsPerThread = agg.length / numThreads
  val threads = (0 until numThreads).map{t =>
    val tweets = agg.slice(t * tweetsPerThread,
      Math.min((t + 1) * tweetsPerThread, agg.length))
    new Thread(new Runnable{
      def run: Unit = {
        tweets.foreach{tweet =>
          if (tweet.length > params.getExampleSize) {
            val batch = fetchDataSets(tweet)
            lstm.synchronized{batch.foreach{ds => lstm.train(ds)}}
          }
        }
      }
    })
  }
  val before = lstm.getParams
  /* Dispatch the generated threads */
  threads.foreach{t => t.start}

  def join: Unit = {
    threads.foreach{t => t.join}
    val after = lstm.getParams
    println("Changed? " + after.zip(before).exists(a => a._1 != a._2))
    lstm.closeWriter
  }

  def fetchDataSets(tweet: String): List[DataSet] = {
    def encode(c: Char): INDArray
      = Encoder.encode(c, params.getCharOrder)

    /*
    def buildDataSet(substring: String): DataSet = {
      val inputs = encode(substring.substring(0, substring.length - 1))
      val outputs = encode(substring.substring(1, substring.length))
      new DataSet(inputs, outputs)
    }

    val substringSize = params.getExampleSize + 1
    (0 until tweet.length - substringSize).map{ i =>
      buildDataSet(tweet.substring(i, i + substringSize))
    }.toList
    */
    (0 until tweet.length - 1).map{ i =>
      new DataSet(encode(tweet(i)), encode(tweet(i+1)))
    }.toList
  }
}
