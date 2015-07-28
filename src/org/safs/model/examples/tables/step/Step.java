/*
 * Created on Feb 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.safs.model.examples.tables.step;

import org.safs.model.StepTestTable;

/**
 * @author Carl Nagle
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Step {

    public static StepTestTable Logon(String userid, String password){ 
        return new Logon(userid, password);}	
}
