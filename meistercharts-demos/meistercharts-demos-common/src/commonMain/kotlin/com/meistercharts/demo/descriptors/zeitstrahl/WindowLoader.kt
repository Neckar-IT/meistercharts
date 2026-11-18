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
package com.meistercharts.demo.descriptors.zeitstrahl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Loads the items of a time window once and keeps them until the window leaves the loaded range or [WindowRequest.key] changes.
 * The previous items stay visible while the next request runs.
 */
class WindowLoader<K, T>(
  private val coroutineScope: CoroutineScope,
  private val load: suspend (request: WindowRequest<K>) -> List<T>,
  private val onChange: () -> Unit,
) {
  var items: List<T> = emptyList()
    private set

  /** The message of the last failed request, `null` after a successful one or while the loaded items cover the window. */
  var failure: String? = null
    private set

  /** The key of [items]; while a request for another key runs, [items] still belong to the previous one. */
  val loadedKey: K?
    get() = loadedRequest?.key

  private var loadedRequest: WindowRequest<K>? = null
  private var pendingRequest: WindowRequest<K>? = null
  private var pendingJob: Job? = null

  /** Requests [from] to [to] plus half a window on each side, unless the loaded or pending request covers it. */
  fun ensureLoaded(key: K, from: Double, to: Double) {
    if (loadedRequest?.covers(key, from, to) == true) {
      // A request still running for an earlier window must not replace these items, and its failure no longer matters.
      cancelPending()
      failure = null
      return
    }
    if (pendingRequest?.covers(key, from, to) == true) {
      return
    }
    val margin = (to - from) / 2
    val request = WindowRequest(key, from - margin, to + margin)
    cancelPending()
    pendingRequest = request
    pendingJob = coroutineScope.launch {
      try {
        // A replaced request is cancelled, so an answer here belongs to the pending request.
        items = load(request)
        loadedRequest = request
        pendingRequest = null
        failure = null
      } catch (exception: CancellationException) {
        throw exception
      } catch (throwable: Throwable) {
        // In the browser a refused connection arrives as `kotlin.Error("Fail to fetch")`, no Exception.
        failure = throwable.message ?: throwable.toString()
      }
      onChange()
    }
  }

  private fun cancelPending() {
    pendingJob?.cancel()
    pendingJob = null
    pendingRequest = null
  }
}
