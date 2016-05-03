/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * APR 29, 2016    (Lei Wang) Initial release.
 */
package org.safs.natives.win32;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Structure;

/**
 * In the latest version of JNA, the abstract class Structure requires that<br>
 * its sub-class must provide implementation of method getFieldOrder().<br>
 * For convenience, we provide a default implementation of getFieldOrder(), if<br>
 * sub-class doesn't care about the field's order, it can extend this one.<br>
 */
public class DefaultStructure extends Structure{

	/**
	 * We return the class's fields. The fields may be returned in order not predictable.
	 * The subclasses may override it if they really care about the order.
	 */
	protected List<String> getFieldOrder() {
		Field[] fields = getClass().getDeclaredFields();
		
		List<String> result = new ArrayList<String>();

		if(fields!=null){
			for(int i=0;i<fields.length;i++){
				result.add(fields[i].getName());
			}
		}
		
		return result;
	}
}
