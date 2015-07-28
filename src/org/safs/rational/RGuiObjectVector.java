/** 
 * Copyright (C) (MSA, Inc) All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.safs.Domains;
import org.safs.GuiChildIterator;
import org.safs.GuiClassData;
import org.safs.GuiObjectRecognition;
import org.safs.GuiObjectVector;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.natives.NativeWrapper;
import org.safs.rational.ft.DynamicEnabler;

import com.rational.test.ft.WrappedException;
import com.rational.test.ft.object.interfaces.DomainTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.ITopWindow;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TopLevelSubitemTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexObjectTestObject;
import com.rational.test.ft.object.map.SpyMappedTestObject;
import com.rational.test.ft.sys.SpyMap;
import com.rational.test.ft.sys.TestContext.Reference;
import com.sun.jna.Platform;


/** 
 * @author Carl Nagle OCT 26, 2005 Catch unexpected Rational Exceptions in getParentObjects
 * @author Carl Nagle OCT 28, 2005 Catch another unexpected Rational Exceptions in getParentObjects
 * @author Carl Nagle JAN 30, 2006 Domain disabling added.
 * @author Bob Lawler AUG 29, 2006 Updated getParentObjects() to also check through the Owned objects
 *                             of each Top Object found.
 *        JunwuMa AUG 15, 2008 Add getTopTestObject() that returns a TestObject. Called by REngineCommandProcessor._highlightMatchingChildObject
 *        JunwuMa OCT 09, 2008 Add Flex domain support.
 *                             Open Flex domain in method getParentObjects(). 
 *                             Modify buildArray(ArrayList array, Object[] elements). For top TestObject found in Flex domain, it will be 
 *                             drilled down to find the REAL Flex application(top class) ignoring its classloader. 
 *                             buildArray only is called by getParentObjects().
 * LeiWang	Oct 14, 2008	Modified method isValidGuiObject(),we need to consider "System.Windows.Forms.ToolBarButton"
 * 							as a valid GuiObject, see defect S0539954.
 * JunwuMa  Oct 21, 2008    Added method getChildren(TestObject) and modified getChildObjects(Object parent) supporting
 *                          Flex object, eliminating the duplicate in Flex object's children. 
 *                          See {@link FlexUtil#getChildren(TestObject)}
 * LeiWang	Nov 05, 2008	Modified method getChildren(): If there are some problems on AUT itself (for example, if the AUT 
 * 									contains null pointer), the RFT WrappedException will be thrown. At this situation
 * 									I catch the exception and let our program continue to work. This modification will
 * 									cause some branchs not to be processed if the root node of that branch contains this 
 * 									kind of problem. See defect S0543032. 
 * LeiWang	Nov 07, 2008	Added method addTopWindowsFromDomainToWindowList().
 * 							Modified method getParentObjects(): Move some codes to new added method addTopWindowsFromDomainToWindowList().
 * 							Modified method getParentsByDomainName(): When add top windows to a List parents, use method 
 * 								addTopWindowsFromDomainToWindowList() instead of DynamicEnabler.getRootTestObjectWindows()
 * LeiWang	Nov 11, 2008	Modified method getParentsByDomainName(): add also those top windows got from DynamicEnabler.getRootTestObjectWindows() if
 * 								we are seeking for a popupmenu.
 * LeiWang	Nov 20, 2008	Modified method getParentsByDomainName(): If we enable SWT domain and DOTNET domain the same time. Both of them will be enabled dynamically,
 * 																	  in our program the top windows of SWT domain will be put to a List firstly, so these windows and 
 * 																	  windows owned by them (win.getOwnedObjects()) will be compared with the given RS 'Type=Window;Caption={List*}'.
 * 																	  And one of these windows match our RS.
 *																	  I guess a DOTNET window has two views for RFT one is under DOTNET domain, the other is under SWT domain.
 *																	  The view under DOTNET domain is what we want.
 *																	  Enable DotNet domain before Swt doamin. See defect S0549176
 * JunwuMa Dec 15, 2008     For supporting Flex in the migration from RFT7 to RFT8, modified getChildren(TestObject), removed the call that is 
 *                          used to eliminate the duplicate in Flex object's children. RFT8 enhances its API getChildren that supports Flex object well.
 * Carl Nagle  Jun 04, 2009     Updates with DynamicEnabler and JNA to catch some RFT hangs. 
 * Carl Nagle  Aug 07, 2009     Updates with DynamicEnabler to enable only a specific process with Process=. 
 * JunwuMa SEP 04, 2009     Update getChildren(TestObject) to support mapped class search mode.
 * JunwuMa OCT 26, 2009     Added method getCachedKeysByValue(Object) to get keys in Hashtable cache by value. 
 * Lei Wang  AUG 13, 2013     Modify method getParentsByDomainName() and addTopWindowsFromDomainToWindowList() to catch UserStoppedScriptError. 
 **/
public class RGuiObjectVector extends GuiObjectVector {

    // Domains class added
	static public final String DEFAULT_JAVA_DOMAIN_NAME = Domains.JAVA_DOMAIN;
	static public final String DEFAULT_HTML_DOMAIN_NAME = Domains.HTML_DOMAIN;
	static public final String DEFAULT_WIN_DOMAIN_NAME = Domains.WIN_DOMAIN;
	static public final String DEFAULT_NET_DOMAIN_NAME = Domains.NET_DOMAIN;
	static public final String DEFAULT_SWT_DOMAIN_NAME = Domains.SWT_DOMAIN;
	static public final String DEFAULT_FLEX_DOMAIN_NAME = Domains.FLEX_DOMAIN;	
	static public final String DEFAULT_ACTIVEX_DOMAIN_NAME = Domains.ACTIVEX_DOMAIN;	
	
