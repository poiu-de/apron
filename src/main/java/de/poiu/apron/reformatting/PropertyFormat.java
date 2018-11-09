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
package de.poiu.apron.reformatting;


/**
 * Specifies the target format of a key-value pair for reformatting.
 * <p>
 * This format specifies the leading whitespace, the separator (with optional surrounding
 * whitespace) and the line ending character(s).
 *
 * @author mherrn
 */
class PropertyFormat {

  /** The leading whitespace in front of the key. */
  public final CharSequence leadingWhitespace;

  /** The separator between key and value (with optional surrounding whitespace). */
  public final CharSequence separator;

  /** The line ending character(s). */
  public final CharSequence lineEnding;


  /**
   * Creates a new PropertyFormat with the given values.
   *
   * @param leadingWhitespace the leading whitespace in front of the key
   * @param separator the separatorbetween key and value (with optional surrounding whitespace)
   * @param lineEnding the line ending character(s)
   */
  public PropertyFormat(final CharSequence leadingWhitespace, final CharSequence separator, final CharSequence lineEnding) {
    this.leadingWhitespace = leadingWhitespace;
    this.separator = separator;
    this.lineEnding = lineEnding;
  }
}
