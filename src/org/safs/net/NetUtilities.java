/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * org.safs.net.NetUtilities.java:
 * Logs for developers, not published to API DOC.
 *
 * History:
 * Jul 10, 2015    (Lei Wang) Initial release.
 */
package org.safs.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.natives.NativeWrapper;

/**
 * Utilities for network.
 * @author Lei Wang
 */
public class NetUtilities {
	
	/**'localhost'*/
	public static final String LOCAL_HOST = StringUtils.LOCAL_HOST;
	/** '127.0.0.1'*/
	public static final String LOCAL_HOST_IP = StringUtils.LOCAL_HOST_IP;
	/** '::1'*/
	public static final String LOCAL_HOST_IPV6_PREFIX_1 = "::1";
	/** 'fe80::'*/
	public static final String LOCAL_HOST_IPV6_PREFIX_FE80 = "fe80::";
	
	/** "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$" the regex pattern for a MAC address. */
	public static final String PATTERN_MAC_ADDRESS = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";
	
	/**
	 * Test if the host is local according to the host's name.<br>
	 * @param host String, the name/ip of the host
	 * @return boolean true if the host is local.
	 */
	public static boolean isLocalHost(String host){
		boolean isLocalHost = LOCAL_HOST.equals(host) || LOCAL_HOST_IP.equals(host);
		
		if(!isLocalHost){
			String localhostIP = getLocalHostIP();
			//If the parameter host equals the localhost's IP?
			isLocalHost = localhostIP.equals(host);
			//If the host's IP equals the localhost's IP?
			if(!isLocalHost) isLocalHost = localhostIP.equals(getHostIPByName(host));

			//above code is enough to test if the host is local or not, we don't need to execute following code, it is waste of time.
//			if(!isLocalHost){
//				//http://en.wikipedia.org/wiki/IPv6_address#Local_addresses
//				String hostip = NativeWrapper.getHostIPByPing(host);
//				if(hostip==null){
//					IndependantLog.warn(StringUtils.debugmsg(false)+" IP is null for host '"+host+"'.");
//					return false;
//				}
//				isLocalHost = LOCAL_HOST_IP.equals(hostip) ||
//						hostip.startsWith(LOCAL_HOST_IPV6_PREFIX_1) ||
//						hostip.startsWith(LOCAL_HOST_IPV6_PREFIX_FE80);
//				isLocalHost = hostip.equals(localhostIP);
//			}
		}
		
		return isLocalHost;
	}

	/**
	 * 
	 * @param address String, the address to be tested
	 * @return boolean true if address is a MAC address
	 */
	public static boolean isMacAddress(String address){
		try {
			if(!StringUtils.isValid(address)) return false;
			return StringUtils.matchRegex(PATTERN_MAC_ADDRESS, address);
		} catch (SAFSException e) {
			IndependantLog.error(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
			return false;
		}
	}

	
	/**
	 * Get the host's IP address.<br>
	 * @param hostname String, the name of a host
	 * @return String, the host's IP address.
	 */
	public static String getHostIP(String hostname){
		String debugmsg = StringUtils.debugmsg(false);
		String hostIP = null;
		
		if(StringUtils.isValid(hostname)){
			if(isLocalHost(hostname)) hostIP = getLocalHostIP();
			else{
				hostIP = getHostIPByName(hostname);
				if(!StringUtils.isValid(hostIP)){
					IndependantLog.debug(debugmsg+"try NativeWrapper.getHostIPByPing(hostname)");
					hostIP = NativeWrapper.getHostIPByPing(hostname);
				}
			}
		}else{
			IndependantLog.error(debugmsg+" parameter hostname '"+hostname+"' is not valid.");
		}
		
		return hostIP;
	}
	
	/**
	 * Get the localhost's IP Address. Try Java's API to get it firstly, 
	 * if it cannot be got the try command {@link NativeWrapper#COMMAND_IPCONFIG}.
	 */
	public static String getLocalHostIP(){
		String debugmsg = StringUtils.debugmsg(false);
		String hostIP = null;

		try {
			hostIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			IndependantLog.warn(debugmsg+" fail due to "+StringUtils.debugmsg(e));
		}

		//Use command "ipconfig"/"ifconfig" to get the IP address
		if(!StringUtils.isValid(hostIP)){
			IndependantLog.debug(debugmsg+"try NativeWrapper.getLocalHostIPByConfig()");
			hostIP = NativeWrapper.getLocalHostIPByConfig();
		}

		if(hostIP!=null) hostIP = hostIP.trim();

		return hostIP;
	}
	
	/**
	 * @return String, the name of localhost
	 */
	public static String getLocalHostName(){
		String debugmsg = StringUtils.debugmsg(false);
		String hostName = null;
		
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			IndependantLog.warn(debugmsg+" fail due to "+StringUtils.debugmsg(e));
		}
		
		//Use command "hostname" to get the local host name
		if(!StringUtils.isValid(hostName)){
			IndependantLog.debug(debugmsg+"try NativeWrapper.getLocalHostName()");
			hostName = NativeWrapper.getLocalHostName();
		}
		
		if(hostName!=null) hostName = hostName.trim();
		
		return hostName;
	}
	
