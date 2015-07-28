/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.staf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSStringTokenizer;
import org.safs.STAFHelper;

import com.ibm.staf.STAFException;

/**
 * <pre>
 * Contains the specific implementation related to STAF2.
 * </pre>
 * 
 * @author Lei Wang
 * 
 */
public class STAFHelperCompatible2 extends AbstractSTAFHelperCompatible{
	
	public List<String> getServiceEvents(String machine, String servicename) throws SAFSException {
		String service = "SEM";
		String command = "LIST EVENT";
		List<String> serviceEvents = new ArrayList<String>();
		
		try {
			//the result got from submit is normal string for STAF 2.X
			//the result got from submit is marshaled string for STAF 3.X
			String input = staf.submit(machine, service, command);
			
			// SAFS/RobotJDispatch: Reset - 1 waiter(s)
			// SAFS/RobotJDone: Reset - 0 waiter(s)
			// SAFS/RobotJReady: Posted
			// SAFS/RobotJResults: Reset - 0 waiter(s)
			
			StringTokenizer st = new SAFSStringTokenizer(input, "\n");
			String token = "";
			while(st.hasMoreTokens()){
				token = st.nextToken();
				if(token.indexOf(servicename)>-1){
					serviceEvents.add(token);
				}
			}
			
			return serviceEvents;
			
		} catch (STAFException e) {
			throw new SAFSException(getClass().getName(), "getServiceEvents ",
					"rc: "+e.rc+ ", command: "+command);
		} catch (RuntimeException re) {
			re.printStackTrace();
			throw new SAFSException(getClass().getName(), "getServiceEvents ",
					" command: "+command+ ", e: "+re);
		}
	}

	  /** 
	   * Get collection of running Engine names.
	   * We parse the STAF command: staf local SEM list EVENT<br>
	   * We deduce the number of unique &lt;engine>Ready or &lt;engine>Running events.
	   * We return the collection of unique engine names.
	   * @return Collection (Vector) of String engine names( ex: SAFS/RobotJ, SAFS/SELENIUM, etc..)
	   * @throws SAFSException
	   **/
	  public Collection getRunningEngineNames (String machine) throws SAFSException {
	    final String service = "SEM";
	    final String command = "LIST EVENT";
	    final String SHUTDOWN_POSTED = "Shutdown: Posted";
	    final String READY_POSTED = "Ready: Posted";
	    final String RUNNING_POSTED = "Running: Posted";
	    final String COLON = ":";
	    try {
	      String input = staf.submit(machine, service, command);
	      // each event is on a separate line in the format eventname:status
	      // ex:
	      // SAFS/RobotJDispatch: Reset - 1 waiter(s)
	      // SAFS/RobotJDone: Reset - 0 waiter(s)
	      // SAFS/RobotJReady: Posted
	      // SAFS/RobotJResults: Reset - 0 waiter(s)
	      // SAFS/RobotJRunning: Reset - 0 waiter(s)
	      // SAFS/RobotJShutdown: Reset - 0 waiter(s)
	      
	      //Log.debug("getRunningEngines processing:\n"+ input);
	      
	      StringTokenizer st = new SAFSStringTokenizer(input, "\n");
	      int events = st.countTokens();
	      Vector engines = new Vector();
	      Vector ignore = new Vector();
	      String event = null;
	      String eventName = null;
	      String eventStatus = null;
	      StringTokenizer eventInfo = null;
	      int statIndex = 0;
	      String engineName = null;
	      for (int j=0; st.hasMoreTokens() && j<events; j++) {
	        try {
	          event = st.nextToken();
	          eventInfo = new SAFSStringTokenizer(event, COLON);
	          eventName = eventInfo.nextToken().trim();
	          eventStatus = eventInfo.nextToken().trim();
	          statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_DISPATCH);
	          // use '1' to ensure <engine>Event and not just Event to avoid event name conflicts
	          if (statIndex < 1) statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_DONE);
	          if (statIndex < 1) statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_READY);
	          if (statIndex < 1) statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_RESULTS);
	          if (statIndex < 1) statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_RUNNING);
	          if (statIndex < 1) statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_SHUTDOWN);
	          if (statIndex < 1) continue;
	          engineName = eventName.substring(0, statIndex);
	          if (ignore.contains(engineName)) continue;
	    	  // ensure not Shutdown
	    	  statIndex = input.indexOf(engineName + SHUTDOWN_POSTED);
	    	  if (statIndex > -1) {
	    		  ignore.add(engineName);
	    		  continue;        	  
	    	  }
	    	  // ensure either Ready or Running
	    	  statIndex = input.indexOf(engineName + READY_POSTED);
	    	  if (statIndex == -1) statIndex = input.indexOf(engineName + RUNNING_POSTED);
	    	  if (statIndex > -1) {
	    		  engines.add(engineName);
	    		  ignore.add(engineName); //so don't care about its other events
	    	  }
	        } catch (Exception ee) {
	        	// continue;
	        }
	      }
	      return engines;
	    } catch (STAFException e) {
	      throw new SAFSException(getClass().getName(), "getRunningEngineNames ",
	                              "rc: "+e.rc+ ", command: "+command);
	    } catch (RuntimeException re) {
	      re.printStackTrace();
	      throw new SAFSException(getClass().getName(), "getRunningEngineNames ",
	                              " command: "+command+ ", e: "+re);
	    }
	  }
}
