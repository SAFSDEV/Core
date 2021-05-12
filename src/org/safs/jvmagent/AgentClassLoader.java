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
package org.safs.jvmagent;

import java.io.*;
import java.net.*;
import java.security.ProtectionDomain;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.jar.*;

import org.safs.IndependantLog;


/** 
 * Provide a class loader that loads from a custom path.  The original intention 
 * is to provide a ClassLoader for an Assistive Technologies Java Extension.  The 
 * new Bootstrap mechanism for Java JVMs does not have access to the system 
 * CLASSPATH while extensions are being loaded.
 * <p>
 * This classloader doesn't do the security checks normally done by the default ClassLoaders.
 * If path given is null, uses java.class.path.
 * @see <A href="http://java.sun.com/j2se/1.4.2/docs/guide/access/index.html" target="_sundoc">Java Accessibility</A>
 * @see Bootstrap
 * @see org.safs.rmi.engine.AgentImpl
 * @see org.safs.abbot.jvmagent.JVMAgent
 * @author Carl Nagle 2005.01.06 Original Release
 * @author Carl Nagle 2005.01.20 Converted static functions to instance functions.
 * @author Lei Wang 2013.02.07 Use IndependantLog instead of Log to log message.
 */
public class AgentClassLoader extends java.lang.ClassLoader {

    private String classPath = null;
    private ClassLoader parentLoader = null;
    
    /** Default path separators for tokenizers: ":;" */
    public static final String DEFAULT_PATH_SEP = ":;";

	/** convenient File.pathSeparator */
	protected static final String PSEP = File.pathSeparator;

	private File[] files = new File[0];
		

    /*************************************************************** 
     * Create a class loader that loads classes from the given search paths. 
     * The search paths are in typical CLASSPATH format.  Though Windows 
     * file separators '\' may need to be reversed to prevent character 
     * escaping:
     * 
     *    C:/MyApp.JAR;C:/ChildDir/Another.JAR
     * 
     * Unix paths should not have this problem.
     * 
     * Using this constructor is the same as using the preferred constructor:
     * 
     *    AgentClassLoader( path, null);
     */
    public AgentClassLoader(String path) {
        this(path, null);
    }


    /***************************************************************
     * Preferred constructor creates a class loader that loads classes from the 
     * given search paths.  Will also refer to any parent ClassLoader as necessary.
     * 
     * The search paths are in typical CLASSPATH format.  Though Windows 
     * file separators '\' may need to be reversed to prevent character 
     * escaping:
     * 
     *    C:/MyApp.JAR;C:/ChildDir/Another.JAR
     * 
     * Unix paths should not have this problem.
     */
    public AgentClassLoader(String path, ClassLoader parent){
        super(parent);
        parentLoader = parent != null ? parent: getClass().getClassLoader();
        classPath = path != null ? path : System.getProperty("java.class.path");
        parseClassPath();
    }


	/**
	 * Parse the classPath so that only JARs, ZIPs, and valid Directories are retained.
	 */
	private void parseClassPath(){
    	File[] tmpfiles = convertPathToFiles(classPath, DEFAULT_PATH_SEP);
    	File[] goodfiles = new File[tmpfiles.length];
    	int filematch = 0;
    	File afile = null;
    	String path = null;
    	String lcpath = null;
    	for(int i=0; i < tmpfiles.length; i++){
    		afile = tmpfiles[i];
    		if (! afile.exists()) continue;
    		path = afile.getAbsolutePath();
    		lcpath = path.toLowerCase();
    		if (afile.isFile()){
    			if ((! lcpath.endsWith(".zip"))&&
    			    (! lcpath.endsWith(".jar"))) continue;
    			goodfiles[filematch++] = afile;
    			
    		}else if (afile.isDirectory()){
    			goodfiles[filematch++] = afile;
    		}
    	}
    	files = new File[filematch];
    	for (int i=0; i< filematch; i++) files[i] = goodfiles[i];
	}


	/**
	 * Cycle through the chain of parent ClassLoaders before resorting to ours.
	 * Overrides the superclass implementation.
	 */
    protected synchronized Class loadClass(String name, boolean resolve) 
        throws ClassNotFoundException {
        
        Class c = findLoadedClass(name);
        
        if (c == null){
	        try{ c = findSystemClass(name);}
	        catch(Throwable x){;}
        }
        if (c == null){
			try{ c = parentLoader.loadClass(name); }
			catch(Throwable x){;}        
        }
        if (c == null){
			try{ c = findClass(name); }
			catch(Exception x){;}        
        }
                
        if (c == null) throw new ClassNotFoundException(name);
        if (resolve) resolveClass(c);
        return c;
    }


