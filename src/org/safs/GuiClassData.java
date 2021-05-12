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
package org.safs;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.safs.jvmagent.AgentClassLoader;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;

/**
 * This class is primarily used to open and read in external data files that map class 
 * names to supported class types as well as mapping class types to the library supporting 
 * the mapped class type.  
 * <ul>
 * <li>Example class mapping: javax.swing.JPopupMenu=JavaPopupMenu
 * <li>Example  type mapping: JavaPopupMenu=PopupMenu
 * </ul>
 * Default class to type mapping is stored in "JavaObjectsMap.dat".<br/>
 * Default types to library mapping is stored in "ObjectTypesMap.dat".<br/>
 * <p>
 * Custom class to type mapping is stored in "CustomJavaObjectsMap.dat".<br/>
 * Custom types to library mapping is stored in "CustomObjectTypesMap.dat".<br/>
 * 
 * @author OCT 31, 2005 Carl Nagle Enhanced to allow a single class to be mapped to multiple 
 *                              object types via comma-separated list.
 * @author JUN 15, 2009 Carl Nagle Enhanced to allow final attempt at "Custom" files. 
 * @author JUN 18, 2009 Carl Nagle Reduce Logging to avoid queue full exceptions. 
 * @author FEB 07, 2013 Lei Wang Use IndependantLog instead of Log to log message.
 * @author AUG 13, 2013 Carl Nagle getMappedClassType supporting new 'recursive' and 'allowGeneric' options.
 **/
public class GuiClassData {

	public static final String DEFAULT_JAVA_OBJECTS_MAP = "JavaObjectsMap.dat";
	public static final String DEFAULT_OBJECT_TYPES_MAP = "ObjectTypesMap.dat";
	public static final String CUSTOM_JAVA_OBJECTS_MAP  = "CustomJavaObjectsMap.dat";
	public static final String CUSTOM_OBJECT_TYPES_MAP  = "CustomObjectTypesMap.dat";
	
	public static final String DEFAULT_CLASS_TYPE      	= "Generic";
	public static final String DEFAULT_OBJECT_TYPE      = "Component";
	public static final String DEFAULT_TYPE_SEPARATOR   = ",";

	// are these just containers?  or Invisible containers?
	// originally from org.safs.rational.ProcessContainer
    public static final String[] CONTAINER_TYPES = {
    	"TabControl", 
    	"JavaPanel", 
    	"JavaWindow", 
    	"Window", 
    	"JavaSplitPane"
    };
    
	// originally from org.safs.rational.ProcessContainer
    public static final String[] ALT_NAME_TYPES = {
    	"PushButton", 
    	"Label"
    };
	
    public static final String[] TOOLTIP_CONTAINER_TYPES = {
    	"JavaPanel",
    	"Panel"
    };
    
    public static final String[] POPUP_MENU_CLASSES = {
    	"#32768",
    	".Menupopup"
    };
    
	// holds java class to (String) classtype mappings (one-to-one mapping)
	// the value map may be a comma separated list of multiple types
	static protected Properties classesmap = null;

	// holds class type (String) to library mappings (one-to-one mapping)
	static protected Properties classtypesmap = null;	

	// holds classtype to classlist mappings (one-to-many mapping)
	static protected Hashtable  classassigns = null;	

