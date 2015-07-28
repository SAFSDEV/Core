/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * 
 * @author canagl
 * @since Jan 30, 2006
 *        Oct 09, 2008 (JunwuMa) Add Flex support.
 *        Feb 12, 2014 (SBJLWA) Add Dojo, Sap domains.
 */
public class Domains {

	/** "JAVA" */
	public static final String JAVA_DOMAIN = "JAVA";
	/** "ACTIVEX" */
	public static final String ACTIVEX_DOMAIN = "ACTIVEX";
	
	/** "HTML" */
	public static final String HTML_DOMAIN = "HTML";
	/** "DOJO" */
	public static final String HTML_DOJO_DOMAIN = "DOJO";
	/** "SAP" */
	public static final String HTML_SAP_DOMAIN = "SAP";
	
	/** "NET" */
	public static final String NET_DOMAIN  = "NET";
	/** "WPF" **/
	public static final String NET_WPF	= "WPF";
	/** "WIN" */
	public static final String WIN_DOMAIN  = "WIN";
	/** "SWT" */
	public static final String SWT_DOMAIN  = "SWT";
	/** "FLEX" */
	public static final String FLEX_DOMAIN  = "FLEX";

	public static final String[] domains = new String[]{
		JAVA_DOMAIN, HTML_DOMAIN, HTML_DOJO_DOMAIN, HTML_SAP_DOMAIN,NET_DOMAIN, WIN_DOMAIN, SWT_DOMAIN, FLEX_DOMAIN, ACTIVEX_DOMAIN
	};
	
    private static Vector _enabledDomains = new Vector();
    
    /**
     * Call to clear out and essentially disable all domains.
     * This is usually followed by calls to enable specific domains in a specific LIFO order.
     */
    public static void clearEnabledDomains(){
		Log.info("Domains clearing and removing ALL enabled domains.");
    	_enabledDomains.clear();
    }

    /**
     * Add (or move) a domain to the top of the enabledDomains list.
     * Items added to the list should be considered LIFO.  That is, each 
     * item added to the list is put at the top of the list.
     */
    public static void addEnabledDomain(String domainname){
    	try{
	    	String ucname = domainname.toUpperCase();
	   		if( _enabledDomains.contains(ucname)){
	   			Log.info("Domains moving "+ domainname +" to top of the list.");
	   			_enabledDomains.remove(ucname);
	   			_enabledDomains.insertElementAt(ucname,0);
	   		}else if (isDomainSupported(ucname)){
	   			Log.info("Domains adding "+ domainname +" to the enabled list.");
	   			_enabledDomains.insertElementAt(ucname,0);
	   		}
	   		Log.info("Domain precedence: "+ getEnabledDomainsString());
	   		return;
    	}catch(Exception x){}    	
		Log.debug("Domains cannot process NULL domain:"+ domainname);
    	throw new IllegalArgumentException(domainname);
    }
    
    /**
     * Remove a domain from the enabledDomains list.
     * Ignores invalid domains and domains not in the list.
     */
    public static void removeEnabledDomain(String domainname){
    	try{
	    	String ucname = domainname.toUpperCase();
	    	_enabledDomains.remove(ucname);
    	}catch(Exception x){}    	
		Log.debug("Domains cannot process NULL domain:"+ domainname);
    }
    
    /**
     * Returns a String array of the currently enabled domains in the order they 
     * should be processed. That is, item[0] is the first with highest precedence, etc..
     * @return String[] of enabled domain names in the order they should be processed.  
     * Can return an array of 0-length.
     */
    public static String[] getEnabledDomains(){
    	try{ return (String[])_enabledDomains.toArray(new String[0]);}
    	catch(Exception x){
			Log.debug ("Domains returning empty array due to "+ x.getClass().getSimpleName());			    		
    		return new String[0];}
    }
    
    /**
     * @return the array of enabled domains as a comma-separated list in a single String.
     * If no domains are enabled then this should be an empty String.  
     */
    public static String getEnabledDomainsString(){
    	String doms = "";
    	String[] arrdoms = getEnabledDomains();
    	for(int i=0;i<arrdoms.length;i++){
    		if(i>0){
    			doms += ","+ arrdoms[i];
    		}else{
    			doms = arrdoms[0];
    		}
    	}
    	return doms;
    }
    
