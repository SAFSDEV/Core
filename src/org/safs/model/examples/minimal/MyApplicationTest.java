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
package org.safs.model.examples.minimal;

import org.safs.model.annotations.AutoConfigureJSAFS;
import org.safs.model.annotations.JSAFSTest;
import org.safs.model.tools.Runner;

/**
 * @author Carl Nagle
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
