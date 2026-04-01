description = """Meistercharts - Canvas"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}

kotlin {
  addKotlinTestDependencies(project)
  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.meistercharts_core))
        api(project(Projects.meistercharts_history_core))
        api(project(Projects.meistercharts_test_commons))

      }
    }

    commonTest {
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

    jsMain {
      dependencies {
      }
    }
    jsTest {
      dependencies {
        implementation(libs.kotlinx.html)
      }
    }
  }
}
