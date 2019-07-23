package com.geshaofeng.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}

object Two {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Two").setMaster("local[3]")
    val sc = new SparkContext(conf)

  }

}
