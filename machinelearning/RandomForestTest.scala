package cn.edu.bjtu

/**
  * Created by xiaoyu on 17-1-12.
  */
import org.apache.spark.SparkConf
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.SparkSession
object RandomForestTest extends App {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
      .setAppName("RandomForestTest")
      .setMaster("spark://master:7077")
      .setJars(Array("/home/hadoop/RandomForest.jar"))

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // Load and parse the data file.
    val data = MLUtils.loadLibSVMFile(spark.sparkContext, "hdfs://master:9000/sample_formatted.txt")

    // Split the data into training and test sets (30% held out for testing)
    val splits = data.randomSplit(Array(0.7, 0.3))

    val (training, test) = (splits(0), splits(1))

    // Train a RandomForest model.
    // Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 3 // Use more in practice.
    val featureSubsetStrategy = "18" // Let the algorithm choose.
    val impurity = "gini"
    val maxDepth = 14
    val maxBins = 16384

    val model = RandomForest.trainClassifier(training, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }
    val metrics = new BinaryClassificationMetrics(predictionAndLabels)
    val auROC = metrics.areaUnderROC()
    println("Area under ROC = " + auROC)
    println("Sensitivity = " + predictionAndLabels.filter(x => x._1 == x._2 && x._1 == 1.0).count().toDouble / predictionAndLabels.filter(x => x._2 == 1.0).count().toDouble)
    println("Specificity = " + predictionAndLabels.filter(x => x._1 == x._2 && x._1 == 0.0).count().toDouble / predictionAndLabels.filter(x => x._2 == 0.0).count().toDouble)
    println("Accuracy = " + predictionAndLabels.filter(x => x._1 == x._2).count().toDouble / predictionAndLabels.count().toDouble)
  }
}

