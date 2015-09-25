/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.annotations;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.safs.Log;
import org.safs.model.tools.EmbeddedHookDriverRunner;
import org.safs.model.tools.EmbeddedHookDriverRunnerAware;
import org.safs.model.tools.Runner;
import org.safs.model.tools.RunnerAware;
import org.safs.model.tools.RuntimeDataAware;
import org.safs.staf.STAFProcessHelpers;
import org.safs.tools.RuntimeDataInterface;

/**
 * Utilities for use by mostly internal classes implementing autoconfigure and execution 
 * modes of JSAFS.
 * 
 * @author canagl
 * @since OCT 15, 2013
 * <br>SEP 25, 2015 CANAGL Added support for AutoConfigureJSAFS classes to identify external RuntimeDataAware classes.
 */
public class Utilities {

	static void debug(String message){
		if (STAFProcessHelpers.hasSTAFHelpers()) Log.debug(message);
		else System.out.println(message); // not sure we want this to go to System.out?
	}
	
	private static boolean isEntryExcluded(String entry, List<String> exclusions){
		try{
			String className = entry.replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');			
			for(String exclude: exclusions){
				if (className.startsWith(exclude)) return true;
			}
		}catch(Exception ignore){}
		return false;
	}
	
	/**
	 * attempts to locate any class resource in the packageName so that the Package can be loaded.
	 * This will get even Packages that have not yet been loaded by any ClassLoader.
	 * @param packageName
	 * @return Package or null if none found or an invalid or unfindable package;
	 */
	private static Package getPackageResource(String packageName){
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Package p = null;
	    assert loader != null;
	    String path = packageName.replace('.', '/');
	    Enumeration<URL> resources = null;	
	    try{
	    	resources = loader.getResources(path);
			if(resources == null) 
				debug("getPackageResource reports NO resources at "+ path);
			else{
				debug("getPackageResource reports valid resources at "+ path);
				String strDirectory = null;
				String filename = null;
				String packagefile = null;
				File file = null;
				URL resource = null;
				Class c = null;
				while( resources.hasMoreElements()){
					resource = resources.nextElement();
					filename = resource.getFile();
					debug("getPackageResource decoding resource "+ filename);
					try{
						strDirectory = URLDecoder.decode(filename, "UTF-8"); 
						debug("getPackageResource processing decoded resource "+ strDirectory);
						if (strDirectory.startsWith("file:") && strDirectory.contains("!")) { 
				        	debug("getPackageResource processing JAR file entry '"+ strDirectory +"'");
							String [] split = strDirectory.split("!"); 
							URL jar = new URL(split[0]); 
							ZipInputStream zip = new ZipInputStream(jar.openStream()); 
							ZipEntry entry = null; 
							String entryName;
							while ((entry = zip.getNextEntry()) != null) {
								entryName = entry.getName();
								if (entryName.endsWith(".class")) { 
									String className = entryName.replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');
									if(!className.startsWith(packageName)) continue;
						        	debug("getPackageResource processing JAR file class '"+ className+"'");
									try{
										c = Class.forName(className, false, loader);
										return c.getPackage();
									}catch(ClassNotFoundException x){
							        	debug("getPackageResource ClassNotFoundException for '"+ className+"'");
									}
								}
							} 
						}
						else {
							file = new File(strDirectory);
							if(file.isDirectory()){
					        	debug("getPackageResource processing Directory '"+ file.getPath() +"'");
								File[] files = file.listFiles();
								for(File entry: files){
						        	debug("getPackageResource evaluating File entry '"+ entry.getName() +"'");
									filename = entry.getName();
						        	if(filename.endsWith(".class")&& !filename.contains("$")){
							        	debug("getPackageResource processing File entry '"+ filename +"'");
										filename = filename.substring(0, filename.length()-6);
										packagefile = packageName +"."+ filename;
						            	debug("getPackageResource attempting to get File URI '"+ packagefile +"'");
										try { 
											c = Class.forName(packagefile, false, loader);
											return c.getPackage();
										}catch( ClassNotFoundException x){
								        	debug("getPackageResource ClassNotFoundException for '"+ packagefile +"'");
										}
						        	}
								}
							}else{
					        	debug("getPackageResource '"+ file.getPath() +"' is NOT seen as a Directory.");
							}
						}
					}catch(UnsupportedEncodingException x){
						debug("getPackageResources UnsupportedEncodingException for "+ strDirectory);
					}					
				}
			}
	    }catch(IOException x){
			debug("getPackageResources IOException using "+ path);
	    }
		return null;
	}
	
