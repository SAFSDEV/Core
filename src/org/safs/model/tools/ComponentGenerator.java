/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model.tools;

/**
 * History:
 * Apr 27, 2014	(sbjlwa) Move most parameters to main method. If we define them as class static fields, they may keep
 *                       the value in the previous call.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.safs.text.CaseInsensitiveHashtable;
import org.safs.text.CaseInsensitiveStringVector;
import org.safs.text.INIFileReader;

/**
 * Process a standard SAFS AppMap file and create a static JSAFS-compatible AppMap with matching 
 * org.safs.model.Component references.  These contain just item names and no recognition string 
 * information.
 * <p>
 * For a SAFS App Map named "App.map" this will create "AppMap.java".
 * <p>
 * If the input parameter is a directory, and not a specific file, then ALL *.map files will be 
 * processed into the resulting java file.
 * <p>
 * This Class is a standalone Java application typically invoked as shown below in static method 'main' below.
 * 
 * @author canagl Oct 15, 2013
 * @see org.safs.model.Component
 * @see #main(String[])
 */
public class ComponentGenerator{
	static BufferedWriter writer  = null;

	static final String ARG_IN 		 = "-in";
	static final String ARG_OUT 	 = "-out";
	static final String ARG_NAME 	 = "-name";
	static final String ARG_PACKAGE  = "-package";
	static final String ARG_ENCODING = "-encoding";
	static final String HELP_OUT = "\nArguments: \n-in AppMapPath\n-out JavaOutDirectory\n[-name Alternate Name of Class]\n[-package JavaPackageName]\n[-encoding AppMapEncoding]\n";
	static final String EMPTY_SECTION = "_EMPTY_";

	static final String BASH 		 = "!";
	static final String SEMI 		 = ";";
	static final String HASH 		 = "#";

	/** Presently, default constructor instancing is not supported. */
	private ComponentGenerator(){}
	
	static void writeln(String line)throws IOException{
		writer.write(line); 
		writer.newLine();
	}
	
