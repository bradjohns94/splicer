package com.splicer.lstm

import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.Updater
import org.deeplearning4j.nn.weights.WeightInit

import com.splicer.twitter.Aggregator

class Params(agg: Aggregator) {
  /* Static parameters defined by Aggregator metatdata */
  val uniqueChars = agg.getCounts.toSeq.sortBy(_._2).map(_._1)

  def getInputSize: Int = uniqueChars.length
  def getOutputSize: Int = uniqueChars.length
  def getCharOrder: Seq[Char] = uniqueChars

  /* Static parameters defined to optimize the LSTM */
  val lossFunction = LossFunction.MCXENT
  val optimizer = OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT
  val weightInitMethod = WeightInit.XAVIER
  val updater = Updater.RMSPROP

  def getLossFunction: LossFunction = lossFunction
  def getOptimizer: OptimizationAlgorithm = optimizer
  def getWeightInit: WeightInit = weightInitMethod
  def getUpdater: Updater = updater

  /* Mutable params with metadata-derived estimates */
  // LSTM Memory. ~= 2 words
  var layerSize = (2 * agg.getAvgWordLength).toInt
  // Length of the input sequence: ~= 1/2 tweet
  var batchSize = (0.5 * agg.getAvgTweetLength).toInt
  // Trucated Backpropogation Through Time Length. ~= 2 words
  var tbpttLength = (2 * agg.getAvgWordLength).toInt
  // Number of times to iterate over the training set.
  var epochs = 10000 / agg.length
  // Degree of which the LSTM compensates for error
  var learningRate = 0.1
  // Decay of the Root Mean Square (RMS) Gradient
  var rmsDecay = 0.95
  // Parameter to specify how much we favor low weights
  var l2Param = 0.01

  def setLayerSize(size: Int): Unit = layerSize = size
  def getLayerSize: Int = layerSize

  def setBatchSize(size: Int): Unit = batchSize = size
  def getBatchSize: Int = batchSize

  def setTBPTTLength(len: Int): Unit = tbpttLength = len
  def getTBPTTLength: Int = tbpttLength

  def setEpochs(num: Int): Unit = epochs = num
  def getEpochs: Int = epochs

  def setLearningRate(rate: Double): Unit = learningRate = rate
  def getLearningRate: Double = learningRate

  def setRMSDecay(decay: Double): Unit = rmsDecay = decay
  def getRMSDecay: Double = rmsDecay

  def setL2Param(param: Double): Unit = l2Param = param
  def getL2Param: Double = l2Param
}
