package it.neckar.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import it.neckar.gradle.formatAsMegaBytes
import it.neckar.gradle.lib
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
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
 * - A `META-INF/app-git-info.properties` resource packed only into the fat jar (the fat-jar is
 *   a leaf artifact — nobody consumes it as a build input, so the volatile git values cannot
 *   churn any build cache; VersionInformation resolves the resource at runtime, #2413).
 *   Deliberately a resource, not manifest attributes: the Shadow plugin shares manifest state
 *   with the jar task, so manifest attributes leak into the plain library jar and make it
 *   volatile per commit.
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
  val createFatJarGitInfoTask = registerCreateFatJarGitInfoTask()

  // Shadow 9.6.0 applies @ShadowDsl (a @DslMarker) to ShadowJar and DependencyFilter, which forbids
  // implicitly calling the outer ShadowJar receiver's `project` from inside minimize {}. Capture the
  // Project extension receiver explicitly so the lib(...) lookups resolve without an implicit receiver.
  val project = this

  return tasks.named<ShadowJar>("shadowJar") {
    isZip64 = true

    archiveBaseName.set(baseName)
    archiveAppendix.set("")
    archiveVersion.set("")
    archiveClassifier.set("")

    // Shadow 9.x requires INCLUDE for mergeServiceFiles() to work (EXCLUDE skips duplicates before merging)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    manifest {
      attributes["Main-Class"] = mainClass
    }

    //Packs META-INF/app-git-info.properties into the fat jar only (see registerCreateFatJarGitInfoTask)
    from(createFatJarGitInfoTask)

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
 * Registers the task creating `META-INF/app-git-info.properties`, packed exclusively into the
 * fat jar (#2413). The file never exists in library jars or module outputs, so the volatile git
 * values cannot churn any build input. VersionInformation resolves it at runtime
 * (chain: system property -> env -> this resource -> "unknown").
 *
 * Real values only on CI/main; locally the file carries no entries so VersionInformation falls
 * back to "unknown" instead of a placeholder hash.
 */
private fun Project.registerCreateFatJarGitInfoTask(): TaskProvider<Task> {
  val gitInfoDirProvider = layout.buildDirectory.dir("generated/fatJarGitInfo")
  val gitInfoEntries: Map<String, String> = if (inCi || onMainBranch) {
    GitProperty.entries.associate { gitProperty ->
      gitProperty.propertyKey to getBuildInfoVarValue(gitProperty.buildInfoVar)
    }
  } else {
    emptyMap()
  }

  return tasks.register("createFatJarGitInfo") {
    group = "Build"
    description = "Creates the META-INF/app-git-info.properties resource packed into the fat jar"

    inputs.property("gitInfoEntries", gitInfoEntries)
    outputs.dir(gitInfoDirProvider)

    doLast {
      val targetFile = gitInfoDirProvider.get().file("META-INF/app-git-info.properties").asFile
      targetFile.parentFile.mkdirs()
      //Written by hand instead of Properties.store() (which prepends a timestamp comment and
      //would break rebuild idempotence). No escaping needed: keys are enum constants, values
      //are a hex hash and an ISO-8601 datetime - neither contains '=' or a leading backslash.
      targetFile.writeText(
        gitInfoEntries.entries.joinToString(separator = "\n", postfix = "\n") { (propertyKey, value) ->
          "$propertyKey=$value"
        }
      )
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
