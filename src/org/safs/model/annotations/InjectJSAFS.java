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
