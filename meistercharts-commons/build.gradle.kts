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

kotlin {
  js {
    browser {
      testTask {
        useKarma {
          useChromeHeadlessNoSandbox()
        }
      }
    }
  }
  jvm()

  sourceSets {
    val versionInformationSources by creating {
      kotlin.srcDir(createVersionConstantsTasks)
    }

    val commonMain by getting {
      dependsOn(versionInformationSources)

      dependencies {
        implementation(Libs.kotlinx_coroutines_core)
        api(KotlinX.serialization.core)
        api(KotlinX.serialization.json)
        api(Libs.klock)
      }
    }

    commonTest {
      dependencies {
        implementation(Libs.kotlin_test_common)
        implementation(Libs.kotlin_test_annotations_common)
      }
    }

    named("jvmMain") {
      dependencies {
        api(Libs.jsr305)
        implementation(Libs.kotlinx_coroutines_core)
        //api(project(Projects.dependencies_sets_jvm_annotations))
        //api(project(Projects.dependencies_sets_jvm_kotlin))
        //api(project(Projects.open_annotations))
        //api(project(Projects.open_commons_time))
        api(Libs.slf4j_api)
        api(Libs.logback_classic)
        api(Libs.kotlin_reflect)
      }
    }

    named("jvmTest") {
      dependencies {
        //implementation(project(":meistercharts-test-commons"))
        implementation(Libs.kotlin_test)
        implementation(Libs.kotlin_test_junit)
        //implementation(project(Projects.dependencies_sets_jvm_kotlin_test))
        //implementation(project(Projects.open_commons_test_utils))
        //implementation(project(Projects.open_commons_javafx_test_utils))
        //implementation(project(Projects.open_commons_kotlinx_serialization_test_utils))
        //
        implementation(Libs.controlsfx)
        implementation(Libs.miglayout_javafx)
        implementation(Libs.commons_io)
        implementation(Libs.assertj_core)
        implementation(Libs.commons_math3)
        implementation(Libs.mockito_kotlin)
        implementation(Libs.logback_classic)
        implementation(Libs.awaitility)
        implementation(Libs.measured)
      }
    }

    named("jsMain") {
      dependencies {
        api(Libs.kotlin_js)
        //implementation(project(Projects.open_unit_unit))
      }
    }
    named("jsTest") {
      dependencies {
        //implementation(project(Projects.dependencies_sets_js_kotlin_test))
      }
    }
  }
}

configureKotlin()
configureToolchainJava8WithFx()
