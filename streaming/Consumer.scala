/**
  * Created by xiaoyu on 17-1-14.
  */
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SparkSession

object Consumer {

  def main(args: Array[String]): Unit = {

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("streaming")

    val sparkConf = new SparkConf().setMaster("local[8]").setAppName("KafkaTest")
    val streamingContext = new StreamingContext(sparkConf, Seconds(1))
    // Create a input direct stream
    val kafkaStream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val sc = SparkSession.builder().master("local[8]").appName("KafkaTest").getOrCreate()
    val model = SVMModel.load(sc.sparkContext, "/home/xiaoyu/model")
    val result = kafkaStream.map(record => (record.key, record.value))
    result.foreachRDD(
      patient => {
        patient.collect().toBuffer.foreach(
          (x: (Any, String)) => {
            val features = x._2.split(',').map(x => x.toDouble).tail
            println(model.predict(Vectors.dense(features)))

          }
        )
      }
    )

    streamingContext.start()
    streamingContext.awaitTermination()

  }
}