	/**
	 * Get the host's IP Address by Java's API.
	 * @see #getHostIPByPing(String)
	 */
	private static String getHostIPByName(String hostname){
		String debugmsg = StringUtils.debugmsg(false);
		String hostIP = null;

		try {
			hostIP = InetAddress.getByName(hostname).getHostAddress();
		} catch (UnknownHostException e) {
			IndependantLog.warn(debugmsg+" fail due to "+StringUtils.debugmsg(e));
		}
		
		if(hostIP!=null) hostIP = hostIP.trim();
		
		return hostIP;
	}
	
	/**
	 * test method {@link #getHostIP(String)} and {@link #isLocalHost(String)}.<br>
	 * @param args String[], arguments array from which to get hostname used to test method {@link #isLocalHost(String)} and {@link #getHostIP(String)}<br>
	 */
	private static void test_HostIP(String[] args){
		System.out.println("-------------------------   test_HostIP   -------------------------");
		//Parse the input args
		String[] hosts = {};
		for(int i=0;i<args.length;i++){
			if(ARG_TEST_HOSTS.equalsIgnoreCase(args[i])){
				if(i+1<args.length){
					hosts = StringUtils.getTokenArray(args[++i], StringUtils.SEMI_COLON);
				}
			}
		}
		System.out.println("local host IP: "+getLocalHostIP()+"\n");
		
		for(String host:hosts){
			System.out.println(host+"'s IP: "+getHostIP(host));
			System.out.println(host+ (isLocalHost(host)? " is ":" is not ")+"Localhost.");
			System.out.println("");
		}
		System.out.println("-----------------------------------------------------------------");
	}
	
	/**"-testhosts" followed by a series of hostname separated by semi-colon, such as "-testhosts host1;host2;host3;host4"*/
	public static final String ARG_TEST_HOSTS = "-testhosts";
	
	/**
	 * Test some implementations of this class.<br>
	 * To test {@link #isLocalHost(String)} and {@link #getHostIP(String)}, call as following:<br>
	 * {@code org.safs.net.NetUtilities -testhosts host1;host2;host3;host4}<br>
	 * @param args String[], <br>
	 *             -testhosts followed by a series of hostname separated by semi-colon, 
	 *             such as "-testhosts host1;host2;host3;host4", used to test method {@link #isLocalHost(String)} and {@link #getHostIP(String)}<br>
	 *             if {@link IndependantLog#ARG_DEBUG} followed by host name such as machine.domain.com, then {@link #test_getHostIp(String[])}<br>
	 */
	public static void main(String[] args){
		IndependantLog.parseArguments(args);
		
		test_HostIP(args);
		
	}
}
