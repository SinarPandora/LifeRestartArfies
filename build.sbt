ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root: Project = (project in file("."))
  .settings(
    name := "Life_Restart_Arfies"
  )
  .aggregate(
    storyGenerator,
    jsFacade,
    jvmFacade
  )

lazy val storyGenerator: Project = (project in file("StoryGenerator"))
  .settings(
    name := "Story_Generator",
    scalacOptions ++= Seq(
      "-Xsource:3",
      "-unchecked",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-deprecation",
      "-encoding",
      "utf8"
    )
  )

lazy val jsFacade: Project = (project in file("Facade/JavaScript"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "JavaScript_Facade",
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.1.0"
    )
  )
  .dependsOn(storyGenerator)

lazy val jvmFacade: Project = (project in file("Facade/JVM"))
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "JVM_Facade",
    assembly / assemblyJarName := "repl.jar",
  )
  .dependsOn(storyGenerator)

