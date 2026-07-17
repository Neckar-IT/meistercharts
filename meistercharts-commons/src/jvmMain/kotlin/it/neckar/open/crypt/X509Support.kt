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
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
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
   * Encrypts the given plain text for confidentiality using the certificate's public key.
   * Only the holder of the matching private key can [decipher] the result, so the certificate
   * (which is distributed) is sufficient to encrypt but not to decrypt.
   */
  fun cipher(plainText: ByteArray): ByteArray {
    val cipher = Cipher.getInstance(RSA_OAEP)
    cipher.init(Cipher.ENCRYPT_MODE, certificate.publicKey, OaepParameters)
    return cipher.doFinal(plainText)
  }

  /**
   * Decrypts a cipher text produced by [cipher] using the private key.
   */
  @RequiresPrivateKey
  fun decipher(bytes: ByteArray): ByteArray {
    val cipher = Cipher.getInstance(RSA_OAEP)
    cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(), OaepParameters)
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
    const val RSA_OAEP: String = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    const val SHA_256_WITH_RSA: String = "SHA256withRSA"
    private const val X_509_CERTIFICATE_TYPE = "X.509"

    /**
     * OAEP parameters that pin both the label digest and the MGF1 digest to SHA-256.
     * Without this the JDK keeps MGF1 on its SHA-1 default even when the transformation
     * string requests SHA-256, silently mixing digests. [cipher] and [decipher] must use
     * the same parameters.
     */
    private val OaepParameters: OAEPParameterSpec =
      OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)

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
     * Reads all X.509 certificates from a PEM file that may contain multiple certificates.
     */
    fun readCertificates(
      /**
       * Path to the PEM certificate file (may contain one or more certificates)
       */
      certPath: Path,
    ): List<X509Certificate> {
      val certificateFactory = CertificateFactory.getInstance(X_509_CERTIFICATE_TYPE)

      return certPath.inputStream().use { inputStream ->
        certificateFactory.generateCertificates(inputStream)
          .map { certificate ->
            require(certificate is X509Certificate) {
              "Certificate at $certPath is not an X.509 certificate"
            }
            certificate
          }
      }
    }

    /**
     * Reads all X.509 certificates from a PEM byte array that may contain multiple certificates.
     */
    fun readCertificates(
      /**
       * Raw PEM content as byte array
       */
      pemBytes: ByteArray,
    ): List<X509Certificate> {
      val certificateFactory = CertificateFactory.getInstance(X_509_CERTIFICATE_TYPE)

      return ByteArrayInputStream(pemBytes).use { inputStream ->
        certificateFactory.generateCertificates(inputStream)
          .map { certificate ->
            require(certificate is X509Certificate) {
              "PEM content does not contain valid X.509 certificates"
            }
            certificate
          }
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
