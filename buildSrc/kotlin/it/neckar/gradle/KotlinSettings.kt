package it.neckar.gradle

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/**
 * Kotlin settings
 */
object KotlinSettings {
  val languageVersion: KotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4
  val languageVersionAsString: String = languageVersion.version

  val apiVersion: KotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4
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
   * Compiler argument sources by Kotlin version (keep sorted by Kotlin version).
   *
   * Kotlin 2.4.x:
   * - https://kotlinlang.org/docs/whatsnew-eap.html
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

    add("-XXexplicit-return-types=warning")
    add("-Xexpect-actual-classes") // https://youtrack.jetbrains.com/issue/KT-61573
    add("-Xconsistent-data-class-copy-visibility") // https://youtrack.jetbrains.com/issue/KT-11914

    // Kotlin 2.2.x
    add("-Xreturn-value-checker=full")
    add("-Xwarning-level=RETURN_VALUE_NOT_USED:error")
    add("-Xcontext-sensitive-resolution")

    add("-Xname-based-destructuring=only-syntax") // https://kotlinlang.org/docs/whatsnew2320.html

    // Kotlin 2.4.x — newly introduced opt-in flags
    add("-Xexplicit-context-arguments") // explicit context arguments — https://kotlinlang.org/docs/whatsnew-eap.html
    add("-Xcollection-literals") // bracket-syntax `[]` collection literals
    add("-Xintrinsic-const-evaluation") // enables IntrinsicConstEvaluation language feature

    // Candidates to evaluate in a follow-up — enable individually after measuring noise:
    // add("-Xreport-all-warnings") // emit warnings even when errors are present
    // add("-Xdata-flow-based-exhaustiveness") // experimental; `when` exhaustiveness via data-flow analysis
    // add("-Xnested-type-aliases") // experimental; allow `object Foo { typealias Bar = ... }`
    // add("-Xlocal-type-aliases") // experimental; type aliases inside function bodies
    // add("-Xmulti-dollar-interpolation") // experimental; `$$${...}` template syntax
  }

  /**
   * Additional compiler args - only used for JS
   */
  val additionalFreeCompilerArgsJS: List<String> = buildList {
    add("-Xfake-override-validator")
    add("-Xoptimize-generated-js")

    // Kotlin 2.2.x
    add("-Xes-long-as-bigint")

    // Required: -Xir-dce must be set in combination with -Xir-dce-print-reachability-info
    add("-Xir-dce-print-reachability-info")
    add("-Xir-dce")
  }

  /**
   * Additional compiler args - only used for the JVM
   */
  val additionalFreeCompilerArgsJVM: List<String> = buildList {
    add("-Xjsr305=strict")
    // Compile against the specified JDK API version (analogous to javac's --release).
    // Setting this also pins -jvm-target to the same version, so newer JDK APIs are
    // rejected by the compiler — protects against an unnoticed build-JDK bump.
    add("-Xjdk-release=${JvmTarget.JVM_25.target}")
    add("-Xemit-jvm-type-annotations") // type annotations (e.g. nullability on generics) in bytecode
    add("-Xuse-inline-scopes-numbers") // better stack traces for inline functions (Coroutines, Sequences)

    // Candidates to evaluate in a follow-up — enable individually after measuring impact:
    // add("-Xenhance-type-parameter-types-to-def-not-null") // `@NotNull T` => `T & Any`; may break Java-interop wrappers
    // add("-Xenhanced-coroutines-debugging") // extra line-numbers for compiler-generated suspend code
  }
}
