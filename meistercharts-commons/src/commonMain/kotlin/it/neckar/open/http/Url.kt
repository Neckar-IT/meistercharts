package it.neckar.open.http

import it.neckar.open.http.io.UrlSerializer
import it.neckar.open.kotlin.lang.fromBase64
import it.neckar.open.kotlin.lang.toBase64
import it.neckar.projects.common.Port
import it.neckar.runtime.context.Hostname
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.uuid.Uuid


/**
 * Represents a URL (relative or absolute)
 */
@JsExport
@JsName("Url")
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

    /**
     * Creates a relative URL template
     */
    @JsExport.Ignore
    inline fun pattern(url: String): UrlPattern.Relative0 {
      return UrlPattern.Relative0(url)
    }

    @Deprecated("Inline")
    @JsExport.Ignore
    inline fun template(url: String): UrlPattern.Relative0 {
      return pattern(url)
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

    /**
     * HTTP url to localhost
     */
    val localhostHttp: Absolute = absolute("http://localhost/")

    val localhostHttp80: Absolute = localhostHttp(Port.HTTP)

    fun localhostHttp(port: Int = Port.HTTP.value): Absolute = absolute("http://localhost:$port/")

    fun localhostHttp(port: Port = Port.HTTP): Absolute = absolute("http://localhost:$port/")

    /**
     * Creates an HTTP URL. Standard port (80) is omitted for cleaner output.
     */
    @Suppress("HttpUrlsUsage")
    fun http(host: String, port: Port = Port.HTTP): Absolute {
      return if (port.value == 80) absolute("http://$host") else absolute("http://$host:$port")
    }

    /**
     * Creates an HTTP URL. Standard port (80) is omitted for cleaner output.
     */
    @Suppress("HttpUrlsUsage")
    fun http(host: Hostname, port: Port = Port.HTTP): Absolute {
      return if (port.value == 80) absolute("http://$host") else absolute("http://$host:$port")
    }

    /**
     * Creates an HTTPS URL. Standard port (443) is omitted for cleaner output.
     */
    fun https(host: String, port: Port = Port.HTTPS): Absolute {
      return if (port.value == 443) absolute("https://$host") else absolute("https://$host:$port")
    }

    /**
     * Creates an HTTPS URL. Standard port (443) is omitted for cleaner output.
     */
    fun https(host: Hostname, port: Port = Port.HTTPS): Absolute {
      return if (port.value == 443) absolute("https://$host") else absolute("https://$host:$port")
    }

    /**
     * Creates a URL from scheme, host, and port.
     * For http/https, standard ports (80/443) are omitted for cleaner output.
     */
    fun from(scheme: String, host: String, port: Port): Absolute {
      return when (scheme) {
        "http" -> http(host, port)
        "https" -> https(host, port)
        else -> absolute("$scheme://$host:${port.value}")
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
  data class DataScheme(
    /**
     * Base64 encoded data
     */
    override val value: String,
  ) : Url {
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

    companion object {
      fun image(bytes: ByteArray, imageFormat: String): DataScheme {
        return DataScheme("data:image/$imageFormat;base64,${bytes.toBase64()}")
      }

      fun png(bytes: ByteArray): DataScheme {
        return image(bytes, "png")
      }
    }
  }

  /**
   * Allows appending a relative URL to this.
   */
  @JsExport.Ignore
  interface RelativeAppender<T : RelativeAppender<T>> : Url {
    operator fun plus(toAppend: Relative): T
    operator fun plus(toAppend: String): T

    operator fun plus(toAppend: Uuid): T {
      return plus(toAppend.toString())
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
        "An absolute URL must start with a schema and contain [$SchemaDelimiter] but was [$value]"
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

    /**
     * Returns the protocol part of the URL (e.g. "https")
     */
    fun protocol(): String {
      return value.substringBefore(SchemaDelimiter)
    }

    /**
     * Returns the URL without the protocol part
     */
    fun withoutProtocol(): String {
      return value.substringAfter(SchemaDelimiter)
    }

    /**
     * Returns the URL without the path part
     */
    fun withoutPath(): String {
      return protocol() + SchemaDelimiter + withoutProtocol().substringBefore("/")
    }

    /**
     * Returns the protocol and host part of the URL (without port)
     */
    fun protocolAndHost(): String {
      return "${protocol()}://${hostPart()}"
    }

    /**
     * Returns the host part of the URL (without protocol and without port)
     */
    fun hostPart(): String {
      return withoutProtocol().substringBefore(":").substringBefore("/")
    }

    /**
     * Returns the host part with port (if available)
     */
    fun hostPartWithPort(): String {
      return withoutProtocol().substringBefore("/")
    }

    /**
     * Returns true if the URL has an explicit port specified.
     */
    fun hasExplicitPort(): Boolean {
      return withoutProtocol().contains(":")
    }

    /**
     * Returns the path part of the URL (starts with "/")
     */
    fun pathPart(): String {
      return withoutProtocol().substringAfter("/", "")
    }

    /**
     * Forces the provided port on the URL.
     */
    fun withForcedPort(port: Port): Absolute {
      return Absolute("${protocol()}://${hostPart()}:$port/${pathPart()}")
    }

    /**
     * Returns the port.
     * Guesses the port based on the protocol if no explicit port is defined.
     */
    fun port(): Port {
      val hostPartWithPort = hostPartWithPort()

      if (hostPartWithPort.contains(":")) {
        val portString = hostPartWithPort.substringAfter(":").substringBefore("/")
        return Port(portString.toInt())
      }

      return when (protocol().lowercase()) {
        "http" -> Port.HTTP
        "https" -> Port.HTTPS
        else -> throw IllegalStateException("Cannot guess port for protocol [${protocol()}] in URL [$value]")
      }
    }

    override fun toString(): String {
      return value
    }

    companion object {
      private const val SchemaDelimiter = "://"

      /**
       * Checks if the URL contains a scheme delimiter (e.g. "://")
       */
      fun containsSchemeDelimiter(url: String): Boolean = url.contains(SchemaDelimiter)
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
      // Check if we're trying to append a path after a query parameter or fragment
      if ((value.contains("?") || value.contains("#"))
          && !toAppend.startsWith("?")
          && !toAppend.startsWith("&")
          && !toAppend.startsWith("#")) {
        throw IllegalArgumentException("Cannot append path segment [$toAppend] after query parameter or fragment in [$value]")
      }
      return Relative(appendUrlStrings(value, toAppend))
    }

    /**
     * Appends an HTTP parameter to the URL.
     * Wraps the parameter in curly braces.
     */
    operator fun plus(imagePathBase64: UrlParameterName): UrlPattern.Relative1 {
      return UrlPattern.Relative1(appendUrlStrings(value, imagePathBase64.asUrlPatternParameter()), imagePathBase64)
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
 *
 * Special handling:
 * - Query parameters (starting with ?) are appended directly
 * - Additional query parameters (starting with &) are appended directly
 * - Fragment identifiers (starting with #) are appended directly
 * - Multiple slashes at boundaries are normalized to a single slash
 */
fun appendUrlStrings(value: String, toAppend: String): String {
  if (value.isEmpty()) {
    return toAppend
  }
  if (toAppend.isEmpty()) {
    return value
  }

  // Query parameters, additional query parameters, and fragments should be appended directly without slash
  if (toAppend.startsWith("?") || toAppend.startsWith("&") || toAppend.startsWith("#")) {
    return value + toAppend
  }

  // Normalize multiple slashes at the boundary
  // Remove all trailing slashes from value and all leading slashes from toAppend
  val valueTrimmed = value.trimEnd('/')
  val toAppendTrimmed = toAppend.trimStart('/')

  if (valueTrimmed.isEmpty()) {
    // If value was just slashes, keep one slash
    return if (toAppendTrimmed.isEmpty()) "/" else "/$toAppendTrimmed"
  }

  if (toAppendTrimmed.isEmpty()) {
    // If toAppend was just slashes, return value without trailing slashes
    return valueTrimmed
  }

  // Join with exactly one slash
  return "$valueTrimmed/$toAppendTrimmed"
}
