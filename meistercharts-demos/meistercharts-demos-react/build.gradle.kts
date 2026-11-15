import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.addKotlinTestDependencies
import it.neckar.gradle.executableJsApplication
import it.neckar.projects.Projects
import it.neckar.projects.project

description = """Meistercharts-Demos React"""

kotlin {
  js {
    executableJsApplication(varName = "MeisterchartsDemosReact")
  }

  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.open.meistercharts.meisterchartsCanvas))
        api(project(Projects.open.meistercharts.meisterchartsReactKotlin))

        api(libs.kotlinx.serialization.json)
      }
    }

    jsMain {
      dependencies {
        api(project.dependencies.platform(libs.kotlin.wrappers.bom))

        api(libs.kotlin.react)
        api(libs.kotlinx.html)
        api(libs.kotlin.react.dom)
        api(libs.kotlin.styled.next)

        api(project(Projects.open.commons.kotlinJs))
        api(project(Projects.open.commons.kotlinReact))
        api(project(Projects.open.commons.kotlinReactRedux))
        api(project(Projects.open.commons.kotlinReactRouter))

        implementation(project(Projects.open.versionInfo))
      }
    }

    jvmTest {
      dependencies {
      }
    }
  }
}
