/* $Id: SASUtil.java,v 1.1.2.1 2011/05/05 08:19:54 Lei Wang Exp $ */
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
