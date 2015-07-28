/** Copyright (C) SAS Institute. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.android.auto.lib;

/**
 * <br><em>Purpose:</em> our user defined application exception used with this package.
 * <p>
 * @author  Carl Nagle
 *
 *   <br>   MAR 27, 2012    (CANAGL) Original Release
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
