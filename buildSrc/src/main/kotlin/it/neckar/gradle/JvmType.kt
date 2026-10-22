package it.neckar.gradle

import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Represents the JVM type
 */
enum class JvmType(
  /** The version a toolchain of this type compiles and runs with. */
  val languageVersion: JavaLanguageVersion,
) {
  /**
   * Latest Java LTS version
   */
  JavaLatestLTS(JavaLanguageVersion.of(25)),
}
