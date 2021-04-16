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
package org.safs.selenium;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

/**
 * 
 * @author unknown
 * <br>	Jun 20, 2011	(Lei Wang)	Add some comments, debug messages, fix some bugs.
 */
public class CFHTMLDocument extends CFComponent {
	
	//HTMLDocumentFunctions Actions
	public static final String CAPTUREBODYSOURCETOFILE		= "CaptureBodySourceToFile";
	public static final String CAPTUREBODYTEXTTOFILE		= "CaptureBodyTextToFile";
	public static final String VERIFYBODYSOURCE				= "VerifyBodySource";
	public static final String VERIFYBODYTEXT				= "VerifyBodyText";
	public static final String CLICKLINK					= "ClickLink";
	public static final String CLICKLINKMATCHING			= "ClickLinkMatching";
	public static final String CLICKLINKBEGINNING			= "ClickLinkBeginning";
	public static final String CLICKLINKCONTAINING			= "ClickLinkContaining";
	
	protected String msg;
	protected String detail;
	
	public CFHTMLDocument() {
		super();
	}
	
	protected void localProcess(){
		if (action == null) return;
		
		try{
			if ((action.equalsIgnoreCase(CAPTUREBODYSOURCETOFILE))||
				(action.equalsIgnoreCase(CAPTUREBODYTEXTTOFILE))||
				(action.equalsIgnoreCase(VERIFYBODYSOURCE))||
				(action.equalsIgnoreCase(VERIFYBODYTEXT))){
					
				doBodyCommands();

			}else if ((action.equalsIgnoreCase(CLICKLINK))||
					  (action.equalsIgnoreCase(CLICKLINKMATCHING))||
					  (action.equalsIgnoreCase(CLICKLINKBEGINNING))||
					  (action.equalsIgnoreCase(CLICKLINKCONTAINING))){
				doLinkCommands();		  
			}
		}catch (SAFSException e) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	    	componentFailureMessage(e.getMessage());
	    }
	}
	
	/**
	 * <em>Purpose:</em>    Handle keywords related to link, as following<br>
	 * <ol>
	 * <li>ClickLink
	 * <li>ClickLinkMatching
	 * <li>ClickLinkBeginning
	 * <li>ClickLinkContaining
	 * </ol>
	 * @throws SAFSException
	 */
	protected void doLinkCommands() throws SAFSException{
		String fulllink = "";
		String _fulllink = null;
		String _link = null;
		String link = "";
		int index = 1;
		boolean caseSensitive = false;
		
		Log.info(".....CFHTMLDocument.doLinkCommands(): "+action+"; win: "+ windowName +"; comp: "+compName);
		
		//Treat parameters
		//At the least, the link value should be provided as parameter
		if(params.size()<1){
			paramsFailedMsg(windowName,compName);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			return;
		}
		Iterator iter = params.iterator();
		// LinkTextValue
		link = (String) iter.next();
		if ((link == null)||(link.length() < 1)){
			Log.error(".....CFHTMLDocument.doLinkCommands(): parameter of link value SHOULD NOT be blank.");
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			detail = failedText.convert("bad_param", "Invalid parameter value for TEXTVALUE",
			                            "TEXTVALUE");
			log.logMessage(testRecordData.getFac(),
				getStandardErrorMessage(action),
				detail,
				FAILED_MESSAGE);
			return;
		}
		// MatchIndex
		if(iter.hasNext()){
			String sindex = (String) iter.next();
			try{
				index = Integer.parseInt(sindex);
			}catch(NumberFormatException e){
				Log.warn(".....CFHTMLDocument.doLinkCommands():  index "+sindex+" can NOT be converted to Integer.");
			}
			if (index < 1) index = 1;
		}
		// CaseSensitive		
		if(iter.hasNext()){ 
			String scase = (String) iter.next();
			caseSensitive =((scase.equalsIgnoreCase("CaseSensitive"))||
			                (scase.equalsIgnoreCase("Case-Sensitive"))||
			                (scase.equalsIgnoreCase("True")));			   
		}
		Log.info(".....CFHTMLDocument.doLinkCommands(): seeking '"+ link +"', matchIndex#"+ index +", CaseSensitive="+ caseSensitive);
		
		//Get all xpath of tag <A> from the html page
		String[] linksXpath = null;
		try{ 
			linksXpath = ( ( SeleniumGUIUtilities )utils ).getAllXPaths("A", selenium);
		}catch(Exception x){
			Log.warn(".....CFHTMLDocument.doLinkCommands(): Can NOT retrieve tag A links from html page. "+x.getMessage());
		}
		if ((linksXpath == null)||(linksXpath.length == 0)){
			Log.error(".....CFHTMLDocument.doLinkCommands(): there are NO links on html page.");
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			detail = genericText.convert("not_contain", "'"+ windowName +":"+ compName +"' does not contain '"+ link +"'",
										windowName +":"+ compName, link);
			log.logMessage(testRecordData.getFac(),
				getStandardErrorMessage(action),
				detail,
				FAILED_MESSAGE);
			return;
		}
		
		int matches = 0;
		String linkLocator = "";
		_link = (caseSensitive)? link:link.toUpperCase();

		for(int i=0 ; i < linksXpath.length ;i++){
			linkLocator = SeleniumGUIUtilities.normalizeXPath(linksXpath[i]);
			try{fulllink = selenium.getText(linkLocator);}
			catch(Exception x){;}
			if (fulllink.length()==0) 
			    try{fulllink = selenium.getValue(linkLocator);}
				catch(Exception x){;}
			if (fulllink.length()==0) 
				try{fulllink = selenium.getAttribute(linkLocator+"/@alt");}
				catch(Exception x){;}
				
			if (fulllink.length()>0){
				_fulllink = caseSensitive? fulllink:fulllink.toUpperCase();
				Log.info(".....evaluating link:"+ fulllink);
				if((action.equalsIgnoreCase(CLICKLINK))||(action.equalsIgnoreCase(CLICKLINKMATCHING))){
					if(_link.equals(_fulllink))	matches++;
				}else if(action.equalsIgnoreCase(CLICKLINKBEGINNING)){
					if(_fulllink.startsWith(_link))	matches++;
				}else if(action.equalsIgnoreCase(CLICKLINKCONTAINING)){
					if(_fulllink.indexOf(_link) > -1) matches++;
				}
			}
			if (matches == index) break; 
		}
		if (matches == index){
			selenium.click(linkLocator);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert("success3a", windowName +":"+ compName + " "+ action +" successful using "+ fulllink,
					windowName, compName, action, fulllink);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
		}else{
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			detail = genericText.convert("not_contain", "'"+ windowName +":"+ compName +"' does not contain '"+ link +"'",
										windowName +":"+ compName, link);
			log.logMessage(testRecordData.getFac(),
				getStandardErrorMessage(action),
				detail,
				FAILED_MESSAGE);
		}		
	}

	/**
	 * <em>Purpose:</em>    Handle keywords related to body, as following<br>
	 * <ol>
	 * <li>CaptureBodySourceToFile
	 * <li>CaptureBodyTextToFile
	 * <li>VerifyBodySource
	 * <li>VerifyBodyText
	 * </ol>
	 * @throws SAFSException
	 */	
	protected void doBodyCommands() throws SAFSException{
		String pageContent = null;
		String benchContent = null;
		String filename = "";
		String fileEncoding = null;

		File fn = null;
		String absfilename = "";
		String absbenchfile = "";
		
		//Treat parameters
		if(params.size()<1){
			paramsFailedMsg(windowName,compName);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			return;
		}
		Iterator iter = params.iterator();
		filename = (String) iter.next();
		if(iter.hasNext()) fileEncoding = (String) iter.next();
		Log.info(".....CFHTMLDocument.doBodyCommands; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
		Log.debug(".....CFHTMLDocument.doBodyCommands; filename="+filename+" fileEncoding="+fileEncoding);
		//If filename has no extension, we will add .txt as its extension
		if(filename.indexOf(".")<0){
			Log.debug("filename has no suffix, adding '.txt' to it. filename="+filename);
			filename += ".txt";
		}
		
		//Retrieve the html content ('body source' or 'body text')
		if((action.equalsIgnoreCase(CAPTUREBODYSOURCETOFILE))||
		    (action.equalsIgnoreCase(VERIFYBODYSOURCE))){
			String source = selenium.getEval("function getInnerHtml(){ return document.getElementsByTagName('html')[0].innerHTML;} getInnerHtml();");
			Pattern bodyPattern = Pattern.compile("(<body.*</body>)",Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher bodyMatcher = bodyPattern.matcher(source);
			if(bodyMatcher.find()){
				pageContent = bodyMatcher.group(1);
			} else{
				detail = failedText.convert("no_node3",
				         action +" did not find node 'BODY' in 'HTML'.",
				         action, "BODY", "HTML");
				log.logMessage(testRecordData.getFac(),
					getStandardErrorMessage(action),
					detail,
					FAILED_MESSAGE);
				return;
			}
		}else if((action.equalsIgnoreCase(CAPTUREBODYTEXTTOFILE))||
			(action.equalsIgnoreCase(VERIFYBODYTEXT))){
			//For IE
			pageContent = selenium.getEval("function getInnerText(){ return window.document.getElementsByTagName('body')[0].innerText;} getInnerText();");
			if(pageContent==null || "null".equalsIgnoreCase(pageContent)){
				//For FireFox
				pageContent = selenium.getEval("function getInnerText(){ return window.document.getElementsByTagName('body')[0].textContent;} getInnerText();");
			}
		}
		
		//Perform capture or verification
		if ((action.equalsIgnoreCase(CAPTUREBODYSOURCETOFILE))||
			(action.equalsIgnoreCase(CAPTUREBODYTEXTTOFILE))){						
			fn = new CaseInsensitiveFile(filename).toFile();
			if (!fn.isAbsolute()) {
			    String pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);						  	
			    if (pdir == null) pdir="";
			    fn = new CaseInsensitiveFile(pdir, filename).toFile();
			}
			absfilename = fn.getAbsolutePath();
			Log.debug("Write to file "+absfilename);
	
			try{
				FileUtilities.writeStringToFile(absfilename, fileEncoding, pageContent);
			}catch(FileNotFoundException nf){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				detail = genericText.convert("not_exist", absfilename +" does not exist.", absfilename);							
				log.logMessage(testRecordData.getFac(),
					getStandardErrorMessage(action),
					detail,
					FAILED_MESSAGE);
				return;
			}catch(IOException io){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				detail = failedText.convert("file_error", "Error opening or reading '"+ absfilename +"'.", absfilename);							
				log.logMessage(testRecordData.getFac(),
					getStandardErrorMessage(action),
					detail,
					FAILED_MESSAGE);
				return;
			}
			
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert("success3a", windowName +":"+ compName + " "+ action +" successful using "+ absfilename,
					windowName, compName, action, absfilename);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
			return;						
		}else if ((action.equalsIgnoreCase(VERIFYBODYSOURCE))||
		          (action.equalsIgnoreCase(VERIFYBODYTEXT))){					

			File benchfile = (new CaseInsensitiveFile(filename)).toFile();
			if(!benchfile.isAbsolute()){
				String pdir = getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
				if (pdir == null) pdir="";
				benchfile = new CaseInsensitiveFile(pdir, filename).toFile();			
			}
			absbenchfile = benchfile.getAbsolutePath();
			Log.debug("Reading benchFile: "+absbenchfile);
			
			try{
				benchContent = FileUtilities.readStringFromEncodingFile(absbenchfile, fileEncoding);
			}catch(FileNotFoundException nf){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				detail = genericText.convert("not_exist", absbenchfile +" does not exist.", absbenchfile);							
				log.logMessage(testRecordData.getFac(),
					getStandardErrorMessage(action),
					detail,
					FAILED_MESSAGE);
				return;
			}catch(IOException io){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				detail = failedText.convert("file_error", "Error opening or reading '"+ absbenchfile +"'.", absbenchfile);							
				log.logMessage(testRecordData.getFac(),
					getStandardErrorMessage(action),
					detail,
					FAILED_MESSAGE);
				return;
			}

	  		//Compare bench file's content and html page's content
			String contentName = action.equalsIgnoreCase(VERIFYBODYSOURCE)? "Html Body Source":"Html Body Text";
	  		if(benchContent.equalsIgnoreCase(pageContent)){
	  	        //Log generic succes message
	  			String detailMsg = genericText.convert(GENStrings.CONTENT_MATCHES_KEY,
	  					"the content of '"+contentName+"' matches the content of '"+absbenchfile+"'",contentName,absbenchfile);
	  			this.componentSuccessMessage("*Match *"+detailMsg);
	  			//Set success status
	  	        testRecordData.setStatusCode(StatusCodes.OK);
	  		}else{
	  			//Log generic fail message
	  			String detailMsg = genericText.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
	  					"the content of '"+contentName+"' does not match the content of '"+absbenchfile+"'",contentName,absbenchfile);
	  			this.componentExecutedFailureMessage("*Not Match *"+detailMsg);
	  	        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  		}
		}
		
	}
	
}
