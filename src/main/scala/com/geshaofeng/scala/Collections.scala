package com.geshaofeng.scala

import java.util

import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer}

/**
  *
  */
object Collections {

  var ageNames: Map[Int, Set[String]] = null

  def main(args: Array[String]): Unit = {
    val arr = Array[(Int, Int)]((3, 1), (2, 1))
    arr.sortBy(_._1).foreach(it => println(it))
  }

  def loadName(age: Int, names: Set[String]): Unit = {
    printf("User age: %d, names: %s\n", age, names)
  }

  def convert(did: Int, list: Array[Int]): List[(Int, String)] = {
    var result = new ListBuffer[(Int, String)]
    for (i <- 0 to list.size - 1) {
      for (j <- i + 1 to list.size - 1) {
        result += ((did, (list(i) + "," + list(j))))
      }
    }
    result.toList
  }

}
