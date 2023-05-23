import java.time.Instant

description = "meistercharts.com"

plugins {
  id("org.jetbrains.kotlin.plugin.serialization") version "_" apply false
}

//The current build date
val buildDate: String by extra { Instant.now().toString() }
//The build day
val buildDateDay: String by extra { java.time.LocalDate.now().toString() }
//The git commit id
val gitCommit: String by extra { arrayOf("git", "rev-parse", "--short", "HEAD").getCmdResult(project.projectDir) }
//The date of the last git commit
val gitCommitDate: String by extra { arrayOf("git", "log", "-1", "--date=short", "--pretty=format:%cI").getCmdResult(project.projectDir) }
//The output of git describe
val gitDescribe: String by extra { arrayOf("git", "describe", "--tags", "--always").getCmdResult(project.projectDir) }

val branch: String = arrayOf("git", "rev-parse", "--abbrev-ref", "HEAD").getCmdResult(project.projectDir).let {
  try {
    if (it == "HEAD") {
      //If in detached HEAD, the next line finds the "best" branch name
      val cmdResult = arrayOf("git", "show", "-s", "--pretty=%D", "HEAD").getCmdResult(project.projectDir)
      println("Running in headless mode. Guessing branch names from possible values: <$cmdResult>")

      val candidates = cmdResult.splitToSequence(',').map { candidate ->
        candidate.trim()
      }.toList()

      if (candidates.isEmpty()) {
        return@let "unknown"
      }

      if (candidates.contains("main") || candidates.contains("origin/main")) {
        //If main, use this
        return@let "main"
      }
      if (candidates.contains("master") || candidates.contains("origin/master")) {
        //If master, use this
        return@let "master"
      }

      //Find the shortest element that is *not* HEAD
      candidates
        .filterNot { candidate ->
          candidate.startsWith("refs/") //skip pipeline refs like: "refs/pipeline/58705"
        }
        .sortedBy { candidate ->
          candidate.length
        }.firstOrNull { candidate ->
          candidate != "HEAD"
        } ?: "unknown"
    } else {
      it
    }
  } catch (e: Exception) {
    logger.warn("Could not guess branch name due to ${e.message}")
    "unknown"
  }
}.also {
  extra.set("branch", it)
}


//Configure the MeisterCharts version number
//
//During development the version number must always be a SNAPSHOT version
//Only increase the version number during releases. Do *NOT* merge the increased version number back to master
val meisterchartVersionBase: String = file("meistercharts.version").readText().trim()

val meisterchartsVersion: String by extra {
  if (meisterchartVersionBase.isSnapshot()) {
    "$meisterchartVersionBase-${gitCommit}"
  } else {
    meisterchartVersionBase
  }
}

version = meisterchartsVersion


//Print the version numbers on the console on every gradle run
println("------------------------------------------------------------")
println("Build variables:")
println("------------------------------------------------------------")
println("\tversion                $version")
println("\tmeisterchartsVersion   $meisterchartsVersion")
println("\tbuildDate              $buildDate")
println("\tbuildDateDay           $buildDateDay")
println("\tbranch                 $branch")
println("\tgitCommit              $gitCommit")
println("\tgitCommitDate          $gitCommitDate")
println("\tgitDescribe            $gitDescribe")
println("------------------------------------------------------------")

println("Java Runtime Environment: ")
println("------------------------------------------------------------")
println("Java Home: ${System.getProperty("java.home")}")
println("Java Version: ${System.getProperty("java.version")}")
println("Java Vendor: ${System.getProperty("java.vendor")}")


if (branch == "main") {
  //Never allow a non-development version on main
  if (!meisterchartsVersion.isSnapshot()) {
    throw InvalidUserDataException("Invalid meisterchart version set. Was <$meisterchartsVersion but must be a -SNAPSHOT version on master branch!")
  }
}


allprojects {
  version = meisterchartsVersion

  repositories {
    mavenCentral()
  }

  task("info") {
    doLast {
      println("|-------------------------------------------------")
      println("| MEISTERCHARTS.COM ------------------------------")
      println("|-------------------------------------------------")
      println("| ${project.group}:${project.name}")
      println("|-------------------------------------------------")
      println("| ${project.description}")
      println("|-------------------------------------------------")
    }
  }

  ////for common
  //extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinCommonProjectExtension>()?.applyKotlinConfiguration()
  //
  ////For JVM projects
  //extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>()?.applyKotlinConfiguration()
  //
  ////For JS projects
  //extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension>()?.applyKotlinConfiguration()
  //
  ////Opt in to experimental annotations - syntax for multi-platform projects
  //extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()?.applyKotlinConfiguration()
  //
  ////Configure the version numbers
  //configureNodeJsRootExtension()
}

subprojects {
  //TODO configure kotlin
}
