package com.geshaofeng.scala

import org.apache.hadoop.mapreduce.Job
import org.apache.spark.{SparkConf, SparkContext}
import parquet.hadoop.thrift.{ParquetThriftInputFormat, ParquetThriftOutputFormat, ThriftReadSupport}
import parquet.hadoop.{ParquetInputFormat, ParquetOutputFormat}

object SparkParquetThriftApp {
  def main(args: Array[String]) {
    val mem = "30g"
    println("Initializing Spark context.")
    println("  Memory: " + mem)
    val sparkConf = new SparkConf()
      .setAppName("SparkParquetThrift")
      .setMaster("local[1]")
      //.setSparkHome("/usr/lib/spark")
      .setJars(Seq())
      .set("spark.executor.memory", mem)
    val sc = new SparkContext(sparkConf)

    println("Creating sample Thrift data.")
    val sampleData = Range(1,10).toSeq.map{ v: Int =>
      new SampleThriftObject("a"+v,"b"+v,"c"+v)
    }
    println(sampleData.map("  - " + _).mkString("\n"))

    val job = new Job()
    val parquetStore = "hdfs://localhost:8020/sample_store1"
    println("Writing sample data to Parquet.")
    println("  - ParquetStore: " + parquetStore)
    ParquetThriftOutputFormat.setThriftClass(job, classOf[SampleThriftObject])
    ParquetOutputFormat.setWriteSupportClass(job, classOf[SampleThriftObject])
    sc.parallelize(sampleData)
      .map(obj => (null, obj))
      .saveAsNewAPIHadoopFile(
        parquetStore,
        classOf[Void],
        classOf[SampleThriftObject],
        classOf[ParquetThriftOutputFormat[SampleThriftObject]],
        job.getConfiguration
      )

    println("Reading 'col_a' and 'col_b' from Parquet data store.")
    ParquetInputFormat.setReadSupportClass(
      job,
      classOf[ThriftReadSupport[SampleThriftObject]]
    )
    job.getConfiguration.set("parquet.thrift.column.filter", "col_a;col_b")
    val parquetData = sc.newAPIHadoopFile(
      parquetStore,
      classOf[ParquetThriftInputFormat[SampleThriftObject]],
      classOf[Void],
      classOf[SampleThriftObject],
      job.getConfiguration
    ).map{case (void,obj) => obj}
    println(parquetData.collect().map("  - " + _).mkString("\n"))
  }
}
