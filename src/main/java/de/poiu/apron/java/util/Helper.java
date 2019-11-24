/*
 * Copyright (C) 2018 Marco Herrn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.poiu.apron.java.util;


/**
 * Small helper methods that are used in multiple places.
 *
 * @author mherrn
 */
class Helper {

  /**
   * Checks whether we currently run with a Java version of at least 9.
   *
   * @return <code>true</code> if the currently running Java VM is at least version 9,
   *         otherwise <code>false</code>
   */
  static boolean isJava9OrHigher() {
    final String javaVersion = System.getProperty("java.version");

    if (javaVersion.contains(".")) {
      final int majorVersion = Integer.parseInt(javaVersion.split("\\.")[0]);
      return majorVersion >= 9;
    } else {
      // no dot in the version ususally means an early access version like "14-ea"
      return true;
    }
  }
}
