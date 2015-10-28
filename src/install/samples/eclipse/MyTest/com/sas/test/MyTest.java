package com.sas.test;

import org.safs.model.annotations.*;
import org.safs.model.tools.Runner;

@AutoConfigureJSAFS
public class MyTest {
	
	public MyTest(){
		super();
	}
	
	/** 
	 * Non-working example ONLY.
	 * Not automatically executed.
	 * Executed only when called by other methods. 
	 * @throws Throwable
	 */
	public void SomeUtilitiyMethod() throws Throwable{
		Runner.Pause(2);
	}

	@JSAFSTest(Order=1000)
	public void TestMethodA() throws Throwable{
		
		// SetApplicationMap not really necessary if automatically loaded via AppMap.order file
		Runner.SetApplicationMap(AppMap.AutoItTestMap()); 
		Runner.LaunchApplication(AppMap.CalculatorApp(), AppMap.CalcEXE());
		
		// example of running ANY ComponentFunction that Runner doesn't already expose.
		// note the use of literal Strings for Child and Window component ids in the App Map (rarely recommended).
		Runner.action("SetFocus", "SAFS Monitor", "SAFS Monitor");

		// example of running ANY DriverCommand that Runner doesn't already expose.
		Runner.command("Pause", "2");

		// note the use of App Map Component objects (instead of literal Strings) identifying the component from the App Map.
		Runner.action(AppMap.Calculator.Calculator, "SetFocus");
		
		// but the Runner DOES expose the Pause command
		Runner.Pause(2); 
		
		Runner.action(AppMap.SAFS_Monitor.SAFS_Monitor, "SetFocus");

		Runner.CloseApplication(AppMap.CalculatorApp());
	}
	
	public static void main(String[] args) throws Throwable {
		MyTest app = new MyTest();
		new Runner().autorun(args);
		Runner.shutdown();
	}
}
