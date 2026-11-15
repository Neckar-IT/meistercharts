import it.neckar.gradle.addAnnotationDependencies
import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.addKotlinTestDependencies

plugins {
}

val neckarOpenVersion: String by project
val meisterchartsVersion: String by project

description = """Meistercharts - Version-Info"""

val createVersionConstantsTasks: TaskProvider<Task> = tasks.register("createVersionConstants") {
  group = "Build"
  description = "Create the version constants file"

  val currentMeisterchartsVersion: String = meisterchartsVersion

  //Define inputs/outputs to support incremental builds
  inputs.property("meisterchartsVersion", currentMeisterchartsVersion)

  val generatedSourcesDir = layout.buildDirectory.dir("generated/sources/$name/main/kotlin")
  val targetFileProvider = layout.buildDirectory.file("generated/sources/$name/main/kotlin/versionInfo/MeisterChartsVersionConstants.kt")
  outputs.dir(generatedSourcesDir)

  doLast {
    val targetFile = targetFileProvider.get().asFile
    targetFile.parentFile.mkdirs()

    targetFile.writeText(
      """
      package com.meistercharts.version

      object MeisterChartsVersionConstants{
        val version: String = "$currentMeisterchartsVersion"
      }
      """.trimIndent()
    )

    logger.info("Wrote version info to : ${targetFile.absolutePath}")
  }
}
kotlin {
  addAnnotationDependencies(project)
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        kotlin.srcDir(createVersionConstantsTasks)

        api("it.neckar.open:version-info:$neckarOpenVersion")
      }
    }

    commonTest {
      dependencies {
      }
    }

    jsMain {
      dependencies {
      }
    }

    jsTest {
      dependencies {
      }
    }

    jvmMain {
      dependencies {
      }
    }

    jvmTest {
      dependencies {
      }
    }
  }
}
