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


/**
 * A enum indicating what to do if a key-value-pair was found in the file a {@link de.poiu.apron.PropertyFile}
 * is written to, but not in the written PropertyFile.
 * <p>
 * This is only relevant when updating existing files.
 *
 * @author mherrn
 */
public enum MissingKeyAction {
  /** Do nothing and leave the existing key-value-pair as it is (the default). */
  NOTHING,
  /** Delete the key-value-pair from the file. */
  DELETE,
  /** Comment the lines of the key-value-pair out. */
  COMMENT,
  ;
}
