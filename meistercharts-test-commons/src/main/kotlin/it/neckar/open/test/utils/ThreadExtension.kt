/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
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
      if (!remainingThreads.isEmpty()) {
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
        for (i in 0..9) {
          try {
            Thread.sleep(10)
          } catch (ignore: InterruptedException) {
          }

          //Second try
          if (!remainingThread.isAlive) {
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
        isAwtRelatedThread(threadGroupName, threadName) ||
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
private fun isAwtRelatedThread(threadGroupName: String, threadName: String) = threadName.startsWith("AWT-")
private fun isQuantumRenderer(threadGroupName: String, threadName: String) = threadGroupName == "main" && threadName.startsWith("QuantumRenderer")
private fun isKeepAliveTimer2(threadGroupName: String, threadName: String) = threadGroupName == "InnocuousThreadGroup" && threadName.startsWith("Keep-Alive-Timer")
