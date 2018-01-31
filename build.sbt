name := "newsrecorder"
 
version := "1.0" 
      
lazy val `newsrecorder` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "org.xerial" % "sqlite-jdbc" % "3.21.0.1"
)

// https://mvnrepository.com/artifact/com.nrinaudo/kantan.xpath
libraryDependencies += "com.nrinaudo" %% "kantan.xpath" % "0.3.2"

//AWS
libraryDependencies ++= Seq(
  "com.gu" %% "scanamo" % "1.0.0-M2" exclude("commons-logging","commons-logging"),
  "org.typelevel" %% "cats-free" % "1.0.1"
)

//logging
libraryDependencies ++= Seq(
  // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  // https://mvnrepository.com/artifact/ch.qos.logback/logback-core
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.25"

)

val circeVersion = "0.9.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      