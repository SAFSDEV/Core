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
package org.safs.tools.counters;

import java.util.Enumeration;
import java.util.Hashtable;
import org.safs.tools.ConfigurableToolsInterface;
import org.safs.tools.CoreInterface;
import org.safs.tools.UniqueIDInterface;
import org.safs.tools.status.StatusInterface;
import org.safs.tools.drivers.DriverInterface;

public class SAFSCOUNTERS implements ConfigurableToolsInterface, CountersInterface {

	/** TRUE if ALL counting is currently suspended. */
	protected boolean suspended = false;
	
	protected DriverInterface driver = null;
	
	protected Hashtable counters = new Hashtable(10);
	
	/**
	 * Constructor for SAFSCOUNTERS
	 */
	public SAFSCOUNTERS() {
		super();
	}

	/**
	 * Expects a DriverInterface object for initialization.
	 * @see SimpleToolsInterface#launchInterface()
	 */
	public void launchInterface(Object driver) { this.driver = (DriverInterface) driver; }

	/** internal convenience routine */
	protected String getUpperCaseKey(UniqueIDInterface counter) throws IllegalArgumentException{
		if (counter==null) throw new IllegalArgumentException("Counter ID null.");
		String id = (String) counter.getUniqueID();
		if(id.length()==0) throw new IllegalArgumentException("Empty Counter ID.");		
		return id.toUpperCase();		
	}
	
	/**
	 * @see CountersInterface#initCounter(UniqueIDInterface)
	 */
	public void initCounter(UniqueIDInterface counter) throws IllegalArgumentException{
		String id=getUpperCaseKey(counter);
		if (counters.containsKey(id)) throw new IllegalArgumentException("Illegal Duplicate Counter:"+ id);
		counters.put(id, new Counter());
	}

	/**
	 * @see CountersInterface#getStatus(UniqueIDInterface)
	 */
	public CountStatusInterface getStatus(UniqueIDInterface counter) {
		String id=getUpperCaseKey(counter);
		if(!(counters.containsKey(id))) throw new IllegalArgumentException("Unknown Counter ID:"+ id);
		return (Counter) counters.get(id);
	}

