/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.staf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSStringTokenizer;
import org.safs.STAFHelper;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFMarshallingContext;

/**
 * <pre>
 * Contains the specific implementation related to STAF3.
 * Be careful to import this class into other class or use the static methods 
 * of this class in other class, which will cause the backward compatibility problem.
 * 
 * To get an instance of this class, use {@link AbstractSTAFHelperCompatible#getCompatibleSTAF(int)}.
 * </pre>
 * 
 * @author Lei Wang
 * 
 */
public class STAFHelperCompatible3 extends AbstractSTAFHelperCompatible{


	/**
	 * From STAF3.3, the result returned from submit will be marshaled String.<br>
	 * We need to un-marshal it to get the structure for human to understand.<br>
	 * The returned structure <a href="http://staf.sourceforge.net/current/STAFJava.htm#Header_STAFMC">STAFMarshallingContext</a>
	 * need to be parsed. Click the link to see how to parse it.<br>
	 * 
	 * @param result
	 * @return STAFMarshallingContext, if result can't be un-marshaled, null is returned.
	 * @see #submit(String, String, String)
	 */
	public static STAFMarshallingContext getUnMarshallResult(String result){
		if(STAFMarshallingContext.isMarshalledData(result)){
			return STAFMarshallingContext.unmarshall(result);
		}else{
			return null;
		}
	}
	
	/**
	 * From STAF3.3, the result returned from submit will be marshalled String.<br>
	 * We need to un-marshal it to get the string for humain to understand.<br>
	 * {@link #submit(String, String, String)} and {@link #submit2(String, String, String)} may<br>
	 * need to call this method to get the human-understood string.<br>
	 * 
	 * @param result
	 * @return
	 * @see #submit(String, String, String)
	 */
	public static String getUnMarshallStringResult(String result){
		if(STAFMarshallingContext.isMarshalledData(result)){
			return STAFMarshallingContext.formatObject(STAFMarshallingContext.unmarshall(result));		  
		}else{
			return result;
		}
	}
	  
	public List<String> getServiceEvents(String machine, String servicename) throws SAFSException {
		String service = "SEM";
		String command = "LIST EVENT";
		List<String> serviceEvents = new ArrayList<String>();
		
		try {
			//the result got from submit is normal string for STAF 2.X
			//the result got from submit is marshaled string for STAF 3.X
			String input = staf.submit(machine, service, command);
			
			STAFMarshallingContext result = STAFHelperCompatible3.getUnMarshallResult(input);
			
//				Log.debug(result);
//				Marshaled string is as following: List of Map
			
//				[
//				  {
//				    Name   : SAFS/DROIDDispatch
//				    State  : Reset
//				    Waiters: 0
//				  }
			//
//				  {
//				    Name   : SAFS/DROIDShutdown
//				    State  : Posted
//				    Waiters: 0
//				  }
//				]
			if(result.getRootObject() instanceof List){
				List events = (List) result.getRootObject();
				Map eventMap = null;
				String name = null;
				String state = null;
				String waiters = null;
				for(Object event: events){
					if(event instanceof Map){
						//Attention the map's key is lower-case
						eventMap = (Map) event;
						//Log.debug(eventMap);
						name = String.valueOf(eventMap.get("name"));
						if(name!=null && name.indexOf(servicename)>-1){
							state = String.valueOf(eventMap.get("state"));
							waiters = String.valueOf(eventMap.get("waiters"));
							serviceEvents.add(name+" : "+state+" : "+waiters);
						}
					}else{
						Log.debug("Ignore type '"+event.getClass().getName()+"'");
					}
				}
			}else{
				Log.debug("Need new implementation for Result type '"+result.getRootObject().getClass().getName()+"'");
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
	    final String POSTED = "Posted";
	    final String COLON = ":";
	    try {
	      String marshalled = staf.submit(machine, service, command);
	      String input = getUnMarshallStringResult(marshalled);
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
	      boolean isShutdown = false;
	      boolean isInteresting = false;
	      for (int j=0; st.hasMoreTokens() && j<events; j++) {
	        try {
	          isShutdown = isInteresting = false;	          
	          event = st.nextToken();
	          eventInfo = new SAFSStringTokenizer(event, COLON);
	          if(eventInfo.countTokens() < 2) continue;
	          eventInfo.nextToken();
	          eventName = eventInfo.nextToken().trim();
	          event = st.nextToken();//get the State : line
	          eventInfo = new SAFSStringTokenizer(event, COLON);
	          eventInfo.nextToken();
	          eventStatus = eventInfo.nextToken().trim();
	          st.nextToken(); //burn the Waiters : line
	          statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_DISPATCH);
	          // use '1' to ensure <engine>Event and not just Event to avoid event name conflicts
	          if (statIndex < 1) statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_DONE);
	          if (statIndex < 1) {
	        	  statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_READY);
	        	  isInteresting = statIndex > 0;
	          }
	          if (statIndex < 1) statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_RESULTS);
	          if (statIndex < 1) {
	        	  statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_RUNNING);
	        	  isInteresting = statIndex > 0;
	          }
	          if (statIndex < 1) {
	        	  statIndex = eventName.indexOf(STAFHelper.SAFS_EVENT_SHUTDOWN);
	        	  isShutdown = statIndex > 0;
	          }
	          
	          if (statIndex < 1) continue;
	          
	          engineName = eventName.substring(0, statIndex);
	          if (ignore.contains(engineName)) continue;
	    	  
	          // check if engine is Shutdown
	    	  if (isShutdown) {
	    		  if(eventStatus.equalsIgnoreCase(POSTED)) ignore.add(engineName);
	    		  continue;        	  
	    	  }
	    	  if (isInteresting) {
	    		  if(eventStatus.equalsIgnoreCase(POSTED)) {
		    		  engines.add(engineName);
		    		  ignore.add(engineName); //so don't care about its other events
	    		  }
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
