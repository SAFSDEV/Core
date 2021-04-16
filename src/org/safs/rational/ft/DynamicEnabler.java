/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
/**
 * @author Carl Nagle  NOV 12, 2007 Added support for custom DAT files in Class & JAR directory.
 * @author WangLei NOV 07, 2008 Added .net related field and methods enableNetWindows()
 * @author Carl Nagle  APR 03, 2009 Refactored primarily removing need for ClassMap.dat files on Windows.
 *                              Also added JNA usage to help find ALL processes that can be enabled.
 * @author Carl Nagle  Jun 04, 2009 Added getJNAProtectedDomainTopObjects
 * @author Carl Nagle  Aug 07, 2009 Added Process= support to enable only one or more specific processes.
 * @author Carl Nagle  Apr 16, 2010 Handle NullPointerExceptions in loadWinEnabled when RFT starts to act up.
 * @author Lei Wang Sep 14, 2011 Modify method simpleEnableTopWindows(): Don't enable 'System Idle Process'.
 * @author Lei Wang Mar 15, 2019 Modify simpleEnableTopWindows(): don't enable top windows dynamically if 'enableTopWindows' is turned off.
 * @author Lei Wang Mar 28, 2019 Modify simpleEnableTopWindows(): If the window's class name is not in the white list (WindowsClassMap.dat), we will not enable it.
 *                                                               Comment out the enableUniqueWinsWithJNA temporarily, it wastes too much times.
 */
package org.safs.rational.ft;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.safs.Domains;
import org.safs.Log;
import org.safs.jvmagent.AgentClassLoader;
import org.safs.natives.NativeWrapper;
import org.safs.rational.RGuiObjectVector;
import org.safs.rational.Script;
import org.safs.text.FileUtilities;
import org.safs.tools.drivers.DriverConstant.SafsROBOTJ;

import com.rational.test.ft.NotSupportedOnUnixException;
import com.rational.test.ft.UnableToHookException;
import com.rational.test.ft.WindowHandleNotFoundException;
import com.rational.test.ft.object.TestObjectReference;
import com.rational.test.ft.object.interfaces.DomainTestObject;
import com.rational.test.ft.object.interfaces.IWindow;
import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TopLevelTestObject;
import com.rational.test.ft.script.CaptionText;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.sys.TestContext.Reference;
import com.sun.jna.Platform;

