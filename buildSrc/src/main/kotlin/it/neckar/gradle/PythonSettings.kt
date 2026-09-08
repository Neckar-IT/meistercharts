package it.neckar.gradle

/**
 * Contains relevant settings for Python.
 */
object PythonSettings {
  /**
   * The MINIMUM version of Python required — not the version anybody runs.
   *
   * `verifyPythonVersion` (part of `./gradlew verify`) checks `python3` against this as a floor,
   * and nothing pins a minor: the CI runner image and a workstation on Ubuntu 26.04 both have 3.14
   * as their `python3`, a workstation still on 24.04 has 3.12. All of them are fine.
   *
   * The number is the python of Ubuntu 24.04 LTS, the oldest release still in use here. Raising it
   * excludes every environment below the new value.
   */
  const val Version: String = "3.12.2"
}
