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
package org.safs.text;

import org.safs.*;
import java.util.*;

/**
 * Constants for string resource IDs in failedSAFSTextResourceBundle<br>
 * Static access to a single instance of this ResourceBundle.
 */
public final class FAILStrings extends FAILKEYS{

  /**
   * Our specific RESOURCE_BUNDLE instance
   */
  private static final GetText strings = new GetText(RESOURCE_BUNDLE, Locale.getDefault());   


  /** Pass-thru to GetText.text **/
  public static String text (String resourceKey, String alternateText)  {  	
  	return strings.text(resourceKey, alternateText);
  }

  /** Pass-thru to GetText.text **/
  public static String text (String resourceKey) throws MissingResourceException {
  	return strings.text(resourceKey);
  }

  /** Pass-thru to GetText.translate **/
  public static String translate (String resourceKey) {
  	return strings.translate(resourceKey);
  }

  /** Pass-thru to GetText.convert **/
  public static String convert (String key, String alttext, Collection params) {
  	return strings.convert(key, alttext, params);
  }

  /** Pass-thru to GetText.convert **/
  public static String convert (String key, String alttext, String p1) {
  	return strings.convert(key, alttext, p1);
  }

  /** Pass-thru to GetText.convert **/
  public static String convert (String key, String alttext, String p1, String p2) {
  	return strings.convert(key, alttext, p1, p2);
  }

  /** Pass-thru to GetText.convert **/
  public static String convert (String key, String alttext, String p1, String p2, String p3) {
  	return strings.convert(key, alttext, p1, p2, p3);
  }

  /** Pass-thru to GetText.convert **/
  public static String convert (String key, String alttext, String p1, String p2, String p3, String p4) {
  	return strings.convert(key, alttext, p1, p2, p3, p4);
  }

  /** Pass-thru to GetText.convert **/
  public static String convert (String key, String alttext, String p1, String p2, String p3, String p4, String p5) {
  	return strings.convert(key, alttext, p1, p2, p3, p4, p5);
  } 
}