	/**
	 * Attempt to locate the URL of a resource.
	 * This can be in the JAR file containing a specified class, 
	 * or in the directory containing the JAR file.
	 * Multitple searches attempted including:
	 * <ol>
	 * <li> clazz.getResource
	 * <li> clazz.getClassLoader().getResource
	 * <li> clazz.getClassLoader().getSystemResource
	 * <li> getSystemClassLoader().getResource
	 * <li> getSystemClassLoader().getSystemResource
	 * <li> Thread.getContextLoader().getResource()
	 * <li> Thread.getContextLoader().getSystemResource()
	 * <li> AgentClassLoader.getResource (using java.class.path)
	 * </ol>
	 * @param clazz -- Class associated with the resource -- mapping to the JAR or directory resource might be found.
	 * @param aresource -- generally, the filename of the resource.
	 * @return URL to a loadable resource or MissingResourceException is thrown.
	 * @throws MissingResourceException if not found
	 * @see org.safs.jvmagent.AgentClassLoader#AgentClassLoader(String)
	 */
	public static URL getUniversalResourceURL(Class clazz, String aresource){
		ClassLoader gcdloader = clazz.getClassLoader();
		IndependantLog.info("GCD.Universal ClassLoader:"+ gcdloader.toString()); // sun.misc.Launcher$AppClassLoader@11b86e7
		
		URL domain = clazz.getProtectionDomain().getCodeSource().getLocation();
		IndependantLog.info("GCD.Universal CodeSoure.Location Ptcl:"+ domain.getProtocol()); // file
		IndependantLog.info("GCD.Universal CodeSoure.Location Path:"+ domain.getPath()); // file:/c:/pathTo/lib/safs.jar
		IndependantLog.info("GCD.Universal CodeSoure.Location File:"+ domain.getFile()); // file:/c:/pathTo/lib/safs.jar

		URL jom =  clazz.getResource(aresource);
		if (jom == null) { //is null
			IndependantLog.info("GCD.Universal trying getClassLoader().getResource()");
			jom = gcdloader.getResource(aresource);
		}
		if (jom == null) { // is null
			IndependantLog.info("GCD.Universal trying getClassLoader().getSystemResource()");
			jom = gcdloader.getSystemResource(aresource);
		}
		if (jom == null) { // is null
			IndependantLog.info("GCD.Universal trying ClassLoader.getSystemClassLoader().getResource()");
			jom = ClassLoader.getSystemClassLoader().getResource(aresource);
		}
		if (jom == null) { // is null
			IndependantLog.info("GCD.Universal trying ClassLoader.getSystemClassLoader().getSystemResource()");
			jom = ClassLoader.getSystemClassLoader().getSystemResource(aresource);
		}
		ClassLoader contextloader = Thread.currentThread().getContextClassLoader();
		if (jom == null) { // is null
			IndependantLog.info("GCD.Universal trying contextloader getResource().");
			jom = contextloader.getResource(aresource);
			// !!! FINALLY WORKS !!! (as long as resource is in RFT project root directory :(
		}
		if(jom == null){ 
			IndependantLog.info("GCD.Universal trying contextloader getSystemResource().");
			jom = contextloader.getSystemResource(aresource);
		}
		if (jom == null){
			IndependantLog.info("GCD.Universal: trying AgentClassLoader with java.class.path="+ System.getProperty("java.class.path"));
			AgentClassLoader loader = new AgentClassLoader(System.getProperty("java.class.path"));
			jom = loader.getResource(aresource);
		}
		if (jom == null){
			IndependantLog.debug("GCD.Universal: dumping System Properties and throwing MissingResourceException for "+ aresource);
			java.util.Properties props = System.getProperties();
			java.util.Enumeration names = props.keys();
			String _name = null;
			while(names.hasMoreElements()){
				_name = (String) names.nextElement();
				IndependantLog.debug("    "+_name+"="+ System.getProperty(_name));
			}
			throw new java.util.MissingResourceException(aresource,aresource,aresource);
		}
		return jom;
	}

