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
 * org.safs.text.KEYSFileGenerator.java:
 * Logs for developers, not published to API DOC.
 *
 * History:
 * Jan 12, 2015    (Lei Wang) Initial release.
 */
package org.safs.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import org.safs.StringUtils;
import org.safs.tools.CaseInsensitiveFile;

/**
 * To generate FAILKEYS, GENKEYS java file from .properties files.<br>
 * PLEASE use English properties file, as the property value will be used as comment.<br>
 * For other properties file, the value will be encoded in UNICODE and it is not readable for human.<br>
 * java org.safs.text.KEYSFileGenerator [args]<br>
 *  </ul>
 *  <p>
 *  args:
 *  <p><dl>
 *  <dt>-properties &lt;pathTo>/resource.properties</dt>
 *  <dd><p>
 *  REQUIRED absolute path to the properties file to process.
 *  <p>
 *  Examples:
 *  <p><ul>
 *  -properties "D:\workspace\RftDevJ\org\safs\SAFSTextResourceBundle_en_US.properties"<br>
 *  </ul>
 *  <p>
 *  <dt>-out OutputDirectory</dt>
 *  <dd><p>
 *  REQUIRED path to the directory in which to write the new Java Class file.
 *  <p>
 *  Example:
 *  <p><ul>
 *  -out "D:\workspace\RftDevJ\"
 *  </ul>
 *  <p>
 *  <dt>-name classname</dt>
 *  <dd><p>
 *  OPTIONAL Alternate name to give the generated java class. When not specified,
 *  the resource-bundle-name will be used. There are exceptions for historical reason, if the
 *  resource-bundle-name is "failedSAFSTextResourceBundle", the class name is "FAILKEYS"; and
 *  if resource-bundle-name is "SAFSTextResourceBundle", the class name is "GENKEYS".
 *  <p>
 *  Example:
 *  <p><ul>
 *  -name ResourceKeys<br>
 *  -name ResourceFailKeys<br>
 *  </ul>
 *  <p>
 *  <dt>-package JavaPackageName</dt>
 *  <dd><p>
 *  OPTIONAL package name to use for the output Java Class file. When not specified, 
 *  no package name is written.  The class is part of the nameless (default) Java package.
 *  <p>
 *  Example:
 *  <p><ul>
 *  -package "org.safs.text"
 *  </ul>
 *  <p>
 *  </dl>
 *  <p>
 *  Putting it all together for some sample invocations:
 *  <p><ul><pre>
 *  java org.safs.text.KEYSFileGenerator -properties D:\workspace\RftDevJ\org\safs\SAFSTextResourceBundle_en_US.properties -out D:\workspace\RftDevJ\
 *  java org.safs.text.KEYSFileGenerator -properties D:\workspace\RftDevJ\org\safs\SAFSTextResourceBundle_en_US.properties -out D:\workspace\RftDevJ\ -package org.safs.text
 *  java org.safs.text.KEYSFileGenerator -properties D:\workspace\RftDevJ\org\safs\SAFSTextResourceBundle_en_US.properties -out D:\workspace\RftDevJ\ -package org.safs.text -name SAFSTextResourceBundle
 *  java org.safs.text.KEYSFileGenerator -properties D:\workspace\RftDevJ\org\safs\failedSAFSTextResourceBundle_en_US.properties -out D:\workspace\RftDevJ\ -package org.safs.text -name failedSAFSTextResourceBundle
 *  </pre></ul>
 *  <p>
 *  It is possible that we call this class without any parameter, it will generate java key class for 2 .properties files
 *  (failedSAFSTextResourceBundle_en_US.properties and SAFSTextResourceBundle_en_US.properties under package org.safs), the
 *  2 generated java files will be org.safs.text.FAILKEYS and org.safs.text.GENKEYS
 *  <ul>
 *  java org.safs.text.KEYSFileGenerator
 *  </ul>
 */
public class KEYSFileGenerator{
	
	public static final String paramPropertyFile = "-properties";
	public static final String paramOut = "-out";
	public static final String paramName = "-name";
	public static final String paramPackage = "-package";
	public static final String usage = "java org.safs.text.KEYSFileGenerator -properties <pathTo>/resource.properties -out outputPath [-name class name] [-package packageName]";
	
	/** the number of space that a Tabulation represents, it is normally 4 in java source code.*/
	public static final int TAB_LENGTH = 4;
	/** the number of tabs from the beginning of a line before the equal sign, here we give 18. If some property has longer name, we may adjust this value.*/
	public static final int DEFAULT_TAB_NUMBER = 18;
	
