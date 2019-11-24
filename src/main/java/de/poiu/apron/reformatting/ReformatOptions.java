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

import de.poiu.apron.UnicodeHandling;
import java.nio.charset.Charset;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Holder object to encapsulate optional parameters when reformatting {@link de.poiu.apron.PropertyFile PropertyFiles}
 * via {@link de.poiu.apron.reformatting.Reformatter}.
 * <p>
 * Be aware that not all combinations of options make sense in all cases. For example a
 * {@link de.poiu.apron.reformatting.AttachCommentsTo} is not useful when {@link de.poiu.apron.reformatting.Reformatter#reformat(java.io.File) reformatting key-value pairs}
 * in a PropertyFile. In these cases those options are ignored.
 * <p>
 * By default this class provides the following values:
 * <ul>
 *  <li><code>UTF-8</code> encoding to read and write .properties files with UTF-8 encoding</li>
 *  <li><code>UnicodeHandling.DO_NOTHING</code> to not change the original unicode value (unless writing in a
 *     non-UTF charset in which case Unicode characters are always written as Unicode escape sequences)</li>
 *  <li>the format string <code>&lt;key&gt; = &lt;value&gt;\\n</code> to use for formatting key-value pairs</li>
 *  <li><code>false</code> for <code>reformatKeyAndValue</code> to not touch the actual key and value on reformatting</li>
 *  <li><code>AttachKeyAndValue.NEXT_PROPERTY</code> to attach comments and empty lines the key-value pair after them
 *     on reordering</li>
 * </ul>
 * <p>
 * This class is immutable and therefore thread safe. All modification methods actually return a new object.
 *
 * @author mherrn
 * @since 2.0.0
 */
public class ReformatOptions {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  // Common Options

  /** The Charset to use for reading and writing a PropertyFile. */
  private final Charset charset;

  /**
   * How to handle Unicode values when writing. This only applies when writing with
   * a supported Unicode charset, since in all other cases Unicode values are always written
   * as Unicode escape sequences.
   */
  private final UnicodeHandling unicodeHandling;

  // Reformat options

  /** The format to use when reformatting key-value pairs. */
  private final String format;

  /** Whether to reformat the keys and values themselves when reformatting key-value pairs. */
  private final boolean reformatKeyAndValue;

  // Reorder options

  /** How to handle comments and empty lines when reordering key-value pairs. */
  private final AttachCommentsTo attachCommentsTo;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new Options object with the default values.
   * <p>
   * This is exactly the as if calling the static {@link #create()} method.
   */
  public ReformatOptions() {
    this(UTF_8, UnicodeHandling.DO_NOTHING, "<key> = <value>\\n", false, AttachCommentsTo.NEXT_PROPERTY);
  }


  /**
   * Creates a new ReformatOptions object with the given values.
   * <p>
   * While this constructor is public and is absolutely safe to use, in most cases it is
   * more convenient to use the provided fluent interface, e.g.
   *
   * <pre>
   * final ReformatOptions reformatOptions= ReformatOptions.create()
   *                               .with(StandardCharsets.ISO_8859_1)
   *                               .withFormat("&lt;key&gt; :\\n\t&lt;value&gt;\\n");
   * </pre>
   *
   * @param charset the Charset to use for reading and writing a PropertyFile
   * @param unicodeHandling how to handle Unicode values when writing.
   * @param format the format to use on reformatting key-value pairs
   * @param reformatKeyAndValue whether to reformat the key and value on reformatting
   * @param attachCommentsTo how to handle comments and empty lines on reordering
   */
  public ReformatOptions(final Charset charset,
                         final UnicodeHandling unicodeHandling,
                         final String format,
                         final boolean reformatKeyAndValue,
                         final AttachCommentsTo attachCommentsTo) {
    Objects.requireNonNull(charset);
    Objects.requireNonNull(unicodeHandling);
    Objects.requireNonNull(format);
    Objects.requireNonNull(attachCommentsTo);
    this.charset= charset;
    this.unicodeHandling= unicodeHandling;
    this.format= format;
    this.reformatKeyAndValue= reformatKeyAndValue;
    this.attachCommentsTo= attachCommentsTo;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Creates a new ReformatOptions object with the default values.
   *
   * @return the newly created ReformatOptions object
   */
  public static ReformatOptions create() {
    return new ReformatOptions();
  }


  /**
   * Returns a copy of this ReformatOptions object, but with the given charset.
   *
   * @param charset the Charset to use when writing the PropertyFile.
   * @return this ReformatOptions object
   */
  public ReformatOptions with(final Charset charset) {
    return new ReformatOptions(charset, this.unicodeHandling, this.format, this.reformatKeyAndValue, this.attachCommentsTo);
  }


  /**
   * Returns a copy of this ReformatOptions object, but with the given UnicodeHandling value.
   *
   * @param unicodeHandling how to handle unicode characters on writing the PropertyFile
   * @return this ReformatOptions object
   */
  public ReformatOptions with(final UnicodeHandling unicodeHandling) {
    return new ReformatOptions(this.charset, unicodeHandling, this.format, this.reformatKeyAndValue, this.attachCommentsTo);
  }


  /**
   * Returns a copy of this ReformatOptions object, but with the given format string.
   * <p>
   * The given format string must conform to the following specification:
   * <ul>
   *  <li>It <i>may</i> contain some leading whitespace before the key.</li>
   *  <li>It <i>must</i> contain the string <code>&lt;key&gt;</code> to indicate the position of the
   *      properties key (case doesn't matter)</li>
   *  <li>It <i>must</i> contain a separator char (either a colon or an equals sign) which <i>may</i>
   *      be surrounded by some whitespace characters.</li>
   *  <li>It <i>must</i> contain the string <code>&lt;value&gt;</code> to indicate the position of the
   *      properties value (case doesn't matter)</li>
   *  <li>It <i>must</i> contain the line ending char(s) (either <code>\n</code> or <code>\r</code>
   *      or <code>\r\n</code>)</li>
   * </ul>
   * The allowed whitespace characters are the space character, the tab character and the linefeed character.
   * <p>
   * Therefore a typical format string is
   * <pre>
   * &lt;key&gt; = &lt;value&gt;\n
   * </pre>
   * for
   * <ul>
   *  <li>no leading whitespace</li>
   *  <li>an equals sign as separator surrounded by a single whitespace character on each side</li>
   *  <li><code>\n</code> as the line ending char.</li>
   * </ul>
   * But it may as well be
   * <pre>
   * \t \f&lt;key&gt;\t: &lt;value&gt;\r\n
   * </pre>
   * for a rather strange format with
   * <ul>
   *  <li>a tab, a whitespace and a linefeed char as leading whitespace</li>
   *  <li>a colon as separator char preceded by a tab and followed a single space character</li>
   *  <li><code>\r\n</code> as the line ending chars
   * </ul>
   * <p>
   * If the format string is omitted the default value of <code>&lt;key&gt; = &lt;value&gt;\n</code>
   * will be used.
   *
   * @param format the format string to use when reformatting a PropertyFile
   * @return this ReformatOptions object
   */
  public ReformatOptions withFormat(final String format) {
    return new ReformatOptions(this.charset, this.unicodeHandling, format, this.reformatKeyAndValue, this.attachCommentsTo);
  }


  /**
   * Returns a copy of this ReformatOptions object, but with the given value to reformat keys and values.
   * <p>
   * This value specifies whether the keys and values of the reformatted entries are also reformatted
   * by removing insignificant whitespace, newlines and escape characters.
   *
   * @param reformatKeyAndValue whether to reformat the key and value when reformatting a PropertyFile
   * @return this ReformatOptions object
   */
  public ReformatOptions withReformatKeyAndValue(final boolean reformatKeyAndValue) {
    return new ReformatOptions(this.charset, this.unicodeHandling, format, reformatKeyAndValue, this.attachCommentsTo);
  }


  /**
   * Returns a copy of this ReformatOptions object, but with the given AttachCommentsTo value.
   *
   * @param attachCommentsTo how to handle comments and empty lines when reordering a PropertyFile
   * @return this ReformatOptions object
   */
  public ReformatOptions with(final AttachCommentsTo attachCommentsTo) {
    return new ReformatOptions(this.charset, this.unicodeHandling, this.format, this.reformatKeyAndValue, attachCommentsTo);
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
   * Returns the UnicodeHandling to use when writing a PropertyFile.
   *
   * @return the UnicodeHandling to use when writing a PropertyFile
   */
  public UnicodeHandling getUnicodeHandling() {
    return unicodeHandling;
  }


  /**
   * Returns the format string to use when reformatting a PropertyFile.
   *
   * @return the format string to use when reformatting a PropertyFile
   */
  public String getFormat() {
    return format;
  }


  /**
   * Returns whether the key and value should be reformatted when reformatting a PropertyFile.
   *
   * @return whether the key and value should be reformatted when reformatting a PropertyFile
   */
  public boolean getReformatKeyAndValue() {
    return reformatKeyAndValue;
  }


  /**
   * Returns how to handle comments and empty lines when reordering a PropertyFile.
   *
   * @return how to handle comments and empty lines when reordering a PropertyFile
   */
  public AttachCommentsTo getAttachCommentsTo() {
    return attachCommentsTo;
  }


  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ReformatOptions) {
      final ReformatOptions that = (ReformatOptions) o;
      return this.charset.equals(that.getCharset())
        && this.unicodeHandling.equals(that.getUnicodeHandling())
        && this.format.equals(that.format)
        && this.reformatKeyAndValue == that.reformatKeyAndValue
        && this.attachCommentsTo.equals(that.attachCommentsTo)
        ;
    }
    return false;
  }


  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= charset.hashCode();
    h$ *= 1000003;
    h$ ^= unicodeHandling.hashCode();
    h$ *= 1000003;
    h$ ^= format.hashCode();
    h$ *= 1000003;
    h$ ^= reformatKeyAndValue ? 1 : 0;
    h$ *= 1000003;
    h$ ^= attachCommentsTo.hashCode();
    return h$;
  }


  @Override
  public String toString() {
    return "ReformatOptions{" + "charset=" + charset + ", unicodeHandling=" + unicodeHandling + ", format=" + format + ", reformatKeyAndValue=" + reformatKeyAndValue + ", attachCommentsTo=" + attachCommentsTo + '}';
  }
}
