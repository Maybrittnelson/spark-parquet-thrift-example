package com.geshaofeng.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}

object Accumulators {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Accumulators").setMaster("local[3]")
    val sc = new SparkContext(conf)
    val accum = sc.longAccumulator("My longAccumulator")

    var beginTime = System.currentTimeMillis
    sc.parallelize(Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).foreach(accum.add(_))
    var endTime = System.currentTimeMillis
    println(endTime - beginTime)
    println(accum.value)

    beginTime = System.currentTimeMillis
    val i = sc.parallelize(Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).foreach(accum.add(_))
    endTime = System.currentTimeMillis
    println(endTime - beginTime)
    println(accum.value)
  }
}
