import it.neckar.gradle.addAnnotationDependencies
import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.addKotlinTestDependencies

plugins {
  kotlinxSerialization
  kspSerialization
}

val neckarOpenVersion: String by project

description = "Meistercharts - History API — REST query model and storage access for time-series history data."

kotlin {
  addAnnotationDependencies(project)
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        api("it.neckar.open.meistercharts:meistercharts-history-core")
        api("it.neckar.open:unit:$neckarOpenVersion")

        api(libs.kotlinx.serialization.json)
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
      }
    }

    jvmMain {
      dependencies {
        api(libs.threeten.extra)
        api(libs.kotlinx.coroutines.core)
        implementation("it.neckar.open:io:$neckarOpenVersion")
        implementation(libs.commons.compress)
      }
    }

    jvmTest {
      dependencies {
        implementation("it.neckar.open:test-utils:$neckarOpenVersion")
        implementation("it.neckar.open:kotlinx-serialization-test-utils:$neckarOpenVersion")

        implementation(libs.kotlinx.serialization.json)
      }
    }
  }
}