	private String javaDomainName  = DEFAULT_JAVA_DOMAIN_NAME;
	private String htmlDomainName  = DEFAULT_HTML_DOMAIN_NAME;
	private String winDomainName   = DEFAULT_WIN_DOMAIN_NAME;
	private String netDomainName   = DEFAULT_NET_DOMAIN_NAME;
	private String swtDomainName   = DEFAULT_SWT_DOMAIN_NAME;
	private String flexDomainName  = DEFAULT_FLEX_DOMAIN_NAME;
	private String activeXDomainName  = DEFAULT_ACTIVEX_DOMAIN_NAME;
	

	private Script script     = null;
	
	/** @see deduceValidDomains */
	protected static Vector bannedWinMailslots = new Vector();

	private static String UC_MENUITEM="MENUITEM";
	
	static RGuiClassData classdata = new RGuiClassData();

	/**
	 * Calls the minimal RGuiObjectVector constructor. 
	 * Instances created with this minimal constructor MUST still have 
	 * windowName, childName, pathVector, and Script set prior to calling 
	 * initGuiObjectRecognition().
	 * 
	 * @see #setWindowName(String)
	 * @see #setChildName(String)
	 * @see #setPathVector(String)
	 * @see #script 
	 * @see #initGuiObjectRecognition()
	 */
	public RGuiObjectVector(){
		super();
	}

	
	/**
	 * Calls the RGuiObjectVector constructor. 
	 * Instances created with this constructor MUST still have 
	 * Script set prior to calling initGuiObjectRecognition().
	 * 
	 * @param window
	 * @param child
	 * @param pathString
	 * 
	 * @see #script
	 * @see #initGuiObjectRecognition()
	 * @see GuiObjectVector#GuiObjectVector(String, String, String)
	 * @see GuiObjectVector#initGuiObjectRecognition()
	 */
	public RGuiObjectVector(String window, String child, String pathString){
		super(window, child, pathString);
	}

	
	/**
	 * Calls the GuiObjectVector constructor and then initGuiObjectRecognition(). 
	 * @param window
	 * @param child
	 * @param pathString
	 * @param script
	 * @see GuiObjectVector#GuiObjectVector(String, String, String)
	 * @see GuiObjectVector#initGuiObjectRecognition()
	 */
	public RGuiObjectVector(String window, String child, String pathString, Script script){
		super(window, child, pathString);
		this.script = script;
		initGuiObjectRecognition();
	}

	/**
	 * Required when alternate constructors are used.  The script MUST be set prior to
	 * functional use of this object.
	 * @param script
	 */
	public void setScript(Script script){
		this.script = script;
	}

	/**
	 * Provide our RGuiChildIterator instance as required. 
	 * @see GuiObjectVector#createGuiChildIterator(Object, GuiObjectVector, List)
	 */
	public GuiChildIterator createGuiChildIterator(Object aparent, GuiObjectVector govVector, java.util.List gather){
		return new RGuiChildIterator(aparent, govVector, gather);
	}

	/**
	 * Provide our RGuiChildIterator instance as required. 
	 * @see GuiObjectVector#createGuiChildIterator(List)
	 */
	public GuiChildIterator createGuiChildIterator(java.util.List gather){
		return new RGuiChildIterator(gather);
	}

	/**
	 * Provide our RGuiObjectRecognition instance as required during initialization.
	 * @see GuiObjectVector#createGuiObjectRecognition(String) 
	 */
	public GuiObjectRecognition createGuiObjectRecognition(String subpath, int govLevel){
		return new RGuiObjectRecognition(subpath, script, govLevel);
	}

    /** Return our RGuiClassData subclass as required. */
    public GuiClassData getGuiClassData(){ return new RGuiClassData(); }
    
	/** Return the constructor-stored Script object. */			
	public Script getScript()       { return script;     }

	/** 
	 * Casts the GuiObjectRecognition from getChildGuiObjectRecognition to
	 * our subclass.
	 * @see GuiObjectVector#getChildGuiObjectRecognition(int)
	 */
	public RGuiObjectRecognition getChildRecognition(int index){
		return (RGuiObjectRecognition) getChildGuiObjectRecognition(index);
	}
	
	/** Must be set by getTopTestObject prior to any call to getMatchingParentObject().*/
	DomainTestObject domain = null;

	// used by getParentObjects
	private ArrayList buildArray(ArrayList array, Object[] elements, String domainname){
		if (array==null){//then elements and domainname should also be null
			Log.info("RGOV.initializing top object array...");
			if ((elements == null)||(elements.length == 0))	return new ArrayList();
			array = new ArrayList(elements.length * 2);
		}
		Log.info("RGOV.evaluating "+ elements.length +" top objects in top object array.");
		String objclass = null;
		TestObject topobj = null;
		TestObject parobj = null;
		for(int i=0;i < elements.length;i++) {
			topobj = (TestObject) elements[i];
			if (topobj == null) {
				Log.debug("RGOV.ignoring null object in array...");
				continue;
			}			
			// support Flex domain			
			//if(isFlexDomain(topobj)){ //throws Exception in .NET WinForm!!!
			if(domainname.equalsIgnoreCase(Domains.FLEX_DOMAIN)){
				// get the REAL Flex Application other than Flex 'runtimeloading' or FlexLoader 
				topobj = FlexUtil.drillDownRealFlexObj(topobj);
				if(topobj == null){
					Log.debug("RGOV.evaluating RealFlexObj returns null...");
					continue;
				}
			}
			array.add(topobj);
		}
		return array;
	}

