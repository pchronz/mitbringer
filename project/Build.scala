import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "breakingbad"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.typesafe" %% "play-plugins-mailer" % "2.0.4",
      "com.roundeights" % "hasher" % "0.3" from "http://cloud.github.com/downloads/Nycto/Hasher/hasher_2.9.1-0.3.jar", 
      "org.mindrot" % "jbcrypt" % "0.3m" 
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
      resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/" 
    )

}
