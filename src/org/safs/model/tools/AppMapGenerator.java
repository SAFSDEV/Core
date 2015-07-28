/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import org.safs.text.CaseInsensitiveHashtable;
import org.safs.text.INIFileReader;

/**
 * Process a standard SAFS AppMap and create a static Driver-compatible Map with matching Component references.
 * 
 * For a SAFS App Map named "JSAFSTest.map" this will create "JSAFSTestMap.java".
 * <p>
 * This Class is a standalone Java application typically invoked as shown below in 'main'.
 * 
 * @author Carl Nagle FEB 02, 2011
 * @see org.safs.model.examples.advanced.JSAFSMap
 */
public class AppMapGenerator {
	
	static File _input;
	static String _inputArg;
	static String _inputRoot;
	static File _output;
	static String _outputArg;
	static String _packageArg;
	static String _encodingArg;
	static BufferedWriter writer;
	static INIFileReader reader;
	static CaseInsensitiveHashtable types = new CaseInsensitiveHashtable();
	
	static final String ARG_IN 		 = "-in";
	static final String ARG_OUT 	 = "-out";
	static final String ARG_PACKAGE  = "-package";
	static final String ARG_ENCODING = "-encoding";
	static final String HELP_OUT = "\nArguments: \n-in AppMapPath\n-out JavaOutDirectory\n[-package JavaPackageName]\n[-encoding AppMapEncoding]\n";
	static final String GENERIC_OBJECT = "GenericObject";
	static final String STRING_OBJECT = "String";
	static final String SEMICOLON = ";";
	
	static void putItem(String ProperName){
		types.put(ProperName, ProperName);
	}
	static { //static startup execution
		putItem("CheckBox");
		putItem("ComboBox");
		putItem("ComboEditBox");
		putItem("ComboListBox");
		putItem("Database");
		putItem("DotNetMenu");
		putItem("DotNetTree");
		putItem("DotNetTable");
		putItem("EditBox");
		putItem("FlexColor");
		putItem("FlexDate");
		putItem("FlexNumericStepper");
		putItem("FlexSlider");
		types.put("Generic", "GenericObject");
		putItem("GenericObject");
		putItem("GraphControl");
		putItem("HTML");
		putItem("HTMLDocument");
		putItem("HTMLImage");
		putItem("HTMLLink");
		putItem("HTMLTable");
		putItem("JavaMenu");
		putItem("JavaTable");
		putItem("JavaTree");
		putItem("Label");
		putItem("ListBox");
		putItem("ListView");
		putItem("MenuPath");
		putItem("PopupMenu");
		putItem("PushButton");
		putItem("RadioButton");
		putItem("ScrollBar");
		putItem("SSTree");
		putItem("TabControl");
		putItem("TableView");
		putItem("CheckBox");
		putItem("ToolBar");
		putItem("TreeView");
		putItem("VSFlexGrid");
		putItem("Window");
	}
	/** Presently, default constructor instancing is not supported. */
	private AppMapGenerator(){}
	
	static void writeln(String line)throws IOException{
		writer.write(line); 
		writer.newLine();
	}
	
