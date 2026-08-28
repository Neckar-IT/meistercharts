import it.neckar.gradle.branch
import it.neckar.gradle.buildDate

import it.neckar.gradle.meisterchartsVersion

description = """Meistercharts - Commons"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}

val createVersionConstantsTasks: Task = task("createVersionConstants") {
  group = "Build"
  description = "Create the version constants file"

  //Define inputs/outputs to support incremental builds
  inputs.property("meisterchartsVersion", meisterchartsVersion)
  inputs.property("version", version)
  inputs.property("branch", branch)
  inputs.property("gitDescribe", gitDescribe)
  inputs.property("gitCommit", gitCommit)
  inputs.property("gitCommitDate", gitCommitDate)
  inputs.property("buildDateDay", buildDateDay)

  val generatedSourcesDir = layout.buildDirectory.dir("generated/sources/$name/main/kotlin")
  val versionConstantsTargetFileProvider = layout.buildDirectory.file("generated/sources/$name/main/kotlin/versionInfo/VersionConstants.kt")
  val meisterChartsVersionTargetFileProvider = layout.buildDirectory.file("generated/sources/$name/main/kotlin/versionInfo/MeisterChartsVersionConstants.kt")

  outputs.dir(generatedSourcesDir)

  doLast {
    val meisterChartsTargetFile = meisterChartsVersionTargetFileProvider.get().asFile
    meisterChartsTargetFile.parentFile.mkdirs()

    meisterChartsTargetFile.writeText(
      """
      package com.meistercharts.version

      object MeisterChartsVersionConstants{
        val version: String = "$meisterchartsVersion"
      }
      """.trimIndent()
    )

    println("Wrote version info to : ${meisterChartsTargetFile.absolutePath}")

    val versionConstantsTargetFile = versionConstantsTargetFileProvider.get().asFile
    versionConstantsTargetFile.parentFile.mkdirs()

    versionConstantsTargetFile.writeText(
      """
      package it.neckar.open.version

      object VersionConstants {
        val monorepoVersion: String = "$version"
        val buildDate: String = "$buildDateDay"
        val buildDateDay: String = "$buildDateDay"
        val branch: String = "$branch"
        val gitDescribe: String = "$gitDescribe"
        val gitCommit: String = "$gitCommit"
      }

      enum class GitProperty(val propertyKey: String) {
        Hash("git.hash"),
        HashShort("git.hash.short"),
        Branch("branch"),
        CommitDateTime("git.commit.date.time"),
      }
      """.trimIndent()
    )

    println("Wrote version info to : ${versionConstantsTargetFile.absolutePath}")
  }
}

repositories {
  mavenCentral()
}

kotlin {
  sourceSets {
    commonMain {
      kotlin.srcDir(createVersionConstantsTasks)

      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
        api(libs.kotlinx.serialization.core)
        api(libs.kotlinx.serialization.json)

        api(libs.kotlin.reflect)
        api(libs.commons.io)
      }
    }

    jvmMain {
      // Local compat sources replacing excluded logback files (LogbackExt without Loki dependency)
      kotlin.srcDir("src-compat/jvmMain/kotlin")

      dependencies {
        api(libs.jsr305)
        api(libs.logback.classic)
        implementation(libs.commons.lang3)
        api(libs.bson.kotlinx)
        api(libs.javax.inject)
        api(libs.commons.codec)
        api(libs.jackson.databind)
        api(libs.jsonassert)

      }
    }



    jsMain {
      dependencies {
        api(libs.kotlin.js)
      }
    }

  }
}
