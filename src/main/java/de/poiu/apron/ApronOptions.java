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

import java.nio.charset.Charset;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Holder object to encapsulate optional parameters when writing {@link PropertyFile PropertyFiles}.
 * <p>
 * Be aware that not all combinations of options make sense in all cases. For example a
 * MissingKeyAction is not useful when {@link de.poiu.apron.PropertyFile#overwrite(java.io.File, de.poiu.apron.ApronOptions) overwriting}
 * a file. In these cases those options are ignored.
 * <p>
 * By default this class provides the following values:
 * <ul>
 *  <li><code>UTF-8</code> encoding to read and write .properties files with UTF-8 encoding</li>
 *  <li><code>MissingKeyAction.NOTHING</code> to leave removed key-value-pairs intact when updating .properties files</li>
 *  <li><code>UnicodeHandling.DO_NOTHING</code> to not change the original unicode value (unless writing in a
 *     non-UTF charset in which case Unicode characters are always written as Unicode escape sequences)</li>
 * </ul>
 * <p>
 * This class is immutable and therefore thread safe. All modification methods actually return a new object.
 *
 * @author mherrn
 */
public class ApronOptions {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** The Charset to use for writing a PropertyFile. */
  private final Charset charset;

  /**
   * The MissingKeyAction to apply when the updated target .properties file
   * contains key-value pairs that do not exist in the written PropertyFile.
   */
  private final MissingKeyAction missingKeyAction;


  /**
   * How to handle Unicode values when writing. This only applies when writing with
   * a supported Unicode charset, since in all other cases Unicode values are always written
   * as Unicode escape sequences.
   */
  private final UnicodeHandling unicodeHandling;

  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new ApronOptions object with the default values.
   * <p>
   * This is exactly the as if calling the static {@link #create()} method.
   */
  public ApronOptions() {
    this(UTF_8, MissingKeyAction.NOTHING, UnicodeHandling.DO_NOTHING);
  }


  /**
   * Creates a new ApronOptions object with the given values.
   * <p>
   * While this constructor is public and is absolutely safe to use, in most cases it is
   * more convenient to use the provided fluent interface, e.g.
   *
   * <pre>
   * final Options options= Options.create()
   *                               .with(StandardCharsets.ISO_8859_1)
   *                               .with(MissingKeyAction.DELETE);
   * </pre>
   *
   * @param charset the Charset to use for writing a PropertyFile
   * @param missingKeyAction the MissingKeyAction to apply when the updated target .properties file
   *                          contains key-value pairs that do not exist in the written PropertyFile
   * @param unicodeHandling how to handle Unicode values when writing.
   */
  public ApronOptions(final Charset charset, final MissingKeyAction missingKeyAction, final UnicodeHandling unicodeHandling) {
    Objects.requireNonNull(charset);
    Objects.requireNonNull(missingKeyAction);
    Objects.requireNonNull(unicodeHandling);
    this.charset= charset;
    this.missingKeyAction= missingKeyAction;
    this.unicodeHandling= unicodeHandling;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Creates a new Options object with the default values.
   *
   * @return the newly created Options object
   */
  public static ApronOptions create() {
    return new ApronOptions();
  }


  /**
   * Returns a copy of this Options object, but with the given charset.
   *
   * @param charset the Charset to use when writing the PropertyFile.
   * @return this Options object
   */
  public ApronOptions with(final Charset charset) {
    return new ApronOptions(charset, this.missingKeyAction, this.unicodeHandling);
  }


  /**
   * Returns a copy of this Options object, but with the given MissingKeyAction.
   * <p>
   * This is only meaningful on updating a File. When writing to an output stream or overwriting a
   * file or creating a new file, this options does nothing.
   *
   * @param missingKeyAction how to handle key-value-pairs that exist in in the written PropertyFile, but not in the updated one
   * @return this Options object
   */
  public ApronOptions with(final MissingKeyAction missingKeyAction) {
    return new ApronOptions(this.charset, missingKeyAction, this.unicodeHandling);
  }


  /**
   * Returns a copy of this Options object, but with the given UnicodeHandling
   *
   * @param unicodeHandling how to handle Unicode values when writing a PropertyFile.
   * @return this Options object
   */
  public ApronOptions with(final UnicodeHandling unicodeHandling) {
    return new ApronOptions(this.charset, this.missingKeyAction, unicodeHandling);
  }


  /**
   * Returns the Charset with which to write a PropertyFile.
   *
   * @return the Charset with which to write a PropertyFile
   */
  public Charset getCharset() {
    return charset;
  }


  /**
   * Returns the MissingKeyAction to use when updating a .properties file.
   * <p>
   * This is only meaningful on updating a File. When writing to an output stream or overwriting a
   * file or creating a new file, this options does nothing.
   *
   * @return the MissingKeyAction to use when updating a .properties file
   */
  public MissingKeyAction getMissingKeyAction() {
    return missingKeyAction;
  }


  /**
   * Returns the UnicodeHandling to use when writing a .properties file.
   *
   * @return the UnicodeHandling to use when writing a .properties file
   */
  public UnicodeHandling getUnicodeHandling() {
    return unicodeHandling;
  }


  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ApronOptions) {
      final ApronOptions that = (ApronOptions) o;
      return this.charset.equals(that.getCharset())
           && this.missingKeyAction.equals(that.getMissingKeyAction())
           && this.unicodeHandling.equals(that.getUnicodeHandling());
    }
    return false;
  }


  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= charset.hashCode();
    h$ *= 1000003;
    h$ ^= missingKeyAction.hashCode();
    h$ *= 1000003;
    h$ ^= unicodeHandling.hashCode();
    return h$;
  }


  @Override
  public String toString() {
    return "Options{" + "charset=" + charset + ", missingKeyAction=" + missingKeyAction + ", unicodeHandling=" + unicodeHandling + '}';
  }

}
