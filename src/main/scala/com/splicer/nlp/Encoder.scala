package com.splicer.nlp

import collection.JavaConverters._

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

object Encoder {
  /* Encode the given character into a one-hot INDArray */
  def encode(c: Char, ordering: Seq[Char]): INDArray =
    Nd4j.create(ordering.map{o => if (o == c) 1.0 else 0.0}.toArray)

  /* Encode the given string into a matrix of one-hot INDArrays */
  def encode(example: String, ordering: Seq[Char]): INDArray =
    Nd4j.create(example.map{c => encode(c, ordering)}.toList.asJava,
      Array(example.length, ordering.length))

  /* Encode a batch of strings into a 3-dimensional INDArray */
  def encode(batch: List[String], ordering: Seq[Char]): INDArray = {
    Nd4j.create(batch.map{example => encode(example, ordering)}.asJava,
      Array(batch.length, batch(0).length, ordering.length))
  }

  /* Decode a one-hot INDArray into a Char */
  def decode(data: Array[Double], ordering: Seq[Char]): Char =
    ordering(data.zipWithIndex.maxBy(_._1)._2)
}
