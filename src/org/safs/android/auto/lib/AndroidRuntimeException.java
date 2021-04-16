/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.android.auto.lib;

/**
 * <br><em>Purpose:</em> our user defined application exception used with this package.
 * <p>
 * @author  Carl Nagle
 *
 *   <br>   MAR 27, 2012    (Carl Nagle) Original Release
 **/
public class AndroidRuntimeException extends RuntimeException {

  /** <br><em>Purpose:</em> constructor
   * @param                     msg, String, the string to pass along to our 'super'
   **/
  public AndroidRuntimeException (String msg) {
    super(msg);
  }

  /** <br><em>Purpose:</em> constructor
   * @param  msg, String, the string to pass along to our 'super'
   * @param  cause, Throwable 'cause' to pass along to our 'super'.
   **/
  public AndroidRuntimeException (Throwable cause) {
    super(cause);
  }

  /** <br><em>Purpose:</em> constructor
   * @param  msg, String, the string to pass along to our 'super'
   * @param  cause, Throwable 'cause' to pass along to our 'super'.
   **/
  public AndroidRuntimeException (String msg, Throwable cause) {
    super(msg, cause);
  }

  /** <br><em>Purpose:</em> constructor
   * @param  cause, Throwable 'cause' to pass along to our 'super'.
   * @param  msg, String, the string to pass along to our 'super'
   **/
  public AndroidRuntimeException ( Throwable cause, String msg) {
    super(msg, cause);
  }
}
