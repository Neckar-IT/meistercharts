package it.neckar.open.test.utils.testcontainer

import assertk.*
import assertk.assertions.*
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.deleteBucket
import aws.sdk.kotlin.services.s3.listBuckets
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url
import it.neckar.open.kotlin.lang.guessInCIEnvironment
import it.neckar.open.kotlin.lang.requireNotNull
import kotlinx.coroutines.*
import org.junit.jupiter.api.BeforeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for tests that require a MinIO container
 */
@Testcontainers
abstract class BaseMinIoContainerTest {
  @BeforeEach
  fun setUp() {
    /**
     * Delete all buckets
     */
    deleteAllBuckets()
  }

  /**
   * Deletes all buckets
   */
  protected fun deleteAllBuckets() {
    client().use { client ->
      runBlocking {
        client.listBuckets {
        }.buckets?.forEach {
          it.name?.let { bucket ->
            client.deleteBucket {
              this.bucket = bucket
            }
          }
        }

        assertThat(client.listBuckets().buckets.requireNotNull()).hasSize(0)
      }
    }
  }

  /**
   * Creates a new S3 client
   */
  fun client(): S3Client {
    return S3Client {
      endpointUrl = Url.parse("http://localhost:${container.firstMappedPort}")
      credentialsProvider = StaticCredentialsProvider(credentials = Credentials.invoke(username, password))
      region = "auto"
      forcePathStyle = true
    }
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("it.neckar.open.test.utils.testcontainer.BaseMinIoContainerTest")

    const val username: String = "minio"
    const val password: String = "minio123"

    @Container
    val container: MinIOContainer = MinIOContainer("minio/minio:RELEASE.2024-05-01T01-11-10Z")
      .also {
        if (guessInCIEnvironment().not()) {
          it.withExposedPorts(9000, 9001)
          it.portBindings = listOf("35000:9001")
        }
      }
      .withUserName(username)
      .withPassword(password)
      .waitingFor(HostPortWaitStrategy())
      .also {
        println("Starting MinIO container...")
        it.start()
        println("MinIO container started with exposed ports: ${it.exposedPorts}")
        println("Port bindings: ${it.portBindings}")
      }
  }
}
