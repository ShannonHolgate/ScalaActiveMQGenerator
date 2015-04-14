import com.github.retronym.SbtOneJar._

oneJarSettings

libraryDependencies += "org.apache.activemq" % "activemq-core" % "5.7.0"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

name := "MessageGenerator"

version := "1.0"

scalaVersion := "2.11.6"