	/**
	 * Attempt to locate the URL of a resource.
	 * This can be in the JAR file containing a specified class, 
	 * or in the directory containing the JAR file.
	 * Multitple searches attempted including:
	 * <ol>
	 * <li> getUniversalResourceURL
	 * <li> AgentClassLoader.getResource (using SAFSDIR pathTo/safs.jar)
	 * </ol>
	 * @param clazz -- Class associated with the resource -- mapping to the JAR or directory resource might be found.
	 * @param aresource -- generally, the filename of the resource.
	 * @return URL to a loadable resource or MissingResourceException is thrown.
	 * @throws MissingResourceException if not found
	 * @see #getUniversalResourceURL(Class, String)
	 * @see org.safs.jvmagent.AgentClassLoader#AgentClassLoader(String)
	 */
	protected static URL getResourceURL(Class clazz, String aresource){
		URL jom = null;
		try{ jom = getUniversalResourceURL(clazz, aresource);}
		catch(MissingResourceException ignore){}
		if (jom == null){
			IndependantLog.info("GCD: trying AgentClassLoader with SAFSDIR Env...");
			String safsjar = System.getenv("SAFSDIR")+ File.separator +"lib"+ File.separator +"safs.jar";
			IndependantLog.info("    "+ safsjar);
			AgentClassLoader loader = new AgentClassLoader(safsjar);
			jom = loader.getResource(aresource);
		}
		if (jom == null){
			IndependantLog.debug("GCD: throwing MissingResourceException for "+ aresource);
			throw new java.util.MissingResourceException(aresource,aresource,aresource);
		}
		return jom;
	}
	
