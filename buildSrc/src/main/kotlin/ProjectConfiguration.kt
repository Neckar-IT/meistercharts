@file:Suppress("UNUSED_VARIABLE")

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.pnpm.task.PnpmInstallTask
import com.github.gradle.node.pnpm.task.PnpmTask
import com.github.gradle.node.task.NodeSetupTask
import de.fayard.refreshVersions.core.versionFor
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import it.neckar.gradle.ansiConsole
import it.neckar.gradle.console
import it.neckar.gradle.pnpm.dependency.PackageNameRegistry
import it.neckar.gradle.pnpm.dependency.PnpmWorkspaceDependencyResolver
import it.neckar.gradle.pnpm.vite.GenerateViteEnvFilePlugin
import it.neckar.gradle.pnpm.packagejson.GeneratePackageJsonPlugin
import it.neckar.gradle.pnpm.workspace.GeneratePnpmWorkspaceYamlPlugin
import it.neckar.gradle.python.PythonPluginExtension
import it.neckar.gradle.ssl.mkcert.CertificatesPlugin
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.serialization.json.jsonObject
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrCompilation
import org.jetbrains.kotlin.gradle.targets.js.npm.PackageJson
import java.io.ByteArrayOutputStream
import java.io.File


/**
 * Contains common code to configure a project
 */
object ProjectConfiguration {
  /**
   * Cached [PackageNameRegistry] - lazy singleton to avoid repeated scanning of all pnpm projects.
   * The registry maps npm package names to Gradle project paths.
   */
  private val packageNameRegistry by lazy {
    PackageNameRegistry.create()
  }
  fun configureParentProject(project: Project) {
    with(project) {
      apply(plugin = Plugins.kover)
      apply(plugin = Plugins.publishToGitlabPages)

      mergeKoverReports()
    }
  }

  /**
   * Configures a JVM project - with the current Java version (Java 25)
   */
  fun configureJvm(project: Project) {
    with(project) {
      configureJvmCommon()
      configureToolchainJava25LTS()
    }
  }

  /**
   * Configures KSP processor projects (JVM projects)
   */
  fun configureKspProcessor(project: Project) {
    with(project) {
      configureJvmCommon()
      configureToolchainJava25LTS()

      project.afterEvaluate {
        val projectDependencies = findAllProjectDependencies(listOf("api", "runtimeClasspath", "compileOnly"))

        val notAllowedProjectDependencies = projectDependencies
          .filter { it != project } //skip this project
          .filter { it.isKspProcessorProject().not() } //skip all KSP processor projects
          .filter { it.path.startsWith(":internal:open:ksp").not() } //skip all ksp related projects (e.g. model)
          .filter {
            //Allowlist of projects that are allowed to be dependencies of KSP processor projects (annotations)
            Projects.allowedDependenciesForKspProcessingProjectPaths.contains(it.path).not()
          }

        if (notAllowedProjectDependencies.isNotEmpty()) {
          throw GradleException("Project dependencies not allowed for KSP processor project [${project.path}]: but has ${notAllowedProjectDependencies.map { it.path }}")
        }
      }
    }
  }

  private var dokkaEnabled = false

