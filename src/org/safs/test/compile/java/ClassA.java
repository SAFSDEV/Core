package org.safs.test.compile.java;

import org.safs.test.compile.java.sub.ClassC;

public class ClassA {

	private static ClassB b=null;
	private static ClassC c=null;
	
	public static void main(String[] args){
		b = new ClassB();
		c = new ClassC();
		System.out.println(b.getMessage());
		System.out.println(c.getMessage());
	}
}
