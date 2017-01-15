/**
  * Created by xiaoyu on 17-1-14.
  */
import java.util.Properties
import scala.io.Source
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * Edited by gxy on 2016/1/9.
  * Reason: Version of kafka api is out of date
  */
object Producer {
  def main(args: Array[String]): Unit = {

    val topic = "streaming"
    val brokers = "localhost:9092"
    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    val producer = new KafkaProducer[String, String](props);

    Source.fromFile("/home/xiaoyu/sample_formatted.txt")
      .getLines()
      .foreach(
        line => {
          println(line)
          val stringBuilder = new StringBuilder()
          line.toString.split(" ").foreach(
            element => {
              if(element.contains(':')){
                stringBuilder.append(element.split(':')(1).replace("\n", "") + ",")
              } else {
                stringBuilder.append(element + ",")
              }
            }
          )
          stringBuilder.deleteCharAt(stringBuilder.length - 1)
          producer.send(new ProducerRecord[String, String](topic, stringBuilder.toString))
          println("Message sent: " + stringBuilder)
          Thread.sleep(1000)
        }
      )


  }
}

