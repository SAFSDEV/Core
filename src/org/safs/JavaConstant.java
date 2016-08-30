package org.safs;

public class JavaConstant {

	/** Disable construction. **/
	protected JavaConstant (){}
	
    /** "java.home" **/
	public static final String PROPERTY_JAVA_HOME ="java.home";
	
    /** "java.version" **/
	public static final String PROPERTY_JAVA_VERSION ="java.version";
	
    /** "java.vm.version" **/
	public static final String PROPERTY_JAVA_VM_VERSION ="java.vm.version";
	
    /** "java.class.path" **/
	public static final String PROPERTY_JAVA_CLASS_PATH ="java.class.path";
	
    /** "file.separator" **/
	public static final String PROPERTY_FILE_SEPARATOR ="file.separator";
	
    /** "path.separator" **/
	public static final String PROPERTY_PATH_SEPARATOR ="path.separator";
	
    /** "line.separator" **/
	public static final String PROPERTY_LINE_SEPARATOR ="line.separator";
	
    /** "user.name" **/
	public static final String PROPERTY_USER_NAME ="user.name";
	
    /** "user.home" **/
	public static final String PROPERTY_USER_HOME ="user.home";
	
    /** "user.dir" **/
	public static final String PROPERTY_USER_DIR ="user.dir";

    /** "os.name" **/
	public static final String PROPERTY_OS_NAME ="os.name";	

    /** "os.arch" **/
	public static final String PROPERTY_OS_ARCH ="os.arch";	

    /** "os.version" **/
	public static final String PROPERTY_OS_VERSION ="os.version";
	
	/**
	 * JVM Bit Version: 32-bit, 64-bit, or unknown
	 * Reference URL: http://www.oracle.com/technetwork/java/hotspotfaq-138619.html#64bit_detection
	 */
	public static final String PROPERTY_JVM_BIT_VERSION = "sun.arch.data.model";
	
	/** "-Xms" JVM Option minimum memory*/
	public static final String JVM_Xms ="-Xms";	
	/** "-Xmx" JVM Option maximum memory*/
	public static final String JVM_Xmx ="-Xmx";
}
