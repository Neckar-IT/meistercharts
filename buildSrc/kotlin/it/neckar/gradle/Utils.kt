package it.neckar.gradle

import it.neckar.ci.ScheduleVariable
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaCompiler
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.jvm.toolchain.internal.DefaultJvmVendorSpec
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.openjfx.gradle.JavaFXModule
import java.io.File
import java.io.FileFilter
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Contains utility methods that are used in the build.gradle.kts files
 */

/**
 * Returns true if the current version is a snapshot version
 */
@Deprecated("Only for maven deployment!")
val Project.isProjectVersionSnapshot: Boolean
  get() = version.toString().isSnapshot()

/**
 * Returns true if this is a meistercharts version number
 */
val Project.isMeisterchartsSnapshot: Boolean
  get() {
    return meisterchartsVersion.isSnapshot()
  }

/**
 * Returns true if the string ends with "-SNAPSHOT")
 */
fun String.isSnapshot(): Boolean = contains("-SNAPSHOT")

/**
 * Returns true if the project is a project that is published as open source
 */
inline val Project.isOpenSource: Boolean
  get() = path.startsWith(":open:")

/**
 * Converts a Gradle path (containing ":") to a file path (containing "/")
 */
fun String.gradlePathToFilePath(): String {
  return this.replace(':', '/')
}


inline val Project.isIntermediate: Boolean
  get() = this.subprojects.isNotEmpty()


/**
 * Converts a string to camel case format.
 *
 * Example: "hello_world" -> "helloWorld"
 *
 * @return The converted camel case string.
 */
fun String.toCamelCase(): String {
  return this.split("_", "-", " ")
    .joinToString(separator = "") {
      it.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
      }
    }
    .replaceFirstChar { it.lowercase() }
}

/**
 * Converts a string to upper camel case.
 *
 * This function takes a string and converts it to upper camel case by removing underscores and capitalizing
 * the first letter of each word. It uses the default locale to determine the character casing.
 *
 * Example: "hello_world" -> "HelloWorld"
 *
 * @return The string converted to upper camel case.
 */
fun String.toUpperCamelCase(): String {
  return this.split("_", "-", " ")
    .joinToString(separator = "") {
      it.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
      }
    }
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}


/**
 * Lists all svg files within the given folder
 */
fun File.listSvgFilesRecursively(): List<File> {
  val svgFiles = mutableListOf<File>()

  listFiles(DirectoryFileFilter.INSTANCE as FileFilter)?.map {
    svgFiles.addAll(it.listSvgFilesRecursively())
  }

  //Add the files from this directory itself
  svgFiles.addAll(listSvgFilesInDirectory())

  return svgFiles
}

private fun File.listSvgFilesInDirectory(): List<File> {
  return listFiles(".svg")
}

/**
 * Lists all files with the given suffix within this directory
 */
fun File.listFiles(suffix: String): List<File> {
  return listFiles { _, name -> name.lowercase(Locale.getDefault()).endsWith(suffix) }
    ?.sortedBy { it.name }
    ?: throw FileNotFoundException("Could not find source folder <$absolutePath>")
}

/**
 * Returns the base names
 */
fun List<File>.baseNames(): List<String> {
  return map {
    it.nameWithoutExtension
  }
}

/**
 * Creates a new child file
 */
fun File.child(path: String): File {
  return File(this, path)
}

/**
 * Runs a command within the given working directory
 */
fun String.runCommand(workingDir: File?): Int {
  val process = ProcessBuilder(split(" "))
    .directory(workingDir)
    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
    .redirectError(ProcessBuilder.Redirect.INHERIT)
    .start()

  val waitFor = process.waitFor(30, TimeUnit.SECONDS)

  if (!waitFor) {
    throw TimeoutException("process did not terminate within 30s")
  }
  return process.exitValue()
}

/**
 * Runs a command. Returns the result
 */
fun Array<String>.getCmdResult(directory: File?): String {
  val process = exec(directory)
  val result = process.waitFor()
  if (result != 0) {
    val errorContent = process.errorStream.readBytes().decodeToString()
    throw IllegalStateException("Process exited with $result\n$errorContent")
  }
  return process.text
}

fun Array<String>.exec(directory: File?): Process {
  return Runtime.getRuntime().exec(this, emptyArray(), directory)
}

/**
 * Executes this command and waits for success
 *
 * Use [exec] using [Array<String>] instead if possible.
 */
fun String.exec(directory: File? = null) {
  if (directory != null) {
    println("Executing <$this> in ${directory.absolutePath}")
  } else {
    println("Executing <$this>")
  }
  return processBuilder(directory).start().waitForSuccess()
}

/**
 * Wait for success, throws an exception if the return result is not 0
 */
fun Process.waitForSuccess() {
  val result = this.waitFor()

  if (result != 0) {
    throw IllegalStateException("Process failed with $result")
  }
}

fun String.processBuilder(dir: File?): ProcessBuilder = ProcessBuilder("/bin/sh", "-c", this)
  .redirectErrorStream(true)
  .inheritIO()
  .directory(dir)


val Process.text: String
  get() {
    return inputStream.bufferedReader().use {
      val line = it.readLine()
      requireNotNull(line) {
        "Process input stream did not return any values"
      }
      line
    }.trim()
  }

/**
 * Returns the gitlab access token - from environment or gradle.properties file.
 * Checks for GITLAB_API_TOKEN first, falls back to legacy GITLAB_CONTAINER_ACCESS_TOKEN.
 */