	/**
	 * @see CountersInterface#copyCounter(UniqueIDInterface, UniqueIDInterface)
	 */
	public void copyCounter( UniqueIDInterface from_counter, UniqueIDInterface to_counter)
	                         throws IllegalArgumentException {
		String from_id=getUpperCaseKey(from_counter);
		String to_id=getUpperCaseKey(to_counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown FROM Counter ID:"+ from_id);
		if(!(counters.containsKey(to_id))) throw new IllegalArgumentException("Unknown TO Counter ID:"+ to_id);
		Counter from_status = (Counter) counters.get(from_id);
		Counter to_status = new Counter( from_status.getTotalRecords(),
		                                 from_status.getSkippedRecords(),
										 from_status.getIOFailures(),
										 from_status.getTestFailures(),
										 from_status.getTestWarnings(),
										 from_status.getTestPasses(),
										 from_status.getGeneralFailures(),
										 from_status.getGeneralWarnings(),
										 from_status.getGeneralPasses());
		if (from_status.isSuspended()) to_status.suspend();
		counters.remove(to_id);
	    counters.put(to_id, to_status);
	}

	/**
	 * @see CountersInterface#copyStatus(StatusInterface, UniqueIDInterface)
	 */
	public void copyStatus( StatusInterface from_status, UniqueIDInterface to_counter) 
	                        throws IllegalArgumentException {
		if (from_status==null) throw new IllegalArgumentException("FROM Status null.");
		String to_id = getUpperCaseKey(to_counter);
		Counter from_counter = (Counter) counters.get(to_id);
		Counter to_status = new Counter( from_status.getTotalRecords(),
		                                 from_status.getSkippedRecords(),
										 from_status.getIOFailures(),
										 from_status.getTestFailures(),
										 from_status.getTestWarnings(),
										 from_status.getTestPasses(),
										 from_status.getGeneralFailures(),
										 from_status.getGeneralWarnings(),
										 from_status.getGeneralPasses());
		if (from_counter.isSuspended()) to_status.suspend();
		counters.remove(to_id);
	    counters.put(to_id, to_status);
	}

	/**
	 * @see CountersInterface#sumCounters(UniqueIDInterface, UniqueIDInterface)
	 */
	public void sumCounters( UniqueIDInterface add_counter, UniqueIDInterface to_counter) 
	                         throws IllegalArgumentException {
		String from_id=getUpperCaseKey(add_counter);
		String to_id=getUpperCaseKey(to_counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown FROM Counter ID:"+ from_id);
		if(!(counters.containsKey(to_id))) throw new IllegalArgumentException("Unknown TO Counter ID:"+ to_id);
		Counter from_status = (Counter) counters.get(from_id);
		Counter to_status = (Counter) counters.get(to_id);
		Counter new_status = new Counter( from_status.getTotalRecords()  + to_status.getTotalRecords(),
		                                 from_status.getSkippedRecords() + to_status.getSkippedRecords(),
										 from_status.getIOFailures()     + to_status.getIOFailures(),
										 from_status.getTestFailures()   + to_status.getTestFailures(),
										 from_status.getTestWarnings()   + to_status.getTestWarnings(),
										 from_status.getTestPasses()     + to_status.getTestPasses(),
										 from_status.getGeneralFailures()+ to_status.getGeneralFailures(),
										 from_status.getGeneralWarnings()+ to_status.getGeneralWarnings(),
										 from_status.getGeneralPasses()  + to_status.getGeneralPasses());
		if (to_status.isSuspended()) new_status.suspend();
		counters.remove(to_id);
	    counters.put(to_id, new_status);
	}

	/**
	 * @see CountersInterface#sumStatus(StatusInterface, UniqueIDInterface)
	 */
	public void sumStatus( StatusInterface add_status, UniqueIDInterface to_counter) 
	                         throws IllegalArgumentException {
		if(add_status==null) throw new IllegalArgumentException("ADD Status null.");
		String to_id=getUpperCaseKey(to_counter);
		if(!(counters.containsKey(to_id))) throw new IllegalArgumentException("Unknown TO Counter ID:"+ to_id);
		Counter to_status = (Counter) counters.get(to_id);
		Counter new_status = new Counter( add_status.getTotalRecords()  + to_status.getTotalRecords(),
		                                 add_status.getSkippedRecords() + to_status.getSkippedRecords(),
										 add_status.getIOFailures()     + to_status.getIOFailures(),
										 add_status.getTestFailures()   + to_status.getTestFailures(),
										 add_status.getTestWarnings()   + to_status.getTestWarnings(),
										 add_status.getTestPasses()     + to_status.getTestPasses(),
										 add_status.getGeneralFailures()+ to_status.getGeneralFailures(),
										 add_status.getGeneralWarnings()+ to_status.getGeneralWarnings(),
										 add_status.getGeneralPasses()  + to_status.getGeneralPasses());
		if (to_status.isSuspended()) new_status.suspend();
		counters.remove(to_id);
	    counters.put(to_id, new_status);
	}

	/**
	 * @see CountersInterface#diffCounters(UniqueIDInterface, UniqueIDInterface)
	 */
	public void diffCounters( UniqueIDInterface sub_counter, UniqueIDInterface from_counter) 
	                         throws IllegalArgumentException {
		String sub_id=getUpperCaseKey(sub_counter);
		String from_id=getUpperCaseKey(from_counter);
		if(!(counters.containsKey(sub_id))) throw new IllegalArgumentException("Unknown SUB Counter ID:"+ sub_id);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown FROM Counter ID:"+ from_id);
		Counter sub_status = (Counter) counters.get(sub_id);
		Counter from_status = (Counter) counters.get(from_id);
		Counter new_status = new Counter( from_status.getTotalRecords()  - sub_status.getTotalRecords(),
		                                 from_status.getSkippedRecords() - sub_status.getSkippedRecords(),
										 from_status.getIOFailures()     - sub_status.getIOFailures(),
										 from_status.getTestFailures()   - sub_status.getTestFailures(),
										 from_status.getTestWarnings()   - sub_status.getTestWarnings(),
										 from_status.getTestPasses()     - sub_status.getTestPasses(),
										 from_status.getGeneralFailures()- sub_status.getGeneralFailures(),
										 from_status.getGeneralWarnings()- sub_status.getGeneralWarnings(),
										 from_status.getGeneralPasses()  - sub_status.getGeneralPasses());
		if (from_status.isSuspended()) new_status.suspend();
		counters.remove(from_id);
	    counters.put(from_id, new_status);
	}

	/**
	 * @see CountersInterface#diffStatus(StatusInterface, UniqueIDInterface)
	 */
	public void diffStatus( StatusInterface sub_status, UniqueIDInterface from_counter) 
	                         throws IllegalArgumentException {
		if(sub_status==null) throw new IllegalArgumentException("SUB Status null.");
		String from_id=getUpperCaseKey(from_counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown FROM Counter ID:"+ from_id);
		Counter from_status = (Counter) counters.get(from_id);
		Counter new_status = new Counter( from_status.getTotalRecords()  - sub_status.getTotalRecords(),
		                                 from_status.getSkippedRecords() - sub_status.getSkippedRecords(),
										 from_status.getIOFailures()     - sub_status.getIOFailures(),
										 from_status.getTestFailures()   - sub_status.getTestFailures(),
										 from_status.getTestWarnings()   - sub_status.getTestWarnings(),
										 from_status.getTestPasses()     - sub_status.getTestPasses(),
										 from_status.getGeneralFailures()- sub_status.getGeneralFailures(),
										 from_status.getGeneralWarnings()- sub_status.getGeneralWarnings(),
										 from_status.getGeneralPasses()  - sub_status.getGeneralPasses());
		if (from_status.isSuspended()) new_status.suspend();
		counters.remove(from_id);
	    counters.put(from_id, new_status);
	}

	/**
	 * @see CountersInterface#clearCounter(UniqueIDInterface)
	 */
	public void clearCounter(UniqueIDInterface counter) 
	                         throws IllegalArgumentException {
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		Counter from_status = (Counter) counters.get(from_id);
		Counter new_status = new Counter();
		if (from_status.isSuspended()) new_status.suspend();
		counters.remove(from_id);
	    counters.put(from_id, new_status);
	}

	/**
	 * @see CountersInterface#suspendCounter(UniqueIDInterface)
	 */
	public void suspendCounter(UniqueIDInterface counter) 
	                         throws IllegalArgumentException {
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		Counter from_status = (Counter) counters.get(from_id);
		from_status.suspend();
	    counters.put(from_id, from_status);
	}

	/**
	 * @see CountersInterface#suspendAllCounting()
	 */
	public void suspendAllCounting() { suspended = true; }

	/**
	 * @see CountersInterface#resumeCounter(UniqueIDInterface)
	 */
	public void resumeCounter(UniqueIDInterface counter) 
	                         throws IllegalArgumentException {
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		Counter from_status = (Counter) counters.get(from_id);
		from_status.resume();
	    counters.put(from_id, from_status);
	}

	/**
	 * @see CountersInterface#resumeAllCounting()
	 */
	public void resumeAllCounting() { suspended = false; }

	/**
	 * @see CountersInterface#incrementAllCounters(long)
	 */
	public void incrementAllCounters(UniqueCounterInterface testlevel, long status) {
		if(suspended) return;
		if(counters.isEmpty()) return;
		boolean isStep = testlevel.getTestLevel().equalsIgnoreCase("STEP");
					
		Enumeration list = counters.elements();
		while(list.hasMoreElements()){
			Counter counter = (Counter) list.nextElement();
			boolean stepsOnly = (counter.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
			if( (!(counter.isSuspended()))&&
			    ((stepsOnly && isStep)||(!stepsOnly))
			  ){
				
				// cannot use SWITCH with 'long' values
				
				      if(status==STATUS_GENERAL_FAILURE){
				      	counter.incrementGeneralFailures();					
				}else if(status==STATUS_GENERAL_WARNING){
				      	counter.incrementGeneralWarnings();										
				}else if(status==STATUS_GENERAL_PASS){
				      	counter.incrementGeneralPasses();					
				}else if(status==STATUS_IO_FAILURE){
				      	counter.incrementIOFailures();					
				}else if(status==STATUS_SKIPPED_RECORD){
				      	counter.incrementSkippedRecords();					
				}else if(status==STATUS_TEST_FAILURE){
				      	counter.incrementTestFailures();					
				}else if(status==STATUS_TEST_IO_FAILURE){
				      	counter.incrementTestIOFailures();					
				}else if(status==STATUS_TEST_WARNING){
				      	counter.incrementTestWarnings();					
				}else if(status==STATUS_TEST_PASS){
				      	counter.incrementTestPasses();					
				}
			}
		}
	}

	/**
	 * @see CountersInterface#incrementTestFailures(UniqueIDInterface)
	 */
	public long incrementTestFailures(UniqueCounterInterface counter)
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended())))&&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  ) 
		    {val = from_status.incrementTestFailures();}
		else                                              
		    {val = from_status.getTestFailures();}
		return val;
	}

	/**
	 * @see CountersInterface#incrementTestWarnings(UniqueIDInterface)
	 */
	public long incrementTestWarnings(UniqueCounterInterface counter) 
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended()))) &&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  )
			{val = from_status.incrementTestWarnings();}
		else                                              
			{val = from_status.getTestWarnings();}
		return val;
	}

	/**
	 * @see CountersInterface#incrementTestPasses(UniqueIDInterface)
	 */
	public long incrementTestPasses(UniqueCounterInterface counter)  
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended()))) &&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  )
			{val = from_status.incrementTestPasses();}
		else                                              
			{val = from_status.getTestPasses();}
		return val;
	}

	/**
	 * @see CountersInterface#incrementGeneralFailures(UniqueIDInterface)
	 */
	public long incrementGeneralFailures(UniqueCounterInterface counter)  
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended()))) &&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  )
			{val = from_status.incrementGeneralFailures();}
		else                                              
			{val = from_status.getGeneralFailures();}
		return val;
	}

	/**
	 * @see CountersInterface#incrementGeneralWarnings(UniqueIDInterface)
	 */
	public long incrementGeneralWarnings(UniqueCounterInterface counter)  
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended()))) &&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  )
			{val = from_status.incrementGeneralWarnings();}
		else                                              
			{val = from_status.getGeneralWarnings();}
		return val;
	}

	/**
	 * @see CountersInterface#incrementGeneralPasses(UniqueIDInterface)
	 */
	public long incrementGeneralPasses(UniqueCounterInterface counter)  
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended()))) &&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  )
			{val = from_status.incrementGeneralPasses();}
		else                                              
			{val = from_status.getGeneralPasses();}
		return val;
	}

	/**
	 * @see CountersInterface#incrementIOFailures(UniqueIDInterface)
	 */
	public long incrementIOFailures(UniqueCounterInterface counter)  
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended()))) &&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  )
			{val = from_status.incrementIOFailures();}
		else                                              
			{val = from_status.getIOFailures();}
		return val;
	}

	/**
	 * @see CountersInterface#incrementTestIOFailures(UniqueIDInterface)
	 */
	public long incrementTestIOFailures(UniqueCounterInterface counter)  
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended()))) &&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  )
			{val = from_status.incrementTestIOFailures();}
		else                                              
			{val = from_status.getIOFailures();}
		return val;
	}

	/**
	 * @see CountersInterface#incrementSkippedRecords(UniqueIDInterface)
	 */
	public long incrementSkippedRecords(UniqueCounterInterface counter)  
	                         throws IllegalArgumentException {
	    long val = -1;
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		boolean isStep = counter.getTestLevel().equalsIgnoreCase("STEP");
		Counter from_status = (Counter) counters.get(from_id);
		boolean stepsOnly = (from_status.getMode()==CountersInterface.STEP_TESTS_ONLY_MODE);
		if( ((! suspended)&&(!(from_status.isSuspended()))) &&
		    ((stepsOnly && isStep)||(!stepsOnly))
		  )
			{val = from_status.incrementSkippedRecords();}
		else                                              
			{val = from_status.getSkippedRecords();}
		return val;
	}

	/**
	 * @see CountersInterface#deleteCounter(UniqueIDInterface)
	 */
	public void deleteCounter(UniqueIDInterface counter)  
	                         throws IllegalArgumentException {
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		counters.remove(from_id);
	}

	/**
	 * @see GenericToolsInterface#isToolRunning()
	 */
	public boolean isToolRunning() {
		return true;
	}

	/**
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {
		suspended = false;
		counters.clear();
	}

	/**
	 * @see GenericToolsInterface#shutdown()
	 */
	public void shutdown() {
		reset();
		driver = null;
	}

	/**
	 * @see CountersInterface#setCounterMode(UniqueIDInterface,long)
	 */
	public void setCounterMode(UniqueIDInterface counter, long mode)  
	                         throws IllegalArgumentException {
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		Counter status = (Counter) counters.get(from_id);
		status.setMode(mode);
	}

	/**
	 * @see CountersInterface#getCounterMode(UniqueIDInterface)
	 */
	public long getCounterMode(UniqueIDInterface counter)  
	                         throws IllegalArgumentException {
		String from_id=getUpperCaseKey(counter);
		if(!(counters.containsKey(from_id))) throw new IllegalArgumentException("Unknown Counter ID:"+ from_id);
		Counter status = (Counter) counters.get(from_id);
		return status.getMode();
	}
	
	/**
	 * @see GenericToolsInterface#getCoreInterface()
	 * @throws IllegalStateException ALWAYS since there is no CoreInterface in this class.
	 */
	public CoreInterface getCoreInterface() throws IllegalStateException {
		throw new IllegalStateException("SAFSCOUNTERS does not interface with the core framework directly.");
	}
	
}

