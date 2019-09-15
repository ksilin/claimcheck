package com

import java.io.InputStream

package object example {

  case class DataMessage(key: String, fileName: String, payload: Array[Byte])

  case class MinioAccessConfig(url: String, accessKey: String, secretKey: String)

}
