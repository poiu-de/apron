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
package de.poiu.apron.escaping;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Helper class for escaping and unescaping of entries in .properties files.
 *
 * @author mherrn
 */
public class EscapeUtils {

  private static final Logger LOGGER= Logger.getLogger(EscapeUtils.class.getName());

  /** The valid hex digits. Used for un-/escaping of unicode characters */
  private static final char[] HEX_DIGITS= {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    , 'a', 'b', 'c', 'd', 'e', 'f'
    , 'A', 'B', 'C', 'D', 'E', 'F' };


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Translates escaped Unicode values of the form \\u\d\d\d\d (where \d is a valid hex
   * digig) back to Unicode.
   *
   * @param input CharSequence that is being translated
   * @return the result of the conversion
   * @throws InvalidUnicodeCharacterException if the given excape sequence cannot be conerted to unicode
   */
  static char translateUnicode(final CharSequence input) throws InvalidUnicodeCharacterException {
    // Get 4 hex digits
    final CharSequence unicode = input.subSequence(2, 6);
    try {
      final int value = Integer.parseInt(unicode.toString(), 16);
      return (char) value;
    } catch (final NumberFormatException nfe) {
      throw new InvalidUnicodeCharacterException("Unable to parse unicode value: " + unicode, nfe);
    }
  }


  /**
   * Returns a copy of the given CharSequence where leading whitespace from each line
   * is removed.
   *
   * @param s the CharSeuqence to operate on
   * @return the given CharSequences with leading whitespace stripped from all lines
   */
  public static CharSequence removeLeadingWhitespace(final CharSequence s) {
    final StringBuilder sb= new StringBuilder();

    boolean nonWhitespaceFound= false;
    for (int i=0; i < s.length(); i++) {
      final char c= s.charAt(i);

      if (c == '\n') {
        nonWhitespaceFound= false;
        sb.append(c);
      } else if (c == '\r') {
        if (i < s.length() && (char) s.charAt(i) == '\n') {
          i++;
        }
        nonWhitespaceFound= false;
        sb.append(c);
      } else if (!nonWhitespaceFound && (c == ' ' || c == '\t' || c == '\f')) {
        // this is whitespace at the beginning of a line, we ignore it
      } else {
        nonWhitespaceFound= true;
        sb.append(c);
      }
    }

    return sb;
  }


  /**
   * Unescapes a given char sequence. The following unescaping will be done by this method:
   * <ul>
   *  <li>all \\uXXXX unicode escape sequences are replaced by the actual unicode value</li>
   *  <li>all backslashes that escape characters that need no escaping are removed</li>
   *  <li>backslashes as the last character are removed</li>
   *  <li>literal newlines are replaced by real newlines</li>
   *  <li>whitespace after a newline is removed</li>
   * </ul>
   * <p>
   * This method does not throw an exception. If an invalid unicode escape sequence is found, an
   * error will be logged and the invalid escape sequence will be left unmodified.
   * <p>
   * This method may be used for property keys as well as property values. There is no difference
   * in the unescaping.
   *
   * @param s the char sequence to unescape
   * @return  the unescaped char sequence
   */
  public static CharSequence unescape(final CharSequence s) {
    final StringBuilder sb= new StringBuilder();

    boolean nonWhitespaceFound= false;
    for (int i=0; i < s.length(); i++) {
      final char c= s.charAt(i);

      if (c == '\\') {
        // if this is the last character, just remove it
        if (i == s.length() -1) {
          continue;
        }

        // two backslashes are reduced to one
        if (i + 1 < s.length() && (char) s.charAt(i + 1) == '\\') {
          sb.append(c);
          i++;
          continue;
        }

        // process unicode escape sequence
        if (i + 1 < s.length() && (char) s.charAt(i + 1) == 'u') {
          if (i + 5 < s.length()) {
            // only try to unescape a unicode value if there are enough characters left…
            try {
              // translate the escape sequence to unicode and add that unicode character
              sb.append(translateUnicode(s.subSequence(i, i + 6)));
              i= i+5;
            } catch (InvalidUnicodeCharacterException e) {
              // if the character cannot be translated, leave the escape sequence as it is,
              // but log an error.
              sb.append(c);
              LOGGER.log(Level.SEVERE, "Found invalid unicode escape sequence {0}. No conversion will be done. Be aware that this file cannot be read by java.util.Properties! The escape sequence should be fixed!", s.subSequence(i, i + 6));
            }
            continue;
          } else {
            //…otherwise just leave it as it is
            sb.append(c);
            LOGGER.log(Level.SEVERE, "Found invalid unicode escape sequence {0}. No conversion will be done. Be aware that this file cannot be read by java.util.Properties! The escape sequence should be fixed!", s.subSequence(i, i + s.length() - i));
          }
        }

        //replace literal newline with real newline
        if (i + 1 < s.length() && (char) s.charAt(i + 1) == 'n') {
          sb.append('\n');
          i++;
        } else if (i  + 1 < s.length() && (char) s.charAt(i + 1) == 'r') {
          sb.append('\r');
          i++;
          if (i + 2 < s.length() && (char) s.charAt(i + 2) == '\\' && (char) s.charAt(i + 3) == 'n') {
            sb.append('\n');
            i+=2;
          }
        }

        // in all other cases the backslash is silently dropped

      } else if (c == '\n') {
        // remove all newline characters
        nonWhitespaceFound= false;
      } else if (c == '\r') {
        // remove all newline characters
        if (i + 1 < s.length() && (char) s.charAt(i + 1) == '\n') {
          i++;
        }
        nonWhitespaceFound= false;
      } else if (!nonWhitespaceFound && (c == ' ' || c == '\t' || c == '\f')) {
        // this is whitespace at the beginning of a line, we ignore it
      } else {
        // all other characters go to the output
        nonWhitespaceFound= true;
        sb.append(c);
      }
    }

    return sb;
  }


