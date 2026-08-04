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
   * Free compiler args applied to every Kotlin compilation (common, JVM and JS).
   *
   * Flags are grouped by status:
   * - **Diagnostics & language mode** — permanent policy toggles, not tied to a language version.
   * - **Stabilization-track features** — opt-in today; each becomes the language-version default in a
   *   later Kotlin release, at which point the compiler prints
   *   `The argument '<flag>' is redundant for the current language version <v>`. Drop the flag then.
   * - **Not active** — every other relevant flag, documented with its purpose and the reason it is off
   *   (already default at LV 2.4, deferred, no effect, deprecated, …). Kept for reference, not enabled.
   *
   * Scope: this catalogs language features and project-level quality / diagnostics / interop decisions for
   * Kotlin 2.2–2.4 — every flag we choose, active or not. Compiler/build-system plumbing (incremental
   * compilation, module & classpath, IR/codegen internals, Wasm/Native targets, debug dumps) is intentionally
   * omitted: the Gradle Kotlin plugin manages it, it is not a decision made here. Full list: `kotlinc -X`.
   *
   * Redundancy audit (run on every Kotlin / language-version bump): recompile and
   * `grep "is redundant for the current language version"` — a flag that warns has become the LV default;
   * move it from active into the "already default" list below. See the per-version argument-source links above.
   */
  /**
   * Requires explicit return types on public API. Not part of [freeCompilerArgs] because the value has
   * to match `-Xexplicit-api` whenever a module turns that on — the compiler rejects the two flags
   * with different values. `Utils.configureKotlin` therefore derives it per project from
   * `kotlin { explicitApi() }`; a module that does not set it keeps the repo-wide `warning`.
   */
  fun explicitReturnTypesArg(explicitApiStrict: Boolean): String =
    if (explicitApiStrict) "-XXexplicit-return-types=strict" else "-XXexplicit-return-types=warning"

  val freeCompilerArgs: List<String> = buildList {
    // Opt-ins for the experimental APIs we use.
    addAll(optInExperimentalAnnotations.map { "-opt-in=$it" })

    // Diagnostics & language mode (permanent policy, not language-version gated).
    add("-progressive") // apply new deprecations/migrations eagerly
    add("-Wextra") // extra opt-in compiler warnings
    add("-Xwarning-level=NOTHING_TO_INLINE:disabled") // do not warn on intentionally non-inline-worthy `inline` funs
    add("-Xreturn-value-checker=full") // report unused return values... (2.2)
    add("-Xwarning-level=RETURN_VALUE_NOT_USED:error") // ...and treat them as errors

    // Stabilization-track features (opt-in until they become the language-version default).
    add("-Xexpect-actual-classes") // silence expect/actual-class beta warning — KT-61573
    add("-Xconsistent-data-class-copy-visibility") // opt into the future default data-class copy() visibility — KT-11914
    add("-Xcontext-sensitive-resolution") // context-sensitive resolution (2.2)
    add("-Xname-based-destructuring=only-syntax") // name-based destructuring (2.3) — https://kotlinlang.org/docs/whatsnew2320.html
    add("-Xexplicit-context-arguments") // explicit passing of context arguments via named-argument syntax (2.4) — https://kotlinlang.org/docs/whatsnew-eap.html
    add("-Xcollection-literals") // bracket-syntax `[]` collection literals (2.4)
    add("-Xintrinsic-const-evaluation") // IntrinsicConstEvaluation language feature (2.4)
    add("-Xlocal-type-aliases") // `typealias` inside function bodies (2.4) — enabled in #1944

    // Not active — documented for reference. Status verified against the Kotlin 2.4.0 compiler (#1944).
    //
    // Already the DEFAULT at language version 2.4 — these language features are usable today WITHOUT any
    // flag; the flag only emits "is redundant for the current language version 2.4". Re-check on LV bumps:
    //   -Xnested-type-aliases             nested `typealias`, e.g. `object Foo { typealias Bar = ... }`
    //   -Xdata-flow-based-exhaustiveness  `when` exhaustiveness via data-flow analysis
    //   -Xmulti-dollar-interpolation      multi-dollar string templates (`$$"..."`), already used repo-wide
    //   -Xwhen-guards                     guard conditions in `when` (`is Foo if cond -> ...`)
    //   -Xnon-local-break-continue        `break`/`continue` out of inline-lambda loops
    //   -Xexplicit-backing-fields         explicit backing fields (`val x: T get() = field`)
    //   -Xcontext-parameters              `context(...)` parameters (the language feature itself)
    //
    // Deferred:
    //   -Xexplicit-api[=warning|strict]   also require explicit visibility on public API. #1944: 25,370
    //                                     violations at =warning (24,885 visibility + 485 return-type), far
    //                                     above the cleanup threshold, and warnings are globally suppressed
    //                                     anyway. Active -XXexplicit-return-types=warning covers the return half.
    //                                     Per-module opt-in exists: `kotlin { explicitApi() }` makes
    //                                     [explicitReturnTypesArg] follow it (commons/concurrent does).
    //                                     Rolling it out to the remaining open-source modules: #2829.
    //
    // Available but deliberately off:
    //   -Xreport-all-warnings             no effect — warning-level diagnostics are globally suppressed (suppressWarnings=true)
    //   -Xallow-returns-result-of         would force PRE-RELEASE binaries (returnsResultOf contract feeding the
    //                                     return-value checker); not worth the pre-release constraint
    //   -Xvalue-classes                   DEPRECATED flag; multi-field value classes stay experimental behind
    //                                     -XXLanguage:+JvmInlineMultiFieldValueClasses. No use case.
  }

  /**
   * Additional free compiler args for the JS target only (appended to [freeCompilerArgs]).
   * All permanent JS code-generation policy — none are language-version gated.
   */
  val additionalFreeCompilerArgsJS: List<String> = buildList {
    add("-Xfake-override-validator") // validate fake overrides in the JS IR
    add("-Xoptimize-generated-js") // optimize the generated JS
    add("-Xes-long-as-bigint") // represent Kotlin Long as JS BigInt (2.2)
    add("-Xir-dce") // dead-code elimination on the JS IR
    add("-Xir-dce-print-reachability-info") // companion to -Xir-dce (must be combined with it)
  }

  /**
   * Additional compiler args - only used for the JVM.
   *
   * [enhancedCoroutinesDebugging] appends `-Xenhanced-coroutines-debugging`, which adds line-number
   * instructions to every compiler-generated suspend func/lambda so a debugger can distinguish them
   * from user code. Off by default — it bloats bytecode across all 646 `suspend fun` files repo-wide.
   * Enable per build with `-PenhancedCoroutinesDebugging` when stepping through coroutines in a debugger.
   */
  fun additionalFreeCompilerArgsJVM(enhancedCoroutinesDebugging: Boolean): List<String> = buildList {
    // Permanent JVM bytecode/interop policy — none are language-version gated.
    add("-Xjsr305=strict") // treat JSR-305 nullability annotations strictly
    // Compile against the specified JDK API version (analogous to javac's --release).
    // Setting this also pins -jvm-target to the same version, so newer JDK APIs are
    // rejected by the compiler — protects against an unnoticed build-JDK bump.
    add("-Xjdk-release=${JvmTarget.JVM_25.target}")
    add("-Xemit-jvm-type-annotations") // type annotations (e.g. nullability on generics) in bytecode
    add("-Xuse-inline-scopes-numbers") // better stack traces for inline functions (Coroutines, Sequences)

    // Opt-in (off by default), toggled per build by -PenhancedCoroutinesDebugging — see the KDoc above.
    if (enhancedCoroutinesDebugging) {
      add("-Xenhanced-coroutines-debugging") // #1944: line-numbers for compiler-generated suspend code
    }

    // Not active — documented for reference (#1944). Already the DEFAULT at language version 2.4:
    //   -Xenhance-type-parameter-types-to-def-not-null  `@NotNull T` => `T & Any` (definitely-non-null) at the
    //     Java-interop boundary — already active at LV 2.4; the flag only emits a redundancy warning.
  }
}