fun Project.getGitlabAccessToken(): String? {
  val fromEnv = System.getenv("GITLAB_API_TOKEN")
    ?: System.getenv("GITLAB_CONTAINER_ACCESS_TOKEN")

  if (fromEnv != null) {
    return fromEnv
  }

  return (properties["GITLAB_API_TOKEN"] ?: properties["GITLAB_CONTAINER_ACCESS_TOKEN"]) as String?
}


fun ZipInputStream.forEachEntry(block: (entry: ZipEntry, stream: InputStream) -> Unit) {
  var entry: ZipEntry?
  while (run {
      entry = nextEntry
      entry
    } != null) {
    try {
      block(entry as ZipEntry, this)
    } finally {
      this.closeEntry()
    }
  }
}

/**
 * Executes the given block for the given task
 */
fun Project.withTask(name: String, block: (Task) -> Unit) {
  tasks.findByName(name)?.let(block) ?: tasks.whenTaskAdded {
    if (this.name == name) {
      block(this)
    }
  }
}

/**
 * The `build` lifecycle task — contributed by any language plugin or [Plugins.base].
 * Resolving it fails at configuration time instead of task-graph time (#2668).
 */
val Project.buildTask: TaskProvider<Task>
  get() = tasks.named("build")

/**
 * Configures JavaFX for this project
 */
fun Project.configureJavaFX(modules: List<JavaFXModule> = listOf(JavaFXModule.CONTROLS)) {
  plugins.apply(Plugins.javafx)

  val javaFxOptions: org.openjfx.gradle.JavaFXOptions = extensions.getByType(org.openjfx.gradle.JavaFXOptions::class.java)

  val javaPluginExtension = extensions.getByType(JavaPluginExtension::class.java)
  val languageVersion = javaPluginExtension.toolchain.languageVersion.get()

  javaFxOptions.version = languageVersion.asInt().toString()
  javaFxOptions.modules = modules.map { it.moduleName }
}

/**
 * Configures the toolchain using the [JvmType] - as configured in the project.
 */
fun Project.configureToolchain(jvmType: JvmType) {
  when (jvmType) {
    JvmType.JavaLatestLTS -> {
      configureToolchainJava25LTS()
    }
  }
}

/**
 * Java 25 is a LTS version
 * See https://en.wikipedia.org/wiki/Java_version_history for details
 */
fun Project.configureToolchainJava25LTS(): Provider<JavaCompiler> {
  return configureToolchain(JavaLanguageVersion.of(25))
}

/**
 * Configures the toolchain
 */
fun Project.configureToolchain(javaLanguageVersion: JavaLanguageVersion, vendor: JvmVendorSpec = DefaultJvmVendorSpec.any(), implementation: JvmImplementation? = null): Provider<JavaCompiler> {
  configure<JavaPluginExtension> {
    toolchain {
      configureJavaToolchain(javaLanguageVersion, vendor, implementation)
    }
  }

  val javaPluginExtension = extensions.getByType(JavaPluginExtension::class.java)
  val javaToolChainService = extensions.getByType(JavaToolchainService::class.java)
  val javaCompiler = javaToolChainService.compilerFor {
    languageVersion = javaLanguageVersion
  }

  tasks.withType<JavaCompile>().configureEach {
    this.javaCompiler = javaCompiler
  }
  tasks.withType<JavaExec>().configureEach {
    this.javaLauncher = javaToolChainService.launcherFor(javaPluginExtension.toolchain)
  }

  extensions.findByName("kotlin")?.let {
    (it as KotlinProjectExtension).jvmToolchain {
      configureJavaToolchain(javaLanguageVersion, vendor, implementation)
    }
  }

  //Set the SDK based upon the language level
  extensions.findByType<org.gradle.plugins.ide.idea.model.IdeaModel>()?.let {
    it.module {
      jdkName = "${javaLanguageVersion.asInt()}"
      languageLevel = IdeaLanguageLevel(javaLanguageVersion.asInt())
    }
  }

  return javaCompiler
}

/**
 * Configures the java tool chain
 */
fun JavaToolchainSpec.configureJavaToolchain(javaLanguageVersion: JavaLanguageVersion, vendor: JvmVendorSpec, implementation: JvmImplementation?) {
  languageVersion = javaLanguageVersion
  this.vendor = vendor
  implementation?.let {
    this.implementation = it
  }
}

/**
 * Whether `-Xenhanced-coroutines-debugging` is enabled for this build, controlled by the
 * `-PenhancedCoroutinesDebugging` Gradle property. Present (any value other than `false`) enables it;
 * absent disables it. Off by default — see [KotlinSettings.additionalFreeCompilerArgsJVM].
 */
fun Project.isEnhancedCoroutinesDebuggingEnabled(): Boolean =
  providers.gradleProperty("enhancedCoroutinesDebugging").orNull?.let { it != "false" } ?: false

/**
 * Configures the Kotlin compiler options for a project — JVM, multiplatform, KSP processor alike.
 *
 * The `KotlinJvmCompile` / `KotlinJsCompile` blocks match no task in a module without the
 * corresponding target, so one function serves every target combination.
 */
