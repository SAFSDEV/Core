/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
 * @author  CANAGL
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

