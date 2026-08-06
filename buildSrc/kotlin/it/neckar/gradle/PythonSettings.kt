package it.neckar.gradle

/**
 * Contains relevant settings for Python.
 */
object PythonSettings {
  /**
   * The MINIMUM version of Python required — not the version anybody runs.
   *
   * `verifyPythonVersion` (part of `./gradlew verify`) checks `python3` against this as a floor,
   * and nothing pins a minor: the CI runner image has 3.12 as its `python3`, a developer on
   * Ubuntu 26.04 has 3.14 and no 3.12 at all. Both are fine.
   *
   * The number is the python of Ubuntu 24.04 LTS, which is what the floor was set from. Raising it
   * excludes every environment below the new value — including CI, until its image moves.
   */
  const val Version: String = "3.12.2"
}
