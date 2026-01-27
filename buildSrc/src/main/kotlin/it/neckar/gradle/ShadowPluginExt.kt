package it.neckar.gradle

import com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import formatAsMegaBytes
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named

/**
 * Configures the shadowJar task for a service module.
 *
 * This function centralizes the common shadowJar configuration that is repeated across many service modules.
 * It sets up:
 * - ZIP64 format for large JARs
 * - Archive naming (baseName only, no appendix/version/classifier)
 * - Main-Class manifest attribute
 * - Default minimize exclusions (Kotlin Reflect, Logback, Clikt)
 * - Service file merging
 * - Logging of created JAR size
 *
 * @param baseName the base name for the shadow JAR (e.g., "gitlab-auto-merge")
 * @param mainClass the fully qualified main class name
 * @param minimizeExclusions optional additional exclusions for the minimize block (beyond the defaults)
 * @return the configured ShadowJar task provider
 */
fun Project.configureServiceShadowJar(
  baseName: String,
  mainClass: String,
  minimizeExclusions: DependencyFilter.() -> Unit = {},
): TaskProvider<ShadowJar> {
  return tasks.named<ShadowJar>("shadowJar") {
    isZip64 = true

    archiveBaseName.set(baseName)
    archiveAppendix.set("")
    archiveVersion.set("")
    archiveClassifier.set("")

    manifest {
      attributes["Main-Class"] = mainClass
    }

    // Default exclusions are always applied
    minimize {
      exclude(Libs.kotlin_reflect)
      exclude(Libs.logback_classic)
      exclude(Libs.clikt)
      exclude(Libs.ktor_serialization_kotlinx_json)
      minimizeExclusions()
    }

    // Service-Provider files are always merged correctly
    mergeServiceFiles()

    doLast {
      val fatJar = archiveFile.get().asFile
      logger.lifecycle("Shadow jar created: ${fatJar.path} (${fatJar.length().formatAsMegaBytes()} MB)")
    }
  }
}

/**
 * Excludes a dependency from minimization using the Libs constant directly.
 *
 * Usage: `exclude(Libs.kotlin_reflect)` instead of `exclude(dependency(Libs.kotlin_reflect.removeSuffix(":_")))`
 */
fun DependencyFilter.exclude(lib: String) {
  exclude(dependency(lib.removeSuffix(":_")))
}

/**
 * Excludes Kotlin Reflect when minimizing the shadow jar.
 * Note: Already included in configureServiceShadowJar defaults.
 */
fun DependencyFilter.excludeKotlinReflect() {
  exclude(Libs.kotlin_reflect)
}

/**
 * Excludes Logback when minimizing the shadow jar.
 * Note: Already included in configureServiceShadowJar defaults.
 */
fun DependencyFilter.excludeLogback() {
  exclude(Libs.logback_classic)
}

/**
 * Excludes Clikt when minimizing the shadow jar.
 * Note: Already included in configureServiceShadowJar defaults.
 */
fun DependencyFilter.excludeClikt() {
  exclude(Libs.clikt)
}

/**
 * Excludes Ktor Serialization Kotlinx JSON from minimization.
 * Required because the provider class is loaded via ServiceLoader and has no direct code references.
 * Note: Already included in configureServiceShadowJar defaults.
 */
fun DependencyFilter.excludeKtorSerializationJson() {
  exclude(Libs.ktor_serialization_kotlinx_json)
}
