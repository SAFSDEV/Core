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

import java.io.*;
import java.util.*;

/**
 * <br><em>Purpose:</em> GetText, get text from a resource bundle
 * <br><em>Lifetime:</em> must be instantiated before it can be used
 * <p>
 * NOTE, the documentation below are thoughts, and are not in-totale implemented here.
 * <p>
 * We are discussing a new STAF service (initially a Java Class that can then point to a service) to provide string resources.  The intent is multi-purpose.  The initial driving force was to have a service that would provide consistent log messages regardless of which engine was in use.  In other words, all the engines would get the text to be logged from the service so that their messages would be the same.  Right now, RobotJ and Robot Classic messages for the same item are very different.  Future engines could just inherit the message pool we will have in place via this service.
 * </p><p>
 * This will also provide the opportunity for engine messages in different languages, and to provide localized string support for NLS testing of the target application.  Test data like string benchmark values for Caption or visible text could be stored in different files based on locale.
 * </p><p>
 * Some thoughts:
 * </p><p>
 * 1) It is important to remember that our logging API takes both a message and a detailed description.  (It still does, right?)  So all of this detailed failure info need not be in the "message" part of the logged message.  Indeed, as we should see by reviewing much of the Robot Classic logging, the details of which table, which line, and sometimes even the inputrecord string itself are often logged in the detailed description, and not in the main message itself.
 * </p><p>
 * 2) The "failure" and "passed" messages are not to be handled differently, or by some type of different code.  We are just requesting a named string that may (or may not) take parameters.  
 * <br>
 *     MyMessage :A message taking %1% parameters for reasons of %2% and %3%.
 * </p><p>
 * 3) Parameters should be reusable in the same message and do not have to be used in passed order.
 * <br>
 *    MyMsg  :A %3% message taking %1% parameters for reasons of %3%, but not %2%.
 * </p><p>
 * 4) Each message independently determines how many arguments it accepts.  What do we do when insufficient arguments are provided?  "<unknown>" ?
 * <br>
 *    Output=A <unknown> message taking 3 parameters for reasons of <unknown>, but not insanity.
 * </p><p>
 * 5) What do we do when additional arguments are provided beyond those needed by the message?  Append to the end of the message?
 * </p><p>
 * 6) The Java Class API must be allowed to accept an optional LOCALE parameter for the call.  LOCALE parameter specifications and derived filename specifications should conform to the industry standard (en, fr, de,  etc..)  Without the optional parameter, the current (or preset?) LOCALE should be used by default.
 * </p><p>
 * 7) "failure" messages (or their description messages) should try to include the test table filename in addition to the line number in that test table that failed.  The "line number" in the example is not for the line number in the CF library, but for the test table that is being executed.
 * <br>
 *    Output="...failed at line 6 in <path to>\LoginUser.sdd"  (we may choose to not output <path to> info.)
 * </p><p>
 * 8) The Java Class API needs to be able to handle multiple named file sources.  We need to be able to request strings not only for the log messages, but for the test data, or any number of other sources.  Much like we are able to open multiple App Maps.
 * </p><p>
 * 9) Like SAFSMAPS and SAFSVARS, we need to be able to programmatically clear any stored cache.  We don't want to have to shutdown the service(Class), STAF, or RobotJ to force newly edited content to be loaded.
 * </p>
 * @author  Doug Bauman
 * @since   NOV 05, 2003
 *
 *   <br>   NOV 06, 2003    (DBauman) Original Release
 *   <br>   NOV 08, 2003    (Carl Nagle) Removed thrown exceptions
 *   <br>   NOV 09, 2003    (Carl Nagle) Fixed problem with altext duplicated in return when
 *                                   MissingResource encountered.  Other Exception code, also.
 **/
public class GetText implements Serializable {

  /** no-arg constructor for javabeans **/
  public GetText () {
  }

  /** <br><em>Purpose:</em> constructor; assigns the prefixBundleName and the locale
   ** and loads the resource.
   * @param                     prefixBundleName, String
   **/
  public GetText (String prefixBundleName, Locale locale) {
    setPrefixBundleName(prefixBundleName);
    setLocale(locale);
  }

  /** delimter (currently %) used to 'convert' text with parameters as in %1% or %2% **/
  String delim = "%";

  /** the locale used to determine the name of the bundle
   **/
  private Locale locale = Locale.getDefault();
  public Locale getLocale () {return locale;}
  public void setLocale (Locale locale) {
    this.locale = locale;
    setBundleName();
  }
  /**
   ** bundle name prefix <br>
   ** default value is: SAFSTextResourceBundle
   **/
  private String prefixBundleName = "SAFSTextResourceBundle";
  public String getPrefixBundleName () {return prefixBundleName;}
  public void setPrefixBundleName (String prefixBundleName) {
    this.prefixBundleName = prefixBundleName;
    setBundleName();
  }
  /**
   ** The fully qualified bundle name, including: <br>
   ** prefixBundleName + "_" + Locale.getDefault(); <br>
   ** example: SAFSTextResourceBundle_en_US
   ** <br> NOTE: the ".properties" is assumed by the 'ResourceBundle.getBundle' method
   **/
  private String bundleName = prefixBundleName + "_" + Locale.getDefault().toString();
  public String getBundleName () {return bundleName;}
  /** set bundle name using prefixBundleName and specified Locale **/
  protected void setBundleName () {
    this.bundleName = prefixBundleName + "_" + locale.toString();
  }

