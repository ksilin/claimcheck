package com.example

import better.files.File._
import better.files._
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectResult, S3Object, S3ObjectInputStream}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.util.IOUtils
import com.typesafe.scalalogging.LazyLogging

// import java.io.ByteArrayInputStream
import java.io.ByteArrayInputStream
// import java.io.InputStream
import java.io.InputStream

import io.minio.MinioClient
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, MustMatchers}


class MinioTest extends FreeSpec
  with MustMatchers
  with LazyLogging
  with FutureConverter
  with ScalaFutures {

  val remoteConfig = MinioAccessConfig( url = "https://play.min.io", accessKey = "Q3AM3UQ867SPQQA43P2F", secretKey = "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
  val localConfig = MinioAccessConfig( url = "http://localhost:9001", accessKey = "AKIAIOSFODNN7EXAMPLE", secretKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")

  "minio client" in {

    val minioConfig = localConfig
    val client = new MinioClient(minioConfig.url, minioConfig.accessKey, minioConfig.secretKey);

    val bucket = "test"
    val fileName = "test.txt"
    MinioSupport.createBucketIfNotExists(client, bucket)
    MinioSupport.deleteObjectIfExists(client, bucket, fileName)
    // Check if the bucket already exists.
    val bucketExists: Boolean = client.bucketExists(bucket);
    bucketExists mustBe true
  }

  "amz s3 1.x client" in {

    import S3Support._

    val minioConfig = localConfig
    val s3Client: AmazonS3 = createClient(minioConfig)

    val bucket = "test"
    val fileName = "test.txt"
    createBucketIfNotExists(s3Client, bucket)
    deleteObjectIfExists(s3Client, bucket, fileName)

    val data = createTestData(fileName)
    val meta = new ObjectMetadata()

    val objCreated: PutObjectResult = s3Client.putObject(bucket, fileName, new ByteArrayInputStream(data), meta)

    val objectExists2 = s3Client.doesObjectExist(bucket, fileName)
    objectExists2 mustBe true

    val obj: S3Object = s3Client.getObject(bucket, fileName)
    obj.getBucketName mustBe bucket
    obj.getKey mustBe fileName
    val stream: S3ObjectInputStream = obj.getObjectContent
    // process data
    val byteArray: Array[Byte] = IOUtils.toByteArray(obj.getObjectContent)
    byteArray.sameElements(data) mustBe true
  }

  private def createTestData(fileName: String, content: String = scala.util.Random.alphanumeric.take(12).mkString): Array[Byte] = {
    val file: File = root / "tmp" / fileName
    file.overwrite(content)
    file.byteArray
  }
}