  private fun Project.configureJvmCommon() {
    run {
      //Ensure there are no "invalid" source directories - this might happen when a project has been converted from a multiplatform project
      //Verify that the directory is empty
      requireDirectoryEmpty("src/commonMain/kotlin")
      requireDirectoryEmpty("src/jsMain/kotlin")
      requireDirectoryEmpty("src/jvmMain/kotlin")
      requireDirectoryEmpty("src/jvmTest/resources")
      requireDirectoryEmpty("src/jsTest/resources")
    }

    apply(plugin = Plugins.java)
    apply(plugin = Plugins.javaLibrary)
    apply(plugin = Plugins.kotlinJvm)
    if (dokkaEnabled) {
      apply(plugin = Plugins.dokka)
    }
    apply(plugin = Plugins.detekt)
    apply(plugin = Plugins.kover)
    apply(plugin = Plugins.publishToGitlabPages)

    configureKotlin()

    configureJunit()

    //Create sources jar
    (extensions.findByName("sourceSets") as SourceSetContainer?)?.let {
      tasks.register<Jar>("sourcesJar") {
        group = "Build"
        description = "Assembles the sources jar."

        dependsOn("jar")
        from(it.getByName<SourceSet>("main").allSource)
        archiveClassifier = "sources"
      }
      tasks.register<Jar>("javadocJar") {
        group = "Build"
        description = "Assembles the javadoc/dokka jar."

        dependsOn("jar")
        if (dokkaEnabled) {
          from(tasks.named("dokkaHtml"))
        }
        archiveClassifier = "javadoc"
      }

      artifacts {
        //archives(tasks.getByName("sourcesJar"))
        add("archives", tasks.named("sourcesJar"))
        //Results in a NPE
        //archives(tasks.getByName("javadocJar"))
      }
    }

    tasks.named<Jar>("jar") {
      manifest {
        attributes(
          "Created-By" to "Neckar IT GmbH",
          "Project" to project.name,
          BuildInfoVars.BuildDate.value to buildDate,
          BuildInfoVars.GitCommitDateTime.value to gitCommitDateTime,
          BuildInfoVars.GitHash.value to gitHash,
          BuildInfoVars.GitHashShort.value to gitHashShort,
        )
      }
    }

    /**
     * Alias: Allows a call to `gradle sourcesJar` at the root
     */
    tasks.register("jvmSourcesJar") {
      dependsOn("sourcesJar")
    }

    configureDetekt {
      source.setFrom(
        files(
          "src/main/kotlin",
          "src/main/java",
        )
      )
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
      // exclude generated code (does not seem to work)
      exclude("build/generated/**")
      exclude("**/build/generated/**")
      exclude("**/build/**")
    }

    configureKover {
    }
  }

  fun configureMultiPlatform(project: Project, jvmType: JvmType) {
    with(project) {

      run {
        //Ensure there are no "forgotten" source directories - this might happen when a project is converted from a JVM or JS project
        //Verify that the directory is empty
        requireDirectoryEmpty("src/main/kotlin")
        requireDirectoryEmpty("src/main/java")
        requireDirectoryEmpty("src/main/resources")
        requireDirectoryEmpty("src/test/kotlin")
        requireDirectoryEmpty("src/test/java")
        requireDirectoryEmpty("src/test/resources")
      }

      //Report generation is not yet working
      apply(plugin = Plugins.kotlinMultiPlatform)
      if (dokkaEnabled) {
        apply(plugin = Plugins.dokka)
      }
      apply(plugin = Plugins.detekt)
      apply(plugin = Plugins.kover)

      apply(plugin = Plugins.publishToGitlabPages)

      //tasks.register<Jar>("javadocJar") {
      //  group = "Build"
      //  description = "Assembles the javadoc/dokka jar."
      //
      //  dependsOn("build")
      //  from(tasks.named<DokkaTask>("dokka"))
      //  archiveClassifier.set("javadoc")
      //}

      configureKotlin()
      configureJunit()

      //Ensure the extension exist
      requireNotNull(extensions.getByType(KotlinMultiplatformExtension::class.java))

      //Default toolchain for multiplatform projects

      configureToolchain(jvmType)

      configureDetekt {
        source.setFrom(
          files(
            "src/commonMain/kotlin",
            "src/jsMain/kotlin",
            "src/jvmMain/kotlin",
          )
        )
      }

      configureKover {
      }

      project.tasks.register("printSourceSets") {
        doLast {
          val ansiConsole = console

          logger.lifecycle("------------------------------------------------------------")
          logger.lifecycle(ansiConsole.green("Source Sets:"))
          logger.lifecycle("------------------------------------------------------------")

          val kotlinMultiplatformExtension: KotlinMultiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

          kotlinMultiplatformExtension.sourceSets.all {
            logger.lifecycle(ansiConsole.orange(name))

            logger.lifecycle("  ${ansiConsole.gray("Source Dirs:")}")
            this.kotlin.srcDirs.forEach {
              logger.lifecycle("    ${ansiConsole.white(it.relativeTo(project.projectDir))}")
            }

            logger.lifecycle("  ${ansiConsole.gray("Resource Dirs:")}")
            this.resources.srcDirs.forEach {
              logger.lifecycle("    ${ansiConsole.white(it.relativeTo(project.projectDir))}")
            }
          }
        }
      }
    }
  }

