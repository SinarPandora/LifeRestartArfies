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
    scalacOptions ++= compileOptions
  )

lazy val jsFacade: Project = (project in file("Facade/JavaScript"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "JavaScript_Facade",
    scalacOptions ++= compileOptions,
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
    scalacOptions ++= compileOptions,
    assembly / assemblyJarName := "repl.jar",
  )
  .dependsOn(storyGenerator)

lazy val excelConverter: Project = (project in file("ExcelConverter"))
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "Excel_Converter",
    scalacOptions ++= compileOptions,
    assembly / assemblyJarName := "excel-cvt.jar",
    libraryDependencies ++= Seq(
      "me.tongfei" % "progressbar" % "0.9.3",
      "org.apache.poi" % "poi" % "5.2.2",
      "org.json4s" %% "json4s-jackson" % "4.0.5"
    )
  )
  .dependsOn(storyGenerator)

lazy val compileOptions: Seq[String] = Seq(
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
