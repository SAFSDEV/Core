/*
 * Created on Feb 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.safs.model.examples.tables;

import org.safs.model.*;
import org.safs.model.examples.tables.cycle.Regression;

/**
 * @author canagl
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RunRegressionTest {

	static String filepath = "C:\\SAFS\\data";
	
	public static void main(String[] args) {
		
		CycleTestTable regression = new Regression();
		regression.exportToCSV(filepath);

		// launch execution engine
	}
}
