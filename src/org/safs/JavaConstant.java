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
package org.safs;

public abstract class JavaConstant extends Constants{

	/** Disable construction. **/
	protected JavaConstant (){}

    /** "java.home" **/
	public static final String PROPERTY_JAVA_HOME ="java.home";

    /** "java.version" **/
	public static final String PROPERTY_JAVA_VERSION ="java.version";

    /** "java.vm.version" **/
	public static final String PROPERTY_JAVA_VM_VERSION ="java.vm.version";

    /** "java.class.path" **/
	public static final String PROPERTY_JAVA_CLASS_PATH ="java.class.path";

    /** "file.separator" **/
	public static final String PROPERTY_FILE_SEPARATOR ="file.separator";

    /** "path.separator" **/
	public static final String PROPERTY_PATH_SEPARATOR ="path.separator";

    /** "line.separator" **/
	public static final String PROPERTY_LINE_SEPARATOR ="line.separator";

    /** "user.name" **/
	public static final String PROPERTY_USER_NAME ="user.name";

    /** "user.home" **/
	public static final String PROPERTY_USER_HOME ="user.home";

    /** "user.dir" **/
	public static final String PROPERTY_USER_DIR ="user.dir";

    /** "os.name" **/
	public static final String PROPERTY_OS_NAME ="os.name";

    /** "os.arch" **/
	public static final String PROPERTY_OS_ARCH ="os.arch";

    /** "os.version" **/
	public static final String PROPERTY_OS_VERSION ="os.version";

	/**
	 * JVM Bit Version: 32-bit, 64-bit, or unknown
	 * Reference URL: http://www.oracle.com/technetwork/java/hotspotfaq-138619.html#64bit_detection
	 */
	public static final String PROPERTY_JVM_BIT_VERSION = "sun.arch.data.model";

	/** "-Xms" JVM Option minimum memory*/
	public static final String JVM_Xms ="-Xms";
	/** "-Xmx" JVM Option maximum memory*/
	public static final String JVM_Xmx ="-Xmx";
}
