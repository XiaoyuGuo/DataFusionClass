package cn.edu.bjtu

import org.apache.spark.SparkConf
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.SparkSession

object KMeansTest {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
      .setAppName("KMeansTest")
      .setMaster("spark://master:7077")
      .setJars(Array("/home/hadoop/KMeans.jar"))

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // Load and parse the data
    val data = MLUtils.loadLibSVMFile(spark.sparkContext, "hdfs://master:9000/sample_formatted.txt")
    val parsedData = data.map(s => s.features).cache()

    // Cluster the data into two classes using KMeans
    val numClusters = 2
    val numIterations = 20
    val clusters = KMeans.train(parsedData, numClusters, numIterations)
    val predictionAndLabels = data.map(
      s => {
        (clusters.predict(s.features), s.label)
      })
    // Evaluate clustering by computing Within Set Sum of Squared Errors
    println("Sensitivity = " + predictionAndLabels.filter(x => x._1 == x._2 && x._1 == 1.0).count().toDouble / predictionAndLabels.filter(x => x._2 == 1.0).count().toDouble)
    println("Specificity = " + predictionAndLabels.filter(x => x._1 == x._2 && x._1 == 0.0).count().toDouble / predictionAndLabels.filter(x => x._2 == 0.0).count().toDouble)
    println("Accuracy = " + predictionAndLabels.filter(x => x._1 == x._2).count().toDouble / predictionAndLabels.count().toDouble)
  }
}