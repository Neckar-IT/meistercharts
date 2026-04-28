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
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

/**
 * Contains all build information variables that can be used in the resources.
 *
 * The variables are used to replace the values in the HTML files.
 */
enum class BuildInfoVars(val value: String, val useAsInput: Boolean = true) {
  /**
   * The build date (*not* the time - for caching reasons)
   */
  BuildDate("buildDate"),
  /**
   * The git commit date and time (ISO 8601 format)
   */
  GitCommitDateTime("gitCommitDateTime"),
  GitHash("gitHash"),
  GitHashShort("gitHashShort"),
  /**
   * The git branch name
   */
  Branch("branch"),
  ;
}

/**
 * Returns the value of the given [BuildInfoVars] variable.
 */
fun Project.getBuildInfoVarValue(buildInfoVar: BuildInfoVars): String {
  return when (buildInfoVar) {
    BuildInfoVars.BuildDate -> buildDate
    BuildInfoVars.GitCommitDateTime -> if (inCi || onMainBranch) gitCommitDateTime else "1970-01-01T00:00:00Z"
    BuildInfoVars.GitHash -> if (inCi || onMainBranch) gitHash else "0".repeat(40)
    BuildInfoVars.GitHashShort -> if (inCi || onMainBranch) gitHashShort else "0000000"
    BuildInfoVars.Branch -> branch
  }
}

/**
 * Configures resource expansion with the default values
 */
fun Project.expandHtmlResourcesWithGitInfo(filePattern: String = "index.html") {
  val var2value = gitInfoVarMap()

  tasks.named<ProcessResources>("jsProcessResources") {
    filesMatching(filePattern) {
      expand(
        var2value,
      )
    }
  }
}

/**
 * Returns the map that maps the build infor vars to strings.
 *
 * The returned map can be used to filter files
 */
fun Project.gitInfoVarMap(): Map<String, String> {
  val var2value = BuildInfoVars.entries
    .filter {
      it.useAsInput
    }
    .associate {
      it.value to getBuildInfoVarValue(it)
    }

  return var2value
}

/**
 * Helper function to replace the build information variables.
 *
 * ATTENTION: Try to avoid this method. Use [expandHtmlResourcesWithGitInfo] instead - if possible.
 */
fun Project.replaceVersionVars(toReplace: String): String {
  var replaced = toReplace

  BuildInfoVars.entries.forEach {
    replaced = replaced.replace("""$${it.value}""", getBuildInfoVarValue(it))
  }

  return replaced
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
    variablePattern.find(line)?.let { match ->
      logger.error("Remaining variable pattern found: [${project.ansiConsole.red(match.value)}] in file: [${project.ansiConsole.red(file)}] in line: [$line]")
      throw GradleException("Remaining variable pattern found: [${match.value}] in file: [${file}] in line: [$line]")
    }
  }
}

/**
 * A regex pattern to find variables in the form of "${*}"
 */
private val variablePattern = "\\$\\{.*}".toRegex()