	/**
	 * Read in all bytes of the InputStream until EOF is reached then return the 
	 * complete array of bytes.  An empty array of bytes will be 
	 * returned if an error is encountered such as a null InputStream or an IOException.
	 */
	private byte[] getBytes(InputStream in){
		final int EOF = -1;
		byte[] buffer = new byte[1024];
		byte[] store = new byte[0];
		int bytesRead = 0;
		try{
			do{
				bytesRead = in.read(buffer);
				if (bytesRead > 0) {
					byte[] tmpstore = new byte[store.length + bytesRead];
					int i =0;
					int l = store.length;
					for(; i<l; i++) 
					    tmpstore[i] = store[i];
					for(; i<tmpstore.length;i++)
					    tmpstore[i] = buffer[i-l];
					store = new byte[l+bytesRead];
					store = (byte[]) tmpstore.clone();
				}
			}while(bytesRead != EOF);
			try{in.close();}catch(IOException cio){;}
		}
		catch(Exception io){store = new byte[0];}
		return store;
	}


	/**
	 * Extracts or retrieves a named resource/class into a temporary local 
	 * file location and returns a URL reference to that temporary file.
	 */
    private URL findURL(String name) throws ClassNotFoundException {
    	File afile = null;
    	File tfile = null;
    	String path = null;
    	String lcpath = null;
    	JarFile jfile = null;
    	JarEntry entry = null;

    	for(int i=0; i < files.length; i++){
    		afile = files[i];
    		// isFile means it is a JAR or ZIP file
    		if (afile.isFile()){
    			// open the zip/jar file and get the file
    			try{
    				jfile = new JarFile(afile);
    				entry = jfile.getJarEntry(name);
					BufferedInputStream in = new BufferedInputStream(jfile.getInputStream(entry));
    				// save to temp?
    				byte[] bytes = getBytes(in); //closed by getBytes
    				File temp = File.createTempFile("safsloaded", null);
    				temp.deleteOnExit();
    				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
    				out.write(bytes, 0, bytes.length);
    				out.flush();
    				out.close();
    				return temp.toURL();
    			}
    			catch(Exception io){continue;}
    		}
    		if (afile.isDirectory()){
    			tfile = new File(afile, name);
    		}
    		try{
    			if (! tfile.exists()) continue;
    			if (! tfile.isFile()) continue;
    			return tfile.toURL();
    		}
    		catch(Exception np){continue;}
    	}
    	
    	throw new ClassNotFoundException(name);
    }
    

	/**
	 * Locates the named class from known JARs, ZIPs, or Directories and returns an 
	 * InputStream to the (file) so that the byte[] can be retrieved.
	 */
    private InputStream findInputStream(String name) throws ClassNotFoundException {
    	File afile = null;
    	File tfile = null;
    	JarFile jfile = null;
    	JarEntry entry = null;
    	for(int i=0; i < files.length; i++){
    		afile = files[i];
    		if (afile.isFile()){
    			// isFile means it is a JAR or ZIP file
    			try{
    				jfile = new JarFile(afile);
    				entry = jfile.getJarEntry(dotConvertClassName(name));
    				return new BufferedInputStream(jfile.getInputStream(entry));
    			}
    			catch(Exception io){continue;}
    		}
    		if (afile.isDirectory()){
    			tfile = new File(afile, dotConvertClassName(name));
    		}
    		try{
    			if (! tfile.exists()) continue;
    			if (! tfile.isFile()) continue;
    			return new BufferedInputStream(new FileInputStream(tfile));
    		}
    		catch(Exception np){;}
    	}
    	
    	throw new ClassNotFoundException(name);
    }
    

	/**
	 * Attempts to open an InputStream with findInputStream and then return the byte[] 
	 * of data with getBytes.
	 */
	private byte[] findIt(String name) throws ClassNotFoundException{
		InputStream in = null;
		byte[] store = new byte[0];
		in = findInputStream(name);
		store = getBytes(in);
		if (store.length == 0) throw new ClassNotFoundException(name);
		return store;
	}


