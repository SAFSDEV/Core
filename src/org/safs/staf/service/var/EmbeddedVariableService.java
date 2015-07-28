/**
 * 
 */
package org.safs.staf.service.var;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.safs.Log;
import org.safs.staf.embedded.EmbeddedServiceHandle;
import org.safs.staf.embedded.ServiceInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.InfoInterface.RequestInfo;
import org.safs.text.CaseInsensitiveHashtable;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFMarshallingContext;
import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;

/**
 * @author canagl
 *
 */
public class EmbeddedVariableService extends AbstractSAFSVariableService implements ServiceInterface {
	
	protected boolean embedVars = false;
	protected Hashtable<String, String> localvars = new CaseInsensitiveHashtable(150);
 	protected STAFCommandParser localparser = new STAFCommandParser(5);


	/**
	 * 
	 */
	public EmbeddedVariableService() { }

	protected void registerHandle(String handleId)throws STAFException{
		client = new EmbeddedServiceHandle(handleId, servicename, this);
		((EmbeddedServiceHandle)client).register();
	}
	
	/**
	 * Set true to tell the service to store variable values locally and not use the STAF VAR service.
	 * @param bool
	 */
	public void setEmbedVars(boolean bool){ embedVars = bool; }

	/**
	 * Intercepts initializing the instance of the service to get servicename information.
	 * <p>
	 * This service is registered under process name 
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	public STAFResult init(InfoInterface.InitInfo initInfo)
	{
		localparser.addOption( SVS_SERVICE_REQUEST_GET      , 1, STAFCommandParser.VALUENOTALLOWED );
		localparser.addOption( SVS_SERVICE_REQUEST_SET      , 1, STAFCommandParser.VALUENOTALLOWED );
		localparser.addOption( SVS_SERVICE_REQUEST_DELETE   , 1, STAFCommandParser.VALUENOTALLOWED );
		localparser.addOption( "handle"                     , 1, STAFCommandParser.VALUEREQUIRED );
		localparser.addOption( "var"                        , 1, STAFCommandParser.VALUEREQUIRED );
		
		try {
			servicename = initInfo.name;
			serviceparms = initInfo.parms;
			registerHandle("STAF/Service/" + servicename);
		} catch (STAFException e) {
			return new STAFResult(STAFResult.STAFRegistrationError);
		}
		int code = doInit(client, servicename, serviceparms);
		return new STAFResult(code);		
	}
	
	/* (non-Javadoc)
	 * @see org.safs.staf.embedded.ServiceInterface#acceptRequest(org.safs.staf.service.InfoInterface.RequestInfo)
	 */
	@Override
	public STAFResult acceptRequest(RequestInfo info) {
		return doAcceptRequest(info.request);
	}

	/**********************************************************************
	 * 	Handle the request to shutdown the service from STAF
	 **********************************************************************/
	public final STAFResult  term(){
		// CANAGL -- removed "clients" code to increment/decrement "clients"
		// This may have to be reinserted.  I'm not sure why it was present.
		try{client.unRegister();client = null;}
		catch(STAFException ex){;}
		return new STAFResult(0);
	}	

	protected String stafGetCommmd(String varname) { 
		return "GET handle "+client.getHandle() +" var "+ varname;
	} 	
	
	protected String stafSetCommmd(String assignExp) { 
		return "SET handle "+client.getHandle() +" var "+ assignExp;
	} 
	
	protected String stafDeleteCommand(String varname) { 
		return "DELETE handle "+client.getHandle() +" var "+ varname; 
	} 	
	
	protected String stafListCommand() {
		return "handle "+ client.getHandle() +" LIST";
	}	
	
	@Override
	protected STAFResult getStoredValue(String getCmd){
		if(! embedVars) return super.getStoredValue(getCmd);
		STAFCommandParseResult parsedData = localparser.parse(getCmd);
		String varname = parsedData.optionValue("var");
		String varvalue = localvars.get(varname);
		if( varvalue == null )
			return new STAFResult(STAFResult.VariableDoesNotExist, varname);
		return new STAFResult(STAFResult.Ok, varvalue);
	}

