package com.sas.test;

import org.safs.model.annotations.*;
import org.safs.model.tools.Runner;

@AutoConfigureJSAFS
public class MyTest {
	
	public MyTest(){
		super();
	}
	
	@JSAFSTest(Order=1000)
	public void TestMethodA() throws Throwable{
		Runner.action("Click", "AnyComp1", "MainWin");
		Runner.command("Pause","10");
		Runner.Click(AppMap.MainWin.AnotherComp2);
		Runner.action(AppMap.AnotherWin.SomeComp1, "GetGUIImage", "SomeCompSnapshot.png");
	}
	
	public static void main(String[] args) throws Throwable {
		MyTest app = new MyTest();
		new Runner().autorun(args);
		Runner.shutdown();
	}
}
