/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年3月10日    (Lei Wang) Initial release.
 * 2017年3月15日    (Lei Wang) Added test of unpickle from JSON file.
 */
package org.safs.persist.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safs.SAFSException;
import org.safs.auth.AuthorizationServer;
import org.safs.auth.Content;
import org.safs.auth.OAuth2;
import org.safs.auth.SimpleAuth;
import org.safs.persist.Persistable;
import org.safs.persist.PersistableDefault;
import org.safs.persist.PersistenceType;
import org.safs.persist.Persistor;
import org.safs.persist.PersistorFactory;
import org.safs.text.FileUtilities;
import org.safs.text.FileUtilities.FileType;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author Lei Wang
 */
public class PersistTest {

	/**
	 *<ul>
	 *<li>{@link FileUtilities#VAR_SAFSBENCHDIRECTORY}
	 *<li>{@link FileUtilities#VAR_SAFSDATAPOOLDIRECTORY}
	 *<li>{@link FileUtilities#VAR_SAFSDIFDIRECTORY}
	 *<li>{@link FileUtilities#VAR_SAFSLOGSDIRECTORY}
	 *<li>{@link FileUtilities#VAR_SAFSPROJECTDIRECTORY}
	 *<li>{@link FileUtilities#VAR_SAFSTESTDIRECTORY}
	 *</ul>
	 * Each variable can be followed by a path (relative or absolute), which is used to initialize the value of this variable of {@link #runtimeData}.<br>
	 * Examples<br>
	 * <b>safstestdirectory relativePath|absolutePath</b><br>
	 * <b>safsprojectdirectory relativePath|absolutePath</b><br>
	 */
	private static final String[] param_vars = {
		FileUtilities.VAR_SAFSBENCHDIRECTORY,
		FileUtilities.VAR_SAFSDATAPOOLDIRECTORY,
		FileUtilities.VAR_SAFSDIFDIRECTORY,
		FileUtilities.VAR_SAFSLOGSDIRECTORY,
		FileUtilities.VAR_SAFSPROJECTDIRECTORY,
		FileUtilities.VAR_SAFSTESTDIRECTORY};

	/**
	 * This parameter is followed by a path (relative or absolute), which is used to initialize the value of variables {@link #param_vars} of {@link #runtimeData}.<br>
	 * The value of each variable can be replaced by setting of {@link #param_vars}.<br>
	 * Examples<br>
	 * <b>-d relativePath|absolutePath</b><br>
	 */
	private static final String param_d = "-d";

	/**
	 * A fake RuntimeDataInterface object providing value for variables {@link #param_vars}.
	 */
	private static RuntimeDataInterface runtimeData = new RuntimeDataInterface(){
		private Map<String, String> variables = new HashMap<String, String>();

		private boolean debug = false;

		@Override
//		public static final String VAR_SAFSBENCHDIRECTORY    = "safsbenchdirectory";
//		public static final String VAR_SAFSDATAPOOLDIRECTORY = "safsdatapooldirectory";
//		public static final String VAR_SAFSDIFDIRECTORY      = "safsdifdirectory";
//		public static final String VAR_SAFSLOGSDIRECTORY     = "safslogsdirectory";
//		public static final String VAR_SAFSPROJECTDIRECTORY  = "safsprojectdirectory";
//		public static final String VAR_SAFSTESTDIRECTORY     = "safstestdirectory";
		public String getVariable(String varName) throws SAFSException {
			String root = System.getProperty("user.dir");
			String value = variables.get(varName);
			String result = null;
			File tempFile = null;
			if(value!=null){
				tempFile = new File(value);
				if(tempFile.exists()){
					result = value;
				}else{
					//append the root directory
					if(root.endsWith(File.separator)) root=root.substring(0, root.length()-1);
					if(value.startsWith(File.separator)) value=value.substring(1);
					result = root+File.separator+value;
					tempFile = new File(result);
					if(tempFile.exists()){
						variables.put(varName, tempFile.getAbsolutePath());
					}else{
						result = root;
					}
				}
			}else{
				result = root;
			}

			if(debug) System.out.println("return '"+result+"' for variable '"+varName+"'");
			return result;
		}

		@Override
		public boolean setVariable(String varName, String varValue) throws SAFSException {
			variables.put(varName, varValue);
			return true;
		}

		@Override
		public String getAppMapItem(String appMapId, String sectionName, String itemName) throws SAFSException {
			throw new SAFSException("Not implemented");
		}
	};

