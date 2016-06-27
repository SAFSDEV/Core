/** Copyright (C) (SAS Institute, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.cukes;

import java.util.List;

import org.safs.model.tools.Driver;
import org.safs.tools.drivers.JSAFSDriver;
/**
 * Driver-enabled StepDriver allowing Cukes users to use SAFS services and engines 
 * through an underlying JSAFSDriver.  
 * <p>
 * There is at least one JVM option that must be specified in the cucumber options or jvm arguments 
 * in order for the JSAFSDriver to be able to properly initialize itself and the SAFS framework:
 * <p>
 * <ul>
 *    -Dsafs.project.config="pathTo\safstest.ini"
 * </ul>
 * 
 * @author Carl Nagle Original Draft 2013.09.19
 * @see org.safs.cukes.steps.SAFSSteps
 * @see org.safs.cukes.steps.StepDefinitions
 */
public class StepDriver {

	/** "CukesTest" */
	public static final String JSAFS_ID = "CukesTest";
	
	private static final JSAFSDriver _jsafs = new JSAFSDriver(JSAFS_ID);
	private static boolean _our_driver = true;
	private static boolean _allow_shutdown = true;
	private static boolean _global_before = false;
	
	private static final Thread SHUTDOWN_HOOK = new Thread(){
		public void run(){
			System.out.println("SAFS AfterAll hook has been invoked.");
			if(allowShutdown()) { 
				try{ shutdownJSAFS(); }
				catch(Throwable t){
					System.out.println(t.getMessage());
				}
			}
		}
	};
	
	/**
	 * Get the instance of our initialized Driver Driver.
	 * If no instance already exists then we will create an initialized JSAFSDriver 
	 * with a call to initJSAFS.
	 * @return 
	 * @throws Exception
	 * @see {@link #initJSAFS()}
	 */
	public static JSAFSDriver jsafs() throws Exception{
		return _jsafs; 
	}
	
	/**
	 * Normally called internally by a call to jsafs().
	 * The routine will attempt to instance and initialize a new JSAFSDriver 
	 * using the current value of JSAFS_ID.
	 * @return JSAFSDriver instance used by our class instance.
	 * @throws Exception
	 * @see {@link #jsafs()}
	 * @see #JSAFS_ID 
	 * @see {@link org.safs.tools.drivers.JSAFSDriver#run()}
	 */
	protected static JSAFSDriver initJSAFS() {
		System.out.println("StepDefinitions has enterd initJSAFS().");
		Driver.setIDriver(_jsafs);
		_our_driver = true;	
		_jsafs.run();
		_jsafs.systemExitOnShutdown = false;
		_jsafs.removeShutdownHook();
		return _jsafs;
	}
	
	/**
	 * Set to false to prevent our class instance finalize() or shutdownJSAFS() methods 
	 * from shutting down Driver services and engines upon termination.  
	 * The setting will have no affect on our ability to shutdown Driver if the 
	 * Driver was provided to the class instance and not initialized by the class instance. 
	 */
	public static void allowShutdown(boolean defaultIsTrue){ _allow_shutdown = defaultIsTrue; }
	
	/**
	 * @return the current status of whether or not the class instance will be allowed to 
	 * shutdown Driver services and engines.  
	 * This can only return true if the class instance initialized the Driver Driver AND 
	 * the flag to allowShutdown is still true--which it is by default.
	 */
	public static boolean allowShutdown(){ return _our_driver && _allow_shutdown; }
	
	/**
	 * Perform a shutdown of our Driver services and engines.  This would normally only 
	 * be called by finalize().
	 * @throws Exception if JSAFSDriver is not our driver, or allowShutdown() returns false.
	 */
	protected static void shutdownJSAFS() throws Exception {
		if(allowShutdown()){	
			Driver.setIDriver(null);
			jsafs().shutdown();
		}else{
			if(!_our_driver)
				throw new Exception("Cannot shutdown a Driver Driver we did not initialize.");
			throw new Exception("allowShutdown=false has prevented Driver Driver shutdown.");
		}
	}
	
	/**
	 * Must be called by Cukes at least once to initialize the JSAFSDriver.
	 * <p>  
	 * It can be called automatically by the execution of Scenarios, or be called 
	 * from an overriding test controller like a JUnit runner, if appropriate.
	 * <p>
	 * If the steps in this class match any steps in the executing Scenario, then 
	 * this method will automatically be called.  If not, then a &#064;JSAFSBefore step from 
	 * another step definition file for the executing Scenario(s) can invoke this method.
	 * <p>
	 * Example:
	 *  <pre><code>
     *  public class OtherStepdefs {
	 *
	 *      private StepDriver safs = new StepDriver();
     *
	 *      &#064;JSAFSBefore(order=10)
	 *      public void beforeAll(){
	 *          safs.beforeAll();
	 *      }
	 *      ...
	 * }
	 * </code></pre>
	 * The method is prevented from initializing Driver more than once and installs 
	 * a JVM Shutdown Hook which acts as &#064;AfterAll functionality to shutdown Driver 
	 * when the test is completed. 
	 */
	public static void beforeAll(){
		if(!_global_before){
			_global_before = true;
			
			// do beforeAll stuff here
			System.out.println("SAFS BeforeAll hook has been invoked.");
			initJSAFS();
			// the register the aferAll JVM shutdown hook
			Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);			
		}
	}
	
	/**
	 * Used by StepDefinitions receiving parsed parameters directly from cukes that may have 
	 * SAFS Expressions or SAFS variable references embedded in them.  This method is automatically 
	 * called by the provided SAFS StepDefinitions running SAFS DriverCommands or SAFS 
	 * ComponentFunctions.
	 * <p>
	 * This method only needs to be called by the user if they are providing custom 
	 * step definition implementations that are accepting SAFS expressions or variables within 
	 * the feature file Scenario or Scenario Outline steps.
	 * <p>
	 * @param parameters List&lt;String> as provided by the cukes parsing engine.
	 * @return List&lt;String> after expressions have been processed, if any.
	 * @see org.safs.cukes.steps.StepDefinitions#runComponentFunction(String, String, String, List)
	 * @see org.safs.cukes.steps.StepDefinitions#runDriverCommand(String, List)
	 */	
	public static List<String> processExpressions(List<String> parameters){
		if(parameters instanceof List){ 
			String arg;
			for(int i=0; i < parameters.size(); i++){
				arg = parameters.get(i);
				//System.out.println("DC processing expression: "+ arg);
				if(arg.trim().length() > 0) {
					try{ arg = jsafs().processExpression(arg);}
					catch(Exception ignore){
						// ignore?
					}
					parameters.set(i, arg);
				}
			}
		}
		return parameters;
	}	
}
