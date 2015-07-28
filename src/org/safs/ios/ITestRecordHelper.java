/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.ios;

import org.safs.*;

/**
 * Extends TestRecordHelper, which holds key data used by the Apple IOS Engine.<br>
 * Based on the SAFS Test Record Data doc.
 *
 * @author  Carl Nagle
 * @since   JUN 30, 2011
 **/
public class ITestRecordHelper extends TestRecordHelper {

   /** 
   * No-arg constructor to make this fully qualified javabean
   **/
  public ITestRecordHelper() {
    super();
  }
    
  /**
   * "org.safs.ios." per the needs of the abstract interface.
   * @see TestRecordHelper#getCompInstancePath()
   */
  public String getCompInstancePath(){ return "org.safs.ios."; }  
  
}

