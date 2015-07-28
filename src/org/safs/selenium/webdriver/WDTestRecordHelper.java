/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.selenium.webdriver;

import java.util.*;

import org.openqa.selenium.WebElement;
import org.safs.*;
import org.safs.tools.drivers.ConfigureInterface;

import com.thoughtworks.selenium.Selenium;

/**
 * Extends TestRecordHelper, which holds key data used by the LocalServer Selenium Engine.
 * This is different from the Serializable STestRecordData used on the Remote Agents 
 * of the Engine.  Based on the SAFS Test Record Data doc.
 *
 * @author  Carl Nagle, PHSABO
 * @since   AUG 15, 2006
 **/
public class WDTestRecordHelper extends TestRecordHelper {

  /** child's Object when it is appropriate. **/
  private WebElement compTestObject = null;

  /** window's Object when it is appropriate. **/
  private WebElement windowTestObject = null;
  
  /** 
   * No-arg constructor to make this fully qualified javabean
   **/
  public WDTestRecordHelper() {
    super();
  }
    
  /** 
   * accessor method
   */
  public WebElement getWindowTestObject () {return windowTestObject;}
  /** 
   * accessor method
   */
  public void setWindowTestObject (WebElement windowTestObject) {this.windowTestObject = windowTestObject;}

  /** 
   * accessor method
   */
  public WebElement getCompTestObject () {return compTestObject;}
  /** 
   * accessor method
   */
  public void setCompTestObject (WebElement compTestObject) {this.compTestObject = compTestObject;}

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
   * A method to return a default class package prefix used by some processors.
   **/
  public String getCompInstancePath(){return "org.safs.selenium.webdriver.";}
  
  
}

