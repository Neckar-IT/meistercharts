package it.neckar.open.crypt

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Represents a Base64-encoded CA certificate in PEM format.
 *
 * The certificate must be a Base64-encoded PEM file content (including headers like -----BEGIN CERTIFICATE-----).
 * Only text PEM format is supported, not raw DER-encoded certificates.
 */
@OptIn(ExperimentalEncodingApi::class)
@JvmInline
value class Base64CertificatePem(val value: String) {
  init {
    require(value.isNotBlank()) { "Base64 certificate must not be blank" }

    // Validate by decoding - this will throw if Base64 is invalid
    val decodedString = decodeToString()

    // Content must be PEM format with valid certificate header
    require(
      decodedString.contains(BeginCertificate) || decodedString.contains(BeginTrustedCertificate)
    ) { "PEM content must contain a valid certificate header ($BeginCertificate or $BeginTrustedCertificate)" }
  }

  /**
   * Decodes the Base64-encoded certificate to raw bytes (PEM file content).
   */
  fun decode(): ByteArray {
    return try {
      Base64.decode(value.trim())
    } catch (e: IllegalArgumentException) {
      throw IllegalArgumentException("Invalid Base64 encoding: ${e.message}", e)
    }
  }

  /**
   * Decodes the Base64-encoded certificate and returns it as a string (PEM format with headers).
   */
  fun decodeToString(): String {
    return decode().decodeToString()
  }

  companion object {
    /**
     * Standard PEM header for X.509 certificates.
     */
    const val BeginCertificate: String = "-----BEGIN CERTIFICATE-----"

    /**
     * Standard PEM footer for X.509 certificates.
     */
    const val EndCertificate: String = "-----END CERTIFICATE-----"

    /**
     * PEM header for trusted certificates (alternative format).
     */
    const val BeginTrustedCertificate: String = "-----BEGIN TRUSTED CERTIFICATE-----"

    /**
     * PEM footer for trusted certificates (alternative format).
     */
    const val EndTrustedCertificate: String = "-----END TRUSTED CERTIFICATE-----"
  }
}