fun Project.configureKotlin() {
  //Ensure that this is only called once for each project
  require(project.extra.has("kotlinConfigured").not()) {
    "Kotlin already configured for project ${project.path}. Do *not* call configureKotlin() multiple times"
  }
  project.extra["kotlinConfigured"] = true

  // JS-only projects ("kotlin-js" plugin) are unrepresentable since Kotlin 2.4
  // (KotlinJsProjectExtension removed). No runtime guard needed.

  tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompile> {
    compilerOptions.freeCompilerArgs.addAllDistinct(KotlinSettings.freeCompilerArgs)
    compilerOptions.freeCompilerArgs.add(explicitReturnTypesArgFor(this))
  }

  tasks.withType<KotlinJvmCompile> {
    compilerOptions.freeCompilerArgs.addAllDistinct(KotlinSettings.freeCompilerArgs + KotlinSettings.additionalFreeCompilerArgsJVM(project.isEnhancedCoroutinesDebuggingEnabled()))
    compilerOptions.freeCompilerArgs.add(explicitReturnTypesArgFor(this))
    compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY //default methods for interfaces
  }

  tasks.withType<KotlinJsCompile> {
    compilerOptions.freeCompilerArgs.addAllDistinct(KotlinSettings.freeCompilerArgs + KotlinSettings.additionalFreeCompilerArgsJS)
    compilerOptions.freeCompilerArgs.add(explicitReturnTypesArgFor(this))
  }


  //for common
  this.extensions.findByType<KotlinProjectExtension>()?.applyKotlinConfiguration()

  //For JVM projects
  extensions.findByType<KotlinJvmProjectExtension>()?.applyJvmKotlinConfiguration(suppressWarnings = true)
}

/**
 * Adds all elements that are not already in the list
 */
/**
 * The `-XXexplicit-return-types` value for [compileTask], as a provider: a module sets
 * `kotlin { explicitApi() }` in its own build script, which runs after `configureKotlin`.
 *
 * Follows `-Xexplicit-api`, which the compiler rejects at a different value than this one — but only
 * where that flag applies. The Kotlin plugin exempts test sources from explicit-api mode, so a strict
 * value on a test compilation would demand return types on `@Test fun`s that no consumer ever sees,
 * and would clash with the disabled explicit-api of that same compilation.
 */
private fun Project.explicitReturnTypesArgFor(compileTask: Task): Provider<String> = provider {
  KotlinSettings.explicitReturnTypesArg(usesExplicitApiStrict() && compileTask.isTestCompilation().not())
}

/**
 * Whether this project turned on `kotlin { explicitApi() }` in strict mode.
 */
private fun Project.usesExplicitApiStrict(): Boolean =
  extensions.findByType<KotlinProjectExtension>()?.explicitApi == org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict

/**
 * Whether this compile task builds test sources — `compileTestKotlin`, `compileTestKotlinJvm`,
 * `compileTestFixturesKotlin`. Production tasks (`compileKotlinJvm`, `compileCommonMainKotlinMetadata`)
 * never carry `Test` in their name.
 */
private fun Task.isTestCompilation(): Boolean = name.contains("Test")

private fun ListProperty<String>.addAllDistinct(elements: List<String>) {
  val newElements = get().toMutableSet()
  newElements.addAll(elements)
  set(newElements)
}

/**
 * Returns true if this is a multiplatform project
 */
fun Project.isMultiplatform(): Boolean {
  return extensions.findByType<KotlinMultiplatformExtension>() != null
}

/**
 * Configures the node and webpack CLI version from the npm version catalog.
 */
fun Project.configureNodeJsRootExtension() {
  allprojects {
    project.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
      project.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().version = npmVersion("node")
    }

    // The wasmJs target provisions its own node distribution via WasmNodeJsEnvSpec.
    // Its default (node 25.0.0 in Kotlin 2.4.0) links against libatomic.so.1, which the
    // CI runner image does not ship — :kotlinWasmNpmInstall then dies with exit code 127.
    // Pin the same catalog version as the JS target so both share one known-good distribution.
    project.plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin> {
      project.the<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec>().version = npmVersion("node")
    }
  }

  // Make root :kotlinNpmInstall depend on root :kotlinNodeJsSetup so the node distribution
  // is downloaded into the root .gradle/nodejs/ before npm install tries to exec it.
  // Without this edge, a fresh CI runner that invokes only subproject :build tasks (e.g.
  // integration-product-build.yml) executes per-subproject :kotlinNodeJsSetup tasks that
  // install into the subproject's own .gradle/nodejs/, while root :kotlinNpmInstall still
  // looks in the root .gradle/nodejs/ — leaving its node binary path non-existent.
  tasks.matching { it.name == "kotlinNpmInstall" }.configureEach {
    dependsOn(tasks.matching { it.name == "kotlinNodeJsSetup" })
  }

  // The Lizergy planner-ui (maintenance-only) pulls in react-json-view 1.21.3,
  // whose declared peer-dep is React <= 17. The workspace ships React 19.
  // Yarn 1.22 only warns on this; npm hard-fails by default. Match the
  // resolution semantics of the previous (yarn-based) setup by enabling
  // npm's --legacy-peer-deps for the Kotlin/JS NPM install step.
  rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>()
      .npmInstallTaskProvider
      .configure { args.add("--legacy-peer-deps") }
  }
}


/**
 * The JUnit Jupiter property that holds the timeout for test methods — `@Test`, `@ParameterizedTest`
 * and the other testable methods, but **not** `@BeforeEach` / `@BeforeAll` / `@AfterEach`.
 *
 * Set by [configureJunit] as a hang guard, removed again by [removePerTestTimeout].
 */
const val JunitPerTestTimeoutProperty: String = "junit.jupiter.execution.timeout.testable.method.default"

/**
 * The JUnit Jupiter property that holds the timeout for lifecycle methods (`@BeforeEach`,
 * `@BeforeAll`, `@AfterEach`, `@AfterAll`).
 *
 * Separate from [JunitPerTestTimeoutProperty] because the two guard different things. A test method
 * that runs long is a hanging test. A lifecycle method that runs long is usually infrastructure
 * coming up — a Testcontainers container, and on a cold Docker cache that includes pulling its image.
 * That happens on the runner hosts every day: their maintenance cron runs
 * `docker image prune -a --force --filter 'until=24h'`, which removes tagged images no container is
 * using, and a test image is only in use while a test runs.
 *
 * Sharing one 120s budget made an image pull abort the first Testcontainers test of the day, and the
 * resulting `TimeoutException` pointed at the test rather than at the pull (#2701).
 */
