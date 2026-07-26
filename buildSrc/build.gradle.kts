@file:Suppress("SpellCheckingInspection")

val kotlinVersion: String = "2.4.10"

plugins {
  openModule
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
  implementation(libs.guava)

  implementation(libs.commons.compress)
  implementation(libs.commons.lang3)

  implementation(libs.kotlinx.serialization.json)
  implementation(kotlin("gradle-plugin", kotlinVersion))
  implementation(kotlin("serialization", kotlinVersion))

  implementation(libs.jackson.core)
  implementation(libs.jackson.databind)
  implementation("com.gradleup.shadow:shadow-gradle-plugin:9.6.1")
  implementation("com.github.node-gradle:gradle-node-plugin:7.1.0")
  implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
  implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.9.9")
  implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.10")
  implementation("org.openjfx:javafx-plugin:0.1.0")

  testImplementation(libs.junit.jupiter.api)
}

gradlePlugin {
  plugins {
    register("GenerateIconsPlugin") {
      id = "it.neckar.generate-icons"
      implementationClass = "it.neckar.gradle.icons.GenerateIconsPlugin"
    }

    register("GenerateTypeScriptDefinitionsPlugin") {
      id = "it.neckar.generate-ts-declaration"
      implementationClass = "it.neckar.gradle.tsdefinition.GenerateTypeScriptDefinitionsPlugin"
    }
    register("NpmBundlePlugin") {
      id = "it.neckar.npm-bundle"
      implementationClass = "it.neckar.gradle.npmbundle.NpmBundlePlugin"
    }
    register("TypescriptDefinitionGenerationPlugin") {
      id = "it.neckar.ksp.generating.ts-plugin"
      implementationClass = "it.neckar.gradle.kps.generating.ts.TypescriptDefinitionGenerationPlugin"
    }
    register("GeneratePackageJsonPlugin") {
      id = "it.neckar.repos.generate-package-json"
      implementationClass = "it.neckar.gradle.pnpm.packagejson.GeneratePackageJsonPlugin"
    }
    register("InstallPnpmDependencyPlugin") {
      id = "it.neckar.repos.install-pnpm-dependency"
      implementationClass = "it.neckar.gradle.packagejson.InstallPnpmDependencyPlugin"
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
