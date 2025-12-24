package it.neckar.open.kotlin.serializers

import kotlinx.serialization.json.Json

/**
 * Defines how values are included when serializing objects to JSON.
 *
 * This controls whether properties with default values or explicit `null`s are written.
 */
enum class JsonInclusionStrategy(
  /**
   * If `true`, properties with default values are encoded.
   */
  val encodeDefaults: Boolean,
  /**
   * If `true`, properties with value `null` are explicitly included in the JSON output.
   */
  val explicitNulls: Boolean,
) {
  /**
   * Encode only non-default properties but include explicit `null`s.
   *
   * This is the default behavior for `kotlinx.serialization.Json` [kotlinx.serialization.json.JsonConfiguration].
   */
  SkipDefaultsIncludeNulls(
    encodeDefaults = false,
    explicitNulls = true,
  ),

  /**
   * Encode all properties, including defaults, but omit explicit `null`s - event for defaults.
   */
  EncodeDefaultsSkipNulls(
    encodeDefaults = true,
    explicitNulls = false,
  ),

  /**
   * Encode all properties, including defaults and explicit `null`s.
   */
  EncodeDefaultsIncludeNulls(
    encodeDefaults = true,
    explicitNulls = true,
  ),

  /**
   * Encode only non-default properties, omit explicit `null`s.
   */
  SkipDefaultsSkipNulls(
    encodeDefaults = false,
    explicitNulls = false,
  ),
  ;

  /**
   * Creates a [Json] instance with the current inclusion strategy
   */
  fun json(prettyPrint: Boolean = true, prettyPrintIndent: String = "  "): Json {
    return Json {
      this.prettyPrint = prettyPrint
      this.prettyPrintIndent = prettyPrintIndent
      this.encodeDefaults = this@JsonInclusionStrategy.encodeDefaults
      this.explicitNulls = this@JsonInclusionStrategy.explicitNulls
    }
  }

  companion object {
    /**
     * The default for `kotlinx.serialization`
     */
    val Default: JsonInclusionStrategy = SkipDefaultsIncludeNulls
  }
}
