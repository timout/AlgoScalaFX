name := "AlgoScalaFX"

version := "0.1"

scalaVersion := "2.12.10"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8")

val javafxModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

libraryDependencies ++= Seq(
  "org.scalafx" % "scalafx_2.12" %  "12.0.2-R18" //"11-R16"
)

libraryDependencies ++= javafxModules.map(m => "org.openjfx" % s"javafx-$m" % "11.0.2" classifier osName)

fork := true
