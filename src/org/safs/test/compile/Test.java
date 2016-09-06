/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2016年9月6日    (SBJLWA) Initial release.
 */
package org.safs.test.compile;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.Utils;
import org.safs.sockets.DebugListener;

public class Test {

    private static void _test_compileAtRuntime(String[] args) throws SAFSException{
		String clazz = "org.safs.RSA;org.safs.Log";
		if(args.length>0){
			for(int i=0;i<args.length;i++){
				if("-class".equalsIgnoreCase(args[i])){
					if(++i<args.length) clazz = args[i];
				}
			}
		}
		IndependantLog.debug("To compile source files: "+clazz);
		if(StringUtils.isValid(clazz)){
			clazz = clazz.replaceAll(";|:", StringUtils.SPACE);
		}
		
		Utils.compile(clazz);
    }

    /**
     * java org.safs.test.compile.Test -class org.safs.test.compile.java.ClassA<br>
     * java org.safs.test.compile.Test -class org.safs.test.compile.groovy.sub.ClassC;org.safs.test.compile.groovy.ClassB;org.safs.test.compile.groovy.ClassA<br>
     * @param args
     * @throws SAFSException
     */
	public static void main(String[] args){
		IndependantLog.setDebugListener(new DebugListener(){
			public String getListenerName() {
				return "Console debugger";
			}

			public void onReceiveDebug(String message) {
				System.out.println(message);
			}
		});
		
		try {
			_test_compileAtRuntime(args);
		} catch (SAFSException e) {
			IndependantLog.error("Failed to compile source code!", e);
		}
	}
}