const val JunitLifecycleTimeoutProperty: String = "junit.jupiter.execution.timeout.lifecycle.method.default"

/**
 * Removes both hang guards [configureJunit] applies to every [Test] task.
 *
 * Separate test suites are the designated place for slow work — browsers, external services,
 * performance measurements — and legitimately run longer than the guards. Aborting them is
 * always a false positive, so the suites opt out instead of raising the guard for everyone.
 *
 * Must be called after [configureJunit] has been applied to the task, which is the case for every
 * suite registered from a project's build script.
 */
fun Test.removePerTestTimeout() {
  systemProperties.remove(JunitPerTestTimeoutProperty)
  systemProperties.remove(JunitLifecycleTimeoutProperty)
}

/**
 * Configures JUnit
 */
fun Project.configureJunit() {
  val skipSlowTests = resolveConfigValueOrNull("skipSlowTests") != null
  if (skipSlowTests) {
    logSlowTestSkippingOnce()
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform {
      includeEngines(
        "junit-jupiter", //JUnit 5 engine
      )

      //Exclude slow tests in CI MR pipelines for faster feedback
      //Tests marked with @SlowTest annotation (uses "slow-test" tag) are skipped
      if (skipSlowTests) {
        excludeTags("slow-test")
      }
    }

    //Enable HTML reports
    reports.html.required.set(true)

    filter {
      includeTestsMatching("*Test")
      includeTestsMatching("*Tests")
      includeTestsMatching("*IT")
      isFailOnNoMatchingTests = false //if there are no tests defined in a project, do *not* fail
    }

    //Show the stack traces of the failing tests on the console
    testLogging {
      events.add(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED)
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
      showStandardStreams = false //do not show standard streams (stdout/stderr) by default
    }

    //Per-test-method timeout: a safety net against a single hanging test blocking the whole
    //build (a runaway test used to stall the main pipeline for tens of minutes). 120s is far
    //above any legitimate unit/functional test — anything slower belongs in a separate
    //test suite, which opts out via `removePerTestTimeout()`.
    //`disabled_on_debug` keeps interactive debug sessions alive.
    systemProperty(JunitPerTestTimeoutProperty, "120s")

    //Lifecycle methods (@BeforeEach/@BeforeAll/@AfterEach/@AfterAll) get their own, larger budget:
    //they are where infrastructure starts, and a Testcontainers container on a cold Docker cache has
    //to pull its image first. The runner hosts prune unused tagged images daily, so that cold start
    //is the normal case for the first container test after 06:00 — a pull that the 120s test budget
    //aborted, reporting a TimeoutException on the test instead of the pull (#2701).
    //Still bounded: a lifecycle method that hangs indefinitely must not stall the build either.
    systemProperty(JunitLifecycleTimeoutProperty, "600s")
    systemProperty("junit.jupiter.execution.timeout.mode", "disabled_on_debug")

    //Set the JVM properties for the tests
    //Set Coroutines Debugging - see https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-d-e-b-u-g_-p-r-o-p-e-r-t-y_-n-a-m-e.html
    systemProperty("kotlinx.coroutines.debug", "on")
    jvmArgs("-Dkotlinx.coroutines.debug=on")

    //Testcontainers: Disable Ryuk resource reaper.
    //With the Singleton Container pattern, containers are shared across test classes
    //and cleaned up at JVM shutdown. Ryuk is not needed and can cause issues in CI.
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")
    environment("TESTCONTAINERS_RYUK_VERBOSE", "true")
  }
}

/**
 * Logs the "@SlowTest tests are being skipped" notice exactly once per Gradle invocation.
 *
 * `configureJunit()` runs in every project that has JVM tests, so a naive log call inside
 * the JUnit configuration block fires 80+ times per build — once per Test task per project.
 * Routing through a `rootProject.extra` flag de-duplicates to a single message.
 */
private fun Project.logSlowTestSkippingOnce() {
  val key = "skipSlowTestsLogged"
  if (rootProject.extra.has(key)) return
  rootProject.extra[key] = true
  rootProject.logger.lifecycle(
    "Skipping @SlowTest tests across all modules (remove -PskipSlowTests to include them)",
  )
}

/**
 * Applies the annotations for the experimental features we are using from Kotlin
 */
fun KotlinProjectExtension.applyKotlinConfiguration() {
  sourceSets.all {
    KotlinSettings.optInExperimentalAnnotations.forEach {
      languageSettings.optIn(it)
    }

    languageSettings {
      progressiveMode = true
      languageVersion = KotlinSettings.languageVersionAsString
      apiVersion = KotlinSettings.apiVersionAsString
    }
  }
}

/**
 * Applies the kotlin configuration to the JVM target
 */
fun KotlinJvmProjectExtension.applyJvmKotlinConfiguration(suppressWarnings: Boolean) {
  compilerOptions {
    languageVersion = KotlinSettings.languageVersion
    apiVersion = KotlinSettings.apiVersion
    progressiveMode = true
    optIn = KotlinSettings.optInExperimentalAnnotations
    javaParameters = true
    this.suppressWarnings = suppressWarnings
    extraWarnings = true
  }

  applyKotlinConfiguration()
}

