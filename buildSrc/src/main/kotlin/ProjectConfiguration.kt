package it.neckar.gradle

import dev.detekt.gradle.extensions.DetektExtension
import it.neckar.projects.ConfiguredProject
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Simplified project configuration for the standalone meistercharts.com-sync build.
 *
 * The monorepo version of this file has grown to reference many monorepo-specific
 * features (Python, Docker, Certificates, KSP processors). This local version
 * provides only the configuration needed for MeisterCharts multiplatform modules.
 */
object ProjectConfiguration {

  /**
   * Declares the module's registered Kotlin targets and the shared setup around them — the same
   * contract the monorepo's `configureMultiplatform` has, reduced to what this build needs: the checks of
   * the declared targets against the registry (`verifyDeclaredTargets`, `verifyTargetSourceDirectories`) stay out.
   */
  fun configureMultiplatform(project: Project, configuredProject: ConfiguredProject) {
    with(project) {
      apply(plugin = Plugins.kotlinMultiPlatform)
      apply(plugin = Plugins.detekt)
      apply(plugin = Plugins.kover)

      configureKotlin()

      val kotlinMultiplatformExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)
      kotlinMultiplatformExtension.applyMultiplatformKotlinConfiguration()
      kotlinMultiplatformExtension.declareTargets(project, configuredProject.targets)

      configureJunit()
      configureToolchain(JvmType.JavaLatestLTS)

      configureDetekt {
        source.setFrom(files(multiplatformDetektSourceDirectories()))
      }

      configureKover {}
    }
  }
}

fun Project.configureDetekt(additionalConfig: DetektExtension.() -> Unit) {
  extensions.getByType(DetektExtension::class.java).apply {
    config.from(rootProject.files("config/detekt/detekt.yml"))
    // Detekt's default (false): per-module multithreading races on the Kotlin compiler's
    // non-thread-safe KotlinCliJavaFileManagerImpl during type resolution and crashes
    // intermittently with `ArrayIndexOutOfBoundsException` at the internal map's rehash().
    // Same root cause and fix as the main build's configureDetekt. Refs: detekt#5403, detekt#2629.
    parallel = false
    buildUponDefaultConfig = true
    additionalConfig()
  }

  plugins.withType(dev.detekt.gradle.plugin.DetektPlugin::class) {
    tasks.withType(dev.detekt.gradle.Detekt::class) {
      reports {
        html.required.set(true)
      }
    }
  }

  tasks.named("check") {
    this.setDependsOn(this.dependsOn.filterNot {
      it is TaskProvider<*> && it.name.contains("detekt")
    })
  }
}

fun Project.configureKover(additionalConfig: KoverProjectExtension.() -> Unit) {
  val koverProjectExtension = extensions.getByType<KoverProjectExtension>()
  koverProjectExtension.additionalConfig()
}
