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

package it.neckar.open.version

/**
 * Common exception for all kinds of version related problems
 *
 */
open class VersionException : RuntimeException {
  /**
   *
   * Constructor for VersionException.
   */
  constructor()

  /**
   *
   * Constructor for VersionException.
   *
   * @param message a String object.
   */
  constructor(message: String) : super(message) {}

  /**
   *
   * Constructor for VersionException.
   *
   * @param message a String object.
   * @param cause   a Throwable object.
   */
  constructor(message: String, cause: Throwable) : super(message, cause) {}

  /**
   *
   * Constructor for VersionException.
   *
   * @param cause a Throwable object.
   */
  constructor(cause: Throwable) : super(cause) {}

  /**
   * Creates a new version exception
   *
   * @param message       the message (is always used)
   * @param messageSuffix the suffix (only used when appendSuffix==true)
   * @param appendSuffix  whether to append the suffix
   */
  constructor(message: String, messageSuffix: String, appendSuffix: Boolean) : super(message + if (appendSuffix) messageSuffix else "") {}
}
