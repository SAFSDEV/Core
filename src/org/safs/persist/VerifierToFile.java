/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 05, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSNullPointerException;
import org.safs.SAFSVerificationException;
import org.safs.StringUtils;
import org.safs.text.FAILKEYS;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author Lei Wang
 */
public class VerifierToFile extends AbstractRuntimeDataVerifier{

	/**
	 * The name of file to hold the information of a Persistable object to be verified.
	 */
	protected String filename = null;

	/**
	 * The Reader object of the bench file. It contains the expected values.
	 */
	protected Reader reader = null;

	/**
	 * Holding the actual contents to verify, they are pairs of (flatKey, content) such as
	 * (Response.ID, "FFE3543545JLFS")
	 * (Response.Headers, "{Date=Tue, 06 DEC 2016 03:08:12 GMT}")
	 * (Response.Request.Headers, "{Content-Length=4574, Via=1.1 inetgw38 (squid)}")
	 */
	protected Map<String, Object> actualContents = null;

	/**
	 * Holding the expected contents for verification, they are pairs of (flatKey, content) such as
	 * (Response.ID, "FFE3543545JLFS")
	 * (Response.Headers, "{Date=Tue, 06 DEC 2016 03:08:12 GMT}")
	 * (Response.Request.Headers, "{Content-Length=4574, Via=1.1 inetgw38 (squid)}")
	 */
	protected Map<String, Object> expectedContents = null;

	/**
	 * Each time a field has been checked, then that field will be put into this Set.
	 * @see #match(String, String)
	 */
	protected Set<String> checkedFields = null;

	/**
	 * Holding the fields which are ignored. No need to verify.<br/>
	 * The field is expressed as a flat key.<br/>
	 * <pre>
	 * If this Set contains a field 'Response.Request', then all its children will
	 * be ignored, such as:
	 * Response.Request.Headers
	 * Response.Request.MessageBody
	 * ...
	 * </pre>
	 * @see #isIgnoredFiled(String)
	 */
	protected Set<String> ignoredFields = null;

	/** If the all the fields of actual object need to match with those in the persistent benchmark.<br/>
	 * If this is true, all the fields of actual object need to be verified.<br/>
	 * Otherwise, only the fields specified in the persistent benchmark need to be verified.<br/>
	 */
	protected boolean matchAllFields 	= false;
	/** If the field's value needs to be matched wholly or partially  */
	protected boolean valueContains 		= false;
	/** If the field's value needs to be matched case-sensitively */
	protected boolean valueCaseSensitive 	= false;

	/** This field will contain true if the verification succeed. */
	protected boolean matched = true;
	/** It contains the messages indicating the non matched fields. */
	protected StringBuilder nonMatchedMessages = null;

	public VerifierToFile(RuntimeDataInterface runtime, String filename){
		super(runtime);
		this.filename = filename;
		ignoredFields = new HashSet<String>();
		checkedFields = new HashSet<String>();
		nonMatchedMessages = new StringBuilder();
		expectedContents = new HashMap<String, Object>();
	}

	@Override
	public void verify(Persistable persistable, boolean... conditions) throws SAFSException {
		super.verify(persistable);

		try {
			//open the persistence file
			File file = FileUtilities.deduceBenchFile(filename, runtime);
			reader = FileUtilities.getUTF8BufferedFileReader(file.getAbsolutePath());
			//write the persistable object to the file
			beforeCheck(persistable, conditions);
			check(persistable, conditions);
			afterCheck(persistable, conditions);
		} catch (IOException e) {
			String message = FAILStrings.convert(FAILKEYS.FILE_ERROR, "Error opening or reading or writing file '"+filename+"'", filename);
			throw new SAFSException(message);
		} finally{
			//close the persistence file
			try {
				reader.close();
			} catch (IOException e) {
				IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to close the reader on file '"+filename+"'.");
			}
		}
	}