	public static Properties typesmap(){
		if(classesmap==null) classmap(); //just insure it is initialized once.
		if (classtypesmap == null) {
			try{
				URL jtm = getResourceURL(GuiClassData.class, DEFAULT_OBJECT_TYPES_MAP );
				URL customjtm = AgentClassLoader.findCustomizedJARResource(jtm, DEFAULT_OBJECT_TYPES_MAP);
				InputStream in = jtm.openStream();
				classtypesmap = new Properties();
				IndependantLog.info("GCD.loading object types mapping from "+ jtm.getPath());
				classtypesmap.load(in);
				in.close();
				in = null;
				
				if(customjtm != null){
					in = customjtm.openStream();
					Properties temp = new Properties();
					IndependantLog.info("GCD.loading custom object types mapping from "+ customjtm.getPath());
					temp.load(in);
					classtypesmap.putAll(temp);
					in.close();
					in = null;
					temp.clear();
					temp = null;
				}
				
				// see if there are local customizations defined
				try{
					Properties custtypesmap = null;
					in = ClassLoader.getSystemResourceAsStream( CUSTOM_OBJECT_TYPES_MAP );
					if(in == null){
						try{
							String safsdir = System.getenv(DriverConstant.SYSTEM_PROPERTY_SAFS_DIR);
							File custurl = new CaseInsensitiveFile(safsdir + File.separator +"lib"+ File.separator +CUSTOM_OBJECT_TYPES_MAP).toFile();
							if(custurl.isFile()) in =custurl.toURL().openStream();
						}catch(MissingResourceException x){
							IndependantLog.info("GCD.ignoring missing custom object types mapping for "+ CUSTOM_OBJECT_TYPES_MAP);
						}catch(MalformedURLException x){
							IndependantLog.info("GCD.ignoring malformed URL mappings for "+ CUSTOM_OBJECT_TYPES_MAP);
						}
					}
					if (in != null) {
						custtypesmap = new Properties();
						IndependantLog.info("GCD.merging custom object types mapping from "+ CUSTOM_OBJECT_TYPES_MAP);
						custtypesmap.load(in);
						in.close();
						in = null;
						if (custtypesmap.size() > 0) classtypesmap.putAll(custtypesmap);
						custtypesmap.clear();
						custtypesmap = null;					
					}
				}
				catch(Exception anye){
					IndependantLog.error("Error loading CustomObjectTypesMap.dat resource.", anye);
				}
				
			}catch(Exception ex){
				try{				
					//If ex.getMessage() return null, for some java sdk, println(null) will throw NullPointerException
					System.err.println(ex.getMessage());				
					IndependantLog.error(ex.getMessage(), ex);
				}catch(Exception e){
					System.err.println(e);					
				}				
			}
		}
		return classtypesmap;
	}
	
	
	public static Properties classmap(){		
		if (classesmap == null) {
			try{
				URL jom = getResourceURL(GuiClassData.class, DEFAULT_JAVA_OBJECTS_MAP);
				URL customjom = AgentClassLoader.findCustomizedJARResource(jom, DEFAULT_JAVA_OBJECTS_MAP);

				IndependantLog.info("GCD.loading standard object class mapping from "+jom.getPath());
				InputStream in = jom.openStream();
				classesmap = new Properties();
				classesmap.load(in);
				in.close();
				in = null;
				
				if(customjom != null){
					in = customjom.openStream();
					Properties temp = new Properties();
					IndependantLog.info("GCD.merging custom object class mapping from "+customjom.getPath());
					temp.load(in);
					classesmap.putAll(temp);
					in.close();
					in = null;
					temp.clear();
					temp = null;
				}
				// see if there are local customizations defined
				// this may now be obsolete if the AgentClassLoader stuff above works.
				try{
					Properties custclassmap = null;
					in = ClassLoader.getSystemResourceAsStream( CUSTOM_JAVA_OBJECTS_MAP );
					if(in == null){
						try{
							String safsdir = System.getenv(DriverConstant.SYSTEM_PROPERTY_SAFS_DIR);
							File custurl = new CaseInsensitiveFile(safsdir + File.separator +"lib"+ File.separator +CUSTOM_JAVA_OBJECTS_MAP).toFile();
							if(custurl.isFile()) in = custurl.toURL().openStream();
						}catch(MissingResourceException x){
							IndependantLog.info("GCD.ignoring missing custom object mappings for "+ CUSTOM_JAVA_OBJECTS_MAP);						
						}catch(MalformedURLException x){
							IndependantLog.info("GCD.ignoring malformed URL mappings for "+ CUSTOM_JAVA_OBJECTS_MAP);
						}
					}
					if(in != null){
						custclassmap = new Properties();
						IndependantLog.info("GCD.merging custom object mappings from "+ CUSTOM_JAVA_OBJECTS_MAP);
						custclassmap.load(in);
						in.close();
						in = null;
						if (custclassmap.size() > 0) classesmap.putAll(custclassmap);
						custclassmap.clear();
						custclassmap = null;					
					}
				}
				catch(Exception anye){
					IndependantLog.error("Error loading CustomJavaObjectsMap.dat resource:"+ anye.getMessage(),
                              anye);
				}
				
				// init and fill Hashtables
				classassigns = new Hashtable(25);

				String aclass    = null;
				String classtype = null;
				Vector classlist = null;
				
				Enumeration classes = classesmap.propertyNames();
				
				while (classes.hasMoreElements()){
					
					aclass    = (String) classes.nextElement();
					
					// may have a comma-separated list
					classtype = classesmap.getProperty(aclass).toUpperCase(); 					

					StringTokenizer toker = new StringTokenizer(classtype, DEFAULT_TYPE_SEPARATOR);
					String aclasstype = null;
					
					// should always have at least 1 token
					while(toker.hasMoreTokens()){
						aclasstype = toker.nextToken().trim();
						classlist = (Vector) classassigns.get(aclasstype);
						if (classlist == null){
							classlist = new Vector(10,10);
							classlist.addElement(aclass);
							classassigns.put(aclasstype, classlist);
						}
						else{
							classlist.addElement(aclass);
						}
					}
				}				
				// trim all Vectors to remove empty elements				
				classes = classassigns.keys();
				while(classes.hasMoreElements()){
					aclass = (String)classes.nextElement();
					classlist = (Vector)classassigns.get(aclass);
					classlist.trimToSize();
					IndependantLog.info(aclass +": "+ classlist);
				}
			}catch(Exception ex){
				try{
					//If ex.getMessage() return null, for some java sdk, println(null) will throw NullPointerException
					System.err.println(ex.getMessage());				
					IndependantLog.error(ex.getMessage(), ex);
				}catch(Exception e){
					System.err.println(e);					
				}
			}
		}						
		if(classtypesmap==null) typesmap(); 
		return classesmap;
	}
	