	@Override
	protected STAFResult setStoredValue(String setCmd){
		if(! embedVars) return super.setStoredValue(setCmd);
		STAFCommandParseResult parsedData = localparser.parse(setCmd);
		String expression = parsedData.optionValue("var");
		int iassign = expression.indexOf(eq);
		if(iassign < 1) {
			return new STAFResult(STAFResult.InvalidRequestString, setCmd);
		}
		String varname = expression.substring(0, iassign);
		String varvalue = expression.length() == iassign+1 ? "" : expression.substring(iassign+1); 
		localvars.put(varname, varvalue);
		return new STAFResult(STAFResult.Ok);
	}

	/**
	 * Made to be overridden by subclasses, if any.
	 * @param deleteCmd
	 * @return STAFResult
	 */
	@Override
	protected STAFResult deleteStoredVariable(String deleteCmd){
		if(! embedVars) return super.deleteStoredVariable(deleteCmd);
		STAFCommandParseResult parsedData = localparser.parse(deleteCmd);
		String varname = parsedData.optionValue("var");
		String varvalue = localvars.get(varname);
		if( varvalue == null )
			return new STAFResult(STAFResult.VariableDoesNotExist, varname);
		localvars.remove(varname);
		return new STAFResult(STAFResult.Ok);
	}
	
	
	private STAFResult localCount(){
		return new STAFResult(STAFResult.Ok, String.valueOf(localvars.size()));
	}
	
	protected STAFResult handleCount() {
		if (embedVars) return localCount();
		STAFResult result = client.submit2("local", "var", stafListCommand());

		if (result.rc == STAFResult.Ok){
            STAFMarshallingContext mc = STAFMarshallingContext.unmarshall(result.result);
            HashMap resultMap = (HashMap)mc.getRootObject();			
			int size = resultMap.size()-1; // remove entry "staf-map-class-name" that is a reserved variable.
			result.result = String.valueOf(size);
		}
		return result;
	}
	
	private STAFResult localReset(){
		localvars.clear();
		return new STAFResult(STAFResult.Ok);
	}
	
	protected STAFResult handleReset() {
		if(embedVars) return localReset();
		STAFResult result = client.submit2("local", "var", stafListCommand());

		if (result.rc == STAFResult.Ok) {
            STAFMarshallingContext mc = STAFMarshallingContext.unmarshall(result.result);
            HashMap resultMap = (HashMap)mc.getRootObject();
            Iterator iter = resultMap.entrySet().iterator();
            String delete = "DELETE handle "+client.getHandle();
            while (iter.hasNext()){
            	java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
                String varname = (String)entry.getKey();
                if (!varname.equalsIgnoreCase(SAFSVariableService3.SVS3_RESERVED_VARS_KEY)) // reserved variable can't be deleted 
                	delete += " var " + varname;
            }  
            result = client.submit2("local", "var", delete);
		} 
		return result;
	}

	private STAFResult localList(){
		STAFResult rc = new STAFResult(STAFResult.Ok, "");
		for(String key: localvars.keySet()){
			rc.result += key +"="+ localvars.get(key) +"\n\r";
		}
		return rc;
	}
	
	/**
	 * Return the STAF VAR LIST STAFResult.result in marshalled V3 format UNLESS the 
	 * V2 command option was given.  
	 * If so, return the data in the original V2 format of key=value\n\r for each item.
	 */
	protected STAFResult handleList(STAFCommandParseResult parsedData) {
		if(embedVars) return localList();
		boolean convertV2 = parsedData.optionTimes(SVS_SERVICE_REQUEST_V2) > 0;
		STAFResult result = client.submit2("local", "var", stafListCommand());
		if (result.rc == STAFResult.Ok && convertV2) {
            STAFMarshallingContext mc = STAFMarshallingContext.unmarshall(result.result);
            HashMap resultMap = (HashMap)mc.getRootObject();
            Iterator iter = resultMap.entrySet().iterator();
            String varname;
            String varval;
            result.result = "";
            while (iter.hasNext()){
            	java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
                varname = (String)entry.getKey();
                varval = (String)entry.getValue();
                result.result += varname +"="+varval +"\n\r";
            }  
		} 
		return result;
	}

	@Override
	public STAFResult terminateService() {		
		return term();
	}	
}
