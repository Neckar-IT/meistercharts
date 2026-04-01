pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

rootProject.name = "meistercharts.com"
rootProject.buildFileName = "build.gradle.kts"

include(":meistercharts-commons")
include(":meistercharts-test-commons")
include(":meistercharts-core")
include(":meistercharts-api:meistercharts-easy-api")
include(":meistercharts-history:meistercharts-history-core")
include(":meistercharts-canvas")
include(":meistercharts-history:meistercharts-history-api")

if (false) {
  include(":ksp:ksp-commons")
  include(":ksp:boxing-verifier")
  include(":ksp:ts-declaration-generator")
}