  /**
   * Returns a copy of the given CharSequence where all Unicode escaped sequences are
   * replaced by their actual unicode value.
   *
   * @param s the CharSequence to operate on
   * @return the given CharSequence with all unicode escape sequences replaced by their actual unicode value
   */
  //This method is very much "inspired" by org.apache.commons.text.translate.UnicodeEscaper
  public static CharSequence unescapeUnicode(final CharSequence s) {
    final StringBuilder sb= new StringBuilder();

    for (int i=0; i < s.length(); i++) {
      final char c= s.charAt(i);

      if (c == '\\') {
        // process unicode escape sequence
        if (i + 1 < s.length() && (char) s.charAt(i + 1) == 'u') {
          if (i + 5 < s.length()) {
            // only try to unescape a unicode value if there are enough characters left…
            try {
              // translate the escape sequence to unicode and add that unicode character
              sb.append(translateUnicode(s.subSequence(i, i + 6)));
              i= i+5;
            } catch (InvalidUnicodeCharacterException e) {
              // if the character cannot be translated, leave the escape sequence as it is,
              // but log an error.
              sb.append(c);
              LOGGER.log(Level.SEVERE, "Found invalid unicode escape sequence {0}. No conversion will be done. Be aware that this file cannot be read by java.util.Properties! The escape sequence should be fixed!", s.subSequence(i, i + 6));
            }
          } else {
            //…otherwise just leave it as it is
            sb.append(c);
            LOGGER.log(Level.SEVERE, "Found invalid unicode escape sequence {0}. No conversion will be done. Be aware that this file cannot be read by java.util.Properties! The escape sequence should be fixed!", s.subSequence(i, i + s.length() - i));
          }
        } else {
          // if this backslash is not part of unicode escape, just leave it as it is
          sb.append(c);
        }
      } else {
        // all other characters go unmodified to the output
        sb.append(c);
      }
    }

    return sb;
  }


  /**
   * Returns a copy of the given CharSequence where all characters that need to be
   * escaped to be used as a value in a .properties file are escaped.
   * <p>
   * The following conversions are done when escaping property values:
   * <ul>
   *  <li>newline characters are translated to literal newlines.</li>
   *  <li>backslashes are escaped by a leading backslash</li>
   * </ul>
   * <p>
   * Unicode values remain in their Unicode form and are not replaced by \\uXXXX unicode escape sequences.
   * This will be done when writing (if necessary)
   *
   * @param s the CharSequence to operate on
   * @return the given CharSequences escaped to be used as a value in a .properties file
   */
  public static CharSequence escapePropertyValue(final CharSequence s) {
    final StringBuilder sb= new StringBuilder();

    for (int i=0; i < s.length(); i++) {
      final char c= s.charAt(i);

      //FIXME: Should this be changed to a switch-statement now? Could be more readable.
      //       Maybe this should be done when other characters need to be prependend by a backslash.
      //       In that case we can use the same 'case'
      if (c == '\n') {
        sb.append('\\');
        sb.append('n');
      } else if (c == '\r') {
        sb.append('\\');
        sb.append('r');
      } else if (c == '\\') {
        sb.append('\\');
        sb.append(c);
      } else {
        sb.append(c);
      }
    }

    return sb;
  }


  /**
   * Returns a copy of the given CharSequence where all characters that need to be
   * escaped to be used as a key in a .properties file are escaped by a backslash.
   * These are actually
   * <ul>
   *  <li>newline characters</li>
   *  <li>whitespace characters</li>
   *  <li>the comment characters '#' and '!'</li>
   *  <li>the assignment characters '=' and ':'</li>
   *  <li>backslash characters</li>
   * </ul>
   * <p>
   * Unicode values remain in their Unicode form and are not replaced by \\uXXXX unicode escape sequences.
   * This will be done when writing (if necessary)
   *
   * @param s the CharSequence to operate on
   * @return the given CharSequences escaped to be used as a key in a .properties file
   */
  public static CharSequence escapePropertyKey(final CharSequence s) {
    final StringBuilder sb= new StringBuilder();

    for (int i=0; i < s.length(); i++) {
      final char c= s.charAt(i);

      if (c == ' ' || c == '\t' || c == '\f'
        || c == '=' || c == ':'
        || c == '\n' || c =='\r'
        || c == '#' || c == '!'
        || c == '\\'
        ) {
        sb.append('\\');
      }

      sb.append(c);

      // do not escape the \n in a \r\n sequence
      if (c == '\r' && i + 1 < s.length()) {
        final char nextChar= s.charAt(i + 1);
        if (nextChar == '\n') {
          sb.append(nextChar);
          i++;
        }
      }
    }

    return sb;
  }


  /**
   * Escapes a unicode character to a unicode escape sequence of the form \\uXXXX.
   * This method does not check whether a character needs such escaping. If a valid ASCII or
   * ISO-8859-1 character is given, this we be escaped as well.
   *
   * @param c the unicode character to escape
   * @return the escaped unicode character
   */
  //This method is very much "inspired" by org.apache.commons.text.translate.UnicodeEscaper
  public static CharSequence escapeUnicode(final char c) {
    final StringBuilder sb= new StringBuilder();

    final int codepoint= (int) c;
    if (codepoint > 0xffff) {
      sb.append("\\u").append(Integer.toHexString(codepoint));
    } else {
      sb.append("\\u");
      sb.append(HEX_DIGITS[(codepoint >> 12) & 15]);
      sb.append(HEX_DIGITS[(codepoint >> 8) & 15]);
      sb.append(HEX_DIGITS[(codepoint >> 4) & 15]);
      sb.append(HEX_DIGITS[(codepoint) & 15]);
    }

    return sb;
  }


  /**
   * Escapes all unicode characters in a CharSequence to a unicode escape sequence of
   * the form \\uXXXX.
   * This escapes all characters with codepoints &gt; 0x7f.
   *
   * @param charSequence the CharSequence to escape
   * @return the CharSequence with unicode characters escaped
   */
  //This method is very much "inspired" by org.apache.commons.text.translate.UnicodeEscaper
  public static CharSequence escapeUnicode(final CharSequence charSequence) {
    final StringBuilder sb= new StringBuilder();

    for (int i=0; i < charSequence.length(); i++) {
      final int codepoint= (int) charSequence.charAt(i);

      if (codepoint <= 0x7f) {
        // all characters up to 0x7f are written as is
        sb.append(charSequence.charAt(i));
      } else {
        // all characters above 0x7f are written as unicode escape sequences
        if (codepoint > 0xffff) {
          sb.append("\\u").append(Integer.toHexString(codepoint));
        } else {
          sb.append("\\u");
          sb.append(HEX_DIGITS[(codepoint >> 12) & 15]);
          sb.append(HEX_DIGITS[(codepoint >> 8) & 15]);
          sb.append(HEX_DIGITS[(codepoint >> 4) & 15]);
          sb.append(HEX_DIGITS[(codepoint) & 15]);
        }
      }
    }

    return sb;
  }


  /**
   * Checks whether the given char <code>c</code> is one of the chars in
   * <code>possibleValues</code>.
   *
   * @param c the char to check for
   * @param possibleValues the reference chars
   * @return whether the given char one of the given possible values
   */
  private static boolean isOf(final char c, final char[] possibleValues) {
    for (char p : possibleValues) {
      if (p == c) {
        return true;
      }
    }
    return false;
  }


  /**
   * Comments a CharSequence. This is done by prepending it with a '#' character.
   * <p>
   * Since PropertyEntries can span multiple lines, this method also prepends each consecutive line
   * with a '#' character. However there is not check whether the given CharSequence is a PropertyEntry.
   * This method just comments out all lines in the given CharSequence.
   *
   * @param charSequence the CharSequence to comment out
   * @return the commented out CharSequence
   */
  //FIXME: This is not real escaping. Should this method be moved to another (probably new) class?
  public static CharSequence comment(final CharSequence charSequence) {
    final StringBuilder sb= new StringBuilder();

    //start with a comment character
    sb.append("#");

    for (int i=0; i < charSequence.length(); i++) {
      final char c= charSequence.charAt(i);

      sb.append(c);

      // add a comment character after each newline
      if (c == '\n' || c == '\r' ) {
        if (c == '\r' && i + 1 < charSequence.length() && charSequence.charAt(i + 1) == '\n') {
          sb.append('\n');
          i++;
        }

        //only append a comment char if there really are more characters after the newline
        if (i + 1 < charSequence.length()) {
          sb.append("#");
        }
      }
    }

    return sb;
  }
}