  /** this field is loaded by the 'loadResource' method
   ** using the 'bundleName', Locale.getDefault() and
   ** ClassLoader.getSystemClassLoader()
   **/
  protected ResourceBundle textResources = null;

  /** <br><em>Purpose:</em> load the resource defined in 'bundleName'
   ** <br><em>Side Effects:</em> 'textResources'
   ** <br><em>State Read:</em>   'bundleName'
   ** <br> NOTE: the ".properties" is assumed by the 'ResourceBundle.getBundle' method
   **/
  public void loadResource () throws MissingResourceException {
    try{ 
    	textResources = ResourceBundle.getBundle(bundleName,
                               					locale,
                               					ClassLoader.getSystemClassLoader());
    	Log.info("GetText loading "+ bundleName);
    }catch(MissingResourceException mr){
    	Log.info("GetText retrying load of "+ bundleName);
    	try{ 
    		textResources = ResourceBundle.getBundle(bundleName,
					locale,
					Thread.currentThread().getContextClassLoader());
        	Log.info("GetText loading "+ bundleName);
    	}
    	catch(MissingResourceException mr2){
        	Log.info("GetText failed to load "+ bundleName);
    		throw mr2;
    	}
    }
  }

  /** <br><em>Purpose:</em> get the text from the resource bundle, if not found, then
   ** return the 'alternateText'
   * <br><em>State Read:</em>   textResources.getString(resourceKey)
   * <br><em>Assumptions:</em>  resource bundle is lazily loaded if not already loaded
   * @param                     resourceKey, String
   * @param                     alternateText, String
   * @return                    String
   **/
  public String text (String resourceKey, String alternateText)  {
    try {
      return text(resourceKey);
    } catch (MissingResourceException mre) {
      return alternateText;
    }
  }

  /** <br><em>Purpose:</em> get the text from the resource bundle
   * <br><em>State Read:</em>   textResources.getString(resourceKey)
   * <br><em>Assumptions:</em>  resource bundle is lazily loaded if not already loaded
   * @param                     resourceKey, String
   * @return                    String
   * @exception                 MissingResourceException
   **/
  public String text (String resourceKey) throws MissingResourceException {
    if (textResources == null) loadResource();
    String resource = textResources.getString(resourceKey);
    return resource;
  }

  /** <br><em>Purpose:</em> get the text from the resource bundle, but if not found, return key
   * <br><em>State Read:</em>   textResources.getString(resourceKey)
   * <br><em>Assumptions:</em>  resource bundle is lazily loaded if not already loaded
   * @param                     resourceKey, String
   * @return                    String, translated, but if not found, return the key
   **/
  public String translate (String resourceKey) {
    try {
      if (textResources == null) loadResource();
      String resource = textResources.getString(resourceKey);
      return resource;
    } catch (MissingResourceException e) {
      return resourceKey;
    }
  }

  /** <br><em>Purpose:</em> Convert text using supplied parameters
   ** in a 'text' message, specified by: %num%, where num starts from 1;
   ** parameters should be reusable in the same message and do not have to be used in passed order.
   * for example: <br>
   * <br><pre>
   *    MyMsg  :A %3% message taking %1% parameters for reasons of %3%, but not %2%.
   * </pre>
   * would return, using parameters : (3, triviality, fun) <br>
   * <br><pre>
   *    MyMsg  :A fun message taking 3 parameters for reasons of fun, but not triviality.
   * </pre>
   * <br><em>Assumptions:</em>  any additional parameters are appended at the end with commas;
   * if there are not enough params, then (unknown) is inserted instead.
   * @param                     text, String with embedded %1% style paramenter tags
   * @param                     altText, String
   * @param                     params, Collection of parameters
   * @return                    String
   **/
  public String convert (String key, String alttext, Collection params) {
    
    String text = null;
    try{ text = text(key);}
    catch(MissingResourceException mr)   { return alttext; }
    if ((text==null)||(text.length()==0)){ return alttext; }
    
    int size = params.size();
    StringBuffer buf = new StringBuffer(text.length() + size*10);
    StringTokenizer st = new StringTokenizer(text, delim, true);
    int max = 0;
    boolean finding = true;
    while (st.hasMoreTokens()) {
      String next = st.nextToken();
      if (next.equals(delim)) {
        if (finding) {
          finding = false;
        } else {
          finding = true;
        }
      } else {
        if (finding) {
          //System.out.println(next);
          buf.append(next);
        } else {
          try {
            Integer j = new Integer(next);
            int num = j.intValue();
            max = (max < num ? num : max);
            String val = getParam(num, params);
            //System.out.println(",val"+num+": "+val);
            buf.append(val);
          } catch (NumberFormatException nfe) {
            //throw new SAFSException("wrong format: "+delim+next+delim+", text: "+text);
            System.err.println("Bad format for \""+key+"\" in "+ getBundleName()+":"+text);
            return alttext;
          }
        }
      }
    }
    Iterator j = params.iterator();
    for(int i=1; j.hasNext(); i++) { // append additional params with commas
      Object next = j.next();
      if (i>max) {
        buf.append(", ");
        buf.append(next==null ? "(null)" : translate(next.toString()));
      }
    }
    return buf.toString();
  }

