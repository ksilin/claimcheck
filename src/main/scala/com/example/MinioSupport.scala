package com.example

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
