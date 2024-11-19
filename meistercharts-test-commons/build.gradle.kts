description = """Meistercharts - Test - Commons"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}


repositories {
  mavenCentral()
}

kotlin {
  addKotlinTestDependencies(Scope.Main)
  sourceSets {
    commonMain {
      dependencies {
        implementation(project(Projects.meistercharts_commons))
      }
    }

    jvmMain {
      dependencies {
        implementation(Libs.commons_lang3)

      }
    }

  }
}