	/**
	 * Retrieves the class Type we have stored for the provided class name (if any).
	 * We will cycle through all possible superclasses (except Object) looking for 
	 * a match to a superclass.
	 * <p>
	 * Tool-dependent subclasses will most likely have to subclass this class and 
	 * provide similar mechanisms for evaluating the class hierarchy.
	 * 
	 * @param classname the actual classname sought as a known class type.
	 * 
	 * @param obj the object we are going to evaluate for "type"
	 * 
	 * @return the class Type (i.e. JavaPanel) for the provided class name (i.e. javax.swing.JPanel).
	 *          <CODE>null</CODE> if no mapped type is found.  The classtype may be 
	 *          returned as a comma-separated list of all types supported for the class.
	 * 
	 * @author AUG 14, 2013 Refactored to support recursive and allowGeneric options.
	 * 
	 * @see #getMappedClassType(String, Object, boolean, boolean)
	 */
	public String getMappedClassType(String classname, Object theObject){ 
		return getMappedClassType(classname, theObject, true, true);
	}
	
	/**
	 * Retrieves the class Type we have stored for the provided class name (if any).
	 * We will cycle through all possible superclasses (except Object) looking for 
	 * a match to a superclass if recursive is true.
	 * <p>
	 * Tool-dependent subclasses will most likely have to subclass this class and 
	 * provide similar mechanisms for evaluating the class hierarchy.
	 * 
	 * @param classname the actual classname sought as a known class type.
	 * 
	 * @param obj the object we are going to evaluate for "type"
	 * 
	 * @param recursive true to look for superclass Type matches. false for only a direct class=type match.
	 * 
	 * @param allowGeneric true to allow Type=Generic if no match is found for the provided classname.
	 *                     false -- no Generic Type will be returned if the classname does not map.
	 * 
	 * @return the class Type (i.e. JavaPanel) for the provided class name (i.e. javax.swing.JPanel).
	 *          <CODE>null</CODE> if no mapped type is found.  The classtype may be 
	 *          returned as a comma-separated list of all types supported for the class.
	 * 
	 * @author OCT 31, 2005 Carl Nagle Enhanced to allow a single class to be mapped to multiple 
	 *                              object types via comma-separated list.
	 * @author AUG 14, 2013 Carl Nagle Refactored to support recursive and allowGeneric options. 
	 */
	public String getMappedClassType(String classname, Object theObject, boolean recursive, boolean allowGeneric)
	{
		if(classname == null){
			IndependantLog.info("GCD classname: null, returning null mapped classtype.");
			return null;
		}
		String typeclass = classmap().getProperty(classname, null);

		if (typeclass == null && recursive) {
			
			try{ 
                IndependantLog.info("GCD:classname: "+classname+", typeclass: "+typeclass);
				Class theClass = theObject.getClass();
				Class superClass  = null;
								
				Object baseObject = new Object();
				String basename   = baseObject.getClass().getName();

				while ((theClass != null ) &&
				       (typeclass == null))  {
					
					superClass = theClass.getSuperclass();
					classname  = superClass.getName();					
					theClass = null;
					
					// don't handle top level Object class
					if (classname.equals(basename)){
						superClass = null;
						break;
					}
					
					typeclass = classmap().getProperty(classname, null);

					if (typeclass == null){						
						theClass = superClass;
						superClass = null;
					}
					else{
						superClass = null;
					}
				}								
			}
			catch(Exception ex) { 				
				IndependantLog.error("GCD.getMappedClassType "+ ex.getMessage() +" "+ ex.getClass().getName(), 
				           ex);
			}
		}		
		if (typeclass == null && allowGeneric) typeclass = DEFAULT_CLASS_TYPE; 
        IndependantLog.info("GCD:classname: "+classname+", typeclass: "+typeclass);
		return typeclass;
	}
	

