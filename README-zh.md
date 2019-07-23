[TOC]

# TSpark, Parquet, and Thrift Example.

[Apache Spark](http://spark.apache.org/)是一个分布式计算（1. 与hdfs交互 2.高度利用内存）的研究项目。现代数据集包含成百上千的数据列，导致内存空间不足以缓存所有的数据集。所以Spark必须分页写入磁盘，写入磁盘必然会带来额外的性能消耗。如果Sprak 应用仅仅只跟列的子集进行交互，那么就选择柱状存储的数据库，如[Parquet](http://parquet.apache.org/)，这种数据库只加载特定的列到Spark [RDD](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.RDD)。

[Matt Massie's example](http://zenfractal.com/2013/08/21/a-powerful-big-data-trio/) 使用Parquet，[Avro](http://avro.apache.org/) 做数据序列化、还有等式谓词做过滤器加载，但是没有展示如何加载一个列的子集。这个项目展示一个完整的[Scala](https://www.scala-lang.org/)/[maven](https://maven.apache.org/)，[Thrift](https://thrift.apache.org/) 做数据序列化、显示如何加载柱状子集。

我是在本地mac上开发的，带有`JDK1.8`、`maven-3.6.1`、`thrift@0.9`、`Hadoop 3.1.2`、`spark 2.4.3`

* [install hadoop in mac](https://isaacchanghau.github.io/post/install_hadoop_mac/)
* [install spark in mac](https://www.freecodecamp.org/news/installing-scala-and-apache-spark-on-mac-os-837ae57d283f/)

# Code Walkthrough

## Sample Thrift Object

以下是我们将要存储在Parquet数据库中的对象的简单Thrift模式。有关Thrift的更详细介绍，请参阅[thrift-missing-guide](http://diwakergupta.github.io/thrift-missing-guide/)

```
namespace java com.geshaofeng.parquet

struct SampleThriftObject {
  10: string col_a;
  20: string col_b;
  30: string col_c;
}
```

## Spark Context Initialization

其余的代码片段来自Scala应用程序，该应用程序将作为本地Spark应用程序运行。确保更改SparkHome并将mem设置为计算机上可以用的最大内存量。

```scala
    val mem = "30g"
    println("Initializing Spark context.")
    println("  Memory: " + mem)
    val sparkConf = new SparkConf()
      .setAppName("SparkParquetThrift")
      .setMaster("local[1]")
      .setJars(Seq())
      .set("spark.executor.memory", mem)
    val sc = new SparkContext(sparkConf)
```

### Output

```shell
Initializing Spark context.
  Memory: 30g
```

## Create Sample Thrift Data

下面代码段创建9个样本Thrift对象

```scala
println("Creating sample Thrift data.")
val sampleData = Range(1,10).toSeq.map{ v: Int =>
  new SampleThriftObject("a"+v,"b"+v,"c"+v)
}
println(sampleData.map("  - " + _).mkString("\n"))
```

### Output

```shell
Creating sample Thrift data.
  - SampleThriftObject(col_a:a1, col_b:b1, col_c:c1)
  - SampleThriftObject(col_a:a2, col_b:b2, col_c:c2)
  - SampleThriftObject(col_a:a3, col_b:b3, col_c:c3)
  - SampleThriftObject(col_a:a4, col_b:b4, col_c:c4)
  - SampleThriftObject(col_a:a5, col_b:b5, col_c:c5)
  - SampleThriftObject(col_a:a6, col_b:b6, col_c:c6)
  - SampleThriftObject(col_a:a7, col_b:b7, col_c:c7)
  - SampleThriftObject(col_a:a8, col_b:b8, col_c:c8)
  - SampleThriftObject(col_a:a9, col_b:b9, col_c:c9)
```

## Store to Parquet

此部分从示例对象创建RDD并将它们序列化到Parquet存储区。

```scala
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
```

### Output

```shell
Writing sample data to Parquet.
  - ParquetStore: hdfs://localhost:8020/sample_store1
```

## Reading columns from Parquet

此部分从Parquet商店加载`parquet.thrift.column.filter`中指定的列。过滤器的glob语法在Parquet Cascading 文档中定义如下。此处未指定的列加载为null

```scala
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
```

### Output

```shell
Reading 'col_a' and 'col_b' from Parquet data store.
  - SampleThriftObject(col_a:a1, col_b:b1, col_c:null)
  - SampleThriftObject(col_a:a2, col_b:b2, col_c:null)
  - SampleThriftObject(col_a:a3, col_b:b3, col_c:null)
  - SampleThriftObject(col_a:a4, col_b:b4, col_c:null)
  - SampleThriftObject(col_a:a5, col_b:b5, col_c:null)
  - SampleThriftObject(col_a:a6, col_b:b6, col_c:null)
  - SampleThriftObject(col_a:a7, col_b:b7, col_c:null)
  - SampleThriftObject(col_a:a8, col_b:b8, col_c:null)
  - SampleThriftObject(col_a:a9, col_b:b9, col_c:null)
 
```

# Parquet store on HDFS

使用hdfs命令可以直接查看hdfs上的Parquet存储。_metadata文件遵循apache/incubator-parquet-format中描述的文件格式，数据存储在大小约为20M的部分文件中。

```shell
> hdfs dfs -ls -h 'hdfs://localhost:8020/sample_store'
2019-07-23 12:10:58,784 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Found 3 items
-rw-r--r--   3 gary supergroup          0 2019-06-24 22:11 hdfs://localhost:8020/sample_store/_SUCCESS
-rw-r--r--   3 gary supergroup        844 2019-06-24 22:11 hdfs://localhost:8020/sample_store/_metadata
-rw-r--r--   3 gary supergroup      1.0 K 2019-06-24 22:11 hdfs://localhost:8020/sample_store/part-r-00000.parquet
```

这些列与其他二进制信息一起存储为_meatadata文件中的json。

```json
> hdfs dfs -cat 'hdfs://localhost:8020/sample_store/_metadata'
  {"id" : "STRUCT",
  "children" : [ {
    "name" : "col_a",
    "fieldId" : 10,
    "requirement" : "DEFAULT",
    "type" : {
      "id" : "STRING"
    }
  }, {
    "name" : "col_b",
    "fieldId" : 20,
    "requirement" : "DEFAULT",
    "type" : {
      "id" : "STRING"
    }
  }, {
    "name" : "col_c",
    "fieldId" : 30,
    "requirement" : "DEFAULT",
    "type" : {
      "id" : "STRING"
    }
  } ]
}
```

并且，窥视零件文件显示列存储的数据。

```shell
hdfs dfs -cat \
  'hdfs://localhost:8020/sample_store/part-r-00000.parquet' \
  | hexdump -C
2019-07-23 12:15:01,919 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
00000000  50 41 52 31 15 00 15 78  15 78 2c 15 12 15 00 15  |PAR1...x.x,.....|
00000010  06 15 08 1c 18 02 61 39  18 02 61 31 16 00 00 00  |......a9..a1....|
00000020  00 02 00 00 00 12 01 02  00 00 00 61 31 02 00 00  |...........a1...|
00000030  00 61 32 02 00 00 00 61  33 02 00 00 00 61 34 02  |.a2....a3....a4.|
00000040  00 00 00 61 35 02 00 00  00 61 36 02 00 00 00 61  |...a5....a6....a|
00000050  37 02 00 00 00 61 38 02  00 00 00 61 39 15 00 15  |7....a8....a9...|
```

