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
 * How to behave when the file a {@link PropertyFile} is written to already exists.
 *
 * @author mherrn
 */
public enum WhenExisting {
  /**
   * Update the existing value, but retain the original formatting.
   * <p>
   * When doing such an update the {@link MissingKeyAction} is relevant, too.
   */
  UPDATE,
  /** Overwrite the existing file with the contents of the written PropertyFile. */
  OVERWRITE,
  ;
}
