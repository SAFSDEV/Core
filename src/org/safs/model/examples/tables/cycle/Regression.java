/*
 * Created on Feb 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.safs.model.examples.tables.cycle;

import org.safs.model.CycleTestTable;
import org.safs.model.examples.tables.suite.*;

/**
 * @author Carl Nagle
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Regression extends CycleTestTable {

    public Regression(){
    	super("Regression");
    	
    	// no test record for Cycle tables
    	
    	// the Cycle table
    	add( Suite.LoginWinTests() );
    	add( Suite.AlternateLoginTests());
    }
}
