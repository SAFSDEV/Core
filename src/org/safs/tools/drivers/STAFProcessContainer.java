/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.tools.drivers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.safs.image.ImageUtils;
import org.safs.jvmagent.LocalServerGuiClassData;
import org.safs.jvmagent.SAFSInvalidActionArgumentRuntimeException;
import org.safs.jvmagent.STAFLocalServer;
import org.safs.natives.MenuUtilities;
import org.safs.staf.STAFProcessHelpers;
import org.safs.text.FileUtilities;
import org.safs.text.INIFileReadWrite;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.engines.EngineInterface;

/**
 * Provides similar functionality as the RRAFS ProcessContainer app in a SAFS tool-independent and multiplatform environment.
 * <p>
 * {@link <img src="STAFProcessContainer.GIF" alt="STAFProcessContainer Screenshot" align="left" hspace="4"/>}
 * <p>
 * <b>Client&nbsp;Type</b>&nbsp;(ComboBox)<br>
 * The type of client (HTML, Java, .NET, etc..) intended to be processed.  
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
 * <b>Short&nbsp;Names</b>&nbsp;(CheckBox)<br>
 * When an object is identifiable by Name output very short recognition strings.  
 * This is because objects with unique names are safely identified wherever they are in 
 * the hierarchy.
 * <p>
 * <b>Ignore&nbsp;Invisible</b>&nbsp;(CheckBox)<br>
 * Do not process invisible containers--containers often hidden or overlayed other 
 * containers.  For example, a TabControl with 5 Tabs typically has 5 panels, but 
 * only one panel is usually visible at a time.  By ignoring the hidden panels we usually 
 * get more accurate recognition strings (indices) for those that are visible.
 * <p>
 * <b>Process&nbsp;Properties</b>&nbsp;(CheckBox)<br>
 * Capture and output the list of all available properties on each object that is 
 * processed.  This can be VERY VERY time consuming, so enable this only when you 
 * really want it.
 * <p>
 * <b>Process&nbsp;Menu</b>&nbsp;(CheckBox)(Disabled Future Feature)<br>
 * Attempt to locate and process a main menu for the Window to be processed.  This 
 * historically only applied to native OS applications (Windows).  
 * Processing involves identifying all menus, menuitems, their properties and state 
 * information.
 * <p>
 * <b>Menu&nbsp;Name</b>&nbsp;(TextField)(Disabled Future Feature)<br>
 * A name to give the menu to be processed.
 * <p>
 * <b>Object&nbsp;Description</b>&nbsp;(TextField)<br>
 * A short description of the object being processed.
 * <p>
 * <b>Output&nbsp;Directory</b>&nbsp;(TextField)<br>
 * The directory where output files should be written.
 * <p>
 * <b>Output&nbsp;Filename&nbsp;Prefix</b>&nbsp;(TextField)<br>
 * The root name to give the object and menu output files.  The tool will append "Obj.txt" 
 * and "Menu.txt" to this root to form the complete filenames for output.
 * <p>
 * <b>Output&nbsp;Filenames</b>&nbsp;(Labels)<br>
 * Display of the filenames that will be output based on the Output Directory and 
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
 * <hr><p>
 * While this class is a JFrame, it wraps a 
 * {@link <a href="STAFProcessContainerDriver.html">STAFProcessContainerDriver</a>} 
 * and requires Driver configuration information (INI file) just like the 
 * {@link <a href="SAFSDRIVER.html">SAFSDRIVER (TID)</a>}
 * <p>
 * The ProcessContainer.INI file for this tool can contain configuration information for both the SAFS Driver and
 * the Process Container functionality.  A bare minimum configuration would identify the location 
 * of the SAFS Project in which additional configuration information might be provided in SAFSTID.INI 
 * files:
 * <p><ul><pre>
 * [SAFS_PROJECT]
 * ProjectRoot="c:\SAFSProject"
 * </pre></ul>
 * <p>
 * More likely the user will want to identify SAFS Engines to use\launch for Process Container to 
 * interact with:
 * <p><ul><pre>
 * [SAFS_PROJECT]
 * ProjectRoot="c:\SAFSProject"
 * 
 * [SAFS_ENGINES]
 * First=org.safs.tools.engines.SAFSROBOTJ
 * 
 * [SAFS_ROBOTJ]
 * AUTOLAUNCH=TRUE
 * PLAYBACK=TestScript
 * DATASTORE=C:\RFTDatastore
 * INSTALLDIR="C:\Program Files\IBM\Rational\SDP\6.1\FunctionalTester\eclipse\plugins\com.rational.test.ft.wswplugin_6.1.0"
 * </pre></ul>
 * <p>
 * With information like this the STAFProcessContainer can be successfully launched and used 
 * with a command line like:
 * <p><ul><pre>
 * java -Dsafs.processcontainer.ini=c:\SAFSProject\ProcessContainer.ini org.safs.tools.drivers.STAFProcessContainer
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
 * <li>"menu=&lt;path to menu output file>"
 * <li>"client=Java Client"
 * <li>"windowRec=Type=Window;Caption={*}"
 * <li>"objectRec=\;Type=Window;Caption={*}"
 * <li>"objectName=&lt;objectname>"
 * <li>"objectDesc=&lt;object description>"
 * <li>"outputviewer=&lt;file viewer/editor>"
 * <li>"fullpathSearch=true"
 * <li>"doChildren=true"
 * <li>"doProperties=false"
 * <li>"doMenu=false"
 * <li>"menuName=&lt;menuname>"
 * <li>"outDir=&lt;path to output directory>"
 * <li>"outPrefix=&lt;objectname>"
 * <li>"appendMap=false"
 * <li>"addInfo=false"
 * <li>"mapJPG=false"
 * <li>"ignoreInvisible=true"
 * <li>"shortRecognition=false"
 * <li>"shortWithName=false"
 * <li>"ignoreChildrenInListTableCombobox=true"
 * <li>"delayToRun=4"
 * <li>"rsStrategy.qualifier=true"
 * <li>"rsStrategy.useName=true"
 * <li>"rsStrategy.useid=false"
 * <li>"rsStrategy.useGenericType=false"
 * </ul>   
 * @author canagl
 * @author CANAGL JUN 07, 2005 Enhanced support for showing indexed properties.
 *         JunwuMa JUN 19,2008 Add a warning message if ignoreInvisible is Unchecked while clicking 'Run', 
 *                informing users that RS information to be created may be incorrect, can't be used by RJ engine.  
 *                RJ engine ignores invisible containers when matching a component.
 
 *         JunwuMa JUL 28,2008 Added a new feature. It is able to show the hierarchy of all components in an application captured by 
 *                 STAFProcessContainer, as well as highlight a component by clicking its corresponding node in the hierarchy. It can 
 *                 graphically help users figure out what the recognition string is for selected component. 
 *                 After 'Run' is finished, the created PCTree shall be shown in a JavaTree hierarchally. Every node in the JavaTree 
 *                 represents a component in the tested application. By clicking a node, its matching component is going 
 *                 to be highlighted by re-drawing its border in red. See engine command hightLightMatchingChildObject.
 *                 Added HierarchyDlg and RsPCTreeMap.
 *                 
 *         JunwuMa NOV 06, 2008 Modified writeTree() and RsPCTreeMap.CreateJTreeNodes(),
 *                 supporting ignoring some containers both in mapping file and in component hierarchy. Some containers are not 
 *                 cared by users; they are toggled together, make tree levels deeper, make it difficult to find out the cared components 
 *                 under them. This ignoring operation will be performed according to this.shortenGeneralRecognition, which is decided by
 *                 CheckBox 'Short Strings'. 
 *                 See PCTree#isIgnoredNode(),PCTree#toIniStringWithoutIgnoredNodes() and GuiObjectRecognition#isContainerTypeIgnoredForRecognition(String)
 *                 
 *                 NOV 07, 2008 Enhance highlighting feature, providing highlighting operation an individual thread, adding a message box notifying users 
 *                 the target component is whether found or not. 
 *                 NOV 11, 2008 Modified genRecogString(PCTree,Object,String), setting the object's class name to the pctree passed in.
 *                 See PCTree#setObjectClass(String). The class name of every represented component in PCTree is held in its node. 
 *         
 *                 NOV 27, 2008 Add 'Advanced Settings' for users who want to use ID and AccessibleName/Name to generate R-Strings.
 *                 JAN 16, 2009 Modify the GUI of 'Advanced Settings'. Add an option to let user decide if using Index qualifier only to 
 *                              generate R-String. 
 *                 MAR 3,  2009 Add a feature that allows users search string to get all matching components in Component Hierarchy, 
 *                              and move on back and forth in the matching list.             
 *                 MAY 28, 2009 Added support for a complete shutdown including STAF if SPC launched STAF. 
 *                 AUG  6, 2009 Reset searching domains on RJ side if clientType changed. 
 *          CANAGL AUG  6, 2005 Added outputviewer option to launch output file viewer.
 *          	                Also fixed output files to use proper System line.separator characters.
 *         LeiWang SEP 07, 2009 Add an option mappedClassSearchMode. If this option is set, RJ engine will process only mappable
 *         						TestObject of GUI Tree, which will reduce much the time to process searching, especially for
 *         						Web Application.
 *         JunwuMa OCT 16, 2009 Adding Capture button in Hierarchy Viewer. It allows user to get recognition strings by 
 *                              hovering mouse over a GUI control in the testing application SPC against. Similar to RFT's Inspector.
 *         JunwuMa OCT 27, 2009 Update highlight functionality to send GUI control's key instead of its r-string to RJ-engine for lookup... 
 *         LeiWang OCT 28, 2009 Add a TextField which permit user to input a delay time before the SPC begin to run.
 *         JunwuMa DEC 29, 2009 Add an option for Process Children allowing user to ignore/unignore the components embedded in TABLE/LISTBOX/LISTVIEW/COMBOBOX.
 *         LeiWang SEP 27, 2010 Save some settings of SPC to ini file, like "Ignore the Children", "Advanced Settings" and "Delay time"
 *         LeiWang NOV 02, 2010 Implement the "Map JPG" function.
 *         Dharmesh4 FEB 11, 2011 Fixed an issue where popup menu was activated, for that, added 'isTopLevelPopupContainer' method.
 *         Dharmesh4 MAY 25, 2011 Added Flex RFSM Search recognition support.
 *         CANAGL  MAY 17, 2012 Added System.out messages for STAF launch reporting and debugging.
 *         LeiWang JAN 29, 2013 Adjust the highLight and Dispose button's enable status of HierarchyDlg.
 *                              Modify method processParent(): when set component's key to the Tree, don't call toUppoerCase().
 *         LeiWang APR 15, 2013 Update HierarchyDlg to let user modify the name of tree node, so that an appropriate name will be wrote to Map.
 *         LeiWang MAY 28, 2013 For android domain, 'MCSM' and 'RFSM' will be disabled.
 *         LeiWang MAY 31, 2013 For android domain, ignore invisible node, ignore children of 'GridView' and 'Spinner'.
 *                              Show the hierarchy viewer even the check box 'process children' is not checked.
 *                              
 */