  /**
   * Configuration for python projects
   */
  fun configurePython(project: Project) {
    /**
     * The python configuration plugin is applied
     */
    with(project) {
      plugins.apply(Plugins.python)

      extensions.getByType<PythonPluginExtension>().apply {
        //This is the default python executable required for all AI projects
        pythonExecutable = "python3.12"
      }
    }
  }

  fun configurePythonRoot(project: Project) {
    with(project) {
      require(project.rootProject == project) {
        "This method must only be called on the root project"
      }

      tasks.register("createPythonVersionFile") {
        group = "Python"
        description = "Creates the .python-version file"

        doLast {
          file(".python-version").writeText(PythonSettings.Version)

          file("tools/pyenv/install.sh").let { installShFile ->
            installShFile.parentFile.mkdirs() //Ensure the parent directory exists

            installShFile.writeText(
              //language=bash
              """
              #!/usr/bin/env sh
              set -e
              pyenv install ${PythonSettings.Version}
            """.trimIndent()
            )

            installShFile.setExecutable(true)
          }
        }
      }
    }
  }

  /**
   * Applies the default pnpm configuration.
   * Both for the root project *and* the pnpm projects
   */
  internal fun configureDefaultPnpm(project: Project) {
    with(project) {
      plugins.apply(Plugins.base)
      plugins.apply(Plugins.node)

      //Disable unused tasks
      listOf("npmInstall", "npmSetup", "yarn", "yarnSetup").forEach {
        tasks.named(it).configure {
          enabled = false
        }
      }

      extensions.getByType<NodeExtension>().let { nodeExtension ->
        nodeExtension.yarnWorkDir.set(layout.buildDirectory.dir("node/yarn"))
        nodeExtension.pnpmWorkDir.set(layout.buildDirectory.dir("node/pnpm"))
        nodeExtension.npmWorkDir.set(layout.buildDirectory.dir("node/npm"))

        nodeExtension.download = true
        nodeExtension.version = versionFor("version.npm.node") //Node
        nodeExtension.pnpmVersion = versionFor("version.npm.pnpm") //PNPM
      }

      val pnpmInstall = tasks.named("pnpmInstall", PnpmInstallTask::class) {
        dependsOn(GeneratePackageJsonPlugin.GeneratePackageJsonTaskName) //Generate the package.json first
      }

      tasks.named<Delete>("clean") {
        delete("dist", "build", "node_modules", ".astro", ".gradle")
      }

      tasks.register<Exec>("printPnpmVersion") {
        // Setze das Kommando, um die PNPM-Version zu überprüfen
        commandLine("pnpm", "--version")

        val out = ByteArrayOutputStream()
        standardOutput = out

        doLast {
          logger.lifecycle("PNPM: ${console.green(out.toString().trim())}")
        }
      }

      tasks.register<Exec>("verifyPnpmVersion") {
        val out = ByteArrayOutputStream()
        standardOutput = out

        // Setze das Kommando, um die PNPM-Version zu überprüfen
        commandLine("pnpm", "--version")

        doLast {
          val nodeExtension = this.project.extensions.getByType<NodeExtension>()

          val actualPnpmVersion = out.toString().trim()
          val expectedPnpmVersion = nodeExtension.pnpmVersion.get()

          logger.lifecycle("            Actual | Expected")
          logger.lifecycle("------------------------------------------------------------")

          if (actualPnpmVersion == expectedPnpmVersion) {
            logger.lifecycle(console.blue("PNPM: ✓ ${console.green(out.toString().trim())}"))
          } else {
            logger.lifecycle("PNPM:      ${console.orange("✗")} ${console.orange(out.toString().trim())} | ${console.blue(expectedPnpmVersion.trim())}")
            logger.lifecycle("Execute ${console.blue("npm install  -g pnpm")}")
          }
        }
      }
    }
  }

