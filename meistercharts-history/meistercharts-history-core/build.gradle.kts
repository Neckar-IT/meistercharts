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
      }
    }

    commonTest {
      dependencies {
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.threeten.extra)
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
      }
    }
  }
}




