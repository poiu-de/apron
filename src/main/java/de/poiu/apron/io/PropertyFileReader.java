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

import de.poiu.apron.entry.BasicEntry;
import de.poiu.apron.entry.Entry;
import de.poiu.apron.entry.PropertyEntry;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A reader to read a PropertyFile from different sources.
 * <p>
 * Be aware that this class is not thread safe!
 *
 * @author mherrn
 */
public class PropertyFileReader implements Closeable {

  private static final Logger LOGGER= Logger.getLogger(PropertyFileReader.class.getName());


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** The actual reader to read. */
  private final BufferedReader reader;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new PropertyFileReader to read from the given file.
   * <p>
   * The file is assumed to be UTF-8 encoded.
   *
   * @param propertyFile the file to read
   * @throws java.io.FileNotFoundException if the given file does not exist
   */
  public PropertyFileReader(final File propertyFile) throws FileNotFoundException {
    this(propertyFile, Charset.forName("UTF-8"));
  }


  /**
   * Creates a new PropertyFileReader to read from the given file.
   * <p>
   * The file is assumed to be in the given encoding.
   *
   * @param propertyFile the file to read
   * @param charset the encoding of the file
   * @throws java.io.FileNotFoundException if the given file does not exist
   */
  public PropertyFileReader(final File propertyFile, final Charset charset) throws FileNotFoundException {
    this.reader= new BufferedReader(new InputStreamReader(new FileInputStream(propertyFile), charset));
  }


  /**
   * Creates a new PropertyFileReader to read from the given reader.
   *
   * @param reader the reader to read from
   */
  public PropertyFileReader(final Reader reader) {
    this.reader= new BufferedReader(reader);
  }


  /**
   * Creates a new PropertyFileReader to read from the given InputStream.
   * <p>
   * The stream is assumed to be UTF-8 encoded.
   *
   * @param inputStream the InputStream to read from
   */
  public PropertyFileReader(final InputStream inputStream) {
    this(inputStream, Charset.forName("UTF-8"));
  }


