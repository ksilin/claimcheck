package com.example

import com.amazonaws.services.s3.model.{ObjectMetadata, S3ObjectInputStream}
import com.amazonaws.util.IOUtils
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Serdes
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, MustMatchers}

// import java.io.ByteArrayInputStream
import java.io.ByteArrayInputStream
// import java.io.InputStream
import java.io.InputStream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random
import scala.jdk.CollectionConverters._

class ClaimCheckTest extends FreeSpec
  with MustMatchers
   with LazyLogging
  with FutureConverter
  with ScalaFutures {

  import S3Support._

  val minioConfig = MinioAccessConfig( url = "http://localhost:9001", accessKey = "AKIAIOSFODNN7EXAMPLE", secretKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
  val bucket = "test"

  val s3Client = createClient(minioConfig)
  createBucketIfNotExists(s3Client, bucket)

  val storeDataAndMakeClaimRecord: (String, String, DataMessage) => ProducerRecord[String, String] = (bucket, topic, msg) => {
    s3Client.putObject(bucket, msg.fileName,new ByteArrayInputStream( msg.payload), new ObjectMetadata())
    new ProducerRecord[String, String](topic, 0, msg.key, s"$bucket/${msg.fileName}")
  }

  val retrieveDataAndRestoreDataMessage: ConsumerRecord[String, String] => DataMessage = record => {
    val path = record.value().split('/')
    val bucketName = path.head
    val fileName = path.tail.head
    val obj = s3Client.getObject(bucketName, fileName)
    val stream: S3ObjectInputStream = obj.getObjectContent
   DataMessage(record.key(), fileName, IOUtils.toByteArray(stream) )
  }

  val kafkaHost: String = "localhost:9091"
  val topicName         = "claimCheckTopic1"
  val loggedStep        = 100

  val stringSerdes = Serdes.String()
  val producer = makeProducer
  val consumer = makeConsumer

  val msgs = mkDataMessages()
  val records = msgs map (storeDataAndMakeClaimRecord(bucket, topicName, _))

  "must produce and consume messages using the claim check pattern" in {

    val x: Future[List[RecordMetadata]] = Future.traverse(records.toList) {
      r: ProducerRecord[String, String] =>
        toScalaFuture(producer.send(r, loggingProducerCallback))
    }
    val metadata = Await.result(x, 10.seconds)

    // faster that subscribe, enough for demo purposes‚
    consumer.assign(List(new TopicPartition(topicName, 0)).asJava)

    var consumed: ConsumerRecords[String, String] = null
    var initialPollAttempts        = 0
    val pollDuration = java.time.Duration.ofMillis(100)
    // subscription is not immediate
    while (consumed == null || consumed.isEmpty) {
      consumed = consumer.poll(pollDuration)
      initialPollAttempts = initialPollAttempts + 1
    }
    println(s"required ${initialPollAttempts} polls to get first data")

    val consumedRecords: List[ConsumerRecord[String, String]] = consumed.records(topicName).asScala.toList
    consumedRecords.size mustBe msgs.size

    val enrichedMessages = consumedRecords map retrieveDataAndRestoreDataMessage

    enrichedMessages.map(_.fileName) must contain theSameElementsAs msgs.map(_.fileName)
    // a bit cumbersome to compare the byte array content
    enrichedMessages.map(_.payload) forall (bA => msgs.map(_.payload).exists(bO => bA.sameElements(bO)))
  }

  private def mkDataMessages(count: Int = 8): Iterable[DataMessage] = {
    (0 until count ) map ( i => DataMessage(i.toString,  Random.alphanumeric.take(12).mkString, Random.nextBytes(16)))
  }

  private def makeProducer = {
    val producerJavaProps = new java.util.Properties
    producerJavaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost)
    new KafkaProducer[String, String](producerJavaProps,
      stringSerdes.serializer(),
      stringSerdes.serializer())
  }

  private def makeConsumer = {
    val consumerGroup = "claimCheckGroup"
    val consumerJavaProps = new java.util.Properties
    consumerJavaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost)
    consumerJavaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    consumerJavaProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup)
    //consumerJavaProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1")       // record-by-record consuming
    consumerJavaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    new KafkaConsumer[String, String](consumerJavaProps, stringSerdes.deserializer(),
      stringSerdes.deserializer())
  }

  val loggingProducerCallback = new Callback {
    override def onCompletion(meta: RecordMetadata, e: Exception): Unit =
      if (e == null)
        logger.info(
          s"published to kafka: ${meta.topic()} : ${meta.partition()} : ${meta.offset()} : ${meta.timestamp()} "
        )
      else logger.error(s"failed to publish to kafka: $e")
  }
}
