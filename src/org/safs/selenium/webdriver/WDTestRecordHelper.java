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
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.safs.TestRecordHelper;

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

