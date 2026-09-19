package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.pnpm.dependency.NpmPackageName

/**
 * A key of the `snapshots:` section of `pnpm-lock.yaml`: one package version resolved against one
 * combination of peers. pnpm installs one copy per key, so two keys of one version are two copies.
 */
data class PnpmSnapshotKey(
  val packageName: NpmPackageName,
  val version: Version,
  /** The resolved peers as pnpm appends them, `(jsdom@30.0.1)(vite@8.3.0)`; empty for a package without peers. */
  val peerSuffix: String,
) {
  /** How a dependency entry names this copy. */
  val resolution: PnpmResolution
    get() = PnpmResolution("$version$peerSuffix")

  override fun toString(): String {
    return "$packageName@$resolution"
  }

  /** The version in front of the peer suffix, `5.0.1`. */
  @JvmInline
  value class Version(val value: String) {
    init {
      require(value.isNotBlank()) { "pnpm snapshot version must not be blank" }
    }

    override fun toString(): String {
      return value
    }
  }

  companion object {
    /** Parses [key] as pnpm writes it: `vitest@5.0.1(vite@8.3.0)`, `@scope/name@1.0.0`. */
    fun parse(key: String): PnpmSnapshotKey {
      val peerSuffixStart = key.indexOf('(').takeIf { it >= 0 } ?: key.length
      // Index 1 skips the scope marker of `@scope/name`.
      val versionSeparator = key.indexOf('@', startIndex = 1)
      require(versionSeparator in 1 until peerSuffixStart - 1) { "pnpm snapshot key <$key> carries no version" }

      return PnpmSnapshotKey(
        packageName = NpmPackageName(key.substring(0, versionSeparator)),
        version = Version(key.substring(versionSeparator + 1, peerSuffixStart)),
        peerSuffix = key.substring(peerSuffixStart),
      )
    }
  }
}
