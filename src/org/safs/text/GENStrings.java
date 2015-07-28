package org.safs.text;

import org.safs.*;
import java.util.*;

/**
 * Constants for string resource IDs in SAFSTextResourceBundle<br>
 * Static access to a single instance of this ResourceBundle.
 */
public final class GENStrings extends GENKEYS{
	
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