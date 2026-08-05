package it.neckar.gradle.pnpm.ladle

import com.github.gradle.node.pnpm.task.PnpmTask
import it.neckar.gradle.localdev.LocalDevPlugin
import it.neckar.gradle.localdev.resolveWorktreePortOffsetOrNull
import it.neckar.projects.common.LadleDevPorts
import it.neckar.projects.common.Port
import it.neckar.projects.common.WorktreeOffset
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

/**
 * Names the Ladle tasks share with the scripts they run, and the environment variable that carries
 * the port to the Playwright suites.
 */
internal object LadleTasks {
  /** Serves the package's Ladle stories — the pnpm script and the Gradle task share the name. */
  const val LadleTaskName: String = "ladle"

  /** Builds the package's stories as a static site — the content of its showcase image. */
  const val LadleBuildTaskName: String = "ladleBuild"

  /** The pnpm script behind [LadleBuildTaskName]. */
  const val LadleBuildScriptName: String = "ladle:build"

  /** Runs the package's Playwright suite through pnpm. */
  const val JsIntegrationTestTaskName: String = "integrationTest"

  /** Read by `ladleWebServer` in the shared Playwright config to place the story browser. */
  const val LadlePortEnvironmentVariable: String = "LADLE_PORT"
}

/**
 * Registers `ladle` for a package that declares a `ladle` script, plus the `run` alias where the
 * package has no dev server of its own — the runnable thing in a component library is its story
 * browser.
 *
 * The port comes from [LadleDevPorts] and this worktree's `dev.portOffset`, passed on the command
 * line. Keeping it out of the `ladle` script is what allows several worktrees and several packages
 * to serve stories at the same time; the scripts themselves stay portable and fall back to Ladle's
 * default port when run by hand.
 */
internal fun Project.registerLadleTasks(pnpmRunBuild: TaskProvider<PnpmTask>, hasDevScript: Boolean) {
  val port = ladleDevPort()
  // Built here, where the project is still at hand: the task action must work on values alone.
  val missingSlotMessage = missingSlotMessage()

  val ladle = tasks.register<PnpmTask>(LadleTasks.LadleTaskName) {
    description = when (port) {
      null -> "Serves the package's Ladle story browser (no port slot registered)"
      else -> "Serves the package's Ladle story browser on http://localhost:$port"
    }
    group = "application"

    dependsOn(pnpmRunBuild)
    args.set(
      buildList {
        add("run")
        add(LadleTasks.LadleTaskName)
        if (port != null) {
          add("--port")
          add(port.toString())
        }
      },
    )

    doFirst {
      if (port == null) {
        throw GradleException(missingSlotMessage)
      }
    }
  }

  // A package with its own dev server keeps `run` on that server — there `ladle` is the secondary
  // view. Registered in afterEvaluate for the same reason the `dev` alias is: the package's own
  // build file may register a `run` of its own.
  if (hasDevScript.not()) {
    afterEvaluate {
      if (tasks.findByName(LocalDevPlugin.RunTaskName) == null) {
        tasks.register(LocalDevPlugin.RunTaskName) {
          description = "Runs the package's story browser (alias for ladle)"
          group = "application"

          dependsOn(ladle)
        }
      }
    }
  }
}

/**
 * Registers `ladleBuild` for a package that declares a `ladle:build` script: the stories as a
 * static site under `build/ladle/`, which a `*.showcase.neckar.it` module packs into its nginx
 * image.
 *
 * Git metadata is deliberately not an input — `.ladle/config.mjs` writes `APP_GIT_*` placeholders
 * that the serving container substitutes at start, so the output stays byte-identical per commit
 * and the task stays cacheable.
 */
internal fun Project.registerLadleBuild() {
  tasks.register<PnpmTask>(LadleTasks.LadleBuildTaskName) {
    description = "Builds the package's Ladle stories as a static site into build/ladle/"
    group = "build"

    dependsOn("build")

    args.set(listOf("run", LadleTasks.LadleBuildScriptName))

    inputs.dir("src")
    inputs.dir(".ladle")
    inputs.file("package.json")
    inputs.file("tsconfig.json")
    outputs.dir(layout.projectDirectory.dir("build/ladle"))
  }
}

/**
 * Registers `integrationTest` for a package that declares an `integration-test` script.
 *
 * Story-browser packages start Ladle from their Playwright config, so the task hands the worktree's
 * Ladle port down through the environment — without it two worktrees running screenshot tests would
 * share one server and compare the wrong stories.
 */
internal fun Project.registerJsIntegrationTest() {
  val port = ladleDevPort()

  tasks.register<PnpmTask>(LadleTasks.JsIntegrationTestTaskName) {
    description = "Runs the Playwright integration tests"
    group = "test"

    dependsOn("build")

    args.set(listOf("run", "integration-test"))

    if (port != null) {
      environment.put(LadleTasks.LadlePortEnvironmentVariable, port.toString())
    }
  }
}

/**
 * The Ladle port of this package in this worktree, or null when the package holds no slot in
 * [LadleDevPorts] — `verifyLadlePorts` reports that case for the whole repository.
 */
private fun Project.ladleDevPort(): Port? {
  // Tolerant: a plain clone without `git config --worktree dev.portOffset` still gets the base
  // port instead of a failing configuration phase.
  val offset = resolveWorktreePortOffsetOrNull() ?: WorktreeOffset(0)
  return LadleDevPorts.port(path, offset)
}

/**
 * What the `ladle` task says when the package holds no slot — the whole repository's view of that
 * case is `verifyLadlePorts`.
 */
private fun Project.missingSlotMessage(): String {
  return buildString {
    appendLine("$path declares a `ladle` script but holds no port slot.")
    appendLine()
    appendLine("Without a slot the story browser falls back to Ladle's default port and collides")
    appendLine("with the other packages. Add it to LadleDevPorts.slots with the next free slot:")
    appendLine()
    appendLine("    build-logic/descriptors/src/jvmMain/kotlin/it/neckar/projects/common/LadleDevPorts.kt")
  }
}