  /**
   * Configures the pnpm root project
   */
  fun configurePnpmRoot(project: Project) {
    with(project) {
      require(project.rootProject == project) {
        "This method must only be called on the root project"
      }

      configureDefaultPnpm(project)

      project.plugins.apply(Plugins.generatePackageJson)
      project.plugins.apply(Plugins.installPnpmDependency)
      project.plugins.apply(Plugins.generatePnpmWorkspaceYaml)

      val pnpmInstallTask = tasks.named("pnpmInstall")
      pnpmInstallTask.configure {
        outputs.upToDateWhen { false } //Always execute the task. Necessary because the node_modules folders in the subprojects are not detected automatically
        doNotTrackState("pnpm manages the state itself")

        //Ensure the workspace.yaml file is generated before the `pnpmInstall` task is executed (in the root project)
        dependsOn(tasks.named(GeneratePnpmWorkspaceYamlPlugin.GenerateWorkspaceYamlTaskName))

        //Ensure that the package.json files are generated before the `pnpmInstall` task is executed (in the root project)
        project.subprojects{
          this.tasks.findNamed(GeneratePackageJsonPlugin.GeneratePackageJsonTaskName)?.let {
            dependsOn(it)
          }
        }
      }

      //Add dependencies
      pnpmInstallTask.configure {
        //This npm bundle content is part of the pnpm workspace. It is necessary to build before calling pnpm install
        //dependsOn(":internal:open:meistercharts:meistercharts-api:meistercharts-easy-api:npmBundleDevelopment")
        dependsOn(project(Projects.meistercharts_api_easy).tasks.getByName(it.neckar.gradle.npmbundle.NpmBundlePlugin.NpmBundleTaskName))
        dependsOn(project(Projects.meistercharts_api_easy).tasks.getByName(it.neckar.gradle.npmbundle.NpmBundlePlugin.NpmBundleDevelopmentTaskName))
      }

      //Special task for update versions - must be called after the versions.properties file has been updated
      tasks.register("updateYarnLock") {
        group = "Pnpm"
        description = "Updates the yarn.lock files - for Kotlin an PNPM"

        dependsOn(":kotlinUpgradeYarnLock")
        dependsOn(pnpmInstallTask)
      }

      tasks.register("createNvmConfiguration") {
        group = "Pnpm"
        description = "Creates the .nvmrc file"

        doLast {
          file(".nvmrc").writeText("v" + versionFor("version.npm.node"))

          file("tools/nvm/install.sh").let { installShFile ->
            installShFile.writeText(
              //language=bash
              """
              #!/usr/bin/env sh

              set -e

              nvm install
              nvm use

              npm install -g pnpm@${versionFor("version.npm.pnpm")}
            """.trimIndent()
            )

            installShFile.setExecutable(true)
          }
        }
      }.also {
        pnpmInstallTask.configure {
          dependsOn(it)
        }
      }
    }
  }

