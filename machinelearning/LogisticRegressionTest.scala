package cn.edu.bjtu

/**
  * Created by xiaoyu on 17-1-12.
  */
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.SparkSession

object LogisticRegressionTest {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
      .setAppName("LogisticRegressionTest")
      .setMaster("spark://master:7077")
      .setJars(Array("/home/hadoop/LogisticRegression.jar"))

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val data = MLUtils.loadLibSVMFile(spark.sparkContext, "hdfs://master:9000/sample_formatted.txt")

    val splits = data.randomSplit(Array(0.7, 0.3), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    // Run training algorithm to build the model
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(2)
      .run(training)

    // Compute raw scores on the test set.
    val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Get evaluation metrics.
    val metrics = new BinaryClassificationMetrics(predictionAndLabels)
    val auROC = metrics.areaUnderROC()
    println("Area under ROC = " + auROC)
    println("Sensitivity = " + predictionAndLabels.filter(x => x._1 == x._2 && x._1 == 1.0).count().toDouble / predictionAndLabels.filter(x => x._2 == 1.0).count().toDouble)
    println("Specificity = " + predictionAndLabels.filter(x => x._1 == x._2 && x._1 == 0.0).count().toDouble / predictionAndLabels.filter(x => x._2 == 0.0).count().toDouble)
    println("Accuracy = " + predictionAndLabels.filter(x => x._1 == x._2).count().toDouble / predictionAndLabels.count().toDouble)
  }
}