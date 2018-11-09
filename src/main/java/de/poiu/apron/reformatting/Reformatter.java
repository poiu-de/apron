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

import de.poiu.apron.ApronOptions;
import de.poiu.apron.PropertyFile;
import de.poiu.apron.entry.BasicEntry;
import de.poiu.apron.entry.Entry;
import de.poiu.apron.entry.PropertyEntry;
import de.poiu.apron.escaping.EscapeUtils;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Reformat .properties files.
 *
 * @author mherrn
 * @since 2.0.0
 */
public class Reformatter {

  private static final Logger LOGGER= Logger.getLogger(Reformatter.class.getName());

  /**
   * The placeholder for the property key to be used as a snipped in a regex.
   */
  private static final String PATTERN_SNIPPET_PLACEHOLDER_KEY= "<KEY>";

  /**
   * The placeholder for the value key to be used as a snipped in a regex.
   */
  private static final String PATTERN_SNIPPET_PLACEHOLDER_VALUE= "<VALUE>";

  /**
   * The allowed whitespace characters to be used as a snipped in a regex.
   */
  private static final String PATTERN_SNIPPET_WHITESPACE= "( |\\\\t|\\\\f)";

  /**
   * The allowed separator characters along with the allowed surrounding whitespace characters
   * to be used as a snipped in a regex.
   */
  private static final String PATTERN_SNIPPET_SEPARATOR= "( |\\\\t|\\\\f|=|:)";

  /**
   * The allowed line ending characters to be used as a snipped in a regex.
   */
  private static final String PATTERN_SNIPPET_LINE_ENDING= "(\\\\n|\\\\r|\\\\r\\\\n)";


  /**
   * A pattern specifying a valid format string for reformatting key-value pairs in .properties files.
   * <p>
   * This pattern defines the following capture groups to refer to parts of the parsed format string:
   * <ul>
   *   <li>LEADINGWHITESPACE</li>
   *   <li>SEPARATOR</li>
   *   <li>LINEENDING</li>
   * </ul>
   * The pattern matches case insensitive. Therefore the placeholders &lt;key&gt; and &lt;value&gt;
   * may be given in any case.
   */
  protected static final Pattern PATTERN_PROPERTY_FORMAT= Pattern.compile(""
    + "^"
    + "(?i)" // match case insensitive
    + "(?<LEADINGWHITESPACE>"+PATTERN_SNIPPET_WHITESPACE+"*)"   // optional leading whitespace
    + PATTERN_SNIPPET_PLACEHOLDER_KEY
    + "(?<SEPARATOR>"+PATTERN_SNIPPET_WHITESPACE+"*"+PATTERN_SNIPPET_SEPARATOR+""+PATTERN_SNIPPET_WHITESPACE+"*)"  // separator char with optional surrounding whitespace
    + PATTERN_SNIPPET_PLACEHOLDER_VALUE
    + "(?<LINEENDING>"+PATTERN_SNIPPET_LINE_ENDING+")" // line ending
    + "$"
  );


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  private final ReformatOptions reformatOptions;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new Reformatter with the default ReformatOptions.
   */
  public Reformatter() {
    this.reformatOptions= new ReformatOptions();
  }