  /**
   * Configures a PNPM project - *not* the root project
   */
  fun configurePnpm(project: Project) {
    with(project) {
      run {
        //Ensure there are no "invalid" source directories - this might happen when a project is converted from a multiplatform project
        //Verify that the directory is empty
        requireDirectoryEmpty("src/commonMain/kotlin")
        requireDirectoryEmpty("src/jsMain/kotlin")
        requireDirectoryEmpty("src/jvmMain/kotlin")
        requireDirectoryEmpty("src/jvmTest/resources")
        requireDirectoryEmpty("src/jsTest/resources")
        requireDirectoryEmpty("src/main/kotlin")
        requireDirectoryEmpty("src/main/java")
        requireDirectoryEmpty("src/main/resources")
        requireDirectoryEmpty("src/test/kotlin")
        requireDirectoryEmpty("src/test/java")
        requireDirectoryEmpty("src/test/resources")
      }

      //Configure IntelliJ IDEA
      extensions.getByType<org.gradle.plugins.ide.idea.model.IdeaModel>().apply {
        module {
          sourceDirs.add(project.file("src"))
          sourceDirs.add(project.file(".ladle"))
          testSources.from(project.file("tests"))
          testSources.from(project.file("mock"))
        }
      }

      configureDefaultPnpm(project)

      /**
       * Apply the package.json generator plugin to be able to generate the package.json file with the correct version numbers
       */
      project.plugins.apply(Plugins.generatePackageJson)
      project.plugins.apply(Plugins.generateViteEnvFile)
      project.plugins.apply(Plugins.installPnpmDependency)
      project.plugins.apply(Plugins.certificates)

      //Ensure the package.json file is generated before the `pnpmInstall` task is executed (in the root project)
      rootProject.tasks.named("pnpmInstall").configure {
        dependsOn(tasks.named(GeneratePackageJsonPlugin.GeneratePackageJsonTaskName))
      }

      //Disable the pnpmInstall task for *this* project
      val warningTask = tasks.register("pnpmInstall - do not use") {
        group = "Pnpm"
        description = "use pnpmInstall at root project instead"

        doLast {
          logger.lifecycle(ansiConsole.yellow("-------------------------------------------------------------------"))
          logger.lifecycle(ansiConsole.yellow("pnpmInstall is disabled for this project. Use :pnpmInstall instead."))
          logger.lifecycle(ansiConsole.yellow("-------------------------------------------------------------------"))
        }
      }

      val verifyProjectConfiguration = tasks.register("verifyProjectConfiguration") {
        description = "Verifies the configuration of the project"

        doLast {
          val tsConfig = file("tsconfig.json")
          if (tsConfig.isFile.not()) {
            logger.error(ansiConsole.red("Missing ${tsConfig.absolutePath}. A tsconfig.json file is required for each PNPM project."))
            throw GradleException("Expected ${tsConfig.absolutePath} to be a file")
          }
        }
      }

      //Skip the pnpmInstall task in the project, delegate to the root project
      tasks.named("pnpmInstall").configure {
        dependsOn(":pnpmInstall", warningTask, verifyProjectConfiguration)
        enabled = false
      }

      run {
        //TODO disable both tasks and use the binaries from the root project

        tasks.named("pnpmSetup").configure {
          dependsOn(":pnpmSetup", verifyProjectConfiguration)
        }
        tasks.named<NodeSetupTask>("nodeSetup").configure {
          dependsOn(":nodeSetup", verifyProjectConfiguration)
        }
      }

      val pnpmRunBuild: TaskProvider<PnpmTask> = tasks.register<PnpmTask>("pnpmRunBuild") {
        description = "Executes `pnpm run build:only` (or `build` as fallback)"

        dependsOn(":pnpmInstall", verifyProjectConfiguration) //implicit dependency to generatePackageJson
        dependsOn(tasks.named(GenerateViteEnvFilePlugin.GenerateEnvFileTaskName)) //Generate .env file with Git info
        dependsOn(CertificatesPlugin.GenerateCertTaskName) //Create the certificate early - at least necessary for vite projects

        // Automatic workspace dependency resolution - Gradle orchestrates build order
        val resolver = PnpmWorkspaceDependencyResolver(packageNameRegistry)
        val deps = resolver.resolveWorkspaceDependenciesByType(project)

        // Include both dependencies AND devDependencies - devDependencies can provide
        // build-relevant artifacts (shared configs, TypeScript types)
        val allDeps = (deps.dependencies + deps.devDependencies).distinctBy { it.path }

        allDeps
          .filter { it.path != project.path } // Filter self-dependency
          .forEach { gradlePath ->
            project.rootProject.findProject(gradlePath.path)?.let { depProject ->
              dependsOn(depProject.tasks.named("pnpmRunBuild"))
            }
          }

        // Script selection: prefer build:only, fallback to build with warning
        val scripts = parsePackageJson().jsonObject["scripts"]?.jsonObject
        val hasBuildOnly = scripts?.containsKey("build:only") == true
        val hasBuild = scripts?.containsKey("build") == true

        onlyIf {
          hasBuildOnly || hasBuild
        }

        args = if (hasBuildOnly) {
          listOf("run", "build:only")
        } else {
          // TODO: Remove fallback after all projects have build:only (see MR !11806)
          if (hasBuild) {
            logger.warn("${project.path}: no 'build:only' script, falling back to 'build'")
          }
          listOf("run", "build")
        }

        //Disable output if *info* is not enabled
        //if (logger.isInfoEnabled.not()) {
        //  execOverrides {
        //    standardOutput = stdOutByteArray
        //    errorOutput = errOutByteArray
        //  }
        //}

        doFirst {
          //ensure node_modules exists
          val dir = file("node_modules")
          require(dir.isDirectory) {
            "Expected ${dir.absolutePath} to be a directory. Maybe :pnpmInstall didn't run?"
          }
        }

        doLast {
          val distDir = file("dist")

          if (distDir.isDirectory.not()) {
            logger.error(ansiConsole.red("Expected ${distDir.absolutePath} to be a directory"))
            throw GradleException("Expected ${distDir.absolutePath} to be a directory")
          }

          distDir.listFiles().let { files ->
            if (files == null) {
              logger.error(ansiConsole.red("Expected ${distDir.absolutePath} to contain files - but could not list"))
              throw GradleException("Expected ${distDir.absolutePath} to contain files - but could not list")
            }

            //Ensure that the dist directory contains at least 2 files, else the build is considered failed
            val expectedMinFileCount = 2
            if (files.size < expectedMinFileCount) {
              logger.error(ansiConsole.red("Expected ${distDir.absolutePath} to contain at least $expectedMinFileCount files. But only got ${files.size}"))
              throw GradleException("Expected ${distDir.absolutePath} to contain at least $expectedMinFileCount files. But only got ${files.size}")
            }
          }
        }
      }

      val buildTask = tasks.named("build") {
        dependsOn(pnpmRunBuild)
      }

      tasks.register<PnpmTask>("jsRun") {
        description = "Executes `pnpm run dev`"
        group = "Pnpm"

        dependsOn(buildTask)
        args = listOf("run", "dev")
      }

      tasks.register<PnpmTask>("pnpmLint") {
        description = "Executes `pnpm run lint`"
        group = "Pnpm"

        dependsOn(":pnpmInstall", "build") //implicit dependency to generatePackageJson

        onlyIf {
          //Check if there is a build script referenced
          packageJsonContainsScript("lint")
        }

        args = listOf("run", "lint")

        //Capture output to display on failure
        ignoreExitValue = true //handle the exit value ourselves
        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        execOverrides {
          standardOutput = stdOut
          errorOutput = stdErr
        }

        doLast {
          val exitValue = result?.exitValue ?: 0
          if (exitValue != 0) {
            val stdOutContent = stdOut.toString(Charsets.UTF_8)
            val stdErrContent = stdErr.toString(Charsets.UTF_8)

            if (stdOutContent.isNotBlank()) {
              logger.error("stdout:\n$stdOutContent")
            }
            if (stdErrContent.isNotBlank()) {
              logger.error("stderr:\n$stdErrContent")
            }

            logger.error("pnpmLint failed with exit code $exitValue")
            throw GradleException("pnpmLint failed with exit code $exitValue")
          }
        }
      }

      tasks.register<PnpmTask>("pnpmLintFix") {
        description = "Executes `pnpm run lint`"
        group = "Pnpm"

        dependsOn(":pnpmInstall", "build") //implicit dependency to generatePackageJson

        onlyIf {
          //Check if there is a build script referenced
          packageJsonContainsScript("lint")
        }

        args = listOf("run", "lint", "--fix")
      }

      tasks.register<Exec>("pnpmLintHtmlReport") {
        description = "Generates HTML lint report using ESLint"
        group = "Pnpm"

        dependsOn(":pnpmInstall", "build")

        onlyIf {
          packageJsonContainsScript("lint")
        }

        val reportDir = project.layout.buildDirectory.dir("reports/eslint").get().asFile
        val reportFile = File(reportDir, "lint-report.html")

        doFirst {
          reportDir.mkdirs()
        }

        commandLine("npx", "eslint", ".", "--format", "html", "--output-file", reportFile.absolutePath)

        // Don't fail if ESLint finds issues - we just want the report
        isIgnoreExitValue = true

        doLast {
          if (reportFile.exists()) {
            logger.lifecycle("HTML lint report generated: ${reportFile.absolutePath}")
          }
        }
      }

      tasks.register<PnpmTask>("pnpmPrettier") {
        description = "Executes `pnpm run prettier`"
        group = "Pnpm"

        dependsOn(":pnpmInstall", "build") //implicit dependency to generatePackageJson

        onlyIf {
          //Check if there is a build script referenced
          packageJsonContainsScript("prettier")
        }

        args = listOf("run", "prettier")

        //Capture output to display on failure
        ignoreExitValue = true //handle the exit value ourselves
        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        execOverrides {
          standardOutput = stdOut
          errorOutput = stdErr
        }

        doLast {
          val exitValue = result?.exitValue ?: 0
          if (exitValue != 0) {
            val stdOutContent = stdOut.toString(Charsets.UTF_8)
            val stdErrContent = stdErr.toString(Charsets.UTF_8)

            if (stdOutContent.isNotBlank()) {
              logger.error("stdout:\n$stdOutContent")
            }
            if (stdErrContent.isNotBlank()) {
              logger.error("stderr:\n$stdErrContent")
            }

            logger.error("pnpmPrettier failed with exit code $exitValue")
            throw GradleException("pnpmPrettier failed with exit code $exitValue")
          }
        }
      }

      tasks.register<PnpmTask>("pnpmPrettierFix") {
        description = "Executes `pnpm run prettier:fix`"
        group = "Pnpm"

        dependsOn(":pnpmInstall", "build") //implicit dependency to generatePackageJson

        onlyIf {
          //Check if there is a build script referenced
          packageJsonContainsScript("prettier:fix")
        }

        args = listOf("run", "prettier:fix")
      }


      tasks.register<PnpmTask>("pnpmTest") {
        description = "Executes `pnpm run test`"
        group = "Pnpm"

        dependsOn(":pnpmInstall") //implicit dependency to generatePackageJson

        onlyIf {
          //Check if there is a build script referenced
          packageJsonContainsScript("test")
        }

        args = listOf("run", "test")

        //Capture output to display on failure
        ignoreExitValue = true //handle the exit value ourselves
        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        execOverrides {
          standardOutput = stdOut
          errorOutput = stdErr
        }

        doLast {
          val exitValue = result?.exitValue ?: 0
          if (exitValue != 0) {
            val stdOutContent = stdOut.toString(Charsets.UTF_8)
            val stdErrContent = stdErr.toString(Charsets.UTF_8)

            if (stdOutContent.isNotBlank()) {
              logger.error("stdout:\n$stdOutContent")
            }
            if (stdErrContent.isNotBlank()) {
              logger.error("stderr:\n$stdErrContent")
            }

            logger.error("pnpmTest failed with exit code $exitValue")
            throw GradleException("pnpmTest failed with exit code $exitValue")
          }
        }
      }

      /**
       * Therefore, it is necessary to update the configuration
       */
      tasks.named<Zip>("zipDistributions") {
        from("dist")

        doLast {
          val outputFile = file("build/distributions.zip")
          require(outputFile.isFile) {
            "Expected ${outputFile.absolutePath} to be a file"
          }
        }
      }

      tasks.register("pnpmDeps") {
        description = "Shows the workspace dependency tree of this pnpm project"
        group = "Pnpm"

        doLast {
          val startTime = System.currentTimeMillis()

          val resolver = PnpmWorkspaceDependencyResolver()

          /**
           * Prints a dependency tree.
           * @param useDevDependencies if true, follows devDependencies for transitive resolution; if false, follows dependencies
           */
          fun printDependencyTree(
            dependencies: List<GradleProjectPath>,
            useDevDependencies: Boolean,
            indent: String = "",
            visited: MutableSet<String> = mutableSetOf(),
          ) {
            dependencies.forEachIndexed { index, gradlePath ->
              val isLast = index == dependencies.lastIndex
              val isVisited = gradlePath.path in visited
              val marker = if (isVisited) " (*)" else ""
              val prefix = if (isLast) "└── " else "├── "
              val childIndent = if (isLast) "$indent    " else "$indent│   "

              logger.lifecycle("$indent$prefix${gradlePath.path}$marker")

              if (isVisited.not()) {
                visited.add(gradlePath.path)
                val depProject = project.rootProject.findProject(gradlePath.path)
                if (depProject != null) {
                  val childDeps = resolver.resolveWorkspaceDependenciesByType(depProject)
                  val relevantChildDeps = if (useDevDependencies) childDeps.devDependencies else childDeps.dependencies
                  printDependencyTree(relevantChildDeps, useDevDependencies, childIndent, visited)
                }
              }
            }
          }

          val deps = resolver.resolveWorkspaceDependenciesByType(project)

          logger.lifecycle("")
          logger.lifecycle("Workspace dependencies for ${project.path}:")
          logger.lifecycle("")

          if (deps.dependencies.isNotEmpty()) {
            logger.lifecycle(ansiConsole.green("dependencies:"))
            printDependencyTree(deps.dependencies, useDevDependencies = false)
          } else {
            logger.lifecycle(ansiConsole.gray("dependencies: (none)"))
          }

          logger.lifecycle("")

          if (deps.devDependencies.isNotEmpty()) {
            logger.lifecycle(ansiConsole.blue("devDependencies:"))
            printDependencyTree(deps.devDependencies, useDevDependencies = true)
          } else {
            logger.lifecycle(ansiConsole.gray("devDependencies: (none)"))
          }

          val durationMs = System.currentTimeMillis() - startTime
          logger.lifecycle("")
          logger.lifecycle(ansiConsole.gray("Resolved in ${durationMs}ms"))
        }
      }
    }
  }

