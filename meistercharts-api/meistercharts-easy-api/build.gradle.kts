import it.neckar.gradle.npmbundle.CopyBundleContentTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

description = """Meistercharts - Easy API"""

plugins {
  kotlinMultiPlatform
  kotlinxSerialization
  npmBundle
  generatePackageJson

  if (false) {
    generateTsDeclaration
  }
}

kotlin {
  js {
    executableJsApplication(
      varName = "meisterchartsEasyApi",
      jsTargetType = JsTargetType.ES2015,
      webpackModuleType = WebpackModuleType.ModernModule,
      webpackModuleTypeForDev = WebpackModuleType.Var,
      //jsTargetType = JsTargetType.ES2015,
      //webpackModuleType = WebpackModuleType.ModernModule,
      //webpackModuleTypeForDev = WebpackModuleType.ModernModule,
    ) {
      //Workaround to avoid
      // java.lang.IllegalStateException: Cannot read properties of undefined (reading 'BarChartGrouped2')
      // in jsBrowserDevelopmentWebpack

      mode = KotlinWebpackConfig.Mode.DEVELOPMENT
    }

  }
  jvm {
  }

  sourceSets {
    commonMain {
      dependencies {
        api(project(Projects.meistercharts_core))
        api(project(Projects.meistercharts_history_core))
        api(project(Projects.meistercharts_canvas))
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
