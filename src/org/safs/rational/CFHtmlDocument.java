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
package org.safs.rational;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safs.Log;
import org.safs.MatchData;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> CFHtmlDocument, process HTMLDocument components.
 * See the file ObjectTypesMap.dat for cross reference as to which map to us.
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Carl Nagle (mod of CFHTMLLink by Doug Bauman)
 * @since   Nov 10, 2004
 *
 *   <br>   Nov 10, 2004    (Carl Nagle) 	Original Release
 * 	 <br>	Mar 18, 2008	(Lei Wang)	Added CaptureBodySourceToFile and VerifyBodySource support
 *   <br>	Mar 18, 2008	(JunwuMa)	Reorganized localProcess().  Added CaptureBodyTextToFile and VerifyBodyText. 
 *                                      Added ClickLink based on orignal CLICKLINKBEGINNING and CLICKLINKCONTAINING 
 *   <br>	Mar 18, 2008	(Lei Wang)	Modify method getHtmlBodyContent()
 *                                      
 **/
public class CFHtmlDocument extends CFComponent {
  //Keyword Constant Definition
  public static final String CAPTUREBODYSOURCETOFILE 	  = "CaptureBodySourceToFile";
  public static final String CAPTUREBODYTEXTTOFILE        = "CaptureBodyTextToFile";
  public static final String CLICKLINK					  = "ClickLink";
  public static final String CLICKLINKBEGINNING           = "ClickLinkBeginning";
  public static final String CLICKLINKCONTAINING          = "ClickLinkContaining";
  public static final String VERIFYBODYSOURCE			  = "VerifyBodySource";
  public static final String VERIFYBODYTEXT				  = "VerifyBodyText";
  //Message Key Constant -- for proxy
  public static final String USE_PROXY_KEY			 	  = "use.proxy";
  public static final String PROXY_SERVER_KEY			  = "proxy.server";
  public static final String PROXY_SERVER_PORT_KEY		  = "proxy.server.port";
  
