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


/**
 * Indicates that the given PropertyFormat has an invalid format.
 *
 * @author mherrn
 * @since 2.0.0
 */
public class InvalidFormatException extends RuntimeException {

  /**
   * @see java.lang.RuntimeException#RuntimeException()
   */
  public InvalidFormatException() {
  }


  /**
   * @see java.lang.RuntimeException#RuntimeException(java.lang.String)
   */
  public InvalidFormatException(String message) {
    super(message);
  }


  /**
   * @see java.lang.RuntimeException#RuntimeException(java.lang.String, java.lang.Throwable)
   */
  public InvalidFormatException(String message, Throwable cause) {
    super(message, cause);
  }


  /**
   * @see java.lang.RuntimeException#RuntimeException(java.lang.Throwable)
   */
  public InvalidFormatException(Throwable cause) {
    super(cause);
  }


  /**
   * @see java.lang.RuntimeException#RuntimeException(java.lang.String, java.lang.Throwable, boolean, boolean)
   */
  public InvalidFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
