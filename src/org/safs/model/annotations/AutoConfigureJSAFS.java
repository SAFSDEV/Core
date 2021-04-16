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
 * Tags a Class as requiring its package and subpackages to be checked for JSAFS auto injection.
 * Alternatively, you can 'include' and/or 'exclude' a semi-colon delimited list of package names that should be
 * autoconfigured.
 * @author Carl Nagle OCT 15, 2013
 * @author Carl Nagle OCT 17, 2013 Documentation updates
 * @see JSAFSBefore
 * @see JSAFSTest
 * @see JSAFSAfter
 * @see Utilities#autoConfigure(String, JSAFSConfiguredClassStore)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoConfigureJSAFS {
	public static final int DEFAULT_ORDER = 1000;
	/**
	 * Optionally, include a semi-colon delimited list of full Class names existing in
	 * other packages to be processed.  Only one Class in a given root package needs to
	 * be listed and all Classes in that package and sub-packages will be processed.
	 * <p>
	 * Example: com.sas.commons.SomeClass;com.sas.roadmaps.AnotherClass;org.mine.AClass
	 * <p>
	 * Note we have to only provide one Class from each unrelated package hierarchy.
	 * A processor should automatically process and include sub-packages of included packages.<br>
	 * (The algorithm currently requires Class names here, but we should figure out how to
	 * support just providing package names instead.)
	 */
	String include() default "";
	/**
	 * Optionally, exclude a semi-colon delimited path of package names to process.
	 * The exclusion applies to packages found during the processing of the inclusion list
	 * or the default root package and sub-package processing.<br>
	 * (Yes, here we are able to use just package names.)
	 */
	String exclude() default "";
	/**
	 * Lower order will be executed before higher order tests.
	 * Tests of the same order value are not guaranteed to execute in any specific order
	 * and may even be intentionally randomized.
	 * The default is 1000.
	 * @return 'order' determines in what order tests shall be executed.
	 */
	int Order() default DEFAULT_ORDER;
	/**
	 * A default for Order not requiring Order= prefix.
	 * The default is 1000.
	 * @return 'order' determines in what order tests shall be executed.
	 */
	int value() default DEFAULT_ORDER;
}
