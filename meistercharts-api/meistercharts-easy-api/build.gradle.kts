import it.neckar.gradle.console
import it.neckar.gradle.npmbundle.CopyBundleContentTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

description = """Meistercharts - Easy API"""

plugins {
  npmBundle
  generatePackageJson
}

kotlin {
  jvm {
  }
  js {
    executableJsApplication(
      varName = "meisterchartsEasyApi",
      jsTargetType = JsTargetType.ES2015,
      webpackModuleType = WebpackModuleType.ModernModule,
      webpackModuleTypeForDev = WebpackModuleType.Var,
    ) {
      mode = KotlinWebpackConfig.Mode.PRODUCTION
    }
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

    jsMain {
      dependencies {
      }
    }

    jsTest {
      dependencies {
        implementation(libs.kotlin.test)
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
  }
}

npmBundle {
  moduleName = "@meistercharts/meistercharts"
  archiveFileName = "@meistercharts-easy-api"
  dirNameInArchive = "package"
  this.version = meisterchartsVersion
}

npmBundleDevelopment {
  moduleName = "@meistercharts-dev/meistercharts"
  archiveFileName = "@meistercharts-easy-api-dev"
  dirNameInArchive = "package"
  this.version = meisterchartsVersion
}


tasks.register("publishNpmBundle") {
  group = "Publishing"
  description = "Publish the NPM bundle to npmjs.org"

  dependsOn("npmBundle")

  doLast {
    val npmTag = if (isMeisterchartsSnapshot) {
      "snapshot"
    } else {
      "latest"
    }

    providers.exec {
      workingDir = projectDir
      commandLine("npm", "publish", "--access", "public", "--tag", npmTag)
      workingDir = file("build/npm/work")
    }.result.get().rethrowFailure()
  }
}

tasks.register("npmInfo") {
  group = "Publishing"
  description = "Get the information about the published version numbers"

  doLast {
    val standardOutput = providers.exec {
      workingDir = projectDir
      commandLine("yarn", "-s", "info", "@meistercharts/meistercharts", "versions")
    }.standardOutputAsStringOnSuccess()

    println(console.green(standardOutput))
  }
}

tasks.register("publishNpmBundleDevelopment") {
  group = "Publishing"
  description = "Publish the NPM dev bundle to npmjs.org"

  dependsOn("npmBundleDevelopment")

  doLast {
    val npmTag = if (isMeisterchartsSnapshot) {
      "snapshot"
    } else {
      "latest"
    }

    providers.exec {
      workingDir = projectDir
      commandLine("npm", "publish", "--access", "public", "--tag", npmTag)
      workingDir = file("build/npmDevelopment/work")
    }.result.get().rethrowFailure()
  }
}

tasks.register("fileSizeMetrics") {
  group = "Reporting"
  description = "Creates the file size metrics for Meistercharts"

  dependsOn("build")

  val originalPath = "build/dist/js/productionExecutable/meistercharts-easy-api.js"
  val reportOutPath = "build/reports/meistercharts.metrics.md"

  inputs.file(originalPath)
  outputs.file(reportOutPath)

  doLast {
    println("Reporting file size:")

    val lengthOriginal = file(originalPath).length()
    println("meistercharts-easy-api.js: ${lengthOriginal / 1024} KB")

    val out = file(reportOutPath)

    out.writeText(
      """
      # Meistercharts-easy-api.JS file size

      meistercharts-easy-api.js ${lengthOriginal / 1024} KB\
    """.trimIndent()
    )
  }
}

tasks.register("metrics") {
  group = "Reporting"
  description = "Creates the metrics for Meistercharts"

  dependsOn("fileSizeMetrics")
}
