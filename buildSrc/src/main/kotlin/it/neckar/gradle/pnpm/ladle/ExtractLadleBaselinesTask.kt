package it.neckar.gradle.pnpm.ladle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Replaces `build/ladle-baselines/` with the package's part of `origin/data/ladle-baselines`;
 * with [SnapshotUpdateMode.All] it only empties the directory, the run writes every baseline.
 */
internal abstract class ExtractLadleBaselinesTask : DefaultTask() {
  @get:Inject
  protected abstract val execOperations: ExecOperations

  @get:Inject
  protected abstract val fileSystemOperations: FileSystemOperations

  @get:Inject
  protected abstract val archiveOperations: ArchiveOperations

  /** The repository root, where git runs. */
  @get:Internal
  abstract val repositoryDirectory: DirectoryProperty

  /** The package's path from the repository root, the prefix of its files on the branch. */
  @get:Input
  abstract val packagePath: Property<String>

  /** `build/ladle-baselines/` of the package, the `snapshotDir` of `ladleSuiteConfig`. */
  @get:Internal
  abstract val baselineDirectory: DirectoryProperty

  /** The `-Pplaywright.updateSnapshots` of this run, unset when the run only compares. */
  @get:Input
  @get:Optional
  abstract val snapshotUpdateMode: Property<SnapshotUpdateMode>

  @TaskAction
  fun extract() {
    fileSystemOperations.delete { delete(baselineDirectory) }
    baselineDirectory.get().asFile.mkdirs()
    if (snapshotUpdateMode.orNull == SnapshotUpdateMode.All) {
      return
    }

    if (refExists(LadleTasks.BaselineRef).not()) {
      throw GradleException("${LadleTasks.BaselineRef} is missing. Fetch it with:\n\n    git fetch origin ${LadleTasks.BaselineBranch}\n")
    }

    val prefix = packagePath.get()
    // A package whose baselines were never written has no directory on the branch; Playwright
    // then reports every comparison as a missing snapshot.
    if (git("ls-tree", "-d", "--name-only", LadleTasks.BaselineRef, prefix).isEmpty()) {
      logger.lifecycle("${LadleTasks.BaselineBranch} holds no baselines for $prefix")
      return
    }

    val archive = temporaryDir.resolve("ladle-baselines.tar")
    git("archive", "--format=tar", "--output=${archive.absolutePath}", LadleTasks.BaselineRef, "$prefix/")

    val prefixDepth = prefix.split('/').size
    fileSystemOperations.copy {
      from(archiveOperations.tarTree(archive))
      into(baselineDirectory)
      includeEmptyDirs = false
      eachFile {
        relativePath = RelativePath(true, *relativePath.segments.drop(prefixDepth).toTypedArray())
      }
    }
  }

  private fun refExists(ref: String): Boolean {
    return execOperations.exec {
      workingDir = repositoryDirectory.get().asFile
      commandLine = listOf("git", "rev-parse", "--verify", "--quiet", ref)
      standardOutput = ByteArrayOutputStream()
      isIgnoreExitValue = true
    }.exitValue == 0
  }

  private fun git(vararg arguments: String): String {
    val output = ByteArrayOutputStream()
    execOperations.exec {
      workingDir = repositoryDirectory.get().asFile
      commandLine = listOf("git") + arguments
      standardOutput = output
    }
    return output.toString().trim()
  }
}
