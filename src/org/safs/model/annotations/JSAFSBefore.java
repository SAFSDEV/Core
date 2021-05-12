/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
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
