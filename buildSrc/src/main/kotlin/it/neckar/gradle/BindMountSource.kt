package it.neckar.gradle

/**
 * A bind-mount source as a compose file writes it, relative to the compose file's own directory:
 * `./seal-key`, `./config/gatus/`, `../shared/overlay.yml`.
 *
 * The spelling is what `VerifyComposeBindMountsTask` compares, so it is checked here.
 */
@JvmInline
value class BindMountSource(val value: String) {
  init {
    require(value.startsWith("./") || value.startsWith("../")) {
      "Bind mount source [$value] is not written the way a compose file writes it — relative to the " +
        "compose file's own directory, so `./$value` rather than `$value`."
    }
  }

  val leavesItsDirectory: Boolean
    get() = value.startsWith("../")

  /** The path below the compose file's directory: `./config/gatus/` names `config/gatus`. */
  val pathBelowItsDirectory: String
    get() = value.removePrefix("./").removeSuffix("/")

  override fun toString(): String = value
}
