import it.neckar.gradle.addKotlinDependencies
import it.neckar.gradle.addKotlinTestDependencies

description = """Meistercharts - Core"""

plugins {
  kotlinxSerialization
  kspBoxing
}

val neckarOpenVersion: String by project

kotlin {
  addKotlinDependencies(project)
  addKotlinTestDependencies(project)

  sourceSets {
    commonMain {
      dependencies {
        api("it.neckar.open:annotations:$neckarOpenVersion")

        api("it.neckar.open:i18n:$neckarOpenVersion")
        api("it.neckar.open:unit:$neckarOpenVersion")
        api("it.neckar.open:geometry:$neckarOpenVersion")
        api("it.neckar.open:kotlin-lang:$neckarOpenVersion")
        api("it.neckar.open:kotlin-async:$neckarOpenVersion")
        api("it.neckar.open:disposable:$neckarOpenVersion")
        api("it.neckar.open:observable:$neckarOpenVersion")
        api("it.neckar.open:provider:$neckarOpenVersion")
        api("it.neckar.open:kotlin-collections:$neckarOpenVersion")
        api("it.neckar.open:datetime-minimal:$neckarOpenVersion")
        api(libs.kotlinx.serialization.core)
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
        api("it.neckar.open:time:$neckarOpenVersion")
      }
    }

    jvmTest {
      dependencies {
        implementation("it.neckar.open:test-utils:$neckarOpenVersion")

        implementation("it.neckar.open:kotlinx-serialization-test-utils:$neckarOpenVersion")

        implementation(libs.controlsfx)

        implementation(libs.measured)
      }
    }
  }
}
