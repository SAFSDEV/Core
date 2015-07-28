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
 * Tags a method to be executed before any JSAFSTest methods are executed in the same Class.
 * @author Carl Nagle
 * @see JSAFSTest
 * @see JSAFSAfter
 * @see InjectJSAFS
 * @see Utilities#autoConfigure(String, JSAFSConfiguredClassStore)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSAFSBefore {
	String Description() default "Driver Test Initialization";
	public static final int DEFAULT_ORDER = AutoConfigureJSAFS.DEFAULT_ORDER;
	/**
	 * JSAFSBefore annotated methods will be executed in ascending order.<br>
	 * That is, lower order will be executed before higher order.<br>
	 * If the order is the same, the execution order will be uncertain.<br>
	 * The default is {@value #DEFAULT_ORDER}.
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
