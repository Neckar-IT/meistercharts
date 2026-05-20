package it.neckar.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.jvm.JvmComponentDependencies
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

/**
 * Adds "common" annotations to the project
 */
fun KotlinMultiplatformExtension.addAnnotationDependencies(project: Project, scope: Scope = Scope.Main) {
  jvm(scope) {
    dependencies {
      // These must remain `api` (not `compileOnly`) because they need to be transitively visible
      // to dependent modules that use annotations like @Nonnull, @Inject, etc.
      api(project.lib("jsr305"))
      api(project.lib("javax-inject"))
      api(project.lib("javax-annotation-api"))
      api(project.lib("com-intellij-annotations"))
    }
  }
}


fun DependencyHandlerScope.addKotlinDependencies() {
  //Do nothing,
  //Keep for symmetry
}

/**
 * Add Kotlin related dependencies to the project.
 * Only configures JS dependencies if a JS target is registered.
 */
fun KotlinMultiplatformExtension.addKotlinDependencies(project: Project) {
  addAnnotationDependencies(project, Scope.Main)

  if (hasJsTarget.not()) {
    return
  }

  sourceSets {
    jsMain {
      dependencies {
        api(project.lib("kotlin-js"))
      }
    }
  }
}

private fun testDepsCommon(project: Project, add: (Any) -> Unit) {
  add(project.lib("kotlin-test"))
  add(project.lib("kotlin-test-common"))
  add(project.lib("kotlin-test-annotations-common"))
  add(project.lib("kotlin-reflect"))
  add(project.lib("kotlinx-coroutines-core"))
  add(project.lib("kotlinx-coroutines-test"))
  add(project.lib("assertk"))
}

private fun testDepsJs(project: Project, add: (Any) -> Unit) {
  add(project.lib("kotlin-test-js"))
}

private fun testDepsJvm(project: Project, add: (Any) -> Unit) {
  add(project.lib("kotlin-test-junit5"))
  add(project.lib("junit-jupiter-api"))
  add(project.lib("junit-jupiter-engine"))
  add(project.lib("junit-jupiter-params"))
  add(project.lib("mockk"))
  add(project.lib("byte-buddy")) // Override MockK's old ByteBuddy for Java 25 support
  add(project.lib("byte-buddy-agent"))
  add(project.lib("commons-io"))
  add(project.lib("commons-math3"))
  add(project.lib("awaitility"))
  add(project.lib("measured"))
}

/**
 * Adds the test dependencies to the project.
 * - Scope.Test: Uses implementation() - for test source sets in regular projects
 * - Scope.Main: Uses api() - for test-utility projects that export test functionality
 */
fun KotlinMultiplatformExtension.addKotlinTestDependencies(project: Project, scope: Scope = Scope.Test) {
  when (scope) {
    Scope.Main -> {
      common(scope) { dependencies { testDepsCommon(project) { api(it) } } }
      js(scope) { dependencies { testDepsJs(project) { api(it) } } }
      jvm(scope) { dependencies { testDepsJvm(project) { api(it) } } }
    }

    Scope.Test -> {
      common(scope) { dependencies { testDepsCommon(project) { implementation(it) } } }
      js(scope) { dependencies { testDepsJs(project) { implementation(it) } } }
      jvm(scope) { dependencies { testDepsJvm(project) { implementation(it) } } }
    }
  }
}

private fun ktorClientDeps(project: Project, add: (Any) -> Unit) {
  add(project.lib("kotlinx-coroutines-core"))
  add(project.lib("ktor-client-core"))
  add(project.lib("ktor-client-json"))
  add(project.lib("ktor-client-serialization"))
  add(project.lib("ktor-client-logging"))
  add(project.lib("ktor-client-content-negotiation"))
  add(project.lib("ktor-serialization-kotlinx"))
  add(project.lib("ktor-serialization-kotlinx-json"))
}

private fun ktorClientJvmDeps(project: Project, add: (Any) -> Unit) {
  add(project.lib("ktor-client-okhttp"))
}

/**
 * Adds the ktor client dependencies.
 * - Scope.Main: Uses api() - for exposing dependencies transitively
 * - Scope.Test: Uses implementation() - for test source sets
 */
fun KotlinMultiplatformExtension.addKtorClientDependencies(project: Project, scope: Scope) {
  when (scope) {
    Scope.Main -> {
      common(scope) { dependencies { ktorClientDeps(project) { api(it) } } }
      jvm(scope) { dependencies { ktorClientJvmDeps(project) { api(it) } } }
    }

    Scope.Test -> {
      common(scope) { dependencies { ktorClientDeps(project) { implementation(it) } } }
      jvm(scope) { dependencies { ktorClientJvmDeps(project) { implementation(it) } } }
    }
  }
}

/**
 * Adds ktor server dependencies
 */