	/**
	 * java org.safs.model.tools.ComponentGenerator [args]
	 *  </ul>
	 *  <p>
	 *  args:
	 *  <p><dl>
	 *  <dt>-in AppMapPath</dt>
	 *  <dd><p>
	 *  REQUIRED relative or absolute path to the SAFS App Map file or directory to process.
	 *  <p>
	 *  Examples:
	 *  <p><ul>
	 *  -in "TIDTest.map"<br>
	 *  -in "C:/SAFS/Project/Datapool/TIDTest.map"<br>
	 *  -in "C:/SAFS/Project/Datapool"
	 *  </ul>
	 *  <p>
	 *  <dt>-out OutputDirectory</dt>
	 *  <dd><p>
	 *  REQUIRED path to the directory in which to write the new Java Class file.
	 *  <p>
	 *  Example:
	 *  <p><ul>
	 *  -out "C:/SAFS/DatastoreJ/"
	 *  </ul>
	 *  <p>
	 *  <dt>-name classname</dt>
	 *  <dd><p>
	 *  OPTIONAL Alternate name to give the generated class.
	 *  <p>
	 *  Example:
	 *  <p><ul>
	 *  -name Map<br>
	 *  -name AppMap<br>
	 *  -name Comps
	 *  </ul>
	 *  <p>
	 *  <dt>-package JavaPackageName</dt>
	 *  <dd><p>
	 *  OPTIONAL package name to use for the output Java Class file. When not specified, 
	 *  no package name is written.  The class is part of the nameless (default) Java package.
	 *  <p>
	 *  Example:
	 *  <p><ul>
	 *  -package "org.safs.model.examples.advanced"
	 *  </ul>
	 *  <p>
	 *  <dt>-encoding AppMapEncoding</dt>
	 *  <dd><p>
	 *  OPTIONAL character encoding used for the existing SAFS App Map. This argument is 
	 *  actually REQUIRED if the SAFS App Map is encoded in something other than the System default 
	 *  character encoding.
	 *  <p>
	 *  Examples:
	 *  <p><ul>
	 *  -encoding "UTF-8"<br>
	 *  -encoding "UTF-16"<br>
	 *  -encoding "ISO-SomethingOrOther"<br>
	 *  </ul>
	 *  </dl>
	 *  <p>
	 *  Putting it all together for some sample invocations:
	 *  <p><ul><pre>
	 *  java org.safs.model.tools.ComponentGenerator -in "TIDTest.map" -out "c:/safs/datastorej/"
	 *  java org.safs.model.tools.ComponentGenerator -in "TIDTest.map" -out "c:/safs/datastorej/" -encoding "UTF-8"
	 *  java org.safs.model.tools.ComponentGenerator -in "c:/safs/project/datapool/tidtest.map" -out "c:/safs/datastorej"
	 *  java org.safs.model.tools.ComponentGenerator -in "tidtest.map" -out "c:/safs/datastorej/org/safs/sample/" -package "org.safs.sample"
	 *  </pre></ul>
	 */
	public static void main(String[] args) {
		String window = null;
		String component  = null;
		
		File _inputFile  = null;
		File[] maps  = null;
//		File mapfile  = null;
		String _inputArg  = null;
		String _inputRoot  = null;
		File _output  = null;
		String _outputArg  = null;
		String _nameArg  = null;
		String _packageArg  = null;
		String _encodingArg  = null;

		INIFileReader filereader  = null;
		
		try{
			if(args.length < 4) throw new Exception(HELP_OUT);
			String arg;
			for (int i=0;i<args.length;i++){
				arg = args[i];
				if(arg.equalsIgnoreCase(ARG_IN)){
					_inputArg = args[++i];
				}else if(arg.equalsIgnoreCase(ARG_OUT)){
					_outputArg = args[++i];
				}else if(arg.equalsIgnoreCase(ARG_NAME)){
					_nameArg = args[++i];
				}else if(arg.equalsIgnoreCase(ARG_PACKAGE)){
					_packageArg = args[++i];
				}else if(arg.equalsIgnoreCase(ARG_ENCODING)){
					_encodingArg = args[++i];
				}else{
					throw new Exception(HELP_OUT); 
				}
			}
			if(_inputArg == null || _outputArg == null) throw new Exception(HELP_OUT);
			
			_inputFile = new File(_inputArg);
			
			if(_inputFile.isFile()){
				if(!_inputFile.canRead()) throw new IllegalArgumentException(
				   "Invalid Input File: "+ _inputArg
				);
				maps = new File[]{_inputFile};
			}else {				
				if(!_inputFile.isDirectory()) throw new IllegalArgumentException(
				   "Input argument is not a valid File or Directory."
				);
				maps = _inputFile.listFiles(new FilenameFilter(){
					public boolean accept(File arg0, String arg1) {
						return arg1.toLowerCase().endsWith(".map");
					}});
				if(maps == null || maps.length==0) throw new IllegalArgumentException(
				   "Input argument contains no .map files for processing."
				);
			}					
			_inputRoot = _nameArg == null ? _inputFile.getName(): _nameArg;
			
			if(_inputRoot.lastIndexOf('.') > -1) _inputRoot = _inputRoot.substring(0,_inputRoot.lastIndexOf('.'))+"Map";
			_inputRoot = normalize(_inputRoot);
			
			_output = new File(_outputArg);
			if(!(_output.isDirectory())) throw new IllegalArgumentException("Invalid Output Directory: "+ _outputArg);
			_output = new File(_output, _inputRoot+".java");
			
			CaseInsensitiveHashtable  sections = new CaseInsensitiveHashtable();
			CaseInsensitiveStringVector existingitems;//does not permit duplicated string
			for(File afile:maps){			
				// Start Processing App Map
				try{
					if(_encodingArg instanceof String){
						filereader = new INIFileReader(afile, 
												   filereader.IFR_MEMORY_MODE_STORED,
												   false,
												   _encodingArg);
					}else{
						filereader = new INIFileReader(afile, filereader.IFR_MEMORY_MODE_STORED);
					}
				}catch(NullPointerException ignore){
					// if the file is empty we get this exception
					continue;
				}
				Vector windows = filereader.getSections();
				Enumeration loop = windows.elements();
				Vector sectionitems;
				Enumeration loop2;
				// Section/Window names
				while(loop.hasMoreElements()){
					window = loop.nextElement().toString();
					component = window.trim();
					if(component.startsWith(BASH)||
					   component.startsWith(SEMI)||
					   component.startsWith(HASH)) continue;
					sectionitems = filereader.getItems(window);
					if(sectionitems == null || sectionitems.isEmpty()) continue;
					
					if(window.length()==0) window = INIFileReader.IFR_DEFAULT_MAP_SECTION;
					
					if(sections.containsKey(window)){
						existingitems = (CaseInsensitiveStringVector) sections.get(window);
					}else{
						existingitems = new CaseInsensitiveStringVector();
					}
					loop2 = sectionitems.elements();
					// items/Component names
					while(loop2.hasMoreElements()){
						component = loop2.nextElement().toString();
//						if(componentName.equalsIgnoreCase(windowName)) continue;
						component = component.trim();
						if(component.startsWith(BASH)||
						   component.startsWith(SEMI)||
						   component.startsWith(HASH)) continue;
						existingitems.add(component);
					}
					sections.put(window, existingitems);
				}
				try{filereader.close();}catch(Throwable t){}
			}			

			writer = new BufferedWriter(new FileWriter(_output));			
			if(_packageArg instanceof String) {
				writeln("package "+ _packageArg +";");
			}
			writeln("import org.safs.model.Component;");
			writeln("import org.safs.model.tools.RuntimeDataAware;");
			writeln("import org.safs.tools.RuntimeDataInterface;");
			writer.newLine();
			writeln("/** *** DO NOT EDIT THIS FILE ***<br>");
			writeln("    THIS FILE IS GENERATED AUTOMATICALLY by org.safs.model.tools.ComponentGenerator!<br><p>");
			writeln("    A JSAFS AppMap reference for SAFS App Map: '"+ _inputArg +"' */");
			writeln("public final class "+ _inputRoot +" implements RuntimeDataAware {");
			writer.newLine();
			writeln("    private static RuntimeDataInterface dataInterface = null;");
			writer.newLine();
			writeln("    /** Called internally as part of the bootstrap process of Dependency Injection. */");
			writeln("    public void setRuntimeDataInterface(RuntimeDataInterface helper){");
			writeln("        dataInterface = helper;");         
			writeln("    }");
			writer.newLine();			
			writeln("    // The Names of ApplicationConstants:");
			Vector constants = (Vector)sections.get(INIFileReader.IFR_DEFAULT_MAP_SECTION);
			String normalizedWindow = null;//used as class-name or field-name in generated java file
			String normalizedComponent = null;//used as class-name or field-name in generated java file
			try{
				Enumeration constantsloop = constants.elements();
				while(constantsloop.hasMoreElements()){
					component = (String)constantsloop.nextElement();
					normalizedComponent = normalize(component);
					if(component == null || component.isEmpty()) continue;
					writer.newLine();
					writeln("    /** \""+ component +"\" */");
					writeln("    public static final String "+ normalizedComponent +" = \""+ component +"\";");
					writer.newLine();			
					writeln("    /** The resolved runtime value of constant '"+ component +"', or null. */");
					writeln("    public static String "+ normalizedComponent +"(){");
					writeln("        try{ return dataInterface.getVariable(\""+ component +"\"); }");
					writeln("        catch(Exception x){ return null; }");
					writeln("    }");
				}	
			}catch(NullPointerException ignore){
				// there are no sections in the App Map!
			}
			Enumeration loop = sections.keys();
			Enumeration loop2;
			Vector items;
			try{
				writer.newLine();
				writeln("    // The Names of Window and Child Component objects: ");
				while(loop.hasMoreElements()){
					window = (String)loop.nextElement();
					normalizedWindow = normalize(window);
					//if(window.equalsIgnoreCase(INIFileReader.IFR_DEFAULT_MAP_SECTION)) continue;
					writer.newLine();
					items = (Vector)sections.get((window));
					if(items == null || items.isEmpty()) continue;
					writeln("    /** \""+ window +"\" Component and its children. */");
					writeln("    public static class "+ normalizedWindow +" {");
					writer.newLine();
					writeln("        // No use for a default constructor.");
					writeln("        private "+ normalizedWindow +"(){}");
					writer.newLine();
					writeln("        /** \""+ window +"\" Window Component itself. */");
					writeln("        public static final Component "+ normalizedWindow +" = new Component(\""+ window +"\");");
					loop2 = items.elements();
					try{
						while(loop2.hasMoreElements()){
							component = (String) loop2.nextElement();
							normalizedComponent = normalize(component);
							if(component.equalsIgnoreCase(window)) continue;
							writer.newLine();
							writeln("        /** \""+ component +"\" Component in \""+ window +"\". */");
							writeln("        public static final Component "+ normalizedComponent +" = new Component("+ normalizedWindow +", \""+ component +"\");");
						}	
					}catch(NullPointerException ignore){
						// no items to list
					}
					writeln("    }");
				}	
			}catch(NullPointerException ignore){
				// there are no items in the section?
			}
			// Complete
			writeln("}");
			writer.flush();
			writer.close();
		}catch(IndexOutOfBoundsException x){
			System.err.println(HELP_OUT);
		}catch(Exception x){
			System.err.println(x.toString());
			x.printStackTrace();
		}
	}
	
