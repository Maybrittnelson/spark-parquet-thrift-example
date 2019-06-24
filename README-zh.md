# Spark, Parquet, and Thrift Example.

[Apache Spark](http://spark.apache.org/)是一个分布式计算（1. 与hdfs交互 2.高度利用内存）的研究项目。现代数据集包含成百上千的数据列，导致内存空间不足以缓存所有的数据集。所以Spark必须分页写入磁盘，写入磁盘必然会带来额外的性能消耗。如果Sprak 应用仅仅只跟列的子集进行交互，那么就选择柱状存储的数据库，如[Parquet](http://parquet.apache.org/)，这种数据库只加载特定的列到Spark [RDD](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.RDD)。

[Matt Massie's example](http://zenfractal.com/2013/08/21/a-powerful-big-data-trio/) 使用Parquet，[Avro](http://avro.apache.org/) 做数据序列化、还有等式谓词做过滤器加载，但是没有展示如何加载一个列的子集。这个项目展示一个完整的[Scala](https://www.scala-lang.org/)/[sbt](https://www.scala-sbt.org/)，[Thrift](https://thrift.apache.org/) 做数据序列化、显示如何加载柱状子集

