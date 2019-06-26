[TOC]

## Overview

在较高的层次上，每个Spark应用程序都包含一个驱动程序，该程序运行用户的**main function**并在群集上执行各种并行操作。 Spark提供的**主要抽象是弹性分布式数据集（RDD）**，它是跨群集节点分区的元素的集合，可以并行操作。 RDD是通过从Hadoop文件系统（或任何其他Hadoop支持的文件系统）中的文件或驱动程序中的现有Scala集合开始并对其进行转换而创建的。用户还可以要求Spark在内存中保留RDD，允许它在并行操作中有效地重用。最后，RDD会自动从节点故障中恢复。 

Spark中的**第二个抽象是可以在并行操作中使用的共享变量**。默认情况下，当Spark并行运行一个函数作为不同节点上的一组任务时，它会将函数中使用的每个变量的副本发送给每个任务。有时，变量需要跨任务共享，或者在任务和驱动程序之间共享。 Spark支持两种类型的共享变量：**广播变量**（broadcast），可用于缓存所有节点的内存中的值;**累加器**（accumulators），它们是仅“添加”到的变量，例如计数器和总和。

本指南以Spark支持的每种语言显示了这些功能。如果你启动Spark的交互式shell，最简单的方法就是 - 用于Scala shell的bin / spark-shell或用于Python的bin / pyspark。

| Spark 抽象 | 生成方式                   | 特性                                            |
| ---------- | -------------------------- | ----------------------------------------------- |
| RDD        | 1. 文件。2.Scala已有的集合 | 1.可以在内存中保留RDD，进行复用。2.自动故障恢复 |
| 共享变量   | 1.广播变量。2.累加器       |                                                 |

# Linking with Spark

Spark 2.4.3 is built and distributed to work with Scala 2.12 by default. (Spark can be built to work with other versions of Scala, too.) To write applications in Scala, you will need to use a compatible Scala version (e.g. 2.12.X).

To write a Spark application, you need to add a Maven dependency on Spark. Spark is available through Maven Central at:

```
groupId = org.apache.spark
artifactId = spark-core_2.1
version = 2.4.3
```

In addition, if you wish to access an HDFS cluster, you need to add a dependency on `hadoop-client` for your version of HDFS.

```
groupId = org.apache.hadoop
artifactId = hadoop-client
version = <your-hdfs-version>
```

Finally, you need to import some Spark classes into your program. Add the following lines:

```
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
```

(Before Spark 1.3.0, you need to explicitly `import org.apache.spark.SparkContext._` to enable essential implicit conversions.)

# Initializing Spark

The first thing a Spark program must do is to create a [SparkContext](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.SparkContext) object, which tells Spark how to access a cluster. To create a `SparkContext`you first need to build a [SparkConf](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.SparkConf) object that contains information about your application.

**Only one SparkContext may be active per JVM**. You must `stop()` the active SparkContext before creating a new one.

```
val conf = new SparkConf().setAppName(appName).setMaster(master)
new SparkContext(conf)
```

