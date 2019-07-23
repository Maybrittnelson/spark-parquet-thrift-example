package com.geshaofeng

object TestCase {

  def main(args: Array[String]): Unit = {
    //（1|0 0|1 1|1 是1）（0|0 是0）
    println(1 | 2)
    //（1&1 是1） (1&0 0&1 0&0 是0)
    println(1 & 2)
  }
}
