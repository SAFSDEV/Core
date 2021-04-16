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
package org.safs.text;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** 
 * Contain information of NLS message in resource bundle.<br>
 * 
 * @author Lei Wang, SAS Institute, Inc
 * 		   Lei Wang SEP 10, 2012 Add method clone().
 *
 */
public class ResourceMessageInfo implements Cloneable{
	
	public static final String BUNDLENAME_FAILEDTEXT  = "org.safs.text.FAILStrings";
	public static final String BUNDLENAME_GENERICTEXT = "org.safs.text.GENStrings";
	
	/**
	 * resource bundle name
	 */
	private String resourceBundleName = null;
	/**
	 * the key in the resource bundle file
	 */
	private String key = null;
	/**
	 * the values used to replace the parameters of message in resource bundle<br>
	 * Refer to SAFSTextResourceBundle_en_US.properties:<br>
	 * There is a message like, "Unable to perform %2% on %1%", this field params will<br>
	 * contain 2 values to replace %2% and %1%<br>
	 */
	private List<String> params = null;
	
	private String altText = null;
	  
	public ResourceMessageInfo(){}
	
	public ResourceMessageInfo(String key){
		this.key = key;
	}
	
	public ResourceMessageInfo(String resourceBundleName, String key){
		this.resourceBundleName = resourceBundleName;
		this.key = key;
	}
	
	public ResourceMessageInfo(String resourceBundleName, String key,
			List<String> params) {
		this.resourceBundleName = resourceBundleName;
		this.key = key;
		this.params = params;
	}
	
	public void reset(){
		this.resourceBundleName = null;
		this.key = null;
		if(params!=null) params.clear();
		this.altText = null;
	}

	public String getResourceBundleName() {
		return resourceBundleName;
	}

	public void setResourceBundleName(String resourceBundleName) {
		this.resourceBundleName = resourceBundleName;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<String> getParams() {
		return params;
	}
	
	public void setParams(List<String> params) {
		this.params = params;
	}
	
	public void setParams(String[] sparams) {
		if(params == null) params = new ArrayList<String>();
		params.clear();
		try{ for(int i=0;i<sparams.length;i++){	addParameter(sparams[i]); }}
		catch(Exception x){}
	}
	
	public void addParameter(String value){
		if(params==null){
			params = new ArrayList<String>();
		}
		params.add(value);
	}

	public String getAltText() {
		return altText;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	/**
	 * The field {@link #params} must be set before calling this method.<br>
	 * The {@link #params} will contain the values to replace the placeholders in alternative text.<br>
	 * 
	 * @param altTextWithPlaceholder	String, text with placeholder as %1%, %2% etc.
	 * @param placeHolderDelim 			String, such as %, used to replace placeholder by real parameter value.
	 * 
	 * @see #params
	 */
	public void setAltText(String altTextWithPlaceholder, String placeHolderDelim) {
		if(params==null || altTextWithPlaceholder==null) return;
	    
	    int size = params.size();
	    StringBuffer buf = new StringBuffer(altTextWithPlaceholder.length() + size*10);
	    StringTokenizer st = new StringTokenizer(altTextWithPlaceholder, placeHolderDelim, true);
	    //Each placeholder has 2 delimiters, between 2 delimiters there is a number.
	    //isPlaceHolder indicates whether the next token is placeholder or normal text.
	    //At the first, isPlaceHolder should be false. When we meet the first delimiter, isPlaceHolder will
	    //be set to true; when we meet the second delimiter, isPlaceHolder will set to false. And so on.
	    boolean isPlaceHolder = false;
	    while (st.hasMoreTokens()) {
	      String next = st.nextToken();
	      if (next.equals(placeHolderDelim)) {
	    	  //isPlaceHolder will change alternatively between true and false when meeting delimiter
	    	  isPlaceHolder = !isPlaceHolder;
	      } else {
	        if (isPlaceHolder) {
	          try {
	            int num = Integer.parseInt(next);
	            if(num>size){//If no more value in the params array, just append the placeholder
	            	buf.append(placeHolderDelim+next+placeHolderDelim);
	            }else{
	            	buf.append(params.get(num-1));
	            }
	          } catch (NumberFormatException nfe) {
	          }
	        }else{
	        	buf.append(next);
	        }
	      }
	    }
	    
	    this.altText = buf.toString();
	}
	

	/**
	 * Attempt to get and convert the desired message out of the resource bundle and return 
	 * it as a String with all parameters substituted.  If no resourceBundleName has been assigned, 
	 * then the routine will assume it is the FAILEDTEXT resource bundle to use by default.
	 * @return the localized String, or null if a successful conversion could not be made.
	 */
	public String getMessage(){
		if(key == null) return null;
		if(resourceBundleName == null) resourceBundleName = BUNDLENAME_FAILEDTEXT;
		String alttext = this.altText;
		//try to make some kind of alternative string--though it won't be pretty.
		if(alttext==null){
			alttext = key;
			List<String> theparams = getParams();
			if(theparams != null){ 
				for(int i=0;i<theparams.size();i++){ 
					alttext += " "+ theparams.get(i); 
				}
			}			
		}
		//support short bundle names like "failstrings" or "genstrings"		
		if(BUNDLENAME_FAILEDTEXT.toLowerCase().contains(resourceBundleName.toLowerCase())){
			try{ return FAILStrings.convert(getKey(), alttext, getParams()); }
			catch(Throwable no_class_or_method){ return null; }
		}else if(BUNDLENAME_GENERICTEXT.toLowerCase().contains(resourceBundleName.toLowerCase())){
			try{ return GENStrings.convert(getKey(), alttext, getParams()); }
			catch(Throwable no_class_or_method){ return null; }
		}else{
			return null;
		}
	}
	
	/**
	 * When we assign the instance of ResourceMessageInfo to an other object, we
	 * should assign the cloned one.
	 */
	public ResourceMessageInfo clone(){
		ResourceMessageInfo clonedObject = null;
		try{
			clonedObject = (ResourceMessageInfo) super.clone();
		}catch(Exception e){
			clonedObject = new ResourceMessageInfo();
			clonedObject.setKey(this.getKey());
			clonedObject.setAltText(this.altText);
			clonedObject.setResourceBundleName(this.getResourceBundleName());
		}	
		
		if(params!=null && params.size()>0){
			ArrayList<String> temp = new ArrayList<String>();
			for(int i=0;i<params.size();i++){
				temp.add(params.get(i));
			}
			clonedObject.setParams(temp);
		}
		
		return clonedObject;
	}
}
