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
package org.safs.rational.flex.custom;

import org.safs.Log;
import org.safs.rational.FlexUtil;

import com.rational.test.ft.object.interfaces.TestObject;

public class SASUtil {
	public static final String SAS_CLASS_PREFIX = "com.sas";
	
	public static boolean isSASFlexComponent(TestObject to){
		String debugmsg = SASUtil.class.getName()+".isSASFlexComponent() ";
		String className = FlexUtil.getObjectClassName(to);
		
		Log.debug(debugmsg+" class name is "+className);
		
		return className.toLowerCase().startsWith(SAS_CLASS_PREFIX);
	}
}
