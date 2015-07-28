/**
 * This class is largely a duplicate of abbot.Platform 
 * {@link http://abbot.sourceforge.net}
 * 
 * It has been duplicated here into the minimum footprint safs.jvmagent 
 * package intended to be installed as a Java extension.  
 * 
 * The Java extension will not have access to the normal System CLASSPATH 
 * and will not find the abbot.jar file
 */
package org.safs.jvmagent;

import java.util.StringTokenizer;

/** Simple utility to figure out what platform we're on, what java version
 * we're running.
 */
public class Platform {

    public static final int JAVA_1_0 = 0x1000;
    public static final int JAVA_1_1 = 0x1100;
    public static final int JAVA_1_2 = 0x1200;
    public static final int JAVA_1_3 = 0x1300;
    public static final int JAVA_1_4 = 0x1400;
    public static final int JAVA_1_5 = 0x1500;

    public static final String OS_NAME;
    public static final String JAVA_VERSION_STRING;
    public static final int JAVA_VERSION;

    static {
        OS_NAME = System.getProperty("os.name");
        JAVA_VERSION_STRING = System.getProperty("java.version");
        JAVA_VERSION = parse(JAVA_VERSION_STRING);
    }
    
    private static boolean isWindows = OS_NAME.startsWith("Windows");
    private static boolean isWindows9X = isWindows
        && (OS_NAME.indexOf("95") != -1
            || OS_NAME.indexOf("98") != -1
            || OS_NAME.indexOf("ME") != -1);
    private static boolean isWindowsXP = isWindows && OS_NAME.indexOf("XP") != -1;
    private static boolean isMac = System.getProperty("mrj.version") != null;
    private static boolean isOSX = isMac && OS_NAME.indexOf("OS X") != -1;
    private static boolean isSunOS = (OS_NAME.startsWith("SunOS")
                                      || OS_NAME.startsWith("Solaris"));
    private static boolean isHPUX = OS_NAME.equals("HP-UX");
    private static boolean isLinux = OS_NAME.equals("Linux");

    /** No instantiations. */
    private Platform() {
    }

    private static String strip(String number) {
        while (number.startsWith("0") && number.length() > 1)
            number = number.substring(1);
        return number;
    }

    static int parse(String vs) {
        int version = 0;
        try {
            StringTokenizer st = new StringTokenizer(vs, "._");
            version = Integer.parseInt(strip(st.nextToken())) * 0x1000;
            version += Integer.parseInt(strip(st.nextToken())) * 0x100;
            version += Integer.parseInt(strip(st.nextToken())) * 0x10;
            version += Integer.parseInt(strip(st.nextToken()));
        }
        catch(NumberFormatException nfe) {
        }
        catch(java.util.NoSuchElementException nse) {
        }
        return version;
    }

    // FIXME this isn't entirely correct, maybe should look for a motif class
    // instead. 
    public static boolean isX11() { return !isOSX && !isWindows; }
    public static boolean isWindows() { return isWindows; }
    public static boolean isWindows9X() { return isWindows9X; }
    public static boolean isWindowsXP() { return isWindowsXP; }
    public static boolean isMacintosh() { return isMac; }
    public static boolean isOSX() { return isOSX; }
    public static boolean isSolaris() { return isSunOS; }
    public static boolean isHPUX() { return isHPUX; }
    public static boolean isLinux() { return isLinux; }
    public static void main(String[] args) {
        System.out.println("Java version is " + JAVA_VERSION_STRING);
        System.out.println("Version number is " + Integer.toHexString(JAVA_VERSION));
        System.out.println("os.name=" + OS_NAME);
    }
}