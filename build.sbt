lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.13.0"
    )),
    name := "claimcheck"
  )
enablePlugins(DockerComposePlugin)
enablePlugins(DockerPlugin)

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.3.0"
libraryDependencies += "ch.qos.logback"             % "logback-classic"                   % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"                    % "3.9.2"
libraryDependencies += "io.minio" % "minio"                    % "6.0.11"
// libraryDependencies += "software.amazon.awssdk" % "s3" % "2.8.7" <- minio does not support sdk 2.x yet
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.106"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0"
libraryDependencies += "commons-io" % "commons-io" % "2.6"

// filler libs, required by com.amazonaws.util.Md5Utils
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.2.11"
libraryDependencies +=  "com.sun.xml.bind" % "jaxb-core" % "2.2.11"
libraryDependencies +=  "com.sun.xml.bind" % "jaxb-impl" % "2.2.11"
libraryDependencies +=  "javax.activation" % "activation" % "1.1.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test
