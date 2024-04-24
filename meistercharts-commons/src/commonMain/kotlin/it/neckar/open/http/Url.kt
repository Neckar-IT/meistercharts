package it.neckar.open.http

import it.neckar.open.http.io.UrlSerializer
import it.neckar.open.kotlin.lang.fromBase64
import it.neckar.open.kotlin.lang.toBase64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Represents a URL (relative or absolute)
 */
@JsExport
@Serializable(with = UrlSerializer::class)
sealed interface Url {
  /**
   * The string value of the URL
   */
  val value: String

  @JsExport.Ignore
  companion object {
    val root: RootRelative = RootRelative.root

    /**
     * Creates a URL
     */
    @JsExport.Ignore
    @Deprecated("call correct method", ReplaceWith("value"), DeprecationLevel.ERROR)
    operator fun invoke(value: String): Absolute {
      TODO()
    }

    @JsExport.Ignore
    inline fun absolute(url: String): Absolute {
      return Absolute(url)
    }

    @JsExport.Ignore
    inline fun relative(url: String): Relative {
      return Relative(url)
    }

    @JsExport.Ignore
    inline fun rootRelative(url: String): RootRelative {
      return RootRelative(url)
    }

    @JsExport.Ignore
    inline fun data(url: String): DataScheme {
      return DataScheme(url)
    }

    @JsExport.Ignore
    inline fun image(bytes: ByteArray, imageFormat: String): DataScheme {
      return DataScheme("data:image/$imageFormat;base64,${bytes.toBase64()}")
    }

    @JsExport.Ignore
    inline fun image(bytesBase64Encoded: String, imageFormat: String): DataScheme {
      return DataScheme("data:image/$imageFormat;base64,$bytesBase64Encoded")
    }

    /**
     * Parses an URL
     */
    fun parse(url: String): Url {
      return when {
        url.startsWith("/") -> RootRelative(url)
        url.startsWith("data:") -> DataScheme(url)
        Absolute.containsSchemeDelimiter(url) -> Absolute(url)
        else -> Relative(url)
      }
    }
  }

  /**
   * A URL that represents a data.
   * E.g. a base64 encoded image: "data:image/jpg;base64,...."
   */
  @Serializable(with = UrlSerializer.DataScheme::class)
  @SerialName("data")
  @JsExport.Ignore
  data class DataScheme(override val value: String) : Url {
    init {
      require(value.startsWith("data:")) {
        "The URL must start with data: but was [$value]"
      }
    }

    /**
     * Returns the media type. E.g. "image/jpg"
     */
    val mediaType: String
      get() {
        return value.substringAfter("data:").substringBefore(";")
      }

    /**
     * Returns the data segment - as base64 encoded string
     */
    val data: String
      get() {
        return value.substringAfter("base64,")
      }

    /**
     * Returns the data segment - as a byte array
     */
    val dataBytes: ByteArray
      get() {
        return data.fromBase64()
      }

    /**
     * Returns true if this a data URL that contains an image
     */
    fun isImage(): Boolean {
      return mediaType.startsWith("image/")
    }

    /**
     * Returns true if this a data URL that contains an image with the given format
     */
    fun isImage(format: String): Boolean {
      return mediaType.startsWith("image/$format")
    }

    override fun toString(): String {
      return value
    }
  }

  /**
   * Allows appending a relative URL
   */
  @JsExport.Ignore
  interface RelativeAppender<T : RelativeAppender<T>> : Url {
    operator fun plus(toAppend: Relative): T
    operator fun plus(toAppend: String): T

    /**
     * Appends an HTTP parameter to the URL.
     * Wraps the parameter in curly braces.
     */
    operator fun plus(imagePathBase64: HttpParameterName): T {
      return plus("{${imagePathBase64.value}}")
    }
  }

  /**
   * Allows appending a root relative URL
   */
  @JsExport.Ignore
  interface RootRelativeAppender<T : RelativeAppender<T>> : RelativeAppender<T> {
    operator fun plus(toAppend: RootRelative): T
  }

  /**
   * A URL that contains a scheme (e.g. "https://")
   *
   * Attention:
   * Does *not* support "mailto:", "urn" or "tel")
   */
  @JsExport.Ignore
  @Serializable(with = UrlSerializer.Absolute::class)
  @SerialName("absolute")
  data class Absolute(override val value: String) : Url, RootRelativeAppender<Absolute> {
    init {
      require(
        containsSchemeDelimiter(value)
      ) {
        "An absolute URL must start with one of the allowed prefixes but was [$value]"
      }
    }

    /**
     * Appends something to the URL
     */
    override operator fun plus(toAppend: String): Absolute {
      return Absolute(appendUrlStrings(value, toAppend))
    }

    override operator fun plus(toAppend: Relative): Absolute {
      return plus(toAppend.value)
    }

    override operator fun plus(toAppend: RootRelative): Absolute {
      return plus(toAppend.value)
    }

    override fun toString(): String {
      return value
    }

    companion object {
      /**
       * Checks if the URL contains a scheme delimiter (e.g. "://")
       */
      fun containsSchemeDelimiter(url: String): Boolean = url.contains("://")
    }
  }

  /**
   * A URL that starts with "/" or "https"
   */
  @JsExport.Ignore
  @Serializable(with = UrlSerializer.RootRelative::class)
  @SerialName("rootRelative")
  data class RootRelative(override val value: String) : RelativeAppender<RootRelative> {
    init {
      require(value.startsWith("/")) {
        "A root relative URL must start with a / but was [$value]"
      }
    }

    override operator fun plus(toAppend: Relative): RootRelative {
      return plus(toAppend.value)
    }

    /**
     * Appends something to the URL
     */
    override operator fun plus(toAppend: String): RootRelative {
      return RootRelative(appendUrlStrings(value, toAppend))
    }

    override fun toString(): String {
      return value
    }

    companion object {
      val root: RootRelative = RootRelative("/")
    }
  }

  /**
   * A URL that just contains parts of the path
   */
  @JsExport.Ignore
  @Serializable(with = UrlSerializer.Relative::class)
  @SerialName("relative")
  data class Relative(override val value: String) : RelativeAppender<Relative> {
    init {
      require(value.startsWith("https").not()) {
        "A relative URL must not start with https but was [$value]"
      }
      require(value.startsWith("/").not()) {
        "A relative URL must not start with a / but was [$value]"
      }
    }

    override operator fun plus(toAppend: Relative): Relative {
      return plus(toAppend.value)
    }

    /**
     * Appends something to the URL
     */
    override operator fun plus(toAppend: String): Relative {
      return Relative(appendUrlStrings(value, toAppend))
    }

    override fun toString(): String {
      return value
    }
  }
}

/**
 * Concatenates two URL strings.
 * Adds a "/" between the two parts if necessary.
 *
 * This method should be used exclusively to concatenate URL parts.
 */
internal fun appendUrlStrings(value: String, toAppend: String): String {
  if (value.isEmpty()) {
    return toAppend
  }
  if (toAppend.isEmpty()) {
    return value
  }

  if (toAppend.startsWith("?")) {
    return value + toAppend
  }

  val endsWithSlash = value.endsWith("/")
  val startsWithSlash = toAppend.startsWith("/")

  if (endsWithSlash && startsWithSlash) {
    return value + toAppend.substring(1)
  }

  if (endsWithSlash || startsWithSlash) {
    return value + toAppend
  }

  return "$value/$toAppend"
}
