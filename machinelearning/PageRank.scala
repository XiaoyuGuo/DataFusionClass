package cn.edu.bjtu

/**
  * Created by xiaoyu on 17-1-14.
  */

import org.apache.spark.SparkConf
import org.apache.spark.graphx.GraphLoader
import org.apache.spark.sql.SparkSession


object PageRank {
  def main(args: Array[String]): Unit = {

    // Creates a SparkSession.
    val sparkConf = new SparkConf()
      .setAppName("PageRank")
      .setMaster("spark://master:7077")
      .setJars(Array("/home/hadoop/PageRank.jar"))

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    val sc = spark.sparkContext

    val graph = GraphLoader.edgeListFile(sc, "hdfs://master:9000/followers.txt")

    val ranks = graph.pageRank(0.0001).vertices

    val users = sc
      .textFile("hdfs://master:9000/users.txt")
      .map {
        line => {
          val fields = line.split(",")
          (fields(0).toLong, fields(1))
        }
      }

    val ranksByUsername = users
      .join(ranks)
      .map {
        case (id, (username, rank)) => (username, rank)
      }

    println(ranksByUsername.collect().mkString("\n"))

  }
}

