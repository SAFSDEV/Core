/**
    W3C Base64 Codec: http://www.w3.org/Jigsaw/ 
<p>
    Copyright © 2002 World Wide Web Consortium, (Massachusetts Institute of Technology, 
    European Research Consortium for Informatics and Mathematics, Keio University). 
    All Rights Reserved. This work is distributed under the W3C® Software License [1] 
    in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
    implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
<p>
    [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231.html
 */
package org.w3c.tools.codec ;

/**
 * Exception for invalid BASE64 streams.
 */

public class Base64FormatException extends Exception {

  /**
   * Create that kind of exception
   * @param msg The associated error message 
   */

  public Base64FormatException(String msg) {
	super(msg) ;
  }

}