	/**
	 * Attempts standard ClassLoader defineClass with the ProtectionDomain of this 
	 * ClassLoader.
	 */
	private Class defineIt(String name, byte[] bytecode) throws ClassNotFoundException{
		ProtectionDomain domain = getClass().getProtectionDomain();
		try{ return defineClass(name, bytecode, 0, bytecode.length, domain);}
		catch(Exception x){;}
		throw new ClassNotFoundException(name);
	}


	/**
	 * Converts simple Java class names to something the file system can actually find.
	 * Ex:  org.safs.JavaHook  is converted to  org/safs/JavaHook.class
	 */
	private String dotConvertClassName(String name){
		String tpath = name.toString();
		tpath = tpath.replace('.','/');
		return tpath +".class";
	}
	

	/**
	 * Overrides the superclass implementation.  Invokes findIt and defineIt to return 
	 * the desired Class.
	 */
	protected Class findClass(String name) throws ClassNotFoundException{		
		byte[] bytecode = findIt(name);
		return defineIt(name, bytecode);
	}
	

	/**
	 * Overrides the superclass implementation.  Invokes findURL to return the URL.
	 */
	protected URL findResource(String name){		
		try{ return findURL(name); }
		catch(ClassNotFoundException cnf){ return null; }
	}
	

    /** calls convertPathToFilenames(path, DEFAULT_PATH_SEP); */
    public String[] convertPathToFilenames(String path) {
        return convertPathToFilenames(path, DEFAULT_PATH_SEP);
    }

    /** 
     * Convert the given CLASSPATH type string into an array of Files. 
     * Only valid paths are copied into the array.  
     * File.exists() must be true to make it into the array.
     **/
    public File[] convertPathToFiles(String path, String seps) {
        String[] names = convertPathToFilenames(path, DEFAULT_PATH_SEP);
        ArrayList tmpfiles = new ArrayList();
        File afile = null;
        for (int i=0;i < names.length;i++) {
        	afile = new File(names[i]);
            if (afile.exists()) tmpfiles.add(afile);
        }
        return (File[])tmpfiles.toArray(new File[tmpfiles.size()]);
    }


