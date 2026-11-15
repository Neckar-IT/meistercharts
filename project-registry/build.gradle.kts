// The registry of the MeisterCharts projects: compiled with Gradle's embedded Kotlin, like build-logic/ of the
// monorepo, which depends on it.
plugins {
  kotlin("jvm") version embeddedKotlinVersion apply false
}

subprojects {
  group = "it.neckar.open.meistercharts.project-registry"

  pluginManager.withPlugin("java-base") {
    extensions.configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
      }
    }
  }

  tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    compilerOptions {
      javaParameters = true
    }
  }

  tasks.withType<Test>().configureEach {
    maxHeapSize = "1g"

    // redirectJvmCrashReportsToBuildDirectory() of internal/buildplatform/conventions, which this build does not apply to itself.
    val crashReportsDirectory = "build/jvm-crashes/$name"
    jvmArgs("-XX:ErrorFile=$crashReportsDirectory/hs_err_pid%p.log")
    doFirst { workingDir.resolve(crashReportsDirectory).mkdirs() }
  }
}
