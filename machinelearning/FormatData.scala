package cn.edu.bjtu

/**
  * Created by xiaoyu on 17-1-12.
  */
import java.io.PrintWriter

import scala.io.Source

object FormatData extends App{
  val sample = Source
    .fromFile("/home/xiaoyu/sample.csv")
    .getLines()
    .map(
      line => {
        line.toString.split(",")
          .map(
            element => {
              if(element == "yes") {
                1.0
              }
              else if(element == "") {
                0.0
              }
              else if(element == "no") {
                0.0
              }
              else {
                element.toDouble
              }
            }
          )
      }
    )

  val printWriter = new PrintWriter("/home/xiaoyu/sample_formatted.txt")

  sample.foreach(
    sampleArr => {
      val stringBuilder = new StringBuilder()
      stringBuilder.append(s"${sampleArr(20)} ")
      for(i <- 0 to 18) {
        stringBuilder.append(s"${i+1}:${sampleArr(i)} ")
      }
      stringBuilder.append(s"20:${sampleArr(19)}\n")
      printWriter.write(stringBuilder.toString)
    }
  )

  printWriter.close()

}