    /** 
     * Convert the given CLASSPATH type string into an array of unique filename paths. 
     * The validity of these filename paths is not verified or checked.
     **/
    public String[] convertPathToFilenames(String path, String seps) {
        if (path == null)
            path = "";
        boolean fixDrives = Platform.isWindows() && seps.indexOf(":") != -1;
        StringTokenizer st = new StringTokenizer(path, seps);
        ArrayList names = new ArrayList();
        while (st.hasMoreTokens()) {
            String fp = st.nextToken();
            // Fix up w32 absolute pathnames
            if (fixDrives && fp.length() == 1 && st.hasMoreTokens()) {
                char ch = fp.charAt(0);
                if ((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')) {
                    fp += ":" + st.nextToken();
                }
            }
            names.add(fp);
        }
        return (String[])names.toArray(new String[names.size()]);
    }

    public String toString() {
        return super.toString() + "\nclassPath=" + classPath + ")";
    }
    
    /**
     * Attempts to locate the system directory where a file or resource 
     * was successfully located.  The URL is assumed to be a successfully 
     * found resource resulting from a call to getResource or getSystemResource. 
     * Since these can be found in the file system or in JAR files we 
     * attempt to identify the directory where the file or JAR was located.
     * We use the URL from the calling class because this Method may end 
     * up using different ClassLoaders and locate resources in different 
     * locations than the one needed by the caller.
     * If the URL does NOT ultimately point to a local system directory 
     * then this routine returns null.
     * @param resource URL to parse for directory information.
     * @return File pointing to containing directory, or null.
     */
    public static File findResourceDirectory(URL url) {
    	try{
	    	String protocol = url.getProtocol();
	        IndependantLog.debug("URL ptcl: '"+ protocol +"'");     // jar
	        IndependantLog.debug("URL path: '"+ url.getPath()+"'"); // file:/C:/SAFS/lib/safs.jar!/org/safs/JavaObjectsMap.dat
	        IndependantLog.debug("URL file: '"+ url.getFile()+"'"); // file:/C:/SAFS/lib/safs.jar!/org/safs/JavaObjectsMap.dat 	        
	        File file = new File(url.getPath());
	        IndependantLog.debug("File path: '"+ file.getPath()+"'");
	        IndependantLog.debug("File isFile: '"+ file.isFile() +"'");  	        
	        IndependantLog.debug("File isDir: '"+ file.isDirectory() +"'");
	        if(file.isDirectory()) return file;  // handle CodeSource.getLocation() usage
	        File directory = file.getParentFile();
	        IndependantLog.debug("Parent path: '"+ directory.getPath()+"'");
	        IndependantLog.debug("Parent isFile: '"+ directory.isFile() +"'");  	        
	        IndependantLog.debug("Parent isDir: '"+ directory.isDirectory() +"'");
	        if(protocol.equals("jar")) {
	        	directory = directory.getParentFile();
	        	if(directory.getPath().startsWith("file:")){
	        		directory = new File(directory.getPath().substring(5));
	    	        IndependantLog.debug("Parent isDir: '"+ directory.isDirectory() +"'");
	    	        IndependantLog.debug("Parent path: '"+ directory.getPath()+"'");
	        	}
	        	while((directory != null)&&(! directory.isDirectory())&& directory.getPath().length()> 0){
	        		directory = directory.getParentFile();
	    	        IndependantLog.debug("Parent isDir: '"+ directory.isDirectory() +"'");
	    	        IndependantLog.debug("Parent path: '"+ directory.getPath()+"'");
	        	}
	        }
	        try{ if (directory.isDirectory()) return directory;}catch(NullPointerException np){;}
	        IndependantLog.debug("AgentClassLoader.findResourceDirectory could NOT determine URL source directory:");
	        IndependantLog.debug("...URL source:"+ url.getPath());
	        return null;
    	}
    	catch(Exception x){
	        IndependantLog.debug(x.getMessage(),x);  
	        return null;
    	}
    }
    
    /**
     * Assuming the URL represents a resource found in a JAR file, this routine 
     * will attempt to return a URL pointing to a user customized resource file 
     * of the given name located in the same directory containing the JAR file.
     * 
     * @param url of existing (JAR'd) resource. 
     * @return URL to a standalone resource on the local file system, or null.
     */
    public static URL findCustomizedJARResource(URL url, String resource){
    	try{
    		File urldir = findResourceDirectory(url);
    		if (urldir.isDirectory()){
    			File newres = new File(urldir, resource);
    			if (newres.exists()&&newres.isFile()) return newres.toURL();
    		}
    	}
    	catch(Exception x){	}
    	return null;
    }
    
    /**
     * Searches the following chain for a resource:
     * <ol>
	 *	<li>Thread.currentThread().getContextClassLoader().getResource(resource);			
	 *	<li>AgentClassLoader.class.getResource(resource);
	 *	<li>ClassLoader.getSystemResource(resource);
	 * </ol>
     * @param resource Name of resource to locate (Ex: "JavaObjectsMap.dat")
     * @return URL to resource
     * @throws MissingResourceException if resource (URL) cannot be found.
     */
	public static URL locateResource(String resource)throws MissingResourceException{
		URL url = null;
		try{ 
			IndependantLog.info("AgentClassLoader try currentThread.getResource");
			url = Thread.currentThread().getContextClassLoader().getResource(resource);}
		catch(Exception x){ IndependantLog.info("ACL ignoring contextClassLoader "+ x.getClass().getSimpleName());}
		if (url == null){ 
			try{
				IndependantLog.info("AgentClassLoader try AgentClassLoader.class.getResource");
				url = AgentClassLoader.class.getResource(resource);}
			catch(Exception x){ IndependantLog.info("ACL ignoring getResource "+ x.getClass().getSimpleName());}
		}
		if (url == null){ 
			try{
				IndependantLog.info("AgentClassLoader try getSystemResource");
				url = ClassLoader.getSystemResource(resource);}
			catch(Exception x){ IndependantLog.info("ACL ignoring getSystemResource "+ x.getClass().getSimpleName());}
		}
		try { 
			IndependantLog.info("AgentClassLoader located resource: "+ url.getPath());
		}
		catch(NullPointerException np){
			String message = "AgentClassLoader could not locate resource: "+ resource;
			IndependantLog.info(message);
			throw new MissingResourceException(message, resource, resource);
		}
		return url;
	}
}
