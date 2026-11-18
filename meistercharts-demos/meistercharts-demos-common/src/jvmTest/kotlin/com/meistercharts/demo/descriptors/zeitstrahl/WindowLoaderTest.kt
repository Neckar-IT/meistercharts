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

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test

class WindowLoaderTest {
  private val answers: MutableMap<Int, CompletableDeferred<List<String>>> = mutableMapOf()

  private fun answer(zoomLevel: Int): CompletableDeferred<List<String>> = answers.getOrPut(zoomLevel) { CompletableDeferred() }

  @Test
  suspend fun `an answer for a zoom level the user left keeps the newer items`(): Unit = coroutineScope {
    val loader = WindowLoader<Int, String>(this, { request -> answer(request.key).await() }, {})
    loader.ensureLoaded(30, 0.0, 10.0)
    answer(30).complete(listOf("z30"))
    yield()

    loader.ensureLoaded(29, 0.0, 10.0)
    yield()
    loader.ensureLoaded(30, 0.0, 10.0)
    answer(29).complete(listOf("z29"))
    yield()

    assertThat(loader.items).containsExactly("z30")
    assertThat(loader.loadedKey).isEqualTo(30)
  }

  @Test
  suspend fun `a refused connection, which the browser reports as Error, shows as failure`(): Unit = coroutineScope {
    val loader = WindowLoader<Int, String>(this, { throw Error("Fail to fetch") }, {})

    loader.ensureLoaded(20, 0.0, 10.0)
    yield()

    assertThat(loader.failure).isEqualTo("Fail to fetch")
  }

  @Test
  suspend fun `another zoom level in the loaded window sends a request`(): Unit = coroutineScope {
    val requests = mutableListOf<WindowRequest<Int>>()
    val loader = WindowLoader<Int, String>(this, { request -> requests.add(request); emptyList() }, {})

    loader.ensureLoaded(20, 0.0, 10.0)
    yield()
    loader.ensureLoaded(21, 0.0, 10.0)
    yield()

    assertThat(requests).containsExactly(WindowRequest(20, -5.0, 15.0), WindowRequest(21, -5.0, 15.0))
  }

  @Test
  suspend fun `a window inside the loaded one plus its margins sends no request`(): Unit = coroutineScope {
    val requests = mutableListOf<WindowRequest<Int>>()
    val loader = WindowLoader<Int, String>(this, { request -> requests.add(request); emptyList() }, {})

    loader.ensureLoaded(20, 0.0, 10.0)
    yield()
    loader.ensureLoaded(20, -4.0, 14.0)
    yield()

    assertThat(requests).containsExactly(WindowRequest(20, -5.0, 15.0))
  }
}
