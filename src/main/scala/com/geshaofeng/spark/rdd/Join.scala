package com.geshaofeng.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}

object Join {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Join").setMaster("local")
    val sc = new SparkContext(conf)

    val one = sc.parallelize(List((1, 2), (3, 4), (3, 6)))
    val other = sc.parallelize(List((4, 9)))

    val mapValues = sc.parallelize(List(1, 2, 3))
/*
    //join：只取两个rdd key的交集（不合并重复的key），所以为空
    val all = one.join(other).foreach(println(_))

    //leftOuterJoin：只取左边的key的全集（不合并重复的key），所以结果为(1，(some(2)，none))，(3，(some(4)，none)), (3，(some(6)，none))
    val left = one.leftOuterJoin(other).foreach(
      it => println(it)
    )

    //rightOuterJoin: 只取右边的key的全集（不合并重复的key），
      val right = one.rightOuterJoin(other).foreach(
      it => println(it)
    )
*/
  }
}
