import it.neckar.gradle.formatAsMegaBytes

import it.neckar.projects.project

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

import it.neckar.projects.Projects

import it.neckar.gradle.Scope
import it.neckar.gradle.addKotlinTestDependencies

import it.neckar.gradle.addAnnotationDependencies
import it.neckar.gradle.addKtorClientDependencies
import it.neckar.gradle.addKtorServerDependencies

description = """Charting - Data Server (REST)"""

plugins {
  `java-library`
  kotlinJvm
  shadow
  kotlinxSerialization
}

dependencies {
  addAnnotationDependencies(project)

  api(project(Projects.open.rest.ktorServer))
  api(project(Projects.open.rest.openapiGenerator))
  api(project(Projects.open.commons.disposable))
  api(libs.ktor.http)
  api(libs.kotlinx.serialization.json)

  implementation(project(Projects.open.commons.kotlinLang))
  implementation(project(Projects.open.commons.repeating))
  implementation(project(Projects.open.commons.time))
  implementation(project(Projects.open.commons.app))
  implementation(project(Projects.open.commons.logging))
  implementation(project(Projects.open.commons.kotlinAsync))
  implementation(project(Projects.open.commons.uuid))
  implementation(project(Projects.open.commons.version))
  implementation(project(Projects.open.units.unit))
  implementation(project(Projects.open.rest.model))
  implementation(project(Projects.open.meistercharts.meisterchartsCore))
  implementation(project(Projects.open.meistercharts.meisterchartsVersionInfo))
  implementation(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryCore))
  implementation(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryApi))
  implementation(project(Projects.open.meistercharts.meisterchartsData.meisterchartsDataApi))

  implementation(libs.clikt.core)
  implementation(libs.ktor.server.cors)
  implementation(libs.ktor.server.websockets)
  implementation(libs.ktor.utils)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.guice)
  implementation(libs.slf4j.api)

  runtimeOnly(libs.logback.classic)

  addKtorServerDependencies(project, Scope.Test)
  addKotlinTestDependencies(project)
  testImplementation(project(Projects.open.commons.testUtils))
  testImplementation(project(Projects.open.rest.ktorClient))
  testImplementation(project(Projects.open.meistercharts.meisterchartsData.meisterchartsDataClient))
  testImplementation(project(Projects.open.rest.ktorServerTestUtils))
  testImplementation(project(Projects.open.commons.i18n))
  testImplementation(project(Projects.open.commons.kotlinCollections))

  addKtorClientDependencies(project, Scope.Test)
}

tasks.named<ShadowJar>("shadowJar") {
  archiveBaseName = "dataServer"

  // Shadow 9.x requires INCLUDE for mergeServiceFiles() to work (EXCLUDE skips duplicates before merging)
  duplicatesStrategy = DuplicatesStrategy.INCLUDE

  manifest {
    attributes(mapOf("Main-Class" to "com.meistercharts.server.DataServerKt"))
  }

  mergeServiceFiles()

  doLast {
    val fatJar = archiveFile.get().asFile
    println("Minimized jar created: ${fatJar.path} (${fatJar.length().formatAsMegaBytes()} MB)")
  }
}

tasks.register<Zip>("package") {
  description = "Creates a zip files that contains everything that is required to run NECKAR.IT Data Server"

  dependsOn.add("shadowJar")
  archiveFileName = "data-server-${project.version}.zip"

  from(project.layout.buildDirectory.dir("libs")) {
    include("*-all.jar")
    rename {
      "dataServer-all.jar"
    }
  }

  from("src/main/assembly") {
    include("logback.xml")
    include("*.sh")
  }

  into("dataServer-${project.version}")
}