	/** keep the pair of (key, irregular variable name) for history reason */
	private static HashMap<String, String> key2IrregularVariable = new HashMap<String, String>();
	static{
		key2IrregularVariable.put("standard_err", 			"STANDARD_ERROR");
		key2IrregularVariable.put("standard_warn", 			"STANDARD_WARNING");
		key2IrregularVariable.put("failureDetail", 			"FAILURE_DETAIL");
		key2IrregularVariable.put("fail.extract", 			"FAIL_EXTRACT_KEY");
		key2IrregularVariable.put("fail.match", 			"FAIL_MATCH_KEY");
		key2IrregularVariable.put("error_performing_1", 	"ERROR_PERFORMING_1");//As the ending number will be replaced by _number
		key2IrregularVariable.put("error_performing_2", 	"ERROR_PERFORMING_2");
		key2IrregularVariable.put("substring_not_found_2", 	"SUBSTRING_NOT_FOUND_2");
		key2IrregularVariable.put("cant_create_directroy", 	"CANT_CREATE_DIRECTORY");//original key spelling wrong
		
		key2IrregularVariable.put("MyMsg", 					"MY_MSG");
		key2IrregularVariable.put("content_matches", 		"CONTENT_MATCHES_KEY");
		key2IrregularVariable.put("content_not_matches", 	"CONTENT_NOT_MATCHES_KEY");
		key2IrregularVariable.put("contains", 				"CONTAINS_KEY");
		key2IrregularVariable.put("not_contain", 			"NOT_CONTAIN_KEY");
		key2IrregularVariable.put("varAssigned2", 			"VARASSIGNED2");
		key2IrregularVariable.put("perfnode4a", 			"PERFNODE4A");
		key2IrregularVariable.put("multi_assigned", 		"MULTIASSIGNED");
	}
	
	private static final String CLASS_FAILKEYS 		= "FAILKEYS";
	private static final String CLASS_GENKEYS 		= "GENKEYS";
	private static final String CLASS_FAILKEYS_SEE 	= "@see org.safs.text.FAILStrings";
	private static final String CLASS_GENKEYS_SEE 	= "@see org.safs.text.GENStrings";
	
	public static void main(String[] args){
		String propertyFileStr = null;
		String outDirectory = null;
		String className = null;
		String packageName = null;
		
		for(int i=0;i<args.length;i++){
			if(args[i].equalsIgnoreCase(paramPropertyFile)){
				if(i++<args.length) propertyFileStr = args[i];
			}else if(args[i].equalsIgnoreCase(paramOut)){
				if(i++<args.length) outDirectory = args[i];
			}else if(args[i].equalsIgnoreCase(paramName)){
				if(i++<args.length) className = args[i];
			}else if(args[i].equalsIgnoreCase(paramPackage)){
				if(i++<args.length) packageName = args[i];
			}else{
				System.out.println("Unkonwn parameters "+args[i]);
			}
		}
		
		if(propertyFileStr!=null && outDirectory!=null){
			generate(propertyFileStr, outDirectory, packageName, className);
			return;
		}
		
		System.out.println("WRNING: provided parameter is not sufficient.");
		System.out.println(usage);
		System.out.println("Will convert 2 properties (failedSAFSTextResourceBundle and SAFSTextResourceBundle) file under package org.safs.");
		URL url = KEYSFileGenerator.class.getResource("KEYSFileGenerator.class");
		System.out.println("KEYSFileGenerator.class URL="+url.getFile());
		String urlFile = url.getFile();// is /D:/IBM/workspace/RftDevJ/classes/org/safs/text/KEYSFileGenerator.class
		String suffix = "/org/safs/text/KEYSFileGenerator.class";
		if(urlFile.endsWith(suffix)){
			urlFile = urlFile.replace(suffix, "");// /D:/IBM/workspace/RftDevJ/classes
			File clazzDir = new File(urlFile);
			if(clazzDir.exists() && clazzDir.isDirectory()){
				outDirectory = clazzDir.getParent();// without the ending File.separator
				packageName = "org.safs.text";
				
				//failedSAFSTextResourceBundle_en_US.properties
				propertyFileStr = outDirectory+File.separator+"org"+File.separator+"safs"+File.separator+"failedSAFSTextResourceBundle_en_US.properties";
				generate(propertyFileStr, outDirectory, packageName, className);
				
				//SAFSTextResourceBundle_en_US.properties
				propertyFileStr = outDirectory+File.separator+"org"+File.separator+"safs"+File.separator+"SAFSTextResourceBundle_en_US.properties";
				generate(propertyFileStr, outDirectory, packageName, className);
			}else{
				System.err.println("Cannot deduce the java project directory!");
			}
		}else if(urlFile.endsWith(".jar")){
			System.err.println("Load class from the jar file, cannot add java source in jar file.!");
		}
		
	}
	