	/** 
	 * return true if bannedWinMailslots contains this domains mailslot id. 
	 * The domain is assumed to be a WIN domain.  No domain name check is made.
	 **/
	public static boolean isBannedWinDomain(DomainTestObject domain){
		return bannedWinMailslots.contains(domain.getTestContextReference().getMailslotName());
	}
	
	/**
	 * Attempts to remove WIN domains that are likely bad associations with other domains like NET 
	 * and, maybe, HTML.  A "bad association" is assumed to be when a WIN domain communication 
	 * MailSlot ID is the same as another domain.  Brief experience suggests that a WIN domain 
	 * with the same Mailslot ID as a Net domain will hang or freeze RFT and produce ChannelSend 
	 * Timeout exceptions.  These usually last up to 2 minutes for each "bad" TestObject found 
	 * in the "bad" WIN domain.  
	 * 
	 * This brief experience is on RFT V8.1.1.1 as of MAY 2010.
	 * However, additional testing has shown that Excel has Net domains even when no 
	 * Net domain window is exposed or open.  Thus, Excel will NOT be found if this 
	 * function is used AND the desired target is the Excel window itself.
	 * 
	 * @param domains[]
	 * @return domains[] with potentially invalid domains removed
	 */
	protected DomainTestObject[] deduceValidDomains(DomainTestObject[] domains){
		Hashtable validdomains = new Hashtable();
		Vector extradomains = new Vector();
		RDomainInfo info = null;
		for(int i=0;i<domains.length;i++){
			info = new RDomainInfo(domains[i]);
			if(DynamicEnabler.hasEnabledDomain()&& ! DynamicEnabler.isEnabledDomain(info.getDomainname())){
				Log.debug("RGOV.deduceValidDomain skipping GOR disabled "+ info.getDomainname() +" domain.");
				continue;
			}
			if(validdomains.containsKey(info.getMailslot())){
				RDomainInfo old = (RDomainInfo) validdomains.get(info.getMailslot());
				if(! old.getDomainname().equals(info.getDomainname())){
					if(old.getDomainname().equalsIgnoreCase(Domains.WIN_DOMAIN)){
						Log.debug("RGOV.deduceValidDomain replacing Mailslot "+info.getMailslot()+" WIN domain with "+ info.getDomainname()+" domain.");
						validdomains.put(info.getMailslot(), info);
						
						if(! bannedWinMailslots.contains(info.getMailslot())) bannedWinMailslots.add(info.getMailslot());
					
					}else if(info.getDomainname().equalsIgnoreCase(Domains.WIN_DOMAIN)){
						Log.debug("RGOV.deduceValidDomain ignoring Mailslot "+ info.getMailslot()+" WIN domain in favor of "+ old.getDomainname()+" domain.");						
						
						if(! bannedWinMailslots.contains(info.getMailslot())) bannedWinMailslots.add(info.getMailslot());
					
					}else{
						Log.debug("RGOV.deduceValidDomain DUPLICATE NON-WIN MAILSLOT "+ info.getMailslot() +
								  " for "+ info.getDomainname()+" will be retained...");
						extradomains.add(domains[i]);
					}
				}else{
					Log.debug("RGOV.deduceValidDomain UNEXPECTED DUPLICATE MAILSLOTS "+ info.getMailslot() +
							  " for two "+ info.getDomainname()+" domains.");
					Log.debug("RGOV.deduceValidDomain DOMAIN 1: "+ old.getDomain());
					Log.debug("RGOV.deduceValidDomain DOMAIN 2: "+ info.getDomain());
					Log.debug("RGOV.deduceValidDomain ONLY DOMAIN 1: is retained.");
				}
			}else{
				Log.info("RGOV.deduceValidDomains adding "+ info.getDomainname()+" domain using Mailslot "+ info.getMailslot());
				validdomains.put(info.getMailslot(), info);
			}
		}
		if(validdomains.size()==0) return new DomainTestObject[0];

		int size = extradomains.size() + validdomains.size();
		DomainTestObject[] rc = new DomainTestObject[size];
		int index = 0;
		Enumeration valid = validdomains.elements();
		while(valid.hasMoreElements()){
			info = (RDomainInfo) valid.nextElement();
			rc[index++] = info.getDomain();
		}
		Enumeration extra = extradomains.elements();
		while(extra.hasMoreElements()){	rc[index++] = (DomainTestObject) extra.nextElement(); }		
		return rc;
	}
	/**
	 * Retrieve ALL parent objects from ALL domains matching the given domainname.
	 * Example: "Java" or "Net" etc.
	 * @param domainname String name of the domain type (like "Java") to
	 *         retrieve.
	 * @return Object array of ALL parent objects from all matching domains.
	 * May return null if none are found.
	 * NOV 06, 2008		(LeiWang)	After enable windows of some domains, we will try
	 * 								to get their parents from the DomainTestObject.
	 * 								The method DynamicEnabler.getRootTestObjectWindows
	 * 								will return some native windows of opreation system which
	 * 								wrapped the window of certain domain.
	 * 								Take a .NET window as example:
	 * 								1. Use DynamicEnabler.getRootTestObjectWindows to get windows
	 * 								  	We will get window's RS as Class=WindowsForms10.Window.8.app.0.378734a,
	 * 									and we will also get many native children like Maxiam, Minimum, ScrolBar etc.
	 * 								2. Use DomainTestObject to get windows
	 * 									We will get window's RS as Type=DotNetWindow, which means the matched window's
	 * 									class is System.Windows.Forms.Form and is mapped to type "DotNetWindow" defined by us.
	 * 									The number of children of this window is much smaller, they are children of 
	 * 									window System.Windows.Forms.Form which are the objects that we really want.
	 * 								
	 * 								So I will keep only those windows got from DomainTestObject.
	 * 								If we need really the windows from DynamicEnabler.getRootTestObjectWindows(),
	 * 								We can add them to the list also. I will not include them for now.
	 */
	protected ArrayList getParentsByDomainName(ArrayList parents, String domainname){
		Log.info("RGOV: Processing "+ domainname +" domain...");
		String realdomain = domainname;
		String displaydomain = domainname;
		Vector domainclasses = null;
		try{
			if(domainname.equalsIgnoreCase(Domains.SWT_DOMAIN)) {
				displaydomain = Domains.SWT_DOMAIN +":"+ Domains.WIN_DOMAIN;
				realdomain = Domains.WIN_DOMAIN;
				domainclasses = DynamicEnabler.enableSWTWindows();
			}
			if(domainname.equalsIgnoreCase(Domains.WIN_DOMAIN)) {
				displaydomain = Domains.WIN_DOMAIN +":"+ Domains.WIN_DOMAIN;
				domainclasses = DynamicEnabler.enableWinWindows();
			}
			if(domainname.equalsIgnoreCase(Domains.NET_DOMAIN)){
				displaydomain = Domains.NET_DOMAIN+":"+Domains.NET_DOMAIN;
				domainclasses = DynamicEnabler.enableNetWindows();
			}			
		}
		catch(Exception x){Log.debug("RFT DynamicEnabler Exception:", x);}

		//After calling DynamicEnabler.enableXXXWindows(), script.getDomains() should return 
		//the domain which contains the application window that we want
		//Firstly we add windows got from DomainTestObject
		DomainTestObject[] domains = script.getDomains();
		
		//DEBUGGING
		//domains = deduceValidDomains(domains);
		Log.debug("Got "+domains.length+" domains.");
		DomainTestObject adomain = null;
		String adomainname = null;
		for (int i = 0; i < domains.length; i++) {
			adomain = domains[i];
			adomainname = (String) adomain.getName();
			Log.debug("Try domain "+adomainname);
			try {
				if (adomainname.equalsIgnoreCase(realdomain)) {
					parents = addTopWindowsFromDomainToWindowList(adomain, parents);
				}
			} catch (Throwable any) {
				Log.debug("RGOV. error getting Top Objects from "+ displaydomain+"; Met "+any.getClass().getSimpleName()+": "+any.getMessage());
			}
		}
		
		//Maybe we need also add the window got from DynamicEnabler.getRootTestObjectWindows(domainclasses)
		if(isSeekingPopupMenu()){
			if (domainclasses != null && domainclasses.size() > 0) {
				if (parents == null) {
					parents = buildArray(parents, null, null);
				}
				ArrayList tops = DynamicEnabler
						.getRootTestObjectWindows(domainclasses);
				for (int i = 0; i < tops.size(); i++) {
					parents.add(tops.get(i));
				}
			}
		}
		// Fixed S0532729. Returning "null" if parents==null.
		Log.info("RGOV. returning "+ (parents!=null? parents.size():0) +" Top Objects from "+ displaydomain);
		return parents;
	}
	
