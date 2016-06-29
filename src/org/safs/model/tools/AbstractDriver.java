/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.tools;

/**
 * History:
 * JUN 11, 2015	(Lei Wang) 	Add method processExpression(): wrap JSAFSDriver.processExpression().
 *
 */
import java.io.File;

import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.model.ComponentFunction;
import org.safs.model.DriverCommand;
import org.safs.model.commands.DDDriverCommands;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.drivers.InputProcessor;
import org.safs.tools.drivers.JSAFSDriver;

/**
 * @author Carl Nagle
 *
 */
public abstract class AbstractDriver {

	/** "AppMap.order" */
	public static final String DEFAULT_APPMAP_ORDER = "AppMap.order";
	/** "testdesigner.appmap.order" */
	public static final String APPMAP_ORDER_PROPERTY = "testdesigner.appmap.order";
	/** "testdesigner.appmap.files" */
	public static final String APPMAP_FILES_PROPERTY = "testdesigner.appmap.files";
	
	/** "!" */
	static final String BASH 		 = "!";
	/** ";" */
	static final String SEMI 		 = ";";
	/** "#" */
	static final String HASH 		 = "#";
	/** "," */
	static final String COMMA 		 = ",";
	
	/**
	 * Holds an instance of an AbstractDriver for non-static method access.
	 */
	protected static AbstractDriver _instance = null;
	
	/**
	 * Internal Use Only<p>
	 * Seek an AppMap.order file in the Datapool directory.
	 * <p>By default the file sought is AppMap.order.  However, the user can specify an alternate 
	 * AppMap order file or AppMap files by using the following JVM Argument:
	 * <p><ul>Examples:
	 * <p>
	 * <li>-Dtestdesigner.appmap.order=AppMap_en.order, or
	 * <li>-Dtestdesigner.appmap.files=SAMPLEApp.map,SAMPLEApp_en.map, or
	 * <li>-Dtestdesigner.appmap.order=AppMap_ja.order, or
	 * <li>-Dtestdesigner.appmap.order=AppMap_cn.order
	 * <li>etc..
	 * </ul>
	 * If found, execute SetApplicationMap on each AppMap in the order listed in the file.
	 */
	protected void preloadAppMaps(){
		String dir = null;
		String filename = null;
		String[] files = null;
		boolean filesarg = false;
		
		try{ 
			if(jsafs() != null) { dir = jsafs().getDatapoolDir(); }
			else{
				// TODO: Use InputProcessor if JSAFSDriver not present.
				dir = processor().getDatapoolDir();
			}
			filename = DEFAULT_APPMAP_ORDER;
			String jvmarg = null;
			jvmarg = System.getProperty(APPMAP_ORDER_PROPERTY);
			if(jvmarg != null && jvmarg.length() > 0) {
				System.out.println("Alternate "+ jvmarg +" App Map ordering requested.");
				filename = jvmarg;
			}
			jvmarg =  System.getProperty(APPMAP_FILES_PROPERTY);
			if(jvmarg != null && jvmarg.length() > 0) {
				filesarg = true;
				if (jvmarg.contains(SEMI))
					files = jvmarg.split(SEMI);
				if (jvmarg.contains(COMMA))
					files = jvmarg.split(COMMA);
				System.out.println("Alternate "+ jvmarg +" App Map file(s) requested.");				
			}			
		}catch(Throwable ignore){}
		
		if (filesarg){
			
			if (files != null && files.length > 0) {
				for (String aFile: files){
					File mapfile = new CaseInsensitiveFile(dir, aFile).toFile();
					if (mapfile.isFile()) {
						System.out.println("Registering "+ aFile);
						try{ runDriverCommand(DDDriverCommands.SETAPPLICATIONMAP_KEYWORD, aFile);}catch(Throwable t){}
					} else {
						System.out.println(aFile + " does not appear to exist in " + dir);
					}
				}
			}
			
		} else if (filename != null) {
			System.out.println("Seeking "+filename+" in directory: "+ dir);
			File order = new CaseInsensitiveFile(dir, filename).toFile();
			if(order.isFile()){
				try{
					String[] lines = FileUtilities.readLinesFromFile(order.getAbsolutePath());				
					for(String line:lines){
						line = line.trim();
						if(line.length() > 0){
							if(line.startsWith(BASH)) continue;
							if(line.startsWith(SEMI)) continue;
							if(line.startsWith(HASH)) continue;
							System.out.println(filename +" registering "+ line);
							try{ runDriverCommand(DDDriverCommands.SETAPPLICATIONMAP_KEYWORD, line);}catch(Throwable t){}
						}
					}
				}catch(Exception x){}
			} else {
				System.out.println(filename + " does not appear to exist in " + dir);
			}
		}else{
			System.out.println(filename +" does not appear to exist in "+dir);
		}
	}

