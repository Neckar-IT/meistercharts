/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
