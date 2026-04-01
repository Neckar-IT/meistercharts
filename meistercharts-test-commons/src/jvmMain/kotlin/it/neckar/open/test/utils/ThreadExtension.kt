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
package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Extension that checks whether some threads have been left after the unit test has finished.
 */
class ThreadExtension @JvmOverloads constructor(
  val ignoredThreadMatcher: ThreadMatcher? = DefaultThreadMatcher(),
) : BeforeEachCallback, AfterEachCallback {

  override fun beforeEach(context: ExtensionContext) {
    check(initialThreads == null) { "initialThreads is not null" }
    initialThreads = Thread.getAllStackTraces().keys
  }

  override fun afterEach(context: ExtensionContext) {
    if (context.executionException.isPresent) {
      afterFailing()
      return
    }
    after()
  }

  private var initialThreads: Collection<Thread>? = null

  fun getInitialThreads(): Collection<Thread> {
    return checkNotNull(initialThreads) { "not initialized yet" }
  }

  private fun afterFailing() {
    val remainingThreads = remainingThreads
    if (!remainingThreads.isEmpty()) {
      System.err.print(
        """
  Some threads have been left:
  ${buildMessage(remainingThreads)}
  """.trimIndent()
      )
    }
    initialThreads = null
  }

  private fun after() {
    try {
      val remainingThreads = remainingThreads
      if (remainingThreads.isNotEmpty()) {
        System.err.println(
          """
  --> Some threads have been left:
  ${buildMessage(remainingThreads)}
  """.trimIndent()
        )
        throw IllegalStateException(
          """
  Some threads have been left:
  ${buildMessage(remainingThreads)}
  """.trimIndent()
        )
      }
    } finally {
      initialThreads = null
    }
  }//Second try
  //Ignore the threads

  //Wait for a little bit, sometimes the threads die off
  val remainingThreads: Set<Thread>
    get() {
      checkNotNull(initialThreads) { "initialThreads is null" }
      val threadsNow: Collection<Thread> = Thread.getAllStackTraces().keys
      val remainingThreads: MutableSet<Thread> = HashSet(threadsNow)
      remainingThreads.removeAll(initialThreads!!)
      val iterator = remainingThreads.iterator()
      while (iterator.hasNext()) {
        val remainingThread = iterator.next()
        if (!remainingThread.isAlive) {
          iterator.remove()
          continue
        }

        //Ignore the threads
        if (ignoredThreadMatcher != null && ignoredThreadMatcher.shallIgnore(remainingThread)) {
          iterator.remove()
          continue
        }

        //Wait for a little bit, sometimes the threads die off
        for (ignored in 0..9) {
          try {
            Thread.sleep(10)
          } catch (_: InterruptedException) {
            return remainingThreads
          }

          //Second try
          if (remainingThread.isAlive.not()) {
            iterator.remove()
            break
          }
        }
      }
      return remainingThreads
    }

  private fun buildMessage(remainingThreads: Set<Thread>): String {
    val builder = StringBuilder()
    builder.append("// Remaining Threads:").append("\n")
    builder.append("-----------------------").append("\n")
    for (remainingThread in remainingThreads) {
      builder.append("---")
      builder.append("\n")
      builder.append(remainingThread)
      builder.append(STACK_TRACE_ELEMENT_SEPARATOR)
      builder.append(remainingThread.stackTrace.joinToString(STACK_TRACE_ELEMENT_SEPARATOR))
      builder.append("\n")
    }
    builder.append("-----------------------").append("\n")
    return builder.toString()
  }

  interface ThreadMatcher {
    fun shallIgnore(remainingThread: Thread): Boolean
  }

  /**
   * Default implementation that ignore several known threads.
   */
  class DefaultThreadMatcher : ThreadMatcher {
    override fun shallIgnore(remainingThread: Thread): Boolean {
      val threadGroup = remainingThread.threadGroup
        ?: //this means the thread has died
        return true

      val threadGroupName = threadGroup.name
      val threadName = remainingThread.name

      if (isKeepAliveTimer(threadGroupName, threadName) ||
        isProcessReaper(threadGroupName, threadName) ||
        isKeepAliveSocketCleaner(threadGroupName, threadName) ||
        isJava2dDisposer(threadGroupName, threadName) ||
        isKeepAliveTimer2(threadGroupName, threadName) ||
        isAwtRelatedThread(threadName) ||
        isQuantumRenderer(threadGroupName, threadName)
      ) {
        return true
      }

      //Special check for awaitility - this lib leaves one thread open for about 100ms
      for (stackTraceElement in remainingThread.stackTrace) {
        if (stackTraceElement.className == "org.awaitility.core.ConditionAwaiter$1") {
          if (stackTraceElement.methodName == "run") {
            return true
          }
        }
      }
      return false
    }
  }

  companion object {
    const val STACK_TRACE_ELEMENT_SEPARATOR: String = "\n\tat "
  }
}

private fun isKeepAliveTimer(threadGroupName: String, threadName: String) = threadGroupName == "system" && threadName == "Keep-Alive-Timer"
private fun isProcessReaper(threadGroupName: String, threadName: String) = threadGroupName == "system" && threadName == "process reaper"
private fun isKeepAliveSocketCleaner(threadGroupName: String, threadName: String) = threadGroupName == "system" && threadName == "Keep-Alive-SocketCleaner"
private fun isJava2dDisposer(threadGroupName: String, threadName: String) = threadGroupName == "system" && threadName == "Java2D Disposer"
private fun isAwtRelatedThread(threadName: String) = threadName.startsWith("AWT-")
private fun isQuantumRenderer(threadGroupName: String, threadName: String) = threadGroupName == "main" && threadName.startsWith("QuantumRenderer")
private fun isKeepAliveTimer2(threadGroupName: String, threadName: String) = threadGroupName == "InnocuousThreadGroup" && threadName.startsWith("Keep-Alive-Timer")