fun KotlinMultiplatformExtension.addKtorServerDependencies(project: Project, scope: Scope) {
  jvm(scope) {
    dependencies {
      api(project.lib("ktor-server-core"))
      api(project.lib("ktor-server-netty"))
      api(project.lib("kotlinx-coroutines-core"))
      api(project.lib("ktor-server"))
      api(project.lib("ktor-server-websockets"))
      api(project.lib("ktor-server-sse"))
      api(project.lib("ktor-server-auth"))
      api(project.lib("ktor-server-metrics"))
      api(project.lib("ktor-server-conditional-headers"))
      api(project.lib("ktor-server-call-id"))
      api(project.lib("ktor-serialization-kotlinx"))
      api(project.lib("ktor-serialization-kotlinx-json"))
      api(project.lib("logback-classic"))
    }
  }
}

fun DependencyHandlerScope.addAnnotationDependencies(project: Project, scope: Scope = Scope.Main) {
  // These must remain `api` (not `compileOnly`) because they need to be transitively visible
  // to dependent modules that use annotations like @Nonnull, @Inject, etc.
  val configurationName = scope.configurationName()

  add(configurationName, project.lib("jsr305"))
  add(configurationName, project.lib("javax-inject"))
  add(configurationName, project.lib("javax-annotation-api"))
  add(configurationName, project.lib("com-intellij-annotations"))
}


/**
 * Adds kotlin test dependencies
 */
fun DependencyHandlerScope.addKotlinTestDependencies(project: Project, scope: Scope = Scope.Test) {
  val configurationName = scope.configurationName()
  add(configurationName, project.lib("kotlin-test"))
  add(configurationName, project.lib("kotlin-test-common"))
  add(configurationName, project.lib("kotlin-test-annotations-common"))
  add(configurationName, project.lib("kotlinx-coroutines-core"))
  add(configurationName, project.lib("kotlinx-coroutines-test"))
  add(configurationName, project.lib("kotlinx-coroutines-debug"))

  add(configurationName, project.lib("kotlin-reflect"))
  add(configurationName, project.lib("assertk"))

  add(configurationName, project.lib("kotlin-test-junit5"))
  add(configurationName, project.lib("junit-jupiter-api"))
  add(configurationName, project.lib("junit-jupiter-engine"))
  add(configurationName, project.lib("junit-jupiter-params"))

  add(configurationName, project.lib("mockk"))
  add(configurationName, project.lib("byte-buddy")) // Override MockK's old ByteBuddy for Java 25 support
  add(configurationName, project.lib("byte-buddy-agent"))

  add(configurationName, project.lib("commons-io"))
  add(configurationName, project.lib("commons-math3"))

  add(configurationName, project.lib("awaitility"))
  add(configurationName, project.lib("measured"))
}

private fun Scope.configurationName(): String {
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
  implementation(project.lib("kotlin-test"))
  implementation(project.lib("kotlin-test-common"))
  implementation(project.lib("kotlin-test-annotations-common"))

  implementation(project.lib("kotlin-reflect"))

  implementation(project.lib("assertk"))
  implementation(project.lib("kotlinx-coroutines-core"))
  implementation(project.lib("kotlinx-coroutines-test"))

  implementation(project.lib("kotlin-test-junit5"))
  implementation(project.lib("junit-jupiter-api"))
  implementation(project.lib("junit-jupiter-engine"))
  implementation(project.lib("junit-jupiter-params"))

  implementation(project.lib("mockk"))
  implementation(project.lib("byte-buddy")) // Override MockK's old ByteBuddy for Java 25 support
  implementation(project.lib("byte-buddy-agent"))

  implementation(project.lib("commons-io"))
  implementation(project.lib("commons-math3"))

  implementation(project.lib("awaitility"))
  implementation(project.lib("measured"))
}

fun DependencyHandler.addKtorClientDependencies(project: Project, scope: Scope = Scope.Main) {
  val configurationName = scope.configurationName()

  add(configurationName, project.lib("kotlin-reflect"))
  add(configurationName, project.lib("kotlinx-coroutines-core"))
  add(configurationName, project.lib("ktor-client-core"))
  add(configurationName, project.lib("ktor-client-json"))
  add(configurationName, project.lib("ktor-client-serialization"))
  add(configurationName, project.lib("ktor-client-logging"))
  add(configurationName, project.lib("ktor-client-content-negotiation"))
  add(configurationName, project.lib("ktor-serialization-kotlinx"))
  add(configurationName, project.lib("ktor-serialization-kotlinx-json"))
  add(configurationName, project.lib("ktor-client-okhttp"))
}


fun DependencyHandlerScope.addKtorServerDependencies(project: Project, scope: Scope = Scope.Main) {
  val configurationName = scope.configurationName()

  add(configurationName, project.lib("ktor-server-core"))
  add(configurationName, project.lib("ktor-server-netty"))
  add(configurationName, project.lib("kotlinx-coroutines-core"))
  add(configurationName, project.lib("ktor-server"))
  add(configurationName, project.lib("ktor-server-websockets"))
  add(configurationName, project.lib("ktor-server-auth"))
  add(configurationName, project.lib("ktor-server-metrics"))
  add(configurationName, project.lib("ktor-server-call-id"))
  add(configurationName, project.lib("ktor-server-conditional-headers"))
  add(configurationName, project.lib("ktor-serialization-kotlinx"))
  add(configurationName, project.lib("ktor-serialization-kotlinx-json"))
  add(configurationName, project.lib("logback-classic"))
}
