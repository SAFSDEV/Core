package org.safs.tools;

/**
 * Generally used to store the full Classname for the main class started by the JVM.
 * Is only set by classes that know to use it.  Can only be set once per running JVM.
 * @author Carl Nagle
 */
public class MainClass {
	
	private static String mainclass = null;
	/**
	 * @param main classname to be stored as the internal main classname.  Can only be set once.
	 */
	public static void setMainClass(String classname){
		if (mainclass == null) mainclass = classname;
	}
	/**
	 * @return the internal saved main classname String.  Can be null if never set or deduced. 
	 */
	public static String getMainClass(){
		return mainclass;
	}
	
	/**
	 * Deduce the main classname started by the JVM.
	 * If the internally set mainclass is NOT set, this routine will also attempt to set it.
	 * @return the main classname as deduced by JVM Property values, or null.
	 */
	public static String deduceMainClass(){
		String theCommand = System.getProperty("sun.java.command").trim();
		if(theCommand == null || theCommand.length()== 0)
			theCommand = System.getProperty("JAVA_MAIN_CLASS").trim();
		if(theCommand == null || theCommand.length()==0) return null;
		String[] theSplit = theCommand.split(" ");
		String theClass = theSplit[0];
		setMainClass(theClass);
		return theClass;
	}
}
