package it.neckar.open.test.utils.testcontainer

import assertk.*
import assertk.assertions.*
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.ClientSession
import com.mongodb.kotlin.client.coroutine.MongoClient
import it.neckar.open.kotlin.lang.guessDebugging
import it.neckar.open.kotlin.lang.guessDebugging
import kotlinx.coroutines.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.utility.DockerImageName
import java.io.File

/**
 * Base test that starts a (static) MongoDB container.
 * Before each test, all databases are deleted.
 */
abstract class BaseMongoDbContainerTest {
  @BeforeEach
  fun setup() {
    runBlocking {
      deleteAllDatabases()
    }
  }

  /**
   * Deletes all databases (except the default ones).
   */
  suspend fun deleteAllDatabases() {
    client().use { client ->
      client.startSession().use {
        client.listDatabases().collect {
          val name = it["name"] as? String ?: return@collect
          if (defaultDatabaseNames.contains(name)) {
            return@collect
          }

          client.getDatabase(name).drop()
        }
      }
    }
  }

  /**
   * Executes the given block with a new MongoDB client.
   */
  suspend fun withClient(block: suspend (client: MongoClient) -> Unit) {
    client().use { client ->
      block(client)
    }
  }

  /**
   * Executes the block with a new MongoDB client and session.
   */
  suspend fun withSession(
    block: suspend (client: MongoClient, session: ClientSession) -> Unit,
  ) {
    withClient { client ->
      client.startSession().use { session ->
        block(client, session)
      }
    }
  }

  /**
   * Creates a new MongoDB client. It is possible to provide a custom codec registry.
   */
  fun client(additionalCodecRegistry: CodecRegistry? = null): MongoClient {
    val connectionString = "mongodb://${container.host}:${container.firstMappedPort}/?directConnect=true"

    val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
      buildList {
        add(MongoClientSettings.getDefaultCodecRegistry())
        if (additionalCodecRegistry != null) {
          add(additionalCodecRegistry)
        }
        add(CodecRegistries.fromProviders(KotlinSerializerCodecProvider()))
      }
    )

    val client = MongoClient.create(
      MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(connectionString))
        .codecRegistry(codecRegistry)
        .build(),
    )

    return client
  }

  companion object {
    /**
     * Contains the default database names that are never deleted
     */
    private val defaultDatabaseNames = setOf("admin", "config", "local")

    /**
     * Reference to the MongoDB container.
     * Is automatically started.
     * This container will be cleaned up by test containers automatically.
     */
    val container: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:7"))
      .waitingFor(HostPortWaitStrategy())
      .also {
        it.start()
        assertThat(it.isRunning).isTrue()

        if (guessDebugging()) {
          println("MongoDB container started with exposed ports: ${it.exposedPorts}")
          println("Port bindings: ${it.portBindings}")

          val port = it.firstMappedPort
          val connectionUrl = "mongodb://localhost:$port?directConnection=true"

          println("------------------------------")
          println(" Connect to MongoDB at: $connectionUrl")
          println("------------------------------")

          startMongoDbCompass(connectionUrl)
        }
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

fun startMongoDbCompass(connectionUrl: String) {
  // Start MongoDB Compass
  println("Starting MongoDB Compass...")
  println("Connect to MongoDB at: $connectionUrl")

  val mongoDbCompassBinary = File("/usr/bin/mongodb-compass")
  if (mongoDbCompassBinary.exists().not()) {
    println("MongoDB Compass binary not found at: ${mongoDbCompassBinary.absolutePath}")
    println("Install MongoDB Compass and try again: https://www.mongodb.com/try/download/atlascli")
    return
  }

  val processBuilder = ProcessBuilder(mongoDbCompassBinary.absolutePath, connectionUrl)
  processBuilder.redirectError()
  processBuilder.redirectOutput()
  val process = processBuilder.start()

  println("MongoDB Compass started.")
}
