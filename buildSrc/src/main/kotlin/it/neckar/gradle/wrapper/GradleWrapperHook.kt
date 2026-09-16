package it.neckar.gradle.wrapper

/**
 * The file-level half of [GradleWrapperHookPlugin]: where the hook goes into a generated `gradlew`, and
 * which wrapper files differ from a generated set. Testable without running a Gradle build.
 */
object GradleWrapperHook {

  /** The files the `wrapper` task generates from the running Gradle distribution, relative to the root. */
  val GeneratedFiles: List<String> = listOf("gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar")

  /** The hook sources the build guard relative to `APP_HOME`, so it has to follow the line that resolves it. */
  private val AppHomeAssignment: Regex = Regex("""^APP_HOME=\$\( cd -P .*$""", RegexOption.MULTILINE)

  private val DistributionVersion: Regex = Regex("""^distributionUrl=.*/gradle-([^/]+)-(?:bin|all)\.zip\s*$""", RegexOption.MULTILINE)

  /** [script] with [hook] inserted after the `APP_HOME` assignment; unchanged when it already carries it. */
  fun insertHook(script: String, hook: String): String {
    val hookBlock = hook.trimEnd('\n')
    if (script.contains(hookBlock)) return script

    val assignments = AppHomeAssignment.findAll(script).toList()
    require(assignments.size == 1) {
      "Expected exactly one `APP_HOME=$( cd -P …` line in the generated gradlew, found ${assignments.size}. " +
        "The Gradle start script template changed; adjust the anchor in GradleWrapperHook."
    }

    val insertAt = assignments.single().range.last + 1
    return script.substring(0, insertAt) + "\n\n" + hookBlock + script.substring(insertAt)
  }

  /** The names in [generated] whose committed content is missing or differs, in the order of [generated]. */
  fun differingFiles(committed: Map<String, ByteArray?>, generated: Map<String, ByteArray>): List<String> =
    generated.keys.filter { name -> committed[name]?.contentEquals(generated.getValue(name)) != true }

  /** The Gradle version the `distributionUrl` of a `gradle-wrapper.properties` names, `null` without one. */
  fun distributionVersion(properties: String): String? =
    DistributionVersion.find(properties)?.groupValues?.get(1)
}
