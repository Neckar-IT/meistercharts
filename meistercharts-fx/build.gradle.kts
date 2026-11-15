import it.neckar.projects.project

import it.neckar.projects.Projects

import it.neckar.gradle.addKotlinTestDependencies

import it.neckar.gradle.addAnnotationDependencies
import it.neckar.gradle.configureJavaFX

description = """Charting FX"""


plugins {
  `java-library`
  kotlinJvm

  shadow

  launch4j
  download

  if (false) {
    //Running into some compiler problems related to cache (2024-03-29)
    kspBoxing
  }
}

configureJavaFX()

dependencies {
  addAnnotationDependencies(project)

  api(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryCore))
  api(project(Projects.open.meistercharts.meisterchartsCanvas))
  api(project(Projects.open.meistercharts.meisterchartsCore))
  api(project(Projects.open.commons.disposable))
  api(project(Projects.open.commons.geometry))
  api(project(Projects.open.commons.i18n))
  api(project(Projects.open.commons.kotlinAsync))
  api(project(Projects.open.commons.kotlinEvents))
  api(project(Projects.open.commons.kotlinLang))
  api(project(Projects.open.commons.observable))
  api(libs.guava)
  api(libs.javafx.base)
  api(libs.javafx.graphics)
  api(libs.javafx.controls)

  implementation(project(Projects.open.commons.javafx))
  implementation(project(Projects.open.commons.memory))
  implementation(project(Projects.open.units.unit))
  implementation(project(Projects.open.commons.kotlinCollections))
  implementation(project(Projects.open.commons.logging))
  implementation(libs.slf4j.api)
  implementation(libs.batik.transcoder)

  runtimeOnly(libs.batik.codec)
  runtimeOnly(libs.kotlinx.coroutines.javafx)

  addKotlinTestDependencies(project)
  testImplementation(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryCore))
  testImplementation(project(Projects.open.commons.testUtils))
  testImplementation(project(Projects.open.commons.javafxTestUtils))
  testImplementation(project(Projects.open.commons.disposable))
  testImplementation(project(Projects.open.commons.geometry))
  testImplementation(project(Projects.open.commons.i18n))
  testImplementation(project(Projects.open.commons.kotlinAsync))
  testImplementation(project(Projects.open.commons.kotlinLang))
  testImplementation(project(Projects.open.commons.observable))
  testImplementation(project(Projects.open.commons.time))
  testImplementation(project(Projects.open.meistercharts.meisterchartsCore))
  testImplementation(project(Projects.open.meistercharts.meisterchartsVersionInfo))
  testImplementation(libs.guava)
  testImplementation(libs.javafx.graphics)
  testImplementation(libs.testfx.junit5)

  runtimeOnly(libs.logback.classic)
}

launch4j {
  icon = ""
}