/**
 * IBM Rational Functional Tester V6 and later supports the testing of Win32 and .NET windows and
 * applications.  However, these environments are not enabled for testing in the traditional
 * means as seen for Java and other environments.  These environments require what Rational
 * calls 'dynamic enabling'.
 * <p>
 * Essentially, we must already know a window handle, process ID, or
 * process name before we can tell FT to inject itself into that process and enable testing.
 * Since a testing framework that is application-independent cannot be hard-coded to know the
 * desired processname, and cannot readily make assumptions about the correct Window handle or
 * process ID, we are going to effectively enable as many Win32 and .NET environments as we can
 * properly identify.
 * <p>
 * There is an issue, of course, when attempting to distribute and use this new feature in older
 * XDE Tester and RobotJ environments that do not have this functionality.  SAFS code attempting
 * to use it must be wrapped in try\catch(Throwable) blocks because XDE Tester and RobotJ will
 * NOT be able to properly load and initialize this class and they will issue an Error, not an
 * Exception.
 * <p>
 * This class uses external resource files (text files) to indicate which classes running on the
 * system are to be enabled, and which classes are to be ignored.  These files do not represent an
 * all-inclusive list and are subject to updates.  There are seperate text files for Windows and
 * Unix.  These text files are:
 * <ul>
 * <li>WindowsClassMap.dat
 * <li>WindowsIgnoreClassMap.dat
 * <p>
 * <li>UnixClassMap.dat
 * <li>UnixIgnoreClassMap.dat
 * </ul>
 * <p>
 * The WindowsClassMap and WindowsIgnoreClassMap files are provided in the distributed JAR file.
 * The Unix files are not yet distributed as their contents has not yet been deduced.
 * <p>
 * This class allows a user or tester to place an identically named resource file in the
 * same directory where the JAR file is found and that external file will add additional
 * mappings and ignores to those in the JAR file.
 * <p>
 * @author Carl Nagle  SEP 02, 2005
 * Copyright (C) (SAS) All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
public class DynamicEnabler {

	/** "WindowsClassMap.dat" **/
	public static final String DEFAULT_WINDOWS_CLASS_MAP        = "WindowsClassMap.dat";

	/** "WindowsIgnoreClassMap.dat" **/
	public static final String DEFAULT_WINDOWS_IGNORE_CLASS_MAP = "WindowsIgnoreClassMap.dat";

	/** "SWTClassMap.dat" **/
	public static final String DEFAULT_SWT_CLASS_MAP        	= "SWTClassMap.dat";

	/** "DotNetClassMap.dat" **/
	public static final String DEFAULT_DOTNET_CLASS_MAP        	= "DotNetClassMap.dat";

	// Unix support not yet coded or will use the same as for Windows
	/** "UnixClassMap.dat" **/
	public static final String DEFAULT_UNIX_CLASS_MAP           = "UnixClassMap.dat";
	/** "UnixIgnoreClassMap.dat" **/
	public static final String DEFAULT_UNIX_IGNORE_CLASS_MAP    = "UnixIgnoreClassMap.dat";

	/** FT 6.x Unique Class object for dynamic enabling **/
	static RootTestObject rto = null;

	/** Window classes or processes that should be ignored. **/
	static Vector ignorewins = new Vector();
	static boolean ignore_classmap_read = false;

	/** Window classes or processes that CAN be enabled. **/
	static Vector enablewins = new Vector();
	static boolean win_classmap_read = false;

	/** SWT classes that CAN be enabled. **/
	static Vector enableswt = new Vector();
	static boolean swt_classmap_read = false;

	/** .NET classes that CAN be enabled. **/
	static Vector enablenet = new Vector();
	static boolean net_classmap_read = false;

	/** Window classes or processes that HAVE already been enabled. **/
	static Vector winenabled = new Vector();

	static String swtmap    = DEFAULT_SWT_CLASS_MAP;
	static String mainmap   = DEFAULT_WINDOWS_CLASS_MAP;
	static String netmap	= DEFAULT_DOTNET_CLASS_MAP;
	static String ignoremap = DEFAULT_WINDOWS_IGNORE_CLASS_MAP;

	/** Strings. process names to enable. */
	static Vector enableprocs = new Vector();
	static Vector enabledomains = new Vector();

	static {

		//see if we should use Unix maps instead
		if(! org.safs.jvmagent.Platform.isWindows()){
			mainmap   = DEFAULT_UNIX_CLASS_MAP;
			ignoremap = DEFAULT_UNIX_IGNORE_CLASS_MAP;
		}
	}

	public static void clearEnabledProcs(){
		Log.info("DynamicEnabler clearing process names storage...");
		enableprocs.clear();
	}
	public static void clearEnabledDomains(){
		Log.info("DynamicEnabler clearing domain names storage...");
		enabledomains.clear();
	}
	public static void addEnabledProc(String procname){
		if(procname != null){
			Log.info("DynamicEnabler adding '"+ procname +"' to process names storage...");
			enableprocs.add(procname);
		}
	}
	public static boolean hasEnabledProc(){
		return enableprocs.size()>0;
	}
	public static boolean isEnabledProc(String procname){
		return enableprocs.contains(procname);
	}
	public static void addEnabledDomain(String domainname){
		if(domainname != null){
			Log.info("DynamicEnabler adding '"+ domainname +"' to domain names storage...");
			enabledomains.add(domainname.toUpperCase());
		}
	}
	public static boolean hasEnabledDomain(){
		return enabledomains.size()>0;
	}
	public static boolean isEnabledDomain(String domainname){
		return enabledomains.contains(domainname.toUpperCase());
	}
	public static Vector enableSWTWindows(){
		//find and load our swt class list for dynamic enabling
		try{
			if(! swt_classmap_read) {
				Log.info("DynamicEnabler loading SWT Class Map Data...");
				enableswt = appendMapVector(swtmap, enableswt);
				swt_classmap_read = true;
			}
		}
		catch(FileNotFoundException nf){;}
		//simpleEnableTopWindows(enableswt);
		simpleEnableTopWindows();
		return enableswt;
	}

	public static Vector enableWinWindows(){
		//find and load our WIN class list for dynamic enabling
		try{
			if(! win_classmap_read) {
				Log.info("DynamicEnabler loading WIN Class Map Data...");
				enablewins = appendMapVector(mainmap, enablewins);
				win_classmap_read = true;
			}
		}
		catch(FileNotFoundException nf){;}
		//simpleEnableTopWindows(enablewins);
		simpleEnableTopWindows();
		return enablewins;
	}

	public static Vector enableNetWindows(){
		//find and load our Net class list for dynamic enabling
		try{
			if(! net_classmap_read) {
				Log.info("DynamicEnabler loading .NET Class Map Data...");
				enablenet = appendMapVector(netmap, enablenet);
				net_classmap_read = true;
			}
		}
		catch(FileNotFoundException nf){;}
		//simpleEnableTopWindows(enablenet);
		simpleEnableTopWindows();
		return enablenet;
	}

	/**
	 * Returns all found matching classes in the provided domain as an ArrayList of
	 * TestObjects.
	 * As of RFT 2.0 the act of locating the matching class and converting it to a
	 * TestObject also "enables" that process.  So attempting to "enable" then "find"
	 * is a duplicated effort.
	 *
	 * @param classes
	 * @param domain
	 * @return ArrayList of TestObjects or an empty ArrayList
	 */
	public static ArrayList getRootTestObjectWindows(Vector classes){
		ArrayList testObjects = new ArrayList();
		rto = RootTestObject.getRootTestObject();
		IWindow[] iwins = rto.getTopWindows();
		Log.info("DynamicEnabler detects "+ iwins.length +" Top-Level Windows.");
		IWindow iwin = null;
		String classname = null;
		boolean enabled;
		for(int i=0;i<iwins.length;i++){
			iwin = iwins[i];
			iwin = iwin.getTopParent();
			classname = iwin.getWindowClassName();
			if(vectorContainsRegexMatch(classes, classname)){

				Long hwnd = new Long(iwin.getHandle());

				//this is bothersome. We need to be able to find by
				//additional means besides text.
				String text = iwin.getText();
				boolean matchText =(text!=null)&&(text.length()>0);

				Log.info("DynamicEnabler matching '"+ classname +"' caption: "+ text);
				enabled = iwin.isEnabled();
				if(!enabled) enabled = rto.enableForTesting(hwnd.longValue());
				Log.info("... successfully enabled for testing? "+ enabled);

				TestObject to[] = null;
				Log.info("... "+ classname +" proxy class: "+ iwin.getClass().getName());
				if(matchText){
					Log.info("... locating object by CaptionText...");
					to = rto.find(RationalTestScript.atChild(".text", new CaptionText(text)));
				}
				if( (to == null)||(to.length==0)) {
					Log.info("... locating object by handle and processId...");
					try{
						int pid = iwin.getPid();
						to = rto.find(RationalTestScript.atChild(".hwnd", hwnd, ".processId", new Integer(pid)));
					}catch(Exception x){
						Log.debug("...Ignoring Exception:", x);
					}
				}
				if(to == null){
					Log.info("... find returned null: matched 0 TestObjects.");
					return testObjects;
				}
				Log.info("... matched "+ to.length +" TestObjects.");
				try{ for(int j = 0;j< to.length;j++) {
					testObjects.add(to[j]); }
				}
				catch(Exception x){;}
			}
		}
		return testObjects;
	}

	/**
	 * Try to "find" the TopLevelWindow associated with a processID and return true if it is the topmost window--
	 * the one with keyboard focus.  In the process of "finding" the window the process should be enabled for
	 * testing.
	 * @param script
	 * @param processID
	 * @return true if the process has the window with the current keyboard focus.
	 * @see org.safs.rational.RDDGUIUtilities#getParentTestObject(TestObject)
	 */
	public static boolean isTopParentWinFocused(Script script, int processID){
		boolean focused = false;
		TopLevelTestObject parent = null;
		RootTestObject rto = script.getRootTestObject();
		TestObject[] wins = script.find(script.atList(
				                     script.atProperty(".processId", new Integer(processID)),
				                     script.atProperty(".domain", "Win")
				                     ), true);
		Log.debug("DynamicEnabler WIN process "+ processID +" matches "+ String.valueOf(wins.length)+" TestObjects.");
		for(int i=0;(i<wins.length && !focused);i++){
			parent = script.getGuiUtilities().getParentTestObject(wins[i]);
			focused = parent.hasFocus();
		}
		if (wins.length > 0) script.unregister(wins);
		return focused;
	}

	protected static void loadWinEnabled(){
		Log.info("DynamicEnabler.loadWinEnabled loading of winenabled Vector.");
		//don't do this too frequently

		//Carl Nagle 2009.08.25 believing this is causing instability
		//winenabled.clear();

		DomainTestObject[] domains = rto.getDomains();
		DomainTestObject idomain = null;
		TestObject[] winobjects = null;
		Integer pidI = null;
		Long pidL = null;
		Integer uiResources = null;
		TestObject itestobject = null;
		TestObjectReference itestref = null;
		String procname = null;
		final String PROCESS = "Process";
		for(int i=0;i< domains.length;i++){
			idomain = domains[i];
			if(idomain.getName().toString().equalsIgnoreCase(PROCESS)){
				Log.info("DynamicEnabler IGNORING '"+ idomain.getName()+"' domain...");
				idomain.unregister();
				continue;
			}
			if(hasEnabledDomain()){
				if(!isEnabledDomain(idomain.getName().toString())){
					Log.info("DynamicEnabler IGNORING undesired '"+ idomain.getName()+"' domain...");
					idomain.unregister();
					continue;
				}
			}
			if(idomain.getName().toString().equalsIgnoreCase(Domains.WIN_DOMAIN)){
				Log.info("DynamicEnabler evaluating WIN domain Mailslot "+ idomain.getTestContextReference().getMailslotName());
				if(RGuiObjectVector.isBannedWinDomain(idomain)){
					Log.info("DynamicEnabler skipping banned WIN domain mailslot for "+idomain);
					idomain.unregister();
					continue;
				}
			}
			if(hasEnabledProc()){
				pidL = new Long(idomain.getProcess().getProcessId());
				procname = (String) NativeWrapper.GetProcessFileName(new Integer(pidL.intValue()));
				if(procname==null) {
					Log.info("DynamicEnabler ignoring domain '"+ idomain.getName() +"' NULL process name; pId "+ pidL.intValue());
					idomain.unregister();
					continue;
				}
				if(!isEnabledProc(procname)){
					Log.info("DynamicEnabler IGNORING process "+ procname+" with pId "+pidL.intValue());
					idomain.unregister();
					continue;
				}
				Log.info("DynamicEnabler handling process '"+ procname +"' with pId "+ pidL.intValue()+" "+ idomain);
			}
        	//bypassing this call because we have run into the case where at least one
        	//machine is NOT properly returning USER object resource counts for windows
        	//known to have GUI components (i.e. CMD windows).
        	//It is uncertain if modifying the routine to look for GDI objects instead
        	//of USER objects will provide any filtering of undesirable domain objects.
			/* winobjects = getJNAProtectedDomainTopObjects(idomain); */

			// this getTopObjects call can sometimes take a long time--especially if resource
			// issues and testobject cleanup are starting to make RFT unstable.
			winobjects = idomain.getTopObjects();

			if(winobjects==null||winobjects.length==0){
				Log.info("DynamicEnabler IGNORING '"+ procname +"' "+ idomain.getName()+" Domain with null or empty window objects...");
				idomain.unregister();
				continue;
			}
			for(int j=0;j<winobjects.length;j++) {
				itestobject = winobjects[j];

				//Carl Nagle: new incidents of itestobject being null throwing NPE:
				try{ itestref = itestobject.getObjectReference();}
				catch(NullPointerException npx){
					Log.debug("...loadWinEnabled ignoring '"+ procname +"' NULL TopObject["+ j +"] returned from "+ idomain.getName() +" domain.");
					itestobject.unregister();
					continue;
				}
				if(itestref == null){
					Log.debug("...loadWinEnabled ignoring '"+ procname +"' NULL TestObject reference from "+ idomain.getName()+" domain object "+ itestobject);
					itestobject.unregister();
					continue;
				}
				int iref = 0;
				try{
					Reference ref = itestref.getTestContextReference();
					if(ref==null){
						Log.debug("...loadWinEnabled ignoring '"+ procname +"' NULL TestContext reference from "+ idomain.getName()+" domain test reference "+ itestref);
						itestobject.unregister();
						continue;
					}
					iref = ref.getProcessId();
					if(iref > 0){
						Log.info("...loadWinEnabled evaluating '"+ procname +"' process "+ iref);
						pidI = new Integer(iref);
						if( !winenabled.contains(pidI)){
							winenabled.add(pidI);
							Log.info("DynamicEnabler storing enabled '"+ idomain.getName()+"' "+ procname +" process with pId "+ iref +":"+pidL.intValue());
						}
					}else{
						Log.debug("...loadWinEnabled IGNORING '"+ procname +"' problematic process ID "+ iref +" on "+ itestobject);
					}
				}
				catch(Exception x){
					Log.debug("...loadWinEnabled getProcessId for '"+ procname +"' IGNORING "+ x.getClass().getSimpleName()+" on "+ itestobject);
				}
				// a BAD testobject will hit Exception
				try{
					// itestobject.exists() is hitting timeout but NOT throwing exception!
					// itestobject.isTopLevelTestObject() is returning FALSE on just about everything.
					//   isTopLevelTestObject does not return TRUE on TopLevelSubitemTestObjects of any type.
					//   I consider that a defect in RFT.
					itestobject.unregister();
				}catch(Exception x){
					Log.debug("...loadWinEnabled IGNORING '"+ procname +"' "+ x.getClass().getSimpleName()+" on "+ itestobject, x);
				}
			}
			idomain.unregister();
		}
		Log.info("DynamicEnabler.loadWinEnabled found "+ winenabled.size()+" unique processes enabled.");
	}

	protected static int enableUniqueWinsWithJNA(){
		Object[] iwins = NativeWrapper.EnumWindows();
		if(iwins != null) {
			Log.info("DynamicEnabler.enableWithJNA examining "+ iwins.length +" native Windows.");
		}else{
			Log.info("DynamicEnabler.enableWithJNA received NULL from EnumWindows...");
			return 0;
		}
		Long iwin = null;
		Object[] pids = null;
		boolean enabled = false;
		Integer pidI = null;
		String procname = null;
		int pidi = 0;
		int topcount = 0;
		if(enableprocs.size()>0)
    		Log.info("DynamicEnabler enableWithJNA limiting to specific process names...");

	    for(int d=0; d<iwins.length; d++){
	    	pidI = new Integer(0);
	    	pidi = 0;
	    	iwin = (Long)iwins[d];
	    	try{
		    	pids = NativeWrapper.GetWindowThreadProcessId(iwin);
	    		pidI = (Integer)pids[1];
	    		pidi = pidI.intValue();
	    		//we sometimes get PID info that is nonsense?!!
	    		if(pidi > 20000){
		    		Log.debug("DynamicEnabler enableWithJNA IGNORING probably bogus PID "+ pidi +" for Window "+String.valueOf(iwin.longValue()));
		    		continue;
	    		}
	    	}catch(Exception x){
	    		Log.debug("DynamicEnabler enableWithJNA IGNORING "+ x.getClass().getSimpleName()+" for Window "+String.valueOf(iwin.longValue()));
	    		continue;
	    	}
	    	if(pidi != 0){
				if (! winenabled.contains(pidI)){
					if(enableprocs.size()>0){
						procname = (String) NativeWrapper.GetProcessFileName(pidI);
						if(procname==null){
				    		Log.info("DynamicEnabler enableWithJNA skipping NULL processname for pId "+ pidi);
							continue;
						}
						if(!enableprocs.contains(procname)){
				    		Log.info("DynamicEnabler enableWithJNA skipping process "+ procname +" with pId "+ pidi);
							continue;
						}
			    		Log.info("DynamicEnabler enableWithJNA HANDLING process "+ procname +" with pId "+ pidi);
					}
					enabled = false;
					try{
						Log.info("DynamicEnabler enableWithJNA enabling process "+ procname +" with pId "+ pidi);
						rto.enableForTesting(pidi);
						winenabled.add(pidI);
						enabled=true;
						topcount++;
				    // Unix apparently does not support this PID stuff
					// according to Rational FT documentation.
					// So, until we know what to do on Unix....
					}catch(NotSupportedOnUnixException nx){
						Log.info("DynamicEnabler JNA handling "+ nx.getClass().getSimpleName());
						try{
							rto.enableForTesting(iwin.longValue());
							winenabled.add(pidI);
							enabled=true;
							topcount++;
						}
						catch(Exception x2){
							Log.debug("DynamicEnabler JNA failed to enable Window "+ iwin +" due to "+
									  nx.getClass().getSimpleName()+" followed by "+ x2.getClass().getSimpleName());
						}
					}catch(UnableToHookException nx){
						Log.info("DynamicEnabler enableWithJNA skipping process "+ procname +" due to "+ nx.getClass().getSimpleName());
						continue;
					}catch(WindowHandleNotFoundException nx){
			    		Log.info("DynamicEnabler enableWithJNA skipping process "+ procname +" due to "+ nx.getClass().getSimpleName());
						continue;
					}catch(Exception nx){
						Log.info("DynamicEnabler enableWithJNA enabling Window "+ iwin.toString() +" DESPITE "+ nx.getClass().getSimpleName());
						try{
							rto.enableForTesting(iwin.longValue());
							winenabled.add(pidI);
							enabled=true;
							topcount++;
						}
						catch(Exception x2){
							Log.debug("DynamicEnabler JNA failed to enable Window "+ iwin +" due to "+
									  nx.getClass().getSimpleName()+" followed by "+ x2.getClass().getSimpleName());
						}
					}
	 	 		    if (! enabled) {
	 	 		    	Log.info("DynamicEnabler JNA HAS NOT ENABLED Window: "+ iwin +" with pId: "+ pidI);
	 	 		    }else{
	 	 		    	Log.info("DynamicEnabler JNA enabled Window: "+ iwin +" with pId: "+ pidI);
	 	 		    }
	 		    }
	    	}else{
 		    	Log.info("DynamicEnabler JNA invalid PID "+ pidi +" for Window "+ iwin);
	    	}
		}
		Log.info("DynamicEnabler enableWithJNA found "+ topcount +" unique Windows to enable.");
	    return topcount;
	}


	private static boolean enableTopWindows = SafsROBOTJ.DEFAULT_DYNAMIC_ENABLE_TOP_WINS;

	public static void setEnableTopWindows(boolean enable){
		enableTopWindows = enable;
	}

	/**
	 * Dynamically enable all Top Windows or processes.
	 * However, if enableprocs contains one or more process names then we will only
	 * enable windows and processes matching the names in enableprocs.
	 *
	 * @return long number of top windows enabled or 0 if not successful.
	 * @see #getRootTestObjectWindows(Vector, String)
	 * @see #enableprocs
	 **/
	protected static long simpleEnableTopWindows(){
		int topcount = 0;
		if(!enableTopWindows){
			Log.info("DynamicEnabler simpleEnableTopWins has been disabled ...");
			return topcount;
		}
		rto = RootTestObject.getRootTestObject();
		IWindow[] iwins = rto.getTopWindows();
		IWindow pwin;
		IWindow owin;
		long hWnd;
		int pid;
		int ctrlid;
		long enabledhWnd;
		String classname;
		IWindow iwin;
		Integer pidI;
		Long hwndL;
		Rectangle rect;
		String procname;
		loadWinEnabled();
		if(enableprocs.size()>0)
    		Log.info("DynamicEnabler simpleEnableTopWins limiting to specific process names...");

	    for(int d=0; d<iwins.length; d++){
	    	iwin = iwins[d];
 		    classname = (String) iwin.getWindowClassName();

 		    pwin = iwin.getParent();
 		    owin = iwin.getOwner();
 		    hWnd = iwin.getHandle();
			pid = iwin.getPid();
 		    rect = iwin.getScreenRectangle();
			//ctrlid = iwin.getId();
			pidI = new Integer(pid);
			hwndL = new Long(hWnd);
 		    procname = null;

 		    Log.info("DynamicEnabler simpleEnableTopWins: find window with pid="+pid+"; classname="+classname+" .................  ");

 		    if(pid==0){
 		    	//The process whose pid is 0, is the 'System Idle process', after enable this process,
 		    	//it may block the program. As it is not necessay to enable this process, so just ignore it.
 		    	Log.info("================= Ignoring 'System Idle process', its pid="+pid+" continue!!! ");
 		    	continue;
 		    }

 		    //If the window's class name is not in the white list (WindowsClassMap.dat), we will not enable it.
 		    if(!vectorContainsRegexMatch(enablewins, classname)){
 		    	Log.info("DynamicEnabler simpleEnableTopWins: ignoring class '"+classname+"', its pid="+pid+", continue ...");
 		    	continue;
 		    }

 		    //if has no parent, but can be owned?
 		    //if ((pwin==null)&&(owin==null))

 		   if (! winenabled.contains(pidI)){
	 		    if(hasEnabledProc()){
	 		    	procname = (String) NativeWrapper.GetProcessFileName(pidI);
	 		    	//skip this iteration if the procname does not match
	 		    	//the one(s) we are limited to.
	 		    	if (procname==null ){
	 		    		Log.info("DynamicEnabler simpleEnableTopWins skipping NULL process name.");
	 		    		if(pwin instanceof TestObject) ((TestObject)pwin).unregister();
	 		    		if(owin instanceof TestObject) ((TestObject)owin).unregister();
	 		    		if(iwin instanceof TestObject) ((TestObject)iwin).unregister();
	 		    		continue;
	 		    	}
	 		    	if(!isEnabledProc(procname)) {
	 		    		Log.info("DynamicEnabler simpleEnableTopWins skipping process '"+ procname +"'");
	 		    		if(pwin instanceof TestObject) ((TestObject)pwin).unregister();
	 		    		if(owin instanceof TestObject) ((TestObject)owin).unregister();
	 		    		if(iwin instanceof TestObject) ((TestObject)iwin).unregister();
	 		    		continue;
	 		    	}
 		    		Log.info("DynamicEnabler simpleEnableTopWins HANDLING process '"+ procname +"'");
	 		    }
 		    	topcount++;
 				boolean notEnabled = true;
				// Handle for .NET does not seem to work on Windows
				// or, it is that there are multiple window handles
				// thus the process is getting enabled multiple times
				// and locking up Windows
				try{
					Log.info("==============   Try to enable pid="+pid+"; classname="+classname+" .................  ");
					rto.enableForTesting(pid); //ORIGINAL
					winenabled.add(pidI); // ORIGINAL
					notEnabled=false;
		 		    Log.info("DynamicEnabler ENABLED process containing: "+ classname +"; pId: "+ pid+"; hWnd: "+ hWnd+"; IWIN: "+iwin);
			    // Unix apparently does not support this PID stuff
				// according to Rational FT documentation.
				// So, until we know what to do on Unix....
				}catch(NotSupportedOnUnixException nx){
					Log.info("DynamicEnabler handling "+ nx.getClass().getSimpleName());
					try{
						rto.enableForTesting(hWnd);
						winenabled.add(pidI);
						notEnabled=false;
			 		    Log.info("DynamicEnabler ENABLED process containing: "+ classname +"; pId: "+ pid+"; hWnd: "+ hWnd+"; IWIN: "+iwin);
					}
					catch(Exception x2){
						Log.debug("DynamicEnabler failed to enable class "+ classname +" with pId "+ pid +" due to "+
								  nx.getClass().getSimpleName()+" followed by "+ x2.getClass().getSimpleName());
					}
				}catch(UnableToHookException nx){
					Log.warn("DynamicEnabler failed to enable class "+ classname +" with pId "+ pid +" due to "+
							  nx.getClass().getSimpleName());
				}catch(Exception nx){
					Log.info("DynamicEnabler attempting to enable DESPITE "+ nx.getClass().getSimpleName());
					try{
						rto.enableForTesting(hWnd);
						winenabled.add(pidI);
						notEnabled=false;
			 		    Log.info("DynamicEnabler ENABLED process containing: "+ classname +"; pId: "+ pid+"; hWnd: "+ hWnd+"; IWIN: "+iwin);
					}
					catch(Exception x2){
						Log.debug("DynamicEnabler failed to enable class "+ classname +" with pId "+ pid +" due to "+
								  nx.getClass().getSimpleName()+" followed by "+ x2.getClass().getSimpleName());
					}
				}
 		    }else{//some domain from the process HAS been enabled
 		    	Log.info("DynamicEnabler bypassing process "+pid+".  This classname: "+ classname +"; pId: "+ pid+"; hWnd: "+ hWnd+"; IWIN: "+iwin);
 		    }
    		if(pwin instanceof TestObject) ((TestObject)pwin).unregister();
    		if(owin instanceof TestObject) ((TestObject)owin).unregister();
    		if(iwin instanceof TestObject) ((TestObject)iwin).unregister();
		}
	    //DEBUGGING might be causing WIN and NET issues...
//	    topcount += enableUniqueWinsWithJNA();
	    return topcount;
	}


	/**
	 * Determine if a particular classname is one we know to ignore.  For example, a class that is
	 * always used by the true target class, like a tooltip class.
	 *
	 * @param classname of an object found within the list of all running classes.
	 * @return true if the provided classname exists in our ignorewins list.
	 */
    public static boolean ignoreTopWindow(String classname){
		return vectorContainsRegexMatch(ignorewins,classname);
	}

    /**
     * Attempt to match a target String to stored regex expressions.
     * The stored regex expressions are partial classnames in our case,
     * like SWT_Window.*.
     * @param item String to match to stored regex expressions.
     * @return true if the vector has a regex that matches the target string.
     */
    private static boolean vectorContainsRegexMatch(Vector store, String target){
    	Enumeration items = store.elements();
    	String source;
    	int sindex = -1;
    	boolean matched=false;
    	while((items.hasMoreElements())&&(!matched)){
    		try{
    			source = (String) items.nextElement();
    			sindex = source.indexOf(REGEX_WILDCARD);
    			if(sindex > 0) matched = target.startsWith(source.substring(0, sindex));
    			else matched = target.equals(source);
    			// matched = target.matches(source); // regex matching
    		}
    		catch(ClassCastException cc){;}
    		catch(java.util.regex.PatternSyntaxException ps){;}
    	}
    	return matched;
    }

    /** ".*" */
    static String REGEX_WILDCARD = ".*";
    /** "*" */
    static String GENERIC_WILDCARD = "*";


	/**
	 * Attempts to load Standard and Custom resource files for the DymamicEnabler.
	 * First we attempt to load the contents of the Standard resource in a JAR file.
	 * Then we determine the location of that JAR file and attempt to load the contents
	 * of a user-customized version of the same resource contained in the directory where
	 * the JAR file is located.
	 * @param resource JAR resource (like "WindowsClassMap.DAT") to seek.
	 * @param vector Vector containing classnames to Dynamically enable or suppress.
	 * @return Vector with potentially new classnames to enable or suppress.
	 * @throws FileNotFoundException
	 */
	private static Vector appendMapVector(String resource, Vector vector)throws FileNotFoundException{
		//find and load our valid class list for dynamic enabling
		URL mapurl = null;
		Properties classmap = new Properties();
		try{
			Log.info("DynamicEnabler try AgentClassLoader.locateResource");
			mapurl = AgentClassLoader.locateResource(resource);
		}catch(Exception x){
			Log.info("DynamicEnabler ignoring AgentClassLoader.locateResource "+ x.getClass().getSimpleName());
		}

		if (mapurl == null){
			try{
				Log.info("DynamicEnabler try DynamicEnabler.class.getResource");
				mapurl = DynamicEnabler.class.getResource(resource);}
			catch(Exception x){ Log.info("DynamicEnabler ignoring DynamicEnabler.class.getResource "+ x.getClass().getSimpleName());}
		}
		if (mapurl == null){
			try{
				Log.info("DynamicEnabler try getSystemResource");
				mapurl = ClassLoader.getSystemResource(resource);}
			catch(Exception x){ Log.info("DynamicEnabler ignoring getSystemResource "+ x.getClass().getSimpleName());}
		}
		if(mapurl == null){
			try{
				Log.info("DynamicEnabler trying AgentClassLoader with SAFSDIR Env...");
				String safsjar = System.getenv("SAFSDIR")+ File.separator +"lib"+ File.separator +"safs.jar";
				Log.info("DynamicEnabler safs.jar file path: "+ safsjar);
				AgentClassLoader loader = new AgentClassLoader(safsjar);
				mapurl = loader.getResource(resource);
			}catch(Exception x){ Log.info("DynamicEnabler ignoring location of safs.jar under SAFSDIR Env..."+ x.getClass().getSimpleName());}
		}
		if(mapurl == null) {
			Log.info("RFT DyanmicEnabler problem loading (or missing) StandardMap '"+ resource +"' file.");
		}else{
			Log.info("RFT DynamicEnabler StandardMap: "+ mapurl.getPath());
			classmap = FileUtilities.appendProperties(classmap, mapurl);
		}
		try{ if(mapurl==null) mapurl = DynamicEnabler.class.getProtectionDomain().getCodeSource().getLocation();}
		catch(Exception x){
			Log.info("RFT DynamicEnabler did not locate its JAR location.");
			Log.info("RFT DynamicEnabler cannot locate a custom version of "+ resource);
			throw new FileNotFoundException(resource);
		}
		try{
			URL mapurlcustom = AgentClassLoader.findCustomizedJARResource(mapurl, resource);
			// see if there are local class list customizations for dynamic enabling (Windows)
			// NullPointerException if custom mainmap not found
			Log.info("RFT DynamicEnabler customized resource: "+ mapurlcustom.getPath());
			classmap = FileUtilities.appendProperties(classmap, mapurlcustom);
		}
		catch(Exception anye){
			Log.info("Problem loading (or missing) custom '"+ resource +"' file.");
		}

		// init and fill enablewins
		Enumeration classes = classmap.elements();
		Object item = null;
		while (classes.hasMoreElements()){
			item = classes.nextElement();
			if (item != null) vector.add(item);
            Log.info("DynamicEnabler storing: "+item);
		}
		return vector;
	}

	/**
	 * Calls domain.getTopObjects() ONLY if the domain's process is determined to
	 * have active UI resources.  However, if there are any problems in determining
	 * if the process is using UI resources then we typically will just call
	 * domain.getTopObjects() anyways.
	 *
	 * @param domain
	 * @return
	 */
	public static TestObject[] getJNAProtectedDomainTopObjects(DomainTestObject domain){
    	TestObject[] wins = new TestObject[0];
    	if (domain == null){
    		Log.debug("DynamicEnabler cannot process a NULL domain.");
    		return wins;
    	}
    	try{
    		if(Platform.isWindows()){
    			int uis = 0;
    			boolean tried = false;
    			int pid = 0;
    			try{
            		pid = domain.getTestContextReference().getProcessId();
        			Integer iUI = (Integer) NativeWrapper.GetProcessUIResourceCount(new Integer(pid));
        			uis = iUI.intValue();
        		}catch(Exception xy){
    				Log.debug("DynamicEnabler IGNORING pId "+ pid +" JNA Exception:"+xy.getClass().getSimpleName()+":"+xy.getMessage());
    				tried = true;
            		return domain.getTopObjects();
        		}catch(Error xy){
    				Log.debug("DynamicEnabler IGNORING pId "+ pid +" JNA Error:"+xy.getClass().getSimpleName()+":"+xy.getMessage());
    				tried = true;
            		return domain.getTopObjects();
        		}
    			if(!tried){
    				if(uis > 0){
                		return domain.getTopObjects();
        			}else{
        				Log.debug("DynamicEnabler skipping domain '"+ domain.getName()+"' pId "+ pid +" due to JNA reporting 0 UI Resources.");
        			}
    			}
    		}else{
    			Log.info("DynamicEnabler reports non-Windows operating system handling...");
        		return domain.getTopObjects();
    		}
    	}
    	catch(Exception x){
    		Log.info("DynamicEnabler skipping domain '"+ domain.getName()+"' due to "+ x.getClass().getSimpleName()+":"+ x.getMessage());
    	}
    	return wins;
	}
}
