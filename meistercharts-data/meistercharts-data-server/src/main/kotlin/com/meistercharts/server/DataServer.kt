/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.server

import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.meistercharts.history.storage.FileHistoryStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.opentelemetry.api.OpenTelemetry
import it.neckar.commons.logback.LogbackConfigurer
import it.neckar.ktor.KtorService
import it.neckar.ktor.baseUrl
import it.neckar.ktor.dumpRoutes
import it.neckar.ktor.installIgnoreTailingSlash
import it.neckar.ktor.installJson
import it.neckar.ktor.installNeckarServiceDefaults
import it.neckar.ktor.launch
import it.neckar.ktor.resolveServiceOpenTelemetry
import it.neckar.ktor.status.installStatusPages
import it.neckar.logging.LoggerDelegate
import it.neckar.open.app.clikt.BaseCommand
import it.neckar.open.dispose.Disposable
import it.neckar.open.file.replaceLeadingTilde
import it.neckar.open.http.Url
import it.neckar.open.time.JVMTimerCoroutineImplementation
import it.neckar.open.time.timerImplementation
import it.neckar.projects.common.Port
import it.neckar.projects.common.ServiceDescriptor
import it.neckar.runtime.context.ServiceHost
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File

/**
 * Service descriptor for the charting data server. Shared between [DataServer] and
 * [StartDataServerCommand] so the command can resolve OpenTelemetry against the same identity.
 */
private val DataServerServiceDescriptor: ServiceDescriptor = ServiceDescriptor("Data Server", "data", Port.of(8088))

/**
 * Starts the data server
 */
fun main(args: Array<String>): Unit {
  runBlocking {
    StartDataServerCommand().main(args)
  }
}

class StartDataServerCommand : BaseCommand<ServiceHost.Default>(serviceHostRegistry = ServiceHost.Default) {

  val storageDir: File by option(help = "Base directory path to the storage directory").file().required()

  override suspend fun execute() {
    LogbackConfigurer.configureLoggingConsoleOnly(levelForRoot = Level.INFO, levelForNeckarIt = Level.DEBUG)

    timerImplementation = JVMTimerCoroutineImplementation()

    val storageDirectory = storageDir.replaceLeadingTilde()

    val dataServer = DataServer(
      storageDirectory,
      openTelemetry = resolveServiceOpenTelemetry(DataServerServiceDescriptor),
    )
    dataServer.launch()
  }
}

class DataServer(
  storageDirectory: File,
  /**
   * OpenTelemetry instance for tracing. Handed to [installNeckarServiceDefaults], which wires the
   * server-span + NeckarIT-Trace-Id bundle. Defaults to [OpenTelemetry.noop] so tests and
   * unconfigured deployments record nothing; production resolves a real SDK via
   * [it.neckar.ktor.resolveServiceOpenTelemetry] in [StartDataServerCommand].
   */
  val openTelemetry: OpenTelemetry = OpenTelemetry.noop(),
  portOverride: Port? = null,
) : KtorService, Disposable {
  val baseDir: File = storageDirectory

  val clientUpdatesSupport: ClientUpdatesSupport = ClientUpdatesSupport.create()

  override val serviceDescriptor: ServiceDescriptor = DataServerServiceDescriptor
  override val baseUrl: Url.Absolute = serviceDescriptor.baseUrl(portOverride)

  override fun Application.configure() {
    logger.info("Starting Charting Data Server")
    logger.info(
      """\n
______      _          _____
|  _  \    | |        /  ___|
| | | |__ _| |_ __ _  \ `--.  ___ _ ____   _____ _ __
| | | / _` | __/ _` |  `--. \/ _ \ '__\ \ / / _ \ '__|
| |/ / (_| | || (_| | /\__/ /  __/ |   \ V /  __/ |
|___/ \__,_|\__\__,_| \____/ \___|_|    \_/ \___|_|
  """.trimIndent()
    )

    logger.info("Data Directory: $baseDir")

    installPlugins()


    val json: Json = Json {}

    val historyStorage: FileHistoryStorage = FileHistoryStorage(baseDir, json).also { historyStorage ->
      historyStorage.scheduleDownSampling()
    }

    historyStorage.observe { updateInfo ->
      clientUpdatesSupport.addUpdateInfo(updateInfo)
    }

    routing(MetaRouting.metaRouting)
    historyRouting(historyStorage, clientUpdatesSupport)

    logger.info(dumpRoutes())


  }

  fun Application.installPlugins() {
    // anyHost() is wanted here (public data API) and allows every origin, so no deployment names
    // its hosts; the platform allow/expose header set stays.
    installNeckarServiceDefaults(
      serviceDescriptor,
      openTelemetry = openTelemetry,
    ) {
      anyHost()
    }
    installJson {
    }
    installStatusPages(logger)
    install(WebSockets)

    installIgnoreTailingSlash()
  }

  companion object {
    val logger: Logger by LoggerDelegate()
    val requestLogger: Logger = LoggerFactory.getLogger("Requests")
  }

  override fun dispose() {
    clientUpdatesSupport.dispose()
  }
}