/**
 * Configures the Kotlin JS target to produce an executable JS browser application.
 * This application can be embedded using a simple script tag in an HTML file.
 *
 * The main function is called when the application is loaded.
 */
fun KotlinJsTargetDsl.executableJsApplication(
  /**
   * The name of the variable that is used to access the library in the browser.
   * Is used to register the library in the global scope.
   */
  varName: String,

  /**
   * THe js target type - default is ES5
   */
  jsTargetType: JsTargetType = JsTargetType.ES2015,

  /**
   * The module type that is used for the webpack configuration.
   */
  webpackModuleType: WebpackModuleType = WebpackModuleType.Window,

  /**
   * The module type that is used for the webpack configuration for dev.
   */
  webpackModuleTypeForDev: WebpackModuleType = WebpackModuleType.Window,

  /**
   * Controls test execution for projects using ES modules (ModernModule).
   * - [JsTestMode.Disabled]: Tests are disabled
   * - [JsTestMode.NodeJs]: Tests run in Node.js with Mocha (supports ES modules natively)
   */
  testMode: JsTestMode = JsTestMode.NodeJs,

  /**
   * Optional configuration for the webpack task.
   * Can be used to add additional configuration to the webpack task.
   *
   * Use with care!
   * It should not be necessary in most cases
   */
  additionalRunTaskWebpackConfig: KotlinWebpack.() -> Unit = {},
) {
  require(varName.isNotBlank()) {
    "varName must not be blank"
  }

  binaries.executable()

  val usesEsModules = webpackModuleType == WebpackModuleType.ModernModule || webpackModuleTypeForDev == WebpackModuleType.ModernModule

  browser {
    runTask {
      webpackModuleType.configure(this, varName)
      additionalRunTaskWebpackConfig()
    }

    webpackTask {
      webpackModuleType.configure(this, varName)
    }

    // Disable browser tests for ES modules - Karma cannot execute ESM
    // For browser-specific tests (Canvas, DOM), use WebpackModuleType.Window instead
    testTask {
      enabled = false
    }

    // Enable outputModule experiment for webpack when using ES modules
    if (usesEsModules) {
      commonWebpackConfig {
        experiments.add("outputModule")
      }
    }
  }


  // Configure Node.js tests when NodeJs test mode is selected
  if (testMode == JsTestMode.NodeJs) {
    nodejs {
      testTask {
        useMocha()
      }
    }
  }


  //Workaround for problem when executing `gradle build jsBrowserDevelopmentWebpack` since Gradle 8.3
  run {
    val tasks = project.tasks

    //These tasks are only available if binaries.executable() is called in the project itself
    val jsBrowserProductionWebpack = tasks.findByName("jsBrowserProductionWebpack").requireNotNull() as KotlinWebpack
    val jsBrowserDevelopmentWebpack = tasks.findByName("jsBrowserDevelopmentWebpack").requireNotNull() as KotlinWebpack
    val jsDevelopmentExecutableCompileSync = tasks.findByName("jsDevelopmentExecutableCompileSync").requireNotNull()
    val jsProductionExecutableCompileSync = tasks.findByName("jsProductionExecutableCompileSync").requireNotNull()
    val jsBrowserDevelopmentRun = tasks.findByName("jsBrowserDevelopmentRun").requireNotNull() as KotlinWebpack

    //Find KSP extension and add dependencies between tasks - if necessary
    project.fixKspTaskDependencies()

    //Add these artificial deps to work around issue
    jsBrowserProductionWebpack.mustRunAfter(jsDevelopmentExecutableCompileSync)
    jsBrowserProductionWebpack.mustRunAfter(jsProductionExecutableCompileSync)

    jsBrowserDevelopmentWebpack.mustRunAfter(jsDevelopmentExecutableCompileSync)
    jsBrowserDevelopmentWebpack.mustRunAfter(jsProductionExecutableCompileSync)

    jsBrowserDevelopmentRun.mustRunAfter(jsDevelopmentExecutableCompileSync)
    jsBrowserDevelopmentRun.mustRunAfter(jsProductionExecutableCompileSync)

    webpackModuleType.configure(jsBrowserProductionWebpack, varName)
    webpackModuleTypeForDev.configure(jsBrowserDevelopmentWebpack, varName)
    webpackModuleTypeForDev.configure(jsBrowserDevelopmentWebpack, varName)
  }

  project.tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
      target = jsTargetType.value
      //target = "es2015"
    }
  }

  // Register a convenient 'run' task as alias for jsBrowserProductionRun
  if (project.tasks.findByName("run") == null) {
    project.tasks.register("run") {
      group = "application"
      description = "Runs the JS browser application in production mode (alias for jsBrowserProductionRun)"
      dependsOn("jsBrowserProductionRun")
    }
  }
}

/**
 * Controls how tests are executed for JS projects using ES modules (ModernModule).
 *
 * ## Important Limitation
 * Browser tests (Karma) are NOT compatible with ES modules (ModernModule) because:
 * 1. Karma cannot execute ESM syntax
 * 2. The Kotlin/JS plugin automatically enables `outputModule` webpack experiment
 *
 * For projects needing browser-specific tests (Canvas, DOM), use [WebpackModuleType.Window]
 * instead of [WebpackModuleType.ModernModule].
 */
enum class JsTestMode {
  /**
   * Tests are disabled. Use this for backwards compatibility or when tests are not needed.
   */
  Disabled,

  /**
   * Tests run in Node.js using Mocha.
   * Node.js has native ES module support, making it ideal for testing ES module code.
   *
   * Note: Browser-specific APIs (Canvas, DOM, localStorage, etc.) are NOT available.
   * For Canvas tests, consider using the `canvas` npm package as a polyfill.
   */
  NodeJs,
}