	/**
	 * Tries to see if we have an RFT Mapped Object before doing the standard search. 
	 * This is primarily when used by STAF Process Container.
	 * @return Object or null if no match found. Will be a cached key if MODE_EXTERNAL_PROCESSING.
	 */
	public Object getMatchingParentObject(){
		TestObject to = null;
		if (path.isEmpty()){
			Log.info("RGOV.getMatchingParentObject no parent path information available.");
			return null;
		}
		if (path.size()>1){
			Log.info("RGOV.getMatchingParentObject parent path not Mappable...");
			return super.getMatchingParentObject();
		}
		try{ 
			to = new GuiTestObject(script.getMappedTestObject(pathVector));
			if (to != null){
				try{
					if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
						Log.info("RGOV.getMatchingParentObject converting parent to unique key for EXTERNAL_PROCESSING...");
						resetExternalModeCache();
						String key = makeUniqueCacheKey(to);
						putCachedItem(key, to);
						return key;
					}else{
						Log.info("RGOV.getMatchingParentObject using parent as-is for INTERNAL_PROCESSING...");
						return to;
					}
				}
				catch(Exception npe){ 
					Log.info("RGOV.getMatchingParentObject trying to ignore "+ npe.getClass().getSimpleName()+":"+npe.getMessage());
				}
			}
		}
		catch(Exception e){
			Log.info("RGOV.getMatchingParentObject Parent NOT GuiTestObject retrievable from Object Map: "+ pathVector);
		}
		return super.getMatchingParentObject();
	}
	
	
	/** 
	 * Return an array representing all known window objects.
	 * This requires the "domain" be set by getTopTestObject, or some other 
	 * means, prior to the call.  
	 * If domain is not set, then parent objects from ALL supported 
	 * domains will be returned.  This can impact performance.
	 * If getProcessMode()==MODE_EXTERNAL_PROCESSING then the routine will 
	 * reset the internal cache and store all objects in the cache returning 
	 * the keys for the objects instead of the objects themselves.
	 * @author Carl Nagle OCT 26, 2005 Catch unexpected Rational Exceptions
	 * @author Carl Nagle OCT 28, 2005 Catch another unexpected Rational Exception
	 * @author Carl Nagle JAN 30, 2006 Only return from Domains we have enabled.
	 * @author Bob Lawler AUG 29, 2006 Updated to also check through the Owned objects
	 *                             of each Top Object found.
	 * @see GuiObjectVector#getParentObjects()
	 * @see GuiObjectVector#getProcessMode()
	 * @see GuiObjectVector#convertToKeys(Object[])
	 **/
	public Object[] getParentObjects(){
	
		ArrayList parents = null;
		Log.info("RGOV:domain="+domain);

		if (domain == null) {
			Log.info("RGOV: evaluating enabled Domains...");
			if (Domains.isJavaEnabled()) parents = getParentsByDomainName(parents, DEFAULT_JAVA_DOMAIN_NAME);
			if (Domains.isHtmlEnabled()) parents = getParentsByDomainName(parents, DEFAULT_HTML_DOMAIN_NAME);
			if (Domains.isNetEnabled())  parents = getParentsByDomainName(parents, DEFAULT_NET_DOMAIN_NAME);			
			if (Domains.isSwtEnabled())  parents = getParentsByDomainName(parents, DEFAULT_SWT_DOMAIN_NAME);
			if (Domains.isWinEnabled())  parents = getParentsByDomainName(parents, DEFAULT_WIN_DOMAIN_NAME);
			if (Domains.isFlexEnabled())  parents = getParentsByDomainName(parents, DEFAULT_FLEX_DOMAIN_NAME);
		}
		else{			
			parents = addTopWindowsFromDomainToWindowList(domain, parents);
		}
		
		try{
			if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
				Log.info("RGOV converting parents to unique keys for EXTERNAL_PROCESSING...");
				resetExternalModeCache();
				return convertToKeys(parents.toArray());
			}else{
				Log.info("RGOV using parents as-is for INTERNAL_PROCESSING...");
				return parents.toArray();
			}
		}
		catch(NullPointerException npe){ 
			Log.info("RGOV. top objects array is NULL.");
			return null;}
	}
	
	/** 
	 * Return an array representing all known window objects in the specified domain.
	 * If getProcessMode()==MODE_EXTERNAL_PROCESSING then the routine will 
	 * reset the internal cache and store all objects in the cache returning 
	 * the keys for the objects instead of the objects themselves.
     * @param domainname should be one of the supported org.safs.Domains constants like 
     * "Java", "Html", "Win", etc..
	 * @see GuiObjectVector#getDomainParentObjects(String)
	 * @see GuiObjectVector#getProcessMode()
	 * @see GuiObjectVector#convertToKeys(Object[])
	 **/
	public Object[] getDomainParentObjects(String domainname){
	
		ArrayList parents = null;
		Log.info("RGOV:domainParents for domain: "+domainname);

		parents = getParentsByDomainName(parents, domainname);
		
		try{
			if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
				Log.info("RGOV converting parents to unique keys for EXTERNAL_PROCESSING...");
				resetExternalModeCache();
				return convertToKeys(parents.toArray());
			}else{
				Log.info("RGOV using parents as-is for INTERNAL_PROCESSING...");
				return parents.toArray();
			}
		}
		catch(NullPointerException npe){ 
			Log.info("RGOV. top objects array is NULL.");
			return null;}
	}
	
	protected boolean isOwnerSameTestContext(TestObject owner, TestObject owned){
		boolean valid = owned.getObjectReference().getTestContextReference().equals(owner.getObjectReference().getTestContextReference());
		if(! valid)	Log.info("    BAD Owned Window is not same TestContext as Owner.");
		return valid;
	}
	
	/**
	 * <b>Note:</b> This method will add the top windows of a domain and also 
	 * 				the owned windows by these top windows.
	 * @param domain		An ojbect of class DomainTestObject which represents Java, Html, Net,Win ect.
	 * @param windowList	A List contains those found windows
	 */
	protected ArrayList addTopWindowsFromDomainToWindowList(DomainTestObject domain,ArrayList windowList){
		String debugmsg = "RGOV.getTopWindowsFromDomain() ";
		String procname = null;
		String domainproc = null;
		Long pidL = null;
		Integer pidI = null;
		RDomainInfo info = new RDomainInfo(domain);
		
		//DEBUGGING
		//if(info.getDomainname().equalsIgnoreCase(Domains.WIN_DOMAIN)){			
		//	Log.info(debugmsg +"evaluating WIN domain Mailslot "+ info.getMailslot());
		//	if(isBannedWinDomain(domain)){
		//		Log.info(debugmsg +"skipping banned WIN domain mailslot for "+domain);
    	//		return windowList;
		//	}
		//}
        try {
        	procname = getParentGuiObjectRecognition().getProcessValue();
        	if(procname != null){
        		pidL = new Long(domain.getProcess().getProcessId());
        		domainproc = (String) NativeWrapper.GetProcessFileName(new Integer(pidL.intValue()));
        		if (domainproc == null || !procname.equals(domainproc)) {
                	Log.info(debugmsg+"skipping "+domain.getName()+" with process name '"+ domainproc +"'");
        			return windowList;
        		}
        	}
        	Log.info(debugmsg+" Evaluating windows of domain "+domain.getName());
        	// only do parents in the set domain, but...
        	// ... for each parent found, need to also include its owned objects
    		
        	//bypassing this call because we have run into the case where at least one 
        	//machine is NOT properly returning USER object resource counts for windows 
        	//known to have GUI components (i.e. CMD windows).
        	//It is uncertain if modifying the routine to look for GDI objects instead 
        	//of USER objects will provide any filtering of undesirable domain objects.
        	/* ***TestObject[] wins = DynamicEnabler.getJNAProtectedDomainTopObjects(domain);*** */
        	
        	TestObject[] wins = null;
        	//some WIN and FLEX domains (others?) throw WrappedExceptions
        	try{ wins = domain.getTopObjects();}catch(WrappedException w){
        		Log.info(debugmsg+ " ignoring a common WrappedException...");
        	}
        	if(wins==null||wins.length==0) return windowList;
        	
        	//Add topObjects to our window list
        	Log.info(debugmsg+ " Checking "+ wins.length+" Parents (TopObjects)");
        	windowList = buildArray(windowList, wins, domain.getName().toString());
        	
        	//Add the owned windows of each topObject
        	for(int j=0; j < wins.length; j++){
				try{
					TestObject owned = wins[j];
					if (owned == null){
						Log.info(debugmsg + " ignoring NULL Parent reference...");
						continue;
					}
					
					//getObjectClassName can produce hang or freeze in incomplete objects
					//how do we find out the object might be bad before we try to use it?
					//see deduceValidDomains for current attempts at this.
					Log.info(debugmsg +" Parent Class: "+ owned.getObjectClassName());
					TestObject[] owns = owned.getOwnedObjects();
					if((owns == null)||(owns.length==0)){
						Log.info(debugmsg + " ignoring empty array of owned objects...");
						continue;
					}
					Log.info(debugmsg+" Listing "+ owns.length+" Owned Windows");
					for(int k=0; k < owns.length; k++){
						TestObject ownedwin = null;
						try{ 
							ownedwin = owns[k];// ??? try to get the objects in-place???
							if(isOwnerSameTestContext(owned, ownedwin)){
								//getObjectClassName can produce hang or freeze in incomplete objects
								Log.info("    Owned Class: "+ ownedwin.getObjectClassName());
							}else{
								owns[k] = null;//try to prevent BAD owned win from being processed
								continue;
							}
						}catch(Exception e){
							Log.info("    Igoring Owned Class: "+ e.getClass().getSimpleName()+" "+ e.getMessage());
						}
					}
					//Add these owned windows to our window list
					//passing domain name *assumes* all owned windows are in the same domain.
					//this is something we are not certain of at this time.
					windowList = buildArray(windowList, owns, domain.getName().toString());
				}catch(Exception x){
		        	Log.debug(debugmsg+" inner "+x.getClass().getSimpleName()+" ocurred. Ignore and continue looping...",x);
				}
			}		            
        } catch (Throwable e) {
            //1. The domain.getTopObjects() call throws a WrappedException when
            //   trying to work with some "Win" and "Flex" objects.  ignore and move on.
        	//2. When using SAFS/RFT with WRS (HTML app), a curious exception occur, ignore it.
        	//   com.rational.test.ft.UserStoppedScriptError: "Hot key was pressed"
        	Log.debug(debugmsg+" outer exception ocurred "+e.getClass().getSimpleName()+":"+e.getMessage()+". Ignore and return windowList.");
        }
        return windowList;
	}
	
	/** 
	 * Return an array representing all known window objects.
	 * This requires the "domain" be set by getTopTestObject, or some other 
	 * means, prior to the call.
	 * If getProcessMode()==MODE_EXTERNAL_PROCESSING then the routine will 
	 * store all objects in the cache returning 
	 * the keys for the objects instead of the objects themselves.
	 * @param parent - will convert from key if MODE_EXTERNAL_PROCESSING
	 * @return array of objects or an empty array of new Object[0].
	 * */
	public Object[] getChildObjects(Object parent){
	
		if (parent == null) return new Object[0];
		if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
			Log.info("RGOV converting children to unique keys for EXTERNAL_PROCESSING...");
			try{
				TestObject to = (TestObject)getCachedItem(parent);
			    String classname = to.getObjectClassName();// 2 min. timeout if TO is bad...
			    String alttype = getGuiClassData().getMappedClassType(classname, to);
			    
			    if (alttype == null) {
			    	return convertToKeys(getChildren(to));
			    }			    
			    String compType = GuiClassData.deduceOneClassType((String)to.getDomain().getName(), alttype);
			    String uccomp = compType.toUpperCase();
			    
			    boolean fullChildrenPath = ! ((uccomp.endsWith(UC_MENUITEM))   ||
			    		                      (uccomp.startsWith(UC_MENUITEM)));
			    
			    if (! GuiObjectRecognition.isIgnoredTypeChild(compType)) {
			    	TestObject[] children = (fullChildrenPath ? getChildren(to) : to.getMappableChildren());
			    	
			    	try{
				    	// try alternate methods of getting the children for some components
			    		if((children.length == 0)&&
						  ((compType.equalsIgnoreCase("Menu"))||
						   (compType.equalsIgnoreCase("JavaMenu"))||
						   (compType.equalsIgnoreCase("MenuItem")))){

			    			// try to get Java menu items
			    			children = (TestObject[]) to.invoke("getMenuComponents");
			    		}
			    	}
			    	catch(Exception nullpointer){ /* ignore it */ }
			    	if((children==null)||(children.length==0)) return new Object[0];
			    	return convertToKeys(children);
			    }
				
		    	return convertToKeys(getChildren(to));
			}
			catch(ClassCastException cce){
				Log.debug("RGOV.getChildObjects cached parent was NOT a TestObject!");
				return new Object[0];
			}
			catch(Exception x){
				Log.debug("RGOV.getChildObjects Exception.",x);
				return new Object[0];
			}
		}else{
			Log.info("RGOV using children as-is for INTERNAL_PROCESSING...");
	    	return getChildren((TestObject)parent);
		}
	}
	
	/* called by this.getChildObjects(Object) for supporting Flex domain. 
	 * RFT8 enhances its API getChildren that supports Flex object well than in RFT7. It is no need to eliminate the 
	 * duplicate by calling FlexUtil.getChildren(flexobj)like in RFT7 we did before.
	 * */
	private TestObject[] getChildren(TestObject obj){
		String debugmsg = getClass().getSimpleName()+".getChildren() ";
		TestObject[] objects = {};
		try{
			if (isMappedClassSearchMode())
				objects = obj.getMappableChildren();  // mapped-class-search-mode
			else
				objects = obj.getChildren();          
							
		}catch(Exception e){
			Log.debug(debugmsg+" Exception occured. "+e.getClass().getSimpleName()+":"+e.getMessage(),e);
			//e.printStackTrace();
		}
		return objects;
	}
	
    /** Return true if the object is a GuiTestObject.
     * @param object - will convert from key if MODE_EXTERNAL_PROCESSING.
     * @see GuiObjectVector#isValidGuiObject(Object) 
     **/	
    public boolean isValidGuiObject(Object object){
    	Object item = object;
    	if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
        	item = getCachedItem(object);
    	}
    	//It seems that RFT does not consider class "System.Windows.Forms.ToolBarButton"
    	//as a GuiTestObject. But we really need this class to be considered a GuiObject.
    	//We need to click the button contained in the ToolBar. See defect S0539954.
    	if(item instanceof TestObject){
    		TestObject to = (TestObject) item;
    		if(RGuiObjectVector.isDotnetDomain(to)){
    			try {
					if(DotNetUtil.isSubclassOf(DotNetUtil.getClazz(to),DotNetUtil.CLASS_TOOLBARBUTTON_NAME)){
						return true;
					}
				} catch (SAFSException e) {
					Log.debug(e.getMessage());
					return false;
				}
    		}
    	}
    	return (item  instanceof GuiTestObject);
    }
    
    /** Return true if the object is a gui container.
     * @param object - will convert from key if MODE_EXTERNAL_PROCESSING.
     * @see GuiObjectVector#isValidGuiContainer(Object) 
     **/	
    public boolean isValidGuiContainer(Object object){
        boolean isContainerType = false;
        TestObject child;
        try{
        	if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
            	child = (TestObject) getCachedItem(object);
        	}else{
        		child = (TestObject) object;
        	}
        	String classname = child.getObjectClassName();
            String alttype = 
                   classdata.getMappedClassType(classname, child);
        	if (alttype!=null) {
        		isContainerType = GuiObjectRecognition.isContainerType(alttype);
        		// How do we know the container is invisible?
  	            if (isContainerType) Log.info("Is Container: "+ classname);
        	}
        }
        catch(ClassCastException ccx){;}
    	return isContainerType;
    }
    
	/**
	 * Call this routine instead of getMatchingParentObject() directly.
	 * This routine must set the Rational DomainTestObject domain and then 
	 * invokes getMatchingParentObject().  
	 * <p>
	 * It also casts the returned object to GuiTestObject--a subclass of 
	 * TestObject.
	 * @see GuiObjectVector#getMatchingParentObject()
	 * @see GuiTestObject
	 */
	public TestObject getTopTestObject (DomainTestObject domain){
		
		String domainname;
		try{ domainname = ((String) domain.getName()).toUpperCase();}
		catch(NullPointerException npe){ return null; }
		
		// validate domain type (Java, etc.)
		if(! Domains.isDomainSupported(domainname)){
		 	Log.info("RGOV: Unsupported DomainTestObject.");
		 	return null;
		}	
		this.domain = domain; // Carl Nagle: this may no longer be needed?
		try{
			if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
				return (GuiTestObject)getCachedItem(getMatchingParentObject());
			}else{
				return (GuiTestObject) getMatchingParentObject();
			}
		}
		catch(Exception ccx){
			Log.debug("RGOV.getTopObjects(Domain) IGNORING "+ ccx.getClass().getSimpleName());
			return null;
		}
	}
	
	/**
	 * Find the one parent object that matches the recognition string set by setPathVector(String).
	 * Invokes getMatchingParentObject(), returns a TestObject. 
	 * @return TestObject
	 * @see getTopTestObject(DomainTestObject)
	 */
	public TestObject getTopTestObject(){
		try{
			if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
				return (GuiTestObject)getCachedItem(getMatchingParentObject());
			}else{
				return (GuiTestObject) getMatchingParentObject();
			}
		}
		catch(Exception ccx){
			Log.debug("RGOV.getTopObjects() IGNORING "+ ccx.getClass().getSimpleName());
			return null;
		}
	}
	
    /** 
     * get matching child TestObject for this RGuiObjectVector.
     * If getProcessMode()==MODE_EXTERNAL_PROCESSING the call to getMatchingChildObject 
     * should be OK as getCachedItem should still return the aparent TestObject issued 
     * and found NOT to be a valid key.
     * @param  aparent, TestObject
     * @param  gather, java.util.List
     * @return GuiTestObject subclass of TestObject
     * @see GuiObjectVector#getMatchingChildObject(Object, List)
     * @see GuiObjectVector#getCachedItem(Object)
     **/
	public TestObject getChildTestObject(TestObject aparent, java.util.List gather) {
		
		try{
			if(getProcessMode()==MODE_EXTERNAL_PROCESSING){
				return (TestObject) getCachedItem(getMatchingChildObject(aparent, gather));
			}else{
				return (TestObject) getMatchingChildObject(aparent, gather);
			}
		}
		catch(ClassCastException ccx){ return null;}
	}

	/**
	 * @param _comp is expected to be of type Interface ITopWindow
	 * @see org.safs.GuiObjectVector#setActiveWindow(java.lang.Object)
	 */
	public void setActiveWindow(Object _comp) {
		
        ITopWindow win;
        try{
        	if (getProcessMode()==MODE_EXTERNAL_PROCESSING){
            	win = (ITopWindow) getCachedItem(_comp);
        	}else{
        		win = (ITopWindow) _comp;
        	}
        	win.activate();
        }
        catch(ClassCastException ccx){;}
	}
	
    public static boolean isJavaDomain(TestObject tobj) {
		String domainName = tobj.getDomain().getName().toString();
		return RGuiObjectVector.DEFAULT_JAVA_DOMAIN_NAME.equalsIgnoreCase(domainName);
	}

    public static boolean isDotnetDomain(TestObject tobj) {
		String domainName = tobj.getDomain().getName().toString();
		return RGuiObjectVector.DEFAULT_NET_DOMAIN_NAME.equalsIgnoreCase(domainName);
	}

    public static boolean isHtmlDomain(TestObject tobj) {
		String domainName = tobj.getDomain().getName().toString();
		return RGuiObjectVector.DEFAULT_HTML_DOMAIN_NAME.equalsIgnoreCase(domainName);
	}

    public static boolean isActiveXDomain(TestObject tobj) {
		String domainName = tobj.getDomain().getName().toString();
		return RGuiObjectVector.DEFAULT_ACTIVEX_DOMAIN_NAME.equalsIgnoreCase(domainName);
	}

    public static boolean isWinDomain(TestObject tobj) {
		String domainName = tobj.getDomain().getName().toString();
		return RGuiObjectVector.DEFAULT_WIN_DOMAIN_NAME.equalsIgnoreCase(domainName);
	}

    public static boolean isSwtDomain(TestObject tobj) {
		String domainName = tobj.getDomain().getName().toString();
		return RGuiObjectVector.DEFAULT_SWT_DOMAIN_NAME.equalsIgnoreCase(domainName);
	}
    public static boolean isFlexDomain(TestObject tobj) {
    	// .NET WinFrom getTestDomain throws Domain.ThreadChannel+ChannelSendFailureException
		String domainName = tobj.getDomain().getName().toString();
		return RGuiObjectVector.DEFAULT_FLEX_DOMAIN_NAME.equalsIgnoreCase(domainName);
	}	
	protected Object getCachedItem(Object key){
		return super.getCachedItem(key);
	}
	
	/**
	 * Retrieve keys from the cache using a value item.
	 * In RFT(RJ) engine, the cache is supposed to store pairs like <key, value(TestObject)> 
	 * One same value may be put into the cache more than one time, and owns different keys. 
	 * Return an ArrayList with all matching keys inside. 
	 * 
	 * @param item, a cached TestObject for being looked up in cache.
	 * @return ArrayList, all matching keys in cache stored in this ArrayList. 
	 * @see #makeUniqueCacheKey(Object)
	 * @see #putCachedItem(Object, Object)
	 * @see #removeCachedItem(Object)	
	 */
	public ArrayList getCachedKeysByValue(Object item){
		ArrayList keys = new ArrayList();
		if (item == null || cache == null) 
			return keys;
		
		java.awt.Rectangle targetSize = null;
		java.awt.Rectangle curSize = null;
		// FlexObjectTestObject can't be compared correctly by equals(), it is a RFT/FLEX'defect, which may be resolved in newer version.
		// A workaround: Two Flex objects are considered same if their positions on the screen are same. 
		// maybe another cache <TestObject, key> is needed if objects can't be compared as expected. OCT 29 2009. Junwu
		if (item instanceof FlexObjectTestObject)  
			targetSize = ((GuiTestObject)item).getScreenRectangle();
		
		// iterator the cache to find the matching pairs [key, TestObjectReference]
    	java.util.Enumeration  eum = cache.keys(); 
    	while (eum.hasMoreElements()) {
    		Object key = eum.nextElement();
    		Object value = cache.get(key);
    		
    		if (item instanceof FlexObjectTestObject) { // only for Flex objects 
        		curSize = ((GuiTestObject)value).getScreenRectangle();
        		if (targetSize.equals(curSize)) 
        			keys.add(key);
    		} else { 
    			if (item.equals(value))
    				keys.add(key);
    		}	
    	}
		return keys;
	}
}

