package it.neckar.gradle.pnpm

import it.neckar.projects.GradleProjectPath

/**
 * Where the node-gradle toolchain lives: the Node distribution plus the pnpm/npm/yarn installs that
 * every `PnpmTask` in the build executes.
 *
 * The whole build shares **one** installation, owned by the root project. node-gradle derives every
 * executable path from `NodeExtension.workDir` / `pnpmWorkDir` / `npmWorkDir` / `yarnWorkDir`
 * (`com.github.gradle.node.variant.VariantComputer`), so pointing a project's extension at the root
 * project's directories makes that project run the root's binaries. Only the owner runs `nodeSetup`
 * and `pnpmSetup`; in every other project both tasks are disabled and depend on the owner's, which
 * guarantees the binaries exist before any task executes them.
 *
 * Two reasons, in this order:
 *
 * 1. **Correctness.** `NodeSetupTask` starts with `deleteExistingNode()` and then re-unpacks the
 *    archive, so two projects installing into the same directory race and can leave `bin/node`
 *    missing or non-executable — the failure mode of #2208 in a different shape. One owner, one
 *    installation, no concurrent writers.
 * 2. **Cost.** A per-project installation is a full Node distribution (~200 MB) plus a pnpm install
 *    (~100 MB), byte-identical in every pnpm project and duplicated again in every worktree. CI
 *    caches the owner's directory only, so per-project installations were re-unpacked on every job
 *    and never cached at all.
 */
object NodeToolchainLayout {
  /**
   * The project that owns the toolchain installation — the root project.
   */
  val OwnerProjectPath: GradleProjectPath = GradleProjectPath(":")

  /**
   * Directory the Node distribution is unpacked into, relative to the owner's *project* directory.
   *
   * Deliberately not node-gradle's default `.gradle/nodejs`: on the root project the Kotlin/JS
   * `NodeJsRootPlugin` installs *its* Node into that exact path, and node-gradle's `NodeSetupTask`
   * (`deleteExistingNode` -> `unpackNodeArchive` -> `setExecutableFlag`) would race the parallel
   * Kotlin `:kotlinNpmInstall` reading `bin/npm` from there, intermittently failing the build
   * (#2208). The two plugins never share a mutable output.
   *
   * Under `.gradle/` because that is the prefix the CI cache is built from — `gitlab-ci.d/mr.yml`
   * and `gitlab-ci.d/main.yml` list `.gradle/node-gradle` under `cache.paths`, and
   * `gitlab-ci.d/mr.yml` reports its size in `after_script`. Change this value and those three
   * places have to change with it, or every CI job silently misses the cache and re-downloads Node.
   * Note that `clean` deletes it regardless of the prefix: `configureDefaultPnpm` wires the owner's
   * `clean` to delete `.gradle` along with `build`.
   */
  const val NodeDistributionPath: String = ".gradle/node-gradle/nodejs"

  /**
   * Directory pnpm is installed into, relative to the owner's *build* directory.
   */
  const val PnpmInstallPath: String = "node/pnpm"

  /**
   * Directory npm is installed into, relative to the owner's *build* directory.
   */
  const val NpmInstallPath: String = "node/npm"

  /**
   * Directory yarn is installed into, relative to the owner's *build* directory.
   */
  const val YarnInstallPath: String = "node/yarn"

  /**
   * Whether the project at [projectPath] installs the toolchain itself — true for the root project,
   * false for every other project, which consumes the root installation.
   */
  fun ownsInstallation(projectPath: GradleProjectPath): Boolean = projectPath == OwnerProjectPath
}
