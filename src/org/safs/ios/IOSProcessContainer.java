/** 
; ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.ios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.safs.GuiClassData;
import org.safs.GuiObjectRecognition;
import org.safs.GuiObjectVector;
import org.safs.Log;
import org.safs.PCTree;
import org.safs.STAFHelper;
import org.safs.SingletonSTAFHelper;
import org.safs.StatusCodes;
import org.safs.image.ImageUtils;
import org.safs.jvmagent.LocalServerGuiClassData;
import org.safs.jvmagent.SAFSInvalidActionArgumentRuntimeException;
import org.safs.jvmagent.STAFLocalServer;
import org.safs.natives.MenuUtilities;
import org.safs.staf.STAFProcessHelpers;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.text.INIFileReadWrite;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.JSAFSDriver;

/**
 * Provides Process Container functionality in the SAFS/IOS Instruments environment.
 * <p>
 * {@link <img src="IOSProcessContainer.GIF" alt="IOSProcessContainer Screenshot" align="left" hspace="4"/>}
 * <p>
 * <b>Launch&nbsp;Trace&nbsp;Template</b>&nbsp;(CheckBox)<br>
 * Select if ProcessContainer should launch Instruments with the Trace Template provided below.  
 * <p>
 * <b>Find&nbsp;Template</b>&nbsp;(Button)<br>
 * File browser to locate desired Instruments Trace Template, if enabled.
 * <p>
 * <b>Trace&nbsp;Template</b>&nbsp;(TextField)<br>
 * The path to the Trace Template used to launch Instruments, if enabled.
 * <p>
 * <b>Set&nbsp;Device&nbsp;and&nbsp;AppName</b>&nbsp;(CheckBox/TextField)<br>
 * OS X Lion and other scenarios may require the automation to forceably select 
 * the desired Device and App Name from the "Choose Target" menu system in the 
 * XCode Instruments application if Instruments does not honor the settings stored 
 * in the saved Trace Template.&nbsp; Values for the device name and the app name 
 * must be entered exactly as they appear in the Instruments menus for proper selection.
 * <p>
 * <b>IOS&nbsp;Instruments&nbsp;Project&nbsp;Path</b>&nbsp;(TextField)<br>
 * The path to the Instruments project where Instruments output will be written.
 * <p>
 * <b>Window&nbsp;Recognition&nbsp;Method</b>&nbsp;(TextField)<br>
 * The recognition string identifying the topmost Window to process.
 * <p>
 * <b>Object&nbsp;Recognition&nbsp;Method</b>&nbsp;(TextField)<br>
 * The recognition string identifying a particular object within the Window to process.  
 * To process all components in the Window this recognition string must be the same as 
 * the Window Recognition Method.
 * <p>
 * <b>Process&nbsp;Children</b>&nbsp;(CheckBox)<br>
 * Process the full hierarchy of children of the object to be processed.  This can be 
 * time consuming on complex containers.  If not selected we will not delve into 
 * processing the children.
 * <p>
 * <b>Window/Object&nbsp;Name</b>&nbsp;(TextField)<br>
 * The name to give the object to be processed.  This is primarily for App Map and other 
 * forms of output that attempt to provide friendly names for objects.
 * <p>
 * <b>Short&nbsp;Strings</b>&nbsp;(CheckBox)<br>
 * Shorten recognition strings.  Do not include each and every layer of the object hierarchy 
 * in the recognition strings generated for App Maps.
 * <p>
 * <b>Process&nbsp;Properties</b>&nbsp;(CheckBox)<br>
 * Capture and output the list of all available properties on each object that is 
 * processed.  This can be VERY VERY time consuming, so enable this only when you 
 * really want it.
 * <p>
 * <b>Object&nbsp;Description</b>&nbsp;(TextField)<br>
 * A short description of the object being processed.
 * <p>
 * <b>Output&nbsp;Directory</b>&nbsp;(TextField)<br>
 * The directory where output files should be written.
 * <p>
 * <b>XML&nbsp;Output</b>&nbsp;(CheckBox)<br>
 * Output object information in XML format which better shows object hierarchy 
 * as well as provides for additional XSL Transformations, if desired.&nbsp; 
 * This format cannot currently be imported into an App Map directly.
 * <p>
 * <b>Output&nbsp;Filename&nbsp;Prefix</b>&nbsp;(TextField)<br>
 * The root name to give the object output files.  The tool will append "Obj.txt"  
 * or "Obj.xml" to this root to form the complete filenames for output.
 * <p>
 * <b>Output&nbsp;Filename</b>&nbsp;(Labels)<br>
 * Display of the filename that will be output based on the Output Directory and 
 * Output Filename Prefix provided.
 * <p>
 * <b>Append&nbsp;App&nbsp;Map</b>&nbsp;(CheckBox)<br>
 * If selected we will append all App Map output to the file specified in App Map File.
 * Skips generating App Map output if this is not selected.
 * <p>
 * <b>App&nbsp;Map&nbsp;File</b>&nbsp;(TextField)<br>
 * The full path filename to a text-based App Map.  The App Map will be created if it 
 * does not already exist.  It will be appended if it does.  Nothing happens if 
 * Append App Map is not selected.
 * <p>
 * <b>Add&nbsp;Component&nbsp;Info</b>&nbsp;(CheckBox)<br>
 * Component "Type" information will be appended to each object recognition string 
 * output.  This is useful when using the App Map output to import component 
 * information into other tools.  However, this information is not compatible with normal 
 * App Map usage (during testing) because the recognition string is no longer properly 
 * formed.
 * <p>
 * <b>Map&nbsp;JPG</b>&nbsp;(CheckBox)(Disabled Future Feature)<br>
 * Attempts to screenshot the window being processed and provide special HTML output 
 * allowing the user to interactively examine the snapshot for component information.  
 * This can be a time consuming process just like Process Properties.
 * <p>
 * <b>Run</b>&nbsp;(Button)<br>
 * Click to begin processing object(s).
 * <p>
 * <b>Cancel</b>&nbsp;(Button)<br>
 * Cancels any object processing that may be in progress.
 * <p>
 * <b>Help</b>&nbsp;(Button)<br>
 * Displays this document.
 * <p>
 * <hr>
 * <p>
 * The IOSProcessContainer can be successfully launched and used with a command line like:
 * <p><ul><pre>
 * java -Dsafs.processcontainer.ini=/Library/SAFS/Project/iosProcessContainer.ini org.safs.ios.IOSProcessContainer
 * </pre></ul>
 * <p>
 * args -- command-line args to main().  These can be overridden by System property 
 * setting using -Dsetting on the command-line.  Consult the doc for each individual 
 * args setting in the Field and Method details of this document.  
 * <p>
 * Generally, the System property arg is in the format 
 * <ul>-Dsafs.processcontainer.[argname]
 * <p> as in:
 * <p>-Dsafs.processcontainer.ini=&lt;path to INI initialization file>
 * </ul>
 * <ul>
 * <li>"ini=&lt;path to INI initialization file>"
 * <li>"map=&lt;path to MAP output file>"
 * <li>"out=&lt;path to detailed output file>"
 * <li>"doLaunch=false"
 * <li>"launchTimeout=30"
 * <li>"template=&lt;path to template>"
 * <li>"doDeviceapp=false"
 * <li>"deviceapp=&lt;-d device menu item -app appname>"
 * <li>"iosproject=&lt;path to instruments project>"
 * <li>"windowRec=Type=Window;Caption={*}"
 * <li>"objectRec=\;Type=Window;Caption={*}"
 * <li>"objectName=&lt;objectname>"
 * <li>"objectDesc=&lt;object description>"
 * <li>"outputviewer=&lt;file viewer/editor>"
 * <li>"doChildren=true"
 * <li>"doProperties=false"
 * <li>"outDir=&lt;path to output directory>"
 * <li>"doXMLOut=false"
 * <li>"outPrefix=&lt;objectname>"
 * <li>"appendMap=false"
 * <li>"addInfo=false"
 * <li>"mapJPG=false"
 * <li>"shortRecognition=false"
 * <li>"ignoreChildrenInListTableCombobox=true"
 * <li>"delayToRun=4"
 * <li>"rsStrategy.qualifier=true"
 * <li>"rsStrategy.useName=true"
 * <li>"rsStrategy.useid=false"
 * <li>"rsStrategy.useGenericType=false"
 * </ul>   
 * @author Carl Nagle AUG 22, 2011 Original 
 */
