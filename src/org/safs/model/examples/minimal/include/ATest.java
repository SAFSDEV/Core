/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.examples.minimal.include;

import org.safs.model.annotations.JSAFSTest;
import org.safs.model.examples.minimal.MyApplicationTest;

/**
 * @author Carl Nagle
 *
 */
public class ATest extends MyApplicationTest{

	@JSAFSTest
	public void TestA(){
		System.out.println("PASS "+ getClass().getName() +".TestA() executed.");
	}

	public void TestB(){
		System.out.println("FAIL "+ getClass().getName() +".TestB() executed!");
	}
}
