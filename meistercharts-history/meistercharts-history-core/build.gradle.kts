import it.neckar.gradle.addKotlinTestDependencies

description = """Meistercharts - History Core"""

plugins {
  kotlinxSerialization
}

val neckarOpenVersion: String by project

repositories {
  mavenCentral()
}

kotlin {
  sourceSets {
    addKotlinTestDependencies(project)

    commonMain {
      dependencies {
        // workaround: dependency=libs:kotlin:2.4.20 observed-on=2026-10-07 upstream=https://youtrack.jetbrains.com/issue/KT-43500
        // compileOnly is unsupported for Kotlin/JS: Kotlin 2.4.20 reports IncorrectCompileOnlyDependencyWarning.
        implementation("it.neckar.open:annotations:$neckarOpenVersion")

        api("it.neckar.open:unit:$neckarOpenVersion")
        api("it.neckar.open:provider:$neckarOpenVersion")
        api("it.neckar.open:disposable:$neckarOpenVersion")
        api("it.neckar.open.meistercharts:meistercharts-core")

        api("it.neckar.open:i18n:$neckarOpenVersion")
        api("it.neckar.open:kotlin-collections:$neckarOpenVersion")

        //Required for serializers
        api("it.neckar.open:kotlinx-serializers:$neckarOpenVersion")

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
        api("it.neckar.open:annotations:$neckarOpenVersion")

        api(libs.threeten.extra)
      }
    }

    jvmTest {
      dependencies {
        implementation("it.neckar.open:test-utils:$neckarOpenVersion")
        implementation("it.neckar.open:kotlinx-serialization-test-utils:$neckarOpenVersion")
      }
    }
  }
}
