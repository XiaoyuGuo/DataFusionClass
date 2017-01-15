package cn.edu.bjtu

/**
  * Created by xiaoyu on 17-1-12.
  */
import org.apache.spark.SparkConf
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.SparkSession

object MLPTest {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
      .setAppName("MLPTest")
      .setMaster("spark://master:7077")
      .setJars(Array("/home/hadoop/MLP.jar"))

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")


    // Load the data stored in LIBSVM format as a DataFrame.
    val data = spark.read.format("libsvm")
      .load("hdfs://master:9000/sample_formatted.txt")

    // Split the data into train and test
    val splits = data.randomSplit(Array(0.7, 0.3), seed = 14L)
    val train = splits(0)
    val test = splits(1)

    val layers = Array[Int](20, 20, 2)

    // create the trainer and set its parameters
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(14L)
      .setMaxIter(100)

    // train the model
    val model = trainer.fit(train)

    // compute accuracy on the test set
    val result = model.transform(test)
    val predictionAndLabels = result.select("prediction", "label")
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")

    println("Sensitivity = " + predictionAndLabels.filter(x => x(0) == x(1) && x(0) == 1.0).count().toDouble / predictionAndLabels.filter(x => x(1) == 1.0).count().toDouble)
    println("Specificity = " + predictionAndLabels.filter(x => x(0) == x(1) && x(0) == 0.0).count().toDouble / predictionAndLabels.filter(x => x(1) == 0.0).count().toDouble)
    println("Test set accuracy = " + evaluator.evaluate(predictionAndLabels))
  }
}