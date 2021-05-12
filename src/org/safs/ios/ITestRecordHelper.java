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

