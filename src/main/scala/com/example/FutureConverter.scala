package com.example

import scala.concurrent.{Future, Promise}
import scala.util.Try

trait FutureConverter {

  def toScalaFuture[T](jFuture: java.util.concurrent.Future[T]): Future[T] = {
    val promise = Promise[T]()
    new Thread(() => {
      promise.complete(Try {
        jFuture.get
      })
    }).start()
    promise.future
  }

}
