import it.neckar.projects.project

import it.neckar.projects.Projects

import it.neckar.gradle.addKotlinTestDependencies

description = """Meistercharts - Core"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}


kotlin {
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.meistercharts_commons))
        api(project(Projects.meistercharts_test_commons))
      }
    }

    jvmTest {
      dependencies {
        implementation(libs.measured)
      }
    }
  }
}
