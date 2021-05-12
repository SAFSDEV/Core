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
package org.safs.rmi.engine;

import java.io.*;
import java.util.*;

import org.safs.*;

/**
 * Extends TestRecordData, which holds key data used by remote agents.
 * This is different from TestRecordHelper used on the LocalServer side of engines.
 * <P>
 * Must be Serializable for RMI transmission.
 * Based on the SAFS Test Record Data doc.
 *
 * @author  Carl Nagle
 * @since   MAR 02, 2006
 **/
public class RMITestRecordData extends TestRecordData implements Serializable {

  /** 
   * child pseudo-Object when it is appropriate.  With Abbot this is an Integer() 
   * component ID or sometimes an AgentWindow.  Other engines may store something 
   * different or may override for stricter type checking.
   * <P>
   * Must be Serializable for RMI transmission.
   */
  private Object compObject = null;

  /** 
   * window pseudo-Object when it is appropriate.  With Abbot this is an Integer() 
   * component ID or sometimes an AgentWindow.  Other engines may store something 
   * different or may override for stricter type checking.
   * <P>
   * Must be Serializable for Abbot RMI transmission.
   */
  private Object windowObject = null;
  
  /** 
   * No-arg constructor to make this fully qualified javabean
   **/
  public RMITestRecordData() {
    super();
  }

  /** 
   * Constructor presets windowObject and compObject.
   * @see #setWindowObject(Object)
   * @see #setCompObject(Object)
   **/
  public RMITestRecordData(Object win, Object comp) {
    super();
    setWindowObject(win);
    setCompObject(comp);
  }

  /** 
   * Constructor presets windowObject and compObject.
   **/
  public RMITestRecordData(TestRecordHelper trd) {
    super();
    trd.copyData(this);
  }

  /** 
   * Constructor presets windowObject and compObject.
   **/
  public RMITestRecordData(TestRecordHelper trd, Object win, Object comp) {
    super();
    trd.copyData(this);
    setWindowObject(win);
    setCompObject(comp);
  }

  /** 
   * windowObject Accessor method
   **/
  public Object getWindowObject () {return windowObject;}
  /** 
   * windowObject Accessor method
   **/
  public void setWindowObject (Object win) {windowObject = win;}


  /** 
   * compObject Accessor method
   **/
  public Object getCompObject () {return compObject;}
  /** 
   * compObject Accessor method
   **/
  public void setCompObject (Object comp) {compObject = comp;}


  /** 
   * Reinit/Reset this object to be used again.  This is generally done by the 
   * active Processors and drivers.
   **/
  public void reinit () {
    super.reinit();
    setCompObject(null);
    setWindowObject(null);
  }
}

