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

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Configures the JVM to trust an additional custom CA certificate alongside the system CAs.
 *
 * Provides two levels of API:
 * - [addToJvmDefaultTrustStore]: Adds a custom CA to the JVM's default trust by setting both the
 *   default [SSLContext] and the `javax.net.ssl.trustStore` system property. This ensures that
 *   **all** HTTPS connections — including libraries like Jersey/GitLab4J that read the system
 *   property — use the combined trust store.
 * - [createSslContextWithCustomCa]: Creates a standalone [SSLContext] without modifying JVM-global state.
 *
 * Both are built from the same building blocks: [loadSystemTrustStore], [createTrustStoreWithCustomCa],
 * and [createSslContextFromTrustStore].
 */
object CustomCaTrustConfiguration {
  private val logger = LoggerFactory.getLogger(CustomCaTrustConfiguration::class.java)

  private const val TruststorePassword = "changeit"

  /**
   * The original system CA certificates, cached on first access before any modifications
   * to `javax.net.ssl.trustStore`. This prevents accumulation of previous custom CAs
   * when [addToJvmDefaultTrustStore] is called multiple times.
   */
  private val originalSystemCaCertificates: List<X509Certificate> by lazy {
    val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(null as KeyStore?)

    trustManagerFactory.trustManagers
      .filterIsInstance<X509TrustManager>()
      .firstOrNull()
      ?.acceptedIssuers
      ?.toList()
      ?: throw IllegalStateException("No X509TrustManager found in default trust managers")
  }

  /**
   * Adds the custom CA [certificate] to the JVM's default trust.
   *
   * After this call, all subsequent HTTPS connections trust both system CAs and the custom CA.
   * Internally, this creates a combined trust store and:
   * - writes it to a temp file and sets the `javax.net.ssl.trustStore` system property
   *   (for libraries that read the system property directly)
   * - sets the JVM's default [SSLContext] via [SSLContext.setDefault]
   *   (for code that uses [SSLContext.getDefault])
   *
   * Must be called before creating any HTTPS connections that need to trust the custom CA.
   */
  fun addToJvmDefaultTrustStore(certificate: X509Certificate) {
    logger.info("Adding custom CA to JVM default trust:")
    logger.info("  Subject: ${certificate.subjectX500Principal}")
    logger.info("  Issuer:  ${certificate.issuerX500Principal}")
    logger.info("  Valid:   ${certificate.notBefore} - ${certificate.notAfter}")

    val trustStore: KeyStore = createTrustStoreWithCustomCa(certificate)

    setTrustStoreSystemProperties(writeTrustStoreToTempFile(trustStore))
    setDefaultSslContext(trustStore)

    logger.info("JVM default trust configured successfully")
  }

  /**
   * Reads the certificate from [caCertPath] and adds it to the JVM's default trust.
   *
   * Convenience overload — see [addToJvmDefaultTrustStore] for details.
   */
  fun addToJvmDefaultTrustStore(caCertPath: Path) {
    logger.info("Reading certificate from file: $caCertPath")
    addToJvmDefaultTrustStore(X509Support.readCertificate(caCertPath))
  }

  /**
   * Decodes the Base64-encoded PEM certificate and adds it to the JVM's default trust.
   *
   * Convenience overload — see [addToJvmDefaultTrustStore] for details.
   */
  fun addToJvmDefaultTrustStore(base64Certificate: Base64CertificatePem) {
    logger.info("Decoding Base64 certificate (${base64Certificate.value.length} chars)")
    addToJvmDefaultTrustStore(X509Support.readCertificate(base64Certificate))
  }

  /**
   * Creates an [SSLContext] that trusts both system CAs and the given custom CA [certificate].
   * Does **not** modify JVM-global state.
   */
  fun createSslContextWithCustomCa(certificate: X509Certificate): SSLContext {
    return createSslContextFromTrustStore(createTrustStoreWithCustomCa(certificate))
  }

  /**
   * Creates a [KeyStore] containing all system CAs plus the given [customCa].
   */
  internal fun createTrustStoreWithCustomCa(customCa: X509Certificate): KeyStore {
    val trustStore: KeyStore = loadSystemTrustStore()
    trustStore.setCertificateEntry("custom-ca", customCa)
    return trustStore
  }

  /**
   * Creates a fresh [KeyStore] containing the original system CA certificates.
   *
   * Always returns the same set of certificates regardless of whether [addToJvmDefaultTrustStore]
   * has already modified the JVM's trust configuration. This is achieved by caching the original
   * certificates in [originalSystemCaCertificates] before any modifications.
   */
  internal fun loadSystemTrustStore(): KeyStore {
    val trustStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    trustStore.load(null, null)

    originalSystemCaCertificates.forEachIndexed { index, cert ->
      trustStore.setCertificateEntry("system-ca-$index", cert)
    }

    return trustStore
  }

  /**
   * Creates an [SSLContext] initialized with the trust managers from the given [trustStore].
   */
  private fun createSslContextFromTrustStore(trustStore: KeyStore): SSLContext {
    val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(trustStore)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagerFactory.trustManagers, null)
    return sslContext
  }

  /**
   * Writes the given [trustStore] to a temporary JKS file that is deleted on JVM exit.
   */
  private fun writeTrustStoreToTempFile(trustStore: KeyStore): File {
    val trustStoreFile: File = File.createTempFile("truststore-", ".jks")
    trustStoreFile.deleteOnExit()
    trustStoreFile.outputStream().use { outputStream ->
      trustStore.store(outputStream, TruststorePassword.toCharArray())
    }
    logger.info("  Trust store ({} certificates) written to: {}", trustStore.size(), trustStoreFile.absolutePath)
    return trustStoreFile
  }

  /**
   * Sets the `javax.net.ssl.trustStore` and `javax.net.ssl.trustStorePassword` system properties
   * to point to the given [trustStoreFile].
   */
  private fun setTrustStoreSystemProperties(trustStoreFile: File) {
    System.setProperty("javax.net.ssl.trustStore", trustStoreFile.absolutePath)
    System.setProperty("javax.net.ssl.trustStorePassword", TruststorePassword)
  }

  /**
   * Creates an [SSLContext] from the given [trustStore] and sets it as the JVM's default
   * via [SSLContext.setDefault].
   */
  private fun setDefaultSslContext(trustStore: KeyStore) {
    SSLContext.setDefault(createSslContextFromTrustStore(trustStore))
  }
}
