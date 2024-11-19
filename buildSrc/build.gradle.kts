@file:Suppress("SpellCheckingInspection")

val kotlinVersion: String = "2.1.0-Beta2"

plugins {
  `kotlin-dsl`
  `java-library`
  `java-gradle-plugin`
  idea
  //kotlin("plugin.serialization")
}

repositories {
  mavenCentral()
}

idea {
  //Add target dir to exclude dirs
  module {
    isDownloadSources = true
  }
}

// Must be called within afterEvaluate to overwrite settings from the `kotlin-dsl` plugin
// https://handstandsam.com/2022/04/13/using-the-kotlin-dsl-gradle-plugin-forces-kotlin-1-4-compatibility/
// Currently the free compiler args are *not* supported: //https://github.com/gradle/gradle/issues/24221
afterEvaluate {
  tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    kotlinOptions {
      languageVersion = "2.1"
      apiVersion = "2.1"
      javaParameters = true
      jvmTarget = "21"
      options.freeCompilerArgs.add("-progressive")
      options.freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
    }
  }
}


dependencies {
  implementation("com.google.guava:guava:_")

  implementation("org.apache.commons:commons-compress:_")
  implementation("org.apache.commons:commons-lang3:_")

  implementation(KotlinX.serialization.json)
  implementation(kotlin("gradle-plugin", kotlinVersion))

  implementation("com.fasterxml.jackson.core:jackson-core:_")
  implementation("com.fasterxml.jackson.core:jackson-databind:_")
  implementation("com.github.jengelman.gradle.plugins:shadow:_")
  implementation("com.github.node-gradle:gradle-node-plugin:_")
  implementation("io.gitlab.arturbosch.detekt:io.gitlab.arturbosch.detekt.gradle.plugin:_")

  testImplementation(Testing.junit.jupiter.api)
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
    register("KspVerifyBoxingPlugin") {
      id = "it.neckar.ksp.boxing-plugin"
      implementationClass = "it.neckar.gradle.kps.boxing.KpsBoxingPlugin"
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