  /**
   * Throws an exception if the provided directory is not empty
   */
  private fun Project.requireDirectoryEmpty(relativePath: String) {
    val dir = file(relativePath)

    require(dir.listFiles().isNullOrEmpty()) {
      "$relativePath must be empty in ${project.path} (${dir.absolutePath})"
    }
  }
}

fun Project.configureDetekt(additionalConfig: DetektExtension.() -> Unit) {
  extensions.getByType(DetektExtension::class.java).apply {
    config.from(rootProject.files("config/detekt/detekt.yml"))

    parallel = true
    buildUponDefaultConfig = true
    //autoCorrect = true

    additionalConfig()
  }

  plugins.withType(io.gitlab.arturbosch.detekt.DetektPlugin::class) {
    tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class) {
      reports {
        //xml.required.set(true)
        html.required.set(true)
      }
    }
  }

  //Remove detekt from check task
  tasks.named("check") {
    this.setDependsOn(this.dependsOn.filterNot {
      it is TaskProvider<*> && it.name.contains("detekt")
    })
  }
}

/**
 * Executes the given [function] on the [packageJson] object for the "main" compilation target.
 */
fun KotlinJsTargetDsl.packageJson(function: PackageJson.() -> Unit) {
  val kotlinJsIrCompilation: KotlinJsIrCompilation = compilations["main"]
  kotlinJsIrCompilation.packageJson(function)
}

/**
 * Configures the Kover plugin for "normal" projects.
 * The configuration is applied to the [KoverProjectExtension].
 */
fun Project.configureKover(additionalConfig: KoverProjectExtension.() -> Unit) {
  val koverProjectExtension = extensions.getByType<KoverProjectExtension>()
  koverProjectExtension.additionalConfig()
}
