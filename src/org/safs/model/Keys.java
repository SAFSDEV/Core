/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;

/**
 * Defines constants for common keys and utilities for specifying repeated keys.
 */
public final class Keys {

   /**
    * A single ENTER keystroke.  For multiple keystrokes see {@link #enter(int)}.
    */
   public static final String ENTER    = "{ENTER}"/*I18nOK:EMS*/;
   
   /**
    * A single ESCAPE keystroke.  For multiple keystrokes see {@link #escape(int)}.
    */
   public static final String ESCAPE   = "{ESCAPE}"/*I18nOK:EMS*/;
   
   /**
    * A single TAB keystroke.  For multiple keystrokes see {@link #tab(int)}.
    */
   public static final String TAB      = "{TAB}"/*I18nOK:EMS*/;
   
   /**
    * A single DELETE keystroke.  For multiple keystrokes see {@link #delete(int)}.
    */
   public static final String DELETE   = "{DELETE}"/*I18nOK:EMS*/;
   
   /**
    * A single F1 keystroke.  
    */
   public static final String F1       = "{F1}"/*I18nOK:EMS*/;
   
   /**
    * A single F2 keystroke.  
    */
   public static final String F2       = "{F2}"/*I18nOK:EMS*/;
   
   /**
    * A single F3 keystroke.  
    */
   public static final String F3       = "{F3}"/*I18nOK:EMS*/;
   
   /**
    * A single F4 keystroke.  
    */
   public static final String F4       = "{F4}"/*I18nOK:EMS*/;
   
   /**
    * A single F5 keystroke.  
    */
   public static final String F5       = "{F5}"/*I18nOK:EMS*/;
   
   /**
    * A single F6 keystroke.  
    */
   public static final String F6       = "{F6}"/*I18nOK:EMS*/;
   
  /**
   * A single F7 keystroke.  
   */
  public static final String F7        = "{F7}"/*I18nOK:EMS*/;
  
  /**
   * A single F8 keystroke.  
   */
  public static final String F8        = "{F8}"/*I18nOK:EMS*/;
  
  /**
   * A single F9 keystroke.  
   */
  public static final String F9        = "{F9}"/*I18nOK:EMS*/;
  
  /**
   * A single F10 keystroke.  
   */
  public static final String F10       = "{F10}"/*I18nOK:EMS*/;
  
  /**
   * A single F11 keystroke.  
   */
  public static final String F11       = "{F11}"/*I18nOK:EMS*/;
  
  /**
   * A single F12 keystroke.  
   */
  public static final String F12       = "{F12}"/*I18nOK:EMS*/;
  
   /**
    * Returns a string indicating that the ALT key should be pressed for each character in <code>text</code>.
    * <p>
    * @param text the characters to press the ALT key for 
    * @return the specified text prefixed with "%"
    */
   public static final String alt(String text) {
      return "%" + text; /*I18nOK:LINE*/
   }
   
   /**
    * Returns a string indicating that the CTRL key should be pressed for each character in <code>text</code>.
    * <p>
    * @param text the characters to press the CTRL key for 
    * @return the specified text prefixed with "^"
    */
   public static final String ctrl(String text) {
      return "^" + text;/*I18nOK:LINE*/
   }
   
   /**
    * Returns a string indicating that the SHIFT key should be pressed for each character in <code>text</code>.
    * <p>
    * @param text the characters to press the SHIFT key for 
    * @return the specified text prefixed with "+"
    */
   public static final String shift(String text) {
      return "+" + text; /*I18nOK:LINE*/
   }
   
   /**
    * Repeats the ENTER key "count" times.
    * <p> 
    * @param count the number of times the ENTER key should be pressed. 
    * @return a string representing the ENTER key pressed "count" times
    * @throws IllegalArgumentException if count is 0
    */
   public static final String enter(int count) {
      if (count == 1)
         return ENTER;
      return repeat("ENTER"/*I18nOK:EMS*/, count);
   }
   
   /**
    * Repeats the ESCAPE key "count" times.
    * <p> 
    * @param count the number of times the ESCAPE key should be pressed. 
    * @return a string representing the ESCAPE key pressed "count" times
    * @throws IllegalArgumentException if count is 0
    */
   public static final String escape(int count) {
      if (count == 1)
         return ESCAPE;
      return repeat("ESCAPE"/*I18nOK:EMS*/, count);
   }
   
   /**
    * Repeats the TAB key "count" times.
    * <p> 
    * @param count the number of times the TAB key should be pressed. 
    * @return a string representing the TAB key pressed "count" times
    * @throws IllegalArgumentException if count is 0
    */
   public static final String tab(int count) {
      if (count == 1)
         return TAB;
      return repeat("TAB"/*I18nOK:EMS*/, count);
   }
   
   /**
    * Repeats the DELETE key "count" times.
    * <p> 
    * @param count the number of times the DELETE key should be pressed. 
    * @return a string representing the DELETE key pressed "count" times
    * @throws IllegalArgumentException if count is 0
    */
   public static final String delete(int count) {
      if (count == 1)
         return DELETE;
      return repeat("DELETE"/*I18nOK:EMS*/, count);
   }
   
   /**
    * Repeats the specified key "count" times.
    * <p>
    * @param key the key to repeat 
    * @param count the number of times the "key" should be pressed. 
    * @return a string representing the specified key pressed "count" times
    * @throws IllegalArgumentException if count is 0
    */
   public static final String repeat(char key, int count) {
      return repeat(String.valueOf(key), count);
   }
   
   private static final String repeat(String key, int count) {
      if (count == 0)
         throw new IllegalArgumentException("Count must be 1 or more."/*I18nOK:EMS*/);
      return "{" + key + " " + String.valueOf(count) + "}";/*I18nOK:LINE*/
   }
}
