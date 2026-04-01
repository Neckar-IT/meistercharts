package it.neckar.gradle

import com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import formatAsMegaBytes
import lib
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
      exclude(project.lib("kotlin-reflect"))
      exclude(project.lib("logback-classic"))
      exclude(project.lib("clikt"))
      exclude(project.lib("ktor-serialization-kotlinx-json"))
      exclude(project.lib("ktor-client-okhttp")) // OkHttp engine is loaded via ServiceLoader
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
 * Excludes a dependency from minimization using a catalog library provider.
 * Extracts the group:artifact pattern for the dependency filter.
 */
fun DependencyFilter.exclude(lib: Any) {
  when (lib) {
    is org.gradle.api.provider.Provider<*> -> {
      val dep = lib.get()
      if (dep is org.gradle.api.artifacts.MinimalExternalModuleDependency) {
        exclude(dependency("${dep.module}"))
      }
    }

    is String -> {
      val groupArtifact = lib.substringBeforeLast(":")
      exclude(dependency(groupArtifact))
    }

    else -> error("Unsupported dependency type: ${lib::class}")
  }
}

/**
 * Excludes Kotlin Reflect when minimizing the shadow jar.
 * Note: Already included in configureServiceShadowJar defaults.
 */
fun DependencyFilter.excludeKotlinReflect(project: Project) {
  exclude(project.lib("kotlin-reflect"))
}

/**
 * Excludes Logback when minimizing the shadow jar.
 * Note: Already included in configureServiceShadowJar defaults.
 */
fun DependencyFilter.excludeLogback(project: Project) {
  exclude(project.lib("logback-classic"))
}

/**
 * Excludes Clikt when minimizing the shadow jar.
 * Note: Already included in configureServiceShadowJar defaults.
 */
fun DependencyFilter.excludeClikt(project: Project) {
  exclude(project.lib("clikt"))
}

/**
 * Excludes Ktor Serialization Kotlinx JSON from minimization.
 * Required because the provider class is loaded via ServiceLoader and has no direct code references.
 * Note: Already included in configureServiceShadowJar defaults.
 */
fun DependencyFilter.excludeKtorSerializationJson(project: Project) {
  exclude(project.lib("ktor-serialization-kotlinx-json"))
}