  public static final String HTML_HTMLDOCUMENT			  = "Html.HtmlDocument";
  public static final String HTML_HTMLBROWSER			  = "Html.HtmlBrowser";
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFHtmlDocument () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** The actions handled here are:
   ** <br><ul>
   ** <li> ClickLinkBeginning
   ** <li> ClickLinkContaining
   ** <li> CaptureBodySourceToFile
   ** <li> CaptureBodyTextToFile
   ** <li> ClickLink
   ** <li> ClickLinkBeginning
   ** <li> ClickLinkContaining
   ** <li> VerifyBodySource
   ** <li> VerifyBodyText
   ** </ul><br>
   ** <br>NOTE: the 'activate' keywords didn't seem to work on the regression test
   ** because the use the guiObject.click instead of guiObject.setState method.
   ** The latter seems to work better
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
    try {
      // then we have to process for specific items not covered by our super
      Log.info(getClass().getName()+".process, searching specific tests...");

      if (action != null) {
        Log.info("....."+getClass().getName()+".process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
        if (action.equalsIgnoreCase(CLICKLINKBEGINNING) ||
            action.equalsIgnoreCase(CLICKLINKCONTAINING)||
			action.equalsIgnoreCase(CLICKLINK)) {
        	doLinkCommands();
        }else if(action.equalsIgnoreCase(CAPTUREBODYSOURCETOFILE)){
        	doCaptureBodySourceToFile();
        }else if(action.equalsIgnoreCase(VERIFYBODYSOURCE)){
        	doVerifyBodySource();
        }else if (action.equalsIgnoreCase(CAPTUREBODYTEXTTOFILE)){
            doCaptureBodyTextToFile();
        }else if (action.equalsIgnoreCase(VERIFYBODYTEXT)){
            doVerifyBodyText();
        }
      }
    } catch (com.rational.test.ft.ObjectNotFoundException e) {
    	componentFailureMessage(e.getMessage());
    } catch (SAFSException e) {
    	componentFailureMessage(e.getMessage());
    }
  }
  /**
   * Purpose: preform CLICKLINKBEGINNING, CLICKLINKCONTAINING and CLICKLINK. Search target link in the HtmlDocument; 
   *                       then click the link if found.
   * @throws SAFSException
   */
  private void doLinkCommands() throws SAFSException{
  	if (params.size() < 1) {
        paramsFailedMsg(windowName, compName);
    } else {
        
        Log.info(" start finding links");
        List matches = searchTypeInDocument("HTMLLink");
		
        if(matches==null || matches.size()==0) { 
            Log.info(" ..... no link found in this HtmlDocument!");
  			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  			this.componentExecutedFailureMessage("no link found in this HtmlDocument");		
		    return;
		}
	    
		Iterator parms = params.iterator();
		
		// the link text
	    String name = (String) parms.next();
	    // what about 0-length link text?

		// get requested matchindex from test table
	    int matchindex = 1;
	    try{ 
	    	if (parms.hasNext()){
		    	String strmatchindex = (String) parms.next();
		    	Log.info("extracted potential MATCHINDEX value of: "+ strmatchindex);
		    	int tindex = Integer.parseInt(strmatchindex);
		    	if (tindex > 0) matchindex = tindex;
	    	}
	    }
	    catch(Exception x){;}
    	Log.info("MATCHINDEX value resolves to: "+ matchindex);

		// see if this is a case-sensitive check
		boolean forcecase = false;
	    try{ 
	    	if(parms.hasNext()){
		    	String strcase = (String) parms.next();
		    	Log.info("extracted potential USECASE value of: "+ strcase);
		    	forcecase = ((strcase.equalsIgnoreCase("CASE-SENSITIVE"))||
		    	    (strcase.equalsIgnoreCase("CASESENSITIVE"))||
		    	    (strcase.equalsIgnoreCase("TRUE")));		    	
	    	}
	    }
	    catch(Exception x){;}
    	Log.info("USECASE value resolves to: "+ forcecase);
	    
        String match = null;
        String linktext = null;
        MatchData data = null;
        GuiTestObject theLink = null;
        String nametest = null;
        String linktest = null;
        
        int matchcount = 0;
        if(!forcecase) nametest = name.toUpperCase();
        else nametest = name;
        
        for(int j= 0;j<matches.size(); j++) {
          linktext = "";
          theLink = null;
          data = null;
          try{
          	data = (MatchData) matches.get(j);
          	theLink = (GuiTestObject) data.getGuiTestObject();
          	linktext = theLink.getProperty(".text").toString();
          	Log.info("Found document link with text: "+ linktext);
            if(!forcecase) linktest = linktext.toUpperCase();
            else linktest = linktext;
          }
          catch(Exception x){continue;}
          
          if (action.equalsIgnoreCase(CLICKLINKBEGINNING)) {
            if (linktest.indexOf(nametest)==0) { //found it
              matchcount++;
              Log.info("matched link: '"+linktext +"' seeking matchindex: "+matchindex +" now at match: "+ matchcount);
              if(matchcount == matchindex){
                  match = linktext;
	              break;
              }
            }
          } else if (action.equalsIgnoreCase(CLICKLINKCONTAINING)) {
            if (linktest.indexOf(nametest)>=0) { //found it
              matchcount++;
              Log.info("matched link: '"+linktext +"' seeking matchindex: "+matchindex +" now at match: "+ matchcount);
              if(matchcount == matchindex){
                  match = linktext;
	              break;                  
              }
            }
          } else if (action.equalsIgnoreCase(CLICKLINK)){
            if (linktest.equals(nametest)) { //found it
                matchcount++;
                Log.info("matched link: '"+linktext +"' seeking matchindex: "+matchindex +" now at match: "+ matchcount);
                if(matchcount == matchindex){
                    match = linktext;
  	              break;                  
                }
              }
          }
        } // end of while
        
        Log.info(" ..... match: "+match);
        if (match == null) {
            Log.info(" ..... no link matches!");
  			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            this.componentExecutedFailureMessage("link " + name + "' not found!");
            return;
        }
        Log.info(" ..... found matched link!");
        
        // click the matched link
        theLink.click();
        
        Log.info(" ..... success to click the matched link!");
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
        this.componentSuccessMessage("success to click '"+name+"'");
      }
  }
 
