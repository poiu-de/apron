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
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Objects;


/**
 * Holder object to encapsulate optional parameters when writing {@link PropertyFile PropertyFiles}.
 * <p>
 * Be aware that not all combinations of options make sense in all cases. For example a
 * MissingKeyAction is not useful when {@link PropertyFile#overwrite(java.io.File, de.poiu.apron.Options) overwriting}
 * a file. In these cases those options are ignored.
 * <p>
 * By default this class provides the following values:
 * <ul>
 *  <li>UTF-8 encoding to read and write .properties files with UTF-8 encoding</li>
 *  <li>MissingKeyAction.NOTHING to leave removed key-value-pairs intact when updating .properties files</li>
 * </ul>
 *
 * This class is immutable and therefore thread safe. All modification methods actually return a new object.
 *
 * @author mherrn
 */
public class Options {

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


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new Options object with the default values.
   * <p>
   * This is exactly the as if calling the static {@link #create() } method.
   */
  public Options() {
    this(UTF_8, MissingKeyAction.NOTHING);
  }


  /**
   * Creates a new Options object with the given values.
   * <p>
   * While this constructor is public and is absolutely safe to use, in most cases it is
   * more convenient to use the provided fluent interface, e.g.
   *
   * <pre>
   * final Options options= Options.create()
   *                               .with(StandardCharsets.ISO_8859_1)
   *                               .with(MissingKeyAction.DELETE);
   * </pre>
   * @param charset the Charset to use for writing a PropertyFile
   * @param missingKeyAction the MissingKeyAction to apply when the updated target .properties file
   *                          contains key-value pairs that do not exist in the written PropertyFile
   */
  public Options(final Charset charset, final MissingKeyAction missingKeyAction) {
    Objects.requireNonNull(charset);
    Objects.requireNonNull(missingKeyAction);
    this.charset= charset;
    this.missingKeyAction= missingKeyAction;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Creates a new Options object with the default values.
   * @return the newly created Options object
   */
  public static Options create() {
    return new Options();
  }


  /**
   * Creates a new Options object as a copy of the given one.
   *
   * @param options the Options to copy
   * @return a new Options object with the same values as the given one
   */
  //FIXME: Do we need this? There is no difference between these objects then and they are immutable
  public static Options of(final Options options) {
    return new Options()
      .with(options.charset)
      .with(options.missingKeyAction);
  }


  /**
   * Returns a copy of this Options object, but with the given charset.
   *
   * @param charset the Charset to use when writing the PropertyFile.
   * @return this Options object
   */
  public Options with(final Charset charset) {
    return new Options(charset, this.missingKeyAction);
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
  public Options with(final MissingKeyAction missingKeyAction) {
    return new Options(this.charset, missingKeyAction);
  }


  /**
   * Returns the Charset with which to write a PropertyFile.
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
   * @return the MissingKeyAction to use when updating a .properties file
   */
  public MissingKeyAction getMissingKeyAction() {
    return missingKeyAction;
  }


  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Options) {
      final Options that = (Options) o;
      return (this.charset.equals(that.getCharset()))
           && (this.missingKeyAction.equals(that.getMissingKeyAction()));
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
    return h$;
  }


  @Override
  public String toString() {
    return "Options{" + "charset=" + charset + ", missingKeyAction=" + missingKeyAction + '}';
  }

}
