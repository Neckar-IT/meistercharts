package it.neckar.gradle

import java.io.File

/**
 * Answers which `internal/infrastructure/common/<subdir>` directories a deploy module's scripts
 * pull in through `# @inline:` markers — the include graph, seen from the outside.
 *
 * Exists for the continuous-deploy resolver (#2341). A deploy candidate is connected to the diff
 * by its Gradle closure, by the images it deploys, or by the `common/` subdirs it consumes. That
 * last edge used to be fed from `composeRoles` alone, which only covers the five compose fragments
 * (traefik, otel-agent, host-exporters, host-management, host-logs). Everything a deploy script
 * inlines — the deploy libs, the docker lock, the maintenance cron installer, secret masking — lives
 * in `common/` too, but was invisible: changing `common/docker-lock/docker-lock-lib.sh` alone selected no deploy at
 * all, so the change merged green and never reached a single host.
 *
 * Resolution is transitive, because an include is usually reached indirectly: a service deploy
 * script inlines `service-deploy/deploy-service-lib.sh`, which inlines
 * `compose-up/compose-up-lib.sh`, which inlines `docker-lock/docker-lock-lib.sh`. Only the first
 * hop is visible in the script itself.
 *
 * Purposely file-based rather than Gradle-based: these includes are folded in by copying text at
 * materialization time, so they leave no trace in the Gradle dependency graph.
 */
object ShellIncludeGraph {
  /**
   * The `common/<subdir>` names reachable from the files in [resourcesDir] via `# @inline:`,
   * resolved transitively through [commonDir].
   *
   * A marker pointing at a file that does not exist contributes its subdir and stops there —
   * reporting a broken include is [inlineCommonShellIncludes]'s job, which fails the build with
   * the offending path. Returning empty for a missing [resourcesDir] keeps modules without
   * deployment resources cheap to ask about.
   */
  fun consumedCommonSubdirs(resourcesDir: File, commonDir: File): Set<String> {
    if (!resourcesDir.isDirectory) return emptySet()

    val reachedPaths = linkedSetOf<String>()
    resourcesDir.walkTopDown()
      .filter { it.isFile }
      .forEach { collectReachable(it.readText(), commonDir, reachedPaths) }

    return reachedPaths.map { it.substringBefore('/') }.toSet()
  }

  /**
   * Adds every include path reachable from [content] to [reachedPaths], following markers in the
   * included files as well. The set doubles as the cycle guard: a path already reached is not
   * descended into twice.
   */
  private fun collectReachable(content: String, commonDir: File, reachedPaths: MutableSet<String>) {
    parseInlineTargets(content).forEach { relativePath ->
      if (!reachedPaths.add(relativePath)) return@forEach
      val includeFile = commonDir.resolve(relativePath)
      if (includeFile.isFile) {
        collectReachable(includeFile.readText(), commonDir, reachedPaths)
      }
    }
  }

  /** The include paths marked in [content], in order of appearance. */
  internal fun parseInlineTargets(content: String): List<String> =
    content.lineSequence()
      .mapNotNull { line -> InlineMarkerRegex.matchEntire(line)?.groupValues?.get(2) }
      .toList()
}
