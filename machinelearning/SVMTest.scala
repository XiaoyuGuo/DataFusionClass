package cn.edu.bjtu

/**
  * Created by xiaoyu on 17-1-12.
  */
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.SparkSession

object SVMTest {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
      .setAppName("SVMTest")
      .setMaster("spark://master:7077")
      .setJars(Array("/home/hadoop/SVM.jar"))

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val data = MLUtils.loadLibSVMFile(spark.sparkContext, "hdfs://master:9000/sample_formatted.txt")
    // Split data into training (80%) and test (20%).
    val splits = data.randomSplit(Array(0.7, 0.3), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    // Run training algorithm to build the model
    val numIterations = 100
    val model = SVMWithSGD.train(training, numIterations)

    // Clear the default threshold.
    model.setThreshold(-5000)

    // Compute raw scores on the test set.
    val scoreAndLabels = test.map { point =>
      val score = model.predict(point.features)
      (score, point.label)
    }

    // Get evaluation metrics.
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = metrics.areaUnderROC()
    println("Area under ROC = " + auROC)
    println("Sensitivity = " + scoreAndLabels.filter(x => x._1 == x._2 && x._1 == 1.0).count().toDouble / scoreAndLabels.filter(x => x._2 == 1.0).count().toDouble)
    println("Specificity = " + scoreAndLabels.filter(x => x._1 == x._2 && x._1 == 0.0).count().toDouble / scoreAndLabels.filter(x => x._2 == 0.0).count().toDouble)
    println("Accuracy = " + scoreAndLabels.filter(x => x._1 == x._2).count().toDouble / scoreAndLabels.count().toDouble)
  }
}