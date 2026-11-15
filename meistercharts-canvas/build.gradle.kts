import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.addKotlinTestDependencies

description = """Meistercharts - Canvas"""

plugins {
  generateIcons
  kotlinxSerialization
  kspBoxing
  generateTypesList
  generateTsDeclaration
}

val neckarOpenVersion: String by project

kotlin {
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {

        api("it.neckar.open:annotations:$neckarOpenVersion")
        api("it.neckar.open:unit:$neckarOpenVersion")
        api("it.neckar.open:unit-conversion:$neckarOpenVersion")
        api("it.neckar.open:kotlin-async:$neckarOpenVersion")
        api("it.neckar.open:logging:$neckarOpenVersion")
        api("it.neckar.open:kotlin-js:$neckarOpenVersion")

        api("it.neckar.open.meistercharts:meistercharts-core")

        //TODO remove deps - or make optional
        api("it.neckar.open.meistercharts:meistercharts-history-core")
        api("it.neckar.open.meistercharts:meistercharts-history-api")
        api("it.neckar.open:kotlin-events:$neckarOpenVersion")
        api("it.neckar.open.meistercharts:meistercharts-version-info")
      }
    }

    commonTest {
      dependencies {
        implementation(libs.kotlinx.html)
      }
    }

    jsMain {
      dependencies {
      }
    }

    webMain {
      dependencies {
        implementation(libs.kotlinx.browser)
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
        implementation("it.neckar.open:test-utils:$neckarOpenVersion")
        implementation("it.neckar.open:kotlinx-serialization-test-utils:$neckarOpenVersion")

        implementation(libs.controlsfx)
      }
    }
  }
}

//tasks.register("sourcesJar") {
//  group = "Build"
//  description = "Assembles the sources jar for JS and JVM"
//
//  dependsOn("jsSourcesJar")
//  dependsOn("jvmSourcesJar")
//}

createIcons {
  objectName = "Icons"

  svgIconsSourceDir = file("src/icons/")
  basicIconsFile = file("src/commonMain/kotlin/com/meistercharts/resources/Icons.kt")

  svgPathsFile = file("src/commonMain/kotlin/com/meistercharts/resources/svg/SvgPaths.kt")
  svgPaintableProvidersFile = file("src/commonMain/kotlin/com/meistercharts/resources/svg/SvgPaintableProviders.kt")
  svgPaintablesProviderProviderFile = file("src/commonMain/kotlin/com/meistercharts/resources/svg/SvgPaintablesProviderProvider.kt")
}