    public static boolean isDomainSupported(String domainname){
    	try{
    		String ucname = domainname.toUpperCase();
    		for(int i=0;i< domains.length;i++){
    			if (ucname.equals(domains[i])) return true;
    		}
    	}catch(NullPointerException x){
    		Log.debug("Domains cannot process NULL domain.");
    	}
    	return false;
    }
    
	
	/**
	 * Enable specified test domains and disable those not specified.
	 * We attempt to enable the domains in the order listed in domains.
	 * @param domains -- case-insensitive string of domains to enable.
	 * Examples:<br> 
	 * "Java": Enables Java domain and disables all others.<br>
	 * "Java, HTML": Enables Java and Html domains--in that order--and disables all others.<br>
	 * @see #clearEnabledDomains()
	 * @see #addEnabledDomain(String)
	 */
    public static void enableDomains(String domains){ 
        try{
        	String ucdomains = domains.toUpperCase();
	        int ijava = ucdomains.indexOf(JAVA_DOMAIN);
	        int ihtml = ucdomains.indexOf(HTML_DOMAIN);
	        int idojo = ucdomains.indexOf(HTML_DOJO_DOMAIN);
	        int isap = ucdomains.indexOf(HTML_SAP_DOMAIN);
	        int inet  = ucdomains.indexOf(NET_DOMAIN);
	        int iwin  = ucdomains.indexOf(WIN_DOMAIN);
	        int iswt  = ucdomains.indexOf(SWT_DOMAIN);
	        int iflex = ucdomains.indexOf(FLEX_DOMAIN);
	        int iactivex = ucdomains.indexOf(ACTIVEX_DOMAIN);

	        //need to enable the domains in the order specified by user
	        int[] ilocs = new int[]{ijava,ihtml,idojo,isap,inet,iwin,iswt,iflex, iactivex};
	        Arrays.sort(ilocs);//sorts in ascending order
        	int n = -1;
        	clearEnabledDomains();
        	//add in descending order (LIFO) so they are put into addEnabledDomain correctly
	        for(int i=ilocs.length -1; i > -1; i--){
	        	n = ilocs[i];
	        	if (n < 0) continue;
	        	if( n == ijava) addEnabledDomain(JAVA_DOMAIN);
	        	if( n == ihtml) addEnabledDomain(HTML_DOMAIN);
	        	if( n == idojo) addEnabledDomain(HTML_DOJO_DOMAIN);
	        	if( n == isap) addEnabledDomain(HTML_SAP_DOMAIN);
	        	if( n == inet) addEnabledDomain(NET_DOMAIN);
	        	if( n == iwin) addEnabledDomain(WIN_DOMAIN);
	        	if( n == iswt) addEnabledDomain(SWT_DOMAIN);
	        	if( n == iflex) addEnabledDomain(FLEX_DOMAIN);
	        	if( n == iactivex) addEnabledDomain(ACTIVEX_DOMAIN);
	        }
        }catch(Exception x){
        	Log.info("Ignoring Exception in Domains.enableDomains while processing: "+ domains, x);
        }
    }        	

    /**
	 * Enable a specified test domain.
	 * @param case-insensitive name of test domain to enable.
	 * @throws IllegalArgumentException
	 */
    public static void enableDomain(String domainname){ addEnabledDomain(domainname);}        	
	
	/**
	 * Disable a specified test domain.
	 * @param case-insensitive name of test domain to disable.
	 * @throws IllegalArgumentException
	 */
    public static void disableDomain(String domainname){ removeEnabledDomain(domainname);}

	/**
	 * Returns true if the specified test domain is enabled.
	 * @param case-insensitive name of test domain to check.
	 * @throws IllegalArgumentException
	 */
	public static boolean isDomainEnabled(String domainname){
    	try{
    		String ucname = domainname.toUpperCase();
    		if (ucname.equals(JAVA_DOMAIN)) return isJavaEnabled();
    		if (ucname.equals(HTML_DOMAIN)) return isHtmlEnabled();
    		if (ucname.equals(HTML_DOJO_DOMAIN)) return isDojoEnabled();
    		if (ucname.equals(HTML_SAP_DOMAIN)) return isSapEnabled();
    		if (ucname.equals(NET_DOMAIN))  return isNetEnabled();
    		if (ucname.equals(WIN_DOMAIN))  return isWinEnabled();
    		if (ucname.equals(SWT_DOMAIN))  return isSwtEnabled();
    		if (ucname.equals(FLEX_DOMAIN))  return isFlexEnabled();
    		if (ucname.equals(ACTIVEX_DOMAIN))  return isActiveXEnabled();
    	}
    	catch(Exception x){}
    	throw new IllegalArgumentException("Invalid domain:"+ domainname );
	}
	        	
