package org.safs.test.compile.groovy

import org.safs.test.compile.groovy.sub.ClassC

public class ClassA {
	static def t = new ClassC()
	
	static void main(String ... args){
		println 'Hello ClassA.'
		println "2^6==${power(6)}"
		println t.format(new Integer(5))
	}
}
