/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.examples.minimal;

import java.util.ArrayList;

import org.safs.Log;
import org.safs.model.annotations.AutoConfigureJSAFS;
import org.safs.model.annotations.JSAFSTest;
import org.safs.model.tools.Runner;

/**
 * @author canagl
 *
 */
@AutoConfigureJSAFS
public class MyApplicationTest{

	void debug(String message){
		System.out.println(message);
		//Log.debug(message);
	}
	/**
	 * 
	 */
	public MyApplicationTest() {
		// TODO Auto-generated constructor stub
	}

	@JSAFSTest
	public void TestPrep(){
		System.out.println("PASS "+ getClass().getName() +".TestPrep() executed.");
	}

	@JSAFSTest
	public void TestA(){
		System.out.println("PASS "+ getClass().getName() +".TestA() executed.");
	}
	
	@JSAFSTest
	public void TestB()throws Throwable{
		System.out.println("PASS "+ getClass().getName() +".TestB() executed.");
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable{
		// TODO Auto-generated method stub
		MyApplicationTest app = new MyApplicationTest();
		new Runner().autorun(args);
	}

}
