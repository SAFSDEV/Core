/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.selenium;

import java.util.*;

import org.safs.*;
import org.safs.tools.drivers.ConfigureInterface;

import com.thoughtworks.selenium.Selenium;

/**
 * Extends TestRecordHelper, which holds key data used by the LocalServer Selenium Engine.
 * This is different from the Serializable STestRecordData used on the Remote Agents 
 * of the Engine.  Based on the SAFS Test Record Data doc.
 *
 * @author  CANAGL, PHSABO
 * @since   AUG 15, 2006
 **/
public class STestRecordHelper extends TestRecordHelper {

  /** child's Object when it is appropriate. **/
  private SGuiObject compTestObject = null;

  /** window's Object when it is appropriate. **/
  private SGuiObject windowTestObject = null;
  
  /** 
   * No-arg constructor to make this fully qualified javabean
   **/
  public STestRecordHelper() {
    super();
  }
    
  /** 
   * accessor method
   */
  public SGuiObject getWindowTestObject () {return windowTestObject;}
  /** 
   * accessor method
   */
  public void setWindowTestObject (SGuiObject windowTestObject) {this.windowTestObject = windowTestObject;}

  /** 
   * accessor method
   */
  public SGuiObject getCompTestObject () {return compTestObject;}
  /** 
   * accessor method
   */
  public void setCompTestObject (SGuiObject compTestObject) {this.compTestObject = compTestObject;}

  /** 
   * reinit this object to be reused over again.
   * fields set to null
   **/
  public void reinit () {
    super.reinit();
    setCompTestObject(null);
    setWindowTestObject(null);
  }

  /**
   * "org.safs.selenium." per the needs of the abstract interface.
   * @see TestRecordHelper#getCompInstancePath()
   */
  public String getCompInstancePath(){ return "org.safs.selenium."; }  
  
}

