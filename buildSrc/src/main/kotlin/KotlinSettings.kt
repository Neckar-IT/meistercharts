import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/**
 * Kotlin settings
 */
object KotlinSettings {
  val languageVersion: KotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
  val languageVersionAsString: String = languageVersion.version

  /**
   * Currently updating to 2.0 does nto work with KSP
   * https://github.com/google/ksp/issues/1536
   */
  val apiVersion: KotlinVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
  val apiVersionAsString: String = apiVersion.version

  /**
   * Contains the annotations we opted in.
   * Can be used to configure Kotlin extensions directly
   */
  val optInExperimentalAnnotations: List<String> = listOf(
    "kotlin.ExperimentalStdlibApi", //additional methods in the std lib
    "kotlin.time.ExperimentalTime", //support for duration and other time related classes
    "kotlin.contracts.ExperimentalContracts", //allows the definition of contracts (e.g. how often a lambda is called in a method)
    "kotlin.experimental.ExperimentalTypeInference", //type inference
    "kotlin.js.ExperimentalJsExport", //required for @JsExport
    "kotlin.ExperimentalMultiplatform", //Multi-platform
    // "kotlinx.serialization.ExperimentalSerializationApi", //Seems to work only with free compiler args

    //"kotlinx.coroutines.FlowPreview", //Coroutines stuff
    "kotlin.ExperimentalUnsignedTypes", //Unsigned Types
    "kotlin.io.path.ExperimentalPathApi",

    "kotlin.io.encoding.ExperimentalEncodingApi", //Base64 encoding lib
    "kotlin.io.path.ExperimentalPathApi", //java.nio.file.Path support
    "kotlin.uuid.ExperimentalUuidApi", //UUID support (since 2.0.20)
    "kotlinx.serialization.ExperimentalSerializationApi", //Serialization
    "kotlinx.coroutines.ExperimentalCoroutinesApi", //Coroutines
  )

  /**
   * Language features that are enabled
   */
  val languageFeatures: List<String> = listOf(
    //"InlineClasses", //enable inline classes - no longer necessary since now value classes are/should be used
  )

  /**
   * Compiler arguments can be found here:
   * For common:
   * https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/CommonCompilerArguments.kt
   *
   * For JS:
   * https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JSCompilerArguments.kt
   *
   * For JVM:
   * https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JVMCompilerArguments.kt
   */

  /**
   * The free compiler args that must be used to configure the Kotlin compiler tasks.
   * These args are used for both JS and JVM
   */
  val freeCompilerArgs: List<String> = buildList {
    addAll(optInExperimentalAnnotations.map { "-opt-in=$it" }) //Opt in to the experimental features we are using
    add("-progressive") //Advanced compiler checks that are not always backwards compatible within a major version of Kotlin

    add("-Xexpect-actual-classes") //Enable expected/actual for classes/interfaces (https://youtrack.jetbrains.com/issue/KT-61573)
    add("-Xconsistent-data-class-copy-visibility") //Enable the new copy visibility
    add("-Xnon-local-break-continue") //Non local break and continue


    add("-Xsuppress-warning=NOTHING_TO_INLINE") //Suppress warnings globally!
    add("-Xmulti-dollar-interpolation") //Multi Dollar Interpolation (https://kotlinlang.org/docs/whatsnew21.html#multi-dollar-string-interpolation)

    if (languageVersion >= KotlinVersion.KOTLIN_2_2) {
      //Kotlin 2.2 required
      add("-Xannotation-default-target=param-property") //Annotations default updated (https://youtrack.jetbrains.com/issue/KT-73255)
    }

    //
    // Old compiler settings, for documentation purposes
    //
    // Context Receivers have been removed!
    //add("-Xcontext-receivers") //Enable context receivers (https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md#detailed-design
    //Stable since 1.9
    //add("-XXLanguage:+EnumEntries") //Enable enum entries (https://youtrack.jetbrains.com/issue/KT-54621/Preview-of-Enum.entries-modern-and-performant-replacement-for-Enum.values)
    //Use value classes instead
    //add("-Xinline-classes") //Enable inline classes
  }

  /**
   * Additional compiler args - only used for JS
   */
  val additionalFreeCompilerArgsJS: List<String> = buildList {
    //add("-Xir-property-lazy-initialization") //enable lazy initialize for top level JS properties //ATTENTION: Does not work with kvision (https://github.com/rjaros/kvision/issues/231)

    add("-Xfake-override-validator") //Enable the IR fake override validator.
    add("-Xoptimize-generated-js") //Perform additional optimizations on the generated JS code

    add("-Xtyped-arrays") //Translate primitive arrays into JS typed arrays.

    //The bundle gets much larger :-( [2024-11-04 - Kotlin 2.0.21-beta2]
    //add("-Xenable-extension-functions-in-externals") //Enable extension function members in external interfaces.

    //ATTENTION! Only activate in pairs!!!
    add("-Xir-dce-print-reachability-info") //Print reachability information about declarations to 'stdout' while performing DCE.
    add("-Xir-dce") //Is *required* to be set in combination with -Xir-dce-print-reachability-info
  }

  /**
   * Additional compiler args - only used for the JVM
   */
  val additionalFreeCompilerArgsJVM: List<String> = buildList {
    add("-Xjvm-default=all") //Enable generation of default methods in interfaces
    add("-Xjsr305=strict") //Strict null checks for kotlin projects
  }
}