	/**
	 * Get some variables prepared for verification.<br/>
	 * Parse the optional parameters<br/>
	 * It is expected that subclass should override this method to provide values to
	 * {@link #expectedContents} and {@link #actualContents}, such as:<br/>
	 * <pre>
	 * <code>
	 * public void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
	 *    super.beforeCheck(persistable, conditions);
	 *    //<b>Fill in Map 'actualContents'</b>
	 *    //<b>Fill in Map 'expectedContents'</b>
	 * }
	 * </code>
	 * </pre>
	 */
	protected void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
		ignoredFields.clear();
		checkedFields.clear();
		nonMatchedMessages.delete(0, nonMatchedMessages.length());
		expectedContents.clear();

		if(conditions.length>0){
			matchAllFields = conditions[0];
		}
		if(conditions.length>1){
			valueContains = conditions[1];
		}
		if(conditions.length>2){
			valueCaseSensitive = conditions[2];
		}
	}


	protected void check(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
		if(actualContents==null){
			throw new SAFSNullPointerException("The actual contents Map has not been initilized!");
		}

		Set<String> keys = expectedContents.keySet();
		Object expectedText = null;

		for(String key:keys){
			expectedText = expectedContents.get(key);
			match(key, expectedText);
		}
	}

	/**
	 * Make the final check.<br/>
	 *
	 */
	protected void afterCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{

		if(!matched){
			throw new SAFSVerificationException(nonMatchedMessages.toString());
		}

		//If we are here, which means all the fields in the persistence benchmark have been satisfied,
		//that is to say "the actual object" contains all the fields in benchmark and matched.
		if(matchAllFields){
			//We still need to check if all the fields of actual object have been satisfied, if fieldContains is not true.
			//all the fields of actual object should be matched.
			Set<String> actualFields = actualContents.keySet();
			actualFields.removeAll(checkedFields);
			if(!actualFields.isEmpty()){
				matched = false;
				String errorMsg = "Missing fields in benchmark file:\n";
				errorMsg += Arrays.toString(actualFields.toArray());
				throw new SAFSVerificationException(errorMsg);
			}
		}
	}

	/**
	 * This method will check a certain filed's value against the {@link #actualContents}<br/>
	 * After calling, these class fields {@link #matched}, {@link #checkedFields}, and {@link #nonMatchedMessages}
	 * may get modified to show the result of verification.<br/>
	 *
	 * @param field	String, the field to check
	 * @param expectation Object, the field's expected value.
	 */
	protected void match(String field, Object expectation){

		if(isIgnoredFiled(field)){
			IndependantLog.debug("Ignoring checking '"+field+"'.");
		}else{
			IndependantLog.debug("Checking field '"+field+"' ... ");

			Object actual = actualContents.get(field);
			checkedFields.add(field);
			if(actual==null){
				nonMatchedMessages.append("Cannot find the actual value for field '"+field+"'!\n");
				matched = false;

			}else{
				//TODO compare the non-String value
				String actualText = actual.toString();
				String expectedText = expectation.toString();

				if(!StringUtils.matchText(actualText, expectedText, valueContains, !valueCaseSensitive)){
					nonMatchedMessages.append("'"+field+"' did not match!\n"
							+ "actual: "+actualText+"\n"
							+ "expected: "+expectation+"\n");
					matched = false;
				}
			}
		}
	}

	/**
	 * Check if the filed is being ignored at the moment for verification.<br/>
	 * <pre>
	 * If the Set {@link #ignoredFields} contains a field 'Response.Request',
	 * then all its children (starting with 'Response.Request') will be ignored, such as:
	 * Response.Request.Headers
	 * Response.Request.MessageBody
	 * ...
	 * </pre>
	 * @param field String, the field to check
	 * @return boolean if this field is being ignored.
	 * @see #ignoredFields
	 */
	protected boolean isIgnoredFiled(String field){
		for(String ignoredField:ignoredFields){
			if(field.startsWith(ignoredField)) return true;
		}
		return false;
	}

	public PersistenceType getType(){
		return PersistenceType.FILE;
	}

	/**
	 * If they have the same filename, then we consider them equivalent
	 */
	@Override
	public boolean equals(Object o){
		if(o==null) return false;
		if(!(o instanceof VerifierToFile)) return false;
		VerifierToFile p = (VerifierToFile) o;

		if(this.equals(o)){
			return true;
		}

		if(filename==null){
			return p.filename==null;
		}else{
			return this.filename.equals(p.filename);
		}
	}

}
