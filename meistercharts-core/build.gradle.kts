description = """Meistercharts - Core"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}


kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.meistercharts_commons))
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
        implementation(Libs.measured)
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
