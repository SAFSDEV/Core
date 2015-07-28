/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Tags a method as one that can be automatically executed by an appropriate 
 * Driver test runner looking for such annotations.
 * <p>
 * Any test runner finding such methods might also check for the InjectJSAFS annotation 
 * provided an appropriate driver for use by the Class. 
 * @author Carl Nagle OCT 15, 2013
 * @see InjectJSAFS
 * @see JSAFSBefore
 * @see JSAFSAfter
 * @see Utilities#autoConfigure(String, JSAFSConfiguredClassStore)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSAFSTest {	
	//Class expected();
	String Description() default "JSAFSTest" ;
	public static final int DEFAULT_ORDER = AutoConfigureJSAFS.DEFAULT_ORDER;
	/**
	 * Lower order will be executed before higher order tests.
	 * Tests of the same order value are not guaranteed to execute in any specific order 
	 * and may even be intentionally randomized.
	 * The default is  {@value #DEFAULT_ORDER}.
	 * @return 'order' determines in what order tests shall be executed.
	 */
	int Order() default DEFAULT_ORDER;	
	/**
	 * A default for Order not requiring Order= prefix.
	 * The default is  {@value #DEFAULT_ORDER}.
	 * @return 'order' determines in what order tests shall be executed.
	 */
	int value() default DEFAULT_ORDER;
	/**
	 * Descriptive unique Name for this test used in logging and reporting.
	 * @return String. If an empty String (default) then the Method name might be used.
	 */
	String Name() default "";
	/**
	 * Used to indicate test summary information should be output to the log at the completion of this 
	 * method execution.  Default is false.  The user must add Summary=true if they want the 
	 * summary to appear in the log for this testcase.
	 * @return
	 */
	boolean Summary() default false;
}
