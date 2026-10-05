package it.neckar.gradle

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * The scope of the source set to add the dependencies to
 */
enum class Scope {
  Main,
  Test
}

/**
 * Returns true if this multiplatform extension has a JS target registered.
 */
val KotlinMultiplatformExtension.hasJsTarget: Boolean
  get() = targets.findByName("js") != null

private fun KotlinMultiplatformExtension.common(scope: Scope, configure: KotlinSourceSet.() -> Unit) {
  sourceSets {
    when (scope) {
      Scope.Main -> {
        commonMain(configure)
      }

      Scope.Test -> {
        commonTest(configure)
      }
    }
  }
}

/**
 * Configures JS source sets only if a JS target is registered.
 * This prevents "Source Set Used Without a Corresponding Target" warnings
 * for JVM-only multiplatform projects.
 */
private fun KotlinMultiplatformExtension.js(scope: Scope, configure: KotlinSourceSet.() -> Unit) {
  if (hasJsTarget.not()) {
    return
  }

  sourceSets {
    when (scope) {
      Scope.Main -> {
        jsMain(configure)
      }

      Scope.Test -> {
        jsTest(configure)
      }
    }
  }
}

private fun KotlinMultiplatformExtension.jvm(scope: Scope, configure: KotlinSourceSet.() -> Unit) {
  sourceSets {
    when (scope) {
      Scope.Main -> {
        jvmMain(configure)
      }

      Scope.Test -> {
        jvmTest(configure)
      }
    }
  }
}

/** A library of the version catalog, as a `libs.*` accessor returns it. */
typealias CatalogLibrary = Provider<MinimalExternalModuleDependency>

/**
 * The libraries the helpers below add; `verifyUnusedDependencies` accepts them unused from a helper,
 * because a standard library is not worth a declaration in every build script.
 */
class StandardDependencies(libs: LibrariesForLibs) {
  /**
   * Guice 7 reads `jakarta.inject` only. `javax.inject` stays until the modules under
   * `internal/closed/lizergy/` declare it themselves: https://git.neckar.it/neckarit/neckar-hub/-/issues/3398
   */
  val annotations: List<CatalogLibrary> = listOf(libs.jsr305, libs.jakarta.inject.api, libs.javax.inject, libs.javax.annotation.api, libs.org.jetbrains.annotations)

  val kotlinJs: List<CatalogLibrary> = listOf(libs.kotlin.js)

  val testCommon: List<CatalogLibrary> = listOf(
    libs.kotlin.test.asProvider(),
    libs.kotlin.test.common,
    libs.kotlin.test.annotations.common,
    libs.kotlin.reflect,
    libs.kotlinx.coroutines.core,
    libs.kotlinx.coroutines.test,
    libs.assertk.asProvider(),
  )

  val testJs: List<CatalogLibrary> = listOf(libs.kotlin.test.js)

  val testJvm: List<CatalogLibrary> = listOf(
    libs.kotlin.test.junit5,
    libs.junit.jupiter.api,
    libs.junit.jupiter.engine,
    libs.junit.jupiter.params,
    libs.mockk,
    libs.kotlinx.coroutines.debug,
    libs.awaitility,
    libs.logback.classic,
  )

  val ktorClient: List<CatalogLibrary> = listOf(
    libs.kotlinx.coroutines.core,
    libs.ktor.client.core,
    libs.ktor.client.json,
    libs.ktor.client.serialization,
    libs.ktor.client.logging,
    libs.ktor.client.content.negotiation,
    libs.ktor.serialization.kotlinx.asProvider(),
    libs.ktor.serialization.kotlinx.json,
  )

  val ktorClientJvm: List<CatalogLibrary> = listOf(libs.ktor.client.okhttp)

  /** Added to the test scope by every Ktor client helper, whatever scope the caller passes. */
  val ktorClientTest: List<CatalogLibrary> = listOf(libs.ktor.client.mock)

  val ktorServer: List<CatalogLibrary> = listOf(
    libs.ktor.server.core,
    libs.ktor.server.netty,
    libs.kotlinx.coroutines.core,
    libs.ktor.server.asProvider(),
    libs.ktor.server.websockets,
    libs.ktor.server.sse,
    libs.ktor.server.auth.asProvider(),
    libs.ktor.server.metrics,
    libs.ktor.server.conditional.headers,
    libs.ktor.server.call.id,
    libs.ktor.serialization.kotlinx.asProvider(),
    libs.ktor.serialization.kotlinx.json,
    libs.logback.classic,
  )

  /** Added to the test scope by every Ktor server helper, whatever scope the caller passes. */
  val ktorServerTest: List<CatalogLibrary> = listOf(libs.ktor.server.test.host)

  /** Every list above; a library in several lists appears several times. */
  val all: List<CatalogLibrary> = (annotations + kotlinJs + testCommon + testJs + testJvm + ktorClient + ktorClientJvm + ktorClientTest + ktorServer + ktorServerTest)
}

/**
 * The [StandardDependencies] from this project's version catalog `libs`; each call builds them anew.
 */
