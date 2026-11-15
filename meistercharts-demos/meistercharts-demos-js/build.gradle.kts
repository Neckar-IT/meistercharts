import it.neckar.projects.project

import it.neckar.projects.Projects

import it.neckar.gradle.addKotlinTestDependencies

import it.neckar.gradle.JsTargetType
import it.neckar.gradle.WebpackModuleType
import it.neckar.gradle.addAnnotationDependencies
import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.executableJsApplication
import it.neckar.gradle.configureWasmJsTarget

description = """Meistercharts-Demos JS"""

plugins {
  webResourcesFromDependencies
}

kotlin {
  js {
    executableJsApplication(
      varName = "meistercharts-demos-js",
      webpackModuleType = WebpackModuleType.Window,
      webpackModuleTypeForDev = WebpackModuleType.Window,
    )

    browser {
      runTask {
        devServerProperty = devServerProperty.get().copy(
          open = "/?all=true"
        )
      }
    }
  }

  configureWasmJsTarget(project, executable = true)
  addAnnotationDependencies(project)
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {

        implementation(project(Projects.open.meistercharts.meisterchartsDemos.meisterchartsDemosCommon))
      }
    }

    commonTest {
      dependencies {
      }
    }

    webMain {
      dependencies {
        implementation(libs.kotlinx.browser)
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
