description = """Meistercharts - Core"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}


kotlin {
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.meistercharts_commons))
        api(project(Projects.meistercharts_test_commons))
      }
    }

    jvmTest {
      dependencies {
        implementation(libs.measured)
      }
    }
  }
}
