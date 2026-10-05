@file:Suppress("SpellCheckingInspection")

// A buildSrc build script gets no type-safe `libs.versions` accessor.
val applicationKotlinVersion: String =
  extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
    .named("libs").findVersion("kotlin").get().requiredVersion

plugins {
  `kotlin-dsl`
  `java-library`
  `java-gradle-plugin`
  idea
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

idea {
  module {
    isDownloadSources = true
  }
}

afterEvaluate {
  tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    compilerOptions {
      languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
      apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
      javaParameters = true
      jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
      freeCompilerArgs.add("-progressive")
      freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
      freeCompilerArgs.add("-Xcontext-parameters")
    }
  }
}


dependencies {
  // The type-safe accessors of `gradle/libs.versions.toml` (`LibrariesForLibs`) that the copied
  // Dependencies.kt reads; Gradle has no API for them in plugin code (gradle/gradle#15383).
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

  implementation(libs.guava)

  implementation(libs.commons.compress)
  implementation(libs.commons.lang3)

  implementation(libs.kotlinx.serialization.json)
  // The .env rules copied in by populateBuildSrc read the file system through kotlinx-io.
  implementation(libs.kotlinx.io.core)
  implementation(kotlin("gradle-plugin", applicationKotlinVersion))
  implementation(kotlin("serialization", applicationKotlinVersion))

  implementation(libs.jackson.core)
  implementation(libs.jackson.databind)
  implementation(libs.shadow.gradle.plugin)
  implementation(libs.node.gradle.plugin)
  implementation(libs.detekt.gradle.plugin)
  implementation(libs.kover.gradle.plugin)
  implementation(libs.symbol.processing.gradle.plugin)
  implementation(libs.javafx.plugin)

  testImplementation(libs.junit.jupiter.api)
}

gradlePlugin {
  plugins {
    register("NpmBundlePlugin") {
      id = "it.neckar.npm-bundle"
      implementationClass = "it.neckar.gradle.npmbundle.NpmBundlePlugin"
    }
  }
}

tasks.withType<Test>()
  .configureEach {
    useJUnitPlatform {
      includeEngines("junit-jupiter", "junit-vintage")
    }

    filter {
      includeTestsMatching("*Test")
      isFailOnNoMatchingTests = false
    }
  }