	/**
	 * java org.safs.model.tools.AppMapGenerator [args]
	 *  </ul>
	 *  <p>
	 *  args:
	 *  <p><dl>
	 *  <dt>-in AppMapPath</dt>
	 *  <dd><p>
	 *  REQUIRED relative or absolute path to the SAFS App Map file to process.
	 *  <p>
	 *  Examples:
	 *  <p><ul>
	 *  -in "TIDTest.map"<br>
	 *  -in "C:/SAFS/Project/Datapool/TIDTest.map"
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
	 *  java org.safs.model.tools.AppMapGenerator -in "TIDTest.map" -out "c:/safs/datastorej/"
	 *  java org.safs.model.tools.AppMapGenerator -in "TIDTest.map" -out "c:/safs/datastorej/" -encoding "UTF-8"
	 *  java org.safs.model.tools.AppMapGenerator -in "c:/safs/project/datapool/tidtest.map" -out "c:/safs/datastorej"
	 *  java org.safs.model.tools.AppMapGenerator -in "tidtest.map" -out "c:/safs/datastorej/org/safs/sample/" -package "org.safs.sample"
	 *  </pre></ul>
	 */
	public static void main(String[] args) {
		try{
			if(args.length < 4) throw new Exception(HELP_OUT);
			String arg;
			for (int i=0;i<args.length;i++){
				arg = args[i];
				if(arg.equalsIgnoreCase(ARG_IN)){
					_inputArg = args[++i];
				}else if(arg.equalsIgnoreCase(ARG_OUT)){
					_outputArg = args[++i];
				}else if(arg.equalsIgnoreCase(ARG_PACKAGE)){
					_packageArg = args[++i];
				}else if(arg.equalsIgnoreCase(ARG_ENCODING)){
					_encodingArg = args[++i];
				}else{
					throw new Exception(HELP_OUT); 
				}
			}
			if(_inputArg == null || _outputArg == null) throw new Exception(HELP_OUT);
			_input = new File(_inputArg);
			if(!(_input.isFile()&& _input.canRead())) throw new IllegalArgumentException("Invalid Input File: "+ _inputArg);
			_inputRoot = _input.getName();
			if(_inputRoot.lastIndexOf('.') > -1) _inputRoot = _inputRoot.substring(0,_inputRoot.lastIndexOf('.'))+"Map";
			_inputRoot = _inputRoot.replace(".","_");
			_output = new File(_outputArg);
			if(!(_output.isDirectory())) throw new IllegalArgumentException("Invalid Output Directory: "+ _outputArg);
			_output = new File(_output, _inputRoot+".java");
			
			writer = new BufferedWriter(new FileWriter(_output));			
			if(_packageArg instanceof String) {
				writeln("package "+ _packageArg +";");
			}
			writeln("import org.safs.model.components.*;");
			writer.newLine();
			writeln("/** A Driver Map reference for SAFS App Map: '"+ _inputArg +"' */");
			writeln("public final class "+ _inputRoot +" {");
			writer.newLine();
			writeln("    /** No use for a default constructor. */");
			writeln("    private "+ _inputRoot +"(){}");
			// Start Processing App Map
			if(_encodingArg instanceof String){
				reader = new INIFileReader(_input, 
										   reader.IFR_MEMORY_MODE_STORED,
										   false,
										   _encodingArg);
			}else{
				reader = new INIFileReader(_input, reader.IFR_MEMORY_MODE_STORED);
			}
			Vector windows = reader.getSections();
			Enumeration loop = windows.elements();
			Enumeration loop2;
			String window;
			String windowName;
			Vector items;
			String item;
			String itemName;
			String itemVal;
			String itemType;
			String casedType;
			int typeindex;
			while(loop.hasMoreElements()){
				window = loop.nextElement().toString();
				windowName = window.replace(".", "_");
				writer.newLine();
				items = reader.getItems(window);
				if(window.length()==0) window = "_EMPTY_";
				if(items == null || items.isEmpty()) continue;
				if(window.equalsIgnoreCase(reader.IFR_DEFAULT_MAP_SECTION)){
					writeln("    /** The Names of ApplicationConstants  in the runtime App Map(s). */");
					writeln("    public static class Constant {");
					writer.newLine();
					writeln("        /** No use for a default constructor. */");
					writeln("        private Constant(){}");
				}else{
					writeln("    /** The Names of Constants and Usable ComponentFunctions objects for this window. */");
					writeln("    public static class "+ windowName +" {");
					writer.newLine();
					writeln("        /** No use for a default constructor. */");
					writeln("        private "+ windowName +"(){}");
				}
				writer.newLine();
				if(! window.equalsIgnoreCase(reader.IFR_DEFAULT_MAP_SECTION)){
					writeln("        /** Retrieve the window's string name without resorting to the Window ComponentFunction. */");
					writeln("        public static String getName(){ return \""+ window +"\";}");
					writeln("        /** The ComponentFunction object for this Window. */");
					writeln("        public static final Window "+ windowName +" = new Window(\""+ window +"\");");
				}
				loop2 = items.elements();
				while(loop2.hasMoreElements()){
					itemType = null;
					item = loop2.nextElement().toString();
					itemName = item.replace(".", "_");
					if(item.equalsIgnoreCase(window)) continue;
					itemVal = reader.getAppMapItem(window, item);
					if(window.equalsIgnoreCase(reader.IFR_DEFAULT_MAP_SECTION)) {
						writeln("        /** The String name of this Constant. */");
						writeln("        public static final String "+ itemName +" = new String(\""+ item +"\");");
						continue;
					}
					typeindex = itemVal.toLowerCase().lastIndexOf("type=");
					if(typeindex > -1){
						itemType=itemVal.substring(typeindex + 5);
						itemType=itemType.split(SEMICOLON)[0];
						casedType = (String)types.get(itemType);
						if(casedType != null) itemType = casedType;
					}
					if(itemType == null) {
						writeln("        /** The String name of this Constant. */");
						writeln("        public static final String "+ itemName +" = new String(\""+ item +"\");");
					}else{
						writeln("        /** The ComponentFunction object for this named Component. */");
						writeln("        public static final "+ itemType +" "+ itemName +" = new "+ itemType +"("+ window +", \""+ item +"\");");
					}
				}				
				writeln("    }");
			}			
			// Complete
			writeln("}");
			writer.flush();
			writer.close();
		}catch(IndexOutOfBoundsException x){
			System.err.println(HELP_OUT);
		}catch(Exception x){
			System.err.println(x.toString());
		}
	}

}