  /**
   * Creates a new PropertyFileReader to read from the given InputStream.
   * <p>
   * The stream is assumed to be in the given encoding.
   *
   * @param inputStream the InputStream to read from
   * @param charset the encoding of the stream
   */
  public PropertyFileReader(final InputStream inputStream, final Charset charset) {
    this.reader= new BufferedReader(new InputStreamReader(inputStream, charset));
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Reads a single entry from this reader.
   * <p>
   * If no the source does not provide any more entries, <code>null</code> is returned.
   *
   * @return the next Entry or <code>null</code> if there are no more entries
   * @throws java.io.IOException if reading the next entry failed
   */
  public Entry readEntry() throws IOException {
    final CharSequence logicalLine= readLogicalLine();

    if (logicalLine != null) {
      // process this logical line
      final Entry entry= this.parseLogicalLine(logicalLine);
      return entry;
    } else {
      // or return null if there are no more entries
      return null;
    }
  }


  /**
   * Reads a logical line from the reader.
   * This actually reads until the next unescaped line break
   * (either '\n', '\r' or '\r' immediately followed by '\n').
   * The line break will be included in the string.
   * <p>
   * Be aware that this method <i>always</i> respects the escaping of line breaks, though the
   * {@link java.util.Properties#load(java.io.InputStream) } API states that this is not valid
   * for comment lines. Therefore the returned logical line may be split if it contains a comment
   * line with a backslash as the last character.
   *
   * @return <code>null</code>, if there is nothing more to read or a string containing the
   *          logical lines content including the newline characters
   * @throws IOException
   */
  private CharSequence readLogicalLine() throws IOException {
    // if there is nothing more to read, return null to indicate the EOS
    if (!reader.ready()) {
      return null;
    }

    // otherwise read the next logical line into a StringBuilder
    final StringBuilder sb= new StringBuilder();

    int cInt;
    boolean escaped= false;
    boolean isCommentLine= false;
    boolean isEmptyLine= true;
    while ((cInt= reader.read()) != -1) {
      final char c= (char) cInt;
      sb.append(c);

      //if the first non-whitespace character is a comment char, this is a comment
      if (isEmptyLine && (c == '#' || c == '!')) {
        isCommentLine= true;
        isEmptyLine= false;
      }

      //if any non-whitespace character is found, this line is not empty
      if (isEmptyLine && c != ' ' && c != '\t' && c != '\f' && !escaped) {
        // still consider this line as empty if the current char is a backslash and the next a newline
        reader.mark(1);
        final int nextCInt= reader.read();
        reader.reset();
        if (c != '\\' || (nextCInt != -1 && (nextCInt != '\n' && nextCInt != '\r'))) {
          isEmptyLine= false;
        }
      }

      // Stop at the first unescaped newline (or each newline for comment and empty lines)
      if (c == '\n' && (!escaped || isCommentLine || isEmptyLine)) {
        break;
      } else if (c == '\r' && !escaped) {
        // If the next character is \n consume that as well
        reader.mark(1);
        final int nextCInt= reader.read();
        if (nextCInt != -1 && nextCInt == '\n') {
          sb.append((char) nextCInt);
        } else {
          reader.reset();
        }
        break;
      }

      if (c == '\r' && escaped) {
        // check for \r\n sequence - in that case, both should be escaped, keep escaped flag on
        reader.mark(1);
        final int nextCInt= reader.read();
        // not the \r\n case
        if (nextCInt != '\n') {
          escaped = false;
        }
        reader.reset();
      } else {
        escaped = c == '\\' && !escaped;
      }
    }

    return sb;
  }


  /**
   * Parses a logical line into an Entry.
   * <p>
   * If the logical line is empty or a comment, a BasicEntry will be returned. Otherwise a
   * PropertyEntry will be returned.
   *
   * @param logicalLine the line to process
   * @return the Entry for the given line
   */
  private Entry parseLogicalLine(CharSequence logicalLine) {
    if (this.isComment(logicalLine) || this.isEmpty(logicalLine)) {
      return new BasicEntry(logicalLine.toString());
    }

    // if the line was no comment and not empty it is a valid key-value-pair
    final CharSequence leadingWhitespace= this.parseLeadingWhitespace(logicalLine);
    final CharSequence key= this.parseKey(logicalLine, leadingWhitespace.length());
    final CharSequence separator= this.parseSeparator(logicalLine, leadingWhitespace.length() + key.length());
    final CharSequence valueWithLineEnding= this.parseValue(logicalLine, leadingWhitespace.length() + key.length() + separator.length());
    final CharSequence[] valueAndLineEnding= this.splitValueAndLineEnding(valueWithLineEnding);
    final CharSequence value= valueAndLineEnding[0];
    final CharSequence lineEnding= valueAndLineEnding[1].length() > 0 ? valueAndLineEnding[1] : "\n";

    return new PropertyEntry(leadingWhitespace.toString(), key.toString(), separator.toString(), value.toString(), lineEnding.toString());
  }


  /**
   * Checks whether the given logical line is a comment line.
   * <p>
   * A line is considered a comment line if the first non-whitespace character is either a '#'
   * or a '!'.
   *
   * @param logicalLine the logical line to check
   * @return whether the given line is a comment line
   */
  private boolean isComment(final CharSequence logicalLine) {
    for (int i= 0; i < logicalLine.length(); i++) {
      final char c= logicalLine.charAt(i);

      // skip all whitespace
      if (c == ' ' || c == '\t' || c == '\f' || c == '\n' || c == '\r') {
        continue;
      }

      // if the first read non-whitespace character is a comment indicator, then this is a comment
      if (c == '!' || c == '#') {
        return true;
      } else {
        return false;
      }
    }

    //return false if the input string was empty
    return false;
  }


  /**
   * Checks whether the given logical line is an empty line.
   * <p>
   * A line is considered empty if it contains only non-escaped whitespace characters.
   *
   * @param logicalLine the logical line to check
   * @return whether the given line is empty
   */
  private boolean isEmpty(final CharSequence logicalLine) {
    boolean escaped= false;
    for (int i= 0; i < logicalLine.length(); i++) {
      final char c= logicalLine.charAt(i);

      //a backslash as the last character of the line is ignored
      if (c == '\\') {
        if (i + 1 < logicalLine.length() && !escaped) {
          final char nextChar= logicalLine.charAt(i + 1);
          if (nextChar == '\n' || nextChar == '\r') {
            return true;
          }
        } else {
          escaped= !escaped;
        }
      }

      // if any non-whitespace character was found, this line is not whitespace
      if (c != ' ' && c != '\t' && c != '\f' && c != '\n' && c != '\r') {
        return false;
      }
    }

    //return true if the input string was empty or we have not found any non-whitespace character
    return true;
  }


  /**
   * Returns the leading whitespace from the given logical line.
   *
   * @param logicalLine the line to process
   * @return the leading whitespace from the given logical line
   */
  private CharSequence parseLeadingWhitespace(final CharSequence logicalLine) {
    for (int i= 0; i < logicalLine.length(); i++) {
      final char c= logicalLine.charAt(i);

      // if there is no key, the whitespace is assigned to the separator and no leading whitespace remains
      if (c == '=' || c == ':') {
        return "";
      }

      if (c != ' ' && c != '\t' && c != '\f' && c != '\n' && c != '\r') {
        return logicalLine.subSequence(0, i);
      }
    }

    return logicalLine.toString();
  }


  /**
   * Returns the key from the given logical line.
   * <p>
   * Special characters in the key (e.g. whitespace) will still be escaped in the returned key.
   * <p>
   * This method requires that leading whitespace was already read and the therefore the starting
   * index of the key is known.
   *
   * @param logicalLine the logical line to process
   * @param startAt the starting position of the key
   * @return the key from the given logical line
   */
  private CharSequence parseKey(final CharSequence logicalLine, final int startAt) {
    // Must be set to true at the start of each line and to false when the first non-whitespace
    // character was read
    boolean ignoreWhitespace= false;
    // Remembers the start of the following whitespace that may or may not be part of a key
    int startOfWhitespace= -1;

    for (int i= startAt; i < logicalLine.length(); i++) {
      final char c= logicalLine.charAt(i);

      // skip whitespace at the start of each line
      if (ignoreWhitespace && (c == ' ' || c == '\t' || c == '\f')) {
        continue;
      }

      // set ignoreWhitespace to true if we reached the end of the line
      // and add the read content to the result
      if (c == '\n' || c == '\r') {
        //consume following \n if present
        if (i + 1 < logicalLine.length()) {
          final char nextChar= logicalLine.charAt(i + 1);
          if (c == '\r' && nextChar == '\n') {
            i++;
          }
        }

        ignoreWhitespace= true;
      }

      // if this character is a backslash treat the next one as part of the key
      if (c == '\\' && i + 1 < logicalLine.length()) {
        // if the next character is a newline, then we need to ignore whitespace again
        final char nextChar= (char) logicalLine.charAt(i + 1);
        if (nextChar == '\n' || nextChar == '\r') {
          ignoreWhitespace= true;
        }

        i++;
        startOfWhitespace= i + 1;
      } else {
        // a non-escaped whitespace, equals-sign or colon means that we have reached the
        // separator and therefore can stop there
        if (c == ' ' || c == '\t' || c == '\f' || c == '\n' || c == '\r' || c == '=' || c == ':') {
          if (startOfWhitespace != -1) {
            return logicalLine.subSequence(startAt, startOfWhitespace);
          } else {
            return logicalLine.subSequence(startAt, i);
          }
        } else {
          ignoreWhitespace= false;
          startOfWhitespace= -1;
        }
      }
    }

    // if we didn't find an end of the key, the whole remaining characters are the key
    return logicalLine.subSequence(startAt, logicalLine.length());
  }


  /**
   * Returns the separator with surrounding whitespace from the given logical line.
   * <p>
   * This method requires that leading whitespace and key were already read and the therefore
   * the starting index of the separator (or its surrounding whitespace) is known.
   *
   * @param logicalLine the logical line to process
   * @param startAt the starting position of the separator
   * @return the separator with its surrounding whitespace from the given logical line
   */
  private CharSequence parseSeparator(final CharSequence logicalLine, final int startAt) {
    // the equals-sign and the colon may occur only once
    boolean separatorCharConsumed= false;

    for (int i= startAt; i < logicalLine.length(); i++) {
      final char c= logicalLine.charAt(i);

      if (c == '=' || c == ':') {
        if (separatorCharConsumed) {
          // if we found another occurrence of a non-whitespace separator char this will be part of
          // the value
          return logicalLine.subSequence(startAt, i);
        } else {
          // otherwise we mark that we have found a non-whitespace separator char
          separatorCharConsumed= true;
        }
      }

      // if any non-separator char was found, we can stop here
      if (c != ' ' && c != '\t' && c != '\f' && c != '=' && c != ':') {
        return logicalLine.subSequence(startAt, i);
      }
    }

    // if we didn't find an end of the separator, the whole remaining characters are the separator
    return logicalLine.subSequence(startAt, logicalLine.length());
  }


  /**
   * Returns the value from the given logical line.
   * <p>
   * Special characters in the value (e.g. newline characters) will still be escaped in the returned key.
   * <p>
   * This method requires that leading whitespace, key and separator were already read and the
   * therefore the starting index of the value is known.
   * <p>
   *
   *
   * @param logicalLine the logical line to process
   * @param startAt the starting position of the value
   * @return the value from the given logical line
   */
  private CharSequence parseValue(final CharSequence logicalLine, final int startAt) {
    // Must be set to true at the start of each line and to false when the first non-whitespace
    // character was read
    boolean ignoreWhitespace= true;

    Optional<Integer> firstNonWhitespaceCharPos= Optional.empty();

    for (int i= startAt; i < logicalLine.length(); i++) {
      final char c= logicalLine.charAt(i);

      // skip whitespace at the start of each line
      if (ignoreWhitespace && (c == ' ' || c == '\t' || c == '\f')) {
        continue;
      }

      // if we found the first nonWhitespace char, remmber it
      if (!firstNonWhitespaceCharPos.isPresent()) {
        firstNonWhitespaceCharPos= Optional.of(i);
      }

      // set ignoreWhitespace to true if we reached the end of the line
      // and add the read content to the result
      if (c == '\n' || c == '\r') {
        //consume following \n if present
        if (i + 1 < logicalLine.length()) {
          final char nextChar= logicalLine.charAt(i + 1);
          if (c == '\r' && nextChar == '\n') {
            i++;
          }
        }

        ignoreWhitespace= true;
      }
    }

    return logicalLine.subSequence(firstNonWhitespaceCharPos.orElse(startAt), logicalLine.length());
  }


  /**
   * Splits a CharSequence known to be a value and its (optional) trailing newline
   * character(s) into a CharSequence array with exactly two parts.
   * <p>
   * The first part will be the actual value without the trailing newline character(s),
   * the second one will be the trailing newline characters.
   * <p>
   * Both CharSequences are guaranteed to be non-null. If any of them does not exist an empty
   * CharSequence will be returned for the part.
   *
   * @param valueWithLineEnding the CharSequence to process
   * @return an array containing the value at the first position and the trailing newline character(s) as the second position
   */
  private CharSequence[] splitValueAndLineEnding(CharSequence valueWithLineEnding) {
    final CharSequence[] result= new CharSequence[2];

    for (int i= valueWithLineEnding.length() - 1; i > -1; i--) {
      final char c= valueWithLineEnding.charAt(i);

      if (c != '\r' && c != '\n') {
        result[0]= valueWithLineEnding.subSequence(0, i + 1);
        result[1]= valueWithLineEnding.subSequence(i + 1, valueWithLineEnding.length());
        return result;
      }
    }

    // if we only found newline characters, the value will be empty
    result[0]= "";
    result[1]= valueWithLineEnding;
    return result;
  }


  @Override
  public void close() throws IOException {
    if (this.reader != null) {
      try {
        this.reader.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Error closing reader.", e);
      }
    }
  }
}
