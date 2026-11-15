import it.neckar.projects.project

import it.neckar.projects.Projects

import it.neckar.gradle.addKotlinTestDependencies

import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.executableJsApplication

description = "Meistercharts React"

plugins {
}

kotlin {
  js {
    useEsModules()
    executableJsApplication(varName = "MeisterchartsReact")
  }

  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.open.meistercharts.meisterchartsCanvas))

        implementation(project(Projects.open.commons.kotlinJs))
      }
    }

    commonTest {
      dependencies {
      }
    }

    jsMain {
      dependencies {
        implementation(project.dependencies.platform(libs.kotlin.wrappers.bom))
        implementation(libs.kotlin.react)
        implementation(libs.kotlin.react.dom)
        implementation(libs.kotlin.styled.next)
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


