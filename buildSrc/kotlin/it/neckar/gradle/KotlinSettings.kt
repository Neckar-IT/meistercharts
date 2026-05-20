package it.neckar.gradle

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/**
 * Kotlin settings
 */
object KotlinSettings {
  val languageVersion: KotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
  val languageVersionAsString: String = languageVersion.version

  val apiVersion: KotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
  val apiVersionAsString: String = apiVersion.version

  /**
   * Contains the annotations we opted in for.
   * Can be used to configure Kotlin extensions directly
   *
   * Look at: https://github.com/JetBrains/kotlin/blob/master/compiler/arguments/src/org/jetbrains/kotlin/arguments/description/CommonCompilerArguments.kt
   */
  val optInExperimentalAnnotations: List<String> = listOf(
    "kotlin.ExperimentalStdlibApi",
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlin.js.ExperimentalJsExport",
    "kotlin.ExperimentalMultiplatform",
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.io.path.ExperimentalPathApi",
    "kotlin.io.encoding.ExperimentalEncodingApi",
    "kotlin.uuid.ExperimentalUuidApi",
    "kotlinx.serialization.ExperimentalSerializationApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlin.concurrent.atomics.ExperimentalAtomicApi",
  )

  /**
   * Language features that are enabled
   */
  val languageFeatures: List<String> = listOf(
  )

  /**
   * Compiler argument sources by Kotlin version (keep sorted by Kotlin version).
   *
   * Kotlin 2.3.x:
   * - https://github.com/JetBrains/kotlin/blob/v2.3.0/compiler/arguments/src/org/jetbrains/kotlin/arguments/description/CommonCompilerArguments.kt
   * - https://github.com/JetBrains/kotlin/blob/v2.3.0/compiler/arguments/src/org/jetbrains/kotlin/arguments/description/JsCompilerArguments.kt
   * - https://github.com/JetBrains/kotlin/blob/v2.3.0/compiler/arguments/src/org/jetbrains/kotlin/arguments/description/JvmCompilerArguments.kt
   *
   * Kotlin 2.2.x:
   * - https://github.com/JetBrains/kotlin/blob/v2.2.20/compiler/arguments/src/org/jetbrains/kotlin/arguments/description/CommonCompilerArguments.kt
   * - https://github.com/JetBrains/kotlin/blob/v2.2.20/compiler/arguments/src/org/jetbrains/kotlin/arguments/description/JsCompilerArguments.kt
   * - https://github.com/JetBrains/kotlin/blob/v2.2.20/compiler/arguments/src/org/jetbrains/kotlin/arguments/description/JvmCompilerArguments.kt
   */

  /**
   * The free compiler args that must be used to configure the Kotlin compiler tasks.
   * These args are used for both JS and JVM.
   *
   * See the links above for a list of all available compiler arguments per Kotlin version.
   */
  val freeCompilerArgs: List<String> = buildList {
    addAll(optInExperimentalAnnotations.map { "-opt-in=$it" })
    add("-progressive")
    add("-Wextra")

    add("-Xwarning-level=NOTHING_TO_INLINE:disabled")

    add("-Xannotation-target-all")
    add("-XXexplicit-return-types=warning")
    add("-Xexpect-actual-classes") // https://youtrack.jetbrains.com/issue/KT-61573
    add("-Xconsistent-data-class-copy-visibility") // https://youtrack.jetbrains.com/issue/KT-11914
    add("-Xcontext-parameters")
    add("-Xnon-local-break-continue")
    add("-Xwhen-guards")

    // Kotlin 2.2 required
    add("-Xannotation-default-target=param-property") // https://youtrack.jetbrains.com/issue/KT-73255

    // Kotlin 2.2.20 required
    add("-Xallow-condition-implies-returns-contracts")
    add("-Xallow-holdsin-contract")
    add("-Xallow-contracts-on-more-functions")
    add("-Xallow-reified-type-in-catch")

    // Kotlin 2.2.x
    add("-Xreturn-value-checker=full")
    add("-Xwarning-level=RETURN_VALUE_NOT_USED:error")
    add("-Xcontext-sensitive-resolution")

    // Kotlin 2.3.x
    add("-Xexplicit-backing-fields") // https://kotlinlang.org/docs/whatsnew23.html#explicit-backing-fields
    add("-Xname-based-destructuring=only-syntax") // https://kotlinlang.org/docs/whatsnew2320.html
  }

  /**
   * Additional compiler args - only used for JS
   */
  val additionalFreeCompilerArgsJS: List<String> = buildList {
    add("-Xfake-override-validator")
    add("-Xoptimize-generated-js")

    // Kotlin 2.2.x
    add("-Xes-long-as-bigint")

    // Kotlin 2.3.x
    add("-Xenable-suspend-function-exporting")

    // Required: -Xir-dce must be set in combination with -Xir-dce-print-reachability-info
    add("-Xir-dce-print-reachability-info")
    add("-Xir-dce")
  }

  /**
   * Additional compiler args - only used for the JVM
   */
  val additionalFreeCompilerArgsJVM: List<String> = buildList {
    add("-Xjsr305=strict")
    add("-Xannotations-in-metadata")
  }
}
