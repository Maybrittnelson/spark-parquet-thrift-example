package com.geshaofeng.parquet

object Func {

  def main(args: Array[String]): Unit = {
    val list = List(1, 2, 3)
    val f2 = (i: Int) => (i * 2)
    val f3 = (i: Int) => (i * 3)

    val fs = List[Int => Int](f2, f3)
    println(list.flatMap(caculators(fs, _)))
    println(list.map(caculators(fs, _)))

    println(list.flatMap(caculators(fs, _)))
  }

  def caculators(fs: List[Int => Int], i: Int): List[(Int, Int)] = {
    fs.map {
      f => ( i, f(i))
    }
  }
}
