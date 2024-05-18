package it.neckar.open.test.utils.testcontainer

import assertk.*
import assertk.assertions.*
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.deleteBucket
import aws.sdk.kotlin.services.s3.listBuckets
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url
import it.neckar.open.kotlin.lang.guessDebugging
import it.neckar.open.kotlin.lang.requireNotNull
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.junit.jupiter.Container

/**
 * Base class for tests that require a MinIO container
 */
@Suppress("HttpUrlsUsage")
abstract class BaseMinIoContainerTest {
  @BeforeEach
  fun setUp() {
    runBlocking {
      /**
       * Delete all buckets
       */
      deleteAllBuckets()
    }
  }

  /**
   * Deletes all buckets
   */
  protected suspend fun deleteAllBuckets() {
    client().use { client ->
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

  /**
   * Creates a new S3 client
   */
  fun client(): S3Client {
    return S3Client {
      endpointUrl = Url.parse("http://${container.host}:${container.firstMappedPort}")
      credentialsProvider = StaticCredentialsProvider(credentials = Credentials.invoke(username, password))
      region = "auto"
      forcePathStyle = true
    }
  }

  /**
   * Executes the given block with a new client.
   */
  suspend fun withClient(block: suspend (client: S3Client) -> Unit) {
    client().use { client ->
      block(client)
    }
  }

  companion object {
    const val username: String = "minio"
    const val password: String = "minio123"

    @Container
    val container: MinIOContainer = MinIOContainer("minio/minio:RELEASE.2024-05-01T01-11-10Z")
      .withUserName(username)
      .withPassword(password)
      .waitingFor(HostPortWaitStrategy())

      .also { container ->
        println("Starting MinIO container...")
        container.start()
        println("MinIO container started with exposed ports: ${container.exposedPorts}")
        println("S3 URL: ${container.s3URL}")
        println("Container Name: ${container.containerName}")
        println("Host: ${container.host}")
        println("Extra hosts: ${container.extraHosts}")
        println("Port bindings: ${container.portBindings}")
        println("First Mapped port: ${container.firstMappedPort}")
        container.exposedPorts.forEach {
          println("\tExposed Port: $it - mapped to ${container.getMappedPort(it)}")
        }

        if (guessDebugging()) {
          //val port = it.firstMappedPort
          val webUiPort = container.getMappedPort(container.exposedPorts[1]) //the second port is the port for the web UI

          println("------------------------------")
          val connectionUrl = "http://127.0.0.1:$webUiPort/"

          //Enable OpenID authentication
          kotlin.run {
            // Set the OIDC environment variables after getting the mapped port
            val openIdConfigUrl = """https://auth.neckar.it/realms/main/.well-known/openid-configuration"""
            val openIdClientSecret = """ZxRYn5YAQ78UkxW5Uf5oX9qFjhpwAs1Y"""
            val openIdRedirectUrl = """http://${container.host}:$webUiPort/oauth_callback"""
            val openIdClaim = """policy"""

            val result = container.execInContainer(
              "sh", "-c", """
                mc alias set myminio http://${container.host}:9000 $username $password && \
                mc admin config set myminio identity_openid config_url=$openIdConfigUrl client_id=MinIO-testcontainers client_secret=$openIdClientSecret claim_name=$openIdClaim redirect_uri=$openIdRedirectUrl && \
                mc admin service restart myminio
            """.trimIndent()
            )

            println("-- Configure MinIO in container [stdout] --")
            println(result.stdout)
            println("----------------------------------------------------------")
            println("-- Configure MinIO in container [stderr] --")
            println(result.stderr)
            println("----------------------------------------------------------")

            assertThat(result.exitCode).isEqualTo(0)
          }

          println(" Connect to MinIO: $connectionUrl")
          println("     User:     $username")
          println("     Password: $password")
          println("------------------------------")

          openMinIoWeb(connectionUrl)
        }
      }

    private fun openMinIoWeb(connectionUrl: String) {
      Runtime.getRuntime().exec("open $connectionUrl")
    }

    /**
     * Keeps the JVM open and the container running if debugging is detected.
     */
    @JvmStatic
    @AfterAll
    fun tearDown() {
      if (guessDebugging()) {
        println("------------------------------")
        println("------------------------------")
        println("------------------------------")
        println(" Debugging Mode detected - Keeping JVM Open and Database Container active!")
        println("------------------------------")
        println("------------------------------")
        println("------------------------------")
        println("Container ID: ${container.containerId}")
        println("Image: ${container.dockerImageName}")
        println("Exposed Ports: ${container.exposedPorts}")
        println("------------------------------")
        Thread.sleep(1000000000000L)
      }
    }
  }
}
