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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * APR 22, 2016    (Lei Wang) Initial release.
 * SEP 06, 2016    (Lei Wang) Added method compile(): compile java/groovy source code at runtime.
 * DEC 12, 2016    (Lei Wang) Added method getMapValue().
 * MAR 16, 2017    (Lei Wang) Added method getArray() and parseValue().
 * AUG 17, 2017    (Lei Wang) Added method setField() and getFieldInstanceClassName().
 * NOV 03, 2017    (Lei Wang) Added method toJsonString() and fromJsonString(): conversion between "Java Map object" and "JSON string".
 * NOV 09, 2017    (Lei Wang) Modified method setNumLock() and getNumLock(): Java Toolkit cannot handle "Num Lock" as expected; Use Robot to handle it.
 * JAN 16, 2018    (Lei Wang) Added method isJSON().
 * MAR 27, 2018    (Lei Wang) Modified method parseValue(): parse both primitive and wrapper type for int, long, short, boolean etc.
 *                           Added method getJsonValue(): Get expected value from a json string according to a key.
 * APR 04, 2018    (Lei Wang) Added more toJsonXXX() methods: provide convenient way to get custom json string.
 *                           Added fromJson() method: provide convenient way to convert json string to Object.
 * MAY 03, 2018    (Lei Wang) Added method phoneHome() and classes xxxDateJsonDeserializer: handle the json data returned from safs data service.
 * MAY 04, 2018    (Lei Wang) Modified method setField(): set a ready-value (not created by constructor) to a field.
 *                           Renamed method phoneHome() to pushToRepository():
 *                                   verify http status code.
 *                                   get ID from json response if we cannot convert json to RestModel directly.
 * MAY 17, 2018    (Lei Wang) Added method deleteFromRepository().
 * MAY 28, 2018    (Lei Wang) Added methods getRunningXXXTestProcess();
 * JUN 04, 2018    (Lei Wang) Added method updateRepository().
 * JUN 22, 2018    (Lei Wang) Added method getFromRepository() and fireEkspresoEvent().
 * NOV 26, 2018    (Lei Wang) Added method getAllFromRepository(): get all data of a certain RestModel from the repository.
 * JUN 20, 2019    (Lei Wang) Modified method getFieldClass(), setField(): if we cannot get a field then we try to get it from its super class.
 * AUG 15, 2019    (Lei Wang) Added getFieldInstanceClass(): Get the class name of the real instance of the field.
 * APR 27, 2021    (Lei Wang) Moved some third-party-jar-independent-methods to 'org.safs.UtilsIndependent'.
 */
