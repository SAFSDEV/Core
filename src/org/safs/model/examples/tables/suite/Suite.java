/*
 */
package org.safs.model.examples.tables.suite;

import org.safs.model.SuiteTestTable;

/**
 * @author Carl Nagle
 */
public class Suite {

    public static SuiteTestTable LoginWinTests(){ 
    	return new LoginWinTests(); }

    public static SuiteTestTable AlternateLoginTests(){ 
    	return new AlternateLoginTests(); }
}
