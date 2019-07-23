package com.geshaofeng.scala

import java.util.{Calendar, Date}

object Time {

  def main(args: Array[String]): Unit = {
    val date = new Date()
    val startTime = date.getTime
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val endTime = calendar.getTime.getTime

    println(startTime)
    println(endTime)
    val interval = (endTime - startTime) / 1000l / 60
    println(interval)
  }

}
