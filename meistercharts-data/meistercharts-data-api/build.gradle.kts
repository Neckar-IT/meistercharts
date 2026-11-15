import it.neckar.projects.project

import it.neckar.projects.Projects

import it.neckar.gradle.addKotlinTestDependencies

import it.neckar.gradle.addKotlinDependencies

description = """Charting - Data API (REST)"""

plugins {
  kotlinxSerialization
  kspSerialization
}

kotlin {
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.open.annotations))

        api(project(Projects.open.commons.uuid))
        api(project(Projects.open.units.unit))
        api(project(Projects.open.commons.version))
        api(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryCore))
      }
    }

    commonTest {
      dependencies {
      }
    }

    jsMain {
      dependencies {
      }
    }

    jsTest {
      dependencies {
      }
    }

    jvmMain {
      dependencies {
      }
    }

    jvmTest {
      dependencies {
      }
    }
  }
}
