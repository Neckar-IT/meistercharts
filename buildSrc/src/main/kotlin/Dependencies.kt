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

private fun KotlinMultiplatformExtension.js(scope: Scope, configure: KotlinSourceSet.() -> Unit) {
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
fun KotlinMultiplatformExtension.addAnnotationDependencies(scope: Scope = Scope.Main) {
  jvm(scope) {
    dependencies {
      api(Libs.jsr305)
      api(Libs.javax_inject)
      api(Libs.javax_annotation_api)
      api(Libs.annotations)

      //TODO think about changing to compileOnly (runs into strange class not found errors with Kotlin 1.9*)
    }
  }
}


fun DependencyHandlerScope.addKotlinDependencies() {
  //Do nothing,
  //Keep for symmetry
}

/**
 * Add Kotlin related dependencies to the project
 */
fun KotlinMultiplatformExtension.addKotlinDependencies() {
  addAnnotationDependencies(Scope.Main)

  sourceSets {
    jsMain {
      dependencies {
        api(Libs.kotlin_js)
      }
    }
  }
}

object Tests {
  /**
   * Test dependencies using api() - for test-utility projects that export test functionality
   */
  val testDepsCommonApi: KotlinSourceSet.() -> Unit = {
    dependencies {
      api(Libs.kotlin_test)
      api(Libs.kotlin_test_common)
      api(Libs.kotlin_test_annotations_common)

      api(Libs.kotlin_reflect)

      api(KotlinX.coroutines.core)
      api(KotlinX.coroutines.test)

      api(Libs.assertk)
    }
  }

  val testDepsJsApi: KotlinSourceSet.() -> Unit = {
    dependencies {
      api(Libs.kotlin_test_js)
    }
  }

  val testDepsJvmApi: KotlinSourceSet.() -> Unit = {
    dependencies {
      api(Libs.kotlin_test_junit5)
      api(Libs.junit_jupiter_api)
      api(Libs.junit_jupiter_engine)
      api(Libs.junit_jupiter_params)

      api(Libs.mockk)

      api(Libs.commons_io)
      api(Libs.commons_math3)

      api(Libs.awaitility)
      api(Libs.measured)
    }
  }

  /**
   * Test dependencies using implementation() - for test source sets in regular projects
   */
  val testDepsCommonImpl: KotlinSourceSet.() -> Unit = {
    dependencies {
      implementation(Libs.kotlin_test)
      implementation(Libs.kotlin_test_common)
      implementation(Libs.kotlin_test_annotations_common)

      implementation(Libs.kotlin_reflect)

      implementation(KotlinX.coroutines.core)
      implementation(KotlinX.coroutines.test)

      implementation(Libs.assertk)
    }
  }

  val testDepsJsImpl: KotlinSourceSet.() -> Unit = {
    dependencies {
      implementation(Libs.kotlin_test_js)
    }
  }

  val testDepsJvmImpl: KotlinSourceSet.() -> Unit = {
    dependencies {
      implementation(Libs.kotlin_test_junit5)
      implementation(Libs.junit_jupiter_api)
      implementation(Libs.junit_jupiter_engine)
      implementation(Libs.junit_jupiter_params)

      implementation(Libs.mockk)

      implementation(Libs.commons_io)
      implementation(Libs.commons_math3)

      implementation(Libs.awaitility)
      implementation(Libs.measured)
    }
  }
}

/**
 * Adds the test dependencies to the project.
 * - Scope.Test: Uses implementation() - for test source sets in regular projects
 * - Scope.Main: Uses api() - for test-utility projects that export test functionality
 */
fun KotlinMultiplatformExtension.addKotlinTestDependencies(scope: Scope = Scope.Test) {
  when (scope) {
    Scope.Main -> {
      // Test-utility projects need api() to export dependencies transitively
      common(scope, Tests.testDepsCommonApi)
      js(scope, Tests.testDepsJsApi)
      jvm(scope, Tests.testDepsJvmApi)
    }
    Scope.Test -> {
      // Regular projects should use implementation() in test source sets
      common(scope, Tests.testDepsCommonImpl)
      js(scope, Tests.testDepsJsImpl)
      jvm(scope, Tests.testDepsJvmImpl)
    }
  }
}

object KtorClient {
  // Main source set versions using api()
  val commonsApi: KotlinSourceSet.() -> Unit = {
    dependencies {
      api(KotlinX.coroutines.core)

      api(Ktor.client.core)
      api(Ktor.client.json)
      api(Ktor.client.serialization)
      api(Ktor.client.logging)

      api(Libs.ktor_client_content_negotiation)
      api(Libs.ktor_serialization_kotlinx)
      api(Libs.ktor_serialization_kotlinx_json)
    }
  }

  val jsApi: KotlinSourceSet.() -> Unit = {
    dependencies {
      //Nothing to add at the moment
    }
  }

  val jvmApi: KotlinSourceSet.() -> Unit = {
    dependencies {
      api(Ktor.client.okHttp)
    }
  }

  // Test source set versions using implementation()
  val commonsImpl: KotlinSourceSet.() -> Unit = {
    dependencies {
      implementation(KotlinX.coroutines.core)

      implementation(Ktor.client.core)
      implementation(Ktor.client.json)
      implementation(Ktor.client.serialization)
      implementation(Ktor.client.logging)

      implementation(Libs.ktor_client_content_negotiation)
      implementation(Libs.ktor_serialization_kotlinx)
      implementation(Libs.ktor_serialization_kotlinx_json)
    }
  }

  val jsImpl: KotlinSourceSet.() -> Unit = {
    dependencies {
      //Nothing to add at the moment
    }
  }

  val jvmImpl: KotlinSourceSet.() -> Unit = {
    dependencies {
      implementation(Ktor.client.okHttp)
    }
  }
}

/**
 * Adds the ktor client dependencies.
 * - Scope.Main: Uses api() - for exposing dependencies transitively
 * - Scope.Test: Uses implementation() - for test source sets
 */
fun KotlinMultiplatformExtension.addKtorClientDependencies(scope: Scope) {
  when (scope) {
    Scope.Main -> {
      common(scope, KtorClient.commonsApi)
      js(scope, KtorClient.jsApi)
      jvm(scope, KtorClient.jvmApi)
    }
    Scope.Test -> {
      common(scope, KtorClient.commonsImpl)
      js(scope, KtorClient.jsImpl)
      jvm(scope, KtorClient.jvmImpl)
    }
  }
}

object KtorServer {
  val jvm: KotlinSourceSet.() -> Unit = {
    dependencies {
      api(Ktor.server.core)
      api(Ktor.server.netty)

      api(KotlinX.coroutines.core)

      api(Libs.ktor_server)
      api(Libs.ktor_server_websockets)
      api(Libs.ktor_server_auth)
      api(Libs.ktor_server_metrics)
      api(Libs.ktor_server_conditional_headers)

      api(Ktor.server.callId)

      api(Libs.ktor_serialization_kotlinx)
      api(Libs.ktor_serialization_kotlinx_json)

      api(Libs.logback_classic)
    }
  }
}

/**
 * Adds ktor server dependencies
 */
fun KotlinMultiplatformExtension.addKtorServerDependencies(scope: Scope) {
  jvm(scope, KtorServer.jvm)
}

fun DependencyHandlerScope.addAnnotationDependencies(scope: Scope = Scope.Main) {
  val configurationName = scope.configurationName()

  add(configurationName, Libs.jsr305)
  add(configurationName, Libs.javax_inject)
  add(configurationName, Libs.javax_annotation_api)
  add(configurationName, Libs.annotations)
}


/**
 * Adds kotlin test dependencies
 */
fun DependencyHandlerScope.addKotlinTestDependencies(scope: Scope = Scope.Test) {
  val configurationName = scope.configurationName()
  add(configurationName, Libs.kotlin_test)
  add(configurationName, Libs.kotlin_test_common)
  add(configurationName, Libs.kotlin_test_annotations_common)
  add(configurationName, Libs.kotlinx_coroutines_core)
  add(configurationName, Libs.kotlinx_coroutines_test)
  add(configurationName, Libs.kotlinx_coroutines_debug)

  add(configurationName, Libs.kotlin_reflect)
  add(configurationName, Libs.assertk)

  add(configurationName, Libs.kotlin_test_junit5)
  add(configurationName, Libs.junit_jupiter_api)
  add(configurationName, Libs.junit_jupiter_engine)
  add(configurationName, Libs.junit_jupiter_params)

  add(configurationName, Libs.mockk)

  add(configurationName, Libs.commons_io)
  add(configurationName, Libs.commons_math3)

  add(configurationName, Libs.awaitility)
  add(configurationName, Libs.measured)
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
fun JvmComponentDependencies.addKotlinTestDependencies() {
  implementation(Libs.kotlin_test)
  implementation(Libs.kotlin_test_common)
  implementation(Libs.kotlin_test_annotations_common)

  implementation(Libs.kotlin_reflect)

  implementation(Libs.assertk)
  implementation(Libs.kotlinx_coroutines_core)
  implementation(Libs.kotlinx_coroutines_test)

  implementation(Libs.kotlin_test_junit5)
  implementation(Libs.junit_jupiter_api)
  implementation(Libs.junit_jupiter_engine)
  implementation(Libs.junit_jupiter_params)

  implementation(Libs.mockk)

  implementation(Libs.commons_io)
  implementation(Libs.commons_math3)

  implementation(Libs.awaitility)
  implementation(Libs.measured)
}

fun DependencyHandler.addKtorClientDependencies(scope: Scope) {
  val configurationName = scope.configurationName()

  add(configurationName, Libs.kotlin_reflect)
  add(configurationName, Libs.kotlinx_coroutines_core)
  add(configurationName, Libs.ktor_client_core)
  add(configurationName, Libs.ktor_client_json)
  add(configurationName, Libs.ktor_client_serialization)
  add(configurationName, Libs.ktor_client_logging)
  if (false) {
    add(configurationName, Libs.ktor_websockets)
  }
  add(configurationName, Libs.ktor_client_content_negotiation)
  add(configurationName, Libs.ktor_serialization_kotlinx)
  add(configurationName, Libs.ktor_serialization_kotlinx_json)
  add(configurationName, Libs.ktor_client_okhttp)
}


fun DependencyHandlerScope.addKtorServerDependencies(scope: Scope) {
  val configurationName = scope.configurationName()

  add(configurationName, Libs.ktor_server_core)
  add(configurationName, Libs.ktor_server_netty)
  add(configurationName, Libs.kotlinx_coroutines_core)
  add(configurationName, Libs.ktor_server)
  add(configurationName, Libs.ktor_server_websockets)
  add(configurationName, Libs.ktor_server_auth)
  add(configurationName, Libs.ktor_server_metrics)
  add(configurationName, Libs.ktor_server_call_id)
  add(configurationName, Libs.ktor_server_conditional_headers)
  add(configurationName, Libs.ktor_serialization_kotlinx)
  add(configurationName, Libs.ktor_serialization_kotlinx_json)
  add(configurationName, Libs.logback_classic)
}
