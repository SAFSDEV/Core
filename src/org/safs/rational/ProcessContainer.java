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
package org.safs.rational;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;

import java.io.*;
import java.util.*;
import org.safs.GuiObjectRecognition;
import org.safs.PCTree;
import org.safs.StringUtils;
import org.safs.tools.CaseInsensitiveFile;

/**
 * ProcessContainer is used to deduce recognition string information using the RobotJ API for  
 * objects like DomainTestObject and TestObject.  Internally, ProcessContainer also relies on 
 * the general-purpose org.safs.PCTree object to correctly produce recognition matching that 
 * used for all SAFS-Engine tools using the SAFS algorithms for locating components.
 * 
 * @author  Carl Nagle
 * @since   MAY 08, 2003
 *
 *   <br>   MAY 08, 2003    (CNagle) Original Release
 *   <br>   OCT 09, 2003    (DBauman) Reformatting, refactoring, commenting
 *   <br>   OCT 31, 2003    (DBauman) now accept parameter 'withCommentsAndBlankLines'
 *                           to space out the output a bit.
 *   <br>   NOV 12, 2003    (DBauman) modifying such that if a component is a container and
 *                           is invisible, then ignore
 *   <br>   SEP 02, 2004    (Carl Nagle) Do not seek children in comboboxes
 *   <br>   SEP 20, 2005    (Carl Nagle) Many enhancements for Functional Tester and object recognition in general.
 * 
 * @see org.safs.PCTree
 **/
public class ProcessContainer {
        
  private int     level  = 0;
  
  //true if Errors were already reported in System.out
  private boolean alreadyReported = false;

  private static boolean verbose = false;
  private static boolean iniVsTreeFormat = true;
  private static boolean withNameIncludeOnlyCaption = false;
  private static boolean shortenGeneralRecognition = true;
  private static boolean withCommentsAndBlankLines = false;
  private static boolean ignoreInvisibleComponents = true;
  private static boolean ignoreInvisibleTabPanes = true;
  private static String  rootFileName = "PCout.txt";
  private static boolean append = true;  
  private static boolean showMethods = false;
  private static boolean showProperties = false;
  
  public static final String INDENT = "    ";
  public static final String pathSep = "->";
  
  private Writer writer  = null;

  /** 
   * Simplified constructor, takes array of DomainTestObject as the starting point,
   * opens preset filename for write, then calls processDomain(domains[i]) 
   * for each domains[i] in the passed array, finally closes the output file.  
   * <p>
   * This constructor is typically called after many of the static methods have been used to 
   * set operating criteria.
   * 
   * @param DomainTestObject[] domains
   **/
  public ProcessContainer (DomainTestObject[] domains) {
    this(domains, rootFileName, verbose, iniVsTreeFormat, withNameIncludeOnlyCaption, 
    	 withCommentsAndBlankLines, ignoreInvisibleComponents, append);
  }

  /** 
   * Intermediate constructor, takes array of DomainTestObject as the starting point,
   * opens filename for write, then calls processDomain(domains[i]) for each domains[i] in the
   * passed array, finally closes the file.
   * <p>
   * The settings provided by other constructors can also be preset through static method calls 
   * prior to calling this constructor.
   * 
   * @param                     domains, DomainTestObject[]
   * @param                     filename, String
   * @param                     withCommentsAndBlankLines, boolean
   **/
  public ProcessContainer (DomainTestObject[] domains, String filename,
                           boolean withCommentsAndBlankLines) {
    this(domains, filename, verbose, iniVsTreeFormat, withNameIncludeOnlyCaption, 
    	 withCommentsAndBlankLines, ignoreInvisibleComponents, append);
  }