The `appName` parameter is a name for your application to show on the cluster UI. `master` is a [Spark, Mesos or YARN cluster URL](https://spark.apache.org/docs/latest/submitting-applications.html#master-urls), or a special “local” string to run in local mode. In practice, when running on a cluster, you will not want to hardcode（硬编码） `master` in the program, but rather [launch the application with `spark-submit`](https://spark.apache.org/docs/latest/submitting-applications.html) and receive it there. However, for local testing and unit tests, you can pass “local” to run Spark in-process.

## Using the Shell

In the Spark shell, a special interpreter-aware SparkContext is already created for you, in the variable called `sc`. Making your own SparkContext will not work. You can set which master the context connects to using the `--master` argument, and you can add JARs to the classpath by passing a comma-separated list to the `--jars` argument. You can also add dependencies (e.g. Spark Packages) to your shell session by supplying a comma-separated list of Maven coordinates to the `--packages` argument. Any additional repositories where dependencies might exist (e.g. Sonatype) can be passed to the `--repositories` argument. For example, to run `bin/spark-shell` on exactly four cores, use:

```
$ ./bin/spark-shell --master local[4]
```

Or, to also add `code.jar` to its classpath, use:

```
$ ./bin/spark-shell --master local[4] --jars code.jar
```

To include a dependency using Maven coordinates:

```
$ ./bin/spark-shell --master local[4] --packages "org.example:example:0.1"
```

For a complete list of options, run `spark-shell --help`. Behind the scenes, `spark-shell` invokes the more general [`spark-submit` script](https://spark.apache.org/docs/latest/submitting-applications.html).

# Resilient Distributed Datasets(RDDs)

Spark revolves around the concept of a *resilient distributed dataset* (RDD), which is a fault-tolerant collection of elements that can be operated on in parallel. There are two ways to create RDDs: **parallelizing an existing collection** in your driver program, or referencing **a dataset in an external storage system**, such as a shared filesystem, HDFS, HBase, or any data source offering a Hadoop InputFormat.

## Paralleized Collections

Parallelized collections are created by calling `SparkContext`’s `parallelize` method on an existing collection in your driver program (a Scala `Seq`). The elements of the collection are copied to form（形成） a distributed dataset that can be operated on in parallel. For example, here is how to create a parallelized collection holding the numbers 1 to 5:

```
val data = Array(1, 2, 3, 4, 5)
val distData = sc.parallelize(data)
```

Once created, the distributed dataset (`distData`) can be operated on in parallel. For example, we might call `distData.reduce((a, b) => a + b)`to add up the elements of the array. We describe operations on distributed datasets later on.

One important parameter for parallel collections is the number of *partitions* （分区）to cut the dataset into. Spark will run one task for each partition of the cluster. Typically you want 2-4 partitions for each CPU in your cluster. Normally, Spark tries to set the number of partitions automatically based on your cluster. However, you can also set it manually by passing it as a second parameter to `parallelize` (e.g. `sc.parallelize(data, 10)`). Note: some places in the code use the term slices (a synonym for partitions) to *maintain backward compatibility*（保持向后兼容）.

## External Datasets

Spark can create distributed datasets from any storage source supported by Hadoop, including your local file system, HDFS, Cassandra, HBase, [Amazon S3](http://wiki.apache.org/hadoop/AmazonS3), etc. Spark supports text files, [SequenceFiles](http://hadoop.apache.org/common/docs/current/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html), and any other Hadoop [InputFormat](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/InputFormat.html).

Text file RDDs can be created using `SparkContext`’s `textFile` method. This method takes an URI for the file (either a local path on the machine, or a `hdfs://`, `s3a://`, etc URI) and reads it as a collection of lines. Here is an example invocation:

```
scala> val distFile = sc.textFile("data.txt")
distFile: org.apache.spark.rdd.RDD[String] = data.txt MapPartitionsRDD[10] at textFile at <console>:26
```

Once created, `distFile` can be acted on by dataset operations. For example, we can add up the sizes of all the lines using the `map` and `reduce`operations as follows: `distFile.map(s => s.length).reduce((a, b) => a + b)`.

Some notes on reading files with Spark:

- If using a path on the local filesystem, the file *must also be accessible at the same path on worker nodes*（必须在其他工作节点的相同路径也能访问到）. Either *copy the file to all workers* or *use a network-mounted shared file system*（两者选其一）.
- All of Spark’s file-based input methods, including `textFile`, support running on directories, compressed（压缩） files, and wildcards（通配符） as well. For example, you can use `textFile("/my/directory")`, `textFile("/my/directory/*.txt")`, and `textFile("/my/directory/*.gz")`.
- The `textFile` method also takes an optional second argument for controlling the number of partitions of the file. By default, Spark creates one partition for each block of the file (blocks being 128MB by default in HDFS), but you can also ask for a higher number of partitions by passing a larger value. *Note that you cannot have fewer partitions than blocks*（你不能拥有比块少的分区）.

Apart from text files, Spark’s Scala API also supports several other data formats:

- `SparkContext.wholeTextFiles` lets you read a directory containing multiple small text files, and returns each of them as (filename, content) pairs. This is in contrast with `textFile`, which would return one record per line in each file. Partitioning is determined by data locality which, in some cases, may result in too few partitions. For those cases, `wholeTextFiles` provides an optional second argument for controlling the minimal number of partitions.
- For [SequenceFiles](http://hadoop.apache.org/common/docs/current/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html), use SparkContext’s `sequenceFile[K, V]` method where `K` and `V` are the types of key and values in the file. These should be subclasses of Hadoop’s [Writable](http://hadoop.apache.org/common/docs/current/api/org/apache/hadoop/io/Writable.html) interface, like [IntWritable](http://hadoop.apache.org/common/docs/current/api/org/apache/hadoop/io/IntWritable.html) and [Text](http://hadoop.apache.org/common/docs/current/api/org/apache/hadoop/io/Text.html). In addition, Spark allows you to specify native types for a few common Writables; for example, `sequenceFile[Int, String]` will automatically read IntWritables and Texts.
- For other Hadoop InputFormats, you can use the `SparkContext.hadoopRDD` method, which takes an arbitrary `JobConf` and input format class, key class and value class. Set these the same way you would for a Hadoop job with your input source. You can also use `SparkContext.newAPIHadoopRDD` for InputFormats based on the “new” MapReduce API (`org.apache.hadoop.mapreduce`).
- `RDD.saveAsObjectFile` and `SparkContext.objectFile` support saving an RDD in a simple format consisting of serialized Java objects. While this is not as efficient as specialized formats like Avro, it offers an easy way to save any RDD.

## RDD Operations

### Basics

To illustrate（说明） RDD basics, consider the simple program below:

```
val lines = sc.textFile("data.txt")
val lineLengths = lines.map(s => s.length)
val totalLength = lineLengths.reduce((a, b) => a + b)
```

The first line defines a base RDD from an external file. This dataset is not loaded in memory or otherwise acted on: `lines` is merely a pointer to the file. The second line defines `lineLengths` as the result of a `map` transformation. Again, `lineLengths` is *not immediately computed*（不会立马执行）, due to laziness（惰性）. Finally, we run `reduce`, which is an action. At this point Spark breaks the computation into tasks to run on separate machines, and each machine runs both its part of the map and a local reduction, returning only its answer to the driver program.

If we also wanted to use `lineLengths` again later, we could add:

```
lineLengths.persist()
```

before the `reduce`, which would cause `lineLengths` to be saved in memory after the first time it is computed.

### Passing Functions to Spark

### Understanding cosures

#### Example

#### Local vs. cluster modes

#### Printing elements of an RDD

### Working with Key-Value Pairs

### Transformations

### Actions

### Shuffle operations

#### Background

#### Performance Impact

## RDD Persistence

One of the most important capabilities in Spark is *persisting* (or *caching*) a dataset in memory across operations. When you persist an RDD, each node stores any partitions of it that it computes in memory and reuses them in other actions on that dataset (or datasets derived from it). **This allows future actions to be much faster** (often by more than 10x). **Caching is a key tool for iterative algorithms and fast interactive use**（缓存是迭代算法和快速交互的关键工具）.

You can mark an RDD to be persisted using the `persist()` or `cache()` methods on it. The first time it is computed in an action, it will be kept in memory on the nodes. **Spark’s cache is fault-tolerant** – if any partition of an RDD is lost, it will automatically be recomputed using the transformations that originally created it.

In addition, each persisted RDD can be stored using a different *storage level*, allowing you, for example, to persist the dataset on disk, persist it in memory but as serialized Java objects (to save space), replicate it across nodes. These levels are set by passing a `StorageLevel` object ([Scala](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.storage.StorageLevel),[Java](https://spark.apache.org/docs/latest/api/java/index.html?org/apache/spark/storage/StorageLevel.html), [Python](https://spark.apache.org/docs/latest/api/python/pyspark.html#pyspark.StorageLevel)) to `persist()`. The `cache()` method is a shorthand for using the default storage level, which is `StorageLevel.MEMORY_ONLY` (store deserialized objects in memory). The full set of storage levels is:

| Storage Level                          | Meaning                                                      |
| :------------------------------------- | :----------------------------------------------------------- |
| MEMORY_ONLY                            | Store RDD as deserialized Java objects in the JVM. If the RDD does not fit in memory, some partitions will not be cached and will be recomputed on the fly each time they're needed. This is the default level. |
| MEMORY_AND_DISK                        | Store RDD as deserialized Java objects in the JVM. If the RDD does not fit in memory, store the partitions that don't fit on disk, and read them from there when they're needed. |
| MEMORY_ONLY_SER  (Java and Scala)      | Store RDD as *serialized* Java objects (one byte array per partition). This is generally more space-efficient than deserialized objects, especially when using a [fast serializer](https://spark.apache.org/docs/latest/tuning.html), but more CPU-intensive to read. |
| MEMORY_AND_DISK_SER  (Java and Scala)  | Similar to MEMORY_ONLY_SER, but spill partitions that don't fit in memory to disk instead of recomputing them on the fly each time they're needed. |
| DISK_ONLY                              | Store the RDD partitions only on disk.                       |
| MEMORY_ONLY_2, MEMORY_AND_DISK_2, etc. | Same as the levels above, but replicate each partition on two cluster nodes. |
| OFF_HEAP (experimental)                | Similar to MEMORY_ONLY_SER, but store the data in [off-heap memory](https://spark.apache.org/docs/latest/configuration.html#memory-management). This requires off-heap memory to be enabled. |

### Which Storage Level to Choose?

Spark’s storage levels are meant to provide different trade-offs between memory usage and CPU efficiency. We recommend going through the following process to select one:

- If your RDDs fit comfortably with the default storage level (`MEMORY_ONLY`), *leave them that way*（请保持这种状态）. This is the most CPU-efficient option, allowing operations on the RDDs to run as fast as possible.
- If not, try using `MEMORY_ONLY_SER` and [selecting a fast serialization library](https://spark.apache.org/docs/latest/tuning.html) to make the objects much more *space-efficient*（节省空间）, but still reasonably fast to access. (Java and Scala)
- **Don’t spill to disk unless the functions that computed your datasets are expensive, or they filter a large amount of the data**. Otherwise, recomputing a partition may be as fast as reading it from disk.
- Use the replicated storage levels if you want fast fault recovery (e.g. if using Spark to serve requests from a web application). *All* the storage levels provide full fault tolerance by recomputing lost data, but the replicated ones let you continue running tasks on the RDD without waiting to recompute a lost partition.

| 存储级别                             | 优点                                                         |
| ------------------------------------ | ------------------------------------------------------------ |
| MEMORY_ONLY                          | cpu效率最高                                                  |
| MEMORY_ONLY_SER                      | 帮对象节省空间                                               |
| disk相关的                           | 处理大数据集                                                 |
| MEMORY_ONLY_2, MEMORY_AND_DISK_2,etc | 一个节点的分区发生故障，可以无需recompute这个节点分区，而是使用另一个节点的相同分区。因为这个存储级别，会将每个分区保存到两个不同集群的节点 |

### Removing Data

* 手动删除：RDD.unpersist
* 自动删除：Spark 自动监控每个节点的缓存使用情况，并以最近最少使用（LRU）的方式删除旧数据分区

# Shared Variables

Normally, when a function passed to a Spark operation (such as `map` or `reduce`) is executed on a remote cluster node, it works on separate **copies of all the variables**（数据副本） used in the function. These variables are copied to each machine, and **no updates to the variables on the remote machine are propagated back to the driver program**（远程机器上更新的变量不会回传给驱动程序）. Supporting general, read-write shared variables **across tasks**（跨任务） would be **inefficient**（效率低下）. However, Spark does provide two limited types of *shared variables* for two common usage patterns: broadcast variables and accumulators.

## Broadcast Variables

Broadcast variables allow the programmer to keep a read-only variable cached on each machine rather than shipping a copy of it with tasks. They can be used, for example, to give every node a copy of a large input dataset in an efficient manner. Spark also attempts to distribute broadcast variables using efficient broadcast algorithms to reduce communication cost.

Spark actions are executed through a set of stages, separated by distributed “shuffle” operations. Spark automatically broadcasts the common data needed by tasks within each stage. The data broadcasted this way is cached in serialized form and deserialized before running each task. This means that explicitly creating broadcast variables is only useful when tasks across multiple stages need the same data or when caching the data in deserialized form is important.

Broadcast variables are created from a variable `v` by calling `SparkContext.broadcast(v)`. The broadcast variable is a wrapper around `v`, and its value can be accessed by calling the `value` method. The code below shows this:

- [**Scala**](https://spark.apache.org/docs/latest/rdd-programming-guide.html#tab_scala_9)
- [**Java**](https://spark.apache.org/docs/latest/rdd-programming-guide.html#tab_java_9)
- [**Python**](https://spark.apache.org/docs/latest/rdd-programming-guide.html#tab_python_9)

```scala
scala> val broadcastVar = sc.broadcast(Array(1, 2, 3))
broadcastVar: org.apache.spark.broadcast.Broadcast[Array[Int]] = Broadcast(0)

scala> broadcastVar.value
res0: Array[Int] = Array(1, 2, 3)
```

After the broadcast variable is created, it should be used instead of the value `v` in any functions run on the cluster so that `v` is not shipped to the nodes more than once. In addition, the object `v` should not be modified after it is broadcast in order to ensure that all nodes get the same value of the broadcast variable (e.g. if the variable is shipped to a new node later).

## Accumulators



