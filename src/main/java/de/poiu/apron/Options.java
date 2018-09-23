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
 * @author mherrn
 */
public class Options {
  private Charset charset= UTF_8;
  private MissingKeyAction missingKeyAction= MissingKeyAction.NOTHING;



  /**
   * Creates a new Options object with the default values.
   * @return the newly created Options object
   */
  public static Options create() {
    return new Options();
  }


  /**
   * Returns this Options object, but set the the charset option to the given value.
   *
   * @param charset the Charset to use when writing the PropertyFile.
   * @return this Options object
   */
  public Options with(final Charset charset) {
    Objects.requireNonNull(charset);
    this.charset= charset;
    return this;
  }


  /**
   * Returns this Options object, but set the the MissingKeyAction option to the given value.
   * <p>
   * This is only meaningful on updating a File. When writing to an output stream or overwriting a
   * file or creating a new file, this options does nothing.
   *
   * @param missingKeyAction how to handle key-value-pairs that exist in in the written PropertyFile, but not in the updated one
   * @return this Options object
   */
  public Options with(final MissingKeyAction missingKeyAction) {
    Objects.requireNonNull(missingKeyAction);
    this.missingKeyAction= missingKeyAction;
    return this;
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
}
