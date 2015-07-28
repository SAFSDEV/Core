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
 * Tags a Class as requiring automatic injection of a JSAFS Driver driver for its use.
 * <p>
 * The actual injection is not yet implemented in org.safs.model.annotations.Utilities. 
 * However, the current intention is that a Class tagged with this annotation will be scanned for 
 * Methods that take:
 * <p><ul>
 * <li>A single parameter of type org.safs.tools.driver.JSAFSDriver
 * <li>A single parameter of type org.safs.model.tools.Driver
 * </ul><p>
 * @author Carl Nagle
 * @see JSAFSBefore
 * @see JSAFSTest
 * @see JSAFSAfter
 * @see Utilities#autoConfigure(String, JSAFSConfiguredClassStore)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectJSAFS {
	String Description() default "Inject Driver";
}