/**
 * The target for [KotlinJsCompile] tasks
 */
enum class JsTargetType(val value: String) {
  /**
   * This is the default value
   */
  ES5("es5"),

  /**
   * Should provide additional features:
   * https://kotlinlang.org/docs/js-project-setup.html#support-for-es2015-features
   */
  ES2015("es2015"),
}

enum class WebpackModuleType {
  Var {
    override fun configure(kotlinWebpack: KotlinWebpack, varName: String) {
      kotlinWebpack.output.library = varName
      kotlinWebpack.output.libraryTarget = "var"
      kotlinWebpack.devtool = "source-map"
    }
  },

  /**
   * Seems to work fine, when running a Kotlin application that is just initialized/started by a single JS statement
   */
  Window {
    override fun configure(kotlinWebpack: KotlinWebpack, varName: String) {
      kotlinWebpack.output.library = varName
      kotlinWebpack.output.libraryTarget = "window"
      kotlinWebpack.devtool = "source-map"
    }
  },
  Umd {
    override fun configure(kotlinWebpack: KotlinWebpack, varName: String) {
      kotlinWebpack.output.library = varName
      kotlinWebpack.output.libraryTarget = "umd"
      kotlinWebpack.devtool = "source-map"
    }
  },
  Umd2 {
    override fun configure(kotlinWebpack: KotlinWebpack, varName: String) {
      kotlinWebpack.output.library = varName
      kotlinWebpack.output.libraryTarget = "umd2"
      kotlinWebpack.devtool = "source-map"
    }
  },

  /**
   * Seems to work when using the generated code as TypeScript module.
   * Note: This requires experiments.outputModule to be enabled in commonWebpackConfig
   */
  ModernModule {
    override fun configure(kotlinWebpack: KotlinWebpack, varName: String) {
      kotlinWebpack.output.library = null // do *not* set a library name
      kotlinWebpack.output.libraryTarget = "modern-module"
      kotlinWebpack.devtool = "source-map"
    }
  },

  ;

  abstract fun configure(kotlinWebpack: KotlinWebpack, varName: String)
}

/**
 * Applies the shared configuration for multiplatform projects.
 *
 * Declares no target: which platforms a module builds for is its registered target set
 * (`multiplatform(path, …)` in the project registry), applied by `declareTarget` per entry.
 */
fun KotlinMultiplatformExtension.applyMultiplatformKotlinConfiguration(suppressWarnings: Boolean = true) {
  applyKotlinConfiguration()

  compilerOptions {
    this.suppressWarnings = suppressWarnings
    extraWarnings = true
  }
}

/**
 * Declares the `jvm` target.
 */
fun KotlinMultiplatformExtension.declareJvmTarget() {
  jvm {
  }
}

/**
 * Declares the `js` target: ES modules, Node.js/Mocha tests, and a webpack setup emitting a
 * modern module.
 */
fun KotlinMultiplatformExtension.declareJsTarget(project: Project) {
  js {
    useEsModules() //if enabled, "*.mjs" files are generated in build/compileSync/js/main/productionExecutable/kotlin

    //Only relevant if `binaries.executable()` is set in the project itself
    generateTypeScriptDefinitions()

    browser {
      // Disable JS browser tests - Karma cannot execute ES modules
      // JS tests run in Node.js with Mocha instead (see nodejs block below)
      configureJsKarma(disableTests = true)

      webpackTask {
        output.library = null //necessary when using modern-module
        output.libraryTarget = "modern-module"
      }

      commonWebpackConfig {
        //devtool = WebpackDevtool.SOURCE_MAP

        cssSupport {
          enabled = true //enable CSS support for all tasks (https://kotlinlang.org/docs/js-project-setup.html#building-executables)
        }

        //Required for "modern-module" support
        experiments.add("outputModule")
      }
    }

    // Enable Node.js tests with Mocha - Node.js supports ES modules natively
    // Note: Browser-specific APIs (Canvas, DOM) are not available in Node.js
    nodejs {
      testTask {
        useMocha()
      }
    }
  }

  //Configure JS Compile tasks to use ES2015 modules
  //It seems necessary to configure this *after* calling useEsModules() on the JS target (to avoid overwriting with "es5")
  project.tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
      // https://kotlinlang.org/docs/js-project-setup.html#support-for-es2015-features
      target = "es2015"
    }
  }
}

/**
 * Declares the `linuxX64` target.
 */
fun KotlinMultiplatformExtension.declareLinuxX64Target() {
  linuxX64()
}

/**
 * Returns true if the opt-in `wasmJs` target is enabled for this build: `-PwasmJs=true`
 * (or `wasmJs=true` in gradle.properties).
 *
 * Default is OFF: the wasmJs target is perspective work (issue #1261) that no product needs yet.
 * Disabled, the build skips all wasm tasks (~830 tasks / ~9% of the full build task graph).
 * The shared webMain source sets stay active - they are compiled and tested by the js target.
 */
fun Project.isWasmJsEnabled(): Boolean {
  return findProperty("wasmJs")?.toString().toBoolean()
}

/**
 * Declares the `wasmJs` browser target — but only when the build runs with `-PwasmJs=true`
 * (see [isWasmJsEnabled]), otherwise a no-op. A module reaches this by registering
 * [it.neckar.projects.KotlinTarget.WasmJs]; registering it keeps the target requirement from
 * cascading onto every multiplatform module in the build, because a wasmJs consumer requires
 * every commonMain dependency to also expose a wasmJs variant.
 *
 * Browser tests are disabled: the wasm test code is still compiled (which verifies wasmJs
 * source-compatibility), but not executed, so CI needs no browser to run the build.
 */
