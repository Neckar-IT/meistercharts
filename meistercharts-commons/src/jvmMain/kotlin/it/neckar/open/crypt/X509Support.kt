/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
 */

package it.neckar.open.crypt

import it.neckar.open.kotlin.lang.checkNotNull
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import kotlin.io.path.inputStream
import java.net.URL
import java.nio.file.Path
import java.security.KeyFactory
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * X509 Support provides methods to cipher, decipher, sign and verify signatures using an X509 certificate and an optional private key.
 *
 * This class can be instantiated with a private key or without one
 */
@OptIn(ExperimentalEncodingApi::class)
open class X509Support(
  /**
   * The certificate
   */
  val certificate: X509Certificate,
  /**
   * The private key - if available.
   *
   * Some methods require the private key to be available, e.g. signing.
   */
  private val privateKey: RSAPrivateKey? = null
) {
  /**
   * Returns whether the private key is available
   *
   * @return whether the private key is available
   */
  val privateKeyAvailable: Boolean
    get() = privateKey != null

  /**
   * Calculates the cipher text for the given plain text using the private key.
   */
  @RequiresPrivateKey
  fun cipher(plainText: ByteArray): ByteArray {
    val cipher = Cipher.getInstance(RSA)
    cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey())
    return cipher.doFinal(plainText)
  }

  /**
   * Deciphers the given byte array using the certificate.
   */
  fun decipher(bytes: ByteArray): ByteArray {
    val cipher = Cipher.getInstance(RSA)
    cipher.init(Cipher.DECRYPT_MODE, certificate)
    return cipher.doFinal(bytes)
  }

  /**
   * Calculates the signature for the given plain text using the private key.
   */
  @RequiresPrivateKey
  fun sign(plainText: ByteArray): it.neckar.open.crypt.Signature {
    val signature = Signature.getInstance(SHA_256_WITH_RSA)
    signature.initSign(getPrivateKey())

    signature.update(plainText)
    return Signature(signature.sign())
  }

  /**
   * Verifies the signature for the given plain text using the certificate.
   */
  fun verifySignature(plainText: ByteArray, signature: it.neckar.open.crypt.Signature): Boolean {
    val sign = Signature.getInstance(SHA_256_WITH_RSA)
    sign.initVerify(certificate)
    sign.update(plainText)
    return sign.verify(signature.getBytes())
  }

  /**
   * Returns the private key (if there is one)
   */
  @RequiresPrivateKey
  fun getPrivateKey(): RSAPrivateKey {
    return privateKey.checkNotNull { "Private key not available" }
  }

  companion object {
    const val RSA: String = "RSA"
    const val SHA_256_WITH_RSA: String = "SHA256withRSA"
    private const val X_509_CERTIFICATE_TYPE = "X.509"

    /**
     * Creates a new instance of [X509Support] using the given certificate and private key urls.
     */
    operator fun invoke(
      @CertificateUrl certificateUrl: URL,
      @PrivateKeyUrl privateKeyUrl: URL? = null
    ): X509Support {
      return X509Support(readCertificate(certificateUrl), readPrivateKey(privateKeyUrl))
    }

    /**
     * Reads a private key form a url
     *
     * @param privateKeyUrl the url containing the private key
     * @return the read private key
     */
    fun readPrivateKey(privateKeyUrl: URL?): RSAPrivateKey? {
      //If a null url is given - just return null
      if (privateKeyUrl == null) {
        return null
      }

      //We have an url --> return it
      DataInputStream(privateKeyUrl.openStream()).use { inputStream ->
        val keyBytes = IOUtils.toByteArray(inputStream)
        val keyFactory = KeyFactory.getInstance(RSA)

        val privSpec = PKCS8EncodedKeySpec(keyBytes)
        return keyFactory.generatePrivate(privSpec) as RSAPrivateKey
      }
    }

    /**
     * Reads the x509 certificate from the given url
     *
     * @param certificateUrl the certificate url
     * @return the certificate
     */
    fun readCertificate(certificateUrl: URL): X509Certificate {
      //Read the certificate from the url
      DataInputStream(certificateUrl.openStream()).use { inputStream ->
        val cf = CertificateFactory.getInstance(X_509_CERTIFICATE_TYPE)
        return cf.generateCertificate(inputStream) as X509Certificate
      }
    }

    /**
     * Reads an X.509 certificate from a PEM file at the given path.
     */
    fun readCertificate(
      /**
       * Path to the PEM certificate file
       */
      certPath: Path,
    ): X509Certificate {
      val certificateFactory = CertificateFactory.getInstance(X_509_CERTIFICATE_TYPE)

      certPath.inputStream().use { inputStream ->
        val certificate = certificateFactory.generateCertificate(inputStream)

        require(certificate is X509Certificate) {
          "Certificate at $certPath is not an X.509 certificate"
        }

        return certificate
      }
    }

    /**
     * Loads an X.509 certificate from a Base64-encoded PEM string.
     *
     * Only PEM format is supported (Base64-encoded PEM file content with headers).
     */
    fun readCertificate(
      /**
       * Base64-encoded PEM certificate.
       */
      base64Certificate: Base64CertificatePem,
    ): X509Certificate {
      val certificateFactory = CertificateFactory.getInstance(X_509_CERTIFICATE_TYPE)

      // Use the decodeToString() method from Base64CertificatePem to get the PEM content
      val pemContent = base64Certificate.decodeToString()
      val certificateBytes = extractCertificateFromPem(pemContent)

      ByteArrayInputStream(certificateBytes).use { inputStream ->
        val certificate = certificateFactory.generateCertificate(inputStream)

        require(certificate is X509Certificate) {
          "Decoded content is not an X.509 certificate"
        }

        return certificate
      }
    }

    /**
     * Extracts the certificate bytes from a PEM-formatted string.
     * Handles both standard PEM format and PEM with different header styles.
     */
    private fun extractCertificateFromPem(pemContent: String): ByteArray {
      // Remove headers, footers, and whitespace
      val base64Content = pemContent
        .replace(Base64CertificatePem.BeginCertificate, "")
        .replace(Base64CertificatePem.EndCertificate, "")
        .replace(Base64CertificatePem.BeginTrustedCertificate, "")
        .replace(Base64CertificatePem.EndTrustedCertificate, "")
        .replace("""\s""".toRegex(), "")

      return Base64.decode(base64Content)
    }

    /**
     * Annotation to mark a method that requires a private key to be available.
     */
    annotation class RequiresPrivateKey
  }
}
