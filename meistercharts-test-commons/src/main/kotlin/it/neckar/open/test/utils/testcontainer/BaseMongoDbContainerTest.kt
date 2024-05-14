package it.neckar.open.test.utils.testcontainer

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.ClientSession
import com.mongodb.kotlin.client.coroutine.MongoClient
import it.neckar.open.kotlin.lang.guessInCIEnvironment
import kotlinx.coroutines.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.utility.DockerImageName

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

  suspend fun withSession(
    block: suspend (client: MongoClient, session: ClientSession) -> Unit,
  ) {
    withClient { client ->
      client.startSession().use { session ->
        block(client, session)
      }
    }
  }

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
    private val defaultDatabaseNames = setOf("admin", "config", "local")

    val container: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:7"))
      .also {
        if (guessInCIEnvironment().not()) {
          it.withExposedPorts(27017) // Expose port 27017
          it.portBindings = listOf("34000:27017") // Bind local port 32794 to exposed port 27017
        }
      }
      .waitingFor(HostPortWaitStrategy())
      .also {
        it.start()
      }
  }
}
