package com.geshaofeng.scala

import java.text.SimpleDateFormat
import java.util.Date

object Date {

  val SDF = new SimpleDateFormat("HH")
  def main(args: Array[String]): Unit = {
    val end = SDF.format(new Date()).toInt
    println(end)
    println(Integer.MAX_VALUE)
  }
}
