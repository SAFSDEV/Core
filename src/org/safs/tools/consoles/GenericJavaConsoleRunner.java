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
package org.safs.tools.consoles;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.safs.android.auto.lib.Process2;
import org.safs.tools.consoles.JavaJVMConsole;

/**
 * Requires 1 command-line arg, minimum:
 * <pre>
 *     -binary "path/To/Executable"
 * </pre>
 * Can also take an optional command-line arg:
 * <pre>
 *     -consoleTitle "Window Title"
 * </pre>
 * The remainder of any command-line args will be passed along to the Process.exec call.
 * <p>
 * @author Carl Nagle
 */
public class GenericJavaConsoleRunner extends JavaJVMConsole{

	/** "-consoleTitle" */
	public static final String ARG_TITLE = "-consoleTitle";

	/** "-binary" */
	public static final String ARG_BINARY = "-binary";
	
	private static String binary = null;
	
			
	private GenericJavaConsoleRunner(){
		super();
	}

    protected String[] processArgs(String[] args) throws IOException{
    	ArrayList<String> list = new ArrayList<String>();
    	String arg = null;
    	for(int i=0; i< args.length;i++){
    		arg = args[i];
    		if(ARG_TITLE.equalsIgnoreCase(arg)){
    			if(++i < args.length){
    				arg = args[i];
    				setTitle(arg);
    			}
    		}else if(ARG_BINARY.equalsIgnoreCase(arg)){
    			if(++i < args.length){
    				arg = args[i];
    				binary = arg;
    				list.add(0, binary);
    			}
    		}else{
    			list.add(arg);
    		}
    	}
    	return list.toArray(new String[]{});
    }

	/**
	 * Is expected to be able to run in its own standalone Java process.
     * Requires 1 command-line arg, minimum:
     * <pre>
     *     -binary "path/To/Executable"
     * </pre>
     * Can also take an optional command-line arg:
     * <pre>
     *     -consoleTitle "Window Title"
     * </pre>
     * The remainder of any command-line args will be passed along to the Process.exec call.<br>
	 * Other JVM params ( -Dwebdriver...) will have already been applied to this JVM process.
	 */
	public static void main(String[] args) {
		String[] passArgs = new String[0];
		GenericJavaConsoleRunner console;
		try{
			console = new GenericJavaConsoleRunner();
			passArgs = console.processArgs(args);			
			if(binary == null){
				throw new IllegalArgumentException("No executable binary was specified on the command-line:" +
			                                       "\n\t-binary \"path/To/Executable/Binary\"");}
			Process2 proc = new Process2(Runtime.getRuntime().exec(passArgs), false).forwardOutput();
			int exitValue = 0;
			while(!console.shutdown){				
				try{ 
					exitValue = proc.exitValue();
					console.shutdown = true;
				}catch(IllegalThreadStateException its){try{Thread.sleep(100);}catch(Exception x){}}				
			}
			console.setVisible(false);
			System.exit(exitValue);
		}catch(IOException io){
			io.printStackTrace();
		} catch (SecurityException s) {
			s.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}				
	}
}