@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.declareWasmJsTarget(project: Project) {
  if (project.isWasmJsEnabled().not()) {
    return
  }

  wasmJs {
    browser {
      testTask {
        enabled = false
      }
    }
  }
}

/**
 * Configures the module's own additions to the `wasmJs` target — a browser [executable] and
 * `wasmJsMain` [wasmJsMainDependencies]. A no-op when the target does not exist, so a module build
 * script never tests [isWasmJsEnabled] itself.
 *
 * Only for what a module needs on top of the shared setup; the target itself comes from the
 * registered [it.neckar.projects.KotlinTarget.WasmJs].
 */
@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.configureWasmJsTarget(
  project: Project,
  /**
   * Whether to build a browser executable (webpack bundle) for the wasmJs target
   */
  executable: Boolean = false,
  /**
   * Dependencies for the wasmJsMain source set. Passed as lambda so the module build files
   * never have to test the wasmJs flag themselves - the source-set accessor only exists
   * when the target is registered.
   */
  wasmJsMainDependencies: (org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.() -> Unit)? = null,
) {
  if (project.isWasmJsEnabled().not()) {
    return
  }

  wasmJs {
    if (executable) {
      binaries.executable()
    }
  }

  if (wasmJsMainDependencies != null) {
    sourceSets.getByName("wasmJsMain").dependencies(wasmJsMainDependencies)
  }
}

/**
 * Configures JS test runner using karma.
 * Note: When ES modules are used (via useEsModules()), Karma tests are disabled
 * because Karma doesn't support ES module syntax.
 */
fun org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl.configureJsKarma(disableTests: Boolean = false) {
  testTask {
    if (disableTests) {
      enabled = false
    } else {
      configureJsKarma()
    }
  }
}

/**
 * Configures JS with karma
 */
fun org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest.configureJsKarma() {
  useKarma {
    if (project.inCi || project.inContainer) {
      useChromeHeadlessNoSandbox()
    } else {
      useChromeHeadless()
    }
  }
}

/**
 * Formats a long value in bytes as megabytes
 */
fun Long.formatAsMegaBytes(): String {
  return String.format("%,.2f", this / 1024.0 / 1024.0)
}

/**
 * Returns the current branch
 */
val Project.branch: String
  get() {
    return rootProject.extra.get("branch") as? String ?: throw IllegalStateException("Could not find branch in extra")
  }

/**
 * Returns the branch-based Docker tag (mutable alias like `main` or `feature_xyz`).
 * Used in docker-compose templates; continuous deploy (#2341) pulls this tag on rollout.
 */
val Project.branchTagForDocker: String
  get() {
    val branchTag: String = branch.encodeForDockerTag()
    require(branchTag.isNotBlank()) {
      "Branch tag for docker must not be blank"
    }

    return branchTag
  }

/**
 * Returns the immutable Docker tag in the format `YYYYMMDD-shortsha` (e.g. `20260326-a1b2c3d`).
 *
 * Uses `gitHashShort`, produced by `git rev-parse --short=12 HEAD` — pinned to 12 characters
 * so the tag does not depend on the repository's object count (CI clone vs. local repo).
 * The date component is the commit date (from [gitCommitDateTime]), not the build date, so the tag
 * is a pure function of the commit: rebuilding the same commit yields the same immutable tag.
 */
val Project.immutableDockerTag: String
  get() {
    val date = gitCommitDateTime.substringBefore("T").replace("-", "")
    require(gitHashShort.length >= 7) {
      "Git short hash too short: '$gitHashShort' (expected at least 7 characters)"
    }
    return "$date-$gitHashShort"
  }

/**
 * Returns true if the current branch is the master/main branch
 */
val Project.onMainBranch: Boolean
  get() {
    return rootProject.branch == "master" || rootProject.branch == "main"
  }

/**
 * Returns the sha1 of the current git commit
 */
val Project.gitHash: String
  get() {
    return rootProject.extra.get("gitHash") as? String ?: throw IllegalStateException("Could not find gitHash in extra")
  }

val Project.gitHashShort: String
  get() {
    return rootProject.extra.get("gitHashShort") as? String ?: throw IllegalStateException("Could not find gitHashShort in extra")
  }

/**
 * The date and time of the current git commit
 */
val Project.gitCommitDateTime: String
  get() {
    return rootProject.extra.get("gitCommitDateTime") as? String ?: throw IllegalStateException("Could not find gitCommitDateTime in extra")
  }
/**
 * The build date (day only), derived from the last commit date — NOT the wall clock.
 *
 * Stable per commit: identical inputs produce identical outputs, so it is safe to embed into
 * manifests, expanded resources and generated .env files. It is the date component of
 * [gitCommitDateTime]. Defined in the root build.gradle.kts (#792).
 */
val Project.buildDate: String
  get() {
    return rootProject.extra.get("buildDate") as? String ?: throw IllegalStateException("Could not find buildDate in extra")
  }

/**
 * The current build date (initialized in /build.gradle.kts)
 */
val Project.inIde: Boolean
  get() {
    return rootProject.extra.get("inIde") as? Boolean ?: throw IllegalStateException("Could not find inIde in extra")
  }

/**
 * The CI information (initialized in /build.gradle.kts)
 */
val Project.ciInformation: GitlabCiInformation
  get() {
    return rootProject.extra.get("ciInformation") as? GitlabCiInformation ?: throw IllegalStateException("Could not find ciInformation in extra")
  }

/**
 * Returns true if running in Continuous Integration
 */
val Project.inCi: Boolean
  get() {
    return ciInformation.inCi
  }