	/**
	 * Used internally to handle the potential that passed parameters may include embedded  
	 * SAFS Expressions or embedded SAFS variable references.  This method is automatically 
	 * called by the internal SAFS DriverCommands or SAFS ComponentFunctions calls and would 
	 * normally not be called by the user directly.
	 * <p>
	 * @param parameters String[] of optional parameters. Can be (String[])null.
	 * @return String[] after expressions have been processed, if any.
	 * Can return the input parameters array unmodified--including null.
	 */	
	protected String[] processExpressions(String... parameters){
		if(parameters instanceof String[] && parameters.length > 0){
			String parm = null;
			for(int i=0;i < parameters.length;i++){
				parm = parameters[i];
				try{
					if(parm.trim().length() > 0){			
						parameters[i] = processExpression(parm);
					}
				}catch(NullPointerException np){
					// if someone passed in a single null String as the array
					if(i==0 && parameters.length==1){
						parameters = new String[0];
						break;
					}
				}
			}
		}
		return parameters;
	}
	
	/**
	 * If "Expressions" is turned on, evaluate expression as both "math" and "DDVariable" string.
	 * Otherwise this method will do nothing, just return the original expression string.
	 */
	protected String processExpression(String expression){
		if(jsafs() != null){ 
			return jsafs().processExpression(expression);//Expression will turn off/on both "math" and "DDVariable" 
			//return jsafs().resolveExpression(expression);//Expression will turn off/on "math", while "DDVariable" will be kept all the time
		}else{
			// TODO: Use InputProcessor if JSAFSDriver not present.
			String sep = processor().getTestRecordData().getSeparator();
			String exp = processor().getVarsInterface().resolveExpressions(expression, sep);
			// LeiWang: should we always remove the wrapping double-quote? If the original expression is double-quoted, then we should not remove them.
			return StringUtils.removeWrappingDoubleQuotes(exp);
		}
	}

	protected abstract JSAFSDriver jsafs();	
	protected abstract InputProcessor processor();
	
	/**
	 * @return DriverInterface currently being used.<br>
	 * Could be a JSAFSDriver.  Could be an InputProcessor.<br>
	 * Subclasses should override to support other DriverInterface instances not tracked in this superclass.  
	 * Can be null if none are set.
	 * @see #setIDriver(DriverInterface)
	 * @see JSAFSDriver
	 * @see InputProcessor
	 */
	public DriverInterface iDriver(){
		if(jsafs() instanceof DriverInterface) return jsafs();
		if(processor() instanceof DriverInterface) return processor();
		return null;
	}
	
	/**
	 * Turn ON Expressions, AppMapChaining, and AppMapResolve.
	 */
	protected void preloadAppMapExpressions(){
		try{ runDriverCommand(DDDriverCommands.EXPRESSIONS_KEYWORD, "ON");}catch(Throwable t){}
		try{ runDriverCommand(DDDriverCommands.APPMAPCHAINING_KEYWORD, "ON");}catch(Throwable t){}
		try{ runDriverCommand(DDDriverCommands.APPMAPRESOLVE_KEYWORD, "ON");}catch(Throwable t){}
	}
	
