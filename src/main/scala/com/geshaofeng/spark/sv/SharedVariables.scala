package com.geshaofeng.spark.sv

import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.mutable

object SharedVariables {


  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Shared Variables").setMaster("local[3]")
    val sc = new SparkContext(conf)
    /*
     val broadVar = sc.broadcast(Array(1, 2, 3))*/
    val targetType = mutable.Set[Int](2)
    val broadVar = sc.broadcast(targetType)

    val disData = sc.parallelize(Array(2))

    disData.filter(it => broadVar.value.contains(it)).map(println(_)).count
    targetType -= 2
    //broadcast不可变吗，但是这里不会打印2
    disData.filter(it => broadVar.value.contains(it)).map(println(_)).count
  }
}
