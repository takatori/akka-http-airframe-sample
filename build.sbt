lazy val akkaHttpVersion = "10.1.5"
lazy val akkaVersion     = "2.5.18"
lazy val slickVersion    = "3.3.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.7"
    )),
    name := "akka-http-airframe-sample",
    libraryDependencies ++= Seq(
      "org.wvlet.airframe" %% "airframe" % "0.76",

      "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"           % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,

      "com.typesafe.slick" %% "slick"          % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,

      "mysql" % "mysql-connector-java" % "6.0.6",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.h2database" % "h2" % "1.4.196",

      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test
    )
  )
