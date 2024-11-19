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
import java.io.DataInputStream
import java.net.URL
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

/**
 * X509 Support
 *
 */
open class X509Support
/**
 * Creates a new x509 support
 *
 * @param certificate the certificate
 * @param privateKey  the (optional) private key
 */
@JvmOverloads constructor(
  /**
   * Returns the certificate
   *
   * @return the certificate
   */
  val certificate: X509Certificate,
  private val privateKey: RSAPrivateKey? = null
) {
  /**
   * Creates a new X509 support without any signing capabilities
   *
   * @param certificate the certificate
   */
  constructor(certificate: URL) : this(certificate, null)

  /**
   * Creates a new X509 support
   *
   * @param certificate the certificate
   * @param privateKey  the private key (if available)
   */
  @Inject
  constructor(@CertificateUrl certificate: URL, @PrivateKeyUrl privateKey: URL?) : this(readCertificate(certificate), readPrivateKey(privateKey))

  /**
   * Returns whether the private key is available
   *
   * @return whether the private key is available
   */
  val privateKeyAvailable: Boolean
    get() = privateKey != null

  /**
   *
   * cipher
   *
   * @param plainText an array of byte.
   * @return an array of byte.
   *
   * @throws GeneralSecurityException
   * if any.
   */
  fun cipher(plainText: ByteArray): ByteArray {
    val cipher = Cipher.getInstance(RSA)
    cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey())
    return cipher.doFinal(plainText)
  }

  /**
   *
   * decipher
   *
   * @param bytes an array of byte.
   * @return an array of byte.
   */
  fun decipher(bytes: ByteArray): ByteArray {
    val cipher = Cipher.getInstance(RSA)
    cipher.init(Cipher.DECRYPT_MODE, certificate)
    return cipher.doFinal(bytes)
  }

  /**
   *
   * sign
   *
   * @param plainText an array of byte.
   * @return a it.neckar.open.crypt.Signature object.
   */
  fun sign(plainText: ByteArray): it.neckar.open.crypt.Signature {
    val signature = Signature.getInstance(SHA_256_WITH_RSA)
    signature.initSign(getPrivateKey())

    signature.update(plainText)
    return Signature(signature.sign())
  }

  /**
   *
   * verifySignature
   *
   * @param plainText an array of byte.
   * @param signature a it.neckar.open.crypt.Signature object.
   * @return a boolean.
   */
  fun verifySignature(plainText: ByteArray, signature: it.neckar.open.crypt.Signature): Boolean {
    val sign = Signature.getInstance(SHA_256_WITH_RSA)
    sign.initVerify(certificate)
    sign.update(plainText)
    return sign.verify(signature.getBytes())
  }

  /**
   * Returns the private key (if there is one)
   *
   * @return the private key
   */
  fun getPrivateKey(): RSAPrivateKey {
    return privateKey.checkNotNull { "Private key not available" }
  }

  companion object {
    const val RSA: String = "RSA"
    const val SHA_256_WITH_RSA: String = "SHA256withRSA"
    private const val X_509_CERTIFICATE_TYPE = "X.509"

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
      DataInputStream(privateKeyUrl.openStream()).use { `in` ->
        val keyBytes = IOUtils.toByteArray(`in`)
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
      //Read the cert
      DataInputStream(certificateUrl.openStream()).use { `in` ->
        val cf = CertificateFactory.getInstance(X_509_CERTIFICATE_TYPE)
        return cf.generateCertificate(`in`) as X509Certificate
      }
    }
  }
}