  /** <br><em>Purpose:</em> convenience function to build a Collection with params
   * <br><em>Assumptions:</em>  calls 'convert(text, Collection of params)'
   **/
  public String convert (String text, String alttext, String p1) {
    Collection c = new LinkedList();
    c.add(p1);
    return convert(text, alttext, c);
  }

  /** <br><em>Purpose:</em> convenience function to build a Collection with params
   * <br><em>Assumptions:</em>  calls 'convert(text, Collection of params)'
   **/
  public String convert (String text, String alttext, String p1, String p2) {
    Collection c = new LinkedList();
    c.add(p1);
    c.add(p2);
    return convert(text, alttext, c);
  }

  /** <br><em>Purpose:</em> convenience function to build a Collection with params
   * <br><em>Assumptions:</em>  calls 'convert(text, Collection of params)'
   **/
  public String convert (String text, String alttext, String p1, String p2, String p3) {
    Collection c = new LinkedList();
    c.add(p1);
    c.add(p2);
    c.add(p3);
    return convert(text, alttext, c);
  }

  /** <br><em>Purpose:</em> convenience function to build a Collection with params
   * <br><em>Assumptions:</em>  calls 'convert(text, Collection of params)'
   **/
  public String convert (String text, String alttext, String p1, String p2, String p3, String p4) {
    Collection c = new LinkedList();
    c.add(p1);
    c.add(p2);
    c.add(p3);
    c.add(p4);
    return convert(text, alttext, c);
  }

  /** <br><em>Purpose:</em> convenience function to build a Collection with params
   * <br><em>Assumptions:</em>  calls 'convert(text, Collection of params)'
   **/
  public String convert (String text, String alttext, String p1, String p2, String p3, String p4, String p5) {
    Collection c = new LinkedList();
    c.add(p1);
    c.add(p2);
    c.add(p3);
    c.add(p4);
    c.add(p5);
    return convert(text, alttext, c);
  }

  /** <br><em>Purpose:</em> grab a param from a Collection, if not there, append (unknown);
   ** it will also attempt to 'translate' the value obtained before returning it, using
   ** the same resource file, if it cannot translate, then it just returns the parameter
   ** at the position.  If a param is null then "(null)" will be returned.
   * @param                     num, int, starting from 1
   * @param                     params, Collection
   * @return                    String, if not found in 'params' then "(unknown)" is found
   **/
  protected String getParam (int num, Collection params) {
    //System.out.println(", params: "+params);
    Iterator j = params.iterator();
    for(int i=1; j.hasNext(); i++) {
      Object next = j.next();
      if (i==num) {
        return (next==null ? "(null)" : translate(next.toString()));
      }
    }
    return "(unknown)";
  }

  /** used to test
   **/
  public static void main (String[] args) {
    if (args.length < 1) {
      System.out.println("Need at least one arg as the key to the resource bundle, then params");
    }
    List params = new LinkedList();
    for(int k=1; k<args.length; k++) params.add(args[k]);
    GetText gt1 = new GetText();
    GetText gt2 = new GetText();
    gt1.loadResource();
    System.out.println("bundle: "+gt1.getBundleName());
    try {
      String text = gt1.text(args[0]);
      String cnv = gt1.convert(args[0], "alttext", params);
      System.out.println("text: "+text);
      System.out.println("converted: "+cnv);
    } catch (MissingResourceException e) {
      System.err.println("Can't find resource for bundle "+gt1.getBundleName()+", key "+args[0]);
    }
    // let's try another
    gt2.setLocale(Locale.GERMAN);
    gt2.loadResource();
    System.out.println("bundle: "+gt2.getBundleName());
    try {
      String text = gt2.text(args[0]);
      String cnv = gt2.convert(args[0], "alttext", params);
      System.out.println("text: "+text);
      System.out.println("converted: "+cnv);
    } catch (MissingResourceException e) {
      System.err.println("Can't find resource for bundle "+gt2.getBundleName()+", key "+args[0]);
    }
  }
  
}