	/**
	 * 
	 * @param propertyFileStr String, the absolute properties file name
	 * @param outDirectory String, the absolute path to put the generated java file
	 * @param packageName String, the package name for the generated java file
	 * @param className String, the java class name
	 */
	private static void generate(String propertyFileStr, String outDirectory, String packageName, String className){
		//Read the properties file
		Properties props = new Properties();
		File propertyFile = null;
		String resourceBundleName = null;
		InputStream ins = null;
		try {
			propertyFile = new CaseInsensitiveFile(propertyFileStr).toFile();
			ins = new FileInputStream(propertyFile);
			resourceBundleName = propertyFile.getName().substring(0, propertyFile.getName().lastIndexOf(".properties"));
			//remove the language_country, example en_US, zh_CN, fr_FR
			resourceBundleName = resourceBundleName.replaceAll("_[a-z][a-z]_[A-Z][A-Z]|_[a-z][a-z]", "");
			props.load(ins);
		} catch (Exception e) {
			System.err.println(StringUtils.debugmsg(e));
			return;
		}finally{
			if(ins!=null) try {ins.close();} catch (IOException e) {}
		}
		
		//Open the java file to write.
		String clazzName = className;
		if(clazzName==null || clazzName.trim().isEmpty()){
			if(resourceBundleName.equals("failedSAFSTextResourceBundle")) clazzName = CLASS_FAILKEYS;
			else if(resourceBundleName.equals("SAFSTextResourceBundle")) clazzName = CLASS_GENKEYS;
			else clazzName = resourceBundleName;
		}
		
		String keysFile = clazzName+".java";
		BufferedWriter out = null;
		try {
			String parent = outDirectory.trim();
			if(packageName!=null && !packageName.trim().isEmpty()){
				if(!parent.endsWith(File.separator)) parent = parent+File.separator;
				parent = parent+packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
			}
			keysFile = new CaseInsensitiveFile(parent, keysFile).getAbsolutePath();
			out = FileUtilities.getUTF8BufferedFileWriter(keysFile);
		} catch (Exception e) {
			System.err.println(StringUtils.debugmsg(e));
			return;
		}
		
		try{
			String see = "";
			if(clazzName.equals(CLASS_FAILKEYS)) see = CLASS_FAILKEYS_SEE;
			else if(clazzName.equals(CLASS_GENKEYS)) see = CLASS_GENKEYS_SEE;
			StringBuffer sb = new StringBuffer();
			
			if(packageName!=null && !packageName.isEmpty()) sb.append("package "+packageName+";\n");
			sb.append("/**\n" +
					  " * DO NOT modified manually! Generated by "+KEYSFileGenerator.class.getName()+" automatically.<br>\n" +
					  " * Constants for string resource IDs in "+resourceBundleName+"<br>\n" +
					  " * "+see+"\n" +
					  " */\n");
			sb.append("public class "+clazzName+"{\n");
			sb.append("\t/** \""+resourceBundleName+"\" Name of resource bundle supported by this class.*/\n");
			sb.append("\tpublic static final String RESOURCE_BUNDLE = \""+resourceBundleName+"\";\n");
			sb.append("\t//String IDs\n");
			
			//Write each key as a variable to the java file, and write the corresponding value as the comment of that variable.
			//public static final String NO_SUBSTRINGS	= "no_substrings";
			List<String> keys = new ArrayList<String>();
			for(Object key: props.keySet())	keys.add((String) key);
			Collections.sort(keys);
			String keyVariable = null;
			String temp = null;
			int tabNumber = 0;
			
			for(String key:keys){
				if(!key2IrregularVariable.containsKey(key)){
					//replace "." by "_"
					keyVariable = key.toUpperCase().replaceAll("\\.", "_");
					//for ending number or numberWithLetter, add a underscore "_" before.
					//example, replace "1", "2", "1A", "2B" by "_1" "_2" "_1A" "_2B"
					keyVariable = keyVariable.replaceAll("([0-9]+[A-Z]*)$", "_$1");					
				}else{
					keyVariable = key2IrregularVariable.get(key);
				}
				temp = "public static final String "+keyVariable;
				tabNumber = DEFAULT_TAB_NUMBER-temp.length()/TAB_LENGTH;
				//Add value as comment
				sb.append("\t/** "+props.getProperty(key)+" */\n");
				//Add key as variable
				sb.append("\t"+temp);
				for(int i=0;i<tabNumber;i++) sb.append("\t");
				sb.append("= \""+key+"\";\n");
			}
			sb.append("\n");
			
			sb.append("}\n");
			out.write(sb.toString());
			out.flush();
			System.out.println(sb.toString());
			
		}catch(Exception e){
			System.err.println(StringUtils.debugmsg(e));
		}finally{
			if(out!=null) try {out.close();} catch (IOException e) {}
		}
	}
}
