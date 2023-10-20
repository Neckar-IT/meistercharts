import it.neckar.gradle.npmbundle.CopyBundleContentTask

description = """Meistercharts - Easy API"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
  npmBundle

  if (false) {
    generateTsDeclaration
  }
}

configureKotlin()
configureToolchainJava17LTS()

kotlin {
  js {
    browser()
  }
  jvm {
  }

  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.meistercharts_core))
        api(project(Projects.meistercharts_history_core))
        api(project(Projects.meistercharts_canvas))

        implementation(Libs.kotlinx_coroutines_core)
        //api(project(meistercharts_version_info))
        api(KotlinX.serialization.core)
      }
    }

    commonTest {
      dependencies {
        implementation(Libs.kotlin_test_common)
        implementation(Libs.kotlin_test_annotations_common)
        implementation(Libs.assertk)
      }
    }

    jvm().compilations["main"].defaultSourceSet {
      dependencies {
        implementation(Libs.kotlinx_coroutines_core)

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
        implementation(Libs.commons_math3)
        implementation(Libs.logback_classic)
        implementation(Libs.awaitility)
        implementation(Libs.measured)

        implementation(Libs.kotlin_reflect)
        implementation(Libs.kotlin_test_junit)
        implementation(KotlinX.coroutines.core)
        implementation(KotlinX.coroutines.test)
        implementation(Libs.mockk)

        implementation(Libs.junit_jupiter_api)
        implementation(Libs.junit_jupiter_params)
        implementation(Libs.junit_jupiter_engine)

        implementation(KotlinX.coroutines.core)
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

/**
 * Generate type script definitions for the Kotlin public API
 */
//generateTypeScriptDefinitions {
//namespace = "Meistercharts"
//  typeScriptDefinitionFile.set(file("build/meistercharts-easy-api.d.ts"))
//targetTypescriptDefinitionFileName = "meistercharts-easy-api.d.ts"
//
//  exportConfigFile.set(file("ts-generation.paths"))
//}

npmBundle {
  moduleName.set("@meistercharts/meistercharts")

  archiveFileName.set("@meistercharts-easy-api")
  dirNameInArchive.set("package")
}

tasks.getByName<CopyBundleContentTask>("npmCopyBundleContent") {
  dependsOn("jsBrowserWebpack", "jsBrowserDistribution")
  tasks.findByName("createTypeScriptDefinitions")?.let {
    dependsOn(it)
  }

  from("build/distributions")
  include("meistercharts-easy-api.js", "meistercharts-easy-api.js.map")

  from("build")
  include("meistercharts-easy-api.d.ts")
}


npmBundleDevelopment {
  moduleName.set("@meistercharts-dev/meistercharts")
  archiveFileName.set("@meistercharts-easy-api-dev")
  dirNameInArchive.set("package")
}


tasks.getByName<CopyBundleContentTask>("npmCopyBundleContentDevelopment") {
  dependsOn("jsBrowserDevelopmentWebpack")
  tasks.findByName("createTypeScriptDefinitions")?.let {
    dependsOn(it)
  }

  from("build/developmentExecutable")
  include("meistercharts-easy-api.js")

  from("build")
  include("meistercharts-easy-api.d.ts")
}
