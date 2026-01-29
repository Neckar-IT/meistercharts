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
    "kotlin.concurrent.atomics.ExperimentalAtomicApi", //Atomic support (since 2.1.20)
    "kotlin.time.ExperimentalTime", //Time support (since 2.1.20)
    //ExperimentalAnnotationsInMetadata TODO enable for 2.2
  )

  /**
   * Language features that are enabled
   */
  val languageFeatures: List<String> = listOf(
    //"InlineClasses", //enable inline classes - no longer necessary since now value classes are/should be used
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
   *
   * Kotlin 2.1.x:
   * - https://github.com/JetBrains/kotlin/blob/v2.1.20/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/CommonCompilerArguments.kt
   * - https://github.com/JetBrains/kotlin/blob/v2.1.20/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JSCompilerArguments.kt
   * - https://github.com/JetBrains/kotlin/blob/v2.1.20/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JVMCompilerArguments.kt
   */

  /**
   * The free compiler args that must be used to configure the Kotlin compiler tasks.
   * These args are used for both JS and JVM
   *
   * See https://github.com/JetBrains/kotlin/blob/master/compiler/arguments/src/org/jetbrains/kotlin/arguments/description/CommonCompilerArguments.kt for a list of all available compiler arguments
   */
  val freeCompilerArgs: List<String> = buildList {
    addAll(optInExperimentalAnnotations.map { "-opt-in=$it" }) //Opt in to the experimental features we are using
    add("-progressive") //Advanced compiler checks that are not always backwards compatible within a major version of Kotlin
    add("-Wextra") //Additional compiler checks

    add("-Xwarning-level=NOTHING_TO_INLINE:disabled") //Suppress warnings for NOTHING_TO_INLINE globally!

    //Does not work with KSP Tests (it.neckar.ksp.KspExtKtTest) at the moment (Kotlin 2.2.0)
    //add("-Xnested-type-aliases") //Enable nested type aliases (https://kotlinlang.org/docs/type-aliases.html)

    add("-Xannotation-target-all") //Enable experimental language support for @all: annotation use-site target

    // Available compiler arguments (Kotlin 2.1.x, common).
    // add("-Xno-check-actual") // Do not check for the presence of the 'actual' modifier in multiplatform projects.
    // add("-Xnew-inference") // Enable the new experimental generic type inference algorithm.
    // add("-Xlegacy-smart-cast-after-try") // Allow 'var' smart casts even in the presence of assignments in 'try' blocks.
    // add("-Xverify-ir") // TODO: description missing
    // add("-Xverify-ir-visibility") // Check for visibility violations in IR when validating it before running any lowerings. Only has effect if '-Xverify-ir' is not 'none'.
    // add("-Xuse-k2") // Compile using the experimental K2 compiler pipeline. No compatibility guarantees are provided yet.
    // add("-Xuse-fir-experimental-checkers") // Enable experimental frontend IR checkers that are not yet ready for production.
    // add("-Xuse-fir-ic") // Compile using frontend IR internal incremental compilation. Warning: This feature is not yet production-ready.
    // add("-Xuse-fir-lt") // Compile using the LightTree parser with the frontend IR.
    // add("-Xmetadata-klib") // Produce a klib that only contains the metadata of declarations.
    // add("-Xexplicit-api") // Force the compiler to report errors on all public API declarations without an explicit visibility or a return type. Use the 'warning' level to issue warnings instead of errors.
    add("-XXexplicit-return-types=warning") // Force the compiler to report errors on all public API declarations without an explicit return type. Use the 'warning' level to issue warnings instead of errors. This flag partially enables functionality of `-Xexplicit-api` flag, so please don't use them altogether
    //add("-XXexplicit-return-types=strict") // Force the compiler to report errors on all public API declarations without an explicit return type. Use the 'warning' level to issue warnings instead of errors. This flag partially enables functionality of `-Xexplicit-api` flag, so please don't use them altogether

    //TODO  2026-01-06: Results in w: Flag is not supported by this version of the compiler: -Xextended-compiler-checks
    //add("-Xextended-compiler-checks") // Enable additional compiler checks that might provide verbose diagnostic information for certain errors. Warning: This mode is not backward compatible and might cause compilation errors in previously compiled code.
    add("-Xexpect-actual-classes") //Enable expected/actual for classes/interfaces (https://youtrack.jetbrains.com/issue/KT-61573)
    add("-Xconsistent-data-class-copy-visibility") // The effect of this compiler flag is the same as applying @ConsistentCopyVisibility annotation to all data classes in the module. See https://youtrack.jetbrains.com/issue/KT-11914
    // add("-Xunrestricted-builder-inference") // Eliminate builder inference restrictions, for example by allowing type variables to be returned from builder inference calls.
    // add("-Xenable-builder-inference") // Use builder inference by default for all calls with lambdas that can't be resolved without it. The corresponding calls' declarations may not be marked with @BuilderInference.
    // add("-Xself-upper-bound-inference") // Support inferring type arguments from the self-type upper bounds of the corresponding type parameters.
    add("-Xcontext-parameters") // Enable experimental context parameters.
    add("-Xnon-local-break-continue") // Enable experimental non-local break and continue.
    // add("-Xdirect-java-actualization") // Enable experimental direct Java actualization support.
    add("-Xmulti-dollar-interpolation") //Multi Dollar Interpolation (https://kotlinlang.org/docs/whatsnew21.html#multi-dollar-string-interpolation)
    // add("-Xenable-incremental-compilation") // Enable incremental compilation.
    // add("-Xrender-internal-diagnostic-names") // Render the internal names of warnings and errors.
    // add("-Xallow-any-scripts-in-source-roots") // Allow compiling scripts along with regular Kotlin sources.
    // add("-Xreport-all-warnings") // Report all warnings even if errors are found.
    // add("-Xfragments") // Declare all known fragments of a multiplatform compilation.
    // add("-Xfragment-sources") // Add sources to a specific fragment of a multiplatform compilation.
    // add("-Xfragment-refines") // Declare that <fromModuleName> refines <onModuleName> with the dependsOn/refines relation.
    // add("-Xignore-const-optimization-errors") // Ignore all compilation exceptions while optimizing some constant expressions.
    // add("-Xdont-warn-on-error-suppression") // Don't report warnings when errors are suppressed. This only affects K2.
    add("-Xwhen-guards") // Enable experimental language support for when guards.
    // add("-Xnested-type-aliases") // Enable experimental language support for nested type aliases.
    // add("-Xsuppress-warning") // Suppress specified warning module-wide.
    // add("-Xannotation-default-target") // Change the default annotation targets for constructor properties: -Xannotation-default-target=first-only: use the first of the following allowed targets: '@param:', '@property:', '@field:'; -Xannotation-default-target=first-only-warn: same as first-only, and raise warnings when both '@param:' and either '@property:' or '@field:' are allowed; -Xannotation-default-target=param-property: use '@param:' target if applicable, and also use the first of either '@property:' or '@field:'; default: 'first-only-warn' in language version 2.2+, 'first-only' in version 2.1 and before.
    // add("-XXdebug-level-compiler-checks") // Enable debug level compiler checks. ATTENTION: these checks can slow compiler down or even crash it.
    // add("-Xannotation-target-all") // Enable experimental language support for @all: annotation use-site target.

    //Kotlin 2.2 required
    add("-Xannotation-default-target=param-property") //Annotations default updated (https://youtrack.jetbrains.com/issue/KT-73255)

    //Kotlin 2.2.20 required
    add("-Xallow-condition-implies-returns-contracts") //Allow contracts that specify a limited conditional returns postcondition
    add("-Xallow-holdsin-contract") //Allow contracts that specify a condition that holds true inside a lambda argument
    add("-Xallow-contracts-on-more-functions") //Allow contracts on some operators and accessors, and allow checks for erased types
    add("-Xallow-reified-type-in-catch") //Allow 'catch' parameters to have reified types

    // Available compiler arguments (Kotlin 2.2.x, common).
    // add("-Xrepl") // Run Kotlin REPL (deprecated)
    // add("-Xreturn-value-checker") // Set improved unused return value checker mode. Use 'check' to run checker only and use 'full' to also enable automatic annotation insertion.
    add("-Xreturn-value-checker=full") // Set improved unused return value checker mode. Use 'check' to run checker only and use 'full' to also enable automatic annotation insertion.
    add("-Xcontext-sensitive-resolution") // Enable experimental context-sensitive resolution.
    // add("-Xdata-flow-based-exhaustiveness") // Enable `when` exhaustiveness improvements that rely on data-flow analysis.
    // add("-Xfragment-dependency") // Declare common klib dependencies for the specific fragment. This argument is required for any HMPP module except the platform leaf module: it takes dependencies from -cp/-libraries. The argument should be used only if the new compilation scheme is enabled with -Xseparate-kmp-compilation
    // add("-Xseparate-kmp-compilation") // Enables the separated compilation scheme, in which common source sets are analyzed against their own dependencies
    // add("-Xwarning-level") // Set the severity of the given warning. - `error` level raises the severity of a warning to error level (similar to -Werror but more granular) - `disabled` level suppresses reporting of a warning (similar to -nowarn but more granular) - `warning` level overrides -nowarn and -Werror for this specific warning (the warning will be reported/won't be considered as an error)
    // add("-XXlenient-mode") // Lenient compiler mode. When actuals are missing, placeholder declarations are generated.
    add("-Xallow-reified-type-in-catch") // Allow 'catch' parameters to have reified types.
    add("-Xallow-contracts-on-more-functions") // Allow contracts on some operators and accessors, and allow checks for erased types.
    add("-Xallow-condition-implies-returns-contracts") // Allow contracts that specify a limited conditional returns postcondition.
    add("-Xallow-holdsin-contract") // Allow contracts that specify a condition that holds true inside a lambda argument.

    // Available compiler arguments (Kotlin 2.3.x, common).
    // add("-Xcompiler-plugin-order") // Specify an execution order constraint for compiler plugins. Order constraint can be specified using the 'pluginId' of compiler plugins. The first specified plugin will be executed before the second plugin. Multiple constraints can be specified by repeating this option. Cycles in constraints will cause an error.
    // add("-Xdetailed-perf") // Enable more detailed performance statistics (Experimental). For Native, the performance report includes execution time and lines processed per second for every individual lowering. For WASM and JS, the performance report includes execution time and lines per second for each lowering of the first stage of compilation.
    // add("-XXdump-model") // Dump compilation model to specified directory for use in modularized tests.
    // add("-Xexplicit-backing-fields") // Enable experimental language support for explicit backing fields.
    // add("-Xfragment-friend-dependency") // Declare common klib friend dependencies for the specific fragment. This argument can be specified for any HMPP module except the platform leaf module: it takes dependencies from the platform specific friend module arguments. The argument should be used only if the new compilation scheme is enabled with -Xseparate-kmp-compilation
    // add("-Xname-based-destructuring") // Enables the following destructuring features: -Xname-based-destructuring=only-syntax: Enables syntax for positional destructuring with square brackets and the full form of name-based destructuring with parentheses; -Xname-based-destructuring=name-mismatch: Reports warnings when short form positional destructuring of data classes uses names that don't match the property names; -Xname-based-destructuring=complete: Enables short-form name-based destructuring with parentheses;


    //
    // Old compiler settings, for documentation purposes
    //
    // Context Receivers have been removed!
    //add("-Xcontext-receivers") //Enable context receivers (https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md#detailed-design
    //Stable since 1.9
    //add("-XXLanguage:+EnumEntries") //Enable enum entries (https://youtrack.jetbrains.com/issue/KT-54621/Preview-of-Enum.entries-modern-and-performant-replacement-for-Enum.values)
    //Use value classes instead
    //add("-Xinline-classes") //Enable inline classes
    //add("-Xsuppress-warning=NOTHING_TO_INLINE") //Old suppress warning until 2.1.20

  }

  /**
   * Additional compiler args - only used for JS
   */
  val additionalFreeCompilerArgsJS: List<String> = buildList {
    //add("-Xir-property-lazy-initialization") //enable lazy initialize for top level JS properties //ATTENTION: Does not work with kvision (https://github.com/rjaros/kvision/issues/231)

    add("-Xfake-override-validator") //Enable the IR fake override validator.
    add("-Xoptimize-generated-js") //Perform additional optimizations on the generated JS code

    // Available compiler arguments (Kotlin 2.1.x, js).
    // add("-output") // This option does nothing and is left for compatibility with the legacy backend. It is deprecated and will be removed in Kotlin 2.2.
    // add("-ir-output-dir") // Destination for generated files.
    // add("-ir-output-name") // Base name of generated files.
    // add("-no-stdlib") // This option does nothing and is left for compatibility with the legacy backend. It is deprecated and will be removed in Kotlin 2.2.
    // add("-libraries") // Paths to Kotlin libraries with .meta.js and .kjsm files, separated by the system path separator.
    // add("-source-map") // Generate a source map.
    // add("-source-map-prefix") // Add the specified prefix to the paths in the source map.
    // add("-source-map-base-dirs") // Base directories for calculating relative paths to source files in the source map.
    // add("-source-map-embed-sources") // Embed source files into the source map.
    // add("-source-map-names-policy") // Mode for mapping generated names to original names.
    // add("-meta-info") // This option does nothing and is left for compatibility with the legacy backend. It is deprecated and will be removed in Kotlin 2.2.
    // add("-target") // Generate JS files for the specified ECMA version.
    // add("-Xir-keep") // TODO: description missing
    // add("-module-kind") // The kind of JS module generated by the compiler. ES modules are enabled by default in case of ES2015 target usage
    // add("-main") // Specify whether the 'main' function should be called upon execution.
    // add("-Xir-produce-klib-dir") // Generate an unpacked klib into the parent directory of the output JS file.
    // add("-Xir-produce-klib-file") // Generate a packed klib into the directory specified by '-ir-output-dir'.
    // add("-Xir-produce-js") // Generate a JS file using the IR backend.
    // add("-Xir-dce") // Perform experimental dead code elimination.
    // add("-Xir-dce-runtime-diagnostic") // Enable runtime diagnostics instead of removing declarations when performing DCE.
    // add("-Xir-dce-print-reachability-info") // Print reachability information about declarations to 'stdout' while performing DCE.
    // add("-Xir-property-lazy-initialization") // Perform lazy initialization for properties.
    // add("-Xir-minimized-member-names") // Minimize the names of members.
    // add("-Xir-module-name") // Specify the name of the compilation module for the IR backend.
    // add("-Xir-safe-external-boolean") // Wrap access to external 'Boolean' properties with an explicit conversion to 'Boolean'.
    // add("-Xir-safe-external-boolean-diagnostic") // Enable runtime diagnostics when accessing external 'Boolean' properties.
    // add("-Xir-per-module") // Generate one .js file per module.
    // add("-Xir-per-module-output-name") // Add a custom output name to the split .js files.
    // add("-Xir-per-file") // Generate one .js file per source file.
    // add("-Xir-generate-inline-anonymous-functions") // Lambda expressions that capture values are translated into in-line anonymous JavaScript functions.
    // add("-Xinclude") // Path to an intermediate library that should be processed in the same manner as source files.
    // add("-Xcache-directory") // Path to the cache directory.
    // add("-Xir-build-cache") // Use the compiler to build the cache.
    // add("-Xgenerate-dts") // Generate a TypeScript declaration .d.ts file alongside the JS file.
    // add("-Xgenerate-polyfills") // Generate polyfills for features from the ES6+ standards.
    // add("-Xstrict-implicit-export-types") // Generate strict types for implicitly exported entities inside d.ts files.
    // add("-Xes-classes") // Let generated JavaScript code use ES2015 classes. Enabled by default in case of ES2015 target usage
    // add("-Xplatform-arguments-in-main-function") // JS expression that will be executed in runtime and be put as an Array<String> parameter of the main function
    // add("-Xes-generators") // Enable ES2015 generator functions usage inside the compiled code. Enabled by default in case of ES2015 target usage
    // add("-Xes-arrow-functions") // Use ES2015 arrow functions in the JavaScript code generated for Kotlin lambdas. Enabled by default in case of ES2015 target usage
    // add("-Xtyped-arrays") // This option does nothing and is left for compatibility with the legacy backend. It is deprecated and will be removed in a future release.
    // add("-Xfriend-modules-disabled") // Disable internal declaration export.
    // add("-Xfriend-modules") // Paths to friend modules.
    // add("-Xenable-extension-functions-in-externals") // Enable extension function members in external interfaces.
    // add("-Xfake-override-validator") // Enable the IR fake override validator.
    // add("-Xoptimize-generated-js") // Perform additional optimizations on the generated JS code.

    // Available compiler arguments (Kotlin 2.2.x, js).
    add("-Xes-long-as-bigint") // Compile Long values as ES2020 bigint instead of object.

    // Available compiler arguments (Kotlin 2.3.x, js).
    add("-Xenable-suspend-function-exporting") // Enable exporting suspend functions to JavaScript/TypeScript.
    //add("-Xtyped-arrays") //No longer supported in Kotlin 2.3+
    //add("-Xes-long-as-bigint") //Generates ES module syntax which is incompatible with Karma tests

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
    add("-Xjsr305=strict") //Strict null checks for kotlin projects

    add("-Xannotations-in-metadata") //Annotations in meta data

    // Available compiler arguments (Kotlin 2.1.x, jvm).
    // add("-no-reflect") // Don't automatically include the Kotlin reflection dependency in the classpath.
    // add("-Xallow-unstable-dependencies") // Do not report errors on classes in dependencies that were compiled by an unstable version of the Kotlin compiler.
    // add("-Xabi-stability") // When using unstable compiler features such as FIR, use 'stable' to mark generated class files as stable to prevent diagnostics from being reported when using stable compilers at the call site. When using the JVM IR backend, conversely, use 'unstable' to mark generated class files as unstable to force diagnostics to be reported.
    // add("-Xir-do-not-clear-binding-context") // When using the IR backend, do not clear BindingContext between 'psi2ir' and lowerings.
    // add("-Xassertions") // 'kotlin.assert' call behavior: -Xassertions=always-enable: enable, ignore JVM assertion settings; -Xassertions=always-disable: disable, ignore JVM assertion settings; -Xassertions=jvm: enable, depend on JVM assertion settings; -Xassertions=legacy: calculate the condition on each call, the behavior depends on JVM assertion settings in the kotlin package; default: legacy
    // add("-Xmultifile-parts-inherit") // Compile multifile classes as a hierarchy of parts and a facade.
    // add("-Xuse-type-table") // Use a type table in metadata serialization.
    // add("-Xuse-old-class-files-reading") // Use the old implementation for reading class files. This may slow down the compilation and cause problems with Groovy interop. This can be used in the event of problems with the new implementation.
    // add("-Xuse-fast-jar-file-system") // Use the fast implementation of Jar FS. This may speed up compilation time, but it is experimental.
    // add("-Xnullability-annotations") // TODO: description missing
    // add("-Xsupport-compatqual-checker-framework-annotations") // TODO: description missing
    // add("-Xjspecify-annotations") // Specify the behavior of 'jspecify' annotations. The default value is 'warn'.
    // add("-Xjvm-default") // TODO: description missing
    // Removed: add("-Xjvm-default=all") - now configured via compilerOptions.jvmDefault in Utils.kt
    // add("-Xgenerate-strict-metadata-version") // TODO: description missing
    // add("-Xsanitize-parentheses") // TODO: description missing
    // add("-Xfriend-paths") // TODO: description missing
    // add("-Xemit-jvm-type-annotations") // Emit JVM type annotations in bytecode.
    // add("-Xstring-concat") // Select the code generation scheme for string concatenation: -Xstring-concat=indy-with-constants Concatenate strings using 'invokedynamic' and 'makeConcatWithConstants'. This requires '-jvm-target 9' or greater. -Xstring-concat=indy Concatenate strings using 'invokedynamic' and 'makeConcat'. This requires '-jvm-target 9' or greater. -Xstring-concat=inline Concatenate strings using 'StringBuilder' default: 'indy-with-constants' for JVM targets 9 or greater, 'inline' otherwise.
    // add("-Xsam-conversions") // Select the code generation scheme for SAM conversions. -Xsam-conversions=indy Generate SAM conversions using 'invokedynamic' with 'LambdaMetafactory.metafactory'. -Xsam-conversions=class Generate SAM conversions as explicit classes. The default value is 'indy'.
    // add("-Xlambdas") // TODO: description missing
    // add("-Xjvm-enable-preview") // Allow using Java features that are in the preview phase. This works like '--enable-preview' in Java. All class files are marked as compiled with preview features, meaning it won't be possible to use them in release environments.
    // add("-Xsuppress-deprecated-jvm-target-warning") // Suppress warnings about deprecated JVM target versions. This option has no effect and will be deleted in a future version.
    // add("-Xtype-enhancement-improvements-strict-mode") // Enable strict mode for improvements to type enhancement for loaded Java types based on nullability annotations, including the ability to read type-use annotations from class files. See KT-45671 for more details.
    // add("-Xserialize-ir") // TODO: description missing
    // add("-Xvalidate-bytecode") // Validate generated JVM bytecode before and after optimizations.
    // add("-Xenhance-type-parameter-types-to-def-not-null") // TODO: description missing
    // add("-Xlink-via-signatures") // Link JVM IR symbols via signatures instead of descriptors. This mode is slower, but it can be useful for troubleshooting problems with the JVM IR backend. This option is deprecated and will be deleted in future versions. It has no effect when -language-version is 2.0 or higher.
    // add("-Xvalue-classes") // Enable experimental value classes.
    // add("-Xir-inliner") // Inline functions using the IR inliner instead of the bytecode inliner.
    // add("-Xuse-inline-scopes-numbers") // Use inline scopes numbers for inline marker variables.
    // add("-Xuse-k2-kapt") // Enable the experimental support for K2 KAPT.

    // Available compiler arguments (Kotlin 2.2.x, jvm).
    // add("-jvm-default") // Emit JVM default methods for interface declarations with bodies. The default is 'enable'. -jvm-default=enable Generate default methods for non-abstract interface declarations, as well as 'DefaultImpls' classes with static methods for compatibility with code compiled in the 'disable' mode. This is the default behavior since language version 2.2. -jvm-default=no-compatibility Generate default methods for non-abstract interface declarations. Do not generate 'DefaultImpls' classes. -jvm-default=disable Do not generate JVM default methods. This is the default behavior up to language version 2.1.
    // add("-Xjvm-expose-boxed") // Expose inline classes and functions, accepting and returning them, to Java.
    // add("-Xindy-allow-annotated-lambdas") // Allow using 'invokedynamic' for lambda expressions with annotations
    // add("-Xenhanced-coroutines-debugging") // Generate additional linenumber instruction for compiler-generated code inside suspend functions and lambdas to distinguish them from user code by debugger.
    // add("-Xannotations-in-metadata") // Write annotations on declarations into the metadata (in addition to the JVM bytecode), and read annotations from the metadata if they are present.
    // add("-Xwhen-expressions") // Select the code generation scheme for type-checking 'when' expressions: -Xwhen-expressions=indy Generate type-checking 'when' expressions using 'invokedynamic' with 'SwitchBootstraps.typeSwitch(..)' and following 'tableswitch' or 'lookupswitch'. This requires '-jvm-target 21' or greater. -Xwhen-expressions=inline Generate type-checking 'when' expressions as a chain of type checks. The default value is 'inline'.

    // Available compiler arguments (Kotlin 2.3.x, jvm).

  }
}
