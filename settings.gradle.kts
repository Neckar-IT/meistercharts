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
  id("org.kordamp.gradle.insight") version "0.54.0"

  id("de.fayard.refreshVersions") version "0.60.5"
}

rootProject.name = "meistercharts.com"
rootProject.buildFileName = "build.gradle.kts"

refreshVersions {
  extraArtifactVersionKeyRules(file("refreshVersions-extra-rules.txt"))
}

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

if (false) {
  include(":ksp:ksp-commons")
  include(":ksp:boxing-verifier")
  include(":ksp:ts-declaration-generator")
}


include(":meistercharts-core")

include(":meistercharts-history:meistercharts-history-core")
include(":meistercharts-history:meistercharts-history-api")

include(":meistercharts-canvas")

include(":meistercharts-api:meistercharts-easy-api")
