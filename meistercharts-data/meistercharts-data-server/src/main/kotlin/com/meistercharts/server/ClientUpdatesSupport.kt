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

import kotlin.uuid.Uuid
import com.meistercharts.time.TimeRange
import com.meistercharts.data.rest.ClientId
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryUpdateInfo
import it.neckar.open.dispose.Disposable
import it.neckar.open.unit.other.GuardedBy
import kotlinx.coroutines.*
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import it.neckar.open.concurrent.repeating.Repeating

/**
 * Call [scheduleCleanups] after initialization
 */
class ClientUpdatesSupport internal constructor(
  val clientCleanup: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
): Disposable {
  /**
   * Guards [clientUpdates] and [clientLastAccessTime] (and the inner
   * [MutableList]s) against concurrent access from three sources: the HTTP
   * producer threads ([addUpdates], [addUpdateInfo]), the HTTP consumer threads
   * ([getUpdates], [registerClient], [deleteClientFromUpdateList]), and the
   * cleanup coroutine ([checkClients], every 10s on [Dispatchers.Default]).
   *
   * A plain [ReentrantLock] rather than a coroutines `Mutex` because the
   * methods cannot all be `suspend`: [addUpdateInfo] is driven by the
   * non-suspend `HistoryObserver` callback (`(HistoryUpdateInfo) -> Unit`,
   * invoked synchronously from `FileHistoryStorage.storeWithoutCache`), so a
   * `Mutex` would force `runBlocking` onto server threads. The lock must be
   * reentrant because [checkClients] calls [deleteClientFromUpdateList] while
   * holding it. The critical sections are pure in-memory map operations, so
   * the brief blocking on the cleanup dispatcher thread is negligible.
   */
  private val lock = ReentrantLock()

  @GuardedBy("lock")
  private val clientUpdates: MutableMap<Uuid, MutableList<HistoryUpdateInfo>> = mutableMapOf()

  @GuardedBy("lock")
  private val clientLastAccessTime: MutableMap<Uuid, Instant> = mutableMapOf()

  fun scheduleCleanups() {
    Repeating.every(CleanupInterval) { checkClients() }.start(clientCleanup)
  }

  /** adds updates to the client update map of the Client Updates Support*/
  fun addUpdates(newHistoryBucketList: MutableList<HistoryBucket>) {
    lock.withLock {
      newHistoryBucketList.forEach { newHistoryBucket ->
        val timeRange = TimeRange(newHistoryBucket.descriptor.start, newHistoryBucket.descriptor.end)
        clientUpdates.forEach { clientEntry ->
          var added = false
          clientEntry.value.forEachIndexed { index, historyUpdateInfo ->
            if (historyUpdateInfo.samplingPeriod == newHistoryBucket.samplingPeriod) {
              clientUpdates[clientEntry.key]?.set(index, historyUpdateInfo.merge(timeRange))
              added = true
            }
          }
          if (added.not()){
            clientEntry.value.add(HistoryUpdateInfo(newHistoryBucket.samplingPeriod, timeRange))
          }
        }
      }
    }
  }

  /** adds update info to the client update map of the Client Updates Support*/
  fun addUpdateInfo(historyUpdateInfo: HistoryUpdateInfo) {
    lock.withLock {
      clientUpdates.forEach{
        it.value.add(historyUpdateInfo)
      }
    }
  }

  /** registers the client id in clientUpdates map of ClientUpdatesSupport*/
  fun registerClient(clientId: ClientId) { // todo should be done in get
    lock.withLock {
      this.clientUpdates[clientId.uuid] = mutableListOf()
    }
  }

  /** deletes the client id from the clientUpdates map of ClientUpdatesSupport*/
  fun deleteClientFromUpdateList(id: Uuid) {
    lock.withLock {
      this.clientUpdates.remove(id)
    }
  }

  /** gets the updates for the given client id and removes the current updates for the client id from the clientUpdates map*/
  fun getUpdates(clientId: ClientId): MutableList<HistoryUpdateInfo> {
    lock.withLock {
      this.clientLastAccessTime[clientId.uuid] = Instant.now()
      return this.clientUpdates.put(clientId.uuid, mutableListOf()) ?: mutableListOf()
    }
  }

  /** checks the clientUpdate map and deletes clients that were inactive for too long*/
  fun checkClients() {
    lock.withLock {
      val now = Instant.now()
      val iterator = this.clientLastAccessTime.iterator()
      while (iterator.hasNext()) {
        val next = iterator.next()
        val limit = next.value.plus(ClientInactivityTimeout.toJavaDuration())
        if (now >= limit) {
          iterator.remove()
          deleteClientFromUpdateList(next.key)
        }
      }
    }
  }

  override fun dispose() {
    clientCleanup.cancel("Disposed!")
  }

  companion object {
    /**
     * How often stale clients are swept out of the update maps.
     */
    private val CleanupInterval: Duration = 10.seconds

    /**
     * Clients that haven't fetched updates within this duration are dropped from
     * [clientLastAccessTime] and [clientUpdates] by the cleanup coroutine.
     */
    private val ClientInactivityTimeout: Duration = 3.minutes

    fun create(): ClientUpdatesSupport {
      return ClientUpdatesSupport().also {
        it.scheduleCleanups()
      }
    }
  }
}
