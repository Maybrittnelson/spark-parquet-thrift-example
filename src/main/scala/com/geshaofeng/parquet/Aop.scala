package com.geshaofeng.parquet

object Aop {

  def main(args: Array[String]): Unit = {
    Array("Tom", "Gary", "Peter").map(word).map(length)
  }

  def length(s: String): Int = aopPrint {
    s.length
  }

  def word(s: String): String = aopPrint {
    s
  }

  def aopPrint[U](i: U): U = {
    print(i + " ")
    i
  }
}