	/**
	 * Recursive method used to find all classes in a given directory and subdirs. 
	 * @param directory The base directory 
	 * @param packageName The package name for classes found inside the base directory 
	 * @return The classes 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 **/ 
	private static List<Class<?>> findClasses(File directory, String packageName, List<String> exclusions) 
			throws ClassNotFoundException,
	               IOException,
	               MalformedURLException { 
		List<Class<?>> classes = new ArrayList<Class<?>>(); 
		String strDirectory = directory.getPath();
		if (strDirectory.startsWith("file:") && strDirectory.contains("!")) { 
        	debug("AutoConfigure processing JAR file entry '"+ strDirectory +"'");
			String [] split = strDirectory.split("!"); 
			URL jar = new URL(split[0]); 
			ZipInputStream zip = new ZipInputStream(jar.openStream()); 
			ZipEntry entry = null; 
			String entryName;
			while ((entry = zip.getNextEntry()) != null) {
				entryName = entry.getName();
				if(isEntryExcluded(entryName, exclusions)) {
		        	debug("AutoConfigure skipping excluded JAR file entry '"+ entryName +"'");
					continue;
				}
				if (entryName.endsWith(".class")) { 
					String className = entryName.replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');
					if(!className.startsWith(packageName)) continue;
		        	debug("AutoConfigure adding JAR file class '"+ className+"'");
					classes.add(Class.forName(className)); 
				}
			} 
			return classes;
		} 

		if (!directory.exists()) { 
        	debug("AutoConfigure found package URI '"+ directory.getPath()+"' does NOT exists.");
			return classes; 
		} 
    	debug("AutoConfigure processing Directory '"+ directory.getPath()+"'");
		File[] files = directory.listFiles(); 
		for (File file : files) { 
			String fileName = file.getName(); 
			if (file.isDirectory()) { 
				assert !fileName.contains("."); 
            	if(exclusions.contains(packageName +"."+ file.getName())) continue;
				classes.addAll(findClasses(file, packageName + "." + fileName, exclusions)); 
			} 
			else if (fileName.endsWith(".class") && !fileName.contains("$")) { 
            	debug("AutoConfigure attempting to add class URI '"+ file.getName() +"' to configurable classes.");
				Class<?> _class; 
				try { 
					_class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6)); 
				} 
				catch (ExceptionInInitializerError e) { 
					// happen, for example, in classes, which depend on 
					// Spring to inject some beans, and which fail, 
					// if dependency is not fulfilled 
					_class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6), false, Thread.currentThread().getContextClassLoader()); 
				} 
				classes.add(_class); 
			}
		} 
		return classes; 
	}	
	
    /**	
	 * <p>
	 * The routine will be creating a static copy of each Class, so the classes will go through 
	 * their initial static initialization if this has not already happened.
	 * <p>
	 * @param apackage -- the Package to interrogate.  
	 * Ex: my.test.package
	 * @param cList -- the current List of classes to append to.
	 * @param exclusions -- a List of package names (not classnames) to be excluded from the search.  
	 * Ex: my.test.package.otherstuff
	 * @return The input cList with all newly found Classes appended to it.  
	 * This allows for the collection of many classes across different package hierarchies.
	 */
	private static List<Class<?>> getPackageClasses(Package apackage, List<Class<?>> cList, List<String> exclusions){
		String packageName = apackage.getName();
		debug("getPackageClasses processing package '"+ packageName +"' for configurable classes.");
		if(exclusions.contains(packageName)) return cList;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
	    assert loader != null;
	    String path = packageName.replace('.', '/');
	    Enumeration<URL> resources = null;	
	    List<File> dirs = new ArrayList<File>();
	    try{ resources = loader.getResources(path);}
	    catch(IOException x){
			debug("getPackageClasses IOException for "+ path);
	    }
	    if(resources != null){
	    	String fileName = null;
	    	String fileNameDecoded = null;
	    	URL resource = null;
		    while (resources.hasMoreElements()) {
		        resource = resources.nextElement();
				fileName = resource.getFile();
				debug("getPackageClasses decoding resource "+ fileName);
				try{
					fileNameDecoded = URLDecoder.decode(fileName, "UTF-8"); 
					dirs.add(new File(fileNameDecoded)); 
				}catch(UnsupportedEncodingException x){
					debug("getPackageClasses UnsupportedEncodingException for "+ fileNameDecoded);
				}
		    }
	    }
	    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
	    for (File directory : dirs) {
	        try{ classes.addAll(findClasses(directory, packageName, exclusions)); }
	        catch(Exception nf){
				debug("getPackageClasses "+ nf.getClass().getSimpleName()+" unable to retrieve SubPackage Classes associated with "+packageName);
	        }
	    }
		debug("getPackageClasses found '"+ classes.size() +"' configurable classes.");
		// Attempt to avoid duplicating items already in the List.
		for(Class c:classes) if(!cList.contains(c)) cList.add(c);
	    return cList;
	}
	
	/**
	 * Given a full classname (not a package name) find the parent package 
	 * and attempt to locate all classes in that package and all sub-packages.
	 * <p>
	 * The routine will be creating a static copy of each Class, so the classes will go through 
	 * their initial static initialization if this has not already happened.
	 * <p>
	 * @param classname -- the full name of the class to interrogate.  
	 * Ex: my.test.package.MyTest
	 * @param cList -- the current List of classes to append to.
	 * @param exclusions -- a List of package names (not classnames) to be excluded from the search.  
	 * Ex: my.test.package.otherstuff
	 * @return The input cList with all newly found Classes appended to it.  
	 * This allows for the collection of many classes across different package hierarchies.
	 * @see #getPackageClasses(Package, List, List)
	 */
	public static List<Class<?>> getPackageClasses(String classname, List<Class<?>> cList, List<String> exclusions){
		Class<?> c;
		try{ c = Class.forName(classname);}
		catch(ClassNotFoundException nf){
			debug("AutoConfigure unable to retrieve calling Class "+ classname);
			return cList;
		}		
		Package pack = c.getPackage();
		return getPackageClasses(pack, cList, exclusions);
	}

	/** 
	 * Look for classes (like AppMapGenerated classes) that may want access to a RuntimeDataInterface.
	 * Look in the package of the provided classInstance.
	 * Look in other packages using the altPackageNames array provided by the caller.  We will attempt to 
	 * use these altPackageNames as both subpackages of the parent package and standalone fullpath package 
	 * names.
     * <p>EXPERIMENTAL<p>
	 * It is IMPORTANT to note that this Dependency Injection model will create a temporary and 
	 * non-cached instance of the RuntimeDataAware Class solely for the purpose of invoking the 
	 * setRuntimeDataInterface instance Method.  In order for subsequent instances to make use of the 
	 * RuntimeDataInterface object it is necessary that any and all instances should store it in a 
	 * shared (static) field.
	 * <p>   
	 * This is NOT part of the normal {@link #autoConfigure(String, JSAFSConfiguredClassStore)} process.
	 * That process is separate and independent and may happen in addition to this function.
	 * <p>
	 * @param classInstance -- The class instance--usually the Main class for the test--to use to seek 
	 * associated packages and classes containing RuntimeDataAware classes.
	 * @param altPackageNames -- list of associated package names to search for RuntimeDataAware classes. 
	 * @param dataInterface --  the RuntimeDataInterface to pass along to RuntimeDataAware classes.
	 * 
	 * @see org.safs.model.tools.AppMapGenerator
	 * @see org.safs.model.tools.RuntimeDataAware
	 * @see org.safs.tools.RuntimeDataInterface 
	 * @see org.safs.model.tools.RuntimeDataAware#setRuntimeDataInterface(RuntimeDataInterface)
	 **/
	public static void injectRuntimeDataAwareClasses(Object classInstance, ArrayList<String> altPackageNames, RuntimeDataInterface dataInterface) {
		// given a class, find the package it is in.
		Class myclass = classInstance.getClass();
		debug("injectRuntimeDataAwareClasses received Class "+ myclass);
		Package mypackage = myclass.getPackage();
		ArrayList<String> exclusions = new ArrayList<String>();
		List<Class<?>> cList = getPackageClasses(mypackage, new ArrayList<Class<?>>(), exclusions);
		String mypackagename = mypackage.getName();
		debug("injectRuntimeDataAwareClasses seeking parent Package for "+ mypackagename);
		Package subpackage = null;
		String subpackagename = null;
		// get the parent package of that package.
		int lastdot = mypackagename.lastIndexOf('.');
		if(lastdot > 0){
			// search parent package + rootPackageNames to locate other RuntimeDataAware classes.
			String parentpackagename = mypackagename.substring(0, lastdot);
			debug("injectRuntimeDataAwareClasses processing parent Package "+ parentpackagename);
			Package parentpackage = getPackageResource(parentpackagename);
			if(parentpackage != null){
				debug("injectRuntimeDataAwareClasses retrieving parentpPackage classes for "+ parentpackagename);
				// new -- process the parent package for possible classes before subpackages.
				cList = getPackageClasses(parentpackage, cList, exclusions);
				for(String rootPackageName: altPackageNames){
					subpackagename = parentpackagename+"."+rootPackageName;
					subpackage = getPackageResource(subpackagename);
					if(subpackage != null){
						debug("injectRuntimeDataAwareClasses processing parent subPackage "+ subpackage.getName());
						cList = getPackageClasses(subpackage, cList, exclusions);
					}else{
						debug("injectRuntimeDataAwareClasses detected no parent subPackage "+ rootPackageName );
					}
				}
			}
		}else{
			debug("injectRuntimeDataAwareClasses detected NO parent Package to process for Package "+ mypackagename);
		}
		// also search rootPackageNames as standalone package names to search.
		for(String rootPackageName: altPackageNames){		    
			subpackage = getPackageResource(rootPackageName);
			if(subpackage != null){
				debug("injectRuntimeDataAwareClasses processing standalone Package "+ rootPackageName);
				cList = getPackageClasses(subpackage, cList, exclusions);
			}else{
				debug("injectRuntimeDataAwareClasses detected no standalone Package "+ rootPackageName );
			}
		}
		// check for AutoConfigureJSAFS Annotated classes to enable external RuntimeDataAware dependencies
		List<String> auto_inclusions = new ArrayList<String>();
		List<String> auto_exclusions = new ArrayList<String>();
		for (Class c:cList){
			if(c.isAnnotationPresent(AutoConfigureJSAFS.class)){
				AutoConfigureJSAFS ann = (AutoConfigureJSAFS) c.getAnnotation(AutoConfigureJSAFS.class);
				String include = ann.include();
				String exclude = ann.exclude();
				if(include.length() > 0){
					String[] ps = include.split(";");
					for(String i:ps) auto_inclusions.add(i);					
				}
				if(exclude.length() > 0){
					String[] ps = exclude.split(";");
					for(String i:ps) auto_exclusions.add(i);
				}
			}			
		}
		// add package exclusions to the list before class inclusions
		for(String s:auto_exclusions) if(!exclusions.contains(s)) exclusions.add(s);
		for(String s:auto_inclusions) cList = getPackageClasses(s, cList, exclusions);
		
		// look for RuntimeDataAware classes to instantiate and inject.
		String matchname = RuntimeDataAware.class.getName();
		for(Class c: cList){
			debug("injectRuntimeDataAwareClasses seeking RuntimeDataAwareness for Class "+ c.getName() );
			Class[] interfaces = c.getInterfaces();
			for(Class i: interfaces){
				if(matchname.equals(i.getName())){
					debug("injectRuntimeDataAwareClasses found RuntimeDataAwareness for Class "+ c.getName() );
					try{
						RuntimeDataAware inst = (RuntimeDataAware) c.newInstance();
						inst.setRuntimeDataInterface(dataInterface);
					}catch(InstantiationException x){
						debug("injectRuntimeDataAwareClasses unable to instantiate Class "+ c.getName());
					}catch(IllegalAccessException x){
						debug("injectRuntimeDataAwareClasses illegal access to Class "+ c.getName());
					}
				}
			}
		}
	}
	
	/**
	 * This is where autoconfiguration and execution of JSAFS-specific tests will generally 
	 * occur.  The routine is generally called internally by other JSAFS classes and methods.
	 * <p>
	 * The Class initialized from the className is checked for the AutoConfigureJSAFS 
	 * annotation to see if additional class packages and sub-packages should be included 
	 * or excluded from the configuration and execution.
	 * <p>
	 * The routine will identify and instantiate all the classes in the 
	 * package and all sub-packages relative the inclusion list.  Those Classes  and 
	 * Methods within the Classes containing JSAFS annotations for configuration or 
	 * execution will be configured and prepared for execution--including the proper 
	 * sorting of execution order, if any. 
	 * <p>
	 * All successfully instantiated Object instances are stored for post-test availability.
	 * <p>
	 * After all preparations are complete, any Methods tagged for execution are executed 
	 * in the order as defined by associated annotations.
	 * 
	 * 
	 * @param className -- Initially, the name of the class that is requesting the automatic 
	 * configuration and execution.  For example, the Runner Class will call into here with 
	 * the name of the class that invoked Runner.autorun(). 
	 * 
	 * @param store -- the JSAFSConfiguredClassStore that will maintain references to 
	 * instantiated object instances for later use by the user, if needed.
	 * 
	 * @see org.safs.model.tools.Runner#autorun(String[])
	 * @see org.safs.model.tools.Runner#getConfiguredClassInstance(String)
	 */
	public static void autoConfigure(String className, JSAFSConfiguredClassStore store){
		try {
			Class<?> aClass = Class.forName(className);
			List<String> inclusions = new ArrayList<String>();
			inclusions.add(className);
			List<String> exclusions = new ArrayList<String>();
			if(aClass.isAnnotationPresent(AutoConfigureJSAFS.class)){
				AutoConfigureJSAFS ann = aClass.getAnnotation(AutoConfigureJSAFS.class);
				String include = ann.include();
				String exclude = ann.exclude();
				if(include.length() > 0){
					String[] ps = include.split(";");
					for(String i:ps) inclusions.add(i);
				}
				if(exclude.length() > 0){
					String[] ps = exclude.split(";");
					for(String i:ps) exclusions.add(i);
				}
			}
			List<Class<?>> cList = new ArrayList<Class<?>>();
			for(String classname:inclusions){
				cList = Utilities.getPackageClasses(classname, cList, exclusions);
			}
			
			Class<?>[] cArray = cList.toArray(new Class[cList.size()]);
			Object object;				
			
			Integer classOrder = 0;
			Integer methodOrder = 0;

			//Contains @JSAFSTest tagged method, ordered by classOrder(AutoConfigureJSAFS.Order), Class, and methodOrder(JSAFSTest.Order).
			Hashtable<Integer/*class order*/,Hashtable<Class<?>,Hashtable<Integer/*method order*/, Hashtable<Method,Object>>>> jsafsTest = 
					new Hashtable<Integer, Hashtable<Class<?>,Hashtable<Integer, Hashtable<Method,Object>>>>();
			Hashtable<Class<?>,Hashtable<Integer/*method order*/, Hashtable<Method,Object>>> classesOfSameOrder = null;

			//Contains @JSAFSBefore tagged method, ordered by Class and methodOrder(JSAFSBefore.Order).
			Hashtable<Class<?>,Hashtable<Integer/*method order*/, Hashtable<Method,Object>>> beforeTest = new Hashtable<Class<?>,Hashtable<Integer, Hashtable<Method,Object>>>();
			//Contains @JSAFSAfter tagged method, ordered by Class and methodOrder(JSAFSAfter.Order).
			Hashtable<Class<?>,Hashtable<Integer/*method order*/, Hashtable<Method,Object>>> afterTest = new Hashtable<Class<?>,Hashtable<Integer, Hashtable<Method,Object>>>();
			Hashtable<Class<?>,Hashtable<Integer/*method order*/, Hashtable<Method,Object>>> beforeOrafterTest = null;
			Hashtable<Integer/*method order*/, Hashtable<Method,Object>> methedsOfSameClass = null;
			Hashtable<Method,Object> methodsOfSameOrder = null;
			Hashtable<Method,Object> methods = null;

			// required to fix bug in original code where methedsOfSameClass was being changed 
			// in inner For loop while still in use by outer For loop
			Hashtable<Integer/*method order*/, Hashtable<Method,Object>> methodsOfBeforeAndAfter = null;
			
			for (Class<?> c : cArray) {
				try{ aClass = Class.forName(c.getName());}
				catch(ClassNotFoundException nf){
					debug("AutoConfigure skipping configuring a Class due to "+ nf.getClass().getSimpleName()+": "+nf.getMessage());
					continue;}				
				boolean stored = (store.getConfiguredClassInstance(c.getName()) instanceof Object);
				if(! stored){
					try{
						Constructor<?> cons = aClass.getConstructor();
						object = cons.newInstance();
					}
					catch(InstantiationException nm){
						debug("AutoConfigure skipping Class "+c.getName()+" configuration due to "+ nm.getClass().getSimpleName());
						continue;}
					catch(NoSuchMethodException nm){
						debug("AutoConfigure skipping Class "+c.getName()+" configuration due to "+ nm.getClass().getSimpleName());
						continue;}
					catch(InvocationTargetException nm){
						debug("AutoConfigure skipping Class "+c.getName()+" configuration due to "+ nm.getClass().getSimpleName());
						continue;}
					catch(IllegalAccessException nm){
						debug("AutoConfigure skipping Class "+c.getName()+" configuration due to "+ nm.getClass().getSimpleName());
						continue;}
				}else{
					object = store.getConfiguredClassInstance(c.getName());
				}
				// we now have aClass and object for invocations.
				
				// try some dependency injections BEFORE execution
				debug("AutoConfigure evaluating dependency injection for Class instance "+ c.getName()+" ...");
				if( object instanceof EmbeddedHookDriverRunnerAware && 
					store instanceof EmbeddedHookDriverRunner){
					debug("AutoConfigure injecting the EmbeddedHookDriverRunner into Class instance "+ c.getName()+" ...");
					((EmbeddedHookDriverRunnerAware)object).setEmbeddedHookDriverRunner((EmbeddedHookDriverRunner) store);
				}
				if( object instanceof RunnerAware && 
						store instanceof Runner){
						debug("AutoConfigure injecting the Runner into Class instance "+ c.getName()+" ...");
						((RunnerAware)object).setRunner((Runner) store);
				}
				
				//According to
				if(aClass.isAnnotationPresent(AutoConfigureJSAFS.class))
					classOrder = Utilities.order(aClass.getAnnotation(AutoConfigureJSAFS.class));
				else
					classOrder = AutoConfigureJSAFS.DEFAULT_ORDER;
				debug("AutoConfigure processing Methods for Class instance "+ c.getName()+" ...");
				Method[] allMethods = aClass.getDeclaredMethods();									
				for (Method method : allMethods){
					if (method.isAnnotationPresent(JSAFSTest.class)){
						methodOrder = Utilities.order(method.getAnnotation(JSAFSTest.class));
						debug("AutoConfigure processing JSAFSTest annotated Method "+ method.getName()+" with class-order="+ classOrder+" method-order="+methodOrder);
						
						classesOfSameOrder = jsafsTest.containsKey(classOrder) ? jsafsTest.get(classOrder) : new Hashtable<Class<?>,Hashtable<Integer, Hashtable<Method,Object>>>();
						methedsOfSameClass = classesOfSameOrder.containsKey(aClass) ? classesOfSameOrder.get(aClass) : new Hashtable<Integer, Hashtable<Method,Object>>();
						methodsOfSameOrder = methedsOfSameClass.containsKey(methodOrder) ? methedsOfSameClass.get(methodOrder) : new Hashtable<Method,Object>();
						methodsOfSameOrder.put(method, object);
						methedsOfSameClass.put(methodOrder,methodsOfSameOrder);
						classesOfSameOrder.put(aClass, methedsOfSameClass);
						jsafsTest.put(classOrder, classesOfSameOrder);
						
					}else if(method.isAnnotationPresent(JSAFSBefore.class) || method.isAnnotationPresent(JSAFSAfter.class)){
						if (method.isAnnotationPresent(JSAFSBefore.class)){
							beforeOrafterTest = beforeTest;
							methodOrder = Utilities.order(method.getAnnotation(JSAFSBefore.class));
							debug("AutoConfigure processing JSAFSBefore annotated Method "+ method.getName()+" with order="+ methodOrder);
						}else if (method.isAnnotationPresent(JSAFSAfter.class)){
							beforeOrafterTest = afterTest;
							methodOrder = Utilities.order(method.getAnnotation(JSAFSAfter.class));
							debug("AutoConfigure processing JSAFSAfter annotated Method "+ method.getName()+" with order="+ methodOrder);
						}
						
						methedsOfSameClass = beforeOrafterTest.containsKey(aClass)? beforeOrafterTest.get(aClass) : new Hashtable<Integer, Hashtable<Method,Object>>();
						methodsOfSameOrder = methedsOfSameClass.containsKey(methodOrder) ? methedsOfSameClass.get(methodOrder) : new Hashtable<Method,Object>();
						methodsOfSameOrder.put(method, object);
						methedsOfSameClass.put(methodOrder,methodsOfSameOrder);
						beforeOrafterTest.put(aClass, methedsOfSameClass);
					}
				}
				if(!stored) store.addConfiguredClassInstance(c.getName(), object);
			}
			
			ArrayList<Integer> classOrders = new ArrayList<Integer>(jsafsTest.keySet());
			ArrayList<Class<?>> classes = null;
			ArrayList<Integer> methodOrders = null;
			Collections.sort(classOrders);
			String displayName = null;
			for (Iterator<Integer> i = classOrders.iterator(); i.hasNext();) {
				classesOfSameOrder = jsafsTest.get(i.next());
				if(classesOfSameOrder == null) continue;
				
				classes = new ArrayList<Class<?>>(classesOfSameOrder.keySet());
				//Sort the classes according to their class name
				Collections.sort(classes, new MyComparator(true));
				
				for (Iterator<Class<?>> j = classes.iterator(); j.hasNext();) {
					methedsOfSameClass = classesOfSameOrder.get(j.next());
					if(methedsOfSameClass == null) continue;
					methodOrders = new ArrayList<Integer>(methedsOfSameClass.keySet());
					Collections.sort(methodOrders);
					for (Iterator<Integer> k = methodOrders.iterator(); k.hasNext();) {
						methods = methedsOfSameClass.get(k.next()); 
						if(methods == null) continue;
						for (Method key: methods.keySet()){
							//Execute @JSAFSBefore tagged method
							methodsOfBeforeAndAfter = beforeTest.get(key.getDeclaringClass());
							if(methodsOfBeforeAndAfter != null) executeAnnotatedMethods(methodsOfBeforeAndAfter, true);
	
							displayName = key.getDeclaringClass().getName()+"#"+key.getName();
							Object o = methods.get(key);						
							JSAFSTest test = key.getAnnotation(JSAFSTest.class);
							key.setAccessible(true);
							String testcase = null;
							try{
								if(test != null){
									debug("AutoConfigure executing Method "+ displayName+" with order="+ test.Order());
							    	Runner.logGENERIC(" ", null);
								    try{
									    if(store instanceof Runner){
									    	testcase = test.Name().length()==0 ? displayName:test.Name();
									    	Runner.command("StartTestCase", testcase);
									    }
								    }catch(Throwable t){}
								}else{
									debug("AutoConfigure executing non-JSAFSTest Method "+ displayName +" ...");
								}
								key.invoke(o);
	
								if(test != null){
									debug("AutoConfigure finished Method "+ displayName+" with order="+ test.Order());
								    try{
									    if(store instanceof Runner){
									    	Runner.command("StopTestCase", testcase);
									    	if(test.Summary()){
										    	Runner.command("LogCounterInfo", testcase);
									    	}
									    }
								    }catch(Throwable t){}
								}else{
									debug("AutoConfigure finished non-JSAFSTest Method "+ displayName +" ...");
								}
							}
							catch(InvocationTargetException it){
								debug("AutoConfigure skipping execution of Method "+ displayName +" due to "+ it.getClass().getSimpleName());
								it.getCause().printStackTrace();
								continue;}
							catch(IllegalAccessException it){
								debug("AutoConfigure skipping execution of Method "+ displayName +" due to "+ it.getClass().getSimpleName());
								continue;}
							
							//Execute @JSAFSAfter tagged method
							methodsOfBeforeAndAfter = afterTest.get(key.getDeclaringClass());
							if(methodsOfBeforeAndAfter != null) executeAnnotatedMethods(methodsOfBeforeAndAfter, false);
						}
					}
				}
			}			

		} catch (ClassNotFoundException e) {
			debug("AutoConfigure aborting "+className+" due to "+ e.getClass().getSimpleName());
			e.printStackTrace();
		}
	}
	
	/**
	 * Execute methods stored in a hashtable according to the method's order.
	 * @param annotatedMehods	Hashtable<Integer, Hashtable<Method,Object>> contains methods, key is the method's order
	 * @param ascending,		boolean, True to execute methods in ascending order; False in descending order.
	 */
	static void executeAnnotatedMethods(Hashtable<Integer, Hashtable<Method,Object>> annotatedMehods, final boolean ascending){
		ArrayList<Integer> methodOrders = new ArrayList<Integer>(annotatedMehods.keySet());
		
		//Sort the methods according to their order
		Collections.sort(methodOrders, new MyComparator(ascending));
		
		String displayName = null;
		String msg = null;
		Hashtable<Method,Object> methods = null;
		for (Iterator<Integer> i = methodOrders.iterator(); i.hasNext();) {
			methods = annotatedMehods.get(i.next());
			for (Method key: methods.keySet()){					
				displayName = key.getDeclaringClass().getName()+"#"+key.getName();
				if(key.getAnnotation(JSAFSTest.class)!=null) msg = "@JSAFSTest "+displayName+" with order="+ Utilities.order(key.getAnnotation(JSAFSTest.class)); 
				else if(key.getAnnotation(JSAFSBefore.class)!=null) msg = "@JSAFSBefore "+displayName+" with order="+ Utilities.order(key.getAnnotation(JSAFSBefore.class)); 
				else if(key.getAnnotation(JSAFSAfter.class)!=null) msg = "@JSAFSAfter "+displayName+" with order="+ Utilities.order(key.getAnnotation(JSAFSAfter.class)); 
				debug("AutoConfigure executing Method "+ msg);
				
				Object o = methods.get(key);
				key.setAccessible(true);
				try{ key.invoke(o);}
				catch(InvocationTargetException it){
					debug("AutoConfigure skipping execution of Method "+ displayName +" due to "+ it.getClass().getSimpleName());
					it.getCause().printStackTrace();
					continue;}
				catch(IllegalAccessException it){
					debug("AutoConfigure skipping execution of Method "+ displayName +" due to "+ it.getClass().getSimpleName());
					continue;}
			}				
		}
	}
	
	/**
	 * For annotation having both 'Order' and 'value' field.<br>
	 * Whichever one ('Order'/'value') is NOT at its default value will be used.<br>
	 * If both are at non-default values then 'Order' should be assumed.<br> 
	 * @param anno, Annotation
	 * @return int, the annotation's order
	 * @see AutoConfigureJSAFS
	 * @see JSAFSTest
	 * @see JSAFSBefore
	 * @see JSAFSAfter
	 * 
	 */
	private static int order(Annotation anno){
		int result = AutoConfigureJSAFS.DEFAULT_ORDER;
		int value = 0;
		int order = 0;
		int defaultOrder = 0;
		
		if(anno instanceof AutoConfigureJSAFS){
			value = ((AutoConfigureJSAFS) anno).value();
			order = ((AutoConfigureJSAFS) anno).Order();
			defaultOrder = AutoConfigureJSAFS.DEFAULT_ORDER;
		}else if(anno instanceof JSAFSTest){
			value = ((JSAFSTest) anno).value();
			order = ((JSAFSTest) anno).Order();
			defaultOrder = JSAFSTest.DEFAULT_ORDER;
		}else if(anno instanceof JSAFSBefore){
			value = ((JSAFSBefore) anno).value();
			order = ((JSAFSBefore) anno).Order();
			defaultOrder = JSAFSBefore.DEFAULT_ORDER;
		}else if(anno instanceof JSAFSAfter){
			value = ((JSAFSAfter) anno).value();
			order = ((JSAFSAfter) anno).Order();
			defaultOrder = JSAFSAfter.DEFAULT_ORDER;
		}else{
			return AutoConfigureJSAFS.DEFAULT_ORDER;
		}
		
		if(value!=defaultOrder && order!=defaultOrder){
			result = order;
		}else if(value!=defaultOrder){
			result = value;
		}else if(order!=defaultOrder){
			result = order;
		}else{
			result = defaultOrder;
		}
		
		return result;
	}
	
}

class MyComparator implements Comparator<Object>{
	/**
	 * The order to sort.<br>
	 */
	private boolean ascending = true;
	
	public MyComparator(boolean ascending){
		this.ascending = ascending;
	}
	
	/**
	 * Compare 2 objects.<br>
	 * If they are of the same class and instance of Comparable, call compareTo to compare.<br>
	 * otherwise, use their hash-code to compare.<br>
	 * If the objects are of Class, we want to use their class-name to compare.<br>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public int compare(Object lhs, Object rhs) {
		int result = 0;
		if(lhs instanceof Comparable &&
		   rhs instanceof Comparable &&
		   lhs.getClass().getName().equals(rhs.getClass().getName())){
			result = ((Comparable)lhs).compareTo(rhs);
		}else if(lhs instanceof Class && rhs instanceof Class){
			result = ((Class<?>) lhs).getName().compareTo(((Class<?>)rhs).getName());
		}else{
			result = lhs.hashCode() - rhs.hashCode();
		}
		return ascending? result:-result;
	}
}