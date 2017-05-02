package com.splicer

import java.io.{File, PrintWriter}

import scopt._

import com.splicer.lstm._
import com.splicer.nlp.{Generator, Trainer}
import com.splicer.twitter._

object Main {

  case class Config(configFile: String = "api.conf",
                    debug: Boolean = false,
                    users: List[String] = List())

  def main(args: Array[String]): Unit = {
    val parser = new OptionParser[Config]("splicer") {
      head("Tweet Splicer", "1.0")
      opt[String]('c', "config-file")
        .action( (x, c) => c.copy(configFile = x))
        .text("Configuration file. (Defaults to api.conf)")
      opt[Unit]('d', "debug")
        .action( (x, c) => c.copy(debug = true))
        .text("Run debug mode. Helps tune hyperparameters")
      arg[String]("<username>...")
        .unbounded()
        .action( (x, c) => c.copy(users = c.users :+ x))
        .text("List of users who's tweets to splice together")
    }
    parser.parse(args, Config()) match {
      case Some(config) => {
        val formatters: List[String => String] = List(Processor.removeUrls,
          Processor.removeSpecialCharacters,
          Processor.removeExcessWhitespace,
          Processor.makeLowerCase)
        val api = new API(config.configFile, formatters)
        val agg = new Aggregator(api, config.users, 1000)
        if (config.debug) run_debug(agg) else run(agg)
      }
      case None => {}
    }
  }


  /* Train a network over the given resources and generate an output string */
  def run(agg: Aggregator): Unit = {
    val params = new Params(agg)
    var lstm = new CharPredictor(params)
    train(agg, params, lstm)
    val gen = new Generator(agg, params, lstm)
    (gen.getStart.length to 140).foreach{ i => gen.next }
  }

  /* Get a feel for what direction to tune hyperparameters
   * (WARNING: Takes ~ 3 hours)
   */
  def run_debug(agg: Aggregator): Unit = {
    var params = new Params(agg)
    val writer = new PrintWriter(new File("debug.out"))

    /* Get average value that next character is below avg */
    def trainAndGet(iterations: Int): Double = {
      val testTweet = agg.filter(_.length > params.getExampleSize)(0)
      val nextChar = params.getCharOrder.indexOf(testTweet(params.getExampleSize + 1))

      /* Get the difference of the expected character from the max in
       * one training session */
      def getNext: Double = {
        val lstm = new CharPredictor(params)
        writer.write("Before: " + lstm.getParams + "\n")
        train(agg, params, lstm)
        writer.write("After: " + lstm.getParams + "\n")
        val gen = new Generator(agg, params, lstm)
        val res = gen.next
        res.max - res(nextChar)
      }

      val diffs = (0 until iterations).map{_ => getNext}
      diffs.sum / diffs.length
    }

    writer.write("\n\n")
    writer.write("Default: " + trainAndGet(5) + "\n")
    /* XXX Debug Layer Size Later
    params = new Params(agg)
    params.setLayerSize(100 * params.getLayerSize)
    writer.write("Layer Size Up: " + trainAndGet(5) + "\n")
    params = new Params(agg)
    params.setLayerSize(params.getLayerSize / 100)
    if (params.getLayerSize == 0) params.setLayerSize(1)
    writer.write("Layer Size Down: " + trainAndGet(5) + "\n")
    params = new Params(agg)
    */
    /* Figure out which direction to move Learning Rate */
    params.setLearningRate(params.getLearningRate * 100)
    writer.write("Learning Rate Up: " + trainAndGet(5) + "\n")
    params = new Params(agg)
    writer.write("Learning Rate Down: " + trainAndGet(5) + "\n")
    /* XXX Debug example size later
    params = new Params(agg)
    params.setExampleSize(params.getExampleSize * 100)
    writer.write("Example Size Up: " + trainAndGet(5) + "\n")
    params = new Params(agg)
    params.setExampleSize(params.getExampleSize / 100)
    if (params.getExampleSize == 0) params.setExampleSize(1)
    writer.write("Example Size Down: " + trainAndGet(5) + "\n")
    */
    /* Figure out which direction to move TBPTT Length */
    params = new Params(agg)
    params.setTBPTTLength(params.getTBPTTLength * 100)
    writer.write("TBPTT Length Up: " + trainAndGet(5) + "\n")
    params = new Params(agg)
    params.setTBPTTLength(params.getTBPTTLength / 100)
    writer.write("TBPTT Length Down: " + trainAndGet(5) + "\n")
    /* Figure out which direction to move RMS Decay */
    params = new Params(agg)
    params.setRMSDecay(params.getRMSDecay * 100)
    writer.write("RMS Decay Up: " + trainAndGet(5) + "\n")
    params = new Params(agg)
    params.setRMSDecay(params.getRMSDecay / 100)
    writer.write("RMS Decay Down: " + trainAndGet(5) + "\n")
    /* Figure out which direction to move L2 Regularization Parameter */
    params = new Params(agg)
    params.setL2Param(params.getL2Param * 100)
    writer.write("L2 Param Up: " + trainAndGet(5) + "\n")
    params = new Params(agg)
    params.setL2Param(params.getL2Param / 100)
    writer.write("L2 Param Down: " + trainAndGet(5) + "\n")
    writer.close()
  }

  def train(agg: Aggregator, params: Params, lstm: CharPredictor): Unit = {
    (0 until params.getEpochs).foreach{_ =>
      val trainer = new Trainer(agg, params, lstm)
      trainer.join
    }
  }
}
