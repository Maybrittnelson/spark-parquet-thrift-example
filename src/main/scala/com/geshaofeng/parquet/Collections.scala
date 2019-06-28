package com.geshaofeng.parquet

import java.util

import scala.collection.JavaConversions._

/**
  *
  */
object Collections {

  var ageNames: Map[Int, Set[String]] = null

  def main(args: Array[String]): Unit = {
    /*    val users = User.getUsers
        ageNames = users.filter(it => !it.name.equals("Tom")).groupBy(_.age).map {
          item => {
            (item._1, item._2.map(_.name).toSet)
          }
        }
        ageNames.map(it => loadName( it._1, it._2))*/


    /* sortBy：默认 desc 降序
     val imptCnt = Iterable((1, 2), (2, 3), (3, 4))
        var sum = 0;
        imptCnt.toArray.sortBy(-_._1).map {
          it => {
            sum = sum + it._2
            (it._1, sum)
          }
        }.foreach(it => println(it._1, it._2))*/

    val arr1 = Array((("1", 2, "1:1"), (2, 0, 0, 0)))
    val arr2 = Array((("1", 1, "1"), (1, 2, 3, 4)))
    val arr3 = arr1 ++ arr2
    arr3.map {
      case (lable, value) => {
        println(lable, value)
      }
    }
  }

  def loadName(age: Int, names: Set[String]): Unit = {
    printf("User age: %d, names: %s\n", age, names)
  }

}
