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
package de.poiu.apron.entry;


/**
 * An Entry in a PropertyFile.
 * An entry corresponds to a logical line in a .properties file.
 * <p>
 * These usually are empty lines, comments and key-value-pairs.
 *
 * @author mherrn
 */
public interface Entry {
  /**
   * Returns the actual content of this entry.
   * <p>
   * If the Entry needs escaping the returned CharSequence already contains this escaping.
   * This allows the returned CharSequence to written directly to a .properties file
   * where certain characters (like whitespace or newlines) need to be escaped.
   * <p>
   * The returned CharSequence also contains the trailing line ending character.
   *
   * @return the escaped content of this entry
   */
  public CharSequence toCharSequence();
}
