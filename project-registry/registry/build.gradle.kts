// The registry of a build rooted at internal/open/meistercharts, and the settings plugin of that build. The monorepo and the build
// rooted at internal/open read `meistercharts-projects` alone and never put this module on a classpath.
plugins {
  `kotlin-dsl`
}

dependencies {
  api(project(":meistercharts-projects"))
  // The settings classpath is the parent of every build script classpath: the build scripts under internal/open/meistercharts apply
  // the convention plugins by id, without a version.
  api("it.neckar.buildplatform:conventions")

  testImplementation(libs.kotlin.test.junit5)
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
  testImplementation(libs.assertk.jvm)
}

gradlePlugin {
  plugins {
    register("MeisterchartsSettingsPlugin") {
      id = "it.neckar.open.meistercharts.settings"
      implementationClass = "it.neckar.projects.MeisterchartsSettingsPlugin"
    }
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform {
    includeEngines("junit-jupiter")
  }

  filter {
    includeTestsMatching("*Test")
    isFailOnNoMatchingTests = true
  }
}
