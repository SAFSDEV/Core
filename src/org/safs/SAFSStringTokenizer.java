/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
package org.safs;

import java.util.*;

/**
 ** <br>All Implemented Interfaces: 
 ** Enumeration 
 ** <p>
 ** --------------------------------------------------------------------------------
 ** <p>
 ** Note, this version extends StringTokenizer, and changes it's behavior.
 ** In this version, if two delimiters come directly next to each other, then
 ** a blank token is inserted between them.  this seems to be the more appropriate
 ** implied action, as in: <br>
 ** X,Y,,Z<br>
 ** the implied output would be 4 tokens:  (X), (Y), (), and (Z)
 ** <p>
 ** --------------------------------------------------------------------------------
 ** <p>
 ** The string tokenizer class allows an application to break a string into tokens. The
 ** tokenization method is much simpler than the one used by the StreamTokenizer class.
 ** The StringTokenizer methods do not distinguish among identifiers, numbers, and quoted
 ** strings, nor do they recognize and skip comments. 
 ** <p>
 ** The set of delimiters (the characters that separate tokens) may be specified either at
 ** creation time or on a per-token basis. 
 ** <p>
 ** An instance of StringTokenizer behaves in one of two ways, depending on whether it was
 ** created with the returnDelims flag having the value true or false: 
 ** <p>
 ** If the flag is false, delimiter characters serve to separate tokens. A token is a maximal
 ** sequence of consecutive characters that are not delimiters.
 ** <p>
 ** If the flag is true, delimiter characters are themselves considered to be tokens. A token
 ** is thus either one delimiter character, or a maximal sequence of consecutive characters
 ** that are not delimiters.
 ** <p>
 ** A StringTokenizer object internally maintains a current position within the string to be
 ** tokenized. Some operations advance this current position past the characters processed.
 **
 ** A token is returned by taking a substring of the string that was used to create the
 ** StringTokenizer object. 
 ** <p>
 ** The following is one example of the use of the tokenizer. The code: 
 ** <p><code>
 **    StringTokenizer st = new SAFSStringTokenizer("this,,is,a,test",",");
 **    while (st.hasMoreTokens()) {
 **        println(st.nextToken());
 **    }
 ** </code><p>
 ** prints the following output: 
 ** <p><code>
 **  <br>this
 **  <br>
 **  <br>is
 **  <br>a
 **  <br>test
 ** </code>
 * @author  Doug Bauman
 * @since   JUN 24, 2003
 *   <br>   JUN 24, 2003    (DBauman) Original Release
 **/

public class SAFSStringTokenizer extends StringTokenizer  {
  private boolean returnDelims = false;
  private String delim = " \t\n\r\f";
  private String str;
  private ArrayList tokens = new ArrayList();
  private int tindex = 0;
  /** <br><em>Purpose:</em> Constructs a string tokenizer for the specified string.
   ** The tokenizer uses the default delimiter set, which is " \t\n\r\f": the space
   ** character, the tab character, the newline character, the carriage-return character,
   ** and the form-feed character.
   ** Delimiter characters themselves will not be treated as tokens, but if two delimiters
   ** are next to each other, then a blank token is provided (unlike our parent).
   * @param                     str a string to be parsed
   **/
  public SAFSStringTokenizer (String str) {
    super(str);
    this.str = str;
    tokenize();
  }
  /** <br><em>Purpose:</em> Constructs a string tokenizer for the specified string.
   ** The characters in the delim argument are the delimiters for separating tokens.
   ** Delimiter characters themselves will not be treated as tokens, but if two delimiters
   ** are next to each other, then a blank token is provided (unlike our parent).
   * @param                     str a string to be parsed
   **/
  public SAFSStringTokenizer (String str, String delim) {
    super(str, delim);
    this.str = str;
    this.delim = delim;
    tokenize();
  }
  /** <br><em>Purpose:</em> Constructs a string tokenizer for the specified string.
   ** The characters in the delim argument are the delimiters for separating tokens.
   ** Delimiter characters themselves will not be treated as tokens, but if two delimiters
   ** are next to each other, then a blank token is provided (unlike our parent).
   * @param                     str a string to be parsed
   * @param                     delim the delimeters
   * @param                     returnDelims flag indicating whether to return the delimiters as tokens.

   **/
  public SAFSStringTokenizer (String str, String delim, boolean returnDelims) {
    super(str, delim, returnDelims);
    this.str = str;
    this.delim = delim;
    this.returnDelims = returnDelims;
    tokenize();
  }

