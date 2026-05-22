import it.neckar.gradle.console
import java.time.Instant

import it.neckar.gradle.DevContainerInformation
import it.neckar.gradle.GitlabCiInformation
import it.neckar.gradle.GradleContext
import it.neckar.gradle.JvmType
import it.neckar.gradle.Plugins
import it.neckar.gradle.ProjectConfiguration
import it.neckar.projects.Projects
import it.neckar.gradle.branch
import it.neckar.gradle.buildDate

import it.neckar.gradle.ciInformation
import it.neckar.gradle.devContainerInformation
import it.neckar.gradle.inIde
import it.neckar.gradle.meisterchartsVersion

description = "meistercharts.com"

GradleContext.initialize(gradle)


plugins {
  openModule
  kotlinxSerialization apply false
  detekt apply false
}

//Prepare the extra variables

//These variables can be used in projects. They are defined as extension vals on Project in buildSrc/src/main/kotlin/Utils.kt
val inIde: Boolean by extra(System.getProperty("idea.version") != null)
val devContainerInformation by extra(DevContainerInformation.create())
val ciInformation: GitlabCiInformation by extra(GitlabCiInformation.create())


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
  if (meisterchartsVersion.isSnapshot().not()) {
    throw InvalidUserDataException("Invalid meisterchart version set. Was <$meisterchartsVersion but must be a -SNAPSHOT version on main branch!")
  }
}


allprojects {
  version = meisterchartsVersion

  repositories {
    mavenCentral()
  }

  tasks.register("info") {
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

  /**
   * Prints all configured plugins
   */
  tasks.register("plugins") {
    group = "Documentation"
    description = "Prints all configured plugins"

    doLast {
      logger.lifecycle("")
      logger.lifecycle("---------------------------------")
      logger.lifecycle("Plugins for ${console.green(project.path)}:")
      logger.lifecycle("---------------------------------")
      project.plugins.forEach {
        logger.lifecycle("Implementation: " + console.yellow(it::class.java.name))
      }
    }
  }

  tasks.register("configurations") {
    description = "Prints all configurations"
    group = "Help"

    doLast {
      logger.lifecycle("--------------------------------------------------------------")
      logger.lifecycle("${"Configuration".padEnd(45)} Resolvable Consumable")
      logger.lifecycle("--------------------------------------------------------------")
      configurations.forEach {
        val resolvableSuffix = if (it.isCanBeResolved) "+" else "-"
        val consumableSuffix = if (it.isCanBeConsumed) "+" else "-"
        println("${it.name.padEnd(45)} $resolvableSuffix          $consumableSuffix")
      }
    }
  }

}


configure(Projects.multiPlatformProjectsLTS()) {
  if (this.enabled) {
    logger.debug("Configuring multi-platform LTS project: ${this.path}")
    ProjectConfiguration.configureMultiPlatform(this.getProject(project), JvmType.JavaLatestLTS)
  }
}
