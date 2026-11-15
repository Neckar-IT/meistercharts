import it.neckar.projects.project

import it.neckar.projects.Projects

import it.neckar.gradle.Scope
import it.neckar.gradle.addKotlinTestDependencies

import it.neckar.gradle.addAnnotationDependencies
import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.addKtorClientDependencies
plugins {
}

description = """Charting - Data Client - JS Client for Data Server"""

kotlin {
  addAnnotationDependencies(project)
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)
  addKtorClientDependencies(project, Scope.Main)

  sourceSets {
    commonMain {
      dependencies {
        api(libs.kotlinx.serialization.json)
        api(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryCore))
        api(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryApi))
        api(project(Projects.open.meistercharts.meisterchartsData.meisterchartsDataApi))

        api(project(Projects.open.rest.ktorClient))

        api(libs.kotlinx.serialization.core)

        api(project(Projects.open.commons.concurrent))
        //implementation(project(Projects.open.commons.kotlinReactRedux))
        //implementation(project(Projects.open.commons.kotlinReactRouter))

        api(project(Projects.open.commons.kotlinxSerializers))
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
        //api(libs.kotlinx.coroutines.javafx)
      }
    }

    jvmTest {
      dependencies {
        //implementation(project(Projects.open.commons.javafxTestUtils))
        implementation(project(Projects.open.rest.ktorServerTestUtils))
      }
    }
  }
}
