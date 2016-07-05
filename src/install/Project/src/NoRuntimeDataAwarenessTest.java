package com.sas.spock.safs.runner.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.model.tools.Runner;

/**
 * This JUnit test provides examples of using SeleniumPlus.Asserts, JUnit.Asserts, and pure Java asserts.
 * <p>
 * The class uses the new org.safs.model.tool.Runner for access to the running framework.
 * 
 * @author Carl Nagle
 */
public class NoRuntimeDataAwarenessTest {
	private String a = null;
	private String b = null;
	private String c = null;

	private TestRecordHelper trd;
	
	private boolean trdPassed(){
		return trd.getStatusCode() == StatusCodes.OK;
	}
	
	@Before
	public void init() {
		a = "a test string";
		b = "a test string";
		c = "a different test string";
		
		try{
			Runner.SetVariableValue("a", a);
			Runner.SetVariableValue("b", b);
			Runner.SetVariableValue("c", c);
		}catch(Throwable sx){
			Assert.fail(sx.getClass().getSimpleName()+":"+sx.getMessage()+", setting SAFS variables ^a, ^b, and ^c.");
		}
	}
	
	@Test
	public void selenium_Assert_pass() {
		try {
			trd = Runner.VerifyValues(a, b);
			Assert.assertTrue("SAFSVAR ^a '"+ a +"' did not match SAFSVAR ^b '"+ b +"'", trdPassed());
		} catch (Throwable sx) {
			Assert.fail(sx.getClass()+", "+sx.getMessage()+", retrieving and comparing local variables ^a, ^b.");
		}
	}
	
	@Test
	public void selenium_vars_Assert_pass() {
		try{
			String sa = Runner.GetVariableValue("a");
			String sb = Runner.GetVariableValue("b");
			trd = Runner.VerifyValues(sa, sb);
			Assert.assertTrue("SAFSVAR ^a '"+ sa +"' did not match SAFSVAR ^b '"+ sb +"'", trdPassed());
		}catch(Throwable x){
			Assert.fail(x.getClass()+", "+x.getMessage()+", retrieving and comparing SAFS Variables ^a, ^b.");
		}
	}
	
	/**
	 * This test (SE+ Assert) will fail. It is used to prove that SE+ test could run within JUnit Test.<br>
	 * It will only write failure message to SAFS Log. The normal JUnit test will NOT reflect this error.<br>
	 * To get a consistent JUnit test report, we test the result returned by SE+ Assert by org.junit.Assert.<br>
	 */
	@Test
	public void selenium_Assert_fail() {
		try {
			trd = Runner.VerifyValues(a, c);
			Assert.assertTrue("SAFSVAR ^a '"+ a +"' did not match SAFSVAR ^c '"+ c +"'", trdPassed());
		} catch (Throwable sx) {
			Assert.fail(sx.getClass()+", "+sx.getMessage()+", retrieving and comparing local variables ^a, ^c.");
		}
	}
	
	/**
	 * This test (SE+ Assert) will fail. It is used to prove that SE+ test could run within JUnit Test.<br>
	 * It will only write failure message to SAFS Log. The normal JUnit test will NOT reflect this error.<br>
	 * To get a consistent JUnit test report, we test the result returned by SE+ Assert by org.junit.Assert.<br>
	 */
	@Test
	public void selenium_vars_Assert_fail() {
		try{
			String sa = Runner.GetVariableValue("a");
			String sc = Runner.GetVariableValue("c");
			trd = Runner.VerifyValues(sa, sc);
			Assert.assertTrue("SAFSVAR ^a '"+ sa +"' did not match SAFSVAR ^c '"+ sc +"'", trdPassed());
		}catch(Throwable x){
			Assert.fail(x.getClass()+", "+x.getMessage()+", retrieving and comparing SAFS Variables ^a, ^c.");
		}
	}
	
	/**
	 * This test using Java assert will only fail if assertions are enabled for this class:
	 * <p>
	 * requires JVM arg: -ea:com.sas.spock.tests.GeneralJUnitTest
	 * <p>
	 * Otherwise, with Java assertions disabled by default,this assert test never actually happens at all.
	 */
	@Test
	public void java_assert_fail() {
		//This test will fail if assertions are enabled
		// JVM command-line arg: -ea:com.sas.spock.tests.GeneralJUnitTest
		assert a==c:"compare a==c failed";
	}
	
	@Test
	public void safs_model_runner_test(){
		String varname  = "safs_model_runner_variable";
		String varvalue = "safs_model_runner_test";
		String strvalue = null;
		try{
			Runner.SetVariableValue(varname,varvalue);
			strvalue = Runner.GetVariableValue(varname);
			assert strvalue==varvalue;
			trd = Runner.VerifyValues(varvalue, strvalue);
			Assert.assertTrue("SAFSVAR '"+ varname +"' received as '"+ strvalue +"' did not match expected value '"+ varvalue +"'", trdPassed());
		}catch(Throwable sx){
			Assert.fail(sx.getClass().getSimpleName()+":"+sx.getMessage());
		}
	}
	
	@After
	public void cleanUp() {
		a = null;
		b = null;
		c = null;
	}
}