  /** <br><em>Purpose:</em> do the work of tokenizing the string
   **/
  private void tokenize() {
    String tstr = str;
    for(;;) {
      // find next delim (least index)
      int least = tstr.length();
      String keepd = null;
      for(int i=0; i<delim.length(); i++) {
        String nd = delim.substring(i, i+1);
        int j = tstr.indexOf(nd);
        if (j >=0 && j < least) {
          least = j;
          keepd = nd;
        }
      }
      if (keepd != null) {
        tokens.add(tstr.substring(0, least));
        if (returnDelims) tokens.add(keepd);
        tstr = tstr.substring(least+1, tstr.length());
      } else {
        tokens.add(tstr);
        break;
      }
    }
  }

  /** <br><em>Purpose:</em>  Calculates the number of times that this tokenizer's
   ** nextToken method can be called before it generates an exception. 
   * @return                    int
   **/
  public int countTokens() {
    return tokens.size() - tindex;
  }
  /** <br><em>Purpose:</em> Returns the same value as the hasMoreTokens method. 
   * @return                    boolean, true if more, false if not
   **/
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }
  /** <br><em>Purpose:</em> Returns the same value as the nextToken method,
   ** except that its declared return value is Object rather than String. 
   ** It exists so that this class can implement the Enumeration interface.
   * @return                    Object
   * @exception NoSuchElementException - if there are no more tokens in this tokenizer's string.
   **/
  public Object nextElement() throws NoSuchElementException {
    return nextToken();
  }

  /** <br><em>Purpose:</em> Tests if there are more tokens available from this tokenizer's string. 
   * @return                    boolean, true if more, false if not
   **/
  public boolean hasMoreTokens() {
    try {
      tokens.get(tindex);
      return true;
    } catch (IndexOutOfBoundsException ii) {
      return false;
    }
  }
  /** <br><em>Purpose:</em> Returns the next token from this string tokenizer. 
   ** except that its declared return value is Object rather than String. 
   * @return                    Object
   * @exception NoSuchElementException - if there are no more tokens in this tokenizer's string.
   **/
  public String nextToken() throws NoSuchElementException {
    try {
      String result = (String) tokens.get(tindex);
      tindex++;
      return result;
    } catch (IndexOutOfBoundsException ii) {
      throw new NoSuchElementException("index: "+tindex);
    }
  }

  /** <br><em>Purpose:</em> Returns the next token in this string tokenizer's string.
   ** Currently not implemented
   * @param                     delim, String
   * @return                    none
   **/
  public String nextToken(String delim) {
    throw new RuntimeException("Not implemented");
  }

  public static void main(String[] args) {
    System.out.println("new SAFSStringTokenizer(\"XXYYZ\",\",\");");
    SAFSStringTokenizer st = new SAFSStringTokenizer("XXYYZ",",");
    while (st.hasMoreTokens()) {
      System.out.print("("+st.nextToken()+")");
    }
    System.out.println("\nnew SAFSStringTokenizer(\"XX  YY		Z\");");
    st = new SAFSStringTokenizer("XX  YY		Z");
    while (st.hasMoreTokens()) {
      System.out.print("("+st.nextToken()+")");
    }
    System.out.println("\nnew SAFSStringTokenizer(\"XX,YY,,Z,,\",\",\");");
    st = new SAFSStringTokenizer("XX,YY,,Z,,",",");
    while (st.hasMoreTokens()) {
      System.out.print("("+st.nextToken()+")");
    }
    System.out.println("\nnew SAFSStringTokenizer(\"XX,YY,,Z,,\",\",\", true);");
    st = new SAFSStringTokenizer("XX,YY,,Z,,",",", true);
    while (st.hasMoreTokens()) {
      System.out.print("("+st.nextToken()+")");
    }
    System.out.println("\nnew SAFSStringTokenizer(\"XX|YY,|Z|,\",\",|\");");
    st = new SAFSStringTokenizer("XX|YY,|Z|,",",|");
    while (st.hasMoreTokens()) {
      System.out.print("["+st.countTokens()+"]("+st.nextToken()+")");
    }
    System.out.println("["+st.countTokens()+"]");
    try {
      st.nextToken();
      System.out.println("Assert that this message should never be reached!");
    } catch (NoSuchElementException ne) {
      System.out.println("Assert that this message should be reached: "+ne.getMessage());
    }
  }
}

