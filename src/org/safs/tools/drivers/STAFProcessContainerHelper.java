/*
 * Created on Jun 29, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.safs.tools.drivers;

import java.util.Vector;

import org.safs.TestRecordHelper;
import org.safs.tools.engines.EngineInterface;

/**
 * @author Carl Nagle
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class STAFProcessContainerHelper extends TestRecordHelper {

	Vector engineResults = new Vector();
	
	/**
	 * 
	 */
	public STAFProcessContainerHelper() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * "org.safs.staf.", but not really used.
	 * @see org.safs.TestRecordHelper#getCompInstancePath()
	 */
	public String getCompInstancePath(){return "org.safs.staf.";}
	
	public void clearResults(){
		engineResults.clear();
	}
	
	public void addResults(Object item){
		engineResults.add(item);
	}
	
	public Vector getResults(){ return engineResults; }
}
