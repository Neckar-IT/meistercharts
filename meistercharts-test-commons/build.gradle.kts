import it.neckar.projects.project

import it.neckar.projects.Projects

import it.neckar.gradle.Scope
import it.neckar.gradle.addKotlinTestDependencies

description = """Meistercharts - Test - Commons"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}


repositories {
  mavenCentral()
}

kotlin {
  addKotlinTestDependencies(project, Scope.Main)
  sourceSets {
    commonMain {
      dependencies {
        implementation(project(Projects.meistercharts_commons))
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.commons.lang3)
        implementation(libs.logback.classic)
        implementation(libs.jackson.module.kotlin)
        implementation(libs.jackson.datatype.jdk8)
        implementation(libs.jackson.datatype.jsr310)
        implementation(libs.jackson.module.parameter.names)
      }
    }

  }
}
