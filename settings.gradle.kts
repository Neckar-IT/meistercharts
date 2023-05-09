import Http4k.format

pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}


//Bootstrap refresh versions early (before plugins)

plugins {
  //Prints the Maven like output at the end of the build
  //https://kordamp.org/kordamp-gradle-plugins/#_org_kordamp_gradle_insight
  id("org.kordamp.gradle.insight") version "0.51.0"

  id("de.fayard.refreshVersions") version "0.40.2"
  ////                          # available:"0.50.0"
  ////                          # available:"0.50.1"
  ////                          # available:"0.50.2"
  ////                          # available:"0.51.0"
}

rootProject.name = "meistercharts.com"
rootProject.buildFileName = "build.gradle.kts"


configure<org.kordamp.gradle.plugin.insight.InsightExtension> {
  enabled.set(true)

  report(org.kordamp.gradle.plugin.insight.reports.SummaryBuildReport::class.java) {
    format.set("long")
    zeroPadding.set(true)
    maxProjectPathSize.set(80)
  }
}


val dirName = "meistercharts.com-gradle"

include(":meistercharts-commons")
include(":meistercharts-core")

include(":meistercharts-history:meistercharts-history-core")
include(":meistercharts-history:meistercharts-history-api")

include(":meistercharts-canvas")

include(":meistercharts-api:meistercharts-easy-api")