	/**
	 * See if the comma-separated list of possible class types contains the one type 
	 * we are looking for. The comparison is NOT case-sensitive.
	 */
	protected static boolean classtypeContainsClassType(String classtypes, String atype){
		try{
			StringTokenizer toker = new StringTokenizer(classtypes, DEFAULT_TYPE_SEPARATOR);
			String atoken = null;
			while(toker.hasMoreTokens()){
				atoken = toker.nextToken().trim();
				if(atype.equalsIgnoreCase(atoken)) return true;
			}
		}
		catch(NullPointerException npe){ }
		return false;
	}
	
	
	/**
	 * Check if our soughtClass is equivalent to a known class type.
	 * We first loop through a predefined list mapping classes to class types.
	 * 
	 * @param soughtType the Type= identifier we are seeking.
	 * @param soughtClass the actual classname we are trying to type match.
	 * 
	 * @return <CODE>true</CODE> if the class or a superclass is a mapped type.
	 *         <CODE>false</CODE> if it is not.
	 */
	public static boolean isMatchedType(String soughtType, String soughtClass){
		
		if (classassigns == null) return false;

		// get the Vector of potential classes matching Type
		Vector classlist = (Vector) classassigns.get(soughtType.toUpperCase());
		if (classlist == null) return false;
		return classlist.contains(soughtClass);
	}


	/**
	 * See if our object is a subclass of a known class type.
	 * We do the latter via getMappedClassType.
	 * <p>
	 * Tool-dependent subclasses will override this method to handle their 
	 * unique object proxies.
	 * 
	 * @param soughtType the Type= identifier we are seeking.
	 * @param soughtClass the actual classname we are trying to type match.
	 * 
	 * @return <CODE>true</CODE> if the class or a superclass is a mapped type.
	 *         <CODE>false</CODE> if it is not.
	 */
	public boolean isAssignableFrom(String soughtType, String soughtClass, Object theObject){
		
		String theTypes = null;
		
		theTypes = getMappedClassType(soughtClass, theObject);				
		if (theTypes == null) return false;
		
		if (classtypeContainsClassType(theTypes, soughtType)) return true;
		return false;
	}

	
	/**
	 * Subclasses should override this to insure proper invocation of the overridden 
	 * functions.<br>
	 * This routine merely calls:<br>
	 * <p>
	 * isMatchingType   - usually not overridden
	 * isAssignableFrom - usually overridden by tool-dependent subclasses.
	 **/
	public boolean isMatched(String soughtType, String soughtClass, Object theObject){
		
		boolean classmatch = isMatchedType(soughtType, soughtClass) ? true:
		                     isAssignableFrom(soughtType, soughtClass, theObject);
		return classmatch;
	}
	
	
	/**
	 * Returns the case-sensitive, generic object type for the given classType.
	 * classType should be provided as a result of a call to getMappedClassType.  
	 * However, the call to getMappedClassType can return a comma-separated list.  
	 * We will always return the object map of the first classtype in such a list.
	 * <p>
	 * classType is also case-sensitive, so it should be used from getMappedClassType 
	 * unmodified.
	 * <p>
	 * This object type is used to instance particular classes of Component Function 
	 * libraries.  A single library may handle several subtypes or even similar 
	 * types in different domains (Web, Java, etc.)
	 * <p>
	 * @param classType getMappedClassType value.  If a comma-separated list is 
	 * provided then we will only return the object type of the first item in the 
	 * list.
	 * <p>
	 * @return String generically mapped object type for classType.  If classType 
	 * is null or some other problem occurs we will generically return the 
	 * DEFAULT_OBJECT_TYPE string.
	 * 
	 * @author Carl Nagle OCT 31, 2005 modified to accept comma-separated classtype
	 **/
	public static String getGenericObjectType(String classType){
		if (classType == null) return DEFAULT_OBJECT_TYPE;
		StringTokenizer toker = new StringTokenizer(classType, DEFAULT_TYPE_SEPARATOR);
		while(toker.hasMoreTokens()){
			String firsttoken = toker.nextToken().trim();
			String libraryClassName = typesmap().getProperty(firsttoken);
			if(libraryClassName==null){
				IndependantLog.info("GCD.getGenericObjectType(): Type "+firsttoken+" is not mapped to a library name in file ObjectTypesMap.dat");
			}else{
				return libraryClassName;
			}
		}
		return DEFAULT_OBJECT_TYPE;
	}
	
	public static boolean isContainerType(String mappedClassType){
		try{
	        for(int i=0; i< CONTAINER_TYPES.length; i++) 
	            if(classtypeContainsClassType(mappedClassType, CONTAINER_TYPES[i])) return true;
		}catch(NullPointerException x){;}
        return false;
	}