  /**
   * Creates a new Reformatter with the given ReformatOptions.
   *
   * @param reformatOptions the ReformatOptiosn to use with this Reformatter
   */
  public Reformatter(final ReformatOptions reformatOptions) {
    this.reformatOptions= reformatOptions;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Reformats the key-value pairs in the given .properties file according to the default
   * {@link de.poiu.apron.reformatting.ReformatOptions}.
   * <p>
   * This method actually changes the file on disk.
   *
   * @param propertyFile the .properties file whose key-value pairs to reformat
   * @throws de.poiu.apron.reformatting.InvalidFormatException if the given format string is invalid
   */
  public void reformat(final File propertyFile) {
    this.reformat(propertyFile, this.reformatOptions);
  }


  /**
   * Reformats the key-value pairs in the given .properties file according to the given
   * ReformatOptions.
   * <p>
   * This method actually changes the files on disk.
   *
   * @param file the .properties file whose key-value pairs to reformat
   * @param reformatOptions the ReformatOptions to use when reformatting the .properties file
   * @throws de.poiu.apron.reformatting.InvalidFormatException if the given format string is invalid
   */
  public void reformat(final File file, final ReformatOptions reformatOptions) {
    Objects.requireNonNull(file);
    Objects.requireNonNull(reformatOptions);

    final PropertyFile pf= PropertyFile.from(file, reformatOptions.getCharset());

    this.reformat(pf, reformatOptions);
    pf.overwrite(file, ApronOptions.create().with(reformatOptions.getCharset()));
  }


  /**
   * Reformats the PropertyEntries in the given PropertyFile according to the default
   * {@link de.poiu.apron.reformatting.ReformatOptions}.
   * <p>
   * This method actually changes the files on disk.
   *
   * @param propertyFile the PropertiesFile whose PropertyEntries to reformat
   * @throws de.poiu.apron.reformatting.InvalidFormatException if the given format string is invalid
   */
  public void reformat(final PropertyFile propertyFile) {
    this.reformat(propertyFile, this.reformatOptions);
  }


  /**
   * Reformats the PropertyEntries in the given PropertyFile according to the given
   * ReformatOptions.
   * <p>
   * This method does not change any files on disk.
   *
   * @param propertyFile the PropertiesFile whose PropertyEntries to reformat
   * @param reformatOptions the reformat options to use when reformatting the PropertyFile
   * @throws de.poiu.apron.reformatting.InvalidFormatException if the given format string is invalid
   */
  public void reformat(final PropertyFile propertyFile, final ReformatOptions reformatOptions) {
    Objects.requireNonNull(propertyFile);
    Objects.requireNonNull(reformatOptions);

    final PropertyFormat propertyFormat= this.parseFormat(reformatOptions.getFormat());

    final List<Entry> formattedEntries= new ArrayList<>();

    for (final Entry entry : propertyFile.getAllEntries()) {
      if (entry instanceof PropertyEntry) {
        // exchange leading whitespace, separator and line ending on PropertyEntries
        final PropertyEntry propertyEntry= (PropertyEntry) entry;
        final PropertyEntry formattedEntry= new PropertyEntry(
          propertyFormat.leadingWhitespace,
          reformatOptions.getReformatKeyAndValue() ? reformatKey(propertyEntry.getKey()) : propertyEntry.getKey(),
          propertyFormat.separator,
          reformatOptions.getReformatKeyAndValue() ? reformatValue(propertyEntry.getValue()) : propertyEntry.getValue(),
          propertyFormat.lineEnding);
        formattedEntries.add(formattedEntry);
      } else if (entry instanceof BasicEntry) {
        // exchange the line ending on BasicEntries
        final BasicEntry basicEntry= (BasicEntry) entry;
        final BasicEntry formattedEntry= new BasicEntry(
          basicEntry.toCharSequence().toString()
            .replaceAll("\\n", "")
            .replaceAll("\\r", "")
            .concat(propertyFormat.lineEnding.toString())
        );
        formattedEntries.add(formattedEntry);
      } else {
        throw new RuntimeException("Unexpected Entry type: "+entry.getClass());
      }
    }

    propertyFile.clear();
    propertyFile.setEntries(formattedEntries);
  }


  /**
   * Reorders the key-value pairs in the given .properties file alphabetically by the names
   * of its keys according to the default {@link de.poiu.apron.reformatting.ReformatOptions}.
   * <p>
   * This method actually changes the file on disk.
   *
   * @param fileToReorder the file whose key-value pairs to reorder
   */
  public void reorderByKey(final File fileToReorder) {
    this.reorderByKey(fileToReorder, this.reformatOptions);
  }


  /**
   * Reorders the key-value pairs in the given .properties file alphabetically by the names
   * of its keys according to the given ReformatOptions.
   * <p>
   * This method actually changes the file on disk.
   *
   * @param fileToReorder the file whose key-value pairs to reorder
   * @param reformatOptions the reformat options to use when reordering the key-value pairs in the file
   */
  public void reorderByKey(final File fileToReorder, final ReformatOptions reformatOptions) {
    Objects.requireNonNull(fileToReorder);
    Objects.requireNonNull(reformatOptions);

    final PropertyFile propertyFile= PropertyFile.from(fileToReorder, reformatOptions.getCharset());
    this.reorderByKey(propertyFile, reformatOptions);
    propertyFile.overwrite(fileToReorder);
  }


  /**
   * Reorders the Entries in the given PropertyFile alphabetically by the names
   * of its PropertyEntries keys according to the default {@link de.poiu.apron.reformatting.ReformatOptions}.
   * <p>
   * This method does not change any files on disk.
   *
   * @param fileToReorder the file whose Entries to reorder
   */
  public void reorderByKey(final PropertyFile fileToReorder) {
    this.reorderByKey(fileToReorder, this.reformatOptions);
  }


  /**
   * Reorders a PropertyFiles entries alphabetically by the names of its PropertyEntries keys
   * according to the given ReformatOptions.
   * <p>
   * This method does not change any files on disk.
   *
   * @param fileToReorder the file whose Entries to reorder
   * @param reformatOptions the reformat options to use when reordering the key-value pairs in the file
   */
  public void reorderByKey(final PropertyFile fileToReorder, final ReformatOptions reformatOptions) {
    Objects.requireNonNull(fileToReorder);
    Objects.requireNonNull(reformatOptions);

    final List<Entry> orderedEntries= new ArrayList<>();

    // sort the entries by propertyKey
    final OrderableEntryList orderableEntries= reformatOptions.getAttachCommentsTo().
      toOrderableEntries(fileToReorder.getAllEntries());
    reformatOptions.getAttachCommentsTo().sort(orderableEntries.getAll());

    //now add the sorted entries the ordered list
    orderableEntries.getAll()
      .forEach(_oe -> {
        orderedEntries.addAll(_oe.entries);
      });

    //now write the reordered entries back to the PropertyFile (not yet to disk)
    fileToReorder.getAllEntries().clear();
    fileToReorder.getAllEntries().addAll(orderedEntries);
  }


  /**
   * Reorders the key-value pairs in a .properties file according to the order of those keys in the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the file to reorder. Those entries are <code>not</code> reordered.
   * <p>
   * This method actually changes the fileToReorder on disk. The template file will not be modified.
   *
   * @param template the reference file to be used as template for the reordering
   * @param templateCharset the charset to use for loading the template file
   * @param fileToReorder the file whose key-value pairs to reorder according to the reference file
   * @param reformatOptions the reformat options to use when reordering the key-value pairs in the file
   */
  public void reorderByTemplate(final File template, final Charset templateCharset, final File fileToReorder, final ReformatOptions reformatOptions) {
    Objects.requireNonNull(template);
    Objects.requireNonNull(fileToReorder);
    Objects.requireNonNull(reformatOptions);

    final PropertyFile reference= PropertyFile.from(template, templateCharset);
    this.reorderByTemplate(reference, fileToReorder, reformatOptions);
  }


  /**
   * Reorders the key-value pairs in a .properties file according to the order of those keys in the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the file to reorder. Those entries are <code>not</code> reordered.
   * <p>
   * This method actually changes the fileToReorder on disk. The template file will not be modified.
   *
   * @param template the reference file to be used as template for the reordering
   * @param fileToReorder the file whose key-value pairs to reorder according to the reference file
   * @param reformatOptions the reformat options to use when reordering the key-value pairs in the file
   */
  public void reorderByTemplate(final File template, final File fileToReorder, final ReformatOptions reformatOptions) {
    Objects.requireNonNull(template);
    Objects.requireNonNull(fileToReorder);
    Objects.requireNonNull(reformatOptions);

    final PropertyFile reference= PropertyFile.from(template, reformatOptions.getCharset());
    this.reorderByTemplate(reference, fileToReorder, reformatOptions);
  }


  /**
   * Reorders the key-value pairs in a .properties file according to the order of those keys in the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the file to reorder. Those entries are <code>not</code> reordered.
   * <p>
   * The default {@link de.poiu.apron.reformatting.ReformatOptions} will be used when reordering the key-value pairs.
   * <p>
   * This method actually changes the fileToReorder on disk. The template file will not be modified.
   *
   * @param template the reference file to be used as template for the reordering
   * @param fileToReorder the file whose key-value pairs to reorder according to the reference file
   */
  public void reorderByTemplate(final File template, final File fileToReorder) {
    this.reorderByTemplate(template, fileToReorder, this.reformatOptions);
  }


  /**
   * Reorders the key-value pairs in a .properties file according to the order of those entries keys of the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the file to reorder. Those entries are <code>not</code> reordered.
   * <p>
   * The default {@link de.poiu.apron.reformatting.ReformatOptions} will be used when reordering the key-value pairs.
   * <p>
   * This method actually changes the fileToReorder on disk. The template file will not be modified.
   *
   * @param reference the reference file to be used as template for the reordering
   * @param fileToReorder the file whose key-value pairs to reorder according to the reference file
   */
  public void reorderByTemplate(final PropertyFile reference, final File fileToReorder) {
    this.reorderByTemplate(reference, fileToReorder, this.reformatOptions);
  }


  /**
   * Reorders the key-value pairs in a .properties file according to the order of those entries keys of the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the file to reorder. Those entries are <code>not</code> reordered.
   * <p>
   * This method actually changes the fileToReorder on disk. The template file will not be modified.
   *
   * @param reference the reference file to be used as template for the reordering
   * @param fileToReorder the file whose key-value pairs to reorder according to the reference file
   * @param reformatOptions the reformat options to use when reordering the key-value pairs in the file
   */
  public void reorderByTemplate(final PropertyFile reference, final File fileToReorder, final ReformatOptions reformatOptions) {
    Objects.requireNonNull(reference);
    Objects.requireNonNull(fileToReorder);
    Objects.requireNonNull(reformatOptions);

    final PropertyFile propertyFile= PropertyFile.from(fileToReorder, reformatOptions.getCharset());
    this.reorderByTemplate(reference, propertyFile, reformatOptions);
    propertyFile.overwrite(fileToReorder);
  }


  /**
   * Reorders a PropertyFiles entries according to the order of those entries keys of the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the PropertyFile. Those entries are <code>not</code> reordered.
   * <p>
   * The default {@link de.poiu.apron.reformatting.ReformatOptions} will be used when reordering the key-value pairs.
   * <p>
   * This method doesn't change any files on disk.
   *
   * @param reference the reference file to be used as template for the reordering
   * @param fileToReorder the file whose Entries to reorder according to the reference file
   */
  public void reorderByTemplate(final PropertyFile reference, final PropertyFile fileToReorder) {
    this.reorderByTemplate(reference, fileToReorder, this.reformatOptions);
  }


  /**
   * Reorders a PropertyFiles entries according to the order of those entries keys of the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the PropertyFile. Those entries are <code>not</code> reordered.
   * <p>
   * This method doesn't change any files on disk.
   *
   * @param reference the reference file to be used as template for the reordering
   * @param fileToReorder the file whose Entries to reorder according to the reference file
   * @param reformatOptions the reformat options to use when reordering the key-value pairs in the file
   */
  public void reorderByTemplate(final PropertyFile reference, final PropertyFile fileToReorder, final ReformatOptions reformatOptions) {
    Objects.requireNonNull(reference);
    Objects.requireNonNull(fileToReorder);
    Objects.requireNonNull(reformatOptions);

    final List<Entry> orderedEntries= new ArrayList<>();

    final OrderableEntryList orderableEntries= reformatOptions.getAttachCommentsTo()
      .toOrderableEntries(fileToReorder.getAllEntries());
    for (final Entry refEntry : reference.getAllEntries()) {
      // only process PropertyEntries
      if (refEntry instanceof PropertyEntry) {
        final PropertyEntry refPEntry= (PropertyEntry) refEntry;
        orderableEntries.pop(refPEntry.getKey())
          .ifPresent(_oe -> {
            orderedEntries.addAll(_oe.entries);
          });
      }
    }

    //now add the remaining entries that have no counterpart in the reference PropertyFile
    orderableEntries.getAll()
      .forEach(_oe -> {
        orderedEntries.addAll(_oe.entries);
      });

    //now write the reordered entries back to the PropertyFile (not yet to disk)
    fileToReorder.getAllEntries().clear();
    fileToReorder.getAllEntries().addAll(orderedEntries);
  }


  /**
   * Parses a format string into a PropertyFormat object.
   *
   * @param format the format string to parse
   * @return the PropertyFormat for the given format string
   * @throws InvalidFormatException if the given format string is not valid according to {@link #PATTERN_PROPERTY_FORMAT}.
   */
  private PropertyFormat parseFormat(final String format) {
    final Matcher matcher= PATTERN_PROPERTY_FORMAT.matcher(format);
    if (!matcher.matches()) {
      throw new InvalidFormatException("The format string is in an invalid format.\n"
        + "A usual format is \"<KEY> = <VALUE>\\n\"\n"
        + "Please refer to the documention for a more detailed explantion of the allowed format.\n"
        + "The given format was: " + format);
    }

    return new PropertyFormat(
      convertEscapes(matcher.group("LEADINGWHITESPACE")),
      convertEscapes(matcher.group("SEPARATOR")),
      convertEscapes(matcher.group("LINEENDING"))
    );
  }


  /**
   * Converts the literal escape sequences (of a format string) to their real instances.
   * @param s the string to convert
   * @return the given string with the literal escape sequences replaced by their real instances
   */
  private String convertEscapes(final String s) {
    return s
      .replace("\\t", "\t")
      .replace("\\f", "\f")
      .replace("\\r", "\r")
      .replace("\\n", "\n")
      ;
  }


  /**
   * Reformats the (escaped) key of a key-value pair by removing all unnecessary whitespace and newlines.
   * The result is also escaped.
   *
   * @param key the (escaped) key to reformat
   * @return the (escaped) reformatted key
   */
  protected CharSequence reformatKey(final CharSequence key) {
    return
      EscapeUtils.escapePropertyKey(
        EscapeUtils.unescape(key));
  }


  /**
   * Reformats the (escaped) value of a key-value pair by removing all unnecessary whitespace and newlines.
   * The result is also escaped.
   *
   * @param value the (escaped) value to reformat
   * @return the (escaped) reformatted value
   */
  protected CharSequence reformatValue(final CharSequence value) {
    return
      EscapeUtils.escapePropertyValue(
        EscapeUtils.unescape(value));
  }
}
