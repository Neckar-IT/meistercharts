package it.neckar.gradle.pnpm.workspace

/** A resolution as `pnpm-lock.yaml` writes it: `5.0.1(vite@8.3.0)` for an installed copy, `link:../utils` for a workspace package. */
@JvmInline
value class PnpmResolution(val value: String) {
  init {
    require(value.isNotBlank()) { "pnpm resolution must not be blank" }
  }

  override fun toString(): String {
    return value
  }
}
