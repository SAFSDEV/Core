/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * APR 22, 2016    (SBJLWA) Initial release.
 * SEP 06, 2016    (SBJLWA) Added method compile(): compile java/groovy source code at runtime.
 * DEC 12, 2016    (SBJLWA) Added method getMapValue().
 * MAR 16, 2017    (SBJLWA) Added method getArray() and parseValue().
 */
package org.safs;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;

import org.json.JSONArray;
import org.safs.natives.NativeWrapper;
import org.safs.persist.Persistable;

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

        public void checkPermission(Permission perm) {}
        public void checkPermission(Permission perm, Object context) {}

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
				IndependantLog.warn("There is yet no implelementation for type '"+expectedType+"'.");
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

	/** java -ea org.safs.Utils */
	public static void main(String[] params){
		testArrays();
	}
}
