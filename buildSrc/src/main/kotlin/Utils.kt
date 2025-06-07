import de.fayard.refreshVersions.core.versionFor
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.JavaExec
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

@Suppress("DEPRECATION")
fun PublishingExtension.configureMavenReposForPublish(project: Project) {
  repositories {
    maven {
      name = if (project.isProjectVersionSnapshot) "SonatypeOssSnapshots" else "SonatypeOssStaging"
      url = if (project.isProjectVersionSnapshot) {
        Repos.sonatype_snapshots
      } else {
        project.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      }

      credentials {
        //Environment variable: ORG_GRADLE_PROJECT_MAVEN_REPO_USER
        username = project.findProperty("MAVEN_REPO_USER") as? String
        //Environment variable: ORG_GRADLE_PROJECT_MAVEN_REPO_PASS
        password = project.findProperty("MAVEN_REPO_PASS") as? String
      }
    }
  }
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
 * Returns the gitlab access token - from environment or gradle.properties file
 */
fun Project.getGitlabAccessToken(): String? {
  val fromEnv: String? = System.getenv("GITLAB_PUSH_ACCESS_TOKEN")

  if (fromEnv != null) {
    return fromEnv
  }

  return properties["GITLAB_PUSH_ACCESS_TOKEN"] as String?
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
 * Configures a project to use Oracle Java 8 (including Java FX)
 */
@Deprecated("no longer supported. Update to 21")
fun Project.configureToolchainJava8WithFx(): Provider<JavaCompiler> {
  //Only Oracle 8 JDK is supported - OpenJDK does *not* contain JavaFX
  return configureToolchain(JavaLanguageVersion.of(8), JvmVendorSpec.ORACLE)
}

/**
 * Configures a project to use Java 8 (not including JavaFX)
 */
@Deprecated("no longer supported. Update to 21")
fun Project.configureToolchainJava8(): Provider<JavaCompiler> {
  //Only Oracle 8 JDK is supported - OpenJDK does *not* contain JavaFX
  return configureToolchain(JavaLanguageVersion.of(8), JvmVendorSpec.ADOPTIUM)
}

/**
 * Configures the toolchain using the [JvmType] - as configured in the project.
 */
fun Project.configureToolchain(jvmType: JvmType) {
  when (jvmType) {
    JvmType.JavaLatestLTS -> {
      configureToolchainJava21LTS()
    }
  }
}

/**
 * Java 17 is a LTS version
 * See https://en.wikipedia.org/wiki/Java_version_history for details
 * Usually one should use [configureToolchainJava21LTS] instead
 */
@Deprecated("Use [configureToolchainJava21LTS] instead in most cases", ReplaceWith("configureToolchainJava21LTS()"))
fun Project.configureToolchainJava17LTS(): Provider<JavaCompiler> {
  return configureToolchain(JavaLanguageVersion.of(17))
}

/**
 * Java 21 is a LTS version
 * See https://en.wikipedia.org/wiki/Java_version_history for details
 */
fun Project.configureToolchainJava21LTS(): Provider<JavaCompiler> {
  return configureToolchain(JavaLanguageVersion.of(21))
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
    languageVersion.set(javaLanguageVersion)
  }

  tasks.withType<JavaCompile>().configureEach {
    this.javaCompiler.set(javaCompiler)
  }
  tasks.withType<JavaExec>().configureEach {
    this.javaLauncher.set(javaToolChainService.launcherFor(javaPluginExtension.toolchain))
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
  languageVersion.set(javaLanguageVersion)
  this.vendor.set(vendor)
  implementation?.let {
    this.implementation.set(it)
  }
}

/**
 * Returns the tools jar path for the compiler provider.
 *
 * ATTENTION: Does only work for Java 8
 */
fun Provider<JavaCompiler>.toolsJarPath(): File {
  return get().toolsJarPath()
}

/**
 * Returns the tools.jar for the installation path of this java compiler
 */
fun JavaCompiler.toolsJarPath(): File {
  val installationPath = this.metadata.installationPath.asFile

  return File(installationPath, "/lib/tools.jar").also {
    require(it.isFile) {
      "No tools.jar found @ <${it.absolutePath}>"
    }
  }
}

/**
 * Configures the Kotlin config todo attempt to add flags here
 */
fun Project.configureKotlin() {
  //Ensure that this is only called once for each project
  require(project.extra.has("kotlinConfigured").not()) {
    "Kotlin already configured for project ${project.path}. Do *not* call configureKotlin() multiple times"
  }
  project.extra["kotlinConfigured"] = true

  //JS projects are no longer supported
  require(extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension>() == null) {
    "Must not contain KotlinJsProjectExtension. Use multiplatform instead"
  }

  tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompile> {
    compilerOptions.freeCompilerArgs.addAllDistinct(KotlinSettings.freeCompilerArgs)
  }

  tasks.withType<KotlinJvmCompile> {
    compilerOptions.freeCompilerArgs.addAllDistinct(KotlinSettings.freeCompilerArgs + KotlinSettings.additionalFreeCompilerArgsJVM)
    compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY //default methods for interfaces
  }

  tasks.withType<KotlinJsCompile> {
    compilerOptions.freeCompilerArgs.addAllDistinct(KotlinSettings.freeCompilerArgs + KotlinSettings.additionalFreeCompilerArgsJS)
  }


  //for common
  this.extensions.findByType<KotlinProjectExtension>()?.applyKotlinConfiguration()

  //For JVM projects
  extensions.findByType<KotlinJvmProjectExtension>()?.applyJvmKotlinConfiguration(suppressWarnings = true)

  //For Multiplatform projects (JS and JVM)
  extensions.findByType<KotlinMultiplatformExtension>()?.applyMultiplatformKotlinConfiguration(this, suppressWarnings = true)
}

/**
 * Adds all elements that are not already in the list
 */
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
 * Configures the node and webpack CLI version to use the version provided by the refreshVersions plugin
 */
fun Project.configureNodeJsRootExtension() {
  allprojects {
    project.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
      project.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().version = versionFor("version.npm.node")
    }
  }
}


/**
 * Configures JUnit
 */
fun Project.configureJunit() {
  tasks.withType<Test>().configureEach {
    useJUnitPlatform {
      includeEngines(
        "junit-jupiter", //JUnit 5 engine
      )
    }

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
    }

    //Set the JVM properties for the tests
    //Set Coroutines Debugging - see https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-d-e-b-u-g_-p-r-o-p-e-r-t-y_-n-a-m-e.html
    systemProperty("kotlinx.coroutines.debug", "on")
  }
}

