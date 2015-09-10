
name := "geoip"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra" % "1.5.3",
  "com.github.philcali" %% "cronish" % "0.1.3"
)

resolvers +=
  "Twitter" at "http://maven.twttr.com"

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case other => MergeStrategy.defaultMergeStrategy(other)
}

val stageTask = TaskKey[Unit]("stage", "Copies assembly jar to staging location")
stageTask <<= assembly map { (asm) =>
  val src = asm.getPath
  val dst = "build/geoip.jar"
  println(s"Copying: $src -> $dst")
  Seq("cp", src, dst) !!
}
