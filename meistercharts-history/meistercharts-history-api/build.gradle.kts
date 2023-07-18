description = """Meistercharts - Core"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
}

kotlin{
  js {
    browser {
      testTask {
        useKarma {
          useChromeHeadlessNoSandbox()
        }
      }

    }
  }
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.meistercharts_core))
        api(project(Projects.meistercharts_history_core))

        implementation(Libs.kotlinx_coroutines_core)
        //api(project(meistercharts_version_info))
        api(KotlinX.serialization.core)
        api(Libs.klock)
      }
    }

    commonTest {
      dependencies {
        implementation(Libs.kotlin_test_common)
        implementation(Libs.kotlin_test_annotations_common)
      }
    }

    jvm().compilations["main"].defaultSourceSet {
      dependencies {
        implementation(Libs.kotlinx_coroutines_core)
        implementation(Libs.commons_compress)

        //api(project(Projects.dependencies_sets_jvm_annotations))
        //api(project(Projects.dependencies_sets_jvm_kotlin))
        //api(project(Projects.open_annotations))
        //api(project(Projects.open_commons_time))
      }
    }

    val jvmTestsCommons by creating {
      kotlin.srcDir(rootProject.file("meistercharts-test-commons/src/main/kotlin"))
    }

    jvm().compilations["test"].defaultSourceSet {
      dependsOn(jvmTestsCommons)

      dependencies {
        implementation(Libs.kotlin_test)
        implementation(Libs.kotlin_test_junit)

        implementation(Libs.jackson_databind)

        implementation(Libs.commons_io)
        implementation(Libs.commons_lang3)
        implementation(Libs.assertj_core)
        implementation(Libs.commons_math3)
        implementation(Libs.mockk)
        implementation(Libs.logback_classic)
        implementation(Libs.awaitility)
        implementation(Libs.measured)

        implementation(Libs.kotlin_reflect)
        implementation(Libs.kotlin_test_junit)
        implementation(KotlinX.coroutines.core)
        implementation(KotlinX.coroutines.test)
        implementation(Libs.filepeek) //necessary to ensure the file peek version is updated to latest version
        implementation(Libs.assertk_jvm)

        implementation(Libs.assertk_jvm)

        implementation(Libs.junit_jupiter_api)
        implementation(Libs.junit_jupiter_params)
        implementation(Libs.junit_jupiter_engine)

        implementation(Libs.assertj_core)
        implementation(KotlinX.coroutines.core)
        implementation(Libs.filepeek) //necessary to ensure the file peek version is updated to latest version
      }
    }

    named("jsMain") {
      dependencies {
        //implementation(project(Projects.open_unit_unit))
      }
    }
    named("jsTest") {
      dependencies {
        implementation(Libs.kotlin_test)
      }
    }
  }
}

configureKotlin()
configureToolchainJava17LTS()