	/**
	 * The purpose is to make a valid java variable/class name.<br>
	 * Replace all invalid characters (not suitable for a java variable/class name) with "_".<br>
	 * If the string start with numbers, remove those numbers.<br>
	 * Valid characters are a-z, A-Z, 0-9 and _.<br>
	 * As a map can contain "." in the section-name or item-name, and this program<br>
	 * will convert the map to a java file, and these names are going to be used as<br>
	 * java class-name or field-name, but "." is not valid for class/field name, we<br>
	 * must convert it to a valid character "_".<br>
	 * 
	 * @param name String, the string to normalize
	 * @return String, the normalized string
	 */
	static String normalize(String name){
		String normalizedStr = name;
		if(normalizedStr==null) return null;
		//replace all non-valid characters by "_"
		normalizedStr = normalizedStr.replaceAll("[^a-zA-Z0-9_]", "_");
		//remove the leading digitals
		while(!normalizedStr.isEmpty() && Character.isDigit(normalizedStr.charAt(0))){
			normalizedStr = normalizedStr.substring(1);
		}
		//if the name is a number, the normalizedStr will be empty now,
		//add a "_" in front of name to create a valid string
		if(normalizedStr.isEmpty()) normalizedStr = "_"+name;
		
		return normalizedStr;
	}
}