	/**
	 * Run a SAFS Component Function on a top-level component (Window).  
	 * The particular safs action takes no additional parameters. 
	 * This method performs SAFS Expression processing on all input parameters.
	 * This method is normally called internally by other methods and classes
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param parent The ComponentFunction parent window name to act on
	 * @return 
	 * @throws Throwable
	 * @see org.safs.StatusCodes#GENERAL_SCRIPT_FAILURE
	 * @see org.safs.StatusCodes#OK
	 * @see org.safs.StatusCodes#SCRIPT_WARNING
	 * @see org.safs.StatusCodes#SCRIPT_NOT_EXECUTED
	 */
	public TestRecordHelper runComponentFunction(String command, String parent) throws Throwable{
		return runComponentFunction(command, parent, parent);
	}

	/**
	 * Run a SAFS Component Function on a child component in a parent (Window).  
	 * The particular safs action takes no additional parameters.
	 * This method performs SAFS Expression processing on all input parameters. 
	 * This method is normally called internally by other methods and classes.
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param child The ComponentFunction child component name to act on
	 * @param parent The ComponentFunction parent window name to act on
	 * @throws Throwable
	 * @see org.safs.StatusCodes#GENERAL_SCRIPT_FAILURE
	 * @see org.safs.StatusCodes#OK
	 * @see org.safs.StatusCodes#SCRIPT_WARNING
	 * @see org.safs.StatusCodes#SCRIPT_NOT_EXECUTED
	 */
	public TestRecordHelper runComponentFunction(String command, String child, String parent) throws Throwable{
		return runComponentFunction(command, parent, child, (String[])null);
	}


	/**
	 * Run a SAFS Component Function on a child component in a parent (Window).
	 * The safs action may take one or more parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * This method is normally called internally by other methods and classes.
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param child The ComponentFunction child component name to act on
	 * @param parent The ComponentFunction parent window name to act on
	 * @param parameters String[] of parameters used by the command.  Can be null.
	 * @throws Throwable
	 * @see org.safs.StatusCodes#GENERAL_SCRIPT_FAILURE
	 * @see org.safs.StatusCodes#OK
	 * @see org.safs.StatusCodes#SCRIPT_WARNING
	 * @see org.safs.StatusCodes#SCRIPT_NOT_EXECUTED
	 */
	public TestRecordHelper runComponentFunction(String command, String child, String parent, String... parameters)throws Throwable{
		command = processExpression(command);
		if(child instanceof String) child =  processExpression(child);
		if(parent instanceof String) parent =  processExpression(parent);
		return runComponentFunctionConverted(command, child, parent, processExpressions(parameters));
	}

	/**
	 * Run the specified DriverCommand requiring zero or more parameters.
	 * @param command -- Cannot be null and is not case-sensitive.
	 * @param parameters -- optional.  Can be (String[])null.
	 * @return TestRecordHelper. Use getStatusCode() and getStatusInfo() for information 
	 * on execution results.
	 * @throws Throwable
	 * @see org.safs.StatusCodes#GENERAL_SCRIPT_FAILURE
	 * @see org.safs.StatusCodes#OK
	 * @see org.safs.StatusCodes#SCRIPT_WARNING
	 * @see org.safs.StatusCodes#SCRIPT_NOT_EXECUTED
	 */
	public TestRecordHelper runDriverCommand(String command, String... parameters) throws Throwable{
		command = processExpression(command);
		return runDriverCommandConverted(command, processExpressions(parameters));
	}

