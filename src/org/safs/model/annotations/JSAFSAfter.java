/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags a method to be executed after all JSAFSTest methods have completed in the same Class. 
 * @author canagl
 * @see JSAFSBefore
 * @see JSAFSTest
 * @see InjectJSAFS
 * @see Utilities#autoConfigure(String, JSAFSConfiguredClassStore)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSAFSAfter {
	String Description() default "Driver Test Teardown";
	public static final int DEFAULT_ORDER = AutoConfigureJSAFS.DEFAULT_ORDER;
	/**
	 * JSAFSAfter annotated methods will be executed in descending order.<br>
	 * That is, higher order will be executed before lower order.<br>
	 * If the order is the same, the execution order will be uncertain.<br>
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
}