  /** 
   * Detailed constructor, takes array of DomainTestObject as the starting point,
   * opens filename for write, then calls processDomain(domains[i]) for each domains[i] in the
   * passed array, finally closes the file.
   * 
   * @param                     domains, DomainTestObject[]
   * @param                     filename, String
   * @param                     verbose, boolean, puts out more info if true
   * @param                     iniVsTreeFormat, boolean, if true then ini format, else tree
   * @param                     withNameIncludeOnlyCaption, if false, include full recog string
   * @param                     withCommentsAndBlankLines, boolean
   * @param                     ignoreInvisibleComponents, boolean
   * @param                     append, boolean
   **/
  public ProcessContainer (DomainTestObject[] domains, String filename,
                           boolean verbose, boolean iniVsTreeFormat,
                           boolean withNameIncludeOnlyCaption,
                           boolean withCommentsAndBlankLines,
                           boolean ignoreInvisibleComponents,
                           boolean append) {
    try {
      if (filename != null) this.rootFileName = filename;
      this.verbose = verbose;
      this.iniVsTreeFormat = iniVsTreeFormat;
      this.withNameIncludeOnlyCaption = withNameIncludeOnlyCaption;
      this.withCommentsAndBlankLines = withCommentsAndBlankLines;
      this.ignoreInvisibleComponents = ignoreInvisibleComponents;
      this.append = append;
      writer = openWriter(rootFileName, append);
      if (writer == null) return;
      for(int i = 0; i < domains.length; i++) {
        PCTree tree = processDomain(domains[i], null);
        if (tree != null) {
          tree.setupIndexMap();
          System.out.println("domain["+i+"], tree: \n"+(tree==null?"<null>":tree.toString()));
          writeTree(tree);
          String xmlFilename = rootFileName + "." + Integer.toString(i) +".xml";
          writeTreeXML(xmlFilename, tree);
        }
      }
      closeWriter(writer);
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }

  /** 
   * Simple constructor, takes a TestObject as the starting point,
   * opens preset filename for write, then calls processContainer(container), 
   * finally closes the file.
   * <p>
   * The settings provided by other constructors can also be preset through static calls to 
   * the ProcessContainer.setXXX methods.
   * 
   * @param                     container, TestObject
   **/
  public ProcessContainer (TestObject container) {
    this(container, rootFileName, verbose, iniVsTreeFormat, withNameIncludeOnlyCaption, 
       	 withCommentsAndBlankLines, ignoreInvisibleComponents, append);
  }  
  
  /** 
   * Intermediate constructor, takes a TestObject as the starting point,
   * opens filename for write, then calls processContainer(container), finally closes the file.
   * <p>
   * The settings provided by other constructors can also be preset through static calls to 
   * the ProcessContainer.setXXX methods.
   * 
   * @param                     container, TestObject
   * @param                     filename, String
   **/
  public ProcessContainer (TestObject container, String filename) {
    this(container, filename, verbose, iniVsTreeFormat, withNameIncludeOnlyCaption, 
       	 withCommentsAndBlankLines, ignoreInvisibleComponents, append);
  }  
  
  /** 
   * Detailed constructor, takes a TestObject as the starting point,
   * opens filename for write, then calls processContainer(container), finally closes the file.
   * 
   * @param                     container, TestObject
   * @param                     filename, String
   * @param                     verbose, boolean, puts out more info if true
   * @param                     iniVsTreeFormat, boolean, if true then ini format, else tree
   * @param                     withNameIncludeOnlyCaption, if false, include full recog string
   * @param                     withCommentsAndBlankLines, boolean
   * @param                     ignoreInvisibleComponents, boolean
   * @param                     append, boolean
   **/
  public ProcessContainer (TestObject container, String filename,
                           boolean verbose, boolean iniVsTreeFormat,
                           boolean withNameIncludeOnlyCaption,
                           boolean withCommentsAndBlankLines,
                           boolean ignoreInvisibleComponents,
                           boolean append) {
    try {
      if (filename != null) this.rootFileName = filename;
      this.verbose = verbose;
      this.iniVsTreeFormat = iniVsTreeFormat;
      this.withNameIncludeOnlyCaption = withNameIncludeOnlyCaption;
      this.withCommentsAndBlankLines = withCommentsAndBlankLines;
      this.ignoreInvisibleComponents = ignoreInvisibleComponents;
      this.append = append;
      writer = openWriter(rootFileName, append);
      if (writer == null) return;
      level = 0;
      PCTree tree = processContainer(container, null, null);
//      PCTree tree = processContainer(container, 1, null, null);
      System.out.println("tree: \n"+(tree==null?"<null>":tree.toString()));
      writeTree(tree);
      closeWriter(writer);
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }

  /**
   * Used internally. 
   * Called by either the constructor or used in recursion to process a domain.
   * 
   * @param                     domain, DomainTestObject
   * @param                     parent, PCTree passed along to processChildren
   * 
   * @see #processChildren(TestObject[], String, PCTree)
   **/
  protected PCTree processDomain (DomainTestObject domain, PCTree parent) {
    String dname = null;
    try {
      dname = (String) domain.getName();
    } catch (Exception e) {
      return null;
    }
    write("");
    write("DomainTestObject Information:");
    write("==============================");
    write("Name = " + dname);
    write("Implementation Name: " + domain.getImplementationName());
    PCTree firstChild = null;
    if ((dname.equalsIgnoreCase("Java"))||
    	(dname.equalsIgnoreCase("Html"))||
    	(dname.equalsIgnoreCase("Net")) ||
    	(dname.equalsIgnoreCase("Win")) ||
    	(dname.equalsIgnoreCase("Swt"))){
        try {
            TestObject[] children  = domain.getTopObjects();
            firstChild = processChildren(children, null, parent);
        } catch (WrappedException we) {
            // the domain.getTopObjects() call throws a WrappedException when
            // trying to work with some "Win" objects.  ignore and move on.
        }
    }
    write("");
    write("Finished with DomainTestObject");
    write("==============================");
    return firstChild;
  }

  /** 
   * Entry point to process a domain or object container.
   * This routine will appropriately call either processDomain or processObjectContainer 
   * depending on which type the 'container' is.
   * 
   * @param                     container, TestObject
   * @param                     parentText, String, for menus, it is the path of our parent
   * 
   * @see #processDomain(DomainTestObject, PCTree)
   * @see #processObjectContainer(TestObject, String, PCTree)
   **/
    public PCTree processContainer (TestObject container, String parentText, PCTree parent) {
    if ((container == null) ||
        (writer    == null)) {
      System.err.println("A Container was not properly initialized");
      System.err.println("Either no TestObject OR no File Writer specified!");
      System.err.println("");
      return null;
    }
    if (container.getClass().getName().endsWith("DomainTestObject")){
    	PCTree achild = processDomain((DomainTestObject)container, parent);
    	if (achild != null) achild.setParent(parent);
    	return achild;
    }else{
    	PCTree achild = processObjectContainer(container, parentText, parent);
    	if (achild != null) achild.setParent(parent);
    	return achild;
    }
  }

  /** 
   * Used internally.
   * Entry point called from 'processContainer' to process a component\container.
   * Process the TestObject container, and recursively, all of it's children through 
   * 'processChildren'.
   * 
   * @param   container, TestObject
   * @param   parentText, String, for menus, it is the path of our parent
   *   <br>   SEP 02, 2004    (Carl Nagle) Do not seek children in comboboxes
   * 
   * @see #processChildren(TestObject[], String, PCTree)
   **/
    protected PCTree processObjectContainer (TestObject container, String parentText, PCTree parent) {
    String classname = container.getObjectClassName();
    
    //output preliminary TestObject header and classname
    write("");
    write("TestObject Information:");
    write("========================");
    write("ClassName = " + classname);

    try {
	    Map recognitionProperties = container.getRecognitionProperties();
	
	    //output recognitionProperties if allowed
	    writeProperties("RecognitionProperties", recognitionProperties);
	
	    //output enabled\visible state info
	    writeState(container);
    } 
    catch(NullPointerException npe) { /* ignore */ }
    catch(WrappedException we) { /* ignore */ }
    
    Map properties = container.getProperties();
    
    // output standard properties, if allowed
    if(showProperties){
        writeProperties("ValueProperties", properties);
        Map nonValueProperties = container.getNonValueProperties();
        writeProperties("NonValueProperties", nonValueProperties);
    }

    // output known invocable methods, if allowed
    if (showMethods) {
        MethodInfo[] methods = container.getMethods();
        writeMethods(methods);
    }    
    String alttype = null;
    
    // check for object visibility
    boolean visible = RGuiObjectRecognition.isObjectVisible((TestObject)container);
    
	try{
      alttype = getMappedClassType(container, classname);		
      if (ignoreInvisibleComponents) {
        boolean isContainerType = (alttype==null)? false : GuiObjectRecognition.isContainerType(alttype);
        if (!visible) {
          if (isContainerType) System.out.println("Invisible container: "+ classname);          
          // ignore anything that is not a TabControl subpanel
          if (parent == null) {
              return null;
          } else {
              // check to see if parent is a TabControl 
          	  // we should still process hidden panels of TabControls, however
              String ptype = parent.getType();
              try{
            	  if(ptype.equalsIgnoreCase("TabControl")){
            		  if((ignoreInvisibleTabPanes)&&(isContainerType)){
            			  return null;
            		  }
            	  }
            	  // Parent IS not a TabControl
            	  else if(isContainerType) {
            	      return null;
                  }
              }
              // do not process invisible unknown types
              catch(NullPointerException nullptype){return null;}
          }
        }
      }
    } 
    catch (ClassCastException cce) { /*ignore*/ }
    catch (PropertyNotFoundException pnfe) { /*ignore*/ }

    PCTree pctree = new PCTree();
    pctree.setParent(parent); // just in case
    pctree.setComponentVisible(visible);
    pctree.setWithNameIncludeOnlyCaption(withNameIncludeOnlyCaption);
    pctree.setShortenGeneralRecognition(shortenGeneralRecognition);
    pctree.setWithCommentsAndBlankLines(withCommentsAndBlankLines);

    String text = genRecogString(pctree, container, properties, parentText);

    // try for all children(true), or only mappable children(false)
    boolean fullChildrenPath = true;
    String uIClassID = null;  
    
    try { uIClassID = (String) container.getProperty("uIClassID"); }
    // uIClassID applies to Java, but apparently not Net and Win
    catch(Exception x){ uIClassID = alttype; }
    
    // for menus(menuitems) only get mappable children
    if (uIClassID != null &&
        uIClassID.length()>=8 && uIClassID.substring(0, 8).equalsIgnoreCase("MenuItem")) {
        fullChildrenPath = false;
    }
    
    TestObject[] children = null;

    //always get children if our current comp type is unknown
    if(alttype == null) {
      children = (fullChildrenPath ? container.getChildren() : container.getMappableChildren());

    // else only get children if our current object's children should NOT be ignored.
    }else {
	    if (! GuiObjectRecognition.isIgnoredTypeChild(alttype)) {
	    	children = (fullChildrenPath ? container.getChildren() : container.getMappableChildren());
	    	
	    	try{
		    	// try alternate methods of getting the children for some components
	    		if((children.length == 0)&&
				  ((alttype.equalsIgnoreCase("Menu"))||
				   (alttype.equalsIgnoreCase("JavaMenu"))||
				   (alttype.equalsIgnoreCase("MenuItem")))){

	    			// try to get Java menu items
	    			children = (TestObject[]) container.invoke("getMenuComponents");
	    		}
	    	}
	    	catch(Exception nullpointer){ /* ignore it */ }
	    }
    }
    write("");
    write((children == null ? "0" : Integer.toString(children.length)) +
          " Children for this Test Object:");

    // recursively process any children we found
    PCTree firstChild = processChildren(children, text, pctree);
    pctree.setFirstChild(firstChild);
    if (firstChild == null) pctree.setChildCount(new Integer(0));
    else pctree.setChildCount(firstChild.getSiblingCount());

    //output final TestObject footer info
    write("");
    write("Finished with TestObject");
    write("========================");
    return pctree;
  }

  /** 
   * Used internally.
   * Essentially, processContainer(children[i]) for all children and handling 
   * parent, child, and sibling relationships.
   *  
   * @param   children, TestObject[] (can be null)
   * @param   parentText, String, typically for menus, it is the Path of our parent
   * @param   pctree PCTree, keeps the recognition string info, will be written
   * @return  firstpctree (the first PCTree generated for the children)
   * 
   * @see #processContainer(TestObject, String, PCTree)
   **/
  protected PCTree processChildren(TestObject[] children, String parentText, PCTree parent) {
    PCTree firstpctree = null;
    if (children != null) {
      level++;
      PCTree lastpctree = null;
      Map compMap = new HashMap();
      int j=1;
      for (int i = 0; i < children.length; i++){
        PCTree next = processContainer(children[i], parentText, parent);
        if (next != null) {
          if (lastpctree!=null) lastpctree.setNextSibling(next);
          else firstpctree = next;
          lastpctree = next;
          next.setLevel(new Integer(level));
          next.setSiblingCount(new Integer(children.length));
          next.setParent(parent);
          next.setSiblingIndex(j);
          next.setCompMap(compMap);
          ArrayList list = (ArrayList)compMap.get(next.getType());
          if (list==null) {
            list = new ArrayList();
            compMap.put(next.getType(), list);
          }
          list.add(next);
          j++;
        }
      }
      level--;
    }
    return firstpctree;
  }

  /** 
   * Used internally.
   * Get the mapped class type (ultimately from something like JavaObjectsMap.dat)
   * This is the derived "Window", "CheckBox", or "EditBox" class type for the given classname.
   * 
   * @param                     tobj, TestObject
   * @param                     objectClassName, String
   * @return                    String, null if not found
   **/
  protected String getMappedClassType(TestObject tobj, String objectClassName) {
    try{
      System.out.println("PC: compTestObject : "+ objectClassName);
      String mappedClassType = (new RGuiClassData()).getMappedClassType(objectClassName, tobj);
      System.out.println("PC: mappedClassType: "+ mappedClassType);
      return mappedClassType;
    } catch (Exception npe) {
      npe.printStackTrace();
      System.out.println(getClass().getName()+".getMappedCompType"+
                         ": An error in RGuiClassData.getMappedClassType MUST be corrected, "+
                         ", tobjClassName:" + objectClassName +
                         ", tobj: " + tobj +
                         "; most likely the def of the class does not exist in the .dat file(s); "
                         +npe);
      return null;
    }
  }

  /**
   * Used internally.
   * Generate the recognition string for this object.  This does not include any parent hierarchy 
   * information.  Only the portion of the recognition string that applies to this component.
   * 
   * @param                     pctree PCTree, keeps the rcognition string info, will be written
   * @param                     container, TestObject
   * @param                     properties, Map
   * @param                     parentText, String, for menus, it is the path of our parent
   * @return                    Text/Name/Caption/Path property value if it exists, else null
   * 
   * @author Bob Lawler (Bob Lawler), 08.29.2006 - Now that RGuiObjectRecognition.getName() has been 
   *         updated to remove first trying to retrieve Object's accessible name, this function
   *         will never return Object's accessible name.  Code needs to be updated in the future to
   *         return getObjectAccessibleName() or getObjectName() based on user's defined preference.
   **/
  protected String genRecogString(PCTree pctree, TestObject container, Map properties, String parentText) {
    String classname = container.getObjectClassName();
    String alttype = getMappedClassType(container, classname);
    String domainname = (String) container.getDomain().getName();
    System.out.println("Domain: "+ domainname);
    Map recognitionProperties = container.getRecognitionProperties();
        
    String compType = alttype;
    if (alttype!=null) {
      String[] types = alttype.split(",");
      if (types.length > 1){      	      	
      	for(int ii=0;ii < types.length; ii++){
      		compType=types[ii];
      		if (compType.toLowerCase().startsWith(domainname.toLowerCase())){
      			break;
      		}
      	}
      }
   	  if ((compType.equalsIgnoreCase("Window"))&&(domainname.equalsIgnoreCase("Java"))) compType="JavaWindow";
	  alttype = compType;      		
      
	  // MenuItems appear as Path= info for the parent Menu.
      // so the Type to set for the menuitem is "Menu"
      compType = compType.equalsIgnoreCase("MenuItem") ? "Menu" : compType ;
      pctree.setType(compType);
      if(alttype.equalsIgnoreCase("Generic")) 
      	pctree.setMyclass(classname);      	      
    } 
    else {
      pctree.setMyclass(classname);
      compType = classname;
    }

    boolean caption = false;
    boolean textval = false;    

    // try to get Name= info for recognition
    String text = RGuiObjectRecognition.getName(container);
    
    // else try to get Caption= recognition where appropriate
    if ((text==null)||(text.length()==0)){
    	if (RGuiObjectRecognition.isTopLevelWindow(container)){
	    	text = RGuiObjectRecognition.getCaption((GuiTestObject)container);    
	    	if ((text != null)&&(text.length()>0)) {	
	    		caption = true; 
	    	}else {
	    		text = null;
	    	}
    	}
    }    
    // else see if we can use Text= recognition for certain comp types
    if((text==null)||(text.length()==0)){
    	if (GuiObjectRecognition.isTextOKForRecognition(alttype)){
    		text = RGuiObjectRecognition.getText(container);
    	    if ((text != null)&&(text.length()>0)) {	
    	    	textval = true;
    	    }else {
    	    	text = null;
    	    }
    	}
    }    
    //trim and\or null out empty values
    if (text != null) {
    	text = text.trim();
    	if (text.length()==0) text = null;
    }

    //begin to set the recognition information    
    String textResult = text;
    if (text != null) {
    	if (alttype.equalsIgnoreCase("MenuItem")){
			if ((parentText != null)&&(parentText.length()>0)) {	        	
        		textResult = parentText + pathSep + text;
	        }
	        pctree.setPath(textResult);
	    } else if (text.length()==0) {
	        pctree.setClassIndex(alttype==null);
	    } else {
	        pctree.setNameValue(textResult);
	        pctree.setCaption(caption);
	        pctree.setTextValue(textval);
	    }
	} else {
	    pctree.setClassIndex(alttype==null);
	}
    pctree.setName(makeName(textResult, compType));    
   	return textResult;
  }

  // Map of all comp names already used (to avoid duplication)
  private Map nameMap = new HashMap();
  
  /**
   * Used internally. 
   * Creates a (unique) name based on the 'text', or if no text, then the 'compType';
   * If the name already exists in our nameMap then we append an index after the name 
   * to make it unique.
   * 
   * @param                     text, String
   * @param                     compType, String
   * @return                    String, the comp name generated
   **/
  protected String makeName (String text, String compType) {
    String name;
    if (text==null || text.length()==0) {
        name = PCTree.removeNonNameChars(compType);
    } 
    else {
        name = PCTree.removeNonNameChars(text);
        if(name.length()==0)
            name = PCTree.removeNonNameChars(compType);
    }
    int i = 1;
    String altName = name + Integer.toString(i);
    if (nameMap.get(altName) == null) {
      nameMap.put(altName, compType);
      return altName;
    } else { // already used, generate another.
      for(++i; ; i++) {
        altName = name + Integer.toString(i);
        if (nameMap.get(altName) != null) { // already used, generate another.
          continue;
        }
        nameMap.put(altName, compType);
        return altName;
      }
    }
  }

  /** 
   * Write the passed property type line(propType) and then the values of the 'properties'
   * The routine will not write the properties out if showProperties is false.
   * 
   * @param   propType, String simple description of the types of properties processed.
   * @param   properties, Map (can be null)
   * 
   * @see #showProperties 
   * @see #getShowProperties()
   **/
  protected void writeProperties (String propType, Map properties) {
  	if (! showProperties) return;
    write(propType+":");
    if (properties != null) {
      Iterator keys = properties.keySet().iterator();
      Object property;
      while(keys.hasNext()){
        property = keys.next();
        Object obj = properties.get(property);
        write(INDENT + property + "=" + (obj==null?"<null>":obj));
      }
    }
  }

  /** 
   * Write the methods info provided.
   * The routine will not write out if showMethods is false.
   * 
   * @param   methods, MethodInfo[] (can be null)
   * 
   * @see #showMethods 
   * @see #getShowMethods()
   **/
  protected void writeMethods (MethodInfo[] methods) {
  	if (! showMethods) return;
    write("Available Methods:");
    if (methods != null) {
      MethodInfo method = null;
      for(int i=0; i< methods.length; i++){
      	method = methods[i];
        write(INDENT + method.getName());
        write(INDENT + INDENT +"From Class: "+ method.getDeclaringClass());
        write(INDENT + INDENT +" Signature: "+ method.getSignature());
      }
    }
  }

  /** 
   * Write the isEnabled and isShowing states of the 'container'
   * 
   * @param   container, TestObject (can be null)
   **/
  protected void writeState (TestObject container) {
    try{
      boolean state = ((GuiTestObject)container).isEnabled();
      write("isEnabled=" + state);
      state = ((GuiTestObject)container).isShowing();
      write("isShowing=" + state);
    }catch(Exception ex){;}
  }

  /** 
   * Writes String data to the writer ONLY if verbose is true.  
   * 
   * @param  data, String
   * 
   * @see #verbose
   * @see #getVerbose()
   **/
  protected void write (String data) {
    if (verbose) writeRaw(data);
  }

  /** 
   * Write the PCTree data to the writer.
   * If iniVsTreeFormat is true uses the toIniString method.
   * Otherwise uses the normal toString method.
   * 
   * @param  tree, PCTree
   * 
   * @see #iniVsTreeFormat
   * @see #getINITreeFormat()
   * @see org.safs.PCTree#toIniString()
   * @see org.safs.PCTree#toString()
   **/
  protected void writeTree (PCTree tree) {
    if (tree == null) return;
    try {
      System.out.println("[ini:"+iniVsTreeFormat);
      if (iniVsTreeFormat) {
        writer.write(tree.toIniString());
      } else {
        writer.write(tree.toString());
      }
      writeNewLine(writer);
      //writer.flush();
    } catch (IOException io) {
      if (!alreadyReported) {
        io.printStackTrace();
        System.err.println(io.getMessage());
      }
      alreadyReported = true;
    }
  }

  /**
   * Output the PCTree data in XML format.
   * 
   * @param xmlfile, String filename for output
   * @param tree, PCTree to output in XML encoded format
   * 
   * @see org.safs.xml.XMLEncoderDecoder#xmlEncode(Serializable)
   */
  protected void writeTreeXML(String xmlfile, PCTree tree){
    try{
    	Writer xmlWriter = openWriter(xmlfile, false);
	    if (xmlWriter != null) {
	      byte[] b = null;
	      try {
	        b = org.safs.xml.XMLEncoderDecoder.xmlEncode(tree);
	      } catch (NoClassDefFoundError ncdfe) {
	        System.err.println("ncdfe: "+ncdfe.getMessage());
	      }
	      if (b != null) {
	        try{
	          xmlWriter.write(new String(b));
	        }catch(IOException io){
	          System.err.println("io: "+io.getMessage());
	        }
	      }
	      closeWriter(xmlWriter);
	    }
    }catch(Exception x){
    	x.printStackTrace();
    }
  }

  // static reusable string buffer
  static StringBuffer msgbuf = new StringBuffer(100);
  
  /** 
   * Write the raw data to the open writer.
   * 
   * @param  data, String
   **/
  protected void writeRaw (String data) {
    msgbuf.delete(0, msgbuf.length());
    for(int i = 0; i < level; i++) {
      msgbuf.append(INDENT);
    }
    msgbuf.append(data);
    try {
      writer.write(msgbuf.toString());
      writeNewLine(writer);
      //writer.flush();
    } catch (IOException io) {
      if (!alreadyReported) {
        io.printStackTrace();
        System.err.println(io.getMessage());
      }
      alreadyReported = true;
    }
  }

  /** 
   * Write a new line to the passed 'writer'
   * 
   * @param writer, Writer
   * @throws IOException Writer pass-thru
   **/
  protected void writeNewLine (Writer writer) throws IOException {
    writer.write(System.getProperty("line.separator"));
  }

  /** 
   * Closes the passed writer.
   * @param writer, Writer
   **/
  protected void closeWriter (Writer writer) {
    try{
      writer.close();
    } catch (IOException io) {
      io.printStackTrace();
      System.err.println(io.getMessage());
    }
  }

  /** 
   * Opens a new BufferedWriter(FileWriter(filename)).
   * 
   * @param filename, String
   * @param append, boolean true if file is to be opened in Append mode.
   * @return Writer, may be null if unsuccessful.
   **/
  protected Writer openWriter (String filename, boolean append) {
    Collection prePart = null;
    try {
      if (append) prePart = StringUtils.readfile(filename);
    } catch (IOException io) { //ignore
    }
    Writer result = null;
    try {
      Writer writer = new FileWriter(new CaseInsensitiveFile(filename).toFile());
      result = new BufferedWriter(writer);
      if (prePart != null && prePart.size()>0) {
        for(Iterator i= prePart.iterator(); i.hasNext(); ) {
          String s = i.next().toString() + "\n";
          result.write(s);
        }
        if (withCommentsAndBlankLines) {
          java.util.Date d = new java.util.Date();
          String s = "\n; Another run of ProcessContainer; "+d.toString()+"\n";
          result.write(s);
        }
      }
      return result;
    } catch (IOException io) {
      io.printStackTrace();
      System.err.println(io.getMessage());
      return null;
    }
  }

  /**
   * @return the status of the 'showMethods' flag.
   * @see #showMethods
   */
  public static boolean getShowMethods(){ return showMethods; }
  
  /**
   * Set the status of the 'showMethods' flag.
   * @see #showMethods
   */
  public static void setShowMethods(boolean show){
  	showMethods = show;
  }
  
  /**
   * @return the status of the 'showProperties' flag.
   * @see #showProperties
   */
  public static boolean getShowProperties(){ return showProperties; }
  
  /**
   * Set the status of the 'showProperties' flag.
   * @see #showProperties
   */
  public static void setShowProperties(boolean show){
  	showProperties = show;
  }

  /**
   * @return the status of the 'verbose' flag.
   * @see #verbose
   */
  public static boolean getVerbose(){ return verbose;}
  
  /**
   * Set the status of the 'verbose' flag.
   * @see #verbose
   */
  public static void setVerbose(boolean set){
  	verbose = set;
  }
  
  /**
   * @return the status of the 'iniVsTreeFormat' flag.
   * @see #iniVsTreeFormat
   */
  public static boolean getINITreeFormat(){ return iniVsTreeFormat;}
  
  /**
   * Set the status of the 'iniVsTreeFormat' flag.
   * @see #iniVsTreeFormat
   */
  public static void setINITreeFormat(boolean set){
  	iniVsTreeFormat = set;
  }
  
  /**
   * @return the status of the 'withNameIncludeOnlyCaption' flag.
   * @see #withNameIncludeOnlyCaption
   */
  public static boolean getShortenNamedRecognition(){ return withNameIncludeOnlyCaption;}
  
  /**
   * Set the status of the 'withNameIncludeOnlyCaption' flag.
   * @see #withNameIncludeOnlyCaption
   */
  public static void setShortenNamedRecognition(boolean set){
  	withNameIncludeOnlyCaption = set;
  }
  
  /**
   * @return the status of the 'shortenGeneralRecognition' flag.
   * @see #shortenGeneralRecognition
   */
  public static boolean getShortenGeneralRecognition(){ return shortenGeneralRecognition;}
  
  /**
   * Set the status of the 'shortenGeneralRecognition' flag.
   * @see #shortenGeneralRecognition
   */
  public static void setShortenGeneralRecognition(boolean set){
  	shortenGeneralRecognition = set;
  }
  
  /**
   * @return the status of the 'withCommentsAndBlankLines' flag.
   * @see #withCommentsAndBlankLines
   */
  public static boolean getShowCommentsAndBlankLines(){return withCommentsAndBlankLines;}
  
  /**
   * Set the status of the 'withCommentsAndBlankLines' flag.
   * @see #withCommentsAndBlankLines
   */
  public static void setShowCommentsAndBlankLines(boolean set){
  	withCommentsAndBlankLines = set;
  }
  
  /**
   * @return the status of the 'ignoreInvisibleComponents' flag.
   * @see #ignoreInvisibleComponents
   */
  public static boolean getIgnoreInvisibleComponents(){return ignoreInvisibleComponents;}
  
  /**
   * Set the status of the 'ignoreInvisibleComponents' flag.
   * @see #ignoreInvisibleComponents
   */
  public static void setIgnoreInvisibleComponents(boolean set){
  	ignoreInvisibleComponents = set;
  }
  
  /**
   * @return the status of the 'ignoreInvisibleTabPanes' flag.
   * @see #ignoreInvisibleTabPanes
   */
  public static boolean getIgnoreInvisibleTabPanes(){return ignoreInvisibleTabPanes;}
  
  /**
   * Set the status of the 'ignoreInvisibleTabPanes' flag.
   * @see #ignoreInvisibleTabPanes
   */
  public static void setIgnoreInvisibleTabPanes(boolean set){
  	ignoreInvisibleTabPanes = set;
  }
  
  /**
   * @return the status of the 'append' flag.
   * @see #append
   */
  public static boolean getAppendMode(){return append;}
  
  /**
   * Set the status of the 'append' flag.
   * @see #append
   */
  public static void setAppendMode(boolean set){
  	append = set;
  }

  /**
   * @return the current value of the 'rootFileName' String.
   * @see #rootFileName
   */
  public static String getRootFilename(){return rootFileName;}
  
  /**
   * Set the current value of the 'rootFileName' String.
   * @see #rootFileName
   */
  public static void setRootFilename(String file){
  	rootFileName = file;
  }
}