	/**
	 * @return boolean true if activeX is Enabled.
	 */
	public static boolean isActiveXEnabled() {
		return _enabledDomains.contains(ACTIVEX_DOMAIN);
	}

	/**
	 * @return boolean true if Html is Enabled.
	 */
	public static boolean isHtmlEnabled() {
		return _enabledDomains.contains(HTML_DOMAIN);
	}
	/**
	 * @return boolean true if Dojo is Enabled.
	 */
	public static boolean isDojoEnabled() {
		return _enabledDomains.contains(HTML_DOJO_DOMAIN);
	}
	/**
	 * @return boolean true if Sap is Enabled.
	 */
	public static boolean isSapEnabled() {
		return _enabledDomains.contains(HTML_SAP_DOMAIN);
	}

	/**
	 * @return boolean true if Java is Enabled.
	 */
	public static boolean isJavaEnabled() {
		return _enabledDomains.contains(JAVA_DOMAIN);
	}

	/**
	 * @return boolean true if Net is Enabled.
	 */
	public static boolean isNetEnabled() {
		return _enabledDomains.contains(NET_DOMAIN);
	}

	/**
	 * @return boolean true if Win is Enabled.
	 */
	public static boolean isWinEnabled() {
		return _enabledDomains.contains(WIN_DOMAIN);
	}

	/**
	 * @return boolean true if Swt is Enabled.
	 */
	public static boolean isSwtEnabled() {
		return _enabledDomains.contains(SWT_DOMAIN);
	}
	/**
	 * @return boolean true if Flex is Enabled.
	 */
	public static boolean isFlexEnabled() {
		return _enabledDomains.contains(FLEX_DOMAIN);
	}


	/**
	 * @param enabled if true, enable activeX domain; otherwise disable it.
	 */
	public static void setActiveXEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(ACTIVEX_DOMAIN);
		else
			removeEnabledDomain(ACTIVEX_DOMAIN);
		Log.info("Domains ActiveX domain enabled: "+ isHtmlEnabled());
	}

	/**
	 * @param enabled if true, enable Html domain; otherwise disable it.
	 */
	public static void setHtmlEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(HTML_DOMAIN);
		else
			removeEnabledDomain(HTML_DOMAIN);
		Log.info("Domains Html domain enabled: "+ isHtmlEnabled());
	}
	
	/**
	 * @param enabled if true, enable Dojo domain; otherwise disable it.
	 */
	public static void setDojoEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(HTML_DOJO_DOMAIN);
		else
			removeEnabledDomain(HTML_DOJO_DOMAIN);
		Log.info("Domains Dojo domain enabled: "+ isDojoEnabled());
	}
	/**
	 * @param enabled if true, enable Sap domain; otherwise disable it.
	 */
	public static void setSapEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(HTML_SAP_DOMAIN);
		else
			removeEnabledDomain(HTML_SAP_DOMAIN);
		Log.info("Domains Sap domain enabled: "+ isSapEnabled());
	}

	/**
	 * @param enabled if true, enable Java domain; otherwise disable it.
	 */
	public static void setJavaEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(JAVA_DOMAIN);
		else
			removeEnabledDomain(JAVA_DOMAIN);
		Log.info("Domains Java domain enabled: "+ isJavaEnabled());
}

	/**
	 * @param enabled if true, enable Net domain; otherwise disable it.
	 */
	public static void setNetEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(NET_DOMAIN);
		else
			removeEnabledDomain(NET_DOMAIN);
		Log.info("Domains Net domain enabled: "+ isNetEnabled());
	}

	/**
	 * @param enabled if true, enable Win domain; otherwise disable it.
	 */
	public static void setWinEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(WIN_DOMAIN);
		else
			removeEnabledDomain(WIN_DOMAIN);
		Log.info("Domains Win domain enabled: "+ isWinEnabled());
	}

	/**
	 * @param enabled if true, enable Swt domain; otherwise disable it.
	 */
	public static void setSwtEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(SWT_DOMAIN);
		else
			removeEnabledDomain(SWT_DOMAIN);
		Log.info("Domains SWT domain enabled: "+ isSwtEnabled());
	}
	/**
	 * @param enabled if true, enable Flex domain; otherwise disable it.
	 */
	public static void setFlexEnabled(boolean enabled) {
		if(enabled)
			addEnabledDomain(FLEX_DOMAIN);
		else
			removeEnabledDomain(FLEX_DOMAIN);
		Log.info("Domains FLEX domain enabled: "+ isFlexEnabled());
	}
}