/**
 * Returns true if the given pipeline-schedule env variable is set to `"true"`.
 *
 * Use the typed [ScheduleVariable] from [ScheduleVariable].
 */
fun Project.inSchedule(scheduleVariable: ScheduleVariable): Boolean {
  return ciInformation.inSchedule(scheduleVariable.variableName)
}

/** Extra-property key: the schedule variable gating a module's `jib`. Set by [gateJibToSchedule], read by `verifyDeploymentImages`. */
const val ScheduleGatedJibExtraKey: String = "scheduleGatedJibVariable"

/**
 * Runs the module's `jib` only in [scheduleVariable]'s scheduled pipeline (always outside CI), so a
 * main-push jib cannot overwrite the schedule's image with an empty build. Records the gate as an
 * extra property so `verifyDeploymentImages` rejects pairing it with `continuousDeploy()` — that
 * would deploy an image the main pipeline never builds (#2694). [reason] shows when the task skips.
 */
fun Project.gateJibToSchedule(scheduleVariable: ScheduleVariable, reason: String) {
  val gatingProject: Project = this
  extra[ScheduleGatedJibExtraKey] = scheduleVariable.variableName
  tasks.named("jib").configure {
    onlyIf(reason) { gatingProject.inCi.not() || gatingProject.inSchedule(scheduleVariable) }
  }
}

val Project.inContainer: Boolean
  get() {
    return devContainerInformation.inDockerContainer
  }

/**
 * Returns true if this process is running in a development container
 */
val Project.devContainerInformation: DevContainerInformation
  get() {
    return rootProject.extra.get("devContainerInformation") as? DevContainerInformation ?: throw IllegalStateException("Could not find DevContainerInformation in extra")
  }

/**
 * Retrieves the CI_JOB_TOKEN, throws an exception if the token is null.
 */
val Project.ciJobToken: String
  get() {
    val ciJobToken: String? = System.getenv("CI_JOB_TOKEN")
    require(ciJobToken.isNullOrBlank().not()) {
      "CI_JOB_TOKEN is blank"
    }
    return ciJobToken
  }

/**
 * The MeisterCharts version
 */
val Project.meisterchartsVersion: String
  get() {
    return rootProject.extra.get("meisterchartsVersion") as? String ?: throw IllegalStateException("Could not find meisterchartsVersion in extra")
  }

@Deprecated("use this.encodeForDockerTag() instead!", ReplaceWith("this.encodeForDockerTag()"))
fun String.safeForDockerTag(): String {
  return this.encodeForDockerTag()
}

/**
 * Replaces unsafe characters that must not be used in docker tags
 */
fun String.encodeForDockerTag(): String {
  return replace('/', '_')
    .replace(':', '_')
    .replace('.', '_')
    .replace(' ', '_')
}


/**
 * Declares both ends of the Orval code generation that runs inside this task — typically
 * `pnpmRunBuild`, whose `pnpm run build:noDeps` starts with `openapi:convert`:
 *
 * - **Input**: the OpenAPI specs produced by the `openapiSpec` task of every module in
 *   [backendModulePaths] (their `build/generated/openapi` directories). Pass more than one for
 *   packages that merge several backend specs before generating, as `typescript-utils` does via
 *   `openapi-merge.sh`. Once `pnpmRunBuild` declares inputs/outputs (issue #2501), it would
 *   otherwise report the build UP-TO-DATE after a backend API change and ship stale generated
 *   types.
 * - **Output**: this project's `build/generated/open-api` directory, where Orval writes the
 *   generated TypeScript types. `pnpmRunBuild` otherwise only declares `dist`, which lives in
 *   the project root and therefore survives `gradle clean`. With the generated types undeclared,
 *   `gradle clean && gradle build` left `dist` in place, found every input unchanged, reported
 *   `pnpmRunBuild` UP-TO-DATE, and never re-ran Orval — so `build/generated/open-api` stayed
 *   deleted. A build-cache hit had the same effect, restoring `dist` alone.
 *
 * The build-order `dependsOn("<backendModulePath>:openapiSpec")` is declared separately in each
 * consuming `build.gradle.kts`; an input directory alone does not create a task dependency. Mirrors
 * the wiring in `OrvalConvertPlugin`, which declares the same two directories for the dev-loop
 * `orvalConvert` task.
 */
fun Task.declaresOrvalCodegen(vararg backendModulePaths: String) {
  require(backendModulePaths.isNotEmpty()) {
    "Expected at least one backend module path for the Orval codegen inputs of <$path>"
  }

  val openapiSpecDirs = backendModulePaths.map { backendModulePath ->
    project.rootProject.project(backendModulePath)
      .layout.buildDirectory.dir("generated/openapi")
  }

  inputs.files(openapiSpecDirs)
    .withPropertyName("backendOpenApiSpecs")
    .withPathSensitivity(PathSensitivity.RELATIVE)

  outputs.dir(project.layout.buildDirectory.dir("generated/open-api"))
    .withPropertyName("orvalGeneratedTypes")
}

/**
 * Returns the instance for the given project
 */
inline fun <reified T> Project.getOrPut(key: String, defaultValue: () -> T): T {
  if (extra.has(key)) {
    val found = extra.get(key)
    return found as T
  }

  val value = defaultValue()
  extra.set(key, value)
  return value
}

/**
 * Returns this string, or null if it is null or blank.
 *
 * build-logic cannot depend on the project's kotlin-lang module, so this mirrors
 * `it.neckar.open.kotlin.lang.nullIfBlank`.
 */
fun String?.nullIfBlank(): String? = takeUnless { it.isNullOrBlank() }
