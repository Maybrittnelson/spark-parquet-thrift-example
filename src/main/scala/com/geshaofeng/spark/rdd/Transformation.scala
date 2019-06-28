package com.geshaofeng.spark.rdd

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Transformation {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Transformation").setMaster("local[3]")
    val sc = new SparkContext(conf)
    val arr = Array(1, 2, 3, 1)

    /*    val unionData = sc.union(List(sc.parallelize(Array(1, 2)), sc.parallelize(Array(1, 2))))
          unionData.foreach(it => println(it))*/
    val campTotalStatTrack = sc.union(arr.map(i => make(sc, i)))

    val unit = campTotalStatTrack.flatMap(it => makeList(it))
      .filter(_._1 > 0)
      .reduceByKey(_ + _)// newVal: just count oldVal By key; return (key, newVal)
      .foreach(it => println(it._1, it._2))


  }

  def makeList(i: Int): List[(Int, Int)] = {
    List((i, i * 2))
  }

  def make(sc: SparkContext, i: Int): RDD[Int] = {
    sc.parallelize(Array(i))
  }

}