public class STAFProcessContainer extends JFrame implements ActionListener, 
                                                            DocumentListener,
                                                            ListSelectionListener,
                                                            Runnable{
	
	/** "STAF Process Container" */
	public static final String STAF_PROCESS_CONTAINER_TITLE = "STAF Process Container";
	
	/** "STAFProcessContainer" */
	public static final String STAF_PROCESS_CONTAINER_PROCESS = "STAFProcessContainer";

	/** JAVA_CLIENT_DISPLAY */
	public static final String DEFAULT_CLIENT_DISPLAY = DriverConstant.JAVA_CLIENT_DISPLAY;
	/** JAVA_CLIENT_TEXT */
	public static final String DEFAULT_CLIENT_TEXT = DriverConstant.JAVA_CLIENT_TEXT;		
	/** VISIBLE_COUNT_IN_LIST*/
	public static final int VISIBLE_COUNT_IN_LIST = 2;

	public static final String ENCODING_UTF8 = "UTF-8";
	
	public static String line_separator = System.getProperty("line.separator");
	
	STAFLocalServer  server;		
	GuiClassData classdata;
	STAFProcessContainerDriver driver;
	Processor processor = null;
	HierarchyDlg mydialog = null;
	HierarchyDlg rsdialog = null;
	
	OutputStream out = System.out;
	OutputStream map = System.out;
	boolean closeout = false;	//set true if OutputStream out is NOT System.out
	boolean closemap = false;	//set true if OutputStream map is NOT System.out
	
	private boolean interrupted = false;
	private boolean stopped = false;
	private boolean shutdown = false;
	private boolean finalized = false;
	
	public void setInterrupt(boolean _interrupted){
		Log.info("SPC SET INTERRUPT '"+ _interrupted +"' RECEIVED.");
		if(!_interrupted) setStopped(false);
		interrupted = _interrupted;
	}	
	public boolean isInterrupted(){
		return interrupted;
	}
	
	public void setStopped(boolean _stop){
		Log.info("SPC SET STOPPED '"+ _stop +"' RECEIVED.");
		stopped = _stop;
		if(stopped && shutdown){
			Log.info("SPC STOPPED WITH SHUTDOWN DETECTED.");
			try{finalize();}catch(Throwable t){;}			
			dispose();
			//System.exit(0);
		}
	}	
	public boolean isStopped(){
		return stopped;
	}
	
	
	INIFileReadWrite inifile = null;
	
	/**
	 * When true enables FULLPATH_SEARCH_MODE recognition string use.  
	 * This mode is NOT compatible with the tradional CLASSIC_SEARCH_MODE.
	 * Defaults to false.
	 */
	boolean fullpathSearchMode = false;
	
	/**
	 * When true enables MAPPED_CLASS_SEARCH_MODE recognition string use.
	 * That means only mappable children will be returned.
	 * For RFT, testObject.getMappableChildren() will be used instead of 
	 * testObject.getChildren().
	 * Defaults to false.
	 */
	boolean mappedClassSearchMode = false;
	
	/**
	 * When true enables RFT_FIND_SEARCH_ACTION recognition string use.
	 * That means only mappable children will be returned.
	 * For RFT, testObject.getMappableChildren() will be used instead of 
	 * testObject.getChildren().
	 * Defaults to false.
	 */
	boolean rfsmSearchMode = false;
	
	/**
	 * When true we will not process containers that are not visible.
	 * For example, hidden non-displayed panels (tabs) within a TabControl.
	 * JVM Command-Line: -Dsafs.processcontainer.ignoreInvisible=
	 * System  Property:safs.processcontainer.ignoreInvisible=
	 * INI File Setting: ignoreInvisible=
	 * main() ARGS Parm: ignoreInvisible=
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */
	boolean ignoreInvisible = true;
	/**
	 * When true and a component is recognized by Name will shorten the recognition string 
	 * to be just the topmost parent and the child component.  All intermediate hierarchy 
	 * information will be removed or ignored.
	 * JVM Command-Line: -Dsafs.processcontainer.shortWithName=
	 * System  Property:safs.processcontainer.shortWithName=
	 * INI File Setting: shortWithName=
	 * main() ARGS Parm: shortWithName=
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */
	boolean withNameIncludeOnlyCaption = false;
	
	boolean withCommentsAndBlankLines  = false; 	// ???  needed?
	boolean iniVsTreeFormat            = true; 		// ???  needed?
    /**
	 * true if recognition strings should be stripped of intermediate parent recognition 
	 * info that may be deemed unnecessary.  For example, we know that the contentPane and 
	 * layoutPane in Java JFrames and several intermediate HTML tags are unnecessary.
	 * JVM Command-Line: -Dsafs.processcontainer.shortRecognition=
	 * System  Property:safs.processcontainer.shortRecognition=
	 * INI File Setting: shortRecognition=
	 * main() ARGS Parm: shortRecognition=
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	*/
	boolean shortenGeneralRecognition = false;
	
	/**
	 *  Holding some information for generating R-Strings optionally.
	 *  Related to qualifier ID and AccessibleName/Name, Index=
	 *  Initially new a RStringStrategy() with default options
	 *  
	 *  These options will be initialized by following settings
	 *  
	 * JVM Command-Line: -Dsafs.processcontainer.rsStrategy.qualifier=
	 * System  Property:safs.processcontainer.rsStrategy.qualifier=
	 * INI File Setting: rsStrategy.qualifier=
	 * main() ARGS Parm: rsStrategy.qualifier=
	 * 
	 * JVM Command-Line: -Dsafs.processcontainer.rsStrategy.qualifier.useid=
	 * System  Property:safs.processcontainer.rsStrategy.qualifier.useid=
	 * INI File Setting: rsStrategy.qualifier.useid=
	 * main() ARGS Parm: rsStrategy.qualifier.useid=
	 * 
	 * JVM Command-Line: -Dsafs.processcontainer.rsStrategy.qualifier.useName=
	 * System  Property:safs.processcontainer.rsStrategy.qualifier.useName=
	 * INI File Setting: rsStrategy.qualifier.useName=
	 * main() ARGS Parm: rsStrategy.qualifier.useName=
	 * 
	 * JVM Command-Line: -Dsafs.processcontainer.rsStrategy.useGenericType=
	 * System  Property:safs.processcontainer.rsStrategy.useGenericType=
	 * INI File Setting: rsStrategy.useGenericType=
	 * main() ARGS Parm: rsStrategy.useGenericType=
	 * 
	 * JVM Command-Line: -Dsafs.processcontainer.rsStrategy.useClassNotSubType=
	 * System  Property:safs.processcontainer.rsStrategy.useClassNotSubType=
	 * INI File Setting: rsStrategy.useClassNotSubType=
	 * main() ARGS Parm: rsStrategy.useClassNotSubType=
	 * 
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */
	RStringStrategy rstringStrategy = new RStringStrategy();
	
	/**
	 * It decides if ignoring the children of TLC (TABLE/LISTBOX/LISTVIEW/COMBOBOX) When running with 'Processing Children' checked 
	 * TLC' children usually are NOT cared by user. It is true as default.
	 * JVM Command-Line: -Dsafs.processcontainer.ignoreChildrenInListTableCombobox=true
	 * System  Property:safs.processcontainer.ignoreChildrenInListTableCombobox=true
	 * INI File Setting: ignoreChildrenInListTableCombobox=true
	 * main() ARGS Parm: ignoreChildrenInListTableCombobox=true
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 * 
	 */
	boolean m_ignoreChildInTLC = true;

	String   domainname = DEFAULT_CLIENT_TEXT;		// "Java" at least for now
	Map      nameMap = new HashMap();

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

	/** 
	 * Provided path to menu output file.
	 * JVM Command-Line: -Dsafs.processcontainer.menu=
	 * System  Property:safs.processcontainer.menu=
	 * INI File Setting: menu=
	 * main() ARGS Parm: menu=
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String menupath = null;

	/** 
	 * Provided clientType. Support values at this time: "Java Client"
	 * JVM Command-Line: "-Dsafs.processcontainer.client=Java Client"
	 * System  Property:safs.processcontainer.client="Java Client"
	 * INI File Setting: client="Java Client"
	 * main() ARGS Parm: "client=Java Client"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theClientType = DEFAULT_CLIENT_DISPLAY;

	/** "CurrentWindow" */
	public static final String DEFAULT_WINDOWREC = "CurrentWindow";

	/** 
	 * Provided windowRec. Defaults to CurrentWindow
	 * JVM Command-Line: "-Dsafs.processcontainer.windowRec=CurrentWindow"
	 * System  Property:safs.processcontainer.windowRec="CurrentWindow"
	 * INI File Setting: windowRec="CurrentWindow"
	 * main() ARGS Parm: windowRec="CurrentWindow"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theWindowRec = DEFAULT_WINDOWREC;

	/** "CurrentWindow" */
	public static final String DEFAULT_OBJECTREC = "CurrentWindow";

	/** 
	 * Provided objectRec. Defaults to CurrentWindow
	 * JVM Command-Line: "-Dsafs.processcontainer.objectRec=CurrentWindow"
	 * System  Property:safs.processcontainer.objectRec="CurrentWindow"
	 * INI File Setting: objectRec="CurrentWindow"
	 * main() ARGS Parm: objectRec="CurrentWindow"
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
	public static final boolean DEFAULT_DOMENU = false;

	/** 
	 * Provided doMenu. Defaults to false.
	 * JVM Command-Line: "-Dsafs.processcontainer.doMenu=true"
	 * System  Property:safs.processcontainer.doMenu="true"
	 * INI File Setting: doMenu="true"
	 * main() ARGS Parm: "doMenu=true"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected boolean theDoMenu = DEFAULT_DOMENU;

	/** "WindowNameMenu" */
	public static final String DEFAULT_MENUNAME = "WindowNameMenu";

	/** 
	 * Provided menuName. Defaults to WindowNameMenu
	 * JVM Command-Line: "-Dsafs.processcontainer.menuName=WindowNameMenu"
	 * System  Property:safs.processcontainer.menuName="WindowNameMenu"
	 * INI File Setting: menuName="WindowNameMenu"
	 * main() ARGS Parm: "menuName=WindowNameMenu"
	 * <p>An ARGS Parm overrides the System Property which overrides any INI file setting.
	 */	
	protected String theMenuName = DEFAULT_MENUNAME;

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
	 * Time to delay before SPC starts to search. Defaults to 2.
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

	JLabel windowRecLabel;
	JLabel objectRecLabel;
	JLabel windowNameLabel;
	JLabel objectDescLabel;
	JLabel outputDirLabel;
	JLabel outputNameLabel;
	JLabel menuNameLabel;
	JLabel domainsLabel;

	JButton outputFullname;
	String OUTFILE_ACTION    = "out";
	String OUTFILE_VIEW_ACTION = "outShow";
	JButton menuFullname;
	String MENUFILE_ACTION   = "menu";
	String MENUFILE_VIEW_ACTION = "menuShow";
	
	JList clientType;
	String CLIENTTYPE_ACTION = "clientType";
	JScrollPane clientTypeScrollPane;
	
	String FULLPATH_SEARCH_ACTION = "fullpathSearch";
	String MAPPED_CLASS_SEARCH_ACTION = "mappedClassSearchMode";
	String RFT_FIND_SEARCH_ACTION = "rfsmSearchMode";
	
	JCheckBox doProperties;
	String PROPERTIES_ACTION = "doProperties";
	JCheckBox doChildren;
	String CHILDREN_ACTION   = "doChildren";
	JButton doChildrenSet;
	String CHILDRENSET_ACTION   = "doChildren_Set";
	String IGNORE_CHILDREN   = "ignoreChildrenInListTableCombobox";
	
	JCheckBox appendMap;
	String APPENDMAP_ACTION  = "appendMap";
	JCheckBox doMenu;
	String MENU_ACTION       = "doMenu";
	JCheckBox addInfo;
	String ADDINFO_ACTION    = "addInfo";
	JCheckBox mapJPG;
	String MAPJPG_ACTION     = "mapJPG";
	JCheckBox doShortStrings;
	String SHORTEN_ACTION    = "shortRecognition";
	JCheckBox doIgnoreInvisible;
	String INVISIBLE_ACTION  = "ignoreInvisible";
	JCheckBox doShortNames;
	String SHORTNAME_ACTION  = "shortWithName";	
	JCheckBox mappedClassSearch;
	JButton advancedSettings;
	JCheckBox rfsmSearch;
	String 	ADVANCEDSETTINGS_ACTION = "advancedSettings";
		
	JTextField windowRec;
	String WINDOWREC_ACTION   = "windowRec";	
	JTextField windowName;
	String WINDOWNAME_ACTION  = "objectName";	
	JTextField objectRec;
	String OBJECTREC_ACTION   = "objectRec";	
	JTextField objectDesc;
	String OBJECTDESC_ACTION  = "objectDesc";	
	JTextField menuName;
	String MENUNAME_ACTION    = "menuName";	
	JTextField outputDir;
	String OUTPUTDIR_ACTION   = "outDir";	
	JTextField outPrefix;
	String OUTPREFIX_ACTION   = "outPrefix";
	
	JTextField appMapFile;
	String APPMAPFILE_ACTION  = "map";
	JButton appMapFileShow;
	String APPMAPFILESHOW_ACTION  = "map_show";
	
	JLabel delayToRunLabel;
	JTextField delayToRunTextField;
	String DELAY_TO_RUN_TEXT = "Delay time (seconds): ";
	String DELAY_TO_RUN_TOOLTIP = "Delay time before runnin SPC, time is in second. Input a number.";
	String DELAY_TO_RUN = "delayToRun";
	
	
	JButton run ;
	String RUN_ACTION    = "run";
	JButton cancel ;
	String CANCEL_ACTION = "cancel";
	JButton help ;
	String HELP_ACTION   = "help";
	
	JLabel status ;
	
	boolean weLaunchedSTAF = false;
	
	String outputViewer = "notepad.exe";
	public static final String OUTPUT_VIEWER_KEY = "outputviewer";

	private String APPFILE_VIEW_ACTION;
	
	/** "Click to Run" */
	public static final String READY_TEXT="Click to Run";

	/** "Running..." */
	public static final String RUNNING_TEXT="Running...";
	public static final String DELAYING_TEXT="Delaying...";
	
	/**
	 * Constructor for ProcessContainer.
	 */
	public STAFProcessContainer() {
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
			System.out.println("STAFRegistration Exception...attempting to launch STAF.");
			try{
				staf = STAFProcessHelpers.launchSTAFProc(processName);
				SingletonSTAFHelper.setInitializedHelper(staf);
				weLaunchedSTAF = true;
			}
			catch(org.safs.SAFSSTAFRegistrationException rx2){
				System.err.println("STAFProcessContainer unable to launch STAF or register with STAF:\n"+ rx2.getMessage());
				return;
			}
		}
		if(staf.isInitialized()) Log.setHelper(staf);
		
		try{
			if (System.getProperty(DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG)==null)		
				System.setProperty(DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG, inipath);
		}catch(Exception x){;}
		
		driver = new STAFProcessContainerDriver();
		driver.initializeDriver();			
		
		openINIReadWrite();

 		fullpathSearchMode = getBooleanArg(FULLPATH_SEARCH_ACTION);
 		mappedClassSearchMode = getBooleanArg(MAPPED_CLASS_SEARCH_ACTION);
 		rfsmSearchMode = getBooleanArg(RFT_FIND_SEARCH_ACTION);

		String val = getArg(APPMAPFILE_ACTION);	
		if (val != null) mappath = val;

		val = getArg(OUTFILE_ACTION);
		if (val != null) outpath = val;

		val = getArg(MENUFILE_ACTION);
		if (val != null) menupath = val;
		
 		val = getArg(CLIENTTYPE_ACTION);
 		if (val != null) theClientType = val; 		

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
 		ignoreInvisible = getBooleanArg(INVISIBLE_ACTION);
 		withNameIncludeOnlyCaption = getBooleanArg(SHORTNAME_ACTION);
 		theDoProperties = getBooleanArg(PROPERTIES_ACTION);
 		theDoMenu       = getBooleanArg(MENU_ACTION);
 		m_ignoreChildInTLC = getBooleanArg(IGNORE_CHILDREN);

 		val = getArg(DELAY_TO_RUN);
 		if(val!=null) theDelayToRun = val;
 		
 		val = getArg(MENUNAME_ACTION);
 		if (val != null) theMenuName = val;

 		val = getArg(OUTPUTDIR_ACTION);
 		if (val != null) theOutDir = val;

 		val = getArg(OUTPREFIX_ACTION);
 		if (val != null) theOutPrefix = val;

 		theAppendMap = getBooleanArg(APPENDMAP_ACTION);
 		theAddInfo   = getBooleanArg(ADDINFO_ACTION);
 		theMapJPG    = getBooleanArg(MAPJPG_ACTION);

 		boolean useQualitier = getBooleanArg(RStringStrategy.AUTO_QUALIFIER);
 		if(useQualitier){
 			rstringStrategy.setIfIndexOnly(false);
 			rstringStrategy.setIfUseId(getBooleanArg(RStringStrategy.QUALIFIER_USE_ID));
 			rstringStrategy.setIfAccessibleNamePriority(getBooleanArg(RStringStrategy.QUALIFIER_USE_NAME));
 		}else{
 			rstringStrategy.setIfIndexOnly(true);
 		}
 		rstringStrategy.setUseGenricType(getBooleanArg(RStringStrategy.USE_GENERIC_TYPE));
 		rstringStrategy.setUseClassNotSubType(getBooleanArg(RStringStrategy.USE_CLASS_NOT_SUBTYPE));
 		
		try{
			server = new STAFLocalServer();
			server.launchInterface(driver);
			classdata = new LocalServerGuiClassData(server);
		}
		catch(Exception x){ x.printStackTrace();}
		
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
			out = new FileOutputStream(afile);
			closeout = true;
		}
		// SecurityExceptions or FileNotFoundException, etc..
		catch(SecurityException sx){ out = System.out;}
		catch(FileNotFoundException nf){
			out = System.out;
			System.err.println("*** ERROR IN DETAILS OUTPUT FILENAME: "+ outpath +" ***");
		}
    }	
	
	/**
	 * Close the detailed output file if possible.  Otherwise we keep the System.out default.
	 */
    protected void closeOutPathStream(){
		if ((closeout)&&(out != null)){
			closeout = false;
			try{
				out.flush();
				out.close();
			}catch(Exception x){;}
			out = System.out;
		}
    }	
	
	/**
	 * Open the map output file if possible.  Otherwise we keep the System.out default.
	 */
    protected void openMapPathStream(){
    	if (mappath == null) return;
		File afile = new CaseInsensitiveFile(mappath).toFile();
		try{ 
			map = new FileOutputStream(afile, appendMap.isSelected());
			closemap = true;
		}
		// SecurityExceptions or FileNotFoundException, etc..
		catch(SecurityException sx){ map = System.out;}
		catch(FileNotFoundException nf){
			map = System.out;
			System.err.println("*** ERROR IN APP MAP OUTPUT FILENAME: "+ mappath +" ***");
		}
    }	
	
	/**
	 * Close the Map output file if possible.  Otherwise we keep the System.out default.
	 */
    protected void closeMapPathStream(){
		if ((closemap)&&(map != null)){
			closemap = false;
			try{
				map.flush();
				map.close();
			}catch(Exception x){;}
			map = System.out;
		}
    }	
	
	/**
	 * Open/Process the initialization file if it is found and appears to be valid.
	 */
	protected void openINIReadWrite(){
		if (inipath==null) return;
		File afile = new CaseInsensitiveFile(inipath).toFile();
		if ((! afile.exists())||(! afile.isFile())) return;
		inifile = new INIFileReadWrite(afile, 0);		
	}

	/**
	 * Retrieve an item out of the initialization file if such a file was identified.
	 */
    protected String getINIValue(String section, String item){
    	if (inifile == null) return null;
    	String value = inifile.getAppMapItem(section, item);
    	if (value==null) value = driver.getConfigureInterface().getNamedValue(section, item);
    	return value;
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
			theMenuName = theObjectName + "Menu";
			menuName.setText(theMenuName);
		}
		else if (comp.equals(menuName.getDocument())){
			theMenuName = menuName.getText();
		}
		else if (comp.equals(objectDesc.getDocument())){
			theObjectDesc = objectDesc.getText();
		}
		else if (comp.equals(outputDir.getDocument())){
			outpath = makeFullPrefix(outputDir.getText(), outPrefix.getText())+"Obj.txt";
			menupath = makeFullPrefix(outputDir.getText(), outPrefix.getText())+"Menu.txt";
			outputFullname.setText(outpath);
			menuFullname.setText(menupath);
			// Verify paths?
		}
		else if (comp.equals(outPrefix.getDocument())){
			outpath = makeFullPrefix(outputDir.getText(), outPrefix.getText())+"Obj.txt";
			menupath = makeFullPrefix(outputDir.getText(), outPrefix.getText())+"Menu.txt";
			outputFullname.setText(outpath);
			menuFullname.setText(menupath);
			// Verify paths?
		}
		else if (comp.equals(appMapFile.getDocument())){
			// do nothing.  Verify path?
		}
	}
	
	/** Configures/Toggles UI components based on current/stored settings. */
	protected void configureUI(){
		menuName.setEnabled(doMenu.isSelected());
		menuFullname.setEnabled(doMenu.isSelected());
		if(appendMap.isSelected()){
			addInfo.setEnabled(true);
			appMapFile.setEnabled(true);
			appMapFileShow.setEnabled(true);
			mapJPG.setEnabled(true);
		}else{
			addInfo.setSelected(false);
			mapJPG.setSelected(false);
			addInfo.setEnabled(false);
			appMapFile.setEnabled(false);
			appMapFileShow.setEnabled(false);
			mapJPG.setEnabled(false);
		}
	}
	
	/**************************************************************************
	 * Launches separate execution Processor when the RUN button is clicked.
	 * This will process all known top-level Windows.
	 **************************************************************************/
	public void actionPerformed(ActionEvent event){
		String comp = event.getActionCommand();
		
		if(comp.equalsIgnoreCase(RUN_ACTION)){
	    	if(domainname.length()>0 && server != null){
                // reset searching domains on RJ according to the client type user wants to search.
		    	// For DotNet domain, WIN maybe need to be openned at same time, this time open them separately.
		    	// User has no need to mannually modify SPC.ini setting DomainsName=, which is going to be overrided by clientType
    			server.enableDomains(domainname);
    			Log.info("Enable searching domains in configured engine: testDomains =" + domainname);
		    }
			
		    //  When output RS to a mapping file with 'Append AppMap' checked, to see if 'ignoreInvisible' is unchecked.
		    //  if ignoreInvisible is false, the recognition information to be created may be incorrect.  
		    if (appendMap.isSelected() && !ignoreInvisible ){
		        if (JOptionPane.showConfirmDialog(this, "'Ignore Invisible' is Unchecked! \n Recognition information to be created may be incorrect. \n  Continue?","Warning message",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
		            return;	
		    }
		    setInterrupt(false);
			nameMap.clear();
			status.setText(RUNNING_TEXT);
			try{
				new Thread(new RunThread()).start();
			}catch(Exception x){;}
		}else if(comp.equalsIgnoreCase(CANCEL_ACTION)){
			Log.info("SPC CANCEL initiated.");
			setInterrupt(true);
		}else if(comp.equalsIgnoreCase(SHORTEN_ACTION)){
			shortenGeneralRecognition = doShortStrings.isSelected();
		}else if(comp.equalsIgnoreCase(SHORTNAME_ACTION)){
			withNameIncludeOnlyCaption = doShortNames.isSelected();
		}else if(comp.equalsIgnoreCase(INVISIBLE_ACTION)){
			ignoreInvisible = doIgnoreInvisible.isSelected(); 
		}else if(comp.equalsIgnoreCase(OUTFILE_VIEW_ACTION)){
			try{Runtime.getRuntime().exec(outputViewer +" "+ outputFullname.getText());}
			catch(Exception x){
				System.err.println("COULD NOT LAUNCH OUTPUT FILE VIEWER!");
			}
		}else if(comp.equalsIgnoreCase(MENUFILE_VIEW_ACTION)){
			try{Runtime.getRuntime().exec(outputViewer +" "+ menuFullname.getText());}
			catch(Exception x){
				System.err.println("COULD NOT LAUNCH MENU FILE VIEWER!");
			}
		}else if(comp.equalsIgnoreCase(APPMAPFILESHOW_ACTION)){
			try{Runtime.getRuntime().exec(outputViewer +" "+ appMapFile.getText());}
			catch(Exception x){
				System.err.println("COULD NOT LAUNCH APPMAP FILE VIEWER!");
			}
		}else if(comp.equalsIgnoreCase(MAPPED_CLASS_SEARCH_ACTION)){
			mappedClassSearchMode = mappedClassSearch.isSelected();
		}else if (comp.equalsIgnoreCase(this.ADVANCEDSETTINGS_ACTION)){
			RStringStrategySettings settingDlg = new RStringStrategySettings(rstringStrategy);
			settingDlg.setLocationRelativeTo(this);
			if (settingDlg.Execute());    
			 	rstringStrategy = settingDlg.GetRStringStrategy();
		}else if(comp.equalsIgnoreCase(CHILDRENSET_ACTION)){
			Log.info("SPC doChildrenOptions...");
			doChildrenOptions settingDlg = new doChildrenOptions(m_ignoreChildInTLC);
			settingDlg.setLocationRelativeTo(this);
			if (settingDlg.Execute());    
				m_ignoreChildInTLC = settingDlg.ignoreChildInTLC();
		}else if (comp.equalsIgnoreCase(RFT_FIND_SEARCH_ACTION)) {
			rfsmSearchMode = rfsmSearch.isSelected();			
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
		
		
		mappedClassSearch = new JCheckBox("Mapped Search", mappedClassSearchMode);
		rfsmSearch = new JCheckBox("RFSM Search (Flex)", rfsmSearchMode);

		String[] data = {DriverConstant.ANDROID_CLIENT_DISPLAY,
						 DriverConstant.JAVA_CLIENT_DISPLAY,
						 DriverConstant.HTML_CLIENT_DISPLAY,
						 DriverConstant.NET_CLIENT_DISPLAY,
						 DriverConstant.RCP_CLIENT_DISPLAY,
						 DriverConstant.FLEX_CLIENT_DISPLAY,
						 DriverConstant.WIN_CLIENT_DISPLAY};
		clientType = new JList(data);
		clientType.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		String[] typesToSelect = convertStringToArray(theClientType);
		setDomainName(typesToSelect);
		clientType.setSelectedIndices(getIndicesToSelect(data, typesToSelect));
		clientType.setVisibleRowCount(VISIBLE_COUNT_IN_LIST);
		clientType.addListSelectionListener(this);
		clientTypeScrollPane = new JScrollPane(clientType);
		clientTypeScrollPane.setPreferredSize(new Dimension(w2,h*VISIBLE_COUNT_IN_LIST));
		
		domainsLabel = new JLabel("Enabled Domains: ");
		domainsLabel.setPreferredSize(new Dimension(w2,h));
		
		windowRecLabel = new JLabel("Window Recognition Method:");
		windowRecLabel.setPreferredSize(new Dimension(w,h));
		windowRec = new JTextField();
		windowRec.setText(theWindowRec);
		windowRec.setPreferredSize(new Dimension(w,h));
		windowRec.setActionCommand(WINDOWREC_ACTION);

		objectRecLabel = new JLabel("Object Recognition Method:");
		objectRecLabel.setPreferredSize(new Dimension(w2,h));

		doChildren = new JCheckBox("Process Children", theDoChildren);
		doChildren.setPreferredSize(new Dimension(w1,h));
		doChildren.setActionCommand(CHILDREN_ACTION);

		doChildrenSet = new JButton("Options for Process children");
		doChildrenSet.setPreferredSize(new Dimension(20,h));
		doChildrenSet.setActionCommand(CHILDRENSET_ACTION);
		doChildrenSet.setToolTipText("Options for Process Children");
		doChildrenSet.addActionListener(this);
		
		objectRec = new JTextField();
		objectRec.setText(theObjectRec);
		objectRec.setPreferredSize(new Dimension(w,h));
		objectRec.setActionCommand(OBJECTREC_ACTION);
						
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
		
		doIgnoreInvisible = new JCheckBox("Ignore Invisible",ignoreInvisible);
		doIgnoreInvisible.setActionCommand(INVISIBLE_ACTION);
		doIgnoreInvisible.setPreferredSize(new Dimension(w1,h));
		doIgnoreInvisible.addActionListener(this);

		doMenu = new JCheckBox("Process Menu", theDoMenu);
		doMenu.setPreferredSize(new Dimension(w2,h));
		doMenu.setActionCommand(MENU_ACTION);
		doMenu.setEnabled(true);
		doMenu.addActionListener(this);

		doShortNames = new JCheckBox("Short Names", withNameIncludeOnlyCaption);
		doShortNames.setActionCommand(SHORTNAME_ACTION);
		doShortNames.setPreferredSize(new Dimension(w1,h));
		doShortNames.addActionListener(this);
		
		mappedClassSearch.setToolTipText("Mapped Class Search Mode speeding up RJ engine search.");
		mappedClassSearch.setActionCommand(MAPPED_CLASS_SEARCH_ACTION);
		mappedClassSearch.setPreferredSize(new Dimension(w1,h));
		mappedClassSearch.addActionListener(this);	
		
		
		rfsmSearch.setToolTipText("RFT Find Search Mode speeding up RFT engine search.");
		rfsmSearch.setActionCommand(RFT_FIND_SEARCH_ACTION);
		rfsmSearch.setPreferredSize(new Dimension(w1,h));
		rfsmSearch.addActionListener(this);	
		
		menuName = new JTextField();
		menuName.setText(theMenuName);
		menuName.setPreferredSize(new Dimension(w2,h));
		menuName.setActionCommand(MENUNAME_ACTION);
		menuName.setName(MENUNAME_ACTION);		
		menuName.setEnabled(false);
		menuName.getDocument().addDocumentListener(this);

		doProperties = new JCheckBox("Process Properties", theDoProperties);
		doProperties.setPreferredSize(new Dimension(w1,h));
		doProperties.setActionCommand(PROPERTIES_ACTION);
		
		advancedSettings = new JButton("Advanced Settings...");
		advancedSettings.setBorder(BorderFactory.createRaisedBevelBorder());
		advancedSettings.setPreferredSize(new Dimension(160, h+4));
		advancedSettings.setActionCommand(ADVANCEDSETTINGS_ACTION);
		advancedSettings.addActionListener(this);

		gbct.gridwidth = 2;
		gbl.setConstraints(domainsLabel, gbct);
		north.add(domainsLabel);
		
		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(mappedClassSearch, gbc);
		north.add(mappedClassSearch);
	
		gbct.gridwidth = 2;
		gbl.setConstraints(clientTypeScrollPane, gbct);
		north.add(clientTypeScrollPane);
		
		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(rfsmSearch, gbcb);
		north.add(rfsmSearch);
		
		
		gbct.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(windowRecLabel, gbct);
		north.add(windowRecLabel);		
		
		gbcb.gridwidth = gbcb.REMAINDER;
		gbl.setConstraints(windowRec, gbcb);
		north.add(windowRec);

		gbct.gridwidth = 3;
		gbl.setConstraints(objectRecLabel, gbct);
		north.add(objectRecLabel);
		
		gbc.gridwidth = gbc.RELATIVE;
		gbl.setConstraints(doChildren, gbc);
		north.add(doChildren);
		
		gbct.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(doChildrenSet, gbct);
		north.add(doChildrenSet);
				
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
		gbl.setConstraints(doShortNames, gbc);
		north.add(doShortNames);		

		gbct.gridwidth = 2;
		gbl.setConstraints(doMenu, gbct);
		north.add(doMenu);

		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(doIgnoreInvisible, gbc);
		north.add(doIgnoreInvisible);

		gbcb.gridwidth = 2;
		gbl.setConstraints(menuName, gbcb);
		north.add(menuName);

		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(doProperties, gbc);
		north.add(doProperties);		

		// Add button "Advanced Settings..." for setting the property-finding priorities.
		gbc.gridwidth = gbc.REMAINDER;
		gbl.setConstraints(advancedSettings, gbc);
		north.add(advancedSettings);
		
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
		
		//outputDir.addActionListener(this);
		//outputDir.addPropertyChangeListener(this);
		//outputDir.addInputMethodListener(this);
		//outputDir.addKeyListener(this);
		
		outputDir.getDocument().addDocumentListener(this);

		outputNameLabel = new JLabel("Output Filename Prefix:");		
		outputNameLabel.setPreferredSize(new Dimension(w,h));
		outPrefix = new JTextField();
		outPrefix.setText(theOutPrefix);
		outPrefix.setPreferredSize(new Dimension(w,h));
		outPrefix.setActionCommand(OUTPREFIX_ACTION);
		outPrefix.setName(OUTPREFIX_ACTION);
		
		//outPrefix.addActionListener(this);
		//outPrefix.addPropertyChangeListener(this);
		//outPrefix.addInputMethodListener(this);
		//outPrefix.addKeyListener(this);
		
		outPrefix.getDocument().addDocumentListener(this);
		
		//outputFullname = new JLabel();
		outputFullname = new JButton();
		outputFullname.setPreferredSize(new Dimension(w,h));
		if ((outpath == null)||(outpath.length() < 1)) outpath = makeFullPrefix(theOutDir, theOutPrefix)+"Obj.txt";
		outputFullname.setText(outpath);
		outputFullname.setToolTipText("View Object Output File");
		outputFullname.setActionCommand(OUTFILE_VIEW_ACTION);
		outputFullname.addActionListener(this);
		menuFullname = new JButton();
		menuFullname.setPreferredSize(new Dimension(w,h));
		if ((menupath == null)||(menupath.length() < 1)) menupath = makeFullPrefix(theOutDir, theMenuName) + "Menu.txt";
		menuFullname.setText(menupath);
		menuFullname.setToolTipText("View Menu Output File");
		menuFullname.setActionCommand(MENUFILE_VIEW_ACTION);
		menuFullname.addActionListener(this);
				
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
		gbl.setConstraints(outputNameLabel, gbct);
		center.add(outputNameLabel);
		gbl.setConstraints(outPrefix, gbcb);
		center.add(outPrefix);
		gbl.setConstraints(outputFullname, gbct);
		center.add(outputFullname);
		gbl.setConstraints(menuFullname, gbcb);
		center.add(menuFullname);
		
		
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
		run.setEnabled(false);

		cancel = new JButton("Cancel");
		cancel.setActionCommand(CANCEL_ACTION);
		cancel.setPreferredSize(new Dimension(w1-10,h+4));
		cancel.setBorder(BorderFactory.createRaisedBevelBorder());
		cancel.addActionListener(this);
		
		/*
		open = new JButton("Open...");
		open.setActionCommand(OPEN_ACTION);
		open.setPreferredSize(new Dimension(w4-10,h+4));
		open.setBorder(BorderFactory.createRaisedBevelBorder());
		open.addActionListener(this);
		*/
		
		help = new JButton("Help");
		help.setActionCommand(HELP_ACTION);
		help.setPreferredSize(new Dimension(w1-10,h+4));
		help.setBorder(BorderFactory.createRaisedBevelBorder());
		//help.addActionListener(this);
		
		status = new JLabel("Status: Awaiting Engine Ready...");
		//status.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
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
	
	/**
	 * shutdown finalization
	 */	
	protected void finalize() throws Throwable {
		Log.info("SPC FINALIZE INVOKED.");
		try{
			try{status.setText("Shutting down...");}catch(Exception x1){}
			if (inifile != null) {
				Log.info("SPC writing initialization information to "+ inifile.getFullpath());
				inifile.setAppMapItem(null, OUTPUT_VIEWER_KEY, outputViewer);
				inifile.setAppMapItem(null, FULLPATH_SEARCH_ACTION, String.valueOf(fullpathSearchMode));
				inifile.setAppMapItem(null, MAPPED_CLASS_SEARCH_ACTION, String.valueOf(mappedClassSearchMode));
				inifile.setAppMapItem(null, RFT_FIND_SEARCH_ACTION, String.valueOf(rfsmSearchMode));
				String path = appMapFile.getText();
				inifile.setAppMapItem(null, APPMAPFILE_ACTION, path);
				path = outputFullname.getText();
				inifile.setAppMapItem(null, OUTFILE_ACTION, path);
				path = menuFullname.getText();
				inifile.setAppMapItem(null, MENUFILE_ACTION, path);
				path = convertArrayToString(clientType.getSelectedValues());
				inifile.setAppMapItem(null, CLIENTTYPE_ACTION, path);
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
				inifile.setAppMapItem(null, INVISIBLE_ACTION, String.valueOf(doIgnoreInvisible.isSelected()));
				inifile.setAppMapItem(null, SHORTNAME_ACTION, String.valueOf(doShortNames.isSelected()));
				inifile.setAppMapItem(null, PROPERTIES_ACTION, String.valueOf(doProperties.isSelected()));
				inifile.setAppMapItem(null, MENU_ACTION, String.valueOf(doMenu.isSelected()));
				
				inifile.setAppMapItem(null, IGNORE_CHILDREN, String.valueOf(m_ignoreChildInTLC));
				inifile.setAppMapItem(null, RStringStrategy.AUTO_QUALIFIER, String.valueOf(rstringStrategy.isAutoQualifier()));
				inifile.setAppMapItem(null, RStringStrategy.QUALIFIER_USE_ID, String.valueOf(rstringStrategy.getIfUseId()));
				inifile.setAppMapItem(null, RStringStrategy.QUALIFIER_USE_NAME, String.valueOf(rstringStrategy.getIfAccessibleNamePriority()));
				inifile.setAppMapItem(null, RStringStrategy.USE_GENERIC_TYPE, String.valueOf(rstringStrategy.isUseGenricType()));
				inifile.setAppMapItem(null, RStringStrategy.USE_CLASS_NOT_SUBTYPE, String.valueOf(rstringStrategy.isUseClassNotSubType()));
				
				path = menuName.getText();
				inifile.setAppMapItem(null, MENUNAME_ACTION, path);
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
				inifile.close();
				Log.info("SPC initialization information complete to "+ inifile.getFullpath());
			}
		}catch(Exception x){
			Log.info("SPC EXCEPTION writing initialization information to "+ inifile.getFullpath(),x);
		}
		if (closeout) closeOutPathStream();
		if (closemap) closeMapPathStream();
		
		if(mydialog != null){
			try{ mydialog.hide();}catch(Exception x){}
			try{ mydialog.dispose();}catch(Exception x){}
			mydialog = null;
		}
		if(rsdialog != null){
		    try{ rsdialog.hide();}catch(Exception x){}
		    try{ rsdialog.dispose();}catch(Exception x){}
		    rsdialog = null;
		}
		try{ if(server != null) server.clearHighlightedDialog();}catch(Exception x){}
		server = null;
		try{driver.shutdownDriver();}catch(Exception x){
			Log.info("SPC FINALIZE Driver Shutdown Exception:",x);
		}
		inifile = null;
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
	 * 'Runnable' Shutdown Hook registered with JVM to do finalization on exit.
	 * Users MUST NOT call this as it will execute the Object's finalize() method.
	 */
	public void run(){
		try{finalize();}
		catch(Throwable t){;}
	}

	protected void enableClickToRun(){
		Log.info("Validating Ready for ClickToRun...");
		ListIterator ei = driver.getEngines();
		if(ei.hasNext()){ //there is at least 1 engine defined
			EngineInterface eng = (EngineInterface) ei.next();
			String engName = eng.getEngineName();
			Log.info("Seeking ready state of '"+ engName +"'");
			Collection elist = null;
			Iterator ni = null;
			boolean isReady = false;
			int looper = 0;
			int maxloops = 120; // 2 minutes ?
			try{ 
				STAFHelper staf = SingletonSTAFHelper.getInitializedHelper(STAF_PROCESS_CONTAINER_PROCESS);			
				while(!isReady && looper++ < maxloops){
					try { 
						elist = staf.getRunningEngineNames();					
						isReady = elist.contains(engName);
					} 
					catch (Exception e) {
						Log.debug("Ignoring "+e.getClass().getSimpleName()+": "+ e.getMessage());
					}
					if(!isReady) try{Thread.sleep(1000);}catch(Exception x){}
				}
			}catch(Exception x){
				Log.info(x.getMessage()+" detecting engine ready state...");
			}
			if(isReady){
				Log.info("Detected engine ready state...");
			}else{
				Log.info("Ignoring engine ready TIMEOUT seeking '"+ engName +"'");
				String engRunning = elist.size()+" ready engines detected: ";
				String[] engs = (String[])elist.toArray(new String[0]);
				for(String en:engs) engRunning +=", "+ en;
				Log.info(engRunning);
			}
		}else{
			Log.info("Blindly enabling Click To Run...");
		}
		run.setEnabled(true);
		status.setText("Ready");
	}
	
	/**************************************************************************
	 * main 
	 *************************************************************************/
	public static void main(String[] args) {		
		STAFProcessContainer.args = args;
		STAFProcessContainer pc = new STAFProcessContainer();	
		//only continue if we know STAF is running and we have a Driver
		if(pc.driver != null){
			pc.show();
			pc.enableClickToRun();
			while(!pc.finalized){
				try{Thread.sleep(1000);}catch(Exception x){}
			}
			try{Thread.sleep(5000);}catch(Exception x){}
		}
		System.exit(0);
	}
	
	public class ShutdownListener extends WindowAdapter {
		public void windowClosing(WindowEvent event){
			Log.info("SPC WINDOW CLOSING EVENT RECEIVED...");
			shutdown = true;
			setInterrupt(true);
			if (processor == null){	setStopped(true); }			
		}
	}
	
	public class RunThread implements Runnable{
    
		public void run(){
			String FPSM = fullpathSearchMode ? GuiObjectVector.FULLPATH_SEARCH_MODE_PREFIX : "";
			String MCSM = mappedClassSearchMode ? GuiObjectVector.MAPPEDCLASS_SEARCH_MODE_PREFIX: "";
			String SM = FPSM+MCSM;
			
			
			//Delay before running, so that we have time to RightMouseClick to show Popup Menu
			try{
				String dealy = delayToRunTextField.getText();
				status.setText(DELAYING_TEXT);
				int delaySeconds = Integer.parseInt(dealy);
				Thread.sleep(delaySeconds*1000);
			}catch(Exception e){	
			}
			
			status.setText(RUNNING_TEXT);
			
			Object parent = server.getMatchingParentObject( SM + windowRec.getText());
			STAFProcessContainerResult result = null;
			if (parent == null) {
				statInfo("SPC: Window not found:"+ windowRec.getText());
				return;
			}else{
				Boolean popup = server.isTopLevelPopupContainer(parent);
				//If the top window is found, make it active, but NOT popup menu.
				//This is useful when the check box 'Map JPG' is checked, as the snapshot will be captured.
				if(!popup){
					server.setActiveWindow(parent);
				}
				statInfo("SPC: Window found:"+ parent);
				statInfo("SPC: Window Class:"+ server.getClassName(parent));
				statInfo("SPC: Window Caption:"+ server.getCaption(parent));
			}

			// stop if the window is the target
			if(objectRec.getText().endsWith(windowRec.getText())){
				processor = new Processor(new Object[]{parent});
				processor.run();
				statInfo("Finished");
				processor = null;
				return;
			}
			
			// continue if a child is sought
			Object[] windows = server.getMatchingChildObjects(parent, SM + objectRec.getText());
			if ((windows == null)||(windows.length ==0)||(windows[0]==null)) {
				statInfo("SPC: Object not found:"+ objectRec.getText());
				return;
			}else{
				try{
					statInfo("SPC: "+ windows.length +" matches found.");
					result = (STAFProcessContainerResult) windows[0];
					statInfo("SPC: Object found:"+ result);
					statInfo("SPC: Object Class:"+ server.getClassName(result));
				}
				catch(ClassCastException cce){
					statInfo("SPC: Child Object reference type invalid:"+ windows[0].getClass().getName());
					return;
				}
			}
			processor = new Processor(new Object[]{result});
			processor.run();					
			statInfo("Finished");
			processor = null;
		}
	}
	
	/**
	 * Process a component.  Will process properties and children based on settings.
	 * Will instantiate additional Processors for child components if we are 
	 * processing children.
	 */
	public class Processor implements Runnable{
		
		private static final String INDENT = "   ";
		private Object container = null;
		private int level = 0;
		private String in = "";
		private boolean ioxreported = false;
		private boolean ioxtreereported = false;
		private PCTree parent = null;
		private PCTree pctree = null;
		private String parentText = null;
		private Object[] topwins = null;
		private boolean hierarchyDialogClosed = false;
		
		public synchronized boolean isHierarchyDialogClosed() {
			return hierarchyDialogClosed;
		}

		public synchronized void setHierarchyDialogClosed(boolean hierarchyDialogClosed) {
			this.hierarchyDialogClosed = hierarchyDialogClosed;
		}

		/**
		 * Constructor with default use of System.out as OutputStream
		 * @param container STAFProcessContainerResult
		 * @param level of object hierarchy
		 */
		public Processor(Object container, int level){
			this.container = container;
			this.level = level;
			makeIndent();
		}
		
		/**
		 * Constructor provided a predefined top level windows array.
		 * @param toplevel STAFProcessContainerResult[]
		 */
		public Processor(Object[] toplevel){
			topwins = toplevel;
		}
		
		/**
		 * The Thread.start entry point.
		 * Always assumes we are processing an Object[] of topwins
		 */
		public void run(){
			run.setEnabled(false);
			run.setText(RUNNING_TEXT);
			
			mappath = appMapFile.getText();
			if (appendMap.isSelected()) openMapPathStream();
			outpath = outputFullname.getText();
			openOutPathStream();
			try{
				for(int i=0;i <topwins.length;i++){
					this.container = topwins[i];
					this.level = 0;
					in="";
					parent = null;
					pctree = null;
					processParent();
					if (isInterrupted()) throw new InterruptedException("SPC Process cancelled or application closed by user.");
				}
			}catch(Exception x){
				Log.debug("SPC Exception aborted run():",x);
				status.setText("Processor run aborted.");
			}
			closeMapPathStream();
			closeOutPathStream();
			if(isInterrupted()){
				setStopped(true);
			}			
			run.setText(READY_TEXT);
			run.setEnabled(true);
		}

		/**
		 * Increase our output indention based on the depth (level) of the hierarchy 
		 * being processed.
		 */		
		protected void makeIndent(){
			if (level > 0) {
				StringBuffer inb = new StringBuffer(INDENT);
				for(int i = 1;i<level;i++) inb.append(INDENT);
				in = inb.toString();
			}else{
				in = "";
			}			
		}
		
		/** Accessor for hierarchy information */
		public void setPCTree(PCTree atree){ parent = atree; }
		/** Accessor for hierarchy information */
		public PCTree getPCTree(){ return pctree; }

		/** Accessor for parentText */
		public void setParentText(String text){ parentText = text; }

		/**
		 * Call to use superclass names from the engine in attempt to getMappedClassTypes.
		 * @param ref
		 * @param mapGeneric
		 * @return the mapped class type of the first superclass found to be mapped.
		 */
		String tryEngineSuperClassnames(Object ref, boolean mapGeneric){
			String compType = null;
    		String[] supers = server.getSuperClassNames(ref);
    		String classname = null;
    		// index=0 is the ref classname, so we don't use index 0;
    		for(int i=1;i<supers.length && compType==null;i++){
    			classname = supers[i];
    			compType = classdata.getMappedClassType(classname, ref, false, mapGeneric);
    		}
    		return compType;
		}
		
		/** 
		 * The primary entry point for processing the initial object.
		 */
		public void processParent(){
	
		    write("");
		    write("=============================================");

		    String classname = server.getClassName(container);
		    String domainName = server.getDomainName(container);
		    write("ClassName = " + classname);			
		    String alttype = null;
		    String altSuperType = null;
		    boolean mapRecursive = !rstringStrategy.isUseClassNotSubType();
		    boolean mapGeneric = rstringStrategy.isUseGenricType();
		    Log.info("SPC.processParent seeking Type recursively: "+ mapRecursive +", using generic Types: "+ mapGeneric);
		    if (classname != null) 
		    	alttype = classdata.getMappedClassType(classname, container, mapRecursive, mapGeneric);		    
		    if(alttype==null && mapRecursive)
	    		alttype = tryEngineSuperClassnames(container, mapGeneric);
		    	
	        //TODO: For Android Domain: by EngineCommand???
		    //TODO: This may be true for ALL domains. Try to have the Engine tell us the Type it would accept 
//	        if(alttype==null){
//	        	alttype = server.getMappedClassType(classname, container);
//	        }
		    if(alttype!=null) altSuperType = alttype;
		    if(alttype==null && !mapRecursive){
		    	altSuperType = classdata.getMappedClassType(classname, container, true, mapGeneric);
		    }
	    	if(altSuperType==null) 
	    		altSuperType = tryEngineSuperClassnames(container, mapGeneric);
	        
	        write("Class Type: "+ altSuperType);
            boolean visible = server.isShowing(container);
	        write("Visible   : "+ visible);
			write("ChildCount= " + server.getChildCount(container));

			// Array of STAFProcessContainerResults or zero-length Object array
			Object[] children;
			status.setText("Type: "+ altSuperType+", Class: "+classname);
			
			// CANAGL do not seek children in comboboxes
		    if(altSuperType == null) {
		      children = server.getChildren(container);
		    }else if( m_ignoreChildInTLC &&    // for some useful components embeded in TABLE/LISTBOX...
		    		 (altSuperType.toUpperCase().indexOf("COMBOBOX")>-1 ||
		    		  altSuperType.toUpperCase().indexOf("SPINNER")>-1 ||
		    		  altSuperType.toUpperCase().indexOf("LISTBOX")>-1 ||
		    		  altSuperType.toUpperCase().indexOf("LISTVIEW")>-1 ||
		    		  altSuperType.toUpperCase().indexOf("TABLE")>-1 ||
		    		  altSuperType.toUpperCase().indexOf("GRIDVIEW")>-1)
		    		  ){
		      children = new Object[0];
		    }else{
		    	children = server.getChildren(container);
		    }
		    
			try{
				if(doProperties.isSelected()) writeProperties(container);
			}catch(InterruptedException x){
				Log.info(x.getMessage());
				Log.info("SPC Exception aborted run():",x);
				status.setText("Processor run aborted.");
				return;
			}
			
	        if (ignoreInvisible && !visible) {
	        	boolean isContainerType = false;
	        	
	        	isContainerType = GuiClassData.isContainerType(altSuperType);
	        	//TODO For Android Domain: get isContainerType by EngineCommand???
//	        	isContainerType = server.isContainerType(alttype);
	        	
	        	if (parent == null) { return; } // return null for pctree originally
	        	else {
	        		// check to see if parent is a tab, if so, then we still process
	        		String ptype = parent.getType();
	        		if ((ptype == null) || (!(ptype.equalsIgnoreCase("TabControl")))) {
	        			if(DriverConstant.ANDROID_CLIENT_TEXT.equalsIgnoreCase(domainName)) return;
	        			if (isContainerType) return; // return null for pctree originally
	        		}
	        	}
	        }
		
		    pctree = new PCTree();
		    if (container instanceof STAFProcessContainerResult)
		    	pctree.setUserObject(((STAFProcessContainerResult)container).get_statusInfo());  //hold the component's key, which is sent back from RJ-engine.
		    pctree.setComponentVisible(visible);
		    pctree.setFullPathSearchMode(fullpathSearchMode);       //FPSM
		    pctree.setMappedClassSearchMode(mappedClassSearchMode); //MCSM
		    pctree.setRfsmSearchMode(rfsmSearchMode);               //RFSM
		    pctree.setWithNameIncludeOnlyCaption(withNameIncludeOnlyCaption);
		    pctree.setWithCommentsAndBlankLines(withCommentsAndBlankLines);		    
		    pctree.setShortenGeneralRecognition(shortenGeneralRecognition);		    
		    pctree.setLevel(new Integer(level));
		    pctree.setDomainName(domainName);
		    if(addInfo.isEnabled()&& addInfo.isSelected())
		    	pctree.setAppendCompInfo(true);
		    if((parent==null) && (objectRec.getText().endsWith(windowRec.getText())))
		    	pctree.setDefaultRecognition(windowRec.getText());
		    
			String text = genRecogString(pctree, container, parentText);
			
			//TODO fullChildrenPath is never used, why we keep it??? comment it out.
//			boolean fullChildrenPath = true;
//			String uIClassID = getObjectProperty(container, ".role");
//			if (uIClassID != null &&
//					uIClassID.length()>=4 && 
//					uIClassID.substring(0, 4).equalsIgnoreCase("Menu")) {
//				
//				fullChildrenPath = false;
//			}
		    
		    write("");
		    write("RecInfo: "+ ((text==null)? "Index":text));
		    write("");
		    write((children == null ? INDENT +"0" : INDENT + Integer.toString(children.length)) +
		         ((children.length == 1) ? " Child ":" Children ") + "for this Object:");
		
			try{
				if(doChildren.isSelected()){
				    PCTree firstChild = processChildren(children, text, pctree);
				    pctree.setFirstChild(firstChild);
				    if (firstChild == null) pctree.setChildCount(new Integer(0));
				    else pctree.setChildCount(firstChild.getSiblingCount());
				}
			    write("");
			    write("Finished with Object");
			    write("========================");
			    
			    if((level==0)&&(pctree != null)) {
			    	pctree.setupIndexMap();

					String mapJpgPathPrefix = "";
					String mapFileName = "";
					int sepIndex = mappath.lastIndexOf(File.separator);
					if (sepIndex > 0) {
						mapJpgPathPrefix = mappath.substring(0, sepIndex + 1);
						mapFileName = mappath.substring(sepIndex+1);
					}else{
						mapFileName = mappath;
					}
					Log.debug("SPC->ProcessParent(): Save path="+mapJpgPathPrefix+" ; mapName="+mapFileName+" ; windowName=" + windowName.getText());
					
					//Capture the snapshot of the window, and output it as a jpg image
					//Create a html file containing that jpg image, and create html_map for each
					//component within the window
					// JSAFSBefore capture the snapshot, we should keep the top window visible.
				    if(mapJPG.isSelected()){
						outputJPGMap(mapJpgPathPrefix, windowName.getText());
				    }
					
				    String topWindowKey = (String) pctree.getUserObject();
				    if(doMenu.isSelected()){
				    	long lhWnd = server.getTopWindowHandle(topWindowKey);
				    	Log.info("SPC Process Menu: hWnd="+lhWnd+" ; path="+menupath+" ; First Line Content="+theMenuName);
				    	MenuUtilities.MUOutputMenuStructure(lhWnd, menupath, true, true, true, theMenuName);
				    }
				    
			    	showTree(pctree); // show the hierarchy of the tree

			    	writeTree(pctree);
			    }
			}catch(InterruptedException x){
				Log.info(x.getMessage());
				Log.info("SPC Exception aborted run():",x);
				status.setText("Processor run aborted.");
			}
		    
		}

		/**
		 * Output the html file and a jpg file of the found component
		 * The html file will contain the jpg file, and map the children of this component
		 * on that jpg picture, when user hover mouse on each part of this jpg picture, the
		 * related RS will be shown as a tooltip.
		 * 
		 * @param mapJpgPathPrefix
		 * @param componentName
		 */
		private void outputJPGMap(String mapJpgPathPrefix, String componentName){
			if(mapJpgPathPrefix==null||"".equals(mapJpgPathPrefix)){
				mapJpgPathPrefix = System.getProperty("user.dir");
			}
			if(componentName==null||"".equals(componentName)){
				componentName = "SAFS_SPC_JPG_MAP";
			}
			
			Log.info("Save path: "+mapJpgPathPrefix+" ; fileNamePrefix="+componentName);
			String mapJpgFileName = mapJpgPathPrefix + componentName+".jpg";
			String mapJpgHtmlName = mapJpgPathPrefix + componentName+".htm";
			String jpgMapName = componentName+" Map";

			if(pctree!=null){
				//1. Capture the component's snapshot and save to a jpg file
				Object key = pctree.getUserObject();
				Rectangle rect = server.getComponentRectangle(key);
				if(rect!=null){
					try {
						File jpgFile = new CaseInsensitiveFile(mapJpgFileName).toFile();
						ImageUtils.saveImageToFile(ImageUtils.captureScreenArea(rect),jpgFile);
					} catch (Exception e) {
						Log.debug("Exception: "+e.getMessage()+" , can not save component to a jpg file.");
						return;
					}
				}else{
					Log.debug("Can not get the rectangle of the component, so can not save it as a jpg file.");
					return;
				}
				
				//2. Save an html file containing the jpg file above, children of the component will be mapped on
				//this jpg file, user can hover mouse on the html image to get the RS of related component
				//A list will be used to contain the html contents and will be saved to a html file
				List<String> htmlContents = new ArrayList<String>();
				htmlContents.add("<html>");
				htmlContents.add("<head><title>"+jpgMapName+" </title></head>");
				htmlContents.add("<body><a name=\"top\"/>");
				htmlContents.add("<h2>"+jpgMapName+" </h2>");
				htmlContents.add("<p>");
				htmlContents.add("Last Updated:");
				htmlContents.add("<script language=\"JavaScript\">document.write(document.lastModified)</script>");
				htmlContents.add("<p>");
				htmlContents.add("<small>");
				htmlContents.add("<b>(mouse over components for information)</b><br>");
				htmlContents.add("<img src=\""+mapJpgFileName+"\" alt=\""+jpgMapName+"\" usemap=\"#map\"/>");
				htmlContents.add("<p>");
				htmlContents.add("<map name=\"map\">");
				//Add "html image map string" of each children of this component to a list
				List<String> children = new ArrayList<String>();
				generateHtmlMapString(children,pctree,rect);
				//Reverse the result list and add to list htmlContents, that is, put the leaves firstly, then 
				//the upper level nodes, last the root.
				//When we map some rectangles on an image of html page, if these rectangles have crossed area
				//the first mapped string will be shown on that area.
				for(int i=children.size();i>0;i--){
					htmlContents.add(children.get(i-1));
				}
				htmlContents.add("</map>");
				htmlContents.add("</body></html>");
				
				try {
					FileUtilities.writeCollectionToFile(FileUtilities.getUTF8BufferedFileWriter(mapJpgHtmlName), htmlContents);
				} catch (Exception e) {
					Log.debug("SPC Processor-->outputJPGMap(): "+e.getMessage());
				}
			}
		}
		
		/**
		 * Use "Breadth first search" go through the pctree and generate the "html image map string" for
		 * each node, then put these strings in a list.
		 * The generated "html image map string" is something like:
		 * <area alt='cmdCancel:Type=PushButton;Name=cmdCancel' nohref coords='174,158,251,185' shape='rect'/>
		 * 
		 * @param list		The list contining the result. "html image map string" for each node of tree.
		 * @param pctree	The tree containing node to be processed.
		 * @param rootRect	The rectangle representing the position on screen of the root of the whole tree.
		 * 					It is used to calculate the position relative to it for its children. 
		 */
		private void generateHtmlMapString(List<String> list, final PCTree pctree, Rectangle rootRect){
			if(list==null || pctree==null){
				Log.debug("SPC Processor-->generateHtmlForComponents(): list or pctree is null.");
				return;
			}
			
			StringBuffer sb = new StringBuffer();
			if(pctree.isComponentVisible()){
				//Get the component's position on screen, and calculate its positon relative to root component
				Object key = pctree.getUserObject();
				Rectangle rect = server.getComponentRectangle(key);
				//Server side may not return a rectangle for some components, just ignore these components
				if(rect!=null){
					rect.setLocation(rect.x-rootRect.x,rect.y-rootRect.y);
					//Generate the html string
					sb.append("<area alt='");
					//For Java MenuItem, only the "Path=File" is returned as the RS by pctree, but it can not
					//be used by SAFSDriver to get component, so its full path RS will be used as the tooltip
					//For other components, the full path RS will be used if the 'short string' is NOT selected.
					if(pctree.getType()!=null && pctree.getType().indexOf("Menu")>0){
						sb.append(pctree.getName()+":"+pctree.getComponentRecogString(true));
					}else{
						sb.append(pctree.getName()+":"+pctree.getComponentRecogString(!doShortStrings.isSelected()));
					}
					sb.append("' nohref coords='");
					//coords=leftUpX,leftUpY,rightLowX,rightLowY
					sb.append(rect.x+","+rect.y+","+(rect.x+rect.width)+","+(rect.y+rect.height));
					sb.append("' shape='rect'/>");
					list.add(sb.toString());
				}else{
					Log.debug("SPC Processor-->generateHtmlForComponents(): the rectangle is null for component "+pctree.getName());
				}
			}
			if(pctree.getNextSibling()!=null) generateHtmlMapString(list,(PCTree)pctree.getNextSibling(),rootRect);
			if(pctree.getFirstChild()!=null) generateHtmlMapString(list,(PCTree)pctree.getFirstChild(),rootRect);
		}

		/** 
		 * Process the children of a parent Processor, 
		 * children can be null because this method first checks for that.
		 * @param   children, Object[] generally retrieved from the processed parent.
		 * @param   text, String the text identifying the parent for setParentText
		 * @param   parent, PCTree the current state of stored hierarchy info
		 * @return  firstpctree (the first PCTree generated for the children)
		 **/
		PCTree processChildren(Object[] children, String text, PCTree parent) throws InterruptedException {
		    PCTree firstpctree = null;
		    if (children != null) {
		        PCTree lastpctree = null;
		        Map compMap = new HashMap();
		        int j=1;
		        for (int i = 0; i < children.length; i++){
		        	if(isInterrupted()) throw new InterruptedException("Process cancelled or Window closed by user.");
		        	Processor proc = new Processor(children[i], level+1);
		        	proc.setParentText(text);
		        	proc.setPCTree(parent);
		        	proc.processParent();
		            PCTree next = proc.getPCTree();
		            if (next != null) {
		                if (lastpctree!=null) lastpctree.setNextSibling(next);
		                else firstpctree = next;
		                lastpctree = next;
		                next.setLevel(new Integer(level+1));
		                next.setSiblingCount(new Integer(children.length));
		                next.setParent(parent);
		                next.setSiblingIndex(j);
		                next.setCompMap(compMap);
		                ArrayList list = (ArrayList)compMap.get(next.getType());
		                if (list==null) {
		                    list = new ArrayList();
		                    compMap.put(next.getType(), list);
		                }
		                list.add(next);
		                j++;
		            }
		        }
		    }
		    return firstpctree;
		} 

		/**
		 * Return an individual object property from the application JVM.
		 * @return String value of the property or null.
		 */
		String getObjectProperty(Object object, String property){
			return server.getProperty(object, property);
		}
		
		String getCompTypeFromMappedType(String atype, boolean mapGeneric){
			String compType = null;
        	if(mapGeneric){
        		//alttype is a comma seperated String, for examle, "JavaPanel, Panel"
        		//alttype is got from JavaObjectMap.dat, the last type in that string
        		//is supposed to be the most generic type.
        		//or we can consider the shortest type as the most generic???
        		StringTokenizer typeTokens = new StringTokenizer(atype, ", ");
        		compType = typeTokens.nextToken();
        		while(typeTokens.hasMoreTokens()){
        			String tmpstr = typeTokens.nextToken();
        			if(tmpstr.length()<compType.length()){
        				compType = tmpstr;
        			}
        		}
        		Log.debug("SPC.genRecogString(): Use the Generic Type " +compType+" as RS. ");
        	}else{
        		compType=GuiClassData.deduceOneClassType(domainname, atype);
        		//TODO For Android Domain: by EngineCommand??? GuiClassData.deduceOneClassType is ok for now.
        		//compType = server.deduceOneClassType(domainname, alttype);
        	}
        	return compType;
		}
		
	    /** 
	     * Deduce the recognition string for the container object and store that 
	     * along with associated object information in the PCTree.
	     * @param  pctree PCTree stores the rcognition string info
	     * @param  container STAFProcessContainerResult
	     * @param  parentText String for menus, it is the path of our parent
	     * @return title/name property value if it exists, else null
	     **/
	    String genRecogString(PCTree pctree,
	                          Object container,
	                          String parentText) {
	    	String alttype = null;
	    	String altSuperType = null;
	        String classname = server.getClassName(container);
	        boolean mapRecursive = !rstringStrategy.isUseClassNotSubType();
	        boolean mapGeneric = rstringStrategy.isUseGenricType();
		    Log.info("SPC.getRecogString seeking Type recursively: "+ mapRecursive +", using generic Types: "+ mapGeneric);
	        if (!rfsmSearchMode){
	        	alttype = classdata.getMappedClassType(classname, container, mapRecursive, mapGeneric);
	        	//TODO For Android Domain: by EngineCommand???
//		        if(alttype==null && domainname.contains(DriverConstant.ANDROID_CLIENT_TEXT)){
//		        	alttype = server.getMappedClassType(classname, container);
//		        }
	        	if(alttype != null) altSuperType = alttype;
	        	if(alttype == null && !mapRecursive){
	        		// do want to retain Type= indices for superclass matches
	        		altSuperType = classdata.getMappedClassType(classname, container, true, mapGeneric);
	        	}
	        }
	        
	        pctree.setObjectClass(classname);
	        
	        if (alttype!=null){
	        	String[] types = GuiClassData.getTypesAsArray(alttype);
	        	//TODO For Android Domain: by EngineCommand??? GuiClassData.getTypesAsArray is ok for now.
	        	//types = server.getTypesAsArray(alttype);
	        	pctree.setIndex_types(types);
	        	if( alttype.equalsIgnoreCase("Window") && domainname.equalsIgnoreCase("Java")) 
	        		alttype = "JavaWindow";
	        }else if (altSuperType!=null){
	        	String[] types = GuiClassData.getTypesAsArray(altSuperType);
	        	//TODO For Android Domain: by EngineCommand??? GuiClassData.getTypesAsArray is ok for now.
	        	//types = server.getTypesAsArray(alttype);
	        	pctree.setIndex_types(types);
	        }	        
	        
	        String compType=null;
	        boolean isMenuItem = false;
	        boolean isMenuBar = false;
	        boolean isPopupMenu = false;
	        
	        if (alttype!=null) {
	        	compType = getCompTypeFromMappedType(alttype, mapGeneric);
	        	alttype = compType;
	        	Log.debug("SPC.genRecogString deduced initial comp type: "+ compType +" for Domain: "+ domainname);
	      	    // MenuItems appear as Path= info for the parent Menu.
	            // so the compType to set for the menuitem is "Menu", altType still "MenuItem"
	        	String compTypeUpper = compType.toUpperCase();
	        	if(compTypeUpper.endsWith("MENUITEM")) {
	        		alttype = "Menu";
	        		isMenuItem = true;
	        	}else if(compTypeUpper.endsWith("MENUBAR")){
	        		isMenuBar = true;
	        	}else if(compTypeUpper.endsWith("POPUPMENU")){
	        		isPopupMenu = true;
	        	}
            	pctree.setType(compType);
	            if(compType.equalsIgnoreCase("Generic")) pctree.setMyclass(classname);	            
	        } else {
	            pctree.setMyclass(classname);
	            if(altSuperType!=null){
	            	pctree.setType(getCompTypeFromMappedType(altSuperType, mapGeneric));
	            }
	            compType = classname;
	        }
	        boolean caption = false;
	        boolean textval = false;
	        
	        String text = null;
	        
	        // menuitem names don't help for menuitem Path=text, use getText() instead.
	        
	        //The name of menubar or popupmenu should not be added to the path.
	        //For java, original code has no problem because server.getName(container) will return null
	        //But for Dotnet and Flex, server.getName(container) will return value.
	        //So if type is menubar, we will not set the name to text
	        if(!isMenuItem && !isMenuBar && !isPopupMenu) {
	        	// get the name of container according to if users need accessible name be found first 
        		text = rstringStrategy.getIfAccessibleNamePriority() ? 
        			   server.getAccessibleName(container) : 
        			   server.getNonAccessibleName(container);
        		if ((text==null)||(text.length()==0))
	        		text = rstringStrategy.getIfAccessibleNamePriority() ? 
	         			   server.getNonAccessibleName(container) : 
	            		   server.getAccessibleName(container);
	            Log.info("SPC.getRecogString getName="+ text);
	        }

	        if(GuiObjectRecognition.isContainerNameIgnoredForRecognition(text)){ //handles text==null 
	           text = null;
	           Log.info("SPC.getRecogString getName result deemed INVALID.");
	        }
	        
	        // else try to get Caption= recognition where appropriate
	        if ((text==null)||(text.length()==0)){
	        	Integer _level = pctree.getLevel();	        	
	        	if(_level==null||_level.intValue()==0){
	    	    	text = server.getCaption(container);    
		        	Log.info("SPC.getRecogString getCaption="+ text);
	    	    	if ((text != null)&&(text.length()>0)) {	
	    	    		caption = true; 
	    	    	}else {
			        	Log.info("SPC.getRecogString caption deemed INVALID.");
	    	    		text = null;
	    	    	}
	        	}else{
		        	try{
		        		Log.info("SPC.getRecogString getCaption invalid at Level="+ _level.intValue());
		        	}catch(NullPointerException np){
		        		Log.info("SPC.getRecogString getCaption invalid at Unknown Level");
		        	}
	        	}
	        }    
	        // else see if we can use Text= recognition for certain comp types
	        if((text==null)||(text.length()==0)){
	        	if (isMenuItem || GuiObjectRecognition.isTextOKForRecognition(alttype)){
	        		text = server.getText(container);
		        	Log.info("SPC.getRecogString getText="+ text);
	        	    if ((text != null)&&(text.length()>0)) {	
	        	    	textval = true;
	        	    }else {
	        	    	text = null;
			        	Log.info("SPC.getRecogString Text deemed INVALID.");
	        	    }
	        	}else{
		        	Log.info("SPC.getRecogString getText invalid for MenuItems and\\or "+ alttype +" components.");
	        	}
	        }    
	        //trim and\or null out empty values
	        // non-empty text will NOT be trimmed, and is kept as it should be (with leading spaces if has). See S0551414. 
	        if ((text != null) && (text.trim().length() == 0)) {
        		Log.info("SPC.getRecogString detected INVALID trimmed text value: "+ text);
        		text = null;
        		textval = false;
        		caption = false;
	        }
	        //begin to set the recognition information    
	        String textResult = text;
	        if (text != null) {
	        	if ((alttype != null) && (alttype.toUpperCase().endsWith("MENU"))){
	    			if ( (parentText != null) && (parentText.length()>0)) {
	            		textResult = parentText + pathSep + text;
	    	        }
	    	    	Log.info("SPC.getRecogString setPath="+ textResult);
	    	        pctree.setPath(textResult);
	    	    } else if (text.length()==0) {
	    	    	Log.info("SPC.getRecogString setting classIndex instead of objectIndex = "+(alttype==null));
	    	        pctree.setClassIndex(alttype==null);
	    	    } else {
	    	    	Log.info("SPC.getRecogString text value: '"+ textResult +"' Caption="+ caption +", Text="+ textval +", Name="+ (!(caption || textval)));
	    	        pctree.setNameValue(textResult);
	    	        pctree.setCaption(caption);
	    	        pctree.setTextValue(textval);
	    	    }
	    	} else {
    	    	Log.info("SPC.getRecogString setting classIndex instead of objectIndex = "+(alttype==null));
	    	    pctree.setClassIndex(alttype==null);
	    	}
	        
	        // ID is considered independantly 
	        if (rstringStrategy.getIfUseId()) {
	        	// ID will be used if exists to generate R-Strings for the component, container.
	        	String Id = server.getID(container);
	        	if (Id == null || Id.length() == 0)
	        		pctree.setId(null);
	        	else
	        		pctree.setId(Id);
	            Log.info("SPC.getRecogString getID="+ Id + ". ID option openned!");
	        } else {
        		pctree.setId(null); // ID is set to null, will NOT be considered when generating R-String for pctree.	        	
	        	Log.info("SPC.getRecogString getID result deemed INVALID. ID option closed!");
	        }
	        
	        // CLASS INDEX for RFSM is conidered independantly
	        String classIndex = server.getClassIndex(container);
	        pctree.setClassAbsIndex(classIndex);
	        	        
	        // set IndexOnly for pctree. The pctree's (except for standard top most nodes) R-Strings will use 'Index/ClassIndex' accordingly.   
	        pctree.setIndexOnly(rstringStrategy.getIfIndexOnly());
	        
	        pctree.setName(makeName(textResult, compType));    
	       	return textResult;
	    }

		/** 
		 * Make a name based on the 'text', or if no text, then the 'compType';
		 * Make sure to remove all non-alphanumeric characters first
		 * When a name is already in our 'map' then we append
		 * an index after the name to make it unique, starting from 2.
		 * @param  text String
		 * @param  compType String
		 * @return String the name generated
		 **/
		protected String makeName (String text, String compType) {
		    String name = null;
			if(parent==null) name = windowName.getText().trim();
			if(name != null && name.length()>0) return name;
			
		    if (text==null || text.length()==0) {
		        name = PCTree.removeNonNameChars(compType);
		    } else {
		        name = PCTree.removeNonNameChars(text);
		    }
		    int i = 1;
		    String altName = name;
		    if (nameMap.get(altName) == null) {
		        nameMap.put(altName, compType);
		        return altName;
		    } else { // already used, generate another.
		        for(++i; ; i++) {
		            altName = name + Integer.toString(i);
		            if (nameMap.get(altName) != null) { // already used, generate another.
		                continue;
		            }
		            nameMap.put(altName, compType);
		            return altName;
		        }
		    }
		}

		
		/**
		 * write a line of (indented) text to our preset OutputStream.
		 * This routine adds any required indentation.  The newline char is automatically 
		 * sent after the data is sent.
		 */
		protected void write(String data){
			String output = in + data;
			try{
				if(out==System.out){
					out.write(output.getBytes());
					out.write(line_separator.getBytes());
				}else{
					out.write(output.getBytes(ENCODING_UTF8));
					out.write(line_separator.getBytes(ENCODING_UTF8));
				}
			}
			catch(IOException x){
				if (! ioxreported){					
					ioxreported = true;
					System.err.println(x.getMessage());
					x.printStackTrace();
				}
			}
		}
		
		/**
		 * write a line of text to our preset AppMap OutputStream.
		 * The newline char is automatically sent after the data is sent.
		 */
		protected void writeMap(String data){
			try{
				if(map==System.out){
					map.write(data.getBytes());
					map.write(line_separator.getBytes());
				}else{
					map.write(data.getBytes(ENCODING_UTF8));
					map.write(line_separator.getBytes(ENCODING_UTF8));
				}
			}
			catch(IOException x){
				if (! ioxtreereported){					
					ioxreported = true;
					System.err.println(x.getMessage());
					x.printStackTrace();
				}
			}
		}
		
		/**
		 * Get and output the full list of available properties for the container.
		 */
		protected void writeProperties(Object container)throws InterruptedException{
			String[] properties = server.getPropertyNames(container);
			String property = null;
			String iproperty = null;
			write("Object Properties:");		
			write("==================");		
			for(int j=0;j<properties.length;j++){
				if(isInterrupted()) throw new InterruptedException("Process cancelled or Window closed by user.");
				try{ 
					property = properties[j];
					if( property.endsWith( "()" ) ){
						property = property.substring(0, property.length() -2);
						// print the first 5 values of any indexed property
						boolean maxed = false;
						for( int k=0;(k<5)&&(!maxed);k++ ){
							if(isInterrupted()) throw new InterruptedException("Process cancelled or Window closed by user.");
							iproperty = property + "("+ String.valueOf(k).trim() +")";
							try{ write(INDENT + iproperty +"="+ 
							     server.getProperty( container, iproperty ));}
							catch(Exception x){ maxed=true;}
						}
						if(!maxed){
							write(INDENT + property +"(N)=[TOO MANY TO LIST]");
						}
					}else{
						write(INDENT + property +"="+ server.getProperty(container, property));
					}
				}
				catch(SAFSInvalidActionArgumentRuntimeException np){//should never happen
				write(INDENT + property +"= INVALID_PROPERTY_NAME");}
			}
		}
		
		/** 
		 * Write the raw tree data to System.out and the writeMap function.
		 * @param tree stored PCTree hierarchy of our processing.
		 * @see #writeMap(String)
		 **/
		protected void writeTree (PCTree tree) {
		    if (tree == null) return;
	        System.out.println("[ini:"+iniVsTreeFormat);
	        if (iniVsTreeFormat) {
	            //writeMap(tree.toIniString());
	        	writeMap(tree.toIniStringWithoutIgnoredNodes());
	        } else {
	            writeMap(tree.toString());
	        }
	        writeMap("");
		}
		
		/**
		 * Show the Hierarchy Tree of AUT.<br>
		 * <b>Note:</b> This method will block the main thread of STAFProcessContainer$Processor<br>
		 * 
		 * User can click a node on that tree and highlight the component of AUT.<br>
		 * User can triple-click a node or use F2 to modify a node's name.<br>
		 * 
		 * @param pctree
		 * @see 
		 */
		protected void showTree(PCTree pctree) {
			rsdialog = new HierarchyDlg(pctree);
		    // if objectRec.getText().equalsIgnoreCase(windowRec.getText()), the RString will be started with the top window.
		    rsdialog.setRsStartWithTopWindow(objectRec.getText().equalsIgnoreCase(windowRec.getText()));
		    rsdialog.setVisible(true);
		    
		    rsdialog.addWindowListener(new WindowAdapter(){
		    	public void windowClosing(WindowEvent e) {
		    		synchronized(Processor.this){
		    			//When the Hierarchy Dialog is closed, notify the Processor main thread
		    			Processor.this.setHierarchyDialogClosed(true);
		    			Processor.this.notifyAll();
		    		}
		    	}			
			});
		    
		    synchronized(this){
			    while(!this.isHierarchyDialogClosed()){
			    	try {
//			    		Log.debug("Waitting for hierarchy dialog closed, Processor main thread stop.");
			    		this.wait();
//						Log.debug("Finish waitting for hierarchy dialog closed, Processor main thread continue.");
					} catch (InterruptedException e) {
						Log.debug("Fail to wait Hierarchy Dialog to close.", e);
					}
			    }
		    }
		}

	} //end of Class processor

	/**
	 * HierarchyDlg derived from JFrame, with a JTree inside, is able to show the hierarchy of the PCTree passed in. 
	 * It can send out a command engine(highlightMatchingChildObject), notify RJ to highlight the component selected in 
	 * the JTree. 
	 * @author sbjjum
	 *
	 */
	public class HierarchyDlg extends JFrame implements MouseHookObserver { 
		// store all nodes in a pctree that represents a window hierarchy
		private DefaultMutableTreeNode  m_nodes = null;
	    private RsPCTreeMap 			m_rsTree = null; 
	    private boolean 				m_isRsStartWithTopWindow = false;
	    
	    private JTree 		 rsTree;   
	    private JLabel 		 rsTextlabel;
	    private JCheckBox    shortStrCheck;
	    private JTextField 	 rsTextField;
	    private JTextField 	 classNameTextField;
	    private JButton 	 bHighlight;
	    private JButton		 bDisposeHighlight;
	    private JButton		 bMouseHookToggle;
	    
	    private JTextField   eSearchString;   
	    private JButton      bFindPrevious;
	    private JButton      bFindNext;
	    private ArrayList    m_nodesFound     = null;  // store all nodes that match the search string
	    private ListIterator m_nodesFoundIter = null;  // the Iterator of m_nodesFound, holding the same info for doing 'Previous' and 'Next'
	    private TreeNode     m_curNodeFound   = null;  // reference to the current node in m_nodesFoundIter
		
	    private MouseCheckTimer m_mouseCheck = null;           // MouseCheckTimer for hovering mouse to trigger a message to run onHandleMouseCheck(Point)  
	    
	    public HierarchyDlg(PCTree pctree){
	        super();
			try{setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);}catch(Exception x){;}
	        m_rsTree = new RsPCTreeMap();
	        m_rsTree.SetPCTree(windowRec.getText(), pctree);
		    //create nodes that hold the hierarchy of components in Pctree
		    m_nodes = m_rsTree.getNodesFromPCTree();
		    
		    initMouseCheckTimer();
			
		    //outPutRSMap(m_rsTree);
	        populateDialog();
	    }
	    
	    private void initMouseCheckTimer(){
			m_mouseCheck = new MouseCheckTimer();
			m_mouseCheck.setObserver(this);
			
			Thread mythread = new Thread(m_mouseCheck);
			mythread.start();
	    }
	    
	    public boolean getRsStartWithTopWindow(){
	    	return m_isRsStartWithTopWindow;
	    }
	    
	    public void setRsStartWithTopWindow(boolean status){
	    	m_isRsStartWithTopWindow = status;
	    }

	    public void onHandleMouseCheck(Point point) {
	    	Log.debug("SPC.HierarchyDlg.onHandleMouseCheck is fired to process ...");

	    	//get top window
	    	Object topWinKey = m_rsTree.getTopWinKey();
	    	Log.debug("...curent topwin' key:" + topWinKey + " at point [" + point.x + "," + point.y + "]");
	    	//send message to engine via STAFLocalServer to get the matching keys
	    	Object[] childKeys = server.getMatchingChildKeysAtPoint(topWinKey, point.x, point.y);
	    	
	    	//m_mouseCheck.setRunning(false); //only run one time for debugging
    		Log.debug("number of matched childkeys: " + childKeys.length);
    		
    		// check to find matching component_name
	    	String tmpname = null;
	    	for (int i = 0; i<childKeys.length; i++) {
	    		tmpname =  m_rsTree.getNodeName((String)childKeys[i]);
	    		if (tmpname != null) 
	    			break;
	    	}
	    	String	uniqueName = ((tmpname != null)? tmpname : "");
	    	
	    	Log.debug("...matching component name: " + uniqueName);
	    	Log.debug("...matching component' r-string: " + m_rsTree.GetNodeVal(uniqueName));
	    	
	    	// move the cursor to the right node in GUI tree, rsTree 
	    	TreeNode shootNode = null;
	    		
		    shootNode = getMatchingNodeByName((TreeNode)rsTree.getModel().getRoot(), uniqueName);
			if (shootNode != null) {
				// expand the tree to set focus on this node
				TreePath visiblePath = new TreePath(((DefaultTreeModel)rsTree.getModel()).getPathToRoot(shootNode));
				rsTree.makeVisible(visiblePath);
				rsTree.scrollPathToVisible(visiblePath);
				rsTree.setSelectionPath(visiblePath);  
			} 
	    }
	    
	    private void populateDialog() {
	    	int width = 400;
	    	int height = 500;
	    	int buttonH = 20;
	    	int w4 = width/4;

	    	// create a JTree with tree nodes created
		    rsTree = new JTree(m_nodes);
		    //Set the tree to be editable, we can modify the tree's node name
		    rsTree.setEditable(true);
		    //Add a new CellEditor for RSTree so that we can validate the node's modification.
		    ValidatingEditor cellEditor = new ValidatingEditor(new JTextField());
		    cellEditor.setValidatingListener(new ValidatingListener(){
				public boolean validate( ValidatingEventObject evt) {
					try{
						//validate the modification of the tree cell
						ValidatingEditor editor = (ValidatingEditor) evt.getSource();
						String newText = (String) editor.getCellEditorValue();
						String originalText = (String) editor.getOriginalNodeValue();
						//Don't accept blank value
						if(newText==null || newText.trim().equals("")){
							JOptionPane.showMessageDialog(HierarchyDlg.this, 
									                      "The tree node's text should not be empty!",
									                      "Rename Warning",
									                      JOptionPane.WARNING_MESSAGE);
							return false;
						}
						//Don't accept the text if there is another node with the same name
						if(m_rsTree.hasRedondanceName(newText)){
							JOptionPane.showMessageDialog(HierarchyDlg.this,
									                      "Another tree node has the same name! Give another name.",
									                      "Rename Warning",
									                      JOptionPane.WARNING_MESSAGE);
							return false;
						}
						//Accept the modification and modify the RsPCTreeMap's caches
						Log.debug("Update node name: oldname="+originalText+" ; newName="+newText);
						m_rsTree.updateCache(originalText, newText);
						m_rsTree.updatePCTree(originalText, newText);
						return true;

					}catch(Exception e){
						Log.debug("Met exception:", e);
						return false;
					}
				}
		    });
		    rsTree.setCellEditor(cellEditor);
		    
		    rsTree.setRootVisible(false);
		    rsTree.setShowsRootHandles(true);
			rsTree.setToolTipText("select the node you want");
			rsTree.setBounds(new Rectangle(5, 5, width - 10, height - 50));
			rsTree.setVisibleRowCount(15);
			rsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			rsTree.addTreeSelectionListener(new TreeSelectionListener(){ 
				public void valueChanged(TreeSelectionEvent e){
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
					String nodeName = node.toString();
		            // get the recog string
		            String rstring = m_rsTree.GetNodeVal(nodeName, m_isRsStartWithTopWindow, shortStrCheck.isSelected());
		            rsTextField.setText(rstring);
		            //Get the class name
		            String className = m_rsTree.GetNodeObjectClass(nodeName);
		            classNameTextField.setText(className);
		        }
		    }); 
			rsTree.addTreeSelectionListener(new ResetSearchListener());
			
			// top
			JPanel top = new JPanel();
		
			eSearchString = new JTextField();	
			eSearchString.setToolTipText("input something like PushButton ...");
			eSearchString.setPreferredSize(new Dimension(w4-40, buttonH));
			eSearchString.addKeyListener(new KeyAdapter() {
        		public void keyReleased(KeyEvent e) { onFindWhatChanged(); }
			});
			
			bFindPrevious = new JButton("Previous");
			bFindPrevious.setToolTipText("");
			bFindPrevious.setPreferredSize(new Dimension(w4, buttonH));
			bFindPrevious.setEnabled(false);
			bFindPrevious.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { doFindPrevious();	}
		    });			

			bFindNext = new JButton("Next");
			bFindNext.setToolTipText("");
			bFindNext.setPreferredSize(new Dimension(w4,buttonH));
			bFindNext.setEnabled(false);
			bFindNext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { doFindNext(); }
		    });
			
			JPanel findPanel = new JPanel();
			findPanel.setLayout(new BoxLayout(findPanel, BoxLayout.X_AXIS));
			findPanel.setBorder(BorderFactory.createTitledBorder("Find What:"));
			findPanel.add(eSearchString);
			findPanel.add(Box.createHorizontalStrut(20));
			findPanel.add(bFindPrevious);
			findPanel.add(Box.createHorizontalStrut(20));
			findPanel.add(bFindNext);
			
			JScrollPane scroller = new JScrollPane(rsTree);

			top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));			
			top.add(findPanel);
			top.add(scroller);
			
		    // center
	        JPanel center = new JPanel();
	        rsTextlabel = new JLabel();
	        rsTextlabel.setText("Select a node above to get its R-Strings");
	        rsTextlabel.setPreferredSize(new Dimension(width-170,buttonH));
	        
	        shortStrCheck = new JCheckBox("Shortest String", false);
	        shortStrCheck.setPreferredSize(new Dimension(150,buttonH));
	        
	        rsTextField = new JTextField();	        	
	        rsTextField.setText("enter your Recognition string!");
	        rsTextField.setToolTipText("recognition string (R-String)");
	        rsTextField.setAlignmentX(10);
	        rsTextField.setPreferredSize(new Dimension(width-10,buttonH));
	        
	        classNameTextField = new JTextField();	        	
	        classNameTextField.setText("Object's class name will be shown here!");
	        classNameTextField.setToolTipText("Object class name.");
	        classNameTextField.setAlignmentX(10);
	        classNameTextField.setPreferredSize(new Dimension(width-10,buttonH));
	        classNameTextField.setEditable(false);
	        
	        center.add(rsTextlabel);
	        center.add(shortStrCheck);
	        center.add(rsTextField);
	        center.add(classNameTextField);
	        	
        	// bottom
        	JPanel bottom = new JPanel();
        	bHighlight = new JButton("Highlight");
        	bHighlight.setToolTipText("highlight the GUI matching the R-String above");
        	bHighlight.setPreferredSize(new Dimension(100,buttonH));
        	bHighlight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { 
					SetMouseCapture(false); // stop capture before doHighLight()
					doHighLight(); 
				}
		    });
        	
        	bDisposeHighlight = new JButton("Dispose Highlight");
        	bDisposeHighlight.setToolTipText("Clear the red highlighted area.");
        	bDisposeHighlight.setPreferredSize(new Dimension(150,buttonH));
        	bDisposeHighlight.setEnabled(false);
        	bDisposeHighlight.addActionListener(new ActionListener(){
        		public void actionPerformed(ActionEvent e){
        			server.clearHighlightedDialog();
        			bDisposeHighlight.setEnabled(false);
        			bHighlight.setEnabled(true);
        		}
        	});

        	bMouseHookToggle = new JButton("Capture");
        	bMouseHookToggle.setToolTipText("Hover over desired Test Object for its R-String.");
        	bMouseHookToggle.setPreferredSize(new Dimension(100,buttonH));
        	bMouseHookToggle.setEnabled(true);
        	bMouseHookToggle.addActionListener(new ActionListener(){
        		public void actionPerformed(ActionEvent e){
        			// toggle current status
        			SetMouseCapture(!m_mouseCheck.isRunning());
        		}
        	});

        	bottom.add(bHighlight);
        	bottom.add(bDisposeHighlight);
        	bottom.add(bMouseHookToggle);
	        	
	        Container cp = this.getContentPane(); 
		    cp.add(top, BorderLayout.NORTH);
		    cp.add(center,BorderLayout.CENTER);
		    cp.add(bottom,BorderLayout.SOUTH);
		    
		    this.addWindowListener(new WindowAdapter(){
		    	public void windowClosing(WindowEvent e) {
		    		server.clearHighlightedDialog();
		    		SetMouseCapture(false);
		    	}
		    });
		    this.setTitle("Component Hierarchy Viewer");   
	        this.setSize(width, height);  
	        this.toFront();
	        this.setVisible(true);
	    }
	    
	    private void SetMouseCapture(boolean isRunning) {	
	    	if (m_mouseCheck == null) {
   				Log.debug("the mouseHook is NULL, can't preform!");
	    		return;
	    	}	
	    	if (isRunning) {
	    		m_mouseCheck.setRunning(true);
	    		bMouseHookToggle.setText("Pause");
	    	} else {
	    		m_mouseCheck.setRunning(false);
	    		bMouseHookToggle.setText("Capture");
	    	}	
	    }
		class ResetSearchListener implements TreeSelectionListener{
			public void valueChanged(TreeSelectionEvent e) {
				TreeNode node = (TreeNode) e.getPath().getLastPathComponent();
				changeCurrentNodeIfClickedNodeOrItschildIsInFoundList(node);
			}
		}
		/**
		 * The purpose of this method is to set the currentFoundNode to the clicked node, 
		 * if the clicked node is found in the "the found nodes list".
		 * 
		 * This method is called by ResetSearchListener.
		 * @param clickedNode
		 */
		private void changeCurrentNodeIfClickedNodeOrItschildIsInFoundList(TreeNode clickedNode){
			String debugmsg = getClass().getName()+".changeCurrentNodeIfClickedNodeIsInFoundList() ";
			//We do not need to reset the m_curNodeFound if the clickedNode is the same as it.
			if(clickedNode.equals(m_curNodeFound)){
				return;
			}
			if(m_nodesFound==null){
				Log.info(debugmsg+" The found nodes list is null. Maybe you have not done the search.");
				return;
			}
			String targetStr = eSearchString.getText();
			if(targetStr==null || targetStr.length()==0){
				Log.info(debugmsg+" You should set the searching text.");
				return;
			}
			
			//Looking for the node matching the targetStr from the clickedNode (just in its subtree)
			TreeNode firstMatchingNode = getFirstMatchingNode(clickedNode,targetStr);
			if(firstMatchingNode==null){
				Log.index(debugmsg+" There is no matching node in the subtree of the clicked node.");
				return;
			}
			
			ListIterator tmpIter = m_nodesFound.listIterator();
			for(int i=0;i<m_nodesFound.size();i++){
				tmpIter.next();
				//If we find the clickedNode in the list, we should reset m_nodesFoundIter and m_curNodeFound
				if(firstMatchingNode.equals(m_nodesFound.get(i))){
					//If the firstMatchingNode is not the same as clickedNode, we should
					//move the pointer back and set the m_curNodeFound to null so that the firstMatchingNode
					//will be returned when you click next button
					//Else we should set m_curNodeFound to firstMatchingNode so that the 'matched node' after firstMatchingNode
					//will be returned when you click next button
					if(!clickedNode.equals(firstMatchingNode)){
						tmpIter.previous();
						m_curNodeFound = null;
					}else{
						if(i==0){
							//If the first node match the clickedNode, we should move the point back
							//so that button previous can be disabled when call onNodesFoundIteratorChange
							tmpIter.previous();
						}
						m_curNodeFound = firstMatchingNode;
					}

					m_nodesFoundIter = tmpIter;
					onNodesFoundIteratorChange(tmpIter);
					Log.info(debugmsg+" Found the clickedNode in the found nodes list at "+i);
					break;
				}
			}
			//Log.info(debugmsg+" Did not find the clickedNode in the found nodes list.");
		}
	    
	    private TreeNode getFirst(ListIterator iter){
	    	if (iter.hasNext()) 
	    		return (TreeNode) iter.next();
	    	else
	    		return null;
	    }	    
	    private TreeNode getNext(ListIterator iter){
	    	if (iter.hasNext()) {
	    		TreeNode next = (TreeNode)iter.next();
	    		if (next == m_curNodeFound) {
	    			if (iter.hasNext())
	    				m_curNodeFound = (TreeNode)iter.next();
	    			else 
	    				return null;
	    		} else {
	    			m_curNodeFound = next;
	    		}
	    		onNodesFoundIteratorChange(iter);	
	    		return m_curNodeFound;
	    	} else 
	    		return	null;
	    }
	    
	    private TreeNode getPrevious(ListIterator iter){
	    	if (iter.hasPrevious()) {
	    		TreeNode prev = (TreeNode)iter.previous();
	    		if (prev == m_curNodeFound) {
	    			if (iter.hasPrevious()) {
	    				m_curNodeFound = (TreeNode)iter.previous();
	    			} else 
	    				return null;
	    		} else {
    				m_curNodeFound = prev;
	    		}
	    		onNodesFoundIteratorChange(iter);	
	    		return m_curNodeFound;
	    	} else 
	    		return null;
	    }
	    
	    private void onNodesFoundIteratorChange(ListIterator iter){
    		if (iter.previousIndex() == -1){
				bFindPrevious.setEnabled(false); // no previous node
    			bFindNext.setEnabled(true);
    		} else if (iter.nextIndex() == m_nodesFound.size()) {
    			bFindPrevious.setEnabled(true);
    			bFindNext.setEnabled(false);    // no next node
    		} else {
    			bFindPrevious.setEnabled(true);
    			bFindNext.setEnabled(true);
    		}
	    }
	    /**
	     * @param node  tree node to be searched 
	     * @param name  text for matching a node
	     * @return an matching TreeNode
	     */
	    private TreeNode getMatchingNodeByName(TreeNode node, String name) {
	    	if (node == null)
	    		return null;
	    	
	    	// get the text of node shown in JTree
            String curname = node.toString();
    		if (curname != null) 
    			if (curname.equalsIgnoreCase(name))
    				return node;

    		TreeNode tmpNode = null;
    		// search its children and return the matching node
    		for(int i=0;i<node.getChildCount();i++){
    			tmpNode = getMatchingNodeByName(node.getChildAt(i),name);
    			if(tmpNode!=null){
    				return tmpNode;
    			}
    		}
    		return null;
	    }
	    
	    /**
	     *  find the first node that match str from the provided node (in its descedants)
	     * @param node, TreeNode represents a GUI hierarchy 
	     * @param str, searching string
	     */
	    private TreeNode getFirstMatchingNode(TreeNode node, String str) {
	    	if (node == null)
	    		return null;
	    	
	    	// see if the 'shortest' r-string contains the target string
            String rstring = m_rsTree.GetNodeVal(node.toString(), m_isRsStartWithTopWindow, true);
    		if (rstring != null) 
    			if (rstring.toUpperCase().indexOf(str.toUpperCase()) >= 0)
    				return node;

    		TreeNode tmpNode = null;
    		// search its children and return the first matched node
    		for(int i=0;i<node.getChildCount();i++){
    			tmpNode=getFirstMatchingNode(node.getChildAt(i),str);
    			if(tmpNode!=null){
    				return tmpNode;
    			}
    		}
    		return null;
	    }
	    /**
	     *  find all nodes that match str and store them in a list m_nodesFound
	     * @param node, TreeNode represents a GUI hierarchy 
	     * @param str, searching string
	     */
	    private void getMatchingNodes(TreeNode node, String str) {
	    	if (node == null)
	    		return;

	    	if (m_nodesFound == null)
	    		m_nodesFound = new ArrayList();
	    	
	    	// see if the 'shortest' r-string contains the target string
            String rstring = m_rsTree.GetNodeVal(node.toString(), m_isRsStartWithTopWindow, true);
    		if (rstring != null) 
    			if (rstring.toUpperCase().indexOf(str.toUpperCase()) >= 0)
    				m_nodesFound.add(node);

    		// search its children
	    	if (node.getChildCount() > 0) 
	    		 for (Enumeration e = node.children(); e.hasMoreElements(); ) 
	    			 getMatchingNodes((TreeNode)e.nextElement(), str);
	    }
	    
	    // find the first or next matching component according to the search string in eSearchString
	    private void doFindNext() {
			// get search string
			String targetStr = eSearchString.getText();
			
			if (targetStr == null || targetStr.length() == 0)
				return;

			// search it in	m_nodes
		    TreeNode shootNode = null;
		    if (m_nodesFoundIter == null) {
    			TreeNode root = (TreeNode)rsTree.getModel().getRoot();
    			getMatchingNodes(root, targetStr);
    			if (m_nodesFound != null) {
    				m_nodesFoundIter = m_nodesFound.listIterator();
    				shootNode = getFirst(m_nodesFoundIter);
    			}	
		    } else
		    	shootNode = getNext(m_nodesFoundIter);        		    	
		    
			if (shootNode != null) {
				// expand the tree to set focus on this node
				TreePath visiblePath = new TreePath(((DefaultTreeModel)rsTree.getModel()).getPathToRoot(shootNode));
				rsTree.setSelectionPath(visiblePath);   
				rsTree.scrollPathToVisible(visiblePath);
			} else
				JOptionPane.showMessageDialog(null,"Can't find it!");	
	    }
	    // find previous matching component in m_nodesFoundIter, the iterator of m_nodesFound
	    private void doFindPrevious(){
			if (m_nodesFoundIter == null)
				return;
			
			TreeNode shootNode = getPrevious(m_nodesFoundIter);
			if (shootNode != null) {
				// expand the tree to set focus on this node
				TreePath visiblePath = new TreePath(((DefaultTreeModel)rsTree.getModel()).getPathToRoot(shootNode));
				rsTree.setSelectionPath(visiblePath);   
				rsTree.scrollPathToVisible(visiblePath);
			}
	    }
	    
	    // called while search string is changed
	    private void onFindWhatChanged(){
	    	// reset because of search string changed
			if (m_nodesFound != null) m_nodesFound.clear();
			if (m_nodesFoundIter != null) m_nodesFoundIter = null;
			if (m_curNodeFound != null)	m_curNodeFound = null;
			
			if (eSearchString.getText().length() > 0) {
				bFindPrevious.setEnabled(false);
				bFindNext.setEnabled(true);
			} else {
				bFindPrevious.setEnabled(false);
				bFindNext.setEnabled(false);
			}
	    }

	    /**
	     *  highlight a child GUI component on its top most window
	     *  rsTextField: the r-string of the child window to be highlighted
	     */
	    private void doHighLight() {
  			// notify RJ engine, highlight the shooting component
	    	Thread runHighlight = new Thread(new Runnable(){
		        public void run(){
//		        	bHighlight.setEnabled(false);
		        	
		        	//////////////////////////////////////////////////////////////////////////
		        	// get selected node's name that stands for the component's unique name auto-generated 
					TreeNode curNode = (TreeNode)((DefaultTreeSelectionModel)rsTree.getSelectionModel()).getSelectionPath().getLastPathComponent();
					Object compkey = m_rsTree.getKeyByNodeName(curNode.toString());
					Object winKey =  m_rsTree.getTopWinKey();
					String rstring = m_rsTree.GetNodeVal(curNode.toString());
			        boolean ishighLighted = server.highlightMatchingChildObjectByKey(winKey, compkey, rstring);
 
		        	//boolean ishighLighted = server.highlightMatchingChildObject(m_rsTree.getTopWinRString(), rsTextField.getText());
		        	String msg = ishighLighted ? "Found and highlighted!" : "Not found or not highligted";
		        	JOptionPane.showMessageDialog(HierarchyDlg.this, msg);
		        	
		        	bDisposeHighlight.setEnabled(ishighLighted);
		        	bHighlight.setEnabled(!ishighLighted);
		        }
			});
			runHighlight.start();
	    }
	    
	} // end of HierarchyDlg

	/**
	 * This class override the method stopCellEditing() of DefaultCellEditor.<br>
	 * User needs to set a ValidatingListener to this class so that it can decide<br>
	 * if the modification of the editor will be accepted or not.<br>
	 */
  	protected class ValidatingEditor extends DefaultCellEditor {

  		private ValidatingListener validatingListener = null;
  		private Object originalNodeValue = null;
  		
		public ValidatingEditor(JTextField textField) {
			super(textField);
		}

		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row) {
			//value is by default a DefaultMutableTreeNode
//			System.out.println("edit object = "+value.getClass().getName());
//			System.out.println("edit object value = "+value.toString());
			originalNodeValue = value.toString();
			return super.getTreeCellEditorComponent(tree, value, isSelected,
					expanded, leaf, row);
		}
		
		public boolean stopCellEditing() {
//			System.out.println("stop editing, cell value=" + this.getCellEditorValue());
			if(validatingListener!=null){
				//The editor contains the modified value, check the value
				//if it doesn't satisfy the requirement, cancel the editing.
				if (!validatingListener.validate(new ValidatingEventObject(this))) {
					cancelCellEditing();
				}
			}

			return super.stopCellEditing();
		}

		public ValidatingListener getValidatingListener() {
			return validatingListener;
		}

		public void setValidatingListener(ValidatingListener validatingListener) {
			this.validatingListener = validatingListener;
		}

		public Object getOriginalNodeValue() {
			return originalNodeValue;
		}
	}
  	
  	public interface ValidatingListener extends EventListener{
  		public boolean validate(ValidatingEventObject evt);
  	}
  
  	public class ValidatingEventObject extends EventObject{
		public ValidatingEventObject(Object source) {
			super(source);
		}
  	}
	
	/* RsPCTreeMap holds a PCTree. It is able to convert the PCTree to JavaTree nodes hierachically. The name of each node is the 
	 * component name that is consistent with created in PCTree. The component name and its recognition string are saved in   
	 * m_rscache as a comp_name/RString pair.
	 * 
	 * @author JunwuMa
	 */
	public class RsPCTreeMap implements Serializable {
	    
		private String	    		m_topWindowRS = null;
		private PCTree    			m_pctree = null;
	    
	    // holds pairs like: <component_name, rs-string>. component_name also is saved as the name of one node in m_nodes.
		// <"textBox1","Type=EditBox;Name=textBox1">
		private Hashtable 				m_rscache = null;  
		private DefaultMutableTreeNode 	m_nodes = null;
		
		// holds pairs like: <object key, component_name>. object key is generated in RJ engine 
		// and put in every tree node as UserObject in PCTree. component_name is auto-generated uniquely; 
		// it is as same as component_name in m_rscache <component_name, rs-string>
		private Hashtable 				m_keycache = null;  
		
		// holds pairs like: <component_name, object-class>
		private Hashtable 				m_classcache = null;  

		RsPCTreeMap(){
	        m_rscache = new Hashtable(10);
	        m_keycache = new Hashtable(10);
	        m_classcache = new Hashtable(10);
	    }
	    void SetPCTree(String topWinRString, PCTree pctree){
	    	m_topWindowRS = topWinRString;
	    	m_pctree = pctree;	
	        //clear hashtable
	        m_rscache.clear();
	        m_keycache.clear();
	        m_classcache.clear();
	    }
	    RsPCTreeMap(PCTree pctree){
	        m_rscache = new Hashtable(10);
	        m_keycache = new Hashtable(10);
	        m_classcache = new Hashtable(10);
	        m_pctree = pctree;
	    }	
	    String getTopWinRString(){
	    	return m_topWindowRS;
	    }
	    
	    Object getTopWinKey(){
	    	return m_pctree.getUserObject();
	    }
	    
		// returns nodes that hold the hierarchy of components in m_pctree
		public DefaultMutableTreeNode getNodesFromPCTree() {
	        if (m_pctree == null)
	        	return null;
	        //clear hashtable
	        m_rscache.clear();
	        m_keycache.clear();
	        m_classcache.clear();
		    //m_nodes will be generated in CreateJTreeNodes
	        CreateJTreeNodes(m_pctree,null);

		    return m_nodes;
		}
		
		/** 
	     * @param name, component's name, value in cache for reverse look-up 
	     * @return String, key to the component's name, it is also attached to a cached object on engine side.
	     * */		
	    public String getKeyByNodeName(String name) {
	    	java.util.Enumeration  eum = m_keycache.keys(); 
	    	while (eum.hasMoreElements()) {
	    		String key = (String) eum.nextElement();
	    		String value = (String)m_keycache.get(key);

	    		if (value.equals(name))
	    			return key;
	    	}
	    	return "";
	    }

		/** 
	     * @param key, unique key 
	     * @return String, value in m_keycache.
	     * */
	    public String getNodeName(String key) {
	    	return (String)m_keycache.get(key);
	    }
		
		/**
		 * @param key, a String of unique component name auto-generated
		 * @return  recognition string of the key (component name)
		 */
		public String GetNodeVal(String key){
		    return (String)m_rscache.get(key);
		}
		
		/**
		 * @param name, a String of unique component name auto-generated
		 * @return  the object class name of the name (component name)
		 */
		public String GetNodeObjectClass(String name){
			return (String)m_classcache.get(name);
		}
		
		// returns a substring of the recognition string represented by key accordingly
		public String GetNodeVal(String key, boolean isRsStartWithTopWindow, boolean isShortest){
		    String rtl = (String)m_rscache.get(key);
			ArrayList prefixlist = new ArrayList();
			
			//Remove the prefix before manipulating the RS
			rtl = GuiObjectVector.removeRStringPrefixes(rtl, prefixlist);
			
			//get whole prefix string
			String addedPrefix = "";
			for(int i=0; i<prefixlist.size(); i++)
				addedPrefix += (String)prefixlist.get(i);
			
		    //manipulating the RS
			if (isShortest){
		    	for (int i; (i = rtl.indexOf(";\\;"))>=0; ) 
		    		rtl = rtl.substring(i+3); //skip 3 charactors, return the part after the last ";\\;" in the r-string
		    	//We should also remove :FPSM: from addedPrefix if it exists.
		    	int fpsmIndex = addedPrefix.indexOf(GuiObjectVector.FULLPATH_SEARCH_MODE_PREFIX);
		    	if(fpsmIndex >-1){
		    		addedPrefix = addedPrefix.substring(0, fpsmIndex)+
		    					  addedPrefix.substring(fpsmIndex+GuiObjectVector.FULLPATH_SEARCH_MODE_PREFIX.length());
		    	}
			}
		    //Comment else if section:
			//As XXX_SEARCH_MODE_PREFIX will be added to top window RS, if we remove the top window,
			//XXX_SEARCH_MODE_PREFIX will not work.
//			else if (isRsStartWithTopWindow) {
//				// drop off top-window for being recoginized
//				// e.g. RString="Class=WinDemo.Form1;Name=Form1;\;Type=TabControl;Name=tabControl1"
//				// 'Class=WinDemo.Form1;Name=Form1' is a top window but is not a standard name like 
//				// "TYPE=Window;.." or "TYPE=JavaWindow;..", which can be recognized and skipped during searching with the algorithm.
//				// So, manually skip it.
//				int i = rtl.indexOf(";\\;");
//				if (rtl.indexOf(";\\;") >= 0)
//					rtl = rtl.substring(i+3); 
//			}

			//Add the prefix back after manipulating the RS
			rtl = addedPrefix+rtl;
			
			return rtl;
		}
		
		private void CreateJTreeNodes(PCTree pctree, DefaultMutableTreeNode inode) {
		    if (pctree == null) 
		        return;
		    String name = pctree.getName();
		    Object objectKey = pctree.getUserObject();
		    // get long R-String
		    String rstring = pctree.getComponentRecogString(true);
		    String className = pctree.getObjectClass();

		    DefaultMutableTreeNode node = inode;
		    if (pctree.isIgnoredNode())		    
		        CreateJTreeNodes((PCTree)pctree.getFirstChild(),node);			    	
		    else if (node == null){
		        node = new DefaultMutableTreeNode(name);
		        
		        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(name);
		        node.add(childNode);		        
		        
		        m_rscache.put(name, rstring);
		        m_classcache.put(name, className);
		        m_keycache.put(objectKey, name);
		        CreateJTreeNodes((PCTree)pctree.getFirstChild(),childNode);			        
		    }
		    else {
		        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(name);
		        node.add(childNode);
		        m_rscache.put(name, rstring);
		        m_classcache.put(name, className);
		        m_keycache.put(objectKey, name);
		        CreateJTreeNodes((PCTree)pctree.getFirstChild(),childNode);
		    }

		    CreateJTreeNodes((PCTree)pctree.getNextSibling(), node);
		    
		    if (inode == null)  // output nodes created
		        if (node != null) // an ignored node is no need to be set value to m_nodes 
		        	m_nodes = node;
		}
		
		public boolean hasRedondanceName(String newName){
			if(m_rscache!=null) return m_rscache.containsKey(newName);
			else return false;
		}
		
		/**
		 * Update the 3 caches {@link #m_rscache}, {@link #m_classcache} and {@link #m_keycache}.<br>
		 * 
		 * @param oldName
		 * @param newName
		 */
		public boolean updateCache(String oldName, String newName){
			if(m_rscache.containsKey(oldName)){
				Object rsString =m_rscache.get(oldName);
				Object classString =m_classcache.get(oldName);
				String refKey = getKeyByNodeName(oldName);
				
				m_rscache.remove(oldName);
				m_rscache.put(newName, rsString);
				
				m_classcache.remove(oldName);
				m_classcache.put(newName, classString);
				
				m_keycache.put(refKey, newName);
				
				return true;
			}else{
				return false;
			}
		}
		
		//Update the PCTree, so that the modification can be wrote to Map file.
		public boolean updatePCTree(String oldName, String newName){
			return m_pctree.updateName(oldName, newName);
		}
		
	}// end of Class RsPCTreeMap

	public void valueChanged(ListSelectionEvent e) {
		Object[] clients = clientType.getSelectedValues();
		setDomainName(clients);
	}
	
	private String convertArrayToString(Object[] array){
		String result = "";
		
		if(array==null)
			return result;
			
		for(int i=0;i<array.length;i++){
			if(i+1==array.length)
				result += array[i];
			else
				result += array[i]+DriverConstant.DOMAIN_SEPARATOR;
		}
		
		return result;
	}
	
	private String[] convertStringToArray(String domains){
		if(domains==null) return null;
		
		List<String> list = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(domains,DriverConstant.DOMAIN_SEPARATOR);
		
		while(tokens.hasMoreTokens()){
			list.add(tokens.nextToken());
		}
		
		String[] result = new String[list.size()];
		for(int i=0;i<list.size();i++){
			result[i] = list.get(i);
		}
		return result;
	}
	
	private int[] getIndicesToSelect(String[] domains, String[] typesToSelect){
		List<Integer> list = new ArrayList<Integer>();
		
		for(int i=0;i<domains.length;i++){
			for(int j=0;j<typesToSelect.length;j++){
				if(domains[i].equalsIgnoreCase(typesToSelect[j])){
					list.add(Integer.valueOf(i));
					break;
				}
			}
		}
		
		int[] result = new int[list.size()];
		for(int i=0;i<list.size();i++){
			result[i] = list.get(i).intValue();
		}
		
		return result;
	}
	
	/*
	 * Purpose: set the domainname, which represent the enabled domains and will
	 * be sent to engine.
	 * clients is a String array, which contains domain-display-name.
	 * domain-display-name is like Java Client, HTML Client, etc which are defined
	 * as constant ( JAVA_CLIENT_DISPLAY, HTML_CLIENT_DISPLAY etc) of this class
	 */
	private void setDomainName(Object[] clients){
		domainname = "";  // reset domainname
		for (int i = 0; i < clients.length; i++) {
			String val = (String) clients[i];
			if (val.equalsIgnoreCase(DriverConstant.ANDROID_CLIENT_DISPLAY)) {
				domainname += DriverConstant.ANDROID_CLIENT_TEXT + DriverConstant.DOMAIN_SEPARATOR;
				//If it is android domain, disable the RFSM, MCSM
				mappedClassSearch.setEnabled(false);
				mappedClassSearchMode = false;
				rfsmSearch.setEnabled(false);
				rfsmSearchMode = false;
			}else{
				//If it is other domain, enable the RFSM, MCSM
				mappedClassSearch.setEnabled(true);
				mappedClassSearchMode = mappedClassSearch.isSelected();
				rfsmSearch.setEnabled(true);
				rfsmSearchMode = rfsmSearch.isSelected();

				if (val.equalsIgnoreCase(DriverConstant.JAVA_CLIENT_DISPLAY)) {
					domainname += DriverConstant.JAVA_CLIENT_TEXT + DriverConstant.DOMAIN_SEPARATOR;
				} else if (val.equalsIgnoreCase(DriverConstant.HTML_CLIENT_DISPLAY)) {
					domainname += DriverConstant.HTML_CLIENT_TEXT + DriverConstant.DOMAIN_SEPARATOR;
				} else if (val.equalsIgnoreCase(DriverConstant.NET_CLIENT_DISPLAY)) {
					domainname += DriverConstant.NET_CLIENT_TEXT + DriverConstant.DOMAIN_SEPARATOR;
				} else if (val.equalsIgnoreCase(DriverConstant.RCP_CLIENT_DISPLAY)) {
					domainname += DriverConstant.RCP_CLIENT_TEXT + DriverConstant.DOMAIN_SEPARATOR;
				} else if (val.equalsIgnoreCase(DriverConstant.FLEX_CLIENT_DISPLAY)) {
					domainname += DriverConstant.FLEX_CLIENT_TEXT + DriverConstant.DOMAIN_SEPARATOR;
				} else if (val.equalsIgnoreCase(DriverConstant.WIN_CLIENT_DISPLAY)) {
					domainname += DriverConstant.WIN_CLIENT_TEXT + DriverConstant.DOMAIN_SEPARATOR;
				}
					
			}
			//Remove the last separator
			if(domainname.length()>0){
				domainname = domainname.substring(0,domainname.length()-1);
			}
		}
	}
}