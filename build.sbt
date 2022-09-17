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

lazy val parserCommon: Project = (project in file("Parser/Common"))
  .settings(
    name := "Parser_Common",
    scalacOptions ++= compileOptions
  )
  .dependsOn(storyGenerator)

lazy val condParser: Project = (project in file("Parser/ConditionParser"))
  .settings(
    name := "Condition_Parser",
    scalacOptions ++= compileOptions,
    libraryDependencies ++= jvmTest.value
  )
  .dependsOn(storyGenerator, parserCommon)

lazy val excelConverter: Project = (project in file("ExcelConverter"))
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "Excel_Converter",
    scalacOptions ++= compileOptions,
    assembly / assemblyJarName := "excel-cvt.jar",
    libraryDependencies ++= Seq(
      "org.apache.poi" % "poi" % "5.2.2",
      "org.apache.poi" % "poi-ooxml" % "5.2.2",
      "me.tongfei" % "progressbar" % "0.9.3",
      "org.json4s" %% "json4s-jackson" % "4.0.5",
      "com.typesafe" % "config" % "1.4.2"
    )
  )
  .dependsOn(storyGenerator, condParser)

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

lazy val jvmTest: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
  "org.scalactic" %% "scalactic" % "3.2.13",
  "org.scalatest" %% "scalatest" % "3.2.13" % "test"
))

lazy val jsTest: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
  "org.scalactic" %%% "scalactic" % "3.2.13",
  "org.scalatest" %%% "scalatest" % "3.2.13" % "test"
))
