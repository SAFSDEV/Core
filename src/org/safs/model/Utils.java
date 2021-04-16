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
package org.safs.model;

/**
 * Various utility functions used by other classes.
 */
public class Utils {

   public static final String TRUE_VALUE = "True"/*I18nOK:EMS*/;
   public static final String FALSE_VALUE = "False"/*I18nOK:EMS*/;
   
   public static final String EMPTY_STRING = ""/*I18nOK:EMS*/;
   
   private static final String MENU_PATH_SEPARATOR = "->"/*I18nOK:EMS*/;
   
	/**
	 * Wrap the provided val String in double-quotes.
	 * @param val String to wrap in double-quotes
	 * @return val wrapped in quotes or a double-quoted empty string if 
	 * val was null.
	 */
	static public String quote(String val){
		if (val == null) return "\"\"";
		return "\""+ val +"\"";
	}
    
    /**
     * Returns a SAFS concatentation of the specified strings using the concat operator ('&').
     * <p> 
     * For example, the result of calling <code>concat</code> with "^newName" and
     * "^currentDate" is "^newName&^currentDate".
     * <p>
     * @param string1 the first string to concatentate
     * @param string2 the second string to concatenate
     * @return <code>string1</code> and <code>string2</code> in a SAFS concatentation.
     */
    public static String concat(String string1, String string2) {
       return string1 + "&" + string2;
    }
    
    /**
     * Convenience for building a menu path where each path element is separated by "->".
     * <p>
     * This is an alias for:
     * <pre>
     *    return Utils.getMenuPath(new String[] {path1, path2});
     * </pre>
     * <p>
     * @param path1 the first path element
     * @param path2 the second path element
     * @return a quoted menu path for the specified path elements
     * @see #getMenuPath(String[])
     */
    public static final String getMenuPath(String path1, String path2) {
       return Utils.getMenuPath(new String[] {path1, path2});
    }
    
    /**
     * Convenience for building a menu path where each path element is separated by "->".
     * <p>
     * This is an alias for:
     * <pre>
     *    return Utils.getMenuPath(new String[] {path1, path2, path3});
     * </pre>
     * <p>
     * @param path1 the first path element
     * @param path2 the second path element
     * @param path3 the third path element
     * @return a quoted menu path for the specified path elements
     * @see #getMenuPath(String[])
     */
    public static final String getMenuPath(String path1, String path2, String path3) {
       return Utils.getMenuPath(new String[] {path1, path2, path3});
    }
    
    /**
     * Convenience for building a menu path where each path element is separated by "->".
     * <p>
     * @param path the path elements for the menu path
     * @return a quoted menu path for the specified path elements
     */
    public static final String getMenuPath(String[] path) {
       if (path == null || path.length == 0)
          throw new IllegalArgumentException("The path must be non-null and have a length of at least 1."/*I18nOK:EMS*/);
       StringBuffer buffer = new StringBuffer();
       for (int i=0, cnt=path.length; i<cnt; i++) {
          if (path[i] == null || path[i].length() == 0)
             throw new IllegalArgumentException("A path value is either null or empty."/*I18nOK:EMS*/);
          buffer.append(path[i]);
          if (i + 1 < cnt)
             buffer.append(MENU_PATH_SEPARATOR);
       }
       // Quote the path...
       buffer.insert(0, "\""/*I18nOK:EMS*/);
       buffer.append("\""/*I18nOK:EMS*/);
       return buffer.toString();
    }
}
