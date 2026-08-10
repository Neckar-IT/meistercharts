package it.neckar.gradle

import it.neckar.gradle.ansiConsole
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * The build information variables behind [GitProperty] — [value] is the key each one is written
 * under, in the fat-jar `app-git-info.properties` resource and in `window.__APP_GIT_INFO__`.
 *
 * One constant per [GitProperty], no more: the only way into [getBuildInfoVarValue] is a
 * [GitProperty.buildInfoVar], so a constant without a property is a value nothing can ask for.
 * `gitHashShort` and `branch` used to sit here for that reason and were dropped — the short hash is
 * derived from the full one at runtime, and the branch is not a property of a commit at all.
 */
enum class BuildInfoVars(val value: String) {
  /**
   * The git commit date and time (ISO 8601 format)
   */
  GitCommitDateTime("gitCommitDateTime"),
  GitHash("gitHash"),
  ;
}

/**
 * The git properties that are injected at the artifact edges (image env, fat-jar resource,
 * serve-time HTML) and resolved at runtime by version-info (#2413).
 *
 * Only values that are deterministic per commit are listed — an artifact is a pure function of
 * the commit. Build date (build-process metadata, breaks rebuild idempotence) and branch
 * (a commit can belong to multiple branches) are deliberately absent. The short hash is derived
 * from [Hash] at runtime.
 *
 * Single source of truth: `version-info` generates the runtime `GitProperty` enum
 * (`it.neckar.open.version`) 1:1 from these constants. The injection sites read
 * [envVar]/[propertyKey] from here, so injection and resolution cannot drift.
 */
enum class GitProperty(
  /**
   * The build-info variable this property exposes; its [BuildInfoVars.value] is the property key
   * used in the fat-jar `app-git-info.properties` resource and in `window.__APP_GIT_INFO__`.
   */
  val buildInfoVar: BuildInfoVars,
  /**
   * The environment variable injected into service images at image build.
   */
  val envVar: String,
  /**
   * The JVM system property that overrides the environment variable (highest resolver priority).
   */
  val systemProperty: String,
) {
  Hash(BuildInfoVars.GitHash, "APP_GIT_HASH", "app.git.hash"),
  CommitDateTime(BuildInfoVars.GitCommitDateTime, "APP_GIT_COMMIT_DATETIME", "app.git.commitDateTime"),
  ;

  val propertyKey: String
    get() = buildInfoVar.value
}

/**
 * Returns the environment map injected into service images at image build:
 * one entry per [GitProperty] ([GitProperty.envVar] to the commit's real value).
 *
 * The values land in the image config JSON, never in a layer — only that JSON changes per commit,
 * so the layers stay identical. They therefore carry the real commit on every build, on CI and
 * locally alike: a placeholder would contradict the `org.opencontainers.image.revision` label of
 * the same image, and [VersionInformation][it.neckar.open.version.VersionInformation] would report
 * a hash of all zeros where it means "unknown" (#2625).
 */
fun Project.gitPropertyEnvironment(): Map<String, String> {
  return GitProperty.entries.associate { gitProperty ->
    gitProperty.envVar to getBuildInfoVarValue(gitProperty.buildInfoVar)
  }
}

/**
 * Returns the value of the given [BuildInfoVars] variable — always the commit's real value.
 *
 * Callers that write into a build INPUT (the fat-jar `app-git-info.properties` resource) must gate
 * the call themselves, otherwise every commit invalidates that input. See
 * `registerCreateFatJarGitInfoTask`, which writes an empty properties file off CI/main so
 * VersionInformation falls back to "unknown". Callers that write into image config or a report
 * use the value unconditionally.
 */
fun Project.getBuildInfoVarValue(buildInfoVar: BuildInfoVars): String {
  return when (buildInfoVar) {
    BuildInfoVars.GitCommitDateTime -> gitCommitDateTime
    BuildInfoVars.GitHash -> gitHash
  }
}

/**
 * Throws an exception if there are any "${*}" left in the files
 */
fun <T : AbstractCopyTask> TaskProvider<T>.ensureAllVariablesHaveBeenReplaced(project: Project) {
  val copyTaskName = this.name

  val verifyTask = project.tasks.register<EnsureAllVariablesHaveBeenReplacedTask>(this.name + " verify-variables-replaced") {
    val copyTask = project.tasks.named<AbstractCopyTask>(copyTaskName)

    this.dependsOn(copyTask)
    this.inputs.files(copyTask)

    onlyIf {
      //Only execute if the copy task itself has been executed
      copyTask.get().didWork
    }
  }

  configure {
    finalizedBy(verifyTask)
  }
}

/**
 * This task verifies that all variables have been replaced in the files.
 */
abstract class EnsureAllVariablesHaveBeenReplacedTask : DefaultTask() {
  init {
    group = "verification"
    description = "Verifies that all variables have been replaced"
  }

  @TaskAction
  fun verifyFiles() {
    inputs.files.forEach { file ->
      verifyFileContainsNoVariablesRecursive(file)
    }
  }
}

private fun Task.verifyFileContainsNoVariablesRecursive(file: File) {
  require(file.exists()) { "File must exist @ ${file.absolutePath}" }

  when {
    file.isDirectory -> {
      file.listFiles()?.forEach {
        verifyFileContainsNoVariablesRecursive(it)
      }
    }

    file.isFile -> {
      verifyFileContainsNoVariables(file)
    }

    else -> {
      throw GradleException("File must be a file or directory @ ${file.absolutePath}")
    }
  }
}

private fun Task.verifyFileContainsNoVariables(file: File) {
  logger.info("Verifying file: ${file.absolutePath}")

  require(file.isFile) { "Must be a file @ ${file.absolutePath}" }
  file.forEachLine { line ->
    //Find the line that contains the invalid pattern! Print the found pattern and line number
    findUnresolvedVariable(line)?.let { match ->
      logger.error("Remaining variable pattern found: [${project.ansiConsole.red(match)}] in file: [${project.ansiConsole.red(file)}] in line: [$line]")
      throw GradleException("Remaining variable pattern found: [$match] in file: [$file] in line: [$line]")
    }
  }
}

/**
 * Returns the unresolved `${…}` placeholder found in [line], or `null` if the line carries none.
 *
 * A `${…}` preceded by a backslash (`\${…}`) is a deliberately escaped shell expansion: the
 * deployment templating leaves it untouched on purpose so the target host evaluates it at runtime
 * (e.g. `short=\${fqdn%%.*}` inside an `ssh "root@$host" "…"` block, where `$host` is expanded
 * locally but `${fqdn%%.*}` must run remotely). Such escaped occurrences are not unresolved
 * placeholders and are ignored.
 *
 * A `${VAR:-default}` is Docker-Compose runtime interpolation with an explicit default (the
 * `.env`-friendly override pattern, e.g. `OTEL_EXPORTER_OTLP_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT:-http://otel-agent:4317}`, #2381).
 * Deploy-time placeholders never carry a `:-` default, so these are not unresolved placeholders
 * either. An unescaped, default-less `${key}` that no replacement filled is a real error
 * and is returned.
 */
internal fun findUnresolvedVariable(line: String): String? {
  return variablePattern.findAll(line)
    .map { it.value }
    .firstOrNull { it.contains(":-").not() }
}

/**
 * Matches an unescaped variable of the form "${*}". The negative lookbehind `(?<!\\)` skips a
 * `${…}` escaped with a leading backslash (`\${…}`), which denotes an intentional runtime shell
 * expansion rather than an unresolved deployment placeholder.
 */
private val variablePattern = "(?<!\\\\)\\$\\{[^}]*}".toRegex()
