package com.kainos.activemq.samples

import java.io.File
import java.util.concurrent.TimeUnit
import javax.jms.{DeliveryMode, Session}

import org.apache.activemq.ActiveMQConnectionFactory
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import scala.util.Random


/**
 * Created by shannonh on 09/04/15.
 */
object Generator {

  val queueConfig:Config = ConfigFactory.parseFile(new File("./queue-connection.conf"))

  def main(args: Array[String]): Unit = {
    if (args.size!=3||
        !BigInt(args(0)).isValidInt||
        !BigInt(args(1)).isValidInt||
        !BigInt(args(2)).isValidInt) {

        args.foreach(arg => println("argument provided: " + arg))
        exit
    }

    val messageNumber:BigInt = BigInt(args(0))
    val iterations:BigInt = BigInt(args(1))
    val timeout:BigInt = BigInt(args(2))

    /**
     * Scan the ./xml-messages directory and load all files into Memory
     * Please be sure your machine can handle this, otherwise it's goodnight
     * and hard reset
     */
    val inputXmls = getRecursiveListOfFiles(new File("./xml-messages"))
    val xmlCount = inputXmls.length

    if (xmlCount <= 1) {
      println("Please provide xml messages in ./xml-messages")
      exit
    }

    val lines = mutable.MutableList[String]()
    try {
      inputXmls.foreach(xmlFile => {
        val source = scala.io.Source.fromFile(xmlFile)
        lines.+=(source.mkString)
        source.close()
      })
    } catch {
      case e: Exception => {
        println("Failed to read XML\n" + e.getMessage)
        exit
      }
    }

    /**
     * Load all ActiveMQ connection settings from the Typesafe
     * ./queue-connection.conf file
     *
     * Not error checked so please be wise to include this
     */
    val activeMqUrl:String = queueConfig.getString("activemq.url")
    if (activeMqUrl.isEmpty) exit
    println("Setting ActiveMQ URL: " + activeMqUrl)

    val connectionFactory = new ActiveMQConnectionFactory(activeMqUrl)
    val connection = connectionFactory.createConnection

    val clientId:String = queueConfig.getString("activemq.client")
    if (clientId.isEmpty) exit
    println("Client ID: " + clientId)
    connection.setClientID(clientId)

    connection.start

    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val queue:String = queueConfig.getString("activemq.queue")
    if (queue.isEmpty) exit
    println("Queue Name: " + queue)
    val sendQueue = session.createQueue(queue)

    val producer = session.createProducer(sendQueue)
    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT)

    val correlationId = {
      val random = new Random(System.currentTimeMillis())
      val randomLong:BigInt = random.nextLong()
      randomLong.toString
    }

    /**
     * Begin producing messages based on the details given
     */
    for (iter <- 0 to iterations.toInt) {
      for (message <- 0 to messageNumber.toInt) {
        val textMessage = session.createTextMessage(lines.toList(getASample(xmlCount)))

        textMessage.setJMSCorrelationID(correlationId)

        println("Sending message...")

        producer.send(textMessage)
      }
      try {
        TimeUnit.SECONDS.sleep(timeout.toInt);
      } catch {
        case ie: InterruptedException => {
          println("Interrupted: " + ie.getMessage)
          System.exit(0)
        }
        case e: Exception => {
          println("Failed to post message to Queue\n" + e.getMessage)
          System.exit(1)
        }
      }
    }
    connection.close
  }

  /**
   * Gets a Array[File] based on the directory given
   * @param dir File the directory to recursively search
   * @return    Array[File] an array of files in the directory
   */
  private def getRecursiveListOfFiles(dir: File): Array[File] = {
    try {
      val these = dir.listFiles
      these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
    } catch {
      case e: Exception => {
        exit
        return null
      }
    }
  }

  /**
   * Produce an exit message and close the application upon an error
   */
  private def exit = {
    val errorMessage = "Usage: Java -jar ./messagegenerator_2.11-1.0-one-jar.jar arg0 arg1 arg2\n" +
        "\targ0<BigInt> number of messages to produce per iteration\n" +
        "\targ1<Int> number of iterations\n" +
        "\targ2<Long> seconds between iterations\n" +
        "\n./queue-connection.conf should define the following:\n" +
        "activemq.url=\"tcp://queue:port\"\n" +
        "activemq.queue=\"Queue\"\n" +
        "activemq.client=\"ClientId\"\n" +
        "\n./xml-messages should contain a range of sample xml messages to " +
        "post to the queue specified"

    println(errorMessage)

    System.exit(1)
  }

  /**
   * Get a random integer index in the range of input messages
   * @param quantity  Int the number of xml-messages in ./xml-messages
   * @return          Int a random number in the range
   */
  private def getASample(quantity:Int): Int = {
    val r = new Random()
    val low = 0
    val high = quantity
    r.nextInt(high-low) + low
  }
}
