package com.geshaofeng.scala

object Case {

  def main(args: Array[String]): Unit = {
    val list = List((1, 2, 3), (4, 5, 6), (7, 8, 9))
    val oneList = list.map(it => s"${it._1}_${it._2}_${it._3}")
    //case 展示字段含义
    val anotherList = list.map {
      case (orderId, shopId, itemId) => {
        s"${orderId}_${shopId}_${itemId}"
      }
    }
    print(oneList, anotherList)

    //case match case
    println("1" match {
      case "1" => "yes"
      case _ => "no"
    })

    val func = (i: Int) => {
      if ((i & 1) > 0) {
        println("hi"+ i)
      } else if((i & 2) > 0) {
        println("hello"+ i)
      } else{
        println("oh no")
      }
    }
    func(3)
  }

  def desc(t: String): String = t match {
    case "1_2_3" => "yes"
    case _ => "no"
  }

}
