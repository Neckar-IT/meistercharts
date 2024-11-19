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
        implementation(Libs.threeten_extra)
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




