import it.neckar.projects.project

import it.neckar.projects.Projects


import it.neckar.gradle.addKotlinTestDependencies

import it.neckar.gradle.addAnnotationDependencies
import it.neckar.gradle.addKotlinDependencies

description = """Meistercharts Demos"""

plugins {
  kotlinxSerialization
  tailwind
}

tailwind {
  input = layout.projectDirectory.file("src/webMain/tailwind/tailwind.input.css")
  output = layout.projectDirectory.file("src/webMain/resources/css/meistercharts-demos-tailwind.css")
}

kotlin {
  addAnnotationDependencies(project)
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {

        //implementation(libs.kotlinx.coroutines.core)
        api(project(Projects.open.units.unit))
        api(project(Projects.open.commons.random))
        api(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryCore))
        api(project(Projects.open.meistercharts.meisterchartsCanvas))
        api(project(Projects.open.meistercharts.meisterchartsHistory.meisterchartsHistoryCore))

        api(project(Projects.open.commons.csv))

        api(project(Projects.open.meistercharts.meisterchartsAdditionalCharts))

        api(project(Projects.open.meistercharts.meisterchartsData.meisterchartsDataClient))
        api(project(Projects.open.commons.money))
      }
    }

    commonTest {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
      }
    }

    webMain {
      dependencies {
        implementation(libs.kotlinx.browser)
        implementation(libs.kotlinx.html)
      }
    }

    jsMain {
      dependencies {
        implementation(libs.kotlinx.serialization.json)
        implementation(project(Projects.open.versionInfo))
      }
    }

    jsTest {
      dependencies {
        implementation(libs.kotlin.csv.js)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.kotlin.csv.jvm)
        api(libs.kotlinx.coroutines.core)
      }
    }

    jvmTest {
      dependencies {
        implementation(project(Projects.open.commons.testUtils))
        implementation(libs.commons.io)
      }
    }
  }
}