fun Project.standardDependencies(): StandardDependencies {
  return StandardDependencies(extensions.getByType(LibrariesForLibs::class.java))
}

/**
 * Declares [libraries] on this source set: `api` for [Scope.Main], `implementation` for [Scope.Test].
 */
private fun KotlinSourceSet.declare(scope: Scope, libraries: List<CatalogLibrary>) {
  dependencies {
    libraries.forEach {
      when (scope) {
        Scope.Main -> api(it)
        Scope.Test -> implementation(it)
      }
    }
  }
}

/**
 * Adds the annotations; never `com.intellij:annotations` besides `org.jetbrains:annotations`, which
 * ships the same classes under other coordinates, so a fat jar would carry them twice.
 */
fun KotlinMultiplatformExtension.addAnnotationDependencies(project: Project, scope: Scope = Scope.Main) {
  jvm(scope) { declare(scope, project.standardDependencies().annotations) }
}

fun DependencyHandlerScope.addKotlinDependencies() {
  //Do nothing,
  //Keep for symmetry
}

/**
 * Add Kotlin related dependencies to the project; [StandardDependencies.kotlinJs] only with a JS target.
 */
fun KotlinMultiplatformExtension.addKotlinDependencies(project: Project) {
  addAnnotationDependencies(project, Scope.Main)
  js(Scope.Main) { declare(Scope.Main, project.standardDependencies().kotlinJs) }
}

/**
 * Adds the test dependencies to the project.
 * - Scope.Test: Uses implementation() - for test source sets in regular projects
 * - Scope.Main: Uses api() - for test-utility projects that export test functionality
 */
fun KotlinMultiplatformExtension.addKotlinTestDependencies(project: Project, scope: Scope = Scope.Test) {
  val standard: StandardDependencies = project.standardDependencies()
  common(scope) { declare(scope, standard.testCommon) }
  js(scope) { declare(scope, standard.testJs) }
  jvm(scope) { declare(scope, standard.testJvm) }
}

/**
 * Adds the ktor client dependencies, and [StandardDependencies.ktorClientTest] to the test scope.
 * - Scope.Main: Uses api() - for exposing dependencies transitively
 * - Scope.Test: Uses implementation() - for test source sets
 */
fun KotlinMultiplatformExtension.addKtorClientDependencies(project: Project, scope: Scope) {
  val standard: StandardDependencies = project.standardDependencies()
  common(scope) { declare(scope, standard.ktorClient) }
  jvm(scope) { declare(scope, standard.ktorClientJvm) }
  common(Scope.Test) { declare(Scope.Test, standard.ktorClientTest) }
}

/**
 * Adds the ktor server dependencies, and [StandardDependencies.ktorServerTest] to the test scope.
 */
fun KotlinMultiplatformExtension.addKtorServerDependencies(project: Project, scope: Scope) {
  val standard: StandardDependencies = project.standardDependencies()
  jvm(scope) { declare(scope, standard.ktorServer) }
  jvm(Scope.Test) { declare(Scope.Test, standard.ktorServerTest) }
}

fun DependencyHandlerScope.addAnnotationDependencies(project: Project, scope: Scope = Scope.Main) {
  // Scope.Main declares them `api`: dependent modules compile against the annotated signatures.
  val configurationName = scope.configurationName()
  project.standardDependencies().annotations.forEach { add(configurationName, it) }
}

/**
 * Adds kotlin test dependencies
 */
fun DependencyHandlerScope.addKotlinTestDependencies(project: Project, scope: Scope = Scope.Test) {
  val configurationName = scope.configurationName()
  project.standardDependencies().let { it.testCommon + it.testJvm }.forEach { add(configurationName, it) }
}

internal fun Scope.configurationName(): String {
  val scopeName = when (this) {
    Scope.Main -> "api"
    Scope.Test -> "testImplementation"
  }
  return scopeName
}

/**
 * Adds kotlin test dependencies - used when configuring a TestSuite
 */
@Suppress("UnstableApiUsage")
fun JvmComponentDependencies.addKotlinTestDependencies(project: Project) {
  project.standardDependencies().let { it.testCommon + it.testJvm }.forEach { implementation(it) }
}

/**
 * Adds the ktor client dependencies, and [StandardDependencies.ktorClientTest] to `testImplementation`.
 */
fun DependencyHandler.addKtorClientDependencies(project: Project, scope: Scope = Scope.Main) {
  val configurationName = scope.configurationName()
  val standard: StandardDependencies = project.standardDependencies()
  (standard.ktorClient + standard.ktorClientJvm).forEach { add(configurationName, it) }
  standard.ktorClientTest.forEach { add(Scope.Test.configurationName(), it) }
}

/**
 * Adds the ktor server dependencies, and [StandardDependencies.ktorServerTest] to `testImplementation`.
 */
fun DependencyHandlerScope.addKtorServerDependencies(project: Project, scope: Scope = Scope.Main) {
  val configurationName = scope.configurationName()
  val standard: StandardDependencies = project.standardDependencies()
  standard.ktorServer.forEach { add(configurationName, it) }
  standard.ktorServerTest.forEach { add(Scope.Test.configurationName(), it) }
}
