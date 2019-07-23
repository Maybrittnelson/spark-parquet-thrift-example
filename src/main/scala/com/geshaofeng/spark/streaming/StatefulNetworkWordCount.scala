package com.geshaofeng.spark.streaming

import org.apache.spark._
import org.apache.spark.streaming._

object StatefulNetworkWordCount {

  def main(args: Array[String]): Unit = {

    StreamingExamples.setStreamLogLevels();

    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    //Create a local StreamingContext with two working thread and batch interval of 1 second.
    //The master requires 2 cores to prevent a starvation scenario.
    val ssc = new StreamingContext(conf, Seconds(1))
   // ssc.checkpoint(".")


    //Initial state RDD for mapWithState operation
    val initialRDD = ssc.sparkContext.parallelize(List[(String, Int)](("hello", 1), ("world", 1)))

    val lines = ssc.socketTextStream("localhost", 9999)

    val words = lines.flatMap(_.split(" "))
    val wordDStream = words.map(word => (word, 1))
    //val wordCounts = pairs.reduceByKey(_+_)
    val mappingFunction = (word: String, one: Option[Int], state: State[Int]) => {
      val sum = one.getOrElse(0) + state.getOption().getOrElse(0)
      val output = (word, sum)
      state.update(sum)
      output
    }

    val stateDStream = wordDStream.mapWithState(
      StateSpec.function(mappingFunction).initialState(initialRDD)
    )


    stateDStream.print()
    //Start receiving data and processing it using
    ssc.start()
    //Wait for the processing to be stopped (manually or due to any error)
    ssc.awaitTermination()
  }
}
