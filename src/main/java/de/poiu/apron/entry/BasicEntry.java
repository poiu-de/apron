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
 * A Basic entry of a PropertyFile with no special meaning.
 * Ususally these are lines containg only whitespace, empty lines and comment lines.
 * <p>
 * No un-/escaping will be done for such entries. They are handled as simple strings.
 *
 * @author mherrn
 */
public class BasicEntry implements Entry {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** The actual content of this BasicEntry */
  private final CharSequence content;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new BasicEntry with the given content.
   *
   * @param content the actual content of the new BasicEntry
   */
  public BasicEntry(final CharSequence content) {
    this.content= content;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public CharSequence toCharSequence() {
    return content;
  }


  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + Objects.hashCode(this.content);
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
    final BasicEntry other = (BasicEntry) obj;
    if (!Objects.equals(this.content, other.content)) {
      return false;
    }
    return true;
  }


  @Override
  public String toString() {
    return "BasicEntry{" + "content=" + content + '}';
  }
}