public class IOSProcessContainer extends JFrame implements ActionListener, 
                                                            DocumentListener,
                                                            Runnable{
	
	/** "IOS Process Container" */
	public static final String STAF_PROCESS_CONTAINER_TITLE = "IOS Process Container";
	
	/** "IOSProcessContainer" */
	public static final String STAF_PROCESS_CONTAINER_PROCESS = "IOSProcessContainer";

	public static String line_separator = System.getProperty("line.separator");
	
	//JSAFSDriver driver = null;
	
	JFileChooser filediag = null;
	
	BufferedWriter out = null;
	BufferedWriter map = null;
	boolean closeout = false;	//set true if OutputStream out is NOT System.out
	boolean closemap = false;	//set true if OutputStream map is NOT System.out
	
	private boolean interrupted = false;
	private boolean stopped = false;
	private boolean shutdown = false;
	private boolean finalized = false;
	
	public void setInterrupt(boolean _interrupted){
		Log.info("IOSPC SET INTERRUPT '"+ _interrupted +"' RECEIVED.");
		if(!_interrupted) setStopped(false);
		interrupted = _interrupted;
	}	
	public boolean isInterrupted(){
		return interrupted;
	}
	
	public void setStopped(boolean _stop){
		Log.info("IOSPC SET STOPPED '"+ _stop +"' RECEIVED.");
		stopped = _stop;
		if(stopped && shutdown){
			Log.info("IOSPC STOPPED WITH SHUTDOWN DETECTED.");
			try{finalize();}catch(Throwable t){;}			
			dispose();
			//System.exit(0);
		}
	}	
	public boolean isStopped(){
		return stopped;
	}
		
	INIFileReadWrite inifile = null;
	
	boolean withCommentsAndBlankLines  = false; 	// ???  needed?
	boolean iniVsTreeFormat            = true; 		// ???  needed?
	
    /**
	 * true if recognition strings should be stripped of intermediate parent recognition 
	 * info that may be deemed unnecessary.
	 * If false, recognition will be FPSM (FullPathSearchMode).
	 * JVM Command-Line: -Dsafs.processcontainer.shortRecognition=
	 * System  Property:safs.processcontainer.shortRecognition=
	 * INI File Setting: shortRecognition=
	 * main() ARGS Parm: shortRecognition=
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	*/
	boolean shortenGeneralRecognition = false;
	
	/** Passed in from static void main() */
	protected static String[] args = null;

	// **** STORED VALUES FOR GUI INITIALIZATION ****
	
	/** 
	 * Provided path to initialization file.
	 * JVM Command-Line: -Dsafs.processcontainer.ini=
	 * System  Property: safs.processcontainer.ini=
	 * main() ARGS Parm: ini=
	 */	
	protected String inipath = null;

	/** 
	 * True if Process Container should launch Instruments when processing.
	 * JVM Command-Line: -Dsafs.processcontainer.doLaunch=
	 * System  Property: safs.processcontainer.doLaunch=
	 * main() ARGS Parm: doLaunch=
	 */	
	protected boolean launch = false;
	
	/** 
	 * True if Process Container should set device and app menu items when launching Instruments.
	 * JVM Command-Line: -Dsafs.processcontainer.doDeviceapp=
	 * System  Property: safs.processcontainer.doDeviceapp=
	 * main() ARGS Parm: doDeviceapp=
	 */	
	protected boolean usedeviceapp = false;
	
	/**
	 * Seconds to watch for initial Instruments Output before issuing failure.
	 */
	protected int launchTimeout = 30;
	public static final String LAUNCH_TIMEOUT = "launchTimeout";
	/** 
	 * Provided path to Instruments Trace Template.
	 * JVM Command-Line: -Dsafs.processcontainer.template=
	 * System  Property: safs.processcontainer.template=
	 * main() ARGS Parm: template=
	 */	
	protected String templatePath = null;
	
	/**
	 * Any device name and app name needed to initialize Instruments upon launch.
	 * This setting, if present, should be in the form "-d device menu item -app appname".  
	 * The values for -d and -app should be exactly as they appear in the Instruments "Choose Target" 
	 * menus.
	 * JVM Command-Line: -Dsafs.processcontainer.deviceapp=
	 * System  Property: safs.processcontainer.deviceapp=
	 * main() ARGS Parm: deviceapp=
	 */
	protected String deviceapp = "";
	
	/** 
	 * Provided path to Instruments project where log output occurs.
	 * JVM Command-Line: -Dsafs.processcontainer.iosproject=
	 * System  Property: safs.processcontainer.iosproject=
	 * main() ARGS Parm: iosproject=
	 */	
	protected String theIosProjectPath = null;

	/** 
	 * Provided path to app map output file.
	 * JVM Command-Line: -Dsafs.processcontainer.map=
	 * System  Property:safs.processcontainer.map=
	 * INI File Setting: map=
	 * main() ARGS Parm: map=
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String mappath = null;

	/** 
	 * Provided path to detailed output file.
	 * JVM Command-Line: -Dsafs.processcontainer.out=
	 * System  Property:safs.processcontainer.out=
	 * INI File Setting: out=
	 * main() ARGS Parm: out=
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String outpath = null;

	/** "Class=UIAWindow;Index=1" */
	public static final String DEFAULT_WINDOWREC = "Class=UIAWindow;Index=1";

	/** 
	 * Provided windowRec. Defaults to DEFAULT_WINDOWREC
	 * JVM Command-Line: "-Dsafs.processcontainer.windowRec=Class=UIAWindow;Index=1"
	 * System  Property:safs.processcontainer.windowRec="Class=UIAWindow;Index=1"
	 * INI File Setting: windowRec="Class=UIAWindow;Index=1"
	 * main() ARGS Parm: windowRec="Class=UIAWindow;Index=1"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theWindowRec = DEFAULT_WINDOWREC;

	/** "Class=UIAWindow;Index=1" */
	public static final String DEFAULT_OBJECTREC = "Class=UIAWindow;Index=1";

	/** 
	 * Provided objectRec. Defaults to DEFAULT_OBJECTREC
	 * JVM Command-Line: "-Dsafs.processcontainer.objectRec=Class=UIAWindow;Index=1"
	 * System  Property:safs.processcontainer.objectRec="Class=UIAWindow;Index=1"
	 * INI File Setting: objectRec="Class=UIAWindow;Index=1"
	 * main() ARGS Parm: objectRec="Class=UIAWindow;Index=1"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theObjectRec = DEFAULT_OBJECTREC;

	/** "WindowName" */
	public static final String DEFAULT_OBJECTNAME = "WindowName";

	/** 
	 * Provided objectName. Defaults to WindowName
	 * JVM Command-Line: "-Dsafs.processcontainer.objectName=WindowName"
	 * System  Property:safs.processcontainer.objectName="WindowName"
	 * INI File Setting: objectName="WindowName"
	 * main() ARGS Parm: "objectName=WindowName"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theObjectName = DEFAULT_OBJECTNAME;

	/** "WindowName Object" */
	public static final String DEFAULT_OBJECTDESC = "WindowName Object";

	/** 
	 * Provided objectDesc. Defaults to WindowName Object
	 * JVM Command-Line: "-Dsafs.processcontainer.objectDesc=WindowName Object"
	 * System  Property:safs.processcontainer.objectDesc="WindowName Object"
	 * INI File Setting: objectDesc="WindowName Object"
	 * main() ARGS Parm: "objectDesc=WindowName Object"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theObjectDesc = DEFAULT_OBJECTDESC;

	/** 'true' */
	public static final boolean DEFAULT_DOCHILDREN = true;

	/** 
	 * Provided doChildren. Defaults to true.
	 * JVM Command-Line: "-Dsafs.processcontainer.doChildren=true"
	 * System  Property:safs.processcontainer.doChildren="true"
	 * INI File Setting: doChildren="true"
	 * main() ARGS Parm: "doChildren=true"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected boolean theDoChildren = DEFAULT_DOCHILDREN;

	/** 'false' */
	public static final boolean DEFAULT_DOPROPERTIES = false;

	/** 
	 * Provided doProperties. Defaults to false.
	 * JVM Command-Line: "-Dsafs.processcontainer.doProperties=true"
	 * System  Property:safs.processcontainer.doProperties="true"
	 * INI File Setting: doProperties="true"
	 * main() ARGS Parm: "doProperties=true"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected boolean theDoProperties = DEFAULT_DOPROPERTIES;

	/** 'false' */
	public static final boolean DEFAULT_DOXMLOUT = false;

	/** 
	 * Provided doXMLOut. Defaults to false.
	 * JVM Command-Line: "-Dsafs.processcontainer.doXMLOut=false"
	 * System  Property:safs.processcontainer.doXMLOut="false"
	 * INI File Setting: doXMLOut="false"
	 * main() ARGS Parm: "doXMLOut=false"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected boolean theDoXMLOut = DEFAULT_DOXMLOUT;

	/** 
	 * Provided output directory. Defaults to user current directory.
	 * JVM Command-Line: "-Dsafs.processcontainer.outDir=&lt;current directory>"
	 * System  Property:safs.processcontainer.outDir="&lt;current directory>"
	 * INI File Setting: outDir="&lt;current directory>"
	 * main() ARGS Parm: "outDir=&lt;current directory>"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theOutDir = System.getProperty("user.dir");

	/** 
	 * Provided output file prefix. Defaults to WindowName.
	 * JVM Command-Line: "-Dsafs.processcontainer.outPrefix=WindowName"
	 * System  Property:safs.processcontainer.outPrefix="WindowName"
	 * INI File Setting: outPrefix="WindowName"
	 * main() ARGS Parm: "outPrefix=WindowName"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theOutPrefix = DEFAULT_OBJECTNAME;


	/** 'false' */
	public static final boolean DEFAULT_APPENDMAP = false;

	/** 
	 * Provided appendMap. Defaults to false.
	 * JVM Command-Line: "-Dsafs.processcontainer.appendMap=false"
	 * System  Property:safs.processcontainer.appendMap="false"
	 * INI File Setting: appendMap="false"
	 * main() ARGS Parm: "appendMap=false"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected boolean theAppendMap = DEFAULT_APPENDMAP;

	/** 'false' */
	public static final boolean DEFAULT_ADDINFO = false;

	/** 
	 * Provided addInfo. Defaults to false.
	 * JVM Command-Line: "-Dsafs.processcontainer.addInfo=false"
	 * System  Property:safs.processcontainer.addInfo="false"
	 * INI File Setting: addInfo="false"
	 * main() ARGS Parm: "addInfo=false"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected boolean theAddInfo = DEFAULT_ADDINFO;

	/** 'false' */
	public static final boolean DEFAULT_MAPJPG = false;

	/** 
	 * Provided mapJPG. Defaults to false.
	 * JVM Command-Line: "-Dsafs.processcontainer.mapJPG=false"
	 * System  Property:safs.processcontainer.mapJPG="false"
	 * INI File Setting: mapJPG="false"
	 * main() ARGS Parm: "mapJPG=false"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected boolean theMapJPG = DEFAULT_MAPJPG;

	public static final String DEFAULT_DELAY_TO_RUN = "2";
	/** 
	 * Time to delay before IOSPC starts to search. Defaults to 2.
	 * JVM Command-Line: "-Dsafs.processcontainer.delayToRun=2"
	 * System  Property:safs.processcontainer.delayToRun="2"
	 * INI File Setting: delayToRun="2"
	 * main() ARGS Parm: "delayToRun=2"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theDelayToRun = DEFAULT_DELAY_TO_RUN;
	
	/** null */
	public static final String DEFAULT_INI_SECTION = null;
	
	/** "->" */
	public static final String pathSep = "->";
	
	JPanel north;
	JPanel center;
	JPanel south;

	JLabel traceTemplateLabel;
	JLabel iosProjectLabel;
	JLabel windowRecLabel;
	JLabel objectRecLabel;
	JLabel windowNameLabel;
	JLabel objectDescLabel;
	JLabel outputDirLabel;
	JLabel outputNameLabel;

	JCheckBox doLaunch;
	String LAUNCH_ACTION = "doLaunch";

	JCheckBox doDeviceapp;
	String DODEVICEAPP_ACTION = "doDeviceapp";
	
	JButton doFindTemplate;
	String FIND_TEMPLATE_ACTION = "findTemplate";
	
	JTextField traceTemplatePath;
	String TEMPLATE_PATH_ACTION = "template";
	
	JTextField deviceappPath;
	String DEVICEAPP_ACTION = "deviceapp";
	
	JButton doFindProject;
	String FIND_PROJECT_ACTION = "findProject";
	
	JTextField iosProjectPath;
	String PROJECT_PATH_ACTION = "iosproject";
	
	JButton outputFullname;
	String OUTFILE_ACTION    = "out";
	String OUTFILE_VIEW_ACTION = "outShow";

	JCheckBox doProperties;
	String PROPERTIES_ACTION = "doProperties";
	JCheckBox doChildren;
	String CHILDREN_ACTION   = "doChildren";
	
	JCheckBox appendMap;
	String APPENDMAP_ACTION  = "appendMap";
	JCheckBox addInfo;
	String ADDINFO_ACTION    = "addInfo";
	JCheckBox mapJPG;
	String MAPJPG_ACTION     = "mapJPG";
	JCheckBox doShortStrings;
	String SHORTEN_ACTION    = "shortRecognition";
		
	JTextField windowRec;
	String WINDOWREC_ACTION   = "windowRec";	
	JTextField windowName;
	String WINDOWNAME_ACTION  = "objectName";	
	JTextField objectRec;
	String OBJECTREC_ACTION   = "objectRec";	
	JTextField objectDesc;
	String OBJECTDESC_ACTION  = "objectDesc";	
	JTextField outputDir;
	String OUTPUTDIR_ACTION   = "outDir";	
	JTextField outPrefix;
	String OUTPREFIX_ACTION   = "outPrefix";
	
	JCheckBox doXMLOut;
	String DOXMLOUT_ACTION = "doXMLOut";
	
	
	JTextField appMapFile;
	String APPMAPFILE_ACTION  = "map";
	JButton appMapFileShow;
	String APPMAPFILESHOW_ACTION  = "map_show";
	
	JLabel delayToRunLabel;
	JTextField delayToRunTextField;
	String DELAY_TO_RUN_TEXT = "Delay time (seconds): ";
	String DELAY_TO_RUN_TOOLTIP = "Delay time before runnin IOSPC, time is in second. Input a number.";
	String DELAY_TO_RUN = "delayToRun";
	
	
	JButton run ;
	String RUN_ACTION    = "run";
	JButton cancel ;
	String CANCEL_ACTION = "cancel";
	JButton help ;
	String HELP_ACTION   = "help";
	
	JLabel status ;
	
	boolean weLaunchedSTAF = false;
	
	String outputViewer = "/Applications/TextEdit.app";
	public static final String OUTPUT_VIEWER_KEY = "outputviewer";

	private String APPFILE_VIEW_ACTION;
	
	/** "Click to Run" */
	public static final String READY_TEXT="Click to Run";

	/** "Running..." */
	public static final String RUNNING_TEXT="Running...";
	public static final String DELAYING_TEXT="Delaying...";
	public static final String FINISHED_TEXT="Finished.";
	
	/**
	 * Constructor for ProcessContainer.
	 */
	public IOSProcessContainer() {
		super(STAF_PROCESS_CONTAINER_TITLE);
		inipath = getArg("ini");
		try{setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);}catch(Exception x){;}
		addWindowListener(new ShutdownListener());

		String processName = STAF_PROCESS_CONTAINER_PROCESS;
		org.safs.STAFHelper staf = null;
		try{ 
			staf = SingletonSTAFHelper.getInitializedHelper(processName);
		}
		catch(org.safs.SAFSSTAFRegistrationException rx){
			try{
				staf = STAFProcessHelpers.launchSTAFProc(processName);
				SingletonSTAFHelper.setInitializedHelper(staf);
				weLaunchedSTAF = true;
			}
			catch(org.safs.SAFSSTAFRegistrationException rx2){
				System.err.println("IOSProcessContainer unable to launch STAF or register with STAF.");
				Log.info("IOSProcessContainer unable to launch STAF or register with STAF.");
				try{finalize();}catch(Throwable t){}
				return;
			}
		}
		if(staf.isInitialized()) Log.setHelper(staf);
		
		
		try{
			if (System.getProperty(DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG)==null)		
				System.setProperty(DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG, inipath);
		}catch(Exception x){;}
						
		openINIReadWrite();

		String val = getArg(APPMAPFILE_ACTION);	
		if (val != null) mappath = val;

		launch = getBooleanArg(LAUNCH_ACTION);
 		launchTimeout = getIntegerArg(LAUNCH_TIMEOUT, launchTimeout);
 		
		val = getArg(TEMPLATE_PATH_ACTION);
		if (val != null) templatePath = val;

		val = getArg(DEVICEAPP_ACTION);
		if (val != null) deviceapp = val;

		usedeviceapp = getBooleanArg(DODEVICEAPP_ACTION);
		
		val = getArg(PROJECT_PATH_ACTION);
		if (val != null) theIosProjectPath = val;

		val = getArg(OUTFILE_ACTION);
		if (val != null) outpath = val;

 		val = getArg(WINDOWREC_ACTION);
 		if (val != null) theWindowRec = val;

 		val = getArg(OBJECTREC_ACTION);
 		if (val != null) theObjectRec = val;

 		val = getArg(WINDOWNAME_ACTION);
 		if (val != null) theObjectName = val;

 		val = getArg(OBJECTDESC_ACTION);
 		if (val != null) theObjectDesc = val;

 		val = getArg(OUTPUT_VIEWER_KEY);
 		if (val != null) outputViewer = val;
 		
 		theDoChildren   = getBooleanArg(CHILDREN_ACTION);
 		shortenGeneralRecognition= getBooleanArg(SHORTEN_ACTION);
 		theDoProperties = getBooleanArg(PROPERTIES_ACTION);
 		theDoXMLOut = getBooleanArg(DOXMLOUT_ACTION);

 		val = getArg(DELAY_TO_RUN);
 		if(val!=null) theDelayToRun = val;
 		
 		val = getArg(OUTPUTDIR_ACTION);
 		if (val != null) theOutDir = val;

 		val = getArg(OUTPREFIX_ACTION);
 		if (val != null) theOutPrefix = val;

 		theAppendMap = getBooleanArg(APPENDMAP_ACTION);
 		theAddInfo   = getBooleanArg(ADDINFO_ACTION);
 		theMapJPG    = getBooleanArg(MAPJPG_ACTION);

		// do this finally, it expects this.server is not null 
		populateFrame();
	}
	
	/**
	 * Open the detailed output file if possible.  Otherwise we keep the System.out default.
	 */
    protected void openOutPathStream(){
    	if (outpath == null) return;
		File afile = new CaseInsensitiveFile(outpath).toFile();
		try{ 
			out = FileUtilities.getUTF8BufferedFileWriter(afile.getAbsolutePath());
			Utilities.spcOutputWriter = out;
			Utilities.spcOutputXML = theDoXMLOut;
			closeout = true;
			if(theDoXMLOut){
				out.write("<?xml version='1.0' encoding='UTF-8' ?>");
				out.newLine();
				out.write("<object name='"          + windowName.getText() +"' "+
						          "winRecognition='"+ windowRec.getText()+"' "+ 
						          "objRecognition='"+ objectRec.getText()+"' "+
						          "objDescription='"+ objectDesc.getText()+"' >");
				out.newLine();
			}else{
				out.write("Output for "+windowName.getText());
				out.newLine();
			}
		}
		// SecurityExceptions or FileNotFoundException, etc..
		catch(SecurityException sx){
			Utilities.spcOutputWriter = null;
			out = null;
		}
		catch(FileNotFoundException nf){
			Utilities.spcOutputWriter = null;
			out = null;
			System.err.println("*** ERROR IN DETAILS OUTPUT FILENAME: "+ outpath +" ***");
		} catch (IOException e) {
			Utilities.spcOutputWriter = null;
			out = null;
			System.err.println("*** IOEXCEPTION ON OUTPUT FILENAME: "+ outpath +" ***");
		}
    }	
	
	/**
	 * Close the detailed output file if possible.  Otherwise we keep the System.out default.
	 */
    protected void closeOutPathStream(){
		if ((closeout)&&(out != null)){
			closeout = false;
			try{
				if(theDoXMLOut){
					Utilities.closeXMLState(Utilities.STATE_INIT);
				}
				out.flush();
				out.close();
			}catch(Exception x){;}
			Utilities.spcOutputWriter = null;
			out = null;
		}
    }	
	
	/**
	 * Open the map output file if possible.  Otherwise we keep the System.out default.
	 */
    protected void openMapPathStream(){
    	if (mappath == null) return;
		File afile = new CaseInsensitiveFile(mappath).toFile();
		try{ 
			map = FileUtilities.getUTF8BufferedFileWriter(afile.getAbsolutePath(), appendMap.isSelected());
			closemap = true;
			Utilities.spcAppMapWriter = map;
		}
		// SecurityExceptions or FileNotFoundException, etc..
		catch(SecurityException sx){ 
			Utilities.spcAppMapWriter = null;
			map = null;
		}
		catch(FileNotFoundException nf){
			Utilities.spcAppMapWriter =null;
			map = null;
			System.err.println("*** ERROR IN APP MAP OUTPUT FILENAME: "+ mappath +" ***");
		}
    }	
	
	/**
	 * Close the Map output file if possible.  Otherwise we keep the System.out default.
	 */
    protected void closeMapPathStream(){
		if ((closemap)&&(map != null)){
			closemap = false;
			Utilities.spcAppMapWriter = null;
			try{
				map.flush();
				map.close();
			}catch(Exception x){;}
			map = null;
		}
    }	
	
	/**
	 * Open/Process the initialization file if it is found and appears to be valid.
	 */
	protected void openINIReadWrite(){
		Log.info("IOSPC attempting to OPEN INI file for Read/Write: "+ inipath);				
		if (inipath==null) return;
		File afile = new CaseInsensitiveFile(inipath).toFile();
		if ((! afile.exists())||(! afile.isFile())) {
			Log.debug("IOSPC Invalid INI file specification: "+inipath);				
			return;
		}
		inifile = new INIFileReadWrite(afile, 0);
		if(inifile == null){
			Log.debug("IOSPC failed to OPEN INI file for Read/Write operations.");					
		}
	}

	/**
	 * Retrieve an item out of the initialization file if such a file was identified.
	 */
    protected String getINIValue(String section, String item){
    	if (inifile == null) return null;
    	return inifile.getAppMapItem(section, item);
    }

	/**************************************************************************
	 * Monitor TextField DocumentChanged Events.
	 **************************************************************************/
	public void changedUpdate(DocumentEvent event){documentChanged(event);}
	public void insertUpdate(DocumentEvent event) {documentChanged(event);}
	public void removeUpdate(DocumentEvent event) {documentChanged(event);}
	protected void documentChanged(DocumentEvent event){
		Document comp = event.getDocument();

		// some will fire new DocumentEvents on affected components
		
		if (comp.equals(windowName.getDocument())){
			theObjectName = windowName.getText();
			theObjectDesc = theObjectName +" Object";
			objectDesc.setText(theObjectDesc);
			theOutPrefix = theObjectName;
			outPrefix.setText(theObjectName);
		}
		else if (comp.equals(objectDesc.getDocument())){
			theObjectDesc = objectDesc.getText();
		}
		else if (comp.equals(outputDir.getDocument())){
			outpath = makeFullObjPath();
			outputFullname.setText(outpath);
			// Verify paths?
		}
		else if (comp.equals(outPrefix.getDocument())){
			outpath = makeFullObjPath();
			outputFullname.setText(outpath);
			// Verify paths?
		}
		else if (comp.equals(appMapFile.getDocument())){
			mappath = appMapFile.getText();
		}
		else if (comp.equals(traceTemplatePath.getDocument())){
			templatePath = traceTemplatePath.getText();
		}
		else if (comp.equals(deviceappPath.getDocument())){
			deviceapp = deviceappPath.getText();
		}
		else if (comp.equals(iosProjectPath.getDocument())){
			theIosProjectPath = iosProjectPath.getText();
		}
	}
	
	/** Configures/Toggles UI components based on current/stored settings. */
	protected void configureUI(){
		if(appendMap.isSelected()){
			//addInfo.setEnabled(true);
			appMapFile.setEnabled(true);
			appMapFileShow.setEnabled(true);
			//mapJPG.setEnabled(true);
		}else{
			addInfo.setSelected(false);
			mapJPG.setSelected(false);
			addInfo.setEnabled(false);
			appMapFile.setEnabled(false);
			//appMapFileShow.setEnabled(false);
			mapJPG.setEnabled(false);
		}
		if(doLaunch.isSelected()){
			traceTemplateLabel.setEnabled(true);
			traceTemplatePath.setEnabled(true);
			doFindTemplate.setEnabled(true);
			doDeviceapp.setEnabled(true);			
		}else{
			traceTemplateLabel.setEnabled(false);
			traceTemplatePath.setEnabled(false);
			//doFindTemplate.setEnabled(false);
			doDeviceapp.setEnabled(false);
		}
		if(doDeviceapp.isEnabled()&&doDeviceapp.isSelected()){
			deviceappPath.setEnabled(true);
		}else{
			deviceappPath.setEnabled(false);
		}
	}
	
	/**************************************************************************
	 * Launches separate execution Processor when the RUN button is clicked.
	 * This will process all known top-level Windows.
	 **************************************************************************/
	public void actionPerformed(ActionEvent event){
		String comp = event.getActionCommand();
		
		if(comp.equalsIgnoreCase(RUN_ACTION)){
		    setInterrupt(false);
			status.setText(RUNNING_TEXT);
			try{
				new Thread(new RunThread()).start();
			}catch(Exception x){;}
		}else if(comp.equalsIgnoreCase(CANCEL_ACTION)){
			Log.info("SPC CANCEL initiated.");
			setInterrupt(true);
		}else if(comp.equalsIgnoreCase(LAUNCH_ACTION)){
			launch = doLaunch.isSelected();
			configureUI();
		}else if(comp.equalsIgnoreCase(DODEVICEAPP_ACTION)){
			usedeviceapp = doDeviceapp.isSelected();
			configureUI();
		}else if(comp.equalsIgnoreCase(APPENDMAP_ACTION)){
			theAppendMap = appendMap.isSelected();
			configureUI();
		}else if(comp.equalsIgnoreCase(DOXMLOUT_ACTION)){
			theDoXMLOut = doXMLOut.isSelected();
			Utilities.spcOutputXML = theDoXMLOut;
			outpath = makeFullObjPath();
			outputFullname.setText(outpath);
		}else if(comp.equalsIgnoreCase(SHORTEN_ACTION)){
			shortenGeneralRecognition = doShortStrings.isSelected();
		}else if(comp.equalsIgnoreCase(OUTFILE_VIEW_ACTION)){
			String launcher = "open -a "+ outputViewer +" "+ outputFullname.getText();
			Log.info("Attempting OutputViewer command: "+ launcher);
			try{Runtime.getRuntime().exec(launcher);}
			catch(Exception x){
				System.err.println("OUTPUT VIEWER Error: "+ x.getClass().getSimpleName()+"; "+ x.getMessage());
				Log.debug("OUTPUT VIEWER Error: "+ x.getClass().getSimpleName()+"; "+ x.getMessage());
			}
		}else if(comp.equalsIgnoreCase(APPMAPFILESHOW_ACTION)){
			String launcher = "open -a "+ outputViewer +" "+ appMapFile.getText();
			Log.info("Attempting AppMapViewer command: "+ launcher);
			try{Runtime.getRuntime().exec(launcher);}
			catch(Exception x){
				System.err.println("APPMAP VIEWER Error: "+ x.getClass().getSimpleName()+"; "+ x.getMessage());
				Log.debug("APPMAP VIEWER Error: "+ x.getClass().getSimpleName()+"; "+ x.getMessage());
			}
		}else if(comp.equalsIgnoreCase(FIND_TEMPLATE_ACTION)){
			if (filediag == null) filediag = new JFileChooser();
			filediag.setDialogTitle("Find Instruments Trace Template");
			filediag.setFileSelectionMode(JFileChooser.FILES_ONLY);
			Log.info("IOSPC launching Trace Template Open dialog...");
			if(filediag.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				templatePath = filediag.getSelectedFile().getAbsolutePath();
				traceTemplatePath.setText(templatePath);
				Log.info("Trace Template Open dialog returned: "+ templatePath);
			}else{
				Log.info("IOSPC Trace Template Open dialog cancelled or closed by user.");
			}
			Log.debug("IOSPC hiding Trace Template Open dialog.");
			
		}else if(comp.equalsIgnoreCase(FIND_PROJECT_ACTION)){
			if (filediag == null) filediag = new JFileChooser();
			filediag.setDialogTitle("Find Instruments Project Directory");
			filediag.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			Log.info("IOSPC launching Find Project Open dialog...");
			if(filediag.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				theIosProjectPath = filediag.getSelectedFile().getAbsolutePath();
				iosProjectPath.setText(theIosProjectPath);
				Log.info("Find Project Open dialog returned: "+ theIosProjectPath);
			}else{
				Log.info("IOSPC Project Path Open dialog cancelled or closed by user.");
			}
			Log.debug("IOSPC hiding Project Path Open dialog.");
			
		}else{
			configureUI();
		}
	}

	/**
	 * Build the JFrame GUI for viewing.
	 */
	protected void populateFrame(){
		int w = 420;
		int h = 20;
		int w1 = w/3;
		int w2 = w1*2 - 30;
		
		// NORTH PANEL		
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		GridBagConstraints gbct = new GridBagConstraints();
		gbct.insets = new Insets(2,2,0,2);
		GridBagConstraints gbcb = new GridBagConstraints();
		gbcb.insets = new Insets(0,2,2,2);
		north = new JPanel();
		north.setLayout(gbl);
		
		doLaunch = new JCheckBox("Launch IOS Instruments", launch);
		doLaunch.setPreferredSize(new Dimension(w2,h));
		doLaunch.setActionCommand(LAUNCH_ACTION);
		doLaunch.addActionListener(this);
		
		doFindTemplate = new JButton("Find Template");
		doFindTemplate.setPreferredSize(new Dimension(w1,h));
		doFindTemplate.setActionCommand(FIND_TEMPLATE_ACTION);
		doFindTemplate.setToolTipText("Browse to Instruments Trace Template");
		doFindTemplate.addActionListener(this);
		
		traceTemplateLabel = new JLabel("Instruments Trace Template:");
		traceTemplateLabel.setPreferredSize(new Dimension(w,h));
		traceTemplatePath = new JTextField();
		traceTemplatePath.setText(templatePath);
		traceTemplatePath.setPreferredSize(new Dimension(w,h));
		traceTemplatePath.setActionCommand(TEMPLATE_PATH_ACTION);
		traceTemplatePath.getDocument().addDocumentListener(this);
		
		doDeviceapp = new JCheckBox("Set Device and AppName (-d Device Name -app App Name)", usedeviceapp);
		doDeviceapp.setPreferredSize(new Dimension(w,h));
		doDeviceapp.setActionCommand(DODEVICEAPP_ACTION);
		doDeviceapp.addActionListener(this);
		
		deviceappPath = new JTextField();
		deviceappPath.setText(deviceapp);
		deviceappPath.setPreferredSize(new Dimension(w,h));
		deviceappPath.setActionCommand(DEVICEAPP_ACTION);
		deviceappPath.getDocument().addDocumentListener(this);
		
		doFindProject = new JButton("Find Project");
		doFindProject.setPreferredSize(new Dimension(w1,h));
		doFindProject.setActionCommand(FIND_PROJECT_ACTION);
		doFindProject.setToolTipText("Browse to Instruments Project");
		doFindProject.addActionListener(this);
		
		iosProjectLabel = new JLabel("IOS Instruments Project Path:");
		iosProjectLabel.setPreferredSize(new Dimension(w2,h));
		iosProjectPath = new JTextField();
		iosProjectPath.setText(theIosProjectPath);
		iosProjectPath.setPreferredSize(new Dimension(w,h));
		iosProjectPath.setActionCommand(PROJECT_PATH_ACTION);
		iosProjectPath.getDocument().addDocumentListener(this);
		
		windowRecLabel = new JLabel("Window Recognition Method:");
		windowRecLabel.setPreferredSize(new Dimension(w,h));
		windowRec = new JTextField();
		windowRec.setText(theWindowRec);
		windowRec.setPreferredSize(new Dimension(w,h));
		windowRec.setActionCommand(WINDOWREC_ACTION);
		windowRec.setEnabled(false);

		objectRecLabel = new JLabel("Object Recognition Method:");
		objectRecLabel.setPreferredSize(new Dimension(w2,h));

		doChildren = new JCheckBox("Process Children", theDoChildren);
		doChildren.setPreferredSize(new Dimension(w1,h));
		doChildren.setActionCommand(CHILDREN_ACTION);

		objectRec = new JTextField();
		objectRec.setText(theObjectRec);
		objectRec.setPreferredSize(new Dimension(w,h));
		objectRec.setActionCommand(OBJECTREC_ACTION);
		//objectRec.setEnabled(false);
						
		windowNameLabel = new JLabel("Window/Object Name:");
		windowNameLabel.setPreferredSize(new Dimension(w2,h));

		doShortStrings = new JCheckBox("Short Strings", shortenGeneralRecognition);
		doShortStrings.setActionCommand(SHORTEN_ACTION);
		doShortStrings.setPreferredSize(new Dimension(w1,h));
		doShortStrings.addActionListener(this);

		windowName = new JTextField();
		windowName.setText(theObjectName);
		windowName.setPreferredSize(new Dimension(w2,h));
		windowName.setActionCommand(WINDOWNAME_ACTION);
		windowName.setName(WINDOWNAME_ACTION);		
		windowName.getDocument().addDocumentListener(this);
		
		doProperties = new JCheckBox("Process Properties", theDoProperties);
		doProperties.setPreferredSize(new Dimension(w1,h));
		doProperties.setActionCommand(PROPERTIES_ACTION);
		
		gbct.gridwidth = 2;
		gbl.setConstraints(doLaunch, gbct);
		north.add(doLaunch);
		
		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(doFindTemplate, gbc);
		north.add(doFindTemplate);
	
		gbct.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(traceTemplateLabel, gbct);
		north.add(traceTemplateLabel);		
		
		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(traceTemplatePath, gbcb);
		north.add(traceTemplatePath);

		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(doDeviceapp, gbcb);
		north.add(doDeviceapp);
		
		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(deviceappPath, gbcb);
		north.add(deviceappPath);
		
		gbct.gridwidth = 2;
		gbl.setConstraints(iosProjectLabel, gbct);
		north.add(iosProjectLabel);
		
		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(doFindProject, gbc);
		north.add(doFindProject);
	
		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(iosProjectPath, gbcb);
		north.add(iosProjectPath);

		gbct.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(windowRecLabel, gbct);
		north.add(windowRecLabel);		
		
		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(windowRec, gbcb);
		north.add(windowRec);

		gbct.gridwidth = 3;
		gbl.setConstraints(objectRecLabel, gbct);
		north.add(objectRecLabel);
		
		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(doChildren, gbc);
		north.add(doChildren);
		
		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(objectRec, gbcb);
		north.add(objectRec);

		gbct.gridwidth = 1;
		gbl.setConstraints(windowNameLabel, gbct);
		north.add(windowNameLabel);

		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(doShortStrings, gbc);
		north.add(doShortStrings);
		
		gbcb.gridwidth = 2;
		gbl.setConstraints(windowName, gbcb);
		north.add(windowName);

		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(doProperties, gbc);
		north.add(doProperties);		

		
		// CENTER PANEL		
		gbl = new GridBagLayout();
		center = new JPanel();
		center.setLayout(gbl);
		center.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		objectDescLabel = new JLabel("Object Description:");
		objectDescLabel.setPreferredSize(new Dimension(w,h));
		objectDesc = new JTextField();
		objectDesc.setText(theObjectDesc);
		objectDesc.setPreferredSize(new Dimension(w,h));
		objectDesc.setActionCommand(OBJECTDESC_ACTION);
		
		objectDesc.getDocument().addDocumentListener(this);
		
		outputDirLabel = new JLabel("Output Directory:");
		outputDirLabel.setPreferredSize(new Dimension(w,h));
		outputDir = new JTextField();
		outputDir.setText(theOutDir);
		outputDir.setPreferredSize(new Dimension(w,h));
		outputDir.setActionCommand(OUTPUTDIR_ACTION);
		outputDir.setName(OUTPUTDIR_ACTION);
		
		outputDir.getDocument().addDocumentListener(this);

		outputNameLabel = new JLabel("Output Filename Prefix:");		
		outputNameLabel.setPreferredSize(new Dimension(w2,h));
		
		doXMLOut = new JCheckBox("XML Output", theDoXMLOut);
		doXMLOut.setPreferredSize(new Dimension(w1,h));
		doXMLOut.setActionCommand(DOXMLOUT_ACTION);
		doXMLOut.addActionListener(this);
		
		outPrefix = new JTextField();
		outPrefix.setText(theOutPrefix);
		outPrefix.setPreferredSize(new Dimension(w,h));
		outPrefix.setActionCommand(OUTPREFIX_ACTION);
		outPrefix.setName(OUTPREFIX_ACTION);
		
		outPrefix.getDocument().addDocumentListener(this);
		
		outputFullname = new JButton();
		outputFullname.setPreferredSize(new Dimension(w,h));
		if ((outpath == null)||(outpath.length() < 1)) outpath = makeFullObjPath();
		outputFullname.setText(outpath);
		outputFullname.setToolTipText("View Object Output File");
		outputFullname.setActionCommand(OUTFILE_VIEW_ACTION);
		outputFullname.addActionListener(this);
				
		gbct.gridwidth = gbct.REMAINDER;
		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(objectDescLabel, gbct);
		center.add(objectDescLabel);
		gbl.setConstraints(objectDesc, gbcb);
		center.add(objectDesc);
		gbl.setConstraints(outputDirLabel, gbct);
		center.add(outputDirLabel);
		gbl.setConstraints(outputDir, gbcb);
		center.add(outputDir);

		gbct.gridwidth = gbct.gridwidth = 2;
		gbcb.gridwidth = gbcb.REMAINDER;
		
		gbl.setConstraints(outputNameLabel, gbct);
		center.add(outputNameLabel);
		gbl.setConstraints(doXMLOut, gbcb);
		center.add(doXMLOut);
		
		gbct.gridwidth = gbct.REMAINDER;
		gbcb.gridwidth = gbcb.REMAINDER;

		gbl.setConstraints(outPrefix, gbcb);
		center.add(outPrefix);
		gbl.setConstraints(outputFullname, gbct);
		center.add(outputFullname);
			
		// SOUTH PANEL
		gbl = new GridBagLayout();
		gbl.columnWidths = new int[]{w/9,w/9,w/9,w/9,w/9,w/9,w/9,w/9,w/9};
		south = new JPanel();
		south.setLayout(gbl);
			
		appendMap = new JCheckBox("Append AppMap", theAppendMap);
		appendMap.setActionCommand(APPENDMAP_ACTION);
		appendMap.addActionListener(this);

		addInfo = new JCheckBox("Add Component Info", theAddInfo);
		addInfo.setActionCommand(ADDINFO_ACTION);
		addInfo.setEnabled(false); //not yet implemented

		mapJPG = new JCheckBox("Map JPG", theMapJPG);
		mapJPG.setActionCommand(MAPJPG_ACTION);
		mapJPG.setEnabled(false); //not yet implemented

		appMapFile = new JTextField();
		appMapFileShow = new JButton("View");
		appMapFileShow.setToolTipText("View AppMap File");
		appMapFile.setPreferredSize(new Dimension((w/9)*8,h));
		appMapFileShow.setPreferredSize(new Dimension(w/9,h));
		if(mappath == null) mappath = makeFullPrefix(theOutDir, "AppMap.map");
		appMapFile.setText(mappath);		
		appMapFile.setActionCommand(APPMAPFILE_ACTION);
		appMapFile.setName(APPMAPFILE_ACTION);
		appMapFileShow.setActionCommand(APPMAPFILESHOW_ACTION);
		appMapFileShow.setName(APPMAPFILESHOW_ACTION);
		appMapFile.getDocument().addDocumentListener(this);
		appMapFileShow.addActionListener(this);
		
		delayToRunLabel = new JLabel(DELAY_TO_RUN_TEXT);
		delayToRunTextField = new JTextField(theDelayToRun);
		delayToRunTextField.setToolTipText(DELAY_TO_RUN_TOOLTIP);
		delayToRunLabel.setPreferredSize(new Dimension((w/9)*5,h));
		delayToRunTextField.setPreferredSize(new Dimension((w/9),h));
		
		run = new JButton(READY_TEXT);
		run.setActionCommand(RUN_ACTION);
		run.setBorder(BorderFactory.createRaisedBevelBorder());
		run.setPreferredSize(new Dimension(w1,h+4));
		run.addActionListener(this);

		cancel = new JButton("Cancel");
		cancel.setActionCommand(CANCEL_ACTION);
		cancel.setPreferredSize(new Dimension(w1-10,h+4));
		cancel.setBorder(BorderFactory.createRaisedBevelBorder());
		cancel.addActionListener(this);
		
		help = new JButton("Help");
		help.setActionCommand(HELP_ACTION);
		help.setPreferredSize(new Dimension(w1-10,h+4));
		help.setBorder(BorderFactory.createRaisedBevelBorder());
		//help.addActionListener(this);
		help.setEnabled(false);
		
		status = new JLabel("Status");
		status.setBorder(BorderFactory.createLoweredBevelBorder());
		status.setForeground(Color.DARK_GRAY);
		status.setPreferredSize(new Dimension(w,h));
		
		gbc.gridwidth = 3;
		gbl.setConstraints(appendMap, gbc);
		south.add(appendMap);
		gbl.setConstraints(addInfo, gbc);
		south.add(addInfo);

		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(mapJPG, gbc);
		south.add(mapJPG);

		gbc.gridwidth = gbc.RELATIVE;
		gbl.setConstraints(appMapFile, gbc);
		south.add(appMapFile);

		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(appMapFileShow, gbc);
		south.add(appMapFileShow);
		
		gbc.gridwidth = gbc.RELATIVE;
		gbl.setConstraints(delayToRunLabel, gbc);
		south.add(delayToRunLabel);
		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(delayToRunTextField, gbc);
		south.add(delayToRunTextField);
		
		gbc.gridwidth = 3;
		gbl.setConstraints(run, gbc);
		south.add(run);
		gbl.setConstraints(cancel, gbc);
		south.add(cancel);
	
		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(help, gbc);		
		south.add(help);
		
		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(status, gbc);		
		south.add(status);

		((BorderLayout)getContentPane().getLayout()).setVgap(7);
		getContentPane().add(north, BorderLayout.NORTH);		
		getContentPane().add(center, BorderLayout.CENTER);
		getContentPane().add(south, BorderLayout.SOUTH);		

		pack();
		setSize(getPreferredSize());
		//setResizable(false);
		configureUI();
		setVisible(true);
	}

	/**
	 * Display a status message.
	 * This message is also forwarded to Log.info
	 * @param info status String to display and log to Log.info
	 */
	protected void statInfo(String info){
		status.setText(info);
		Log.info(info);
	}
	
	/** 
	 * Add the prefix to our stored directory info adding(or not) the 
	 * File.separator if needed.
	 */	
	protected String makeFullPrefix(String theDir, String thePrefix){
		try{
			if (theDir.endsWith(File.separator)) return theDir + thePrefix;
			return theDir + File.separator + thePrefix;
		}
		catch(Exception x){ return theDir + thePrefix;}
	}
	
	protected String makeFullObjPath(){
		return theDoXMLOut ? makeFullPrefix(outputDir.getText(), outPrefix.getText())+"Obj.xml":
				             makeFullPrefix(outputDir.getText(), outPrefix.getText())+"Obj.txt";
	}
	
	/**
	 * Output our current settings to the provided INI file.
	 * If the file was not successfully opened, we will try to create it.
	 */
	protected void saveINIFileData(boolean closeit){
		Log.info("IOSPC attempting to save INI file configuration data...");
		try{
			if (inifile == null){
			    openINIReadWrite();
			}
			if (inifile != null) {
				Log.info("IOSPC writing initialization information to "+ inifile.getFullpath());
				inifile.setAppMapItem(null, LAUNCH_ACTION, String.valueOf(doLaunch.isSelected()));
				inifile.setAppMapItem(null, LAUNCH_TIMEOUT, String.valueOf(launchTimeout));
				String path = traceTemplatePath.getText();
				inifile.setAppMapItem(null, TEMPLATE_PATH_ACTION, path);
				inifile.setAppMapItem(null, DODEVICEAPP_ACTION, String.valueOf(doDeviceapp.isSelected()));
				path = deviceappPath.getText();
				inifile.setAppMapItem(null, DEVICEAPP_ACTION, path);
				path = iosProjectPath.getText();
				inifile.setAppMapItem(null, PROJECT_PATH_ACTION, path);
				inifile.setAppMapItem(null, OUTPUT_VIEWER_KEY, outputViewer);
				path = appMapFile.getText();
				inifile.setAppMapItem(null, APPMAPFILE_ACTION, path);
				path = outputFullname.getText();
				inifile.setAppMapItem(null, OUTFILE_ACTION, path);
				path = windowRec.getText();
				inifile.setAppMapItem(null, WINDOWREC_ACTION, path);
				path = objectRec.getText();
				inifile.setAppMapItem(null, OBJECTREC_ACTION, path);
				path = windowName.getText();
				inifile.setAppMapItem(null, WINDOWNAME_ACTION, path);
				path = objectDesc.getText();
				inifile.setAppMapItem(null, OBJECTDESC_ACTION, path);

				inifile.setAppMapItem(null, CHILDREN_ACTION, String.valueOf(doChildren.isSelected()));
				inifile.setAppMapItem(null, SHORTEN_ACTION, String.valueOf(doShortStrings.isSelected()));
				inifile.setAppMapItem(null, PROPERTIES_ACTION, String.valueOf(doProperties.isSelected()));
				inifile.setAppMapItem(null, DOXMLOUT_ACTION, String.valueOf(doXMLOut.isSelected()));
				
				path = outputDir.getText();
				inifile.setAppMapItem(null, OUTPUTDIR_ACTION, path);
				path = outPrefix.getText();
				inifile.setAppMapItem(null, OUTPREFIX_ACTION, path);

				inifile.setAppMapItem(null, APPENDMAP_ACTION, String.valueOf(appendMap.isSelected()));
				inifile.setAppMapItem(null, ADDINFO_ACTION, String.valueOf(addInfo.isSelected()));
				inifile.setAppMapItem(null, MAPJPG_ACTION, String.valueOf(mapJPG.isSelected()));
				inifile.setAppMapItem(null, DELAY_TO_RUN, delayToRunTextField.getText());
				
				//keep ini file formatted with UTF-8. Fix the issue of not displaying DBCS charactors in the ini file. --Junwu
				inifile.writeUTF8INIFile(null);  
				Log.info("SPC initialization information complete to "+ inipath);
				if(closeit) {					
					inifile.close();
					inifile = null;
				}
			}
		}catch(Exception x){
			Log.info("SPC EXCEPTION writing initialization information to "+ inipath,x);
		}
	}
	
	/**
	 * shutdown finalization
	 */	
	protected void finalize() throws Throwable {
		Log.info("SPC FINALIZE INVOKED.");
		try{status.setText("Shutting down...");}catch(Exception x1){}
		saveINIFileData(true);
		if (closeout) closeOutPathStream();
		if (closemap) closeMapPathStream();
		

		// TODO: Should we close Instruments and the Application?
		
		
		try{ super.finalize();}catch(Throwable x){;}
		Log.info("SPC FINALIZE COMPLETE.");
		if(weLaunchedSTAF){
			try{
				org.safs.STAFHelper staf = SingletonSTAFHelper.getHelper();
				if (staf != null){
					staf.shutDown(staf.getMachine());
					staf = null;
					//weLaunchedSTAF = false;
				}
			}catch(Exception x){
				System.err.println("UNABLE TO SHUTDOWN STAF: "+ x.getClass().getSimpleName());
			}
		}
		finalized = true;
	}
	
	/** 
	 * Attempt to retrieve an arg String value via our specific search chain.
	 * First try the command-line args directly for a match.  
	 * <ul>arg=value
	 * </ul>
	 * If not found there try the the appropriate System property that would have 
	 * been set on the command-line..
	 * <ul>-Dsafs.processcontainer.arg=value
	 * </ul>
	 * If not found there try looking for a stored value in the INI file.
	 * @return String value of the arg or null if the value was not found.
	 */
	protected String getArg(String argid){
		String arg;
		if(args != null){
			for(int i=0; i<args.length;i++){
				arg = args[i];
				if (arg.toLowerCase().startsWith(argid.toLowerCase()+"=")){
					return arg.substring(argid.length()+1);
				}
			}
		}
		arg = System.getProperty("safs.processcontainer."+ argid);
		if (arg == null) arg = getINIValue(DEFAULT_INI_SECTION, argid);
		return arg;
	}

	/**
	 * @return true if arg String value equals ignoring case to "true".  Any other 
	 * String value will result in a return of false.
	 */
	protected boolean getBooleanArg(String argid){
		return new Boolean(getArg(argid)).booleanValue();
	}

	/**
	 * @return the integer value of the retrieved arg/ini value or the provided defaultValue. 
	 * The value will be limited to >= 0.
	 */
	protected int getIntegerArg(String argid, int defaultValue){
		int value = defaultValue;
		try{
			value = Integer.parseInt(getArg(argid));
		}catch(NumberFormatException x){}
		if (value < 0) value = 0;
		return value;
	}

	/**
	 * 'Runnable' Shutdown Hook registered with JVM to do finalization on exit.
	 * Users MUST NOT call this as it will execute the Object's finalize() method.
	 */
	public void run(){
		try{finalize();}
		catch(Throwable t){;}
	}

    protected void takeFocus(){
        toFront();	
    }
    
	/**************************************************************************
	 * main 
	 *************************************************************************/
	public static void main(String[] args) {		
		IOSProcessContainer.args = args;
		IOSProcessContainer pc = new IOSProcessContainer();	
		pc.show();
		while(!pc.finalized){
			try{Thread.sleep(1000);}catch(Exception x){}
		}
		try{Thread.sleep(3000);}catch(Exception x){}
		System.exit(0);
	}
	
	public class ShutdownListener extends WindowAdapter {
		public void windowClosing(WindowEvent event){
			Log.info("SPC WINDOW CLOSING EVENT RECEIVED...");
			shutdown = true;
			setInterrupt(true);
			setStopped(true); 			
		}
	}
	
	public class RunThread implements Runnable{
    
		public void run(){
			//Delay before running, so that we have time to RightMouseClick to show Popup Menu
			try{
				String dealy = delayToRunTextField.getText();
				statInfo(DELAYING_TEXT);
				int delaySeconds = Integer.parseInt(dealy);
				Thread.sleep(delaySeconds*1000);
			}catch(Exception e){ /* ignore any delay problem */	}
			
			statInfo(RUNNING_TEXT);
			saveINIFileData(false);
			startFileOutput();
			
			try{
				Utilities.ROOT_INSTRUMENTS_PROJECT_DIR = iosProjectPath.getText();
				
				Log.info("IOSPC preparing "+ Utilities.JSCRIPTS_IOSPCDATA +"...");
			    Utilities.prepareNextProcessContainerData(null, //targetdir
                    doChildren.isSelected(),
                    doProperties.isSelected(),
                    appendMap.isSelected(),
                    addInfo.isSelected(),
                    doShortStrings.isSelected(),
                    windowName.getText(),
                    windowRec.getText(),
                    objectRec.getText());
			}catch(Exception x){
				statInfo("IOSPC Data "+ x.getClass().getSimpleName()+": "+ x.getMessage());
				stopFileOutput();
				takeFocus();
				
				// TODO: throw up an alert dialog!
				
				return;				
			}
			
			if(doLaunch.isSelected()){
			  try{
				Utilities.INSTRUMENTS_LAUNCH_TIMEOUT = launchTimeout;
				Utilities.prepareNewAutomationTest();			
				Utilities.presetProcessContainerInstrumentsScript();
				int tried = 0;
				boolean success = false;
				String arguments = null;
				String thedeviceapp = "";
				while(!success && tried++ < 2){
					Log.info("IOSPC preparing to launch IOS Instruments for Process Container execution.");
				    try{
				    	arguments = traceTemplatePath.getText();
				    	if(doDeviceapp.isSelected()){
							Log.info("IOSPC checking IOS Instruments Device and App settings.");
				    		try{ thedeviceapp = deviceappPath.getText();}catch(Exception x){}
				    		if(thedeviceapp.length() > 11){ // "-d a -app b" minimally
				    			arguments +=" "+ thedeviceapp;
				    		}else{
				    			// not a valid setting.  Probably should popup a dialog.
				    		}
				    	}
						Log.info("IOSPC attempting to launch IOS Instruments using "+ arguments);
					    Utilities.launchInstrumentsTest(arguments);
					    
					    // for IOS 5 we need to detect if we have to set the script
					    // we run AppleScript which will do this ONLY for IOS 5
					    Utilities.runAppleScript(null, Utilities.PATH_SELECT_RECENT_SCRIPT_ASCRIPT, 
					    		                       Utilities.DEFAULT_PROCESSCONTAINER_SCRIPT, true, 10);
					    
					    Utilities.startInstrumentsTest();
					    Utilities.verifyInstrumentsRecording(null);
					    success = true;
					    launch = false;
					    doLaunch.setSelected(false);
						configureUI();
				    }catch(InstrumentsStartScriptException x){
						Log.info("Launch Instruments "+x.getClass().getSimpleName()+"; "+x.getMessage());
					    Utilities.killAllInstruments();
					    Utilities.killAllSimulators();
					    try{Thread.sleep(1000);}catch(Exception x2){}
				    }
			    }
				if(!success) throw new InstrumentsStartScriptException("IOSPC did not detect proper Instruments execution after "+ tried +" attempts.");
			  }
			  catch(Exception x){
				statInfo("IOSPC Launch "+ x.getClass().getSimpleName()+": "+ x.getMessage());
				stopFileOutput();
				takeFocus();
				Utilities.restoreInstrumentsScript();

				// TODO: throw up an alert dialog!
				
				return;
			  }
			}else{ // not launching Instruments.  It should be running already, but we don't really know.
                try{
                	Utilities.findRunningLastLine(null);
    			    Utilities.nextInstrumentsTest();
                }
			    catch(Exception x){
				    statInfo("IOSPC Run "+ x.getClass().getSimpleName()+": "+ x.getMessage());
					stopFileOutput();
					takeFocus();
				    Utilities.restoreInstrumentsScript();			    

				    // TODO: throw up an alert dialog!
					
					return;
			    }
			}
            try{
				Log.info("IOSPC Waiting for ProcessContainer script completion...");
    			Utilities.waitScriptComplete();
    			IStatus stat =  Utilities.getIStatus();
            }
			catch(Exception x){
				statInfo("IOSPC Wait "+ x.getClass().getSimpleName()+": "+ x.getMessage());
				stopFileOutput();
				takeFocus();
				Utilities.restoreInstrumentsScript();			    

			    // TODO: throw up an alert dialog!
				
				return;
			}
			stopFileOutput();
			statInfo(FINISHED_TEXT);
			takeFocus();
		}
		
		/**
		 * Start/Open output file and, if enabled, the App Map file.
		 */
		void startFileOutput(){
			openOutPathStream();
			if(theAppendMap) openMapPathStream();
		}
		
		/**
		 * Close the output file and any App Map file.
		 */
		void stopFileOutput(){
			closeOutPathStream();
			closeMapPathStream();
		}
	}	
}