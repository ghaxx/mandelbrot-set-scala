name := "Mandelbrot Set"
version := "1.0"

scalaVersion := "2.13.5"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

Compile / mainClass := Some("ghx.simulation.life.SimulationApp")

libraryDependencies ++= {
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3",
    "com.typesafe" % "config" % "1.4.1",
    "org.influxdb" % "influxdb-java" % "2.21",
    "io.kamon" %% "kamon-bundle" % "2.1.20",
    "io.kamon" %% "kamon-influxdb" % "2.1.20",
    //    "org.specs2"          %%  "specs2-core"     % "2.3.11" % "test"
  )
}
// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add JavaFX dependencies
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(m =>
  "org.openjfx" % s"javafx-$m" % "11" classifier osName
)

javaOptions in run ++= Seq(
  "--module-path", Properties.javaFxPath,
  "--add-modules=" + javaFXModules.map("javafx." + _).mkString(","))

fork := true
