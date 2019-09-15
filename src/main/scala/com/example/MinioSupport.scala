package com.example

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import io.minio.MinioClient

object MinioSupport {

  val createBucketIfNotExists: (MinioClient, String) => Unit = (client, bucket) => {
    val bucketExist = client.bucketExists(bucket)
    if (!bucketExist) {
      println("bucket does not exist")
      client.makeBucket(bucket)
      println("bucket created")
    }
  }

  val deleteObjectIfExists: (MinioClient, String, String) => Unit = (client, bucket, fileName) => {
    client.removeObject(bucket, fileName)
      }

}
