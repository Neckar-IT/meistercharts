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

      object VersionConstants{
        const val monorepoVersion: String = "$version"
        const val buildDateDay: String = "$buildDateDay"
        const val branch: String = "$branch"
        const val gitDescribe: String = "$gitDescribe"
        const val gitCommit: String = "$gitCommit"
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
        implementation(Libs.kotlinx_coroutines_core)
        api(KotlinX.serialization.core)
        api(KotlinX.serialization.json)

        api(Libs.kotlin_reflect)
        api(Libs.commons_io)
      }
    }

    jvmMain {
      dependencies {
        api(Libs.jsr305)
        api(Libs.logback_classic)
        implementation(Libs.commons_lang3)
        api(Libs.bson_kotlinx)
        api(Libs.javax_inject)
        api(Libs.commons_codec)
        api(Libs.jackson_databind)
        api(Libs.jsonassert)

      }
    }



    jsMain {
      dependencies {
        api(Libs.kotlin_js)
      }
    }

  }
}
