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

import java.util.Objects;


/**
 * An entry of a PropertyFile containing a key-value-pair.
 * <p>
 * Additionally to the actual key and value this class stores
 * <ul>
 *  <li>the leading whitespace</li>
 *  <li>the separator (with optional surrounding whitespace)</li>
 *  <li>and the line ending character</li>
 * </ul>
 * <p>
 * of the entry. This allows writing this entry back to file in exactly the same form as it was
 * written, retaining all the formattings.
 *
 * @author mherrn
 */
public class PropertyEntry implements Entry {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** The leading whitespace before the key */
  private CharSequence leadingWhitespace= "";
  /** The actual key */
  private final CharSequence key;
  /** The separator with surrounding whitespace */
  private CharSequence separator= " = ";
  /** The actual value */
  private CharSequence value;
  /** The line ending */
  private CharSequence lineEnding= "\n";


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new PropertyEntry with the given <i>escaped</i> key and value.
   * <p>
   * No leading whitespace is added.
   * <p>
   * The separator will be '=' surrounded by a single space on both sides.
   * <p>
   * The line ending will be a '\n'.
   *
   * @param key the escaped key
   * @param value the esacepd value
   */
  public PropertyEntry(final CharSequence key, final CharSequence value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);

    this.key = key;
    this.value = value;
  }


  /**
   * Creates a new PropertyEntry with the given <i>escaped</i> key and value.
   * Additionally the leading whitespace, separator (whith optional surrounding whitespace) and the
   * line ending character(s) need to be given.
   *
   * @param leadingWhitespace the leading whitespace before the key
   * @param key the escaped key
   * @param separator the separator with optional surrounding whitespace
   * @param value the esacepd value
   * @param lineEnding the line ending
   */
  public PropertyEntry(final CharSequence leadingWhitespace, final CharSequence key, final CharSequence separator, final CharSequence value, final CharSequence lineEnding) {
    Objects.requireNonNull(leadingWhitespace);
    Objects.requireNonNull(key);
    Objects.requireNonNull(separator);
    Objects.requireNonNull(value);
    Objects.requireNonNull(lineEnding);

    this.leadingWhitespace= leadingWhitespace;
    this.key= key;
    this.separator= separator;
    this.value= value;
    this.lineEnding= lineEnding;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public CharSequence toCharSequence() {
    return new StringBuilder()
      .append(leadingWhitespace)
      .append(key)
      .append(separator)
      .append(value)
      .append(lineEnding);
  }


  /**
   * Returns the <i>escaped</i> leading whitespace of this PropertyEntry.
   *
   * @return the <i>escaped</i> leading whitespace of this PropertyEntry
   * @since 2.0.0
   */
  public CharSequence getLeadingWhitespace() {
    return leadingWhitespace;
  }


  /**
   * Returns the <i>escaped</i> key of this PropertyEntry.
   *
   * @return the <i>escaped</i> key of this PropertyEntry
   */
  public CharSequence getKey() {
    return this.key;
  }


  /**
   * Returns the <i>escaped</i> separator with optional surrounding whitespace of this PropertyEntry.
   *
   * @return the <i>escaped</i> separator with optional surrounding whitespace of this PropertyEntry
   * @since 2.0.0
   */
  public CharSequence getSeparator() {
    return separator;
  }


  /**
   * Returns the <i>escaped</i> value of this PropertyEntry.
   *
   * @return the <i>escaped</i> value of this PropertyEntry
   */
  public CharSequence getValue() {
    return this.value;
  }


  /**
   * Sets the new <i>escaped</i> value for this PropertyEntry.
   *
   * @param value the new <i>escaped</i> value for this PropertyEntry
   */
  public void setValue(final CharSequence value) {
    this.value= value;
  }


  /**
   * Returns the <i>escaped</i> line ending of this PropertyEntry.
   *
   * @return the <i>escaped</i> line ending of this PropertyEntry
   * @since 2.0.0
   */
  public CharSequence getLineEnding() {
    return lineEnding;
  }


  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(this.leadingWhitespace);
    hash = 97 * hash + Objects.hashCode(this.key);
    hash = 97 * hash + Objects.hashCode(this.separator);
    hash = 97 * hash + Objects.hashCode(this.value);
    hash = 97 * hash + Objects.hashCode(this.lineEnding);
    return hash;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PropertyEntry other = (PropertyEntry) obj;
    if (!Objects.equals(this.leadingWhitespace, other.leadingWhitespace)) {
      return false;
    }
    if (!Objects.equals(this.key, other.key)) {
      return false;
    }
    if (!Objects.equals(this.separator, other.separator)) {
      return false;
    }
    if (!Objects.equals(this.value, other.value)) {
      return false;
    }
    if (!Objects.equals(this.lineEnding, other.lineEnding)) {
      return false;
    }
    return true;
  }


  @Override
  public String toString() {
    return "PropertyEntry{" + "leadingWhitespace=" + leadingWhitespace + ", key=" + key + ", separator=" + separator + ", value=" + value + ", lineEnding=" + lineEnding + '}';
  }
}
