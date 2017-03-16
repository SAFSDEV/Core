/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * APR 22, 2016    (Lei Wang) Initial release.
 * SEP 06, 2016    (Lei Wang) Added method compile(): compile java/groovy source code at runtime.
 * DEC 12, 2016    (Lei Wang) Added method getMapValue().
 * MAR 16, 2017    (Lei Wang) Added method getArray() and parseValue().
 */
package org.safs;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.safs.natives.NativeWrapper;
import org.safs.persist.Persistable;

public class Utils {
	/** '.java' the suffix of java source code. */
	public static final String FILE_SUFFIX_JAVA 				= ".java";
	/** '.groovy' the suffix of groovy source code. */
	public static final String FILE_SUFFIX_GROOVY 				= ".groovy";
	/** 'src' the sub-folder containing java/groovy source code. */
	public static final String DEFAULT_SOURCE_DIRECTORY 		= "src";
	/** 'bin' the sub-folder containing compiled classes. */
	public static final String DEFAULT_OUTPUT_DIRECTORY 		= "bin";

	/** 'javac' the default java compiler. */
	public static final String DEFAULT_JAVA_COMPILER			= "javac";

	/**
	 * @param onOff boolean, Set keyboard's 'NumLock' on or off.
	 */
	public static void setNumLock(boolean onOff){
		Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, onOff);
	}
	/**
	 * @return boolean, the current keyboard's 'NumLock' status.
	 */
	public static boolean getNumLock(){
		return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
	}

	/**
	 * Compile the java/groovy source code.<br>
	 * <b>NOTE:</b><br>
	 * 1. It is supposed the "<b>source codes</b>" resides in the <b>{@value #DEFAULT_SOURCE_DIRECTORY}</b> sub-folder of the root directory.<br>
	 * The "<b>compiled classes</b>" will be put the the <b>{@value #DEFAULT_OUTPUT_DIRECTORY}</b> sub-folder of the root directory.<br>
	 * 2. Any dependency jars should be included in the environment "<b>CLASSPATH</b>" before this compilation.<br>
	 * <b>File structure:</b><br>
	 * <pre>
	 * root
	 *   + {@value #DEFAULT_SOURCE_DIRECTORY} (source files folder)
	 *      + package1
	 *      + package2
	 *   + {@value #DEFAULT_OUTPUT_DIRECTORY} (compiled classes go into this folder)
	 * </pre>
	 *
	 * @param classnames String, the class names to compile separated by space, such as my.package.ClassA my.pack2.ClassB<br>
	 *                           this parameter could be mixed with java and groovy class.<br>
	 *                           <b>Note:</b> For groovy, we have to specify all dependency classes in order.<br>
	 * @throws SAFSException if the compilation failed.
	 */
	public static void compile(String classnames) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		if(!StringUtils.isValid(classnames)){
			throw new SAFSException("The input parameter 'classnames' must NOT be null or empty!");
		}

		IndependantLog.debug(debugmsg+" parameter classnames: "+classnames);

		//The property "user.dir" represent the current working directory.
		String userdir = System.getProperty("user.dir");
		IndependantLog.debug("user.dir (current working directory) is "+userdir);

		//It is supposed that the "source code" locates at the sub-folder 'src' of current working directory.
		File sourceDir = new File(userdir, DEFAULT_SOURCE_DIRECTORY);
		if(!sourceDir.exists() || !sourceDir.isDirectory()){
			throw new SAFSException("The source directory path '"+sourceDir.getAbsolutePath()+"' does not exist or is not a directory!");
		}
		//It is supposed that the "compiled classes" go to the sub-folder 'bin' of current working directory.
		File outputDir = null;
		outputDir = new File(userdir, DEFAULT_OUTPUT_DIRECTORY);
		if(!outputDir.exists()) outputDir.mkdir();
		if(!outputDir.exists() || !outputDir.isDirectory()){
			throw new SAFSException("The output path '"+outputDir.getAbsolutePath()+"' does not exist or is not a directory.");
		}

		//prepare the source codes to compile
		StringBuffer javafiles = new StringBuffer();
		List<String> groovyfiles = new ArrayList<String>();
		String[] clazzes = classnames.split(StringUtils.SPACE);
		String source = null;
		File sourceFile = null;
		for(int i=0;i<clazzes.length;i++){
			source = clazzes[i].replace(".", File.separator);
			sourceFile = new File(sourceDir, source+FILE_SUFFIX_JAVA);
			if(sourceFile.exists() && sourceFile.isFile()){
				IndependantLog.debug("Append java file '"+sourceFile.getAbsolutePath()+"'.");
				javafiles.append(sourceFile.getAbsolutePath()+StringUtils.SPACE);
			}else{
				sourceFile = new File(sourceDir, source+FILE_SUFFIX_GROOVY);
				if(sourceFile.exists() && sourceFile.isFile()){
					IndependantLog.debug("Append groovy file '"+sourceFile.getAbsolutePath()+"'.");
					groovyfiles.add(sourceFile.getAbsolutePath());
				}else{
					IndependantLog.warn("cannot find source file for class '"+source+"' as a java or groovy within folder '"+sourceDir.getAbsolutePath()+"'.");
				}
			}
		}

		String classpath = NativeWrapper.GetSystemEnvironmentVariable("CLASSPATH");
		classpath = DEFAULT_OUTPUT_DIRECTORY+";"+classpath;
		IndependantLog.debug("The classpath is "+classpath);

		if(!javafiles.toString().isEmpty()){
			IndependantLog.debug(debugmsg+" Try to compile JAVA source codes: "+javafiles);
			String[] args = {"-cp", classpath, "-encoding", "UTF-8", "-sourcepath", sourceDir.getAbsolutePath(), "-d", outputDir.getAbsolutePath(), "-implicit:class", javafiles.toString()};
			//TODO to get the correct java compiler java 32 bits, will be provided by SAFS itself
			String javaCompiler = DEFAULT_JAVA_COMPILER;
			try {
				Hashtable result = NativeWrapper.runShortProcessAndWait(javaCompiler, args);
				int rc = (int) result.get(NativeWrapper.RESULT_KEY);
				Vector output = (Vector) result.get(NativeWrapper.VECTOR_KEY);
				IndependantLog.debug(debugmsg+" java compilation with error code "+rc+"\n  Console output is:\n"+output.toString());
				if(rc!=0){
					throw new SAFSException(javafiles+" cannot be compiled at runtime.");
				}
			} catch (IOException e) {
				IndependantLog.error(debugmsg+javafiles+" cannot be compiled at runtime, met "+StringUtils.debugmsg(e));
				throw new SAFSException("Failed to compile source codes: " + javafiles+" at runtime.");
			}
		}

		if(!groovyfiles.isEmpty()){
			IndependantLog.debug(debugmsg+" Try to compile GROOVY source codes: "+groovyfiles.toString());
			String[] args = {"-cp", classpath, "-encoding", "UTF-8", "-sourcepath", sourceDir.getAbsolutePath(), "-d",outputDir.getAbsolutePath(), ""/*ONE groovy source file*/};
			SecurityManager sm = System.getSecurityManager();
			try{
				System.setSecurityManager(new NoSystemExitSecurityManager());
				for(String groovyfile:groovyfiles){
					args[args.length-1] = groovyfile;
					try{
						IndependantLog.debug(debugmsg+" groovy comile: "+Arrays.toString(args));
						org.codehaus.groovy.tools.FileSystemCompiler.main(args);
					}catch(NoSystemExitException e){
						IndependantLog.warn(debugmsg+groovyfile+" cannot be compiled at runtime, met "+StringUtils.debugmsg(e));
					}
				}
			}finally{
				System.setSecurityManager(sm);
			}

		}
	}

	/**
	 * This special SecurityManager is used to avoid JVM to halt by System.exit().<br>
	 */
    private static class NoSystemExitSecurityManager extends SecurityManager{

    	public NoSystemExitSecurityManager(){}

        public void checkPermission(Permission perm) {}
        public void checkPermission(Permission perm, Object context) {}

        public void checkExit(int status){
            //avoid JVM termination by System.exit().
        	super.checkExit(status);
            throw new NoSystemExitException(status);
        }
    }
    /** a Runtime exception to throw in NoSystemExitSecurityManager. */
    private static class NoSystemExitException extends SecurityException{
        private int status=0;
        public NoSystemExitException(int status){
            super("System.exit() has been called with code '"+status+"', but the JVM will not terminate!");
            this.status = status;
        }
        public int getStatus(){
        	return status;
        }
    }

    /**
     * Get the value form the map according to a key.<br/>
     * If the map is null or the value is null, then return the default value.
     * @param map Map
     * @param key Object,
     * @param defaultValue Object,
     * @return Object
     */
	public static Object getMapValue(Map<?, ?> map, Object key, Object defaultValue){
		if(map==null){
			return defaultValue;
		}
		if(map.containsKey(key)){
			return map.get(key);
		}else{
			return defaultValue;
		}
	}

	/**
	 * Convert the parameter 'value' to an appropriate Object according to the expectedType.
	 * @param expectedType Class, the expected field type
	 * @param value Object, the value to parse.
	 * @return Object, the converted Object.
	 */
	public static Object parseValue(Class<?> expectedType, Object value) throws SAFSException{
		if(expectedType==null || value==null){
			return value;
		}
		if(expectedType.isAssignableFrom(value.getClass())){
			return value;
		}

		Object fieldValue = value;

		try{

			if(Persistable.class.isAssignableFrom(expectedType)){
				if(!(value instanceof Persistable)){
					IndependantLog.warn("Need to convert the object from '"+value.getClass().getName()+"' to Persistable subclass '"+expectedType.getName()+"'");
				}
				//else, even the 'value' is a Persistable, it is not sure that it can be assigned to the field.

			}else if(expectedType.getName().equals(String.class.getName())){
				if(!(value instanceof String)){
					fieldValue = String.valueOf(value);
				}
			}else if(expectedType.getName().equals(Boolean.TYPE.getName())){
				if(!(value instanceof Boolean)){
					fieldValue = Boolean.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Integer.TYPE.getName())){
				if(!(value instanceof Integer)){
					fieldValue = Integer.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Short.TYPE.getName())){
				if(!(value instanceof Short)){
					fieldValue = Short.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Long.TYPE.getName())){
				if(!(value instanceof Long)){
					fieldValue = Long.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Double.TYPE.getName())){
				if(!(value instanceof Double)){
					fieldValue = Double.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Float.TYPE.getName())){
				if(!(value instanceof Float)){
					fieldValue = Float.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Byte.TYPE.getName())){
				if(!(value instanceof Byte)){
					fieldValue = Byte.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Character.TYPE.getName())){
				if(!(value instanceof Character)){
					fieldValue = Character.valueOf(value.toString().charAt(0));
				}
			}else if(expectedType.isArray()){
				if(value.getClass().isArray()){
					//convert to appropriate array
					Class<?> actualArrayType = value.getClass().getComponentType();
					Class<?> expectedArrayType = expectedType.getComponentType();
					int length = Array.getLength(value);
					Object arrayItem = null;

					if(!expectedArrayType.isAssignableFrom(actualArrayType)){
						fieldValue = Array.newInstance(expectedArrayType, length);
						for(int i=0;i<length;i++){
							arrayItem = Array.get(value, i);
							//cast arrayItem to 'expectedArrayType'
							arrayItem = parseValue(expectedArrayType, arrayItem);
							Array.set(fieldValue, i, arrayItem);
						}
					}
				}else{
					throw new SAFSException("cannot set a non array object to an array field!");
				}
			}
		}catch(NumberFormatException | IndexOutOfBoundsException e){
			//user's data error
			throw new SAFSException(e.toString());
		}catch(SAFSException se){
			throw se;
		}catch(Exception e){
			//program error, a bug
			throw new SAFSException(e.toString());
		}

		return fieldValue;
	}

	/**
	 * @param array Object, an array object.
	 * @return Object[]
	 * @throws SAFSException
	 */
	public static Object[] getArray(Object array) throws SAFSException{
		if(array==null){
			throw new SAFSException("cannot convert a null to an array object.");
		}

		Class<?> clazz = array.getClass();
		if(!clazz.isArray()){
			throw new SAFSException(clazz.getName()+ "is not an array object.");
		}

		Class<?> componentType = clazz.getComponentType();

		if(componentType.isPrimitive()){
			int length = Array.getLength(array);
			Object[] values = new Object[length];
			for(int i=0;i<length;i++){
				values[i] = Array.get(array, i);
			}
			return values;
		}else{
			return (Object[]) array;
		}
	}
}
