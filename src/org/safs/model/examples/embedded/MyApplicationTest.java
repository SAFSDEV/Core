/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.examples.embedded;

import org.safs.EmbeddedDCHookDriver;
import org.safs.model.annotations.AutoConfigureJSAFS;
import org.safs.model.annotations.JSAFSAfter;
import org.safs.model.annotations.JSAFSBefore;
import org.safs.model.annotations.JSAFSTest;
import org.safs.model.examples.embedded.exclude.XTest;
import org.safs.model.examples.embedded.include.ATest;
import org.safs.model.tools.EmbeddedHookDriverRunner;

/**
 * Sample invocation:
 * <p>
 * %SAFSDIR%\jre\bin\java <br>
 * -cp "C:\STAF\bin\JSTAF.jar;c:\safs\lib\safsmodel.jar;c:\safs\lib\safs.jar"<br>
 * -Dsafs.project.config=test.ini<br>
 * org.safs.model.examples.embedded.MyApplicationTest
 * <p>
 * @author Carl Nagle
 *
 */
@AutoConfigureJSAFS(exclude="org.safs.model.examples.embedded.exclude")
public class MyApplicationTest{

	static EmbeddedHookDriverRunner runner;
	
	static void debug(String message){ runner.debug(message);}
	
	/**
	 * 
	 */
	public MyApplicationTest() {
		// TODO Auto-generated constructor stub
	}

	@JSAFSBefore(Order=1)
	public void TestBefore1(){
		runner.logPASSED(getClass().getName() +"#TestBefore1() executed.", null);
	}
	@JSAFSBefore(Order=2)
	public void TestBefore2(){
		runner.logPASSED(getClass().getName() +"#TestBefore2() executed.", null);
	}
	@JSAFSBefore
	public void TestBeforeDefault(){
		runner.logPASSED(getClass().getName() +"#TestBeforeDefault() executed.", null);
	}
	
	@JSAFSAfter(Order=1)
	public void TestAfter1(){
		runner.logPASSED(getClass().getName() +"#TestAfter1() executed.", null);
	}
	@JSAFSAfter(Order=2)
	public void TestAfter2(){
		runner.logPASSED(getClass().getName() +"#TestAfter2() executed.", null);
	}
	@JSAFSAfter
	public void TestAfterDefault(){
		runner.logPASSED(getClass().getName() +"#TestAfterDefault() executed.", null);
	}
	
	@JSAFSTest
	public void TestPrep(){
		runner.logPASSED(getClass().getName() +"#TestPrep() executed.", null);
	}

	@JSAFSTest
	public void TestA(){
		runner.logPASSED(getClass().getName() +"#TestA() executed.", null);
	}
	
	@JSAFSTest
	public void TestB()throws Throwable{
		runner.logPASSED(getClass().getName() +"#TestB() executed.", null);
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable{
		// TODO Auto-generated method stub
		MyApplicationTest app = new MyApplicationTest();
		
		try{
			runner = new EmbeddedHookDriverRunner(EmbeddedDCHookDriver.class);
			runner.run();
			
			// run some optional SAFS-specific tests
			runner.command("Expressions", "on");
			runner.command("AppMapChaining", "on");
			runner.command("AppMapResolve", "on");
			runner.command("SetApplicationMap", "TIDTest.Map");
			
			runner.action("ClickScreenLocation", "Desktop", "Desktop", "800 400");
			
			//run as many automatically found and executed tests that might exist
			runner.autorun(args);
			
			//run any additional controlled actions and commands you want 
			runner.command("Expressions", "OFF");
			runner.command("AppMapChaining", "OFF");
			runner.command("AppMapResolve", "OFF");
			
			ATest included = (ATest)runner.getConfiguredClassInstance(ATest.class.getName());
			XTest excluded = (XTest)runner.getConfiguredClassInstance(XTest.class.getName());
			included.TestB();
			try{ 
				excluded.TestB();
			}
			catch(NullPointerException x){
				debug("Handling Expected "+ x.getClass().getSimpleName()+": "+ x.getMessage());
				excluded = new XTest();
				excluded.setEmbeddedHookDriverRunner(runner);
				excluded.TestB();
			}
			
		}catch(Throwable x){
			x.printStackTrace();
		}
		EmbeddedHookDriverRunner.shutdown();
	}
}