	/**
	 * Run a SAFS Driver Command.
	 * The safs command may take one or more parameters.
	 * This method is normally called internally by overloaded methods and performs no SAFS Expression 
	 * processing on the input parameters.
	 * 
	 * @param command The DriverCommand keyword (command) to perform
	 * @param parameters String[] of parameters used by the command.  Can be null.
	 * @throws Throwable
	 * @see org.safs.StatusCodes#GENERAL_SCRIPT_FAILURE
	 * @see org.safs.StatusCodes#OK Success
	 * @see org.safs.StatusCodes#SCRIPT_WARNING
	 * @see org.safs.StatusCodes#SCRIPT_NOT_EXECUTED
	 */
	public TestRecordHelper runDriverCommandConverted(String command, String... parameters) throws Throwable{
		DriverCommand model = new DriverCommand(command);
		if(parameters instanceof String[] && parameters.length > 0) 
			model.addParameters(parameters);
		
		String sep = null;
		try{
			sep = StringUtils.getUniqueSep(jsafs().SEPARATOR, command, parameters);
		}catch(NullPointerException np){
			// TODO: Use InputProcessor if JSAFSDriver not present.
			sep = StringUtils.getUniqueSep(processor().getDefaultSeparator(), command, parameters);
		}
		TestRecordHelper trd = null;
		if(sep==null){
			if(jsafs() != null){ 
				trd = jsafs().initTestRecordData(null);
				trd.setRecordType(DriverConstant.RECTYPE_C);
				trd.setSeparator(jsafs().SEPARATOR);
				trd.setInputRecord(model.exportTestRecord(jsafs().SEPARATOR));
			}else{
			    trd = processor().initTestRecordData(
			    		model.exportTestRecord(processor().getDefaultSeparator()), 
			    		processor().getDefaultSeparator());
			}
			trd.setCommand(command);
			trd.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			trd.setStatusInfo("Unable to dynamically deduce an acceptable unique SEPARATOR for this Command!");
			return trd;
		}
		if(jsafs() != null)	return jsafs().runDriverCommand(model, sep);
		else {
		    trd = processor().initTestRecordData(
		    		model.exportTestRecord(processor().getDefaultSeparator()), 
		    		processor().getDefaultSeparator());
			trd.setCommand(command);
			trd.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			long result = processor().processDriverCommand(trd);
			return trd;
		}
	}

	/**
	 * Run a SAFS Component Function on a child component in a parent (Window).
	 * The safs action may take one or more parameters.
	 * This method is typically called internally and performs no SAFS Expression processing on the input 
	 * parameters.
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param child The ComponentFunction child component name to act on
	 * @param parent The ComponentFunction parent window name to act on
	 * @param parameters String[] of parameters used by the command.  Can be null.
	 * @throws Throwable
	 * @see org.safs.StatusCodes#GENERAL_SCRIPT_FAILURE
	 * @see org.safs.StatusCodes#OK
	 * @see org.safs.StatusCodes#SCRIPT_WARNING
	 * @see org.safs.StatusCodes#SCRIPT_NOT_EXECUTED
	 */
	public TestRecordHelper runComponentFunctionConverted(String command, String child, String parent, String... parameters)throws Throwable{
		ComponentFunction model = new ComponentFunction(command, parent, child);
		String sep = null;
		try{ sep = StringUtils.getUniqueSep(jsafs().SEPARATOR, command+child+parent, parameters);}
		catch(NullPointerException np){
			// TODO: Use InputProcessor if JSAFSDriver not present.
			sep = StringUtils.getUniqueSep(processor().getDefaultSeparator(), command+child+parent, parameters);
		}
		if(parameters instanceof String[]&& parameters.length > 0) 
			model.addParameters(parameters);

		TestRecordHelper trd = null;
		if(sep==null){
			if(jsafs() instanceof JSAFSDriver){
				trd = jsafs().initTestRecordData(null);
				trd.setRecordType(DriverConstant.RECTYPE_T);
				trd.setSeparator(jsafs().SEPARATOR);
				trd.setInputRecord(model.exportTestRecord(jsafs().SEPARATOR));
			}else{
			    trd = processor().initTestRecordData(
			    		model.exportTestRecord(processor().getDefaultSeparator()), 
			    		processor().getDefaultSeparator());
			}
			trd.setCommand(command);
			trd.setWindowName(parent);
			trd.setCompName(child);
			trd.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			trd.setStatusInfo("Unable to dynamically deduce an acceptable unique SEPARATOR for this Action!");
			return trd;
		}
		if(jsafs() != null)	return jsafs().runComponentFunction(model, sep);
		else {
		    trd = processor().initTestRecordData(
		    		model.exportTestRecord(processor().getDefaultSeparator()), 
		    		processor().getDefaultSeparator());
		    processor().checkTestLevelForStepExecution();
			trd.setCommand(command);
			trd.setWindowName(parent);
			trd.setCompName(child);
			trd.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			long result = processor().processTestRecord(trd);
			processor().resetTestLevel();
			return trd;
		}
	}	
}
