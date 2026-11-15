import it.neckar.gradle.JsTargetType
import it.neckar.gradle.WebpackModuleType
import it.neckar.gradle.addAnnotationDependencies
import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.addKotlinTestDependencies
import it.neckar.gradle.ansiConsole
import it.neckar.gradle.executableJsApplication
import it.neckar.gradle.ksp.generating.ts.generateTypeScriptDefinitions
import it.neckar.gradle.npmbundle.CopyBundleContentTask
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations

description = """Meistercharts - Easy API"""

plugins {
  generateTsDeclaration
  npmBundle
}

val neckarOpenVersion: String by project
val meisterchartsVersion: String by project
val npmTag: String = if (meisterchartsVersion.contains("-SNAPSHOT")) "snapshot" else "latest"

kotlin {
  js {
    executableJsApplication(
      varName = "meisterchartsEasyApi",
      jsTargetType = JsTargetType.ES2015,
      webpackModuleType = WebpackModuleType.ModernModule,
      webpackModuleTypeForDev = WebpackModuleType.Var,
    )
  }

  addAnnotationDependencies(project)
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        api("it.neckar.open.meistercharts:meistercharts-history-core")
        api("it.neckar.open.meistercharts:meistercharts-canvas")

        api("it.neckar.open:unit:$neckarOpenVersion")
        api("it.neckar.open:kotlin-js:$neckarOpenVersion")
        api("it.neckar.open:kotlin-collections:$neckarOpenVersion")
      }
    }

    commonTest {
      dependencies {
      }
    }

    jsMain {
      dependencies {
        implementation("it.neckar.open:version-info:$neckarOpenVersion")
      }
    }

    jsTest {
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
  }
}

/**
 * Generate TypeScript definitions for the Kotlin public API
 */
generateTypeScriptDefinitions {
  targetTypescriptDefinitionFileName = "meistercharts-easy-api.d.ts"
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

tasks.named<CopyBundleContentTask>("npmCopyBundleContent") {
  //Add verification that the TypeScript definition file is present
  val typeScriptDefinitionFile: File = file("build/generated/ksp/js/jsMain/resources/meistercharts-easy-api.d.ts")
  doFirst {
    require(typeScriptDefinitionFile.isFile) {
      "${typeScriptDefinitionFile.absolutePath} is not a file"
    }
  }
}

tasks.register("publishNpmBundle") {
  group = "Publishing"
  description = "Publish the NPM bundle to npmjs.org"

  dependsOn("npmBundle")

  val bundleWorkDirectory = npmBundle.workingDir
  val execOperations = serviceOf<ExecOperations>()

  doLast {
    execOperations.exec {
      commandLine("npm", "publish", "--access", "public", "--tag", npmTag)
      workingDir = bundleWorkDirectory.get().asFile
    }
  }
}

tasks.register("npmInfo") {
  group = "Publishing"
  description = "Get the information about the published version numbers"

  val providerFactory = providers
  val moduleDirectory = projectDir
  val console = ansiConsole

  doLast {
    val standardOutput = providerFactory.exec {
      workingDir = moduleDirectory
      commandLine("yarn", "-s", "info", "@meistercharts/meistercharts", "versions")
    }.standardOutput.asText.get()

    println(console.green(standardOutput))
  }
}

tasks.register("publishNpmBundleDevelopment") {
  group = "Publishing"
  description = "Publish the NPM dev bundle to npmjs.org"

  dependsOn("npmBundleDevelopment")

  val bundleWorkDirectory = npmBundleDevelopment.workingDir
  val execOperations = serviceOf<ExecOperations>()

  doLast {
    execOperations.exec {
      commandLine("npm", "publish", "--access", "public", "--tag", npmTag)
      workingDir = bundleWorkDirectory.get().asFile
    }
  }
}

tasks.register("npmInfoDevelopment") {
  group = "Publishing"
  description = "Get the information about the published version numbers"

  val providerFactory = providers
  val moduleDirectory = projectDir
  val console = ansiConsole

  doLast {
    providerFactory.exec {
      workingDir = moduleDirectory
      commandLine("yarn", "-s", "info", "@meistercharts-dev/meistercharts", "versions")
    }.standardOutput.asText.get().let { out ->
      println(console.green(out))
    }
  }
}

//tasks.named<CopyBundleContentTask>("npmCopyBundleContentDevelopment") {
//  dependsOn("jsBrowserDevelopmentWebpack")
//
//  from("build/dist/js/developmentExecutable")
//  include("meistercharts-easy-api.js")
//
//  from("build/generated/ksp/js/jsMain/resources")
//  include("meistercharts-easy-api.d.ts")
//
//  doFirst {
//    file("build/generated/ksp/js/jsMain/resources/meistercharts-easy-api.d.ts").let {
//      require(it.isFile) {
//        "${it.absolutePath} is not a file}"
//      }
//    }
//  }
//}

tasks.register<Zip>("packageSources") {
  group = "Release"
  description = "Package the sources for deployment to the backup repository"

  //This task should be outdated now, since the sources are deployed to GitHub anyway

  archiveFileName = "@meistercharts-easy-api-sources.zip"
  duplicatesStrategy = DuplicatesStrategy.FAIL

  inputs.property("meisterchartsVersion", meisterchartsVersion)

  from("src/main/kotlin") {
    into("meistercharts/src/main/kotlin")
  }

  fun multiPlatformProject(projectName: String) {
    //Algorithms
    from("../../$projectName/src/jvmMain/kotlin") {
      into("$projectName/jvm")
    }
    from("../../$projectName/src/commonMain/kotlin") {
      into("$projectName/common")
    }
  }

  multiPlatformProject("meistercharts-version-info")
  multiPlatformProject("meistercharts-core")
  multiPlatformProject("meistercharts-canvas")
  multiPlatformProject("meistercharts-history/meistercharts-history-core")
  multiPlatformProject("meistercharts-history/meistercharts-history-api")

  into("meistercharts-${meisterchartsVersion}")
}

tasks.register<Copy>("backupDistribution") {
  group = "Release"
  description = "Saves the deployed stuff to the release-repo"

  dependsOn("npmBundle", "packageSources")

  into("../../backup/release-repo/${project.name}/$meisterchartsVersion")
  from(layout.buildDirectory)

  //The NPM bundle
  include("npm/@${project.name}.tar.gz")
  //The distributions ZIP file - containing all sources and PDF files
  include("distributions/@${project.name}-sources.zip")

  eachFile {
    //Flatten the structure (avoid sub dirs in the target repo)
    path = name
  }
  includeEmptyDirs = false //flattening leaves empty dirs
}

tasks.register("fileSizeMetrics") {
  group = "Reporting"
  description = "Creates the file size metrics for Meistercharts"

  dependsOn("build")

  val originalPath = "build/dist/js/productionExecutable/meistercharts-easy-api.js"

  val reportOutPath = "build/reports/meistercharts.metrics.md"

  //For incremental builds
  inputs.file(originalPath)
  outputs.file(reportOutPath)

  val originalFile = file(originalPath)
  val reportFile = file(reportOutPath)

  doLast {
    println("Reporting file size:")

    val lengthOriginal = originalFile.length()
    println("meistercharts-easy-api.js: ${lengthOriginal / 1024} KB")

    reportFile.writeText(
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

tasks.register("npmVersionInfo") {
  val execOperations = serviceOf<ExecOperations>()
  val moduleDirectory = projectDir

  doLast {
    execOperations.exec {
      workingDir = moduleDirectory
      commandLine = listOf("yarn", "info", "@meistercharts/meistercharts", "versions")
    }
  }
}
