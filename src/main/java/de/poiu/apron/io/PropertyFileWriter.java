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
package de.poiu.apron.io;

import de.poiu.apron.entry.Entry;
import de.poiu.apron.escaping.EscapeUtils;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A writer to write a PropertyFile to different targets.
 * <p>
 * Be aware that this class is not thread safe!
 *
 * @author mherrn
 */
public class PropertyFileWriter implements Closeable {

  private static final Logger LOGGER= Logger.getLogger(PropertyFileWriter.class.getName());


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** The actual Writer doing the writing */
  private final BufferedWriter writer;

  /** The charset to use for writing */
  private final Charset charset;

  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new PropertyFileWrite to write to the given file.
   * <p>
   * The file will be written in UTF-8 encoding.
   *
   * @param propertyFile the file to write to
   * @throws FileNotFoundException if the file cannot be written
   */
  public PropertyFileWriter(final File propertyFile) throws FileNotFoundException {
    this(propertyFile, Charset.forName("UTF-8"));
  }


  /**
   * Creates a new PropertyFileWrite to write to the given file.
   *
   * @param propertyFile the file to write to
   * @param charset the charset to use for writing
   * @throws FileNotFoundException if the file cannot be written
   */
  public PropertyFileWriter(final File propertyFile, final Charset charset) throws FileNotFoundException {
    this.writer= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propertyFile), charset));
    this.charset= charset;
  }

  /**
   *
   * @param writer
   * @deprecated Deprecated, since we would not be able to find out the Encoding of that writer.
   * But we need to know the encoding to decide whether we escape unicode sequences or not via \\uxxxx.
   */
  @Deprecated
  private PropertyFileWriter(final Writer writer) {
    this.writer= new BufferedWriter(writer);
    this.charset= null;
  }


  /**
   * Creates a new PropertyFileWrite to write to the given OutputStream.
   * <p>
   * UTF-8 encoding will be used to write to the OutputStream.
   *
   * @param outputStream the OutputStream to write to
   */
  public PropertyFileWriter(final OutputStream outputStream) {
    this(outputStream, Charset.forName("UTF-8"));
  }


  /**
   * Creates a new PropertyFileWrite to write to the given OutputStream.
   *
   * @param outputStream the OutputStream to write to
   * @param charset the charset to use for writing
   */
  public PropertyFileWriter(final OutputStream outputStream, final Charset charset) {
    this.writer= new BufferedWriter(new OutputStreamWriter(outputStream, charset));
    this.charset= charset;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Writes an Entry to this Writer.
   *
   * @param entry the entry to write
   * @throws IOException if the writing failed
   */
  public void writeEntry(final Entry entry) throws IOException {
    //FIXME: UTF-32 is not in the required Charsets. Should we still support it?
    if (charset != UTF_8
      && charset != UTF_16
      && charset != UTF_16LE
      && charset != UTF_16BE
      ) {
      // if the encoding is not one of the supported Unicode encodings
      // escape all unicode values with \\uxxxx
      writer.append(EscapeUtils.escapeUnicode(entry.toCharSequence()));
    } else {
      // …otherwise write the content as is
      writer.append(entry.toCharSequence());
    }
  }


  @Override
  public void close() throws IOException {
    if (this.writer != null) {
      try {
        this.writer.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Error closing writer.", e);
      }
    }
  }
}