	/**
	 * Save SimpleAuth object to an XML file.<br>
	 * Create SimpleAuth object from that XML file.<br>
	 * Verify the un-pickled SimpleAuth object is the same as the original SimpleAuth object.<br>
	 * Create SimpleAuth object from that XML file, but ignore the field 'password'.<br>
	 */
	private static void testSimpleAuth(){
		String simpleauthfile = "simpleauth.xml";

		SimpleAuth simpleauth = new SimpleAuth();
		simpleauth.setUserName("Tom");
		simpleauth.setPassword("unitA123");

		try {
			System.out.println("Original SimpleAuth:\n"+simpleauth);

			Persistor p = PersistorFactory.create(PersistenceType.FILE, FileType.XML, runtimeData, simpleauthfile);
			p.persist(simpleauth);

			Persistable persist = p.unpickle(null);
			System.out.println("Unpickled SimpleAuth:\n"+persist);

			assert persist.equals(simpleauth);

			Map<String/*className*/, List<String>/*field-names*/> ignoredFields = new HashMap<String, List<String>>();
			List<String> fields = new ArrayList<String>();
			fields.add("password");
			ignoredFields.put("org.safs.auth.SimpleAuth", fields);
			persist = p.unpickle(ignoredFields);
			System.out.println("Unpickled SimpleAuth without fields "+fields+"\n"+persist);

		} catch (SAFSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save OAuth2 object to an XML file.<br>
	 * Create OAuth2 object from that XML file.<br>
	 * Verify the un-pickled OAuth2 object is the same as the original OAuth2 object.<br>
	 */
	private static void testOAuth2(){
		String xmlfile = "auth2.xml";
		String jsonfile = "auth2.json";

		SimpleAuth simpleauth = new SimpleAuth();
		simpleauth.setUserName("Tom");
		simpleauth.setPassword("unitA123");

		Content content = new Content();
		content.setAccessToken("HARHA34HAZFUREHFDAEIPOZ=D");

		AuthorizationServer server = new AuthorizationServer();
		server.setRootUrl("http://oauth2.authorization.server:8080");
		server.setBaseServiceName("login");
		server.setAuthCodeResource("oauth/authorize");
		server.setAuthTokenResource("oauth/token");

		OAuth2 auth2 = new OAuth2();
		auth2.setContent(content);
		auth2.setSimpleAuth(simpleauth);
		auth2.setAuthorizationServer(server);

		try {
			System.out.println("Original OAuth2:\n"+auth2);

			//Test the persistor to XML file
			Persistor p = PersistorFactory.create(PersistenceType.FILE, FileType.XML, runtimeData, xmlfile);
			p.persist(auth2);
			Persistable persist = p.unpickle(null);
			System.out.println("Unpickled OAuth2 from file '"+xmlfile+"'\n"+persist);
			assert persist.equals(auth2);

			//Test the persistor to JSON file
			p = PersistorFactory.create(PersistenceType.FILE, FileType.JSON, runtimeData, jsonfile);
			p.persist(auth2);
			persist = p.unpickle(null);
			System.out.println("Unpickled OAuth2 from file '"+jsonfile+"'\n"+persist);
			assert persist.equals(auth2);

		} catch (SAFSException e) {
			e.printStackTrace();
		}
	}

	private static void testMyPersistable(){
		String xmlfile = "MyPersistable.xml";
		String jsonfile = "MyPersistable.json";

		String[] stringArray = {"item1","item2","item3","item4","item5"};
		int[] intArray = {1,2,3,4,5};
		Double[] doubleArray = {1.0, 2.0, 3.0, 4.0, 5.0};

		MyPersistable myPersistable = new MyPersistable();
		myPersistable.setByteField((byte)21);
		myPersistable.setDoubleField(25.36);
		myPersistable.setFloatField(25.00F);
		myPersistable.setIntField(369);
		myPersistable.setLongField(456L);
		myPersistable.setShortField((short)78);
		myPersistable.setStringArray(stringArray);
		myPersistable.setIntArray(intArray);
		myPersistable.setDoubleArray(doubleArray);

		System.out.println("Original :\n"+myPersistable);

		Persistor p = null;
		Persistable persist = null;

		try {

			//Test the persistor to XML file
			p = PersistorFactory.create(PersistenceType.FILE, null, runtimeData, xmlfile);
			p.persist(myPersistable);
			persist = p.unpickle(null);
			System.out.println("Unpickled from file '"+xmlfile+"'\n"+persist);
			assert persist.equals(myPersistable);

		} catch (SAFSException e) {
			e.printStackTrace();
		}

		try {
			//Test the persistor to JSON file
			p = PersistorFactory.create(PersistenceType.FILE, null, runtimeData, jsonfile);
			p.persist(myPersistable);
			persist = p.unpickle(null);
			System.out.println("Unpickled from file '"+jsonfile+"'\n"+persist);
			assert persist.equals(myPersistable);

		} catch (SAFSException e) {
			e.printStackTrace();
		}
	}

	public static class MyPersistable extends PersistableDefault{
		private int intField;
		private short shortField;
		private long longField;
		private double doubleField;
		private float floatField;
		private byte byteField;

		private String[] stringArray;
		private int[] intArray;
		private Double[] doubleArray;

		public int getIntField() {
			return intField;
		}
		public void setIntField(int intField) {
			this.intField = intField;
		}
		public short getShortField() {
			return shortField;
		}
		public void setShortField(short shortField) {
			this.shortField = shortField;
		}
		public long getLongField() {
			return longField;
		}
		public void setLongField(long longField) {
			this.longField = longField;
		}
		public double getDoubleField() {
			return doubleField;
		}
		public void setDoubleField(double doubleField) {
			this.doubleField = doubleField;
		}
		public float getFloatField() {
			return floatField;
		}
		public void setFloatField(float floatField) {
			this.floatField = floatField;
		}
		public byte getByteField() {
			return byteField;
		}
		public void setByteField(byte byteField) {
			this.byteField = byteField;
		}
		public String[] getStringArray() {
			return stringArray;
		}
		public void setStringArray(String[] stringArray) {
			this.stringArray = stringArray;
		}
		public int[] getIntArray() {
			return intArray;
		}
		public void setIntArray(int[] intArray) {
			this.intArray = intArray;
		}
		public Double[] getDoubleArray() {
			return doubleArray;
		}
		public void setDoubleArray(Double[] doubleArray) {
			this.doubleArray = doubleArray;
		}
	}
	/**
	 * @param args refer to {@link #param_d} and {@link #param_vars}.
	 * @throws SAFSException
	 */
	private static void handleArgs(String... args) throws SAFSException{
		String value = null;
		String arg = null;

		//Handle parameter '-d'
		for(int i=0;i<args.length;i++){
			if(param_d.equalsIgnoreCase(args[i])){
				if(i+1<args.length){
					value = args[++i];
					for(String var:param_vars){
						runtimeData.setVariable(var, value);
					}
				}
				break;
			}
		}

		//Handle other parameters
		for(int i=0;i<args.length;i++){
			arg = args[i];
			for(String var:param_vars){
				if(arg.equalsIgnoreCase(var)){
					if(i+1<args.length){
						value = args[++i];
						runtimeData.setVariable(var, value);
					}
				}
			}
		}

	}

	/**
	 * java -ea org.safs.persist.test.PersistorTest
	 * java -ea org.safs.persist.test.PersistorTest -d src\install\doc\configure
	 * @param args
	 * @throws SAFSException
	 */
	public static void main(String[] args) throws SAFSException{
		handleArgs(args);

		testSimpleAuth();
		testOAuth2();
		testMyPersistable();
	}
}
