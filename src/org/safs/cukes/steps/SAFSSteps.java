/** Copyright (C) (SAS Institute, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.cukes.steps;


import org.safs.TestRecordHelper;
import org.safs.cukes.steps.StepDefinitions;
import org.safs.cukes.StepDriver;
import org.safs.tools.drivers.JSAFSDriver;

/**
 * A convenience class providing default SAFS Framework objects to any custom cukes 
 * step definitions subclass wishing to exploit the SAFS services, API, and engines.
 * <p>
 * It is not absolutely necessary to subclass this class, but it is necessary to provide the 
 * functionality provided herein to any cukes step definitions class planning to use 
 * the SAFS framework in its implementation.
 * <p>
 * The functionality provided is, essentially:
 * <ol>
 * <li>One-time &#064;BeforeAll initialization of the SAFS Framework before step definitions are executed.<br>
 * (This also registers the equivalent &#064;AfterAll JVM Shutdown Hook for SAFS shutdown when the run is completed.)
 * <li>Optional access to the underlying JSAFSDriver exposing a great deal of SAFS functionality.
 * <li>Access to a SAFS TestRecordHelper to retrieve status information about SAFS execution.
 * <li>Access to predefined SAFS Cukes StepDefinition implementations callable from custom step definition implementations.
 * </ol>
 * <p>
 * Note this abstract class MUST NOT attempt to implement or even appear to implement any Cukes 
 * step definitions or Hook implementations directly.  The Cukes runtime will initialization runtime errors 
 * and abort execution to avoid DuplicateStepDefinition errors if it thinks it is attempting to load 
 * multiple copies of the same Hook or step definition via subclassing.
 * 
 * @author Carl Nagle Original Draft 2013.09.19
 * @see org.safs.tools.drivers.JSAFSDriver
 * @see org.safs.TestRecordHelper#getStatusCode()
 * @see org.safs.TestRecordHelper#getStatusInfo()
 * @see org.safs.StatusCodes
 */
public abstract class SAFSSteps {
	
	/** Access to a SAFS TestRecordHelper to retrieve status information about SAFS execution.*/
	protected static TestRecordHelper helper = null;
	/** Access to predefined SAFS Cukes StepDefinition implementations callable from custom step definition implementations.*/
	protected static StepDefinitions safsstep = new StepDefinitions();
	/** Optional access to the underlying JSAFSDriver exposing a great deal of SAFS functionality.*/
	protected static JSAFSDriver safs = null;

	/**
	 * static class initialization will perform a one-time setup and initialization 
	 * of the underlying SAFS framework.
	 */
	static {
		if(safsstep == null) safsstep = new StepDefinitions();
		try{ if(safs == null) safs = StepDriver.jsafs();}
		catch(Exception x){
			RuntimeException y = new RuntimeException("Error initializing SAFS Framework.");
			y.initCause(x);
			throw y;
		}
	}
	
	/**
	 * All subclass step definition implementations MUST implement a beforeAll Cukes hook using the 
	 * cucumber.api.java.Before hook annotation.  This method will be invoked by cucumber BEFORE 
	 * any step definitions are executed--which is required to do a one-time (static) initialization 
	 * of the SAFS framework for the test run.
	 * <p>
	 * The method signature and implementation for the concrete implementation should minimally be:
	 * <p>
	 * <ul><code><pre>
	 *     &#064;JSAFSBefore(order=10)
	 *     public void beforeAll(){
	 *         safsstep.beforeAll();
	 *     }
	 * </pre></code></ul>
	 * @see cucumber.api.java.Before
	 */
	public abstract void beforeAll();
}