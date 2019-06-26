package com.geshaofeng.spark.rdd

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

/**
  *
  */
object Persist {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Spark NB").setMaster("local[1]")
    val sc = new SparkContext(conf)
    val data = Array("1", "12", "123", "1234")

    val disData = sc.parallelize(data)
    /*    transformations操作：map这些惰性执行,不会立刻执行，需要action操作才会执行
        val lineLength = disData.map(it => print(it.length))*/
    val lineLength = disData.map(it => it.length)
    lineLength.persist(StorageLevel.DISK_ONLY)

    //action操作：reduce、collect、count、foreach
    var beignTime = System.currentTimeMillis
    lineLength.foreach(print)
    var endTime = System.currentTimeMillis
    println(endTime - beignTime)

    beignTime = System.currentTimeMillis
    lineLength.foreach(print)
    endTime = System.currentTimeMillis
    println(endTime - beignTime)

    lineLength.unpersist()
  }

  def print(i: Int) = {
    println(i)
  }

}
