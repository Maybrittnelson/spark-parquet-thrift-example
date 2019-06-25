package com.geshaofeng.parquet

import java.util

import scala.collection.JavaConversions._

/**
  *
  */
object Collections {

  var ageNames: Map[Int, Set[String]] = null

  def main(args: Array[String]): Unit = {
    val users = User.getUsers
    ageNames = users.filter(it => !it.name.equals("Tom")).groupBy(_.age).map {
      item => {
        (item._1, item._2.map(_.name).toSet)
      }
    }

    ageNames.map(it => loadName( it._1, it._2))
  }

  def loadName(age: Int, names: Set[String]): Unit = {
    printf("User age: %d, names: %s\n", age, names)
  }

}
