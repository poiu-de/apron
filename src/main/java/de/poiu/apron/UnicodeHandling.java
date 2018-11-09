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
package de.poiu.apron;

/**
 * Specifies how Unicode characters are written when writing a {@link de.poiu.apron.PropertyFile} to
 * a file or OutputStream.
 * <p>
 * This is actually only relevant when writing in a supported UTF charset, since in other cases
 * Unicode characters are always written as Unicode escape sequences.
 *
 * @author mherrn
 */
public enum UnicodeHandling {
  /**
   * Leave existing strings as they are; write new strings according to the given charset (the default).
   * This does only work with UTF encodings.
   */
  DO_NOTHING,
  /**
   * Escape all Unicode characters with \\uxxxx escape sequences.
   */
  ESCAPE,
  /**
   * Write all Unicode characters as their real unicode value.
   * This does only work with UTF encodings.
   */
  UNICODE,
  /**
   * Writes Unicode characters as their real unicode value when using UTF encoding, otherwise escape them.
   *  This does only work with UTF encodings.
   */
  BY_CHARSET,
  ;
}
