package com.example

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.{Bucket, PutObjectResult}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

object S3Support {

  val createClient: MinioAccessConfig => AmazonS3 = (minioConfig) => {
    val credentials = new BasicAWSCredentials(minioConfig.accessKey, minioConfig.secretKey)
    val clientConfiguration = new ClientConfiguration
    clientConfiguration.setSignerOverride("AWSS3V4SignerType")

    AmazonS3ClientBuilder.standard
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(minioConfig.url, Regions.US_EAST_1.name))
      .withPathStyleAccessEnabled(true)
      .withClientConfiguration(clientConfiguration)
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .build
  }

  val createBucketIfNotExists: (AmazonS3, String) => Unit = (s3Client, bucket) => {
    val bucketExist = s3Client.doesBucketExist(bucket)
    if (!bucketExist) {
      println("bucket does not exist")
      val created: Bucket = s3Client.createBucket(bucket)
      println("bucket created")
      println(created)
    }
  }

  val deleteObjectIfExists: (AmazonS3, String, String) => Unit = (s3Client, bucket, fileName) => {
    val objectExists = s3Client.doesObjectExist(bucket, fileName)
    if (objectExists) {
      val deleted = s3Client.deleteObject(bucket, fileName)
      println("deleted")
      println(deleted)
    }
  }

  val printPutObjectResult: PutObjectResult => Unit = res => {
    println("object created: ")
    println(res.getMetadata)
    println(res.getContentMd5)
    println(res.getETag)
    println(res.getExpirationTime)
    println(res.getVersionId)
  }

}