  /**
   * Note: Compares the HTML source of the BODY tag with that of a benchmark file. 
   * @throws SAFSException
   */
  private void doVerifyBodySource() throws SAFSException{
  	String debugMsg = getClass().getName() + ".doVerifyBodySource(): ";
  	
  	if(params.size()<1){
  		this.paramsFailedMsg(windowName,compName);
  		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  	}else{
  		Iterator iter = params.iterator();
  		//Get bench fileName from the parameter
  		String benchFileName = iter.next().toString().trim();
  		String fileEncoding = null;
  		if(iter.hasNext()){
      		fileEncoding = (String) iter.next();
        	//If user put a blank string as encoding,
        	//we should consider that user does NOT provide a encoding, reset encoding to null.
        	fileEncoding = "".equals(fileEncoding.trim())? null: fileEncoding;
        }
        Log.info("...filename: "+benchFileName+" ; encoding:"+fileEncoding);
        
  		//Get the content of the file
  		File file = (new CaseInsensitiveFile(benchFileName)).toFile();
  		
  		if(!file.isAbsolute()){
  			String parentDir = null;
  			parentDir = this.getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
  			if(parentDir==null) parentDir = "";
  			file = (new CaseInsensitiveFile(parentDir,benchFileName)).toFile();
  			benchFileName = file.getAbsolutePath();
  		}
  		Log.info("Reading benchFile: "+benchFileName);
  		
  		String benchContent = null;
  		try {
  	        //If a file encoding is given or we need to keep the encoding consistent
  	        if (fileEncoding != null || keepEncodingConsistent) {
  	        	benchContent = FileUtilities.readStringFromEncodingFile(benchFileName, fileEncoding);
  	        } else {
  				// Keep compatible with old version
  	        	benchContent = FileUtilities.readStringFromUTF8File(benchFileName);
  			}
		} catch (FileNotFoundException e) {
			Log.debug("Exception occur in "+debugMsg+e.getMessage());
			testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
			throw new SAFSException(e.getMessage());
		} catch (IOException e) {
			Log.debug("Exception occur in "+debugMsg+e.getMessage());
			testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
			throw new SAFSException(e.getMessage());
		}

  		//Get the html body content
		Log.info("Get Html Body content.");
  		String bodyContent = this.getHtmlBodyContent();
  		
  		//Compare file's content and html body's content
  		if(benchContent.equalsIgnoreCase(bodyContent)){
  	        //Log generic succes message
  			String detailMsg = genericText.convert(GENStrings.CONTENT_MATCHES_KEY,
  					"the content of 'Html Body' matches the content of '"+benchFileName+"'","Html Body",benchFileName);
  			this.componentSuccessMessage("*Match *"+detailMsg);
  			//Set success status
  	        testRecordData.setStatusCode(StatusCodes.OK);
  		}else{
  			//Log generic fail message
  			String detailMsg = genericText.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
  					"the content of 'Html Body' does not match the content of '"+benchFileName+"'","Html Body",benchFileName);
  			this.componentExecutedFailureMessage("*Not Match *"+detailMsg);
  	        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  		}
  	}
  }
  
  /**
   * Note: Be used only by doVerifyBodySource() and doCaptureBodySourceToFile()
   * <br><b>Side Effects:</b>
   * 	 If you use proxy to connect Internet,
   * 	 set the proxy.server and proxy.server.port correctly in proxyParameter.properties
   * @return The HTML source of the BODY tag, tag included
   * @throws SAFSException
   */
  private String getHtmlBodyContent() throws SAFSException{
  	BufferedReader input = null;
  	String bodyContent = null;
  	//This object should be our HTML Document object
  	TestObject documentObj = new TestObject(obj1.getObjectReference());
  	String debugMsg = getClass().getSimpleName()+ ".getHtmlBodyContent(): ";

  	try{
  		bodyContent = getObjectProperty(obj1, "outerHTML").toString();
  		if (bodyContent != null) return bodyContent;
  	}
  	catch(Exception np){
  		Log.debug(debugMsg + np.getClass().getSimpleName()+" IGNORED.");
  	}
  	
  	//Get the url of this html document
  	try {
  	  	Properties proxyParameters = new Properties();
  	  	InputStream in = getClass().getResourceAsStream("proxyParameter.properties");
  	  	
  	  	if(in!=null){
  	  		proxyParameters.load(in);
  	  		//Set the correct VM parameter
  	  		Boolean useProxy = new Boolean(proxyParameters.getProperty(USE_PROXY_KEY));
  	  		if(useProxy.booleanValue()){
  	  			System.setProperty("http.proxyHost",proxyParameters.getProperty(PROXY_SERVER_KEY));
  	  			System.setProperty("http.proxyPort",proxyParameters.getProperty(PROXY_SERVER_PORT_KEY));
  	  		}
  	  	}else{
  	  		Log.debug(debugMsg+" Can not load proxy parameters");
  	  	}
  		
  	  	//S0531946, Try to get the parent of 'Html.Body' which is 'Html.HtmlDocument'
  	  	do{
  	  		String className = documentObj.getObjectClassName();
  	  		Log.debug(debugMsg+className);
  	  		if(!className.toUpperCase().startsWith("HTML")){
  				String message = failedText.convert(FAILStrings.FAILURE_2, "Unable to perform "+action+" on"+className,className,action);
  				Log.debug(debugMsg+message+". Can not perform on non HTML object.");
  				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  				throw new SAFSException(message);
  	  		}
  	  		if(className.equalsIgnoreCase(HTML_HTMLDOCUMENT)) break;
  	  		else if(className.equalsIgnoreCase(HTML_HTMLBROWSER)){
  				String message = failedText.convert(FAILStrings.SOMETHING_NOT_FOUND, HTML_HTMLDOCUMENT+" was not found.",HTML_HTMLDOCUMENT);
  				Log.debug(debugMsg+message+". We have reached the top parent "+HTML_HTMLBROWSER+".");
  				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  				throw new SAFSException(message);
  	  		}else documentObj = documentObj.getParent();
  	  	}while(true);
  	  	
  	  	
  		//Get the whole content of this url from 'Html.Docuemnt'
		URL url = null;
		try{
			url = new URL(documentObj.getProperty(".url").toString());
		}catch(PropertyNotFoundException pne1){
			Log.info(debugMsg+ "can not get property '.url'!");
			try{
				url = new URL(documentObj.getProperty("url").toString());
			}catch(PropertyNotFoundException pne2){
				Log.info(debugMsg+ "can not get property 'url'!");
				try{
					url = new URL(documentObj.getProperty("URLUnencoded").toString());
				}catch(PropertyNotFoundException pne3){
					Log.info(debugMsg+ "can not get property 'URLUnencoded'!");
				}
			}
		}
		
		if(url==null){
			String message = failedText.text(FAILStrings.PROPERTY_NOT_FOUND, "Can not find property.")+" .url or url or URLUnencoded!";
			Log.debug(debugMsg+message);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			throw new SAFSException(message);
		}else{
			Log.info(debugMsg+ " url is "+url.toString());
		}
		
		input = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuffer htmlContent = new StringBuffer();
		String tmp = input.readLine();
		while(tmp!=null){
			htmlContent.append(tmp);
			tmp = input.readLine();
		}
		//Use regular expression to get the body content
		Pattern bodyPattern = Pattern.compile("(<body.*</body>)",Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher bodyMatcher = bodyPattern.matcher(htmlContent.toString());
		
		if(bodyMatcher.find()){
			bodyContent = bodyMatcher.group(1);
			Log.info("Html Body Content found");
		}else{
			bodyContent = "";
			Log.info("Html Body Content not found");
		}
		input.close();
	} catch (MalformedURLException e) {
		Log.error("Exception occur in "+debugMsg+e.getMessage());
		testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
		throw new SAFSException(e.getMessage());
	} catch (IOException e) {
		Log.error("Exception occur in "+debugMsg+e.getMessage());
		testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
		throw new SAFSException(e.getMessage());
	}finally{
		if(input!=null)
			try {
				input.close();
			} catch (IOException e1) {
			}
	}
	
  	return bodyContent;
  }

  /**
   * Note: Write the HTML source of the BODY tag to a test file.
   * @throws SAFSException
   */
  private void doCaptureBodySourceToFile() throws SAFSException{
  	String debugMsg = getClass().getName()+ ".doCaptureBodySourceToFile(): ";
  	if(params.size()<1){
  		this.paramsFailedMsg(windowName,compName);
  		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  	}else{
  		Iterator iter = params.iterator();
  		//Get bench fileName from the parameter
  		String testFileName = iter.next().toString().trim();
        String encoding = null;
  		if(iter.hasNext()){
      		encoding = (String) iter.next();
        	//If user put a blank string as encoding,
        	//we should consider that user does NOT provide a encoding, reset encoding to null.
        	encoding = "".equals(encoding.trim())? null: encoding;
        }
        Log.info("...filename: "+testFileName+" ; encoding:"+encoding);
  		//Get the content of the file
  		File file = (new CaseInsensitiveFile(testFileName)).toFile();
  		
  		if(!file.isAbsolute()){
  			String parentDir = null;
  			parentDir = this.getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
  			if(parentDir==null) parentDir = "";
  			file = (new CaseInsensitiveFile(parentDir,testFileName)).toFile();
  			testFileName = file.getAbsolutePath();
  		}
  		Log.info("Save to file: "+testFileName);
  		
  		try {
  		    //If a file encoding is given or we need to keep the encoding consistent
  		    if(encoding!=null || keepEncodingConsistent){
  		    	FileUtilities.writeStringToFile(testFileName, encoding, getHtmlBodyContent());
  		    }else{
  		    	//Keep compatible with old version
  		    	FileUtilities.writeStringToUTF8File(testFileName, getHtmlBodyContent());
  		    }
		} catch (FileNotFoundException e) {
			Log.error("Exception occur in "+debugMsg+e.getMessage());
			testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
			throw new SAFSException(e.getMessage());
		} catch (IOException e) {
			Log.error("Exception occur in "+debugMsg+e.getMessage());
			testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
			throw new SAFSException(e.getMessage());
		}
		this.componentSuccessMessage("");		
		testRecordData.setStatusCode(StatusCodes.OK);
  	}
  }
   /** 
   * Purpose: perform action CaptureBodyTextToFile, capture the body text in target HtmlDocument and 
   *           write the content to a new file with UTF8 format. VerifyBodyText shall use the UTF8 file for comparing.
   * @see doVerifyBodyText
   * @exception SAFSException 
   **/ 
  protected void doCaptureBodyTextToFile() throws SAFSException {
    String debugMsg = getClass().getName() + ".doCaptureBodyTextToFile: ";        
    if (params.size() < 1) {
        paramsFailedMsg(windowName, compName);
        return;
    }

    Iterator iter = params.iterator();
    String filename = iter.next().toString().trim();
    String encoding = null;
  	if(iter.hasNext()){
    	encoding = (String) iter.next();
        //If user put a blank string as encoding,
        //we should consider that user does NOT provide a encoding, reset encoding to null.
        encoding = "".equals(encoding.trim())? null: encoding;
    }
    Log.info("...filename: "+filename+" ; encoding:"+encoding);
       
    File fn = new CaseInsensitiveFile(filename).toFile();
    if (!fn.isAbsolute()) {
        String pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
        if (pdir == null) pdir="";
          	fn = new CaseInsensitiveFile(pdir, filename).toFile();
            filename = fn.getAbsolutePath();
    }
   	Log.info(" ..... output body text filename: " + filename+" encoding:"+encoding);
        
  	// capture body text with property .text
    String textInf = null;
  	try{
	    Log.info(" ..... get body text by property 'innerText'");
  		textInf = getObjectProperty(obj1, "innerText").toString();
  	}
  	catch(Exception np){
  		Log.debug(debugMsg + np.getClass().getSimpleName()+" IGNORED.");
  	}  	
  	if(textInf == null){
	    // This object should be our HTML Document object
	    try{
		    Log.info(" ..... get body text by property '.text'");
		    GuiTestObject guiObj = new GuiTestObject(obj1);
		    textInf = (String) getObjectProperty(guiObj,".text");
	    }
	  	catch(Exception np){
	  		Log.debug(debugMsg + np.getClass().getSimpleName()+" IGNORED.");
	  	}  	
  	}
        
    try {
        Log.info(" ..... write to file: " + filename); 
        //If a file encoding is given or we need to keep the encoding consistent
        if (encoding != null || keepEncodingConsistent) {
        	FileUtilities.writeStringToFile(filename, encoding, textInf);
        } else {
			// Keep compatible with old version
			FileUtilities.writeStringToUTF8File(filename, textInf);
		}

        Log.info(" ..... success writing bodytext to file:" + filename);
        // set status to ok
        testRecordData.setStatusCode(StatusCodes.OK);
	    this.componentSuccessMessage("success to write to file: "+ filename);
    
    } catch (FileNotFoundException fe) {
		Log.debug("Exception occur in "+debugMsg+fe.getMessage());
        testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
        this.componentSuccessMessage(fe.getMessage());
    } catch (IOException ioe) {
		Log.debug("Exception occur in "+debugMsg+ioe.getMessage());
        testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
		this.componentSuccessMessage(ioe.getMessage());
    }

  }
  
  /** 
   * Purpose: perform action VerifyBodyText, capture body text in target HtmlDocument, then compare the content captured 
   *          with the content of a bench file (UTF8 formated).  The bench file was already created by doCaptureBodyTextToFile.
   *          Expected a benchmark file in UTF8 format.       
   * @see doCaptureBodyTextToFile
   * @exception SAFSException,  when catching a non-SAFSException, a new SAFSException with error message will be thrown.  
   **/  
  protected void doVerifyBodyText() throws SAFSException {
    String debugMsg = getClass().getName() + ".doVerifyBodyText(): ";       
    if (params.size() < 1) {
        paramsFailedMsg(windowName, compName);
    } else {
    	Iterator iter = params.iterator();
    	// get bench file on the first param
    	String benchfilename = (String) iter.next();
    	String fileEncoding = null;
	  	if(iter.hasNext()){
	  		fileEncoding = (String) iter.next();
        	//If user put a blank string as encoding,
	        //we should consider that user does NOT provide a encoding, reset encoding to null.
    	    fileEncoding = "".equals(fileEncoding.trim())? null: fileEncoding;
	    }
	    Log.info("...filename: "+benchfilename+" ; encoding:"+fileEncoding);
	    
    	File fn = new CaseInsensitiveFile(benchfilename).toFile();
      
    	if (!fn.isAbsolute()) {
            String pdir = getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
            if (pdir == null) pdir="";
            fn = new CaseInsensitiveFile(pdir, benchfilename).toFile();
        }
        benchfilename = fn.getAbsolutePath();  
    	Log.info(" ..... benchfile filename: " + benchfilename);
    	
        String textInf = null;
      	try{
    	    Log.info(" ..... get body text by property 'innerText'");
      		textInf = getObjectProperty(obj1, "innerText").toString();
      	}
      	catch(Exception np){
      		Log.debug(debugMsg + np.getClass().getSimpleName()+" IGNORED.");
      	}  	
    	
        // Capture body .text
      	if(textInf == null){
	        // This object should be our HTML Document object
          	try{
        	    Log.info(" ..... get body text by property '.text'");
		        GuiTestObject guiObj = new GuiTestObject(obj1);
		        textInf = (String) getObjectProperty(guiObj,".text");
          	}
          	catch(Exception np){
          		Log.debug(debugMsg + np.getClass().getSimpleName()+" IGNORED.");
          	}  	
      	}
        
        Collection testcontents = new ArrayList();
        Collection benchcontents = new ArrayList();  

        // load source and target for verifying 
   		try {
   	        testcontents = StringUtils.readstring(textInf);
  	        //If a file encoding is given or we need to keep the encoding consistent
  	        if (fileEncoding != null || keepEncodingConsistent) {
  	        	benchcontents = StringUtils.readEncodingfile(benchfilename, fileEncoding);
  	        } else {
  				// Keep compatible with old version
  	        	benchcontents = StringUtils.readUTF8file(benchfilename);
  			}
		} catch (FileNotFoundException e) {
			testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
		    Log.debug("Exception occur in "+debugMsg+e.getMessage());
			throw new SAFSException(e.getMessage());
		} catch (IOException e) {
			Log.debug("Exception occur in "+debugMsg+e.getMessage());
			testRecordData.setStatusCode(StatusCodes.INVALID_FILE_IO);
			throw new SAFSException(e.getMessage());
		}

		Log.info(" ..... compare with file:" + benchfilename); 
        // now compare the two files
		if (!benchcontents.equals(testcontents)) {
            Log.info(" ..... does't match the benchfile"); 

            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
  			String detailMsg = genericText.convert(GENStrings.CONTENT_NOT_MATCHES_KEY, 
  													"the content of 'Body Text' does not match the content of '"+benchfilename+"'", 
  													"Body Text", 
  													benchfilename);
  			this.componentExecutedFailureMessage("*Not Match* " + detailMsg);
        } else {
            Log.info(" ..... matches the benchfile"); 
            
            // set status to ok
            testRecordData.setStatusCode(StatusCodes.OK);
  			String detailMsg = genericText.convert(GENStrings.CONTENT_MATCHES_KEY, 
  													"the content of 'Body Text' matches the content of '"+benchfilename+"'", 
  													"Body Text", 
  													benchfilename);
  			this.componentSuccessMessage("*Match* " + detailMsg);
        }
    }
  }
 
  /**
   * Purpose: Search a specified object in HtmlDocument. Called by doLink().
   * @param type: the object type that we search in htmlDocument
   * @return a list<MatchData> of matched objects in htmlDocument
   */
  private List searchTypeInDocument(String type){
  	List matches = null;
  	StringBuffer regcStr = new StringBuffer("Type=");
  	
  	if(type==null)
  		return null;
  	regcStr.append(type);
  	
  	// this should be our HTML Document object
    GuiTestObject guiObj = new GuiTestObject(obj1);
    RGuiObjectVector search = new RGuiObjectVector(windowName, 
                                                   compName, 
                                                   regcStr.toString(), 
                                                   script);
    
    // the input Vector is NOT the same as the output Vector
	matches = search.getChildMatchData(guiObj, new java.util.Vector());

	return matches;
  }
  
} 
