description = """Meistercharts - Core"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.meistercharts_core))
        api(project(Projects.meistercharts_history_core))
      }
    }

    commonTest {
      dependencies {
      }
    }

    jvmMain {
      dependencies {
        implementation(Libs.commons_compress)
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
        implementation(Libs.kotlin_test)
      }
    }
  }
}
