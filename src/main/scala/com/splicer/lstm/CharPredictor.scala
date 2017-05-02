package com.splicer.lstm

import java.io.{File, PrintWriter}

import org.deeplearning4j.nn.conf.{BackpropType, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.layers.{GravesLSTM, RnnOutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.DataSet

class CharPredictor(params: Params) {
  /* Build a hidden layer of Graves LSTM Nodes */
  private def buildGravesLayer(inputSize: Int, outputSize: Int): GravesLSTM =
    new GravesLSTM.Builder()
      .nIn(inputSize)
      .nOut(outputSize)
      .activation(Activation.TANH)
      .build()

  /* Build an RNN output layer with a softmax activation function */
  private def buildOutputLayer(inputSize: Int, outputSize: Int): RnnOutputLayer =
    new RnnOutputLayer.Builder(params.getLossFunction)
      .activation(Activation.SOFTMAX)
      .nIn(inputSize)
      .nOut(outputSize)
      .build()

  val config = new NeuralNetConfiguration.Builder()
    .optimizationAlgo(params.getOptimizer)
    .iterations(1)
    .learningRate(params.getLearningRate)
    .rmsDecay(params.getRMSDecay)
    .seed(12345)
    .regularization(true)
    .l2(params.getL2Param)
    .weightInit(params.getWeightInit)
    .updater(params.getUpdater)
    .list()
    .layer(0, buildGravesLayer(params.getInputSize, params.getLayerSize))
    .layer(1, buildGravesLayer(params.getLayerSize, params.getLayerSize))
    .layer(2, buildOutputLayer(params.getLayerSize, params.getOutputSize))
    .backpropType(BackpropType.TruncatedBPTT)
    .tBPTTForwardLength(params.getTBPTTLength)
    .tBPTTBackwardLength(params.getTBPTTLength)
    .pretrain(false)
    .backprop(true)
    .build()

  val lstm = new MultiLayerNetwork(config)
  lstm.init()
  lstm.setListeners(new ScoreIterationListener(1))

  val writer = new PrintWriter(new File("debug.out"))
  def closeWriter: Unit = writer.close()

  def getParamMap: java.util.Map[String, INDArray] = lstm.paramTable
  def getParams: Array[Double] = lstm.params.data.asDouble

  def train(ds: DataSet): Unit = {
    val inputs = ds.getFeatures.data.asDouble
    val outputs = ds.getLabels.data.asDouble
    inputs.foreach{i => writer.write(" " + i.toString)}
    writer.write("\t\t")
    outputs.foreach{o => writer.write(" " + o.toString)}
    writer.write("\n\n")
    lstm.fit(ds)
  }

  def run(inputs: INDArray): INDArray = {
    val activations = lstm.feedForward(inputs)
    activations.get(activations.size - 1).getRow(params.getExampleSize - 1)
  }
}
