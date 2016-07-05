
package com.sas.spock.safs.runner.tests;

import org.safs.TestRecordHelper
import org.safs.model.tools.Runner

import spock.lang.*

public class SpockExperimentWithRunner extends Specification{
	
	TestRecordHelper trd;

	def "a simple runner test"() {
		
		given:
			Runner.command("LogMessage", "Started SpockExperimentWithRunner.")
		
			trd = Runner.command("CallJunit", "com.sas.spock.safs.runner.tests.SimpleTest")
			System.out.println("JUNIT RESULT: "+trd.getCommand()+": "+trd.getStatusInfo())
		
		when:
		
			Runner.command("LogMessage", "SpockExperimentWithRunner executing When.")
		
		then:
		
			Runner.command("LogMessage", "Finished SpockExperimentWithRunner.")
	}
	
	def "computing the maximum of two numbers"() {
		
		expect:
		
			Math.max(a, b) == c
		  
		where:
			
			a << [5, 3]
			b << [1, 9]
			c << [5, 9]
	  }	
}
