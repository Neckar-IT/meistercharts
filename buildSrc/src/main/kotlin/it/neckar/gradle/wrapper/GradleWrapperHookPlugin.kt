package it.neckar.gradle.wrapper

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion
import java.io.File

/**
 * Makes the `wrapper` task produce the repository's `gradlew`, build-guard hook included, and registers
 * [VerifyGradleWrapperTaskName], which fails when the checked-in wrapper differs from a generated one.
 * Apply to the root project.
 *
 * The hook lives once, in [HookPath]. Regenerating the wrapper without it would silently drop the build
 * guard and the run supervision from every `./gradlew` invocation.
 */
class GradleWrapperHookPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    require(target == target.rootProject) {
      "GradleWrapperHookPlugin must be applied to the root project but was applied to <${target.path}>"
    }

    val rootDirectory: File = target.rootDir
    val hookFile = File(rootDirectory, HookPath)

    // `wrapper` and the reference wrapper share one configuration; a difference between them would be
    // reported as a difference of the checked-in files.
    target.tasks.withType<Wrapper>().configureEach {
      distributionType = Wrapper.DistributionType.ALL
      // An input, or a changed hook leaves the task up to date with the old one inserted.
      inputs.file(hookFile)
      doLast { insertHookInto(scriptFile, hookFile) }
    }

    val referenceDirectory: File = target.layout.buildDirectory.dir("gradle-wrapper-reference").get().asFile
    val generateReference = target.tasks.register<Wrapper>(GenerateReferenceGradleWrapperTaskName) {
      description = "Generates the wrapper that $VerifyGradleWrapperTaskName compares the checked-in one against"
      setScriptFile(File(referenceDirectory, "gradlew"))
      setJarFile(File(referenceDirectory, "gradle/wrapper/gradle-wrapper.jar"))
      // The reference is never used to download anything; checking its URL would put the network into the gate.
      validateDistributionUrl.set(false)
    }

    target.tasks.register(VerifyGradleWrapperTaskName) {
      group = "Verification"
      description = "Fails when gradlew, gradlew.bat or gradle-wrapper.jar differ from what the wrapper task generates"
      dependsOn(generateReference)

      doLast {
        val runningVersion = GradleVersion.current().version
        val propertiesVersion = GradleWrapperHook.distributionVersion(File(rootDirectory, "gradle/wrapper/gradle-wrapper.properties").readText())
        val differingFiles = GradleWrapperHook.differingFiles(
          committed = GradleWrapperHook.GeneratedFiles.associateWith { name -> File(rootDirectory, name).takeIf { it.isFile }?.readBytes() },
          generated = GradleWrapperHook.GeneratedFiles.associateWith { name -> File(referenceDirectory, name).readBytes() },
        )

        val problems = buildList {
          when (propertiesVersion) {
            null -> add("gradle/wrapper/gradle-wrapper.properties has no distributionUrl naming a Gradle version")
            runningVersion -> Unit
            else -> add("gradle/wrapper/gradle-wrapper.properties names Gradle $propertiesVersion, but this build runs Gradle $runningVersion")
          }
          differingFiles.forEach { name -> add("$name differs from the one Gradle $runningVersion generates with the hook from $HookPath") }
        }
        if (problems.isNotEmpty()) {
          throw GradleException(
            problems.joinToString(separator = "\n", postfix = "\nRegenerate with `./gradlew wrapper`, see docs/gradle/update-gradle.md."),
          )
        }
      }
    }
  }

  private fun insertHookInto(scriptFile: File, hookFile: File) {
    scriptFile.writeText(GradleWrapperHook.insertHook(scriptFile.readText(), hookFile.readText()))
  }

  companion object {
    /** The hook block, relative to the repository root. */
    const val HookPath: String = "tools/build-guard/gradlew-hook.sh"

    const val VerifyGradleWrapperTaskName: String = "verifyGradleWrapper"

    const val GenerateReferenceGradleWrapperTaskName: String = "generateReferenceGradleWrapper"
  }
}