/**
 * Applies the annotations for the experimental features we are using from Kotlin
 */
fun KotlinProjectExtension.applyKotlinConfiguration() {
  sourceSets.all {
    KotlinSettings.optInExperimentalAnnotations.forEach {
      languageSettings.optIn(it)
    }

    KotlinSettings.languageFeatures.forEach {
      languageSettings.enableLanguageFeature(it)
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
  jsTargetType: JsTargetType = JsTargetType.ES5,

  /**
   * The module type that is used for the webpack configuration.
   */
  webpackModuleType: WebpackModuleType = WebpackModuleType.Window,

  /**
   * The module type that is used for the webpack configuration for dev.
   */
  webpackModuleTypeForDev: WebpackModuleType = WebpackModuleType.Window,

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

  browser {
    runTask {
      webpackModuleType.configure(this, varName)
      additionalRunTaskWebpackConfig()
    }

    webpackTask {
      webpackModuleType.configure(this, varName)
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
 * Applies the (default) configuration for multiplatform projects.
 * Registers both JVM and JS projects
 */
fun KotlinMultiplatformExtension.applyMultiplatformKotlinConfiguration(project: Project, suppressWarnings: Boolean = true) {
  applyDefaultHierarchyTemplate()
  applyKotlinConfiguration()

  compilerOptions {
    this.suppressWarnings = suppressWarnings
    extraWarnings = true
  }

  //Add an JVM configuration
  jvm {
  }

  //Add the JS configuration
  js {
    useEsModules() //if enabled, "*.mjs" files are generated in build/compileSync/js/main/productionExecutable/kotlin

    //Only relevant if `binaries.executable()` is set in the project itself
    generateTypeScriptDefinitions()

    browser {
      configureJsKarma()

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
  }

  //
  // Configure JS modules
  //

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
 * Configures JS test runner using karma
 */
fun org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl.configureJsKarma() {
  testTask {
    configureJsKarma()
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
 * Returns the guessed tag that can/should be used for docker - based on the current branch name
 */
val Project.tagForDocker: String
  get() {
    val tagForDocker: String = branch.encodeForDockerTag()
    require(tagForDocker.isNotBlank()) {
      "Tag for docker must not be blank"
    }

    return tagForDocker
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
val Project.gitCommit: String
  get() {
    return rootProject.extra.get("gitCommit") as? String ?: throw IllegalStateException("Could not find gitCommit in extra")
  }

/**
 * The date of the current git commit
 */
val Project.gitCommitDate: String
  get() {
    return rootProject.extra.get("gitCommitDate") as? String ?: throw IllegalStateException("Could not find gitCommitDate in extra")
  }

/**
 * Describes the current git commit
 */
val Project.gitDescribe: String
  get() {
    return rootProject.extra.get("gitDescribe") as? String ?: throw IllegalStateException("Could not find gitDescribe in extra")
  }

/**
 * The current build date
 */
@Deprecated("use buildDateDay instead to allow for better caching")
val Project.buildDate: String
  get() {
    return rootProject.extra.get("buildDate") as? String ?: throw IllegalStateException("Could not find buildDate in extra")
  }

/**
 * The current build date - without time
 */
val Project.buildDateDay: String
  get() {
    return rootProject.extra.get("buildDateDay") as? String ?: throw IllegalStateException("Could not find buildDateDay in extra")
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
    return ciJobToken ?: throw IllegalStateException("CI_JOB_TOKEN is null")
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
