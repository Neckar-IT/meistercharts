@file:Suppress("SpellCheckingInspection")

val kotlinVersion: String = "1.9.10"

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

dependencies {
  implementation("com.google.guava:guava:30.1.1-jre")
  implementation("commons-io:commons-io:_")
  implementation("org.apache.commons:commons-compress:_")
  implementation("org.apache.commons:commons-lang3:_")

  implementation("com.github.cretz.kastree:kastree-ast-jvm:_")
  implementation("com.github.cretz.kastree:kastree-ast-psi:_")
  implementation("io.gitlab.arturbosch.detekt:detekt-psi-utils:_")
  implementation("io.gitlab.arturbosch.detekt:detekt-parser:_")

  //Enforce version numbers for Kotlin - transitive dependencies
  implementation(kotlin("compiler-embeddable", "_"))
  implementation(Kotlin.stdlib.common)
  implementation(Kotlin.stdlib.jdk8)
  implementation(Kotlin.stdlib.jdk7)
  implementation(kotlin("reflect", "_"))

  implementation(kotlin("compiler-embeddable", "_"))
  implementation(kotlin("scripting-compiler-embeddable", "_"))
  implementation(kotlin("klib-commonizer-embeddable", "_"))

  implementation("com.google.devtools.ksp:symbol-processing-api:_")
  implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:_")

  implementation(KotlinX.serialization.json)

  implementation(kotlin("gradle-plugin", kotlinVersion))

  //Avoid Class not found exception related to JNA
  implementation("net.java.dev.jna:jna:_")

  testImplementation(Testing.junit.jupiter.api)
  testRuntimeOnly(Testing.junit.jupiter.engine)
  testImplementation("com.willowtreeapps.assertk:assertk-jvm:_")
}

gradlePlugin {
  plugins {
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