	public static boolean isPopupMenuClass(String classname){
		try{
	        for(int i=0; i< POPUP_MENU_CLASSES.length; i++) 
	            if(POPUP_MENU_CLASSES[i].equals(classname)) return true;
		}catch(NullPointerException x){;}
        return false;
	}
	
	public static boolean isToolTipContainerType(String mappedClassType){
		try{
	        for(int i=0; i< TOOLTIP_CONTAINER_TYPES.length; i++) 
	            if(classtypeContainsClassType(mappedClassType, TOOLTIP_CONTAINER_TYPES[i])) return true;
		}catch(NullPointerException x){;}
        return false;
	}

    public static boolean isAltNameType(String type) {
        try{
            for(int i=0; i < ALT_NAME_TYPES.length; i++) 
	            if(classtypeContainsClassType(type, ALT_NAME_TYPES[i])) return true;
		}catch(NullPointerException x){;}
        return false;
    }	

	/** GuiClassData supports multiple 'types' for each class.  That is given class 
	 * might be mappable to more than one type (or one type might be appropriate 
	 * for one class).  Example: mapping to "JavaPanel, Panel".
	 * This routine attempts to return one mapped type based on multiple mapped 
	 * types and the provided domain.  The logic relies on the convention that 
	 * the object type with the domain in its name is the most suitable match or 
	 * an object type with no domain in its name is next best.
	 * @param _domain "Java", "Win", "Html", or "Java;Html;Flex" etc...
	 * @param _type potentially comma-separated list of possible types.
	 *  Must not be null.
	 * @return String best matching type.
	 */
	public static String deduceOneClassType(String _domain, String _type){
		String result = null;		
		if(_type.indexOf(',')>-1){
			StringTokenizer typeTokens = new StringTokenizer(_type, ", ");
			String t = null;
			String tuc = null;
			StringTokenizer domainTokens = new StringTokenizer(_domain.toUpperCase().trim(),DriverConstant.DOMAIN_SEPARATOR);
			while((typeTokens.hasMoreTokens())&&(result==null)){
				t=typeTokens.nextToken().trim();
				tuc=t.toUpperCase();
				//As _domain maybe more than one domain, it will be separated by ; so we should check
				//each of them.
				while(domainTokens.hasMoreTokens()){
					//if the type startswith the domain use that
					if(tuc.indexOf(domainTokens.nextToken())==0){
						result = t;
						break;
					}
				}
					
				//otherwise see if the type is domainless (generic)
				if(result==null){
					if(tuc.indexOf(DriverConstant.JAVA_CLIENT_TEXT.toUpperCase())==0) continue;
					if(tuc.indexOf(DriverConstant.HTML_CLIENT_TEXT.toUpperCase())==0) continue;
					if(tuc.indexOf(DriverConstant.NET_CLIENT_TEXT.toUpperCase())==0) continue;
					if(tuc.indexOf(DriverConstant.SWT_CLIENT_TEXT.toUpperCase())==0) continue;
//					if(tuc.indexOf(DriverConstant.WIN_CLIENT_TEXT.toUpperCase())==0) continue;  // uh-oh "WINDOW"
					result = t;
				}
			}
		}
		return (result==null) ? _type:result;
	}		
	/** 
	 * GuiClassData supports multiple 'types' for each class.  That is given class 
	 * might be mappable to more than one type (or one type might be appropriate 
	 * for one class).  Example: mapping to "JavaPanel, Panel".
	 * This routine attempts to return these multiple class types as an array. 
	 * @param _type potentially comma-separated list of types retrieved from getMappedClassType.
	 *  Must not be null.
	 * @return String[] of types.  
	 * Can be an array of only 0 items on input error. 
	 * @see #getMappedClassType(String, Object)
	 */
	public static String[] getTypesAsArray(String _type){
		if(_type==null) return new String[0];
		String[] result = _type.split(",");
		for(int i=0;i<result.length;i++) result[i]=result[i].trim();
		return result;
	}
}

