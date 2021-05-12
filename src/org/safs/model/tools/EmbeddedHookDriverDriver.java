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
package org.safs.model.tools;
/**
 * History:<br>
 *
 * <br>	Dec 17, 2014	(Lei Wang) 	Modify runDriverCommandConverted()/runComponentFunctionConverted():
 * <br>                             if we execute a command directly by a processor, we need to call jsafs().delayBetweenRecords()
 * <br>                             to make the 'millisBetweenRecords' setting works.
 */

import org.safs.Processor;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.model.AbstractCommand;
import org.safs.model.ComponentFunction;
import org.safs.model.DriverCommand;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.EmbeddedHookDriver;
import org.safs.tools.drivers.InputProcessor;
import org.safs.tools.drivers.JSAFSDriver;

/**
 * An EmbeddedHookDriver wrapper attempting to provide a minimalist (simple) interface--hiding a
 * good bit of JSAFS complexity.
 * <p>
 * @author Carl Nagle
 * @since OCT 15, 2013
 * @see org.safs.tools.drivers.JSAFSDriver
 */
public class EmbeddedHookDriverDriver extends AbstractDriver{

	/** An available reference to an EmbeddedHookDriver */
	public static EmbeddedHookDriver driver;

	/* hidden constructor */
	private EmbeddedHookDriverDriver(){
		super();
		_instance = this;
	}

	/**
	 * Initialize an instance of the EmbedddedHookDriverDriver with the
	 * specific of EmbeddedHookDriver to instantiate.
	 * @param clazz
	 * @see org.safs.EmbeddedDCHookDriver
	 * @see org.safs.selenium.EmbeddedSeleniumHookDriver
	 */
	public EmbeddedHookDriverDriver(Class<?> clazz){
		this();
		if(driver == null){
			try{
				Object o = clazz.newInstance();
				driver = (EmbeddedHookDriver) o;
			}
			catch(Exception x){
				x.printStackTrace();
				throw new Error("Cannot instantiate required Drivers due to "+
				      x.getClass().getSimpleName()+": "+ x.getMessage());
			}
		}
	}

	/** Provided by our internal EmbeddedHookDriver.
	 * @see EmbeddedHookDriver */
	@Override
	protected JSAFSDriver jsafs(){ return driver.jsafs();}
	/** Does not support storing or using an InputProcessor internally. */
	@Override
	protected InputProcessor processor(){ return null; }

	/**
	 * Must be called at least once to initialize the internal Driver.
	 * This will be called automatically when using AutoConfigureJSAFS and Runner.autorun
	 * appropriately.
	 * <p>
	 * It can be called automatically by annotations (dependency injection), or be called
	 * from an overriding test controller like a JUnit runner, if appropriate.
	 * @see org.safs.model.annotations.AutoConfigureJSAFS
	 * @see org.safs.model.tools.EmbeddedHookDriverRunner#autorun(String[])
	 */
	@Override
	public void run() throws Exception{
		driver.run();
		preloadAppMapExpressions();
		preloadAppMaps();
	}

	/**
	 * This is an 'AfterAll' JVM ShutdownHook--if it is needed.
	 */
	public final Thread SHUTDOWN_HOOK = new Thread(){
		@Override
		public void run(){
			System.out.println("SAFS EmbeddedDriver AfterAll hook has been invoked.");
			try{ shutdown(); }
			catch(Throwable t){
				System.out.println(t.getMessage());
			}
		}
	};

	/**
	 * Used to invoke the Driver shutdown function.
	 * @throws Exception
	 * @see EmbeddedHookDriver#shutdown()
	 */
	@Override
	public void shutdown() throws Exception {
		driver.shutdown();
	}

	/**
	 * Used to invoke the Driver shutdown function.
	 * @throws Exception
	 * @see EmbeddedHookDriver#shutdown()
	 * @deprecated Use the same method name defined in the super class. Please use {@link #shutdown()} instead.
	 */
	@Deprecated
	public void shutdownDriver() throws Exception {
		shutdown();
	}

	public TestRecordHelper initializeTestRecordData(AbstractCommand model, String separator){
		TestRecordHelper trd = driver.getRequestProcessor().getTestRecordData();
		trd = jsafs().initTestRecordData(trd);
		String testRecord = model.exportTestRecord(separator);
		trd.setSeparator(separator);
		trd.setInputRecord(testRecord);
		trd.setCommand(model.getCommandName());
        //below may not be necessary if "" will suffice for default app map.
		try{
			String defaultMap = (String) jsafs().getMapsInterface().getDefaultMap().getUniqueID();
			trd.setAppMapName(defaultMap);
		}
		catch(ClassCastException ccx){trd.setAppMapName("");}
		catch(NullPointerException npx){trd.setAppMapName("");}
		return trd;
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
	@Override
	public TestRecordHelper runDriverCommandConverted(String command, String... parameters) throws Throwable{
		DriverCommand model = new DriverCommand(command);
		if(parameters instanceof String[] && parameters.length > 0) model.addParameters(parameters);
		String sep = StringUtils.getUniqueSep(jsafs().SEPARATOR, command, parameters);
		TestRecordHelper trd = initializeTestRecordData(model, sep);
		Processor proc = driver.getRequestProcessor().getDriverCommandProcessor();
		trd.setRecordType(DriverConstant.RECTYPE_C);
		proc.setTestRecordData(trd);
		proc.process();
		trd = proc.getTestRecordData();
		if(trd.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			trd = jsafs().runDriverCommand(model, sep);
		}else{
			jsafs().delayBetweenRecords();
			if(jsafs().useSAFSMonitor) jsafs().checkSAFSMonitorStatus();
		}
		return trd;
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
	@Override
	public TestRecordHelper runComponentFunctionConverted(String command, String child, String parent, String... parameters)throws Throwable{
		ComponentFunction model = new ComponentFunction(command, parent, child);
		if(parameters instanceof String[]&& parameters.length > 0) model.addParameters(parameters);
		String sep = StringUtils.getUniqueSep(jsafs().SEPARATOR, command, parameters);
		TestRecordHelper trd = initializeTestRecordData(model, sep);
		Processor proc = driver.getRequestProcessor().getTestStepProcessor();
		trd.setRecordType(DriverConstant.RECTYPE_T);
		proc.setTestRecordData(trd);
		proc.process();
		trd = proc.getTestRecordData();
		if(trd.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			trd = jsafs().runComponentFunction(model, sep);
		}else{
			jsafs().delayBetweenRecords();
			if(jsafs().useSAFSMonitor) jsafs().checkSAFSMonitorStatus();
		}
		return trd;
	}

}