package org.safs;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.Permission;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safs.Constants.SAFS_LogConstants;
import org.safs.data.model.EkspresoEvent;
import org.safs.data.model.Engine;
import org.safs.data.model.Framework;
import org.safs.data.model.History;
import org.safs.data.model.HistoryEngine;
import org.safs.data.model.Machine;
import org.safs.data.model.Orderable;
import org.safs.data.model.RestModel;
import org.safs.data.model.Status;
import org.safs.data.model.Testcase;
import org.safs.data.model.Testcycle;
import org.safs.data.model.Teststep;
import org.safs.data.model.Testsuite;
import org.safs.data.model.User;
import org.safs.natives.NativeWrapper;
import org.safs.persist.Persistable;
import org.safs.rest.REST.DELETE;
import org.safs.rest.REST.GET;
import org.safs.rest.REST.POST;
import org.safs.rest.REST.PUT;
import org.safs.rest.service.Response;
import org.safs.robot.Robot;
import org.safs.tools.GenericProcessMonitor;
import org.safs.tools.GenericProcessMonitor.ProcessInfo;
import org.safs.tools.GenericProcessMonitor.WQLSearchCondition;
import org.safs.tools.drivers.DriverConstant;
import org.springframework.http.HttpStatus;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public abstract class Utils{
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
	 * true or false, keep the current state of "Num Lock".
	 */
	private static boolean currentNumLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);

	/**
	 * @param onOff boolean, Set keyboard's 'NumLock' on or off.
	 */
	public static void setNumLock(boolean onOff){
		if(currentNumLock!=onOff){
			//setLockingKeyState() is not stable at all! Use Robot to type 'Num Lock'
//			Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, onOff);
			Robot.keyPress(KeyEvent.VK_NUM_LOCK);
			Robot.keyRelease(KeyEvent.VK_NUM_LOCK);
			currentNumLock = onOff;
		}
	}
	/**
	 * @return boolean, the current keyboard's 'NumLock' status.
	 */
	public static boolean getNumLock(){
		//getLockingKeyState() always returns the original state! If we type the "Num Lock" during the program,
		//getLockingKeyState() will NOT return the latest state. So we keep track of the "Num Lock state" in a static field
//		return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
		return currentNumLock;
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
	 *   + {@value #DEFAULT_OUTPUT_DIRECTORY} (compiled classes go into this folder--which will be created if not already present.)
	 * </pre>
	 *
	 * @param classnames String, the class names to compile separated by space, such as my.package.ClassA my.pack2.ClassB<br>
	 *                           this parameter could be mixed with java and groovy class.<br>
	 *                           <b>Note:</b> For groovy, we have to specify all dependency classes in order.<br>
	 * @throws SAFSException if the compilation failed for any reason--including bad parameter values.
	 */
	public static void compile(String classnames) throws SAFSException{
		if(!StringUtils.isValid(classnames)){
			throw new SAFSException("The input parameter 'classnames' must NOT be null or empty!");
		}

		//The property "user.dir" represent the current working directory.
		String userdir = System.getProperty("user.dir");

		//It is supposed that the "source code" locates at the sub-folder 'src' of current working directory.
		File sourceDir = new File(userdir, DEFAULT_SOURCE_DIRECTORY);

		//It is supposed that the "compiled classes" go to the sub-folder 'bin' of current working directory.
		File outputDir = null;
		outputDir = new File(userdir, DEFAULT_OUTPUT_DIRECTORY);

		String classpath = NativeWrapper.GetSystemEnvironmentVariable("CLASSPATH");
		classpath = DEFAULT_OUTPUT_DIRECTORY+";"+classpath;

		compile(classnames, sourceDir.getAbsolutePath(), outputDir.getAbsolutePath(), classpath);
	}

	/**
	 * Compile java and/or groovy source code.<br>
	 *
	 * @param classnames String, the class names to compile separated by space, such as my.package.ClassA my.pack2.ClassB<br>
	 *                           this parameter could be mixed with java and groovy class.<br>
	 *                           <b>Note:</b> For groovy, we have to specify all dependency classes in order.<br>
	 * @param sourceDIR String, the root directory off which the sourcecode files reside.<br>
	 * @param outDIR String, the root directory off which the compiled classes will be stored. The directory will be created, if necessary.<br>
	 * @param useClasspath String, the semi-colon separated CLASSPATH to be used for the compile.<br>
	 *
	 * @throws SAFSException if the compilation failed for any reason--including bad parameter values.
	 */
	@SuppressWarnings("rawtypes")
	public static void compile(String classnames, String sourceDIR, String outDIR, String useClasspath) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		if(!StringUtils.isValid(classnames)){
			throw new SAFSException("The input parameter 'classnames' must NOT be null or empty!");
		}
		IndependantLog.debug(debugmsg+" parameter classnames: "+classnames);

		if(!StringUtils.isValid(sourceDIR)){
			throw new SAFSException("The input parameter 'sourceDIR' must NOT be null or empty!");
		}
		IndependantLog.debug(debugmsg+" parameter sourceDIR: "+sourceDIR);

		if(!StringUtils.isValid(outDIR)){
			throw new SAFSException("The input parameter 'outDIR' must NOT be null or empty!");
		}
		IndependantLog.debug(debugmsg+" parameter outDIR: "+outDIR);

		if(!StringUtils.isValid(useClasspath)){
			throw new SAFSException("The input parameter 'useClasspath' must NOT be null or empty!");
		}
		IndependantLog.debug(debugmsg+" parameter useClasspath: "+useClasspath);

		//It is supposed that the "source code" locates at the sub-folder 'src' of current working directory.
		File sourceDir = new File(sourceDIR);
		if(!sourceDir.exists() || !sourceDir.isDirectory()){
			throw new SAFSException("The source directory path '"+sourceDir.getAbsolutePath()+"' does not exist or is not a directory!");
		}

		//It is supposed that the "compiled classes" go to the sub-folder 'bin' of current working directory.
		File outputDir = null;
		outputDir = new File(outDIR);
		if(!outputDir.exists()) outputDir.mkdir();
		if(!outputDir.exists() || !outputDir.isDirectory()){
			throw new SAFSException("The output path '"+outputDir.getAbsolutePath()+"' does not exist, is not a directory, and could not be created.");
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

		String classpath = outDIR +";"+ useClasspath;
		IndependantLog.debug("The full classpath is "+classpath);

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

        @Override
		public void checkPermission(Permission perm) {}
        @Override
		public void checkPermission(Permission perm, Object context) {}

        @Override
		public void checkExit(int status){
            //avoid JVM termination by System.exit().
        	super.checkExit(status);
            throw new NoSystemExitException(status);
        }
    }
    /** a Runtime exception to throw in NoSystemExitSecurityManager. */
    @SuppressWarnings("serial")
	private static class NoSystemExitException extends SecurityException{
        private int status=0;
        public NoSystemExitException(int status){
            super("System.exit() has been called with code '"+status+"', but the JVM will not terminate!");
            this.status = status;
        }
        @SuppressWarnings("unused")
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
	//PLEASE CALL org.safs.persist.test.PersistTest after modifying this parseValue() method.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object parseValue(Class<?> expectedType, Object value) throws SAFSException{
		Object fieldValue = value;

		if(expectedType==null || value==null){
			return fieldValue;
		}

		//The type of the value
		Class<?> actualType = value.getClass();

		if(expectedType.isAssignableFrom(actualType)){
			return fieldValue;
		}

		try{

			if(Collection.class.isAssignableFrom(expectedType)){
				//If we expect collection data,
				Collection collectionValue = null;

				//Create an appropriate collection object
				if(expectedType.isInterface() || Modifier.isAbstract(expectedType.getModifiers())){
					if(expectedType.isAssignableFrom(ArrayList.class)){
						collectionValue = new ArrayList();
					}else if(expectedType.isAssignableFrom(HashSet.class)){
						collectionValue = new HashSet();
					}else if(expectedType.isAssignableFrom(PriorityQueue.class)){
						collectionValue = new PriorityQueue();
					}else{
						IndependantLog.warn("Failed to instantiate collection of type '"+expectedType+"'.");
					}
				}else{
					try{
						collectionValue = (Collection) expectedType.newInstance();
					}catch(InstantiationException ite){
						IndependantLog.warn("Failed to instantiate collection of type '"+expectedType+"', due to "+ite.toString());
						collectionValue = new ArrayList();
					}
				}

				if(collectionValue!=null){
					if(Collection.class.isAssignableFrom(actualType)){
						collectionValue.addAll((Collection) value);//add the actual collection of objects to fieldValue
					}else if(JSONArray.class.isAssignableFrom(actualType)){
						//TODO If the JSONArray contains JSONObject???
						collectionValue.addAll(((JSONArray)value).toList());
					}else if(Iterable.class.isAssignableFrom(actualType)){
						Iterator<?> iter = ((Iterable<?>) value).iterator();
						while(iter.hasNext()){
							collectionValue.add(iter.next());
						}
					}else if(actualType.isArray()){
						int length = Array.getLength(value);
						for(int i=0;i<length;i++){
							collectionValue.add(Array.get(value, i));
						}
					}else{
						IndependantLog.warn("Need more implementation to handle '"+actualType+"', and assign it to '"+expectedType+"'!");
					}

					if(!collectionValue.isEmpty()){
						return collectionValue;
					}
				}

			}

			if(Persistable.class.isAssignableFrom(expectedType)){
				if(!(value instanceof Persistable)){
					IndependantLog.warn("Need to convert the object from '"+actualType.getName()+"' to Persistable subclass '"+expectedType.getName()+"'");
				}
				//else, even the 'value' is a Persistable, it is not sure that it can be assigned to the field.

			}else if(expectedType.getName().equals(String.class.getName())){
				if(!(value instanceof String)){
					fieldValue = String.valueOf(value);
				}
			}else if(expectedType.getName().equals(Boolean.TYPE.getName()) /*For primitive type 'boolean'*/||
					 expectedType.getName().equals(Boolean.class.getName()) /*For wrapper type 'Boolean'*/
					){
				if(!(value instanceof Boolean)){
					fieldValue = Boolean.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Integer.TYPE.getName()) ||
					 expectedType.getName().equals(Integer.class.getName())
					){
				if(!(value instanceof Integer)){
					try{
						fieldValue = Integer.valueOf(value.toString());
					}catch(NumberFormatException e){
						IndependantLog.warn(e.toString());
						fieldValue = new Integer((int)Double.parseDouble(value.toString()));
					}
				}
			}else if(expectedType.getName().equals(Short.TYPE.getName()) ||
					 expectedType.getName().equals(Short.class.getName())
					){
				if(!(value instanceof Short)){
					try{
						fieldValue = Short.valueOf(value.toString());
					}catch(NumberFormatException e){
						IndependantLog.warn(e.toString());
						fieldValue = new Short((short)Double.parseDouble(value.toString()));
					}
				}
			}else if(expectedType.getName().equals(Long.TYPE.getName()) ||
					 expectedType.getName().equals(Long.class.getName())
					){
				if(!(value instanceof Long)){
					try{
						fieldValue = Long.valueOf(value.toString());
					}catch(NumberFormatException e){
						IndependantLog.warn(e.toString());
						fieldValue = new Long((long)Double.parseDouble(value.toString()));
					}
				}
			}else if(expectedType.getName().equals(Double.TYPE.getName()) ||
					 expectedType.getName().equals(Double.class.getName())
					 ){
				if(!(value instanceof Double)){
					fieldValue = Double.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Float.TYPE.getName()) ||
					 expectedType.getName().equals(Float.class.getName())
					){
				if(!(value instanceof Float)){
					fieldValue = Float.valueOf(value.toString());
				}
			}else if(expectedType.getName().equals(Byte.TYPE.getName()) ||
					 expectedType.getName().equals(Byte.class.getName())
					){
				try{
					if(!(value instanceof Byte)){
						fieldValue = Byte.valueOf(value.toString());
					}
				}catch(NumberFormatException e){
					IndependantLog.warn(e.toString());
					fieldValue = new Byte((byte)Double.parseDouble(value.toString()));
				}

			}else if(expectedType.getName().equals(Character.TYPE.getName()) ||
					 expectedType.getName().equals(Character.class.getName())
					){
				if(!(value instanceof Character)){
					fieldValue = Character.valueOf(value.toString().charAt(0));
				}
			}else if(Map.class.isAssignableFrom(expectedType)){
				//We expect a Map value
				if(!(value instanceof Map)){
					fieldValue = fromJsonString(value.toString(), expectedType);
				}
			}else if(expectedType.isArray()){
				Class<?> expectedItemType = expectedType.getComponentType();
				//convert the value (Object) to an appropriate array
				if(actualType.isArray()){
					Class<?> actualItemType = value.getClass().getComponentType();
					if(!expectedItemType.isAssignableFrom(actualItemType)){
						IndependantLog.warn("'"+actualItemType+"' cannot be assigned to '"+expectedItemType+"'");
						//if expectedItemType is [L, but actualItemType is a List of Long, this situation should be valid
					}

					int length = Array.getLength(value);
					fieldValue = Array.newInstance(expectedItemType, length);
					Object arrayItem = null;
					for(int i=0;i<length;i++){
						arrayItem = Array.get(value, i);
						//cast arrayItem to 'expectedItemType'
						arrayItem = parseValue(expectedItemType, arrayItem);
						Array.set(fieldValue, i, arrayItem);
					}
				}else if(JSONArray.class.isAssignableFrom(actualType)){
					//TODO If the JSONArray contains JSONObject???
					fieldValue = parseValue(expectedType, ((JSONArray)value).toList());

				}else if(Iterable.class.isAssignableFrom(actualType)){
					Iterator<?> iter = ((Iterable<?>) value).iterator();
					List<Object> items = new ArrayList<Object>();
					while(iter.hasNext()){
						items.add(iter.next());
					}
					fieldValue = Array.newInstance(expectedItemType, items.size());

					if(items.size()>0){
						Class<?> actualItemType = items.get(0).getClass();
						if(!expectedItemType.isAssignableFrom(actualItemType)){
							IndependantLog.warn("'"+actualItemType+"' cannot be assigned to '"+expectedItemType+"'");
							//if expectedItemType is [L, but actualItemType is a List of Long, this situation should be valid
						}
					}

					Object arrayItem = null;
					for(int i=0;i<items.size();i++){
						//cast arrayItem to 'expectedItemType'
						arrayItem = parseValue(expectedItemType, items.get(i));
						Array.set(fieldValue, i, arrayItem);
					}

				}else{
					throw new SAFSException("cannot set a non array object to an array field!");
				}
			}else{
				IndependantLog.warn("There is yet no implelementation to convert value '"+value+"' to Object of type '"+expectedType+"'.");
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
			throw new SAFSException("parameter array is null.");
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

	/**
	 * Convert an Object to a string, even this object is a multiple dimension array.
	 * @param object Object
	 * @return String
	 */
	public static String toString(Object object){
		String result = null;
		try{
			Object[] array = getArray(object);
			result = Arrays.deepToString(array);
		}catch(SAFSException se){
			result = object!=null? object.toString():null;
		}

		return result;
	}

	/**
	 * Compare 2 objects even they are multiple-dimension arrays.
	 * @param value1 Object
	 * @param value2 Object
	 * @return boolean
	 */
	public static boolean equals(Object value1, Object value2){
		if(value1==value2)
			return true;
		if(value1==null || value2==null)
			return false;

		if(value1.getClass().isArray()){
			if(!value2.getClass().isArray()){
				return false;
			}
			try {
				Object[] array1 = getArray(value1);
				Object[] array2 = getArray(value2);
				if(array1.length!=array2.length){
					return false;
				}
				for(int i=0;i<array1.length;i++){
					if(!equals(array1[i], array2[i])) return false;
				}
			} catch (SAFSException e) {
				IndependantLog.warn("Not equal, due to "+e.toString());
				return false;
			}
		}else if(!value1.equals(value2)){
			return false;
		}

		return true;
	}

	/**
	 * Change the windows .lnk file's property to make it "Run As Administrator".
	 * @param lnkFileName String, the full path to a link file
	 * @throws IOException
	 */
	public static void makeRunAsAdministrator(String lnkFileName) throws IOException{
		//#set byte 21 (0x15) bit 6 (0x20) ON, which represents the "Run As Administrator" property
		RandomAccessFile lnkFile = null;
		try{
			lnkFile = new RandomAccessFile(lnkFileName, "rw");
			int propertyBytePos = 0x15;

			//read the byte containing "Run As Administrator" property
			lnkFile.seek(propertyBytePos);
			byte propertyByte = lnkFile.readByte();

			//Set the 6th bit on
			propertyByte = (byte) (propertyByte | 0x20);

			//write the byte back to the link file
			lnkFile.seek(propertyBytePos);
			lnkFile.writeByte(propertyByte);
		}finally{
			if(lnkFile!=null)
				lnkFile.close();
		}
	}

	/** "FIELD_INSTANCE" */
	private static final String FIELD_INSTANCE = "FIELD_INSTANCE";
	/** Object.class */
	private static final Class<?> FIELD_INSTANCE_CLASS = Object.class;

	/**
	 * Set a value to a field.<br>
	 * @param object Object, the object whose field will be set
	 * @param fieldName String, the name of the field to be set
	 * @param value Object, the value to set. It must be the same type of sub type as the field's.
	 */
	public static void setField(Object object, String fieldName, Object value){
		LinkedHashMap<String, LinkedHashMap<Class<?>, Object>> fieldClassNameToArgumentsMap = new LinkedHashMap<String, LinkedHashMap<Class<?>, Object>>();
		LinkedHashMap<Class<?>, Object> fieldValue = new LinkedHashMap<Class<?>, Object>();
		fieldValue.put(FIELD_INSTANCE_CLASS, value);
		fieldClassNameToArgumentsMap.put(FIELD_INSTANCE, fieldValue);

		setField(object, fieldName, fieldClassNameToArgumentsMap);
	}

	/**
	 * Set a value to a field.<br>
	 * The value will be normally instantiated according parameter <b>fieldClassNameToConstructorParamsMap</b> if it doesn't contain key {@link #FIELD_INSTANCE}.
	 *
	 * @param object Object, the object whose field will be set
	 * @param fieldName String, the name of the field to be set
	 * @param fieldClassNameToConstructorParamsMap LinkedHashMap&lt;String, LinkedHashMap&lt;Class&lt;?>, Object>>, the map of pair (fieldClassName, constructor-arguments) to create an instance for a field.<br>
	 *        The map's key is a class name, which is used to create an instance for the field.<br>
	 *        The map's key can be<br>
	 *        <ol>
	 *        <li>{@link #FIELD_INSTANCE}, then map's value is a map containing pair(<b>Object.class</b>, <b>value</b>), and the <b>value</b> will be assigned to the field directly.<br>
	 *            The key {@link #FIELD_INSTANCE} is exclusive in the map, we should NOT put other keys in this map.
	 *        <li>a real class name (it can be sub-type of this field), then the map's value is a map containing constructor-arguments, which are used to create an instance by constructor.<br>
	 *            We can put multiple 'class name' (different sub-class, or with different constructor) as the key in this map, we try to create instance one by one, the first successful
	 *            instance will be assigned to this field.
	 *        </ol>
	 *
	 * @usages Please refer to {@link #testReflectField()}
	 *
	 *
	 * @see #setField(Object, String, Object)
	 * @see #testReflectField()
	 */
	public static void setField(Object object, String fieldName, LinkedHashMap<String/*fieldClassName*/, LinkedHashMap<Class<?>, Object>/*constructor arguments*/> fieldClassNameToConstructorParamsMap){
		String debugmsg = "Utils.setField(): ";

		try {
			Field field = null;
			try{
				field = object.getClass().getDeclaredField(fieldName);
			}catch(NoSuchFieldException e){
				field = object.getClass().getSuperclass().getDeclaredField(fieldName);
			}
			field.setAccessible(true);

			Object fieldObject = null;

			//we will give a chance to the ready cooked field
			if(fieldClassNameToConstructorParamsMap.containsKey(FIELD_INSTANCE)){
				try{
					fieldObject = fieldClassNameToConstructorParamsMap.get(FIELD_INSTANCE).get(FIELD_INSTANCE_CLASS);
				}catch(Exception ignore){
					IndependantLog.warn(debugmsg+" ignore "+ignore);
				}
			}else{
				Set<String> fieldClassNames = fieldClassNameToConstructorParamsMap.keySet();
				LinkedHashMap<Class<?>, Object> arguments = null;

				//we try to create 'field object' with constructor of field's class
				for(String fieldClassName: fieldClassNames){
					try {
						arguments = fieldClassNameToConstructorParamsMap.get(fieldClassName);
						if(arguments==null){
							//Create fieldObject by default constructor
							fieldObject = Class.forName(fieldClassName).newInstance();
						}else{
							//Create fieldObject by constructor with parameters
							Constructor<?> constructor = Class.forName(fieldClassName).getConstructor(arguments.keySet().toArray(new Class[0]));
							fieldObject = constructor.newInstance(arguments.values().toArray());
						}
						if(fieldObject!=null){
							IndependantLog.debug(debugmsg+" got an instance of class '"+fieldClassName+"', and it will be assigned to field '"+fieldName+"'.");
							break;
						}
					} catch (Exception ignore) {
						IndependantLog.warn(debugmsg+" ignore "+ignore);
					}
				}
			}

			if(fieldObject!=null){
				field.set(object, fieldObject);
			}else{
				IndependantLog.error(debugmsg+"cannot create a valid object to set for field '"+fieldName+"'");
			}

		} catch (Exception e) {
			IndependantLog.error(debugmsg+" met "+e);
		}
	}

	/**
	 * Get the class name of the field's defined type. <br>
	 *
	 * @param object Object, the object from which to get class of a field
	 * @param fieldName String, the name of the field to get its class
	 * @return Class, the class name of the field's defined type.
	 */
	public static Class<?> getFieldClass(Object object, String fieldName) throws NoSuchFieldException, SecurityException{
		Field field = null;
		try{
			field = object.getClass().getDeclaredField(fieldName);
		}catch(NoSuchFieldException e){
			field = object.getClass().getSuperclass().getDeclaredField(fieldName);
		}
		return field.getType();
	}

	/**
	 * Get the class name of the real instance of the field.<br>
	 *
	 * @param object Object, the object from which to get class of a field
	 * @param fieldName String, the name of the field to get its class
	 * @return Class, the class name of the real instance of the field.
	 */
	public static Class<?> getFieldInstanceClass(Object object, String fieldName) throws NoSuchFieldException, SecurityException{
		Field field = null;
		try{
			field = object.getClass().getDeclaredField(fieldName);
		}catch(NoSuchFieldException e){
			field = object.getClass().getSuperclass().getDeclaredField(fieldName);
		}

		try {
			field.setAccessible(true);
			Object fieldObject = field.get(object);
			return fieldObject.getClass();
		} catch (Exception e) {
			IndependantLog.error(StringUtils.debugmsg(false)+" Met "+e.toString());
		}

		return field.getType();
	}

	/**
	 * @param object Object, the object from which to get class name of the instance of a field
	 * @param fieldName String, the name of the field to get its instance class name
	 * @return String the class name of a field's instance (it can be the sub-class of the defined type of the field).
	 */
	public static String getFieldInstanceClassName(Object object, String fieldName){
		String debugmsg = "Utils.getFieldInstanceClassName(): ";
		String fieldInstanceClassName = "";
		try {
			fieldInstanceClassName = getFieldInstanceClass(object, fieldName).getName();
		} catch (Exception e) {
			IndependantLog.error(debugmsg+" met "+e);
		}

		return fieldInstanceClassName;
	}

	/** "yyyy-MM-dd'T'HH:mm:ss.SSSZ" */
	public static String DATE_FORMAT_ACCEPTED_BY_SPRING_MVC = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	/** "yyyy-MM-dd'T'HH:mm:ss'Z'" */
	public static String DATE_FORMAT_ISO8601 				= "yyyy-MM-dd'T'HH:mm:ss'Z'";

	/**
	 * Convert an object to JSON String.<br>
	 * <b>Note:</b> It needs com.google.gson.Gson, which is currently included in the selenium-server-standalone jar.
	 *              If we want this Utils class independent from selenium-server-standalone jar, we need to put the google gson jar on the classpath.
	 *
	 * @param source Object, to be converted to JSON String
	 * @return String, the JSON string converted from source object.
	 */
	public static String toJsonString(Object source){
		return toJson(new GsonBuilder(), source);
	}

	/**
	 * Convert an object to JSON string accepted by Spring.<br>
	 * Currently it will make sure the Date will be converted to format {@link #DATE_FORMAT_ACCEPTED_BY_SPRING_MVC}.<br>
	 * @param source Object, to be converted to JSON String
	 * @return String, the JSON string converted from source object; especially this string should be accepted by Spring.
	 */
	public static String toJsonForSpring(Object source){
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat(DATE_FORMAT_ACCEPTED_BY_SPRING_MVC);
		//We can still modify the Gson builder to constraint other stuff
		return toJson(builder, source);
	}

	/**
	 * Convert an object to JSON String.<br>
	 * Especially it will convert Date to a certain format specified by parameter 'dateFormat'.<br>
	 *
	 * @param source Object, to be converted to JSON String
	 * @param dateFormat String,
	 * @return String, the JSON string converted from source object;
	 */
	public static String toJson(Object source, String dateFormat){
		GsonBuilder builder = new GsonBuilder().setDateFormat(dateFormat);
		return toJson(builder, source);
	}

	/**
	 * Provide a flexible way to convert an Object to json string.
	 * User can provide its own GsonBuilder.<br>
	 *
	 * @param builder GsonBuilder, the user-provided builder.
	 * @param source Object, to be converted to json string.
	 * @return String, the converted json string.
	 */
	public static String toJson(GsonBuilder builder, Object source){
		//We can mark a field with 'transient' modifier so that it will not be transformed.

		//@Expose annotation will mark a field that should be transformed
		//@Expose(serialize = false, deserialize = false)
//		builder.excludeFieldsWithoutExposeAnnotation();

		//We can also implement a ExclusionStrategy to define a custom strategy
//		builder.setExclusionStrategies(new CustomStrategy());

		String jsonStr = builder.create().toJson(source);
		IndependantLog.info("convert "+source.getClass().getSimpleName()+" object '"+source+"' to json String '"+jsonStr+"'.");

		return jsonStr;
	}

	/**
	 * Convert a JSON String to a certain type.<br>
	 * <b>Note:</b> It needs com.google.gson.Gson, which is currently included in the selenium-server-standalone jar.
	 *              If we want this Utils class independent from selenium-server-standalone jar, we need to put the google gson jar on the classpath.
	 *
	 * @param jsonString String, the JSON string.
	 * @param type Class<T>, the type of object which "JSON String" to be converted.
	 * @return T, the T object converted from JSON string.
	 */
	public static <T> T fromJsonString(String jsonString, Class<T> type){
		return fromJson(new GsonBuilder(), jsonString, type);
	}

	/**
	 * Provide a flexible way to convert a JSON String to a certain type.<br>
	 * User can provide its own GsonBuilder.<br>
	 *
	 * @param builder GsonBuilder, the user-provided builder.
	 * @param jsonString String, the JSON string.
	 * @param type Class<T>, the type of object which "JSON String" to be converted.
	 * @return T, the T object converted from JSON string.
	 */
	public static <T> T  fromJson(GsonBuilder builder, String jsonString, Class<T> type){
		T result = builder.create().fromJson(jsonString, type);
		IndependantLog.info("convert json String '"+jsonString+"' to "+type.getName()+" object '"+result+"'.");
		return result;
	}

	/**
	 * Provide a flexible way to convert a JSON String to a certain type.<br>
	 * User can provide its own GsonBuilder.<br>
	 *
	 * @param builder GsonBuilder, the user-provided builder.
	 * @param jsonString String, the JSON string.
	 * @param type Type, the type of object which "JSON String" to be converted.
	 * @return T, the T object converted from JSON string.
	 */
	public static <T> T fromJson(GsonBuilder builder, String jsonString, Type type){
		T result = builder.create().fromJson(jsonString, type);
		IndependantLog.info("convert json String '"+jsonString+"' to "+type.getTypeName()+" object '"+result+"'.");
		return result;
	}

	/**
	 * Delete a {@link RestModel} from a repository.<br>
	 * @param safsdataServiceID String, the session ID. REST.StartServiceSession( safsDataServiceID, serverURL) should have been called previously.
	 * @param restModel RestModel, the model to delete from a repository in safs data service.
	 * @throws SAFSDatabaseException if we fail to delete the rest model from repository.
	 */
	public static void deleteFromRepository(String safsdataServiceID, RestModel restModel) throws SAFSDatabaseException{
		Response response = null;
		HttpStatus status = null;
		String restModelName = restModel.getClass().getSimpleName();

		try{
			response = DELETE.json(safsdataServiceID, restModel.getRestPath()+"/"+restModel.getId(), null);
			status = HttpStatus.valueOf(response.get_status_code());
		}catch(Exception e){
			String errmsg = "Failed to delete '"+restModelName+"' from safs data repository.";
			throw new SAFSDatabaseException(errmsg, e);
		}

		//Verify that the http status code is 'NO_CONTENT' or 'NOT_FOUND'
		if(!(HttpStatus.NO_CONTENT.equals(status) || HttpStatus.NOT_FOUND.equals(status))){
			throw new SAFSDatabaseException("Failed to delete '"+restModelName+"' from safs data repository, met un-expected http status code '"+status.value()+":"+status.getReasonPhrase()+"'.");
		}
	}

	/**
	 * Push a {@link RestModel} to a repository.<br>
	 * @param safsdataServiceID String, the session ID. REST.StartServiceSession( safsDataServiceID, serverURL) should have been called previously.
	 * @param restModel RestModel, the model to push to a repository in safs data service.
	 * @return RestModel (with generated ID) got from the successful http response.
	 * @throws SAFSDatabaseException if we fail to push the rest model to repository.
	 * @throws SAFSModelCreationException if we fail to get rest model (with generated ID) from the http response. But the model has been pushed successfully.
	 */
	public static RestModel pushToRepository(String safsdataServiceID, RestModel restModel) throws SAFSDatabaseException, SAFSModelCreationException{
		Response response = null;
		HttpStatus status = null;
		String restModelName = restModel.getClass().getSimpleName();

		try{
			response = POST.json(safsdataServiceID, restModel.getRestPath(), Utils.toJsonForSpring(restModel));
			status = HttpStatus.valueOf(response.get_status_code());
		}catch(Exception e){
			String errmsg = "Failed to push '"+restModelName+"' to safs data repository.";
			throw new SAFSDatabaseException(errmsg, e);
		}

		//Verify that the http status code is 'CREATED' or 'FOUND'
		if(HttpStatus.CREATED.equals(status) || HttpStatus.FOUND.equals(status)){
			String modelInJsonString = response.get_entity_body().toString();
			try{
				//The rest model has been successfully pushed to the repository.
				//Response's entity body is json string representing the Model object created in the repository.
				//We will convert it to a Model object, which contains the "ID" generated during insertion to the repository.

				//The Date object will be returned as long value from safs-data-service, create a JsonDeserializer for Date to handle long value.
				GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Date.class, new LongDateJsonDeserializer(new DefaultDateJsonDeserializer()));
				RestModel model = Utils.fromJson(builder, modelInJsonString, restModel.getClass());
				//IndependantLog.debug(model.getClass().getSimpleName()+": "+model+" has been saved to repository.");
				return model;
			}catch(Exception e){
				String errmsg = "Failed to convert json string to '"+restModelName+"'\nJSON Model String: "+modelInJsonString+"\nDue to "+e.getMessage();
				IndependantLog.warn(errmsg);

				//Try to get the 'id' from the http response, and set it to rest model.
				String idFieldName = "id";
				Class<?> idFieldClass = null;
				Object ID = null;

				try{
					idFieldClass = Utils.getFieldClass(restModel, idFieldName);
					ID = Utils.getJsonValue(modelInJsonString, idFieldName, idFieldClass);
				}catch(Exception failGetID){
					IndependantLog.warn("Failed to get '"+idFieldName+"' as type '"+idFieldClass.getSimpleName()+"' from model json string\n"+modelInJsonString+"\nDue to "+failGetID);
				}

				try{
//					restModel.setId(ID);
					Utils.setField(restModel, idFieldName, ID);
					return restModel;
				}catch(Exception failSetID){
					errmsg = "Failed to set '"+ID+"' to "+restModelName+"'s field '"+idFieldName+"'";
					throw new SAFSModelCreationException(errmsg, failSetID);
				}
			}
		}else{
			throw new SAFSDatabaseException("Failed to push '"+restModelName+"' to safs data repository, met un-expected http status code '"+status.value()+":"+status.getReasonPhrase()+"'.");
		}
	}

	/**
	 * @param safsdataServiceID String, the SAFS data service ID.
	 * @param restModel RestModel, the model to update in repository.
	 * @return RestModel, the updated model
	 * @throws SAFSDatabaseException if the http reponse's status is not OK.
	 */
	public static RestModel updateRepository(String safsdataServiceID, RestModel restModel) throws SAFSDatabaseException{
		Response response = null;
		HttpStatus status = null;
		String restModelName = restModel.getClass().getSimpleName();

		try{
			response = PUT.json(safsdataServiceID, restModel.getRestPath()+"/"+restModel.getId(), Utils.toJsonForSpring(restModel));
			status = HttpStatus.valueOf(response.get_status_code());
		}catch(Exception e){
			String errmsg = "Failed to update '"+restModelName+"' in safs data repository.";
			throw new SAFSDatabaseException(errmsg, e);
		}

		//Verify that the http status code is 'OK'
		if(HttpStatus.OK.equals(status)){
			String modelInJsonString = response.get_entity_body().toString();
			try{
				//The rest model has been successfully pushed to the repository.
				//Response's entity body is json string representing the Model object created in the repository.
				//We will convert it to a Model object, which contains the "ID" generated during insertion to the repository.

				//The Date object will be returned as long value from safs-data-service, create a JsonDeserializer for Date to handle long value.
				GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Date.class, new LongDateJsonDeserializer(new DefaultDateJsonDeserializer()));
				RestModel model = Utils.fromJson(builder, modelInJsonString, restModel.getClass());
				//IndependantLog.debug(model.getClass().getSimpleName()+": "+model+" has been saved to repository.");
				return model;
			}catch(Exception e){
				String errmsg = "Failed to convert json string to '"+restModelName+"'\nJSON Model String: "+modelInJsonString+"\nDue to "+e.getMessage();
				IndependantLog.warn(errmsg);
				//Just return the parameter 'restModel'
				return restModel;
			}
		}else{
			throw new SAFSDatabaseException("Failed to update '"+restModelName+"' in safs data repository, met un-expected http status code '"+status.value()+":"+status.getReasonPhrase()+"'.");
		}
	}

	/**
	 * @param safsdataServiceID String, the SAFS data service ID.
	 * @param event_type String, the event_type of the EkspresoEvent being fired.
	 * @param parameters String, it is used by the "SAFS data service" EkspresoEvent controller.
	 *                           It can be anything, it depends the "SAFS data service" EkspresoEvent controller.
	 *                           For example it could be a SAFS event name + historyID, such as "safs_test_start/2", "safs_test_stop/2".
	 * @return RestModel, an EkspresoEvent object.
	 * @throws SAFSDatabaseException if the http reponse's status is not OK.
	 */
	public static RestModel fireEkspresoEvent(String safsdataServiceID, String event_type, String parameters) throws SAFSDatabaseException{
		EkspresoEvent ekspresoEvent = new EkspresoEvent(event_type);
		return getFromRepository(safsdataServiceID, ekspresoEvent, event_type +"/"+ parameters);
	}

	/**
	 * Get a RestModel from data repository according to 'parameters'.
	 * @param safsdataServiceID String, the SAFS data service ID.
	 * @param restModel RestModel, the model to get from repository.
	 * @param parameters String, it is used to select model from repository.
	 *                           The parameters can be anything, it depends the "SAFS Data service". Normally it could be a model's ID.
	 * @return RestModel, got from repository according to the parameters.
	 * @throws SAFSDatabaseException if the http reponse's status is not OK.
	 */
	public static RestModel getFromRepository(String safsdataServiceID, RestModel restModel, String parameters) throws SAFSDatabaseException{
		Response response = null;
		HttpStatus status = null;
		String restModelName = restModel.getClass().getSimpleName();

		try{
			response = GET.json(safsdataServiceID, restModel.getRestPath()+"/"+parameters, null);
			status = HttpStatus.valueOf(response.get_status_code());
		}catch(Exception e){
			String errmsg = "Failed to get '"+restModelName+"' from safs data repository.";
			throw new SAFSDatabaseException(errmsg, e);
		}

		//Verify that the http status code is 'OK'
		if(HttpStatus.OK.equals(status)){
			String modelInJsonString = response.get_entity_body().toString();
			try{
				//The rest model has been successfully got from the repository.
				//Response's entity body is json string representing the Model object in the repository.
				//We will convert it to a Model object

				//The Date object will be returned as long value from safs-data-service, create a JsonDeserializer for Date to handle long value.
				GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Date.class, new LongDateJsonDeserializer(new DefaultDateJsonDeserializer()));
				RestModel model = Utils.fromJson(builder, modelInJsonString, restModel.getClass());
				return model;
			}catch(Exception e){
				String errmsg = "Failed to convert json string to '"+restModelName+"'\nJSON Model String: "+modelInJsonString+"\nDue to "+e.getMessage();
				IndependantLog.warn(errmsg);
				//Just return the parameter 'restModel'
				return restModel;
			}
		}else{
			throw new SAFSDatabaseException("Failed to get '"+restModelName+"' from safs data repository, met un-expected http status code '"+status.value()+":"+status.getReasonPhrase()+"'.");
		}
	}

	/**
	 * Get all of a certain RestModel from data repository.
	 * @param safsdataServiceID String, the SAFS data service ID.
	 * @param model RestModel, the model to get from repository.
	 * @return List<RestModel>, the list of a certain RestModel got from repository. It can be null if there is any unexpected exceptions.
	 * @throws SAFSDatabaseException if the http reponse's status is not OK.
	 */
	public static List<RestModel> getAllFromRepository(String safsdataServiceID, RestModel model) throws SAFSDatabaseException{
		Response response = null;
		HttpStatus status = null;
		String restModelName = model.getClass().getSimpleName();

		try{
			response = GET.json(safsdataServiceID, model.getRestPath() , null);
			status = HttpStatus.valueOf(response.get_status_code());
		}catch(Exception e){
			String errmsg = "Failed to get '"+restModelName+"' from safs data repository.";
			throw new SAFSDatabaseException(errmsg, e);
		}

		//Verify that the http status code is 'OK'
		if(HttpStatus.OK.equals(status)){
			String modelInJsonString = response.get_entity_body().toString();
			try{
				//The collection of rest model has been successfully got from the repository.
				//Response's entity body is json string representing a collection of Model object in the repository.
				//We will convert it to a List of Model object

				//The Date object will be returned as long value from safs-data-service, create a JsonDeserializer for Date to handle long value.
				GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Date.class, new LongDateJsonDeserializer(new DefaultDateJsonDeserializer()));
				Type listType = null;
				if(model instanceof EkspresoEvent){
					listType = new TypeToken<List<EkspresoEvent>>(){}.getType();
				}else if(model instanceof Engine){
					listType = new TypeToken<List<Engine>>(){}.getType();
				}else if(model instanceof Framework){
					listType = new TypeToken<List<Framework>>(){}.getType();
				}else if(model instanceof History){
					listType = new TypeToken<List<History>>(){}.getType();
				}else if(model instanceof HistoryEngine){
					listType = new TypeToken<List<HistoryEngine>>(){}.getType();
				}else if(model instanceof Machine){
					listType = new TypeToken<List<Machine>>(){}.getType();
				}else if(model instanceof Orderable){
					listType = new TypeToken<List<Orderable>>(){}.getType();
				}else if(model instanceof Status){
					listType = new TypeToken<List<Status>>(){}.getType();
				}else if(model instanceof Testcase){
					listType = new TypeToken<List<Testcase>>(){}.getType();
				}else if(model instanceof Testcycle){
					listType = new TypeToken<List<Testcycle>>(){}.getType();
				}else if(model instanceof Teststep){
					listType = new TypeToken<List<Teststep>>(){}.getType();
				}else if(model instanceof Testsuite){
					listType = new TypeToken<List<Testsuite>>(){}.getType();
				}else if(model instanceof User){
					listType = new TypeToken<List<User>>(){}.getType();
				}

				List<RestModel> models = Utils.fromJson(builder, modelInJsonString, listType);
				return models;
			}catch(Exception e){
				String errmsg = "Failed to convert json string to a collection of '"+restModelName+"'\nJSON Model String: "+modelInJsonString+"\nDue to "+e.getMessage();
				IndependantLog.error(errmsg);
				return null;
			}
		}else{
			throw new SAFSDatabaseException("Failed to get a collection of '"+restModelName+"' from safs data repository, met un-expected http status code '"+status.value()+":"+status.getReasonPhrase()+"'.");
		}
	}

	/**
	 * A wrapper {@link JsonDeserializer} to parse a value to Date.<br>
	 * It wraps a {@link JsonDeserializer} and delegate the parse work to it. It self does not do any specific parse work.<br>
	 */
	public static class WrapperDateJsonDeserializer implements JsonDeserializer<Date>{
		protected JsonDeserializer<Date> delegate;

		public WrapperDateJsonDeserializer(JsonDeserializer<Date> delegate){
			this.delegate = delegate;
		}

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if(delegate==null){
				throw new JsonParseException("The delegate JsonDeserializer is null, please assign it.");
			}
			return delegate.deserialize(json, typeOfT, context);
		}
	}

	/**
	 * A custom {@link JsonDeserializer} to parse a string value to Date.<br>
	 * Currently, it can parse date string as below:<br>
	 * {@link Utils#DATE_FORMAT_ISO8601}<br>
	 * {@link Utils#DATE_FORMAT_ACCEPTED_BY_SPRING_MVC}<br>
	 * {@link SAFS_LogConstants#FORMAT_DATE_TIME}<br>
	 *
	 */
	public static class DefaultDateJsonDeserializer implements JsonDeserializer<Date>{
		/**
		 * The DateFormats used to parse a String to a Date.
		 */
		List<DateFormat> dateFormats = new ArrayList<DateFormat>();

		public DefaultDateJsonDeserializer(){
			//Default format for Gson
			DateFormat iso8601Format = new SimpleDateFormat(DATE_FORMAT_ISO8601, Locale.US);
			iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
			DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
			DateFormat localFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);

			//our custom formats
			DateFormat springFormat = new SimpleDateFormat(DATE_FORMAT_ACCEPTED_BY_SPRING_MVC);
			DateFormat safsLogFormat = new SimpleDateFormat(SAFS_LogConstants.FORMAT_DATE_TIME);

			dateFormats.add(iso8601Format);
			dateFormats.add(enUsFormat);
			dateFormats.add(localFormat);

			dateFormats.add(safsLogFormat);
			dateFormats.add(springFormat);
		}

		public DefaultDateJsonDeserializer(List<DateFormat> customFormats){
			this();
			if(customFormats!=null){
				dateFormats.addAll(customFormats);
			}
		}

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			for(DateFormat format:dateFormats){
				try {
					return format.parse(json.getAsString());
				} catch (ParseException ignoreIt) {
				}
			}
			throw new JsonParseException("Cannot convert "+json.getAsJsonPrimitive()+" to Date.");
		}
	}

	/**
	 * A custom {@link JsonDeserializer} to parse a long value to Date.<br>
	 * If we create it with an other JsonDeserializer, then it will also try to parse the date with that JsonDeserializer.<br>
	 * For example:<br>
	 * JsonDeserializer&lt;Date> dateDesrializer = new {@link LongDateJsonDeserializer}(new {@link DefaultDateJsonDeserializer}());<br>
	 *
	 */
	public static class LongDateJsonDeserializer extends WrapperDateJsonDeserializer{
		/**
		 * @param delegate JsonDeserializer&lt;Date>, an extra deserializer used to parse an object to Date.
		 *                                            it can be null if only 'long value' needs to be parsed.
		 */
		public LongDateJsonDeserializer(JsonDeserializer<Date> delegate){
			super(delegate);
		}

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try{
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}catch(NumberFormatException e){
				return super.deserialize(json, typeOfT, context);
			}
		}
	}

	/**
	 * Get expected value from a json string according to a key.<br>
	 * @param jsonString String, the json string.
	 * @param key String, the key mapping to a value in json string.
	 * @param expectedType Class&lt;T>, the expected type of the value got by key from json string.
	 * @return T, the value of 'key' in the json string.
	 * @throws SAFSException
	 *
	 * @example
	 * <pre>
	 * String jsonstring = "{'int':10.0, 'long':12, 'boolean':true}";
	 * Integer intValue = getJsonValue(jsonstring, "int", Integer.class);
	 * Long longValue = getJsonValue(jsonstring, "long", Long.class);
	 * Boolean boolValue = getJsonValue(jsonstring, "boolean", Boolean.class);
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getJsonValue(String jsonString, String key, Class<T> expectedType) throws SAFSException{
		Map<?, ?> entity = Utils.fromJsonString(jsonString, Map.class);
		Object value = entity.get(key);

		//Problem: When converting a json string to Map, Gson will convert an integer to a float string, for example, an ID 1 will be converted to 1.0, it is wrong!
		//We will call our method parseValue() to convert a float/double to int or long as expected by user.
		return (T) parseValue(expectedType, value);
	}

	/**
	 * @param value String
	 * @return boolean if the parameter 'value' is a json string.
	 */
	public static boolean isJSON(String value) {
	    try {
	        new JSONObject(value);
	    } catch (JSONException ex) {
	        try {
	            new JSONArray(value);
	        } catch (JSONException ex1) {
	            return false;
	        }
	    }
	    return true;
	}

	/**
	 * Get a list of running java process.
	 * @param host	String, the host name where to get processes
	 * @param fields List<String>, a list of field to catch for a process. It can contain any of "Caption CommandLine CreationClassName  CreationDate CSCreationClassName CSName  Description  ExecutablePath ExecutionState  Handle  HandleCount  InstallDate  KernelModeTime  MaximumWorkingSetSize  MinimumWorkingSetSize  Name OSCreationClassName OSName  OtherOperationCount  OtherTransferCount  PageFaults  PageFileUsage  ParentProcessId  PeakPageFileUsage  PeakVirtualSize  PeakWorkingSetSize  Priority  PrivatePageCount  ProcessId  QuotaNonPagedPoolUsage  QuotaPagedPoolUsage  QuotaPeakNonPagedPoolUsage  QuotaPeakPagedPoolUsage  ReadOperationCount  ReadTransferCount  SessionId  Status  TerminationDate  ThreadCount  UserModeTime  VirtualSize  WindowsVersion  WorkingSetSize  WriteOperationCount  WriteTransferCount".
	 * @return List<ProcessInfo>, a list of processes running java.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> getRunningJavaProcess(String host, List<String> fields) throws SAFSException{
		IndependantLog.debug("WDLibrary.getRunningJavaProcess(): getting running process 'java' on machine '"+host+"'.");

		//wmic process where "(name='java.exe' or name='javaw.exe')"
		String wmiSearchCondition = GenericProcessMonitor.wqlCondition("name", "java.exe", false, false);
		wmiSearchCondition += " or "+ GenericProcessMonitor.wqlCondition("name", "javaw.exe", false, false);
		WQLSearchCondition condition = new WQLSearchCondition(wmiSearchCondition);

		return GenericProcessMonitor.getProcess(host, condition, fields);
	}

	/**
	 * Get a list of process running JSAFS Test.<br>
	 * @param host	String, the host name where to get processes
	 * @param fields List<String>, a list of field to catch for a process. It can contain any of "Caption CommandLine CreationClassName  CreationDate CSCreationClassName CSName  Description  ExecutablePath ExecutionState  Handle  HandleCount  InstallDate  KernelModeTime  MaximumWorkingSetSize  MinimumWorkingSetSize  Name OSCreationClassName OSName  OtherOperationCount  OtherTransferCount  PageFaults  PageFileUsage  ParentProcessId  PeakPageFileUsage  PeakVirtualSize  PeakWorkingSetSize  Priority  PrivatePageCount  ProcessId  QuotaNonPagedPoolUsage  QuotaPagedPoolUsage  QuotaPeakNonPagedPoolUsage  QuotaPeakPagedPoolUsage  ReadOperationCount  ReadTransferCount  SessionId  Status  TerminationDate  ThreadCount  UserModeTime  VirtualSize  WindowsVersion  WorkingSetSize  WriteOperationCount  WriteTransferCount".
	 * @return List<ProcessInfo>, a list of processes which are running SAFS Test.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> getRunningJSAFSTestProcess(String host, List<String> fields) throws SAFSException{
		IndependantLog.debug("WDLibrary.getRunningJavaProcess(): getting running process of JSAFS test on machine '"+host+"'.");

		//"org.eclipse.equinox.launcher" is for the Eclipse-IDE itself, we don't want it
		//wmic process where "(name='java.exe' or name='javaw.exe') and (commandline like '%-Dsafs.project.config%')"
		String wmiSearchCondition = "( " + GenericProcessMonitor.wqlCondition("name", "java.exe", false, false);//"java.exe" Launched from command line
		wmiSearchCondition += " or "+ GenericProcessMonitor.wqlCondition("name", "javaw.exe", false, false)+") ";//"javaw.exe" Launched from Eclipse IDE

		//JSAFS test is very flexible, for now I can only check string "-Dsafs.project.config" on the command-line.
		wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "-D"+DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG, true, false)+"";

		//"org.eclipse.equinox.launcher" (org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar) is for the Eclipse-IDE itself, we don't want Eclipse-IDE
		wmiSearchCondition += " and (not "+ GenericProcessMonitor.wqlCondition("commandline", "org.eclipse.equinox.launcher", true, false)+")";
		wmiSearchCondition += " and (not "+ GenericProcessMonitor.wqlCondition("commandline", Utils.class.getName(), true, false)+")";//we don't want org.safs.Utils

		WQLSearchCondition condition = new WQLSearchCondition(wmiSearchCondition);

		return GenericProcessMonitor.getProcess(host, condition, fields);
	}

	/**
	 * Get a list of process running SAFS Test.
	 * @param host	String, the host name where to get processes
	 * @param fields List<String>, a list of field to catch for a process. It can contain any of "Caption CommandLine CreationClassName  CreationDate CSCreationClassName CSName  Description  ExecutablePath ExecutionState  Handle  HandleCount  InstallDate  KernelModeTime  MaximumWorkingSetSize  MinimumWorkingSetSize  Name OSCreationClassName OSName  OtherOperationCount  OtherTransferCount  PageFaults  PageFileUsage  ParentProcessId  PeakPageFileUsage  PeakVirtualSize  PeakWorkingSetSize  Priority  PrivatePageCount  ProcessId  QuotaNonPagedPoolUsage  QuotaPagedPoolUsage  QuotaPeakNonPagedPoolUsage  QuotaPeakPagedPoolUsage  ReadOperationCount  ReadTransferCount  SessionId  Status  TerminationDate  ThreadCount  UserModeTime  VirtualSize  WindowsVersion  WorkingSetSize  WriteOperationCount  WriteTransferCount".
	 * @return List<ProcessInfo>, a list of processes which are running SAFS Test.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> getRunningSAFSTestProcess(String host, List<String> fields) throws SAFSException{
		IndependantLog.debug("WDLibrary.getRunningJavaProcess(): getting running process of SAFS test on machine '"+host+"'.");

		//wmic process where "(name='java.exe' or name='javaw.exe') and (commandline like '%org.safs.tools.drivers.SAFSDRIVER%') and (not commandline like '%org.eclipse.equinox.launcher%') and (not commandline like '%org.safs.selenium.util.SeleniumServerRunner%')"
		String wmiSearchCondition = "( " + GenericProcessMonitor.wqlCondition("name", "java.exe", false, false);//"java.exe" Launched from command line
		wmiSearchCondition += " or "+ GenericProcessMonitor.wqlCondition("name", "javaw.exe", false, false)+") ";//"javaw.exe" Launched from Eclipse IDE

		wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "org.safs.tools.drivers.SAFSDRIVER", true, false)+"";//we need "org.safs.tools.drivers.SAFSDRIVER"
		String classpath = System.getenv("classpath");
		if(classpath==null || !classpath.toLowerCase().contains("safs.jar")){
			wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "safs.jar", true, false)+"";//we need "safs.jar"
		}

		//"org.eclipse.equinox.launcher" (org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar) is for the Eclipse-IDE itself, we don't want Eclipse-IDE
		wmiSearchCondition += " and (not "+ GenericProcessMonitor.wqlCondition("commandline", "org.eclipse.equinox.launcher", true, false)+")";
		wmiSearchCondition += " and (not "+ GenericProcessMonitor.wqlCondition("commandline", Utils.class.getName(), true, false)+")";//we don't want org.safs.Utils

		WQLSearchCondition condition = new WQLSearchCondition(wmiSearchCondition);

		return GenericProcessMonitor.getProcess(host, condition, fields);
	}

	/**
	 * Get a list of process running SeleniumPlus Test.
	 * @param host	String, the host name where to get processes
	 * @param fields List<String>, a list of field to catch for a process. It can contain any of "Caption CommandLine CreationClassName  CreationDate CSCreationClassName CSName  Description  ExecutablePath ExecutionState  Handle  HandleCount  InstallDate  KernelModeTime  MaximumWorkingSetSize  MinimumWorkingSetSize  Name OSCreationClassName OSName  OtherOperationCount  OtherTransferCount  PageFaults  PageFileUsage  ParentProcessId  PeakPageFileUsage  PeakVirtualSize  PeakWorkingSetSize  Priority  PrivatePageCount  ProcessId  QuotaNonPagedPoolUsage  QuotaPagedPoolUsage  QuotaPeakNonPagedPoolUsage  QuotaPeakPagedPoolUsage  ReadOperationCount  ReadTransferCount  SessionId  Status  TerminationDate  ThreadCount  UserModeTime  VirtualSize  WindowsVersion  WorkingSetSize  WriteOperationCount  WriteTransferCount".
	 * @return List<ProcessInfo>, a list of processes which are running SAFS Test.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> getRunningSeleniumPlusTestProcess(String host, List<String> fields) throws SAFSException{
		IndependantLog.debug("WDLibrary.getRunningJavaProcess(): getting running process of SeleniumPlus test on machine '"+host+"'.");

		//wmic process where "(name='java.exe' or name='javaw.exe') and (commandline like '%selenium-server-standalone%') and (not commandline like '%org.eclipse.equinox.launcher%') and (not commandline like '%org.safs.selenium.util.SeleniumServerRunner%')"
		String wmiSearchCondition = "( " + GenericProcessMonitor.wqlCondition("name", "java.exe", false, false);//"java.exe" Launched from command line
		wmiSearchCondition += " or "+ GenericProcessMonitor.wqlCondition("name", "javaw.exe", false, false)+") ";//"javaw.exe" Launched from Eclipse IDE
		String classpath = System.getenv("classpath");
		if(classpath==null){
			wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "selenium-server-standalone", true, false)+"";//we need "selenium-server-standalone-xxx.jar"
			wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "seleniumplus.jar", true, false)+"";//we need "seleniumplus.jar"
			wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "JSTAFEmbedded.jar", true, false)+"";//we need "JSTAFEmbedded.jar"
		}else{
			if(!classpath.toLowerCase().contains("selenium-server-standalone")){
				wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "selenium-server-standalone", true, false)+"";//we need "selenium-server-standalone-xxx.jar"
			}
			if(!classpath.toLowerCase().contains("seleniumplus.jar")){
				wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "seleniumplus.jar", true, false)+"";//we need "selenium-server-standalone-xxx.jar"
			}
			if(!classpath.toLowerCase().contains("JSTAFEmbedded.jar")){
				wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("commandline", "JSTAFEmbedded.jar", true, false)+"";//we need "selenium-server-standalone-xxx.jar"
			}
		}

		//"org.eclipse.equinox.launcher" (org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar) is for the Eclipse-IDE itself, we don't want  Eclipse-IDE.
		wmiSearchCondition += " and (not "+ GenericProcessMonitor.wqlCondition("commandline", "org.eclipse.equinox.launcher", true, false)+")";
		wmiSearchCondition += " and (not "+ GenericProcessMonitor.wqlCondition("commandline", "org.safs.selenium.util.SeleniumServerRunner", true, false)+")";//we don't want org.safs.selenium.util.SeleniumServerRunner
		wmiSearchCondition += " and (not "+ GenericProcessMonitor.wqlCondition("commandline", Utils.class.getName(), true, false)+")";//we don't want org.safs.Utils
		//TODO remove the process started by "RemoteServer.bat",
		//commandline="c:\seleniumplus\Java64\jre\bin\java.exe"  -Xms512m -Xmx2g -Dwebdriver.chrome.driver="c:\seleniumplus\extra\chromedriver.exe" -Dwebdriver.ie.driver="c:\seleniumplus\extra\IEDriverServer.exe" -Dwebdriver.gecko.driver="c:\seleniumplus\extra\geckodriver_64.exe" -jar "c:\seleniumplus\libs\selenium-server-standalone-3.4.0.jar" -timeout 0 -browserTimeout 0

		WQLSearchCondition condition = new WQLSearchCondition(wmiSearchCondition);

		return GenericProcessMonitor.getProcess(host, condition, fields);
	}

	/**
	 * Get the running process of SeleniumPlus, SAFS or JSAFS test.<br>
	 * We assume there is only one running test on a machine.<br>
	 * @param host	String, the host name where to get processes
	 * @param fields List<String>, a list of field to catch for a process. It can contain any of "Caption CommandLine CreationClassName  CreationDate CSCreationClassName CSName  Description  ExecutablePath ExecutionState  Handle  HandleCount  InstallDate  KernelModeTime  MaximumWorkingSetSize  MinimumWorkingSetSize  Name OSCreationClassName OSName  OtherOperationCount  OtherTransferCount  PageFaults  PageFileUsage  ParentProcessId  PeakPageFileUsage  PeakVirtualSize  PeakWorkingSetSize  Priority  PrivatePageCount  ProcessId  QuotaNonPagedPoolUsage  QuotaPagedPoolUsage  QuotaPeakNonPagedPoolUsage  QuotaPeakPagedPoolUsage  ReadOperationCount  ReadTransferCount  SessionId  Status  TerminationDate  ThreadCount  UserModeTime  VirtualSize  WindowsVersion  WorkingSetSize  WriteOperationCount  WriteTransferCount".
	 * @return List<ProcessInfo>, a list of processes which are running Test (SeleniumPlus, SAFS or JSAFS).
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> getRunningTestProcess(String host, List<String> fields) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		List<ProcessInfo> runningTests = getRunningSeleniumPlusTestProcess(host, fields);

		if(runningTests.size()==0){
			IndependantLog.warn(debugmsg+"Cannot get any running process for SeleniumPlus test! Try to get for SAFS test.");
			runningTests = getRunningSAFSTestProcess(host, fields);
		}

		if(runningTests.size()==0){
			IndependantLog.warn(debugmsg+"Cannot get any running process for SAFS test!");
			runningTests = getRunningJSAFSTestProcess(host, fields);
		}

		if(runningTests.size()==0){
			IndependantLog.error(debugmsg+"Cannot get any running process for JSAFS test!");
		}

		return runningTests;
	}

	/**
	 * Get the command-line of a running test (SeleniumPlus, SAFS).<br>
	 * We assume there is only one running test on a machine.<br>
	 * @param host	String, the host name where to get process
	 * @return String, the command-line of the running test. null if no running test is found.
	 * @throws SAFSException
	 */
	public static String getRunningTestCommandLine(String host) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		List<String> fields = new ArrayList<String>();
		fields.add("commandline");
		List<ProcessInfo> runningTests = getRunningTestProcess(host, fields);

		if(runningTests.size()>0){
			if(runningTests.size()>1){
				IndependantLog.debug(debugmsg+"running tests are\n"+runningTests);
			}
			//we only return the first running process
			return runningTests.get(0).getField("commandline");
		}else{
			return null;
		}
	}

	/**
	 * @param url String, the URL to verify
	 * @return boolean if the URL exist, return true.
	 * @deprecated Please use {@link UtilsIndependent#isURLExist(String)} instead.
	 */
	@Deprecated
	public static boolean isURLExist(String url){
		return UtilsIndependent.isURLExist(url);
	}

	/**
	 * Download the content from an URL and save it to a local file.
	 *
	 * @param url String, the URL to download
	 * @param outfile File, the destination file
	 * @deprecated Please use {@link UtilsIndependent#downloadURL(String, File)} instead.
	 * @throws IOException
	 *
	 */
	@Deprecated
	public static void downloadURL(String url, File outfile) throws IOException{
		downloadURL(url, outfile, true);
	}

	/**
	 * Download the content from an URL and save it to a local file.
	 *
	 * @param url String, the URL to download
	 * @param outfile File, the destination file
	 * @param checkContent boolean, if need to check the response content
	 * @deprecated Please use {@link UtilsIndependent#downloadURL(String, File, boolean)} instead.
	 * @throws IOException
	 */
	@Deprecated
	public static void downloadURL(String url, File outfile, boolean checkContent) throws IOException{
		UtilsIndependent.downloadURL(url, outfile, checkContent);
	}

	private static void testJsonMapConversion(){
		Map<String, String> inputMap = new HashMap<String, String>();
		inputMap.put("name", "json");
		inputMap.put("value", "hello world.");
		// convert map to JSON String
		String jsonStr = toJsonString(inputMap);
		System.out.println(jsonStr);

		Map<?, ?> unpickleMap = fromJsonString(jsonStr, Map.class);
		boolean conversionOK = inputMap.equals(unpickleMap);
		System.out.println("conversionOK="+conversionOK);

		assert conversionOK: "The conversion between 'Map' and 'Json String' failed.";
	}

	/**
	 * The method to test {@link #setField(Object, String, LinkedHashMap)} and {@link #getFieldInstanceClassName(Object, String)}.
	 */
	private static void testReflectField(){
		Tree tree = new Tree();
		LinkedHashMap<Class<?>, Object> arguments = new LinkedHashMap<Class<?>, Object>();
		LinkedHashMap<String/*fieldClassNames*/, LinkedHashMap<Class<?>, Object>/*arguments*/> fieldClassNameToArgumentsMap = new LinkedHashMap<String, LinkedHashMap<Class<?>, Object>>();

		//Set tree's userObject
		fieldClassNameToArgumentsMap.put("java.lang.Object", arguments);
		setField(tree, "userObject", fieldClassNameToArgumentsMap);

		//Set tree's level
		fieldClassNameToArgumentsMap.clear();
		arguments.clear();
		arguments.put(String.class, "5");
		fieldClassNameToArgumentsMap.put("java.lang.Integer", arguments);
		setField(tree, "level", fieldClassNameToArgumentsMap);

		//Set tree's firstChild
		fieldClassNameToArgumentsMap.clear();
		arguments.clear();
		fieldClassNameToArgumentsMap.put("org.safs.Tree", arguments);
		setField(tree, "firstChild", fieldClassNameToArgumentsMap);
		tree.getFirstChild().setUserObject("Component Label");
		tree.getFirstChild().setLevel(6);
		setField(tree, "childCount", 1);

		//Set tree's nextSibling
		Tree nextSibling = new Tree();
		nextSibling.setUserObject("Component Value");
		nextSibling.setLevel(6);
		setField(tree, "nextSibling", nextSibling);
		setField(tree, "siblingCount", 1);

		System.out.println("The tree's level "+tree.getLevel()+"\n"
				        + "The tree's userObject "+tree.getUserObject()+"\n"
						+ "The tree is "+tree);

		String fieldName = getFieldInstanceClassName(tree, "level");
		System.out.println("The tree's field 'level' is an instance of '"+fieldName+"'");

		int tests = 54;
		fieldName = "tests";
		Testcycle cycle = new Testcycle();
		Utils.setField(cycle, fieldName, tests);
		System.out.println("cycle.getTests() '"+cycle.getTests()+"'");
		assert cycle.getTests()==tests: "Failed to set '"+tests+"' to "+cycle.getClass().getSimpleName()+"'s field '"+fieldName+"'.";

		fieldName = "id";
		long id = 69;
		Utils.setField(cycle, fieldName, id);
		System.out.println("cycle.getId() '"+cycle.getId()+"'");
		assert cycle.getId()==id: "Failed to set '"+id+"' to "+cycle.getClass().getSimpleName()+"'s field '"+fieldName+"'.";

		//Get the 'id' from a json string, and set it to rest model object.
		fieldName = "id";
		id = 1230;
		String modelInJsonString = "{'"+fieldName+"':"+id+"}";
		try {
			Class<?> fieldClass = Utils.getFieldClass(cycle, fieldName);
			Object ID =  Utils.getJsonValue(modelInJsonString, fieldName, fieldClass);
			Utils.setField(cycle, fieldName, ID);
			System.out.println("cycle.getId() '"+cycle.getId()+"'");
			assert cycle.getId()==id: "Failed to set '"+id+"' to "+cycle.getClass().getSimpleName()+"'s field '"+fieldName+"'.";
		} catch (Exception e) {
			assert false: "Failed to set '"+id+"' to "+cycle.getClass().getSimpleName()+"'s field '"+fieldName+"', due to "+e.getMessage();
		}

		System.out.println("");
	}

	private static void testArrays(){
		String[] stringArray1 = {"item1","item2","item3","item4","item5"};
		String[] stringArray2 = {"item1","item2","item3","item4","item5"};
		String[] stringArray3 = {"item1","item2","item3","item4","item5", ""};

		int[] intArray1 = {1,2,3,4,5};
		int[] intArray2 = {1,2,3,4,5};
		int[] intArray3 = {1,2,3};

		Double[] doubleArray1 = {1.0, 2.0, 3.0, 4.0, 5.0};
		Double[] doubleArray2 = {1.0, 2.0, 3.0, 4.0, 5.0};
		Double[] doubleArray3 = null;

		float[][] _2DimFloatArray1 = {{19.0F, 17.45F}, {65.0F, 25.40F}, {1.42F, 78.23F}};
		float[][] _2DimFloatArray2 = {{19.0F, 17.45F}, {65.0F, 25.40F}, {1.42F, 78.23F}};
		float[][] _2DimFloatArray3 = {{19.0F, 17.45F}, {65.0F, 25.40F}, {1.42F}};

		__assert_2_arrays(stringArray1, stringArray2, true);
		__assert_2_arrays(stringArray1, stringArray3, false);

		__assert_2_arrays(intArray1, intArray2, true);
		__assert_2_arrays(intArray1, intArray3, false);

		__assert_2_arrays(doubleArray1, doubleArray2, true);
		__assert_2_arrays(doubleArray1, doubleArray3, false);

		__assert_2_arrays(_2DimFloatArray1, _2DimFloatArray2, true);
		__assert_2_arrays(_2DimFloatArray1, _2DimFloatArray3, false);

	}

	private static void __assert_2_arrays(Object array1, Object array2, boolean positive){
		System.out.println("comparing\narray1: "+toString(array1)+"\narray2: "+toString(array2)+"\n");

		if(positive){
			assert toString(array1).equals(toString(array2));
			assert equals(array1, array2);
		}else{
			assert !toString(array1).equals(toString(array2));
			assert !equals(array1, array2);
		}
	}

	private static void test_NumLock(){
		//Make a break point, and observe the keyboard's "Num Lock" light to verify
		Utils.setNumLock(true);
		Utils.setNumLock(false);
		Utils.setNumLock(true);
		Utils.setNumLock(false);

		Utils.setNumLock(false);
		Utils.setNumLock(true);
		Utils.setNumLock(false);
		Utils.setNumLock(true);
		Utils.setNumLock(false);

		Utils.setNumLock(true);
		Utils.setNumLock(true);

		Utils.setNumLock(false);
		Utils.setNumLock(false);
	}

	private static void testParseValue(){
		try {
			assert new Long(1).equals(Utils.parseValue(long.class, "1.0")):" Failed to convert '1.0' to Long value 1.";
			assert new Long(1).equals(Utils.parseValue(Long.class, "1.0")):" Failed to convert '1.0' to Long value 1.";

			assert new Integer(1).equals(Utils.parseValue(int.class, "1.0")):" Failed to convert '1.0' to Integer value 1.";
			assert new Integer(1).equals(Utils.parseValue(Integer.class, "1.0")):" Failed to convert '1.0' to Integer value 1.";

			assert new Short((short)1).equals(Utils.parseValue(short.class, "1.0")):" Failed to convert '1.0' to Short value 1.";
			assert new Short((short)1).equals(Utils.parseValue(Short.class, "1.0")):" Failed to convert '1.0' to Short value 1.";

			assert new Byte((byte)1).equals(Utils.parseValue(byte.class, "1.0")):" Failed to convert '1.0' to Byte value 1.";
			assert new Byte((byte)1).equals(Utils.parseValue(Byte.class, "1.0")):" Failed to convert '1.0' to Byte value 1.";

			assert new Character('a').equals(Utils.parseValue(char.class, "a")):" Failed to convert 'a' to Character a.";
			assert new Character('a').equals(Utils.parseValue(Character.class, "a")):" Failed to convert 'a' to Character a.";

			assert new Boolean(true).equals(Utils.parseValue(boolean.class, "true")):" Failed to convert 'true' to Boolean true.";
			assert new Boolean(true).equals(Utils.parseValue(Boolean.class, "true")):" Failed to convert 'true' to Boolean true.";

			assert new Boolean(false).equals(Utils.parseValue(boolean.class, "false")):" Failed to convert 'false' to Boolean false.";
			assert new Boolean(false).equals(Utils.parseValue(Boolean.class, "false")):" Failed to convert 'false' to Boolean false.";

		} catch (SAFSException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void testGetJsonValue(){
		Map expectedValue = new HashMap();
		expectedValue.put(int.class, 1);
		expectedValue.put(long.class, 35L);
		expectedValue.put(boolean.class, true);
		expectedValue.put(char.class, 'a');

		String jsonString = "{"
				+ "'int':"+expectedValue.get(int.class)+".0 , " /*deliberately put a .0 at the end of an integer*/
				+ "'long':"+expectedValue.get(long.class)+".0 , " /*deliberately put a .0 at the end of a long*/
				+ "'boolean':"+expectedValue.get(boolean.class)+" , "
				+ "'char':'"+expectedValue.get(char.class)+"'}";
		System.out.println("Json string with wrong format of integer and long value\n"+jsonString+"\n");

		Iterator<Class> keys = expectedValue.keySet().iterator();
		keys.forEachRemaining(clazz->{
			Object actual = null;
			try {
				actual = Utils.getJsonValue(jsonString.toString(), clazz.getName(), clazz);
				assert actual.equals(expectedValue.get(clazz)): actual+" does NOT equal "+expectedValue.get(clazz);
			} catch (SAFSException e) {
				e.printStackTrace();
			}
		});
	}

	private static void testGetJsonDate(){
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        DateFormat localFormat = DateFormat.getDateTimeInstance(2, 2);
        DateFormat springFormat = new SimpleDateFormat(DATE_FORMAT_ACCEPTED_BY_SPRING_MVC);
        DateFormat safsLogFormat = new SimpleDateFormat(SAFS_LogConstants.FORMAT_DATE_TIME);

		Date d = new Date();
		String ds = null;
		Date date = null;

		try{
			System.out.println("Using Default GsonBuilder:");
			GsonBuilder builder = new GsonBuilder();
			ds = iso8601Format.format(d);
			date = Utils.fromJson(builder, StringUtils.quote(ds), Date.class);
			System.out.println("convert iso8601Format '"+ds+"' to Date "+date);

			ds = enUsFormat.format(d);
			date = Utils.fromJson(builder, StringUtils.quote(ds), Date.class);
			System.out.println("convert enUsFormat '"+ds+"' to Date "+date);

			ds = localFormat.format(d);
			date = Utils.fromJson(builder, StringUtils.quote(ds), Date.class);
			System.out.println("convert localFormat '"+ds+"' to Date "+date);

			ds = springFormat.format(d);
			builder.setDateFormat(DATE_FORMAT_ACCEPTED_BY_SPRING_MVC);
			date = Utils.fromJson(builder, StringUtils.quote(ds), Date.class);
			System.out.println("convert springFormat '"+ds+"' to Date "+date);

			ds = safsLogFormat.format(d);
			builder.setDateFormat(SAFS_LogConstants.FORMAT_DATE_TIME);
			date = Utils.fromJson(builder, StringUtils.quote(ds), Date.class);
			System.out.println("convert safsLogFormat '"+ds+"' to Date "+date);

			System.out.println("");
			System.out.println("Using GsonBuilder with IntegerDateJsonDeserialize:");
			builder = new GsonBuilder();
			builder.registerTypeAdapter(Date.class, new LongDateJsonDeserializer(new DefaultDateJsonDeserializer()));

			ds = springFormat.format(d);
			date = Utils.fromJson(builder, StringUtils.quote(ds), Date.class);
			System.out.println("convert springFormat '"+ds+"' to Date "+date);

			ds = String.valueOf(d.getTime());
			date = Utils.fromJson(builder,ds, Date.class);
			System.out.println("convert date.getTime() '"+ds+"' to Date "+date);

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private static void test_get_safs_test(){
		try {
			System.out.println();
			List<String> fields = new ArrayList<String>();
			//Caption    CommandLine CreationClassName  CreationDate CSCreationClassName   CSName  Description  ExecutablePath ExecutionState  Handle  HandleCount  InstallDate  KernelModeTime  MaximumWorkingSetSize  MinimumWorkingSetSize  Name OSCreationClassName OSName  OtherOperationCount  OtherTransferCount  PageFaults  PageFileUsage  ParentProcessId  PeakPageFileUsage  PeakVirtualSize  PeakWorkingSetSize  Priority  PrivatePageCount  ProcessId  QuotaNonPagedPoolUsage  QuotaPagedPoolUsage  QuotaPeakNonPagedPoolUsage  QuotaPeakPagedPoolUsage  ReadOperationCount  ReadTransferCount  SessionId  Status  TerminationDate  ThreadCount  UserModeTime  VirtualSize  WindowsVersion  WorkingSetSize  WriteOperationCount  WriteTransferCount
			fields.add("commandline");
			fields.add("name");
			fields.add("ProcessId");
			List<ProcessInfo> tests = getRunningTestProcess(null, fields);

			if(tests.size()>0) System.out.println("The current running tests are");
			for(ProcessInfo test:tests){
				System.out.println(test);
			}
		} catch (SAFSException e) {
			IndependantLog.error("Met "+e.toString());
		}
	}

	/** java -ea org.safs.Utils */
	public static void main(String[] params){
		testArrays();
		testReflectField();
		testJsonMapConversion();
		test_NumLock();
		testParseValue();
		testGetJsonValue();
		testGetJsonDate();
		test_get_safs_test();
	}
}
