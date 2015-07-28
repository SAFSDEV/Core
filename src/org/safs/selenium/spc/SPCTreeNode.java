package org.safs.selenium.spc;

import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.WDLibrary;

/**
 * 
 * AUG 10, 2012		(Lei Wang) Add two fields: id and name, for record property value of HTML element.
 * JAN 28, 2015     (Carl Nagle) Adding support for Frames.
 */
public class SPCTreeNode extends DefaultMutableTreeNode{
	
	/** If the item is in a frame, a reference to that frame will be stored here. */
	public SPCTreeNode frame = null;
	
	/** full hierarchy xpath */
	public String xpath = null;
	
	/** last child xpath portion only. */
	public String xpart = null;
	
	public Rectangle bounds = null;
	
	/** Stores true/false that the item was visible during the last query. */
	private boolean visible = false;
	
	/** Stores "HTML", "DOJO", or "SAP" domain string. */
	private String domain = WDLibrary.DOMAIN_HTML;// can also be DOJO or SAP
	
	/** Stores domain-specific recognition string, if any. For SAP and DOJO. */
	private String domainRecognition = null;
	
	/** Stores domain-specific class, if any. For SAP and DOJO. */
	private String domainClass = null;
	
	/** Stores mapped compType string, if any. */
	private String compType = null;
	
	/**
	 * recognitionString: user-preferred recognition string for the element
	 */
	private String recognitionString;
	/**
	 * id: it contains the value of property id of a HTML element
	 * For example: <tag id="idValue">
	 */
	private String id = null;
	/**
	 * name: it contains the value of property name of a HTML element
	 * For example: <tag name="nameValue">
	 */
	private String name = null;
	
	/**
	 * text: it contains the retrievable text value of HTML element, if any.
	 */
	private String text = null;
	
	/**
	 * title: it contains the retrievable title value of HTML element, if any.
	 * For example: <tag title="titleValue">
	 */
	private String title = null;
	
	/**
	 * tag: it contains the element type name.
	 * For example: div, span, textarea, input, etc..
	 */
	private String tag = null;
	
	/**
	 * holds a subType for a tag (such as 'input' that might have an attribute containing 
	 * a subcategory of information about the element type.
	 */
	private String subType = null;
	
	/**
	 * attrClass: it contains the retrievable class value of HTML element, if any.
	 * For example: <tag class="classValue">
	 */
	private String attrClass = null;

	public SPCTreeNode(){ }
	
	
	/**
	 * Expects a String[5] containing:
	 * <p><pre>
	 * all[0] = xpath
	 * all[1] = x
	 * all[2] = y
	 * all[3] = w
	 * all[4] = h
	 * </pre>
	 * @param all
	 */
	public SPCTreeNode(String [] all){
		this.xpath = all[0];
		int x=0, y=0, w=0, h=0;
		try{ x = Integer.parseInt(all[1]);}catch(Throwable t){} 
		try{ y = Integer.parseInt(all[2]);}catch(Throwable t){} 
		try{ w = Integer.parseInt(all[3]);}catch(Throwable t){} 
		try{ h = Integer.parseInt(all[4]);}catch(Throwable t){} 
		bounds = new Rectangle(x,y,w,h);
//		this.xpart = xpath.substring(xpath.lastIndexOf("/")+1);
		this.xpart = StringUtils.breakXpath(xpath, true, false)[0];
	}
	
	public SPCTreeNode findChild(String xpart){
		Enumeration e = children();
		SPCTreeNode temp = null;
		while(e.hasMoreElements()){
			temp = (SPCTreeNode)e.nextElement();
			if(temp.xpart.equals(xpart)){
				return temp;
			}
		}
		return null;
	}
	
	public SPCTreeNode findFrame(String frameRS){
		Enumeration e = children();
		SPCTreeNode temp = null;
		while(e.hasMoreElements()){
			temp = (SPCTreeNode)e.nextElement();
			IndependantLog.info("TreeNode.findFrame testing node with xpath: "+ temp.getXpath());
			String rs = temp.getRecognitionString();
			IndependantLog.info("TreeNode.findFrame testing node with xpath: "+ temp.getXpath()+", rs: "+ rs);
			if(frameRS.equals(rs)){
				return temp;
			}
			Enumeration e2 = temp.children();
			if(e2 != null && e2.hasMoreElements()){
				IndependantLog.info("TreeNode.findFrame testing children of xpath: "+ temp.getXpath());
				temp = temp.findFrame(frameRS);
				if(temp != null) return temp;
			}
		}
		return null;
	}
	
	public String getRecognitionString() {
		return recognitionString;
	}

	public void setRecognitionString(String recognitionString) {
		this.recognitionString = recognitionString;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getAttrClass() {
		return attrClass;
	}

	public void setAttrClass(String attrClass) {
		this.attrClass = attrClass;
		
	}
	
	/**
	 * This ends up being the visible node text in the TreePanel.
	 * !Don't like it!
	 */
	public String toString(){
		return xpart;
	}


	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}


	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}


	/**
	 * @return the domainRecognition
	 */
	public String getDomainRecognition() {
		return domainRecognition;
	}


	/**
	 * @param domainRecognition the domainRecognition to set
	 */
	public void setDomainRecognition(String domainRecognition) {
		this.domainRecognition = domainRecognition;
	}

	/**
	 * @return the domainClass
	 */
	public String getDomainClass() {
		return domainClass;
	}


	/**
	 * @param domainClass the domainClass to set
	 */
	public void setDomainClass(String domainClass) {
		this.domainClass = domainClass;
	}

	/**
	 * @return the compType
	 */
	public String getCompType() {
		return compType;
	}

	/**
	 * @param compType the mapped compType to set
	 */
	public void setCompType(String compType) {
		this.compType = compType;
	}


	/**
	 * @return the subType
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * @param subType the element subType, if any.  For example, input element 'type' attribute.
	 */
	public void setSubType(String subType) {
		this.subType = subType;
	}


	/**
	 * @return whether the item was visible during the last query.
	 */
	public boolean isVisible() {
		return visible;
	}


	/**
	 * Set true if the item was visible during last query of this state.
	 * @param visible -- 
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/** "@CLASS='" */
	String CLASS_XPATTR = "@CLASS='";
	/** "@ID='" */
	String ID_XPATTR = "@ID='";
	/** "@NAME='" */
	String NAME_XPATTR = "@NAME='";
	/** "CLASS=" */
	String CLASS_SFATTR = "CLASS=";
	/** "ID=" */
	String ID_SFATTR = "ID=";
	/** "NAME=" */
	String NAME_SFATTR = "NAME=";
	
	/**
	 * Generate a simple name for an element that can be temporarily used in an App Map.
	 * Normally, the user will override or completely rename the component upon saving it 
	 * to an App Map.
	 * 
	 * @return a name.  Default "COMPNAME" if we don't get something better.
	 */
	public String generateComponentName(){
		String debugmsg = "SPCTreeNode.generateComponentName: ";
		String rs = getRecognitionString();
		String nametemp = "";
		String rec = null;
		int index = -1;
		int end = -1;
		// if rec contains id=
		if(rs!=null && rs.length()> 0){
			String ucrs = rs.toUpperCase();
			// if XPATH=...
			if(ucrs.startsWith(SearchObject.SEARCH_CRITERIA_XPATH)){
				rec = rs.substring(SearchObject.SEARCH_CRITERIA_XPATH.length()+1).trim();
				ucrs = rec.toUpperCase();
				try{
					// ends with /a  or /a[@name='something']
					index = rec.lastIndexOf("/");
					if (index >= 0) {
						rec = rec.substring(index+1);
						ucrs = rec.toUpperCase();
					}
					index = ucrs.indexOf(CLASS_XPATTR);
					if(index > 0){						
						end = rec.indexOf("'", index+CLASS_XPATTR.length());
						try{ nametemp += rec.substring(index+CLASS_XPATTR.length(), end);}catch(Exception x){}
					}
					index = ucrs.indexOf(ID_XPATTR);
					if(index > 0){						
						end = rec.indexOf("'", index+ID_XPATTR.length());
						try{ nametemp += rec.substring(index+ID_XPATTR.length(), end);}catch(Exception x){}
					}else{
						index = ucrs.indexOf(NAME_XPATTR);
						if(index > 0){						
							end = rec.indexOf("'", index+NAME_XPATTR.length());
							try{ nametemp += rec.substring(index+NAME_XPATTR.length(), end);}catch(Exception x){}
						}
					}
				}catch(Exception np){
					
				}
			}
			// NOT XPATH?
			else {
				// examples:
				// SAPCompType;id=anId
				// id=anId
				// id=anId;class=aClass
				// id=aParentId;class=aParentClass;\;SAPCompType;id=anId;class=aClass
				try{
					// ends with /a  or /a[@name='something']
					index = rec.lastIndexOf(";\\;");
					if (index >= 0) {
						rec = rec.substring(index+3);
						ucrs = rec.toUpperCase();
					}
					index = ucrs.indexOf(CLASS_SFATTR);
					if(index > 0){						
						end = rec.indexOf(";", index+CLASS_SFATTR.length());
						if(end < index) end = ucrs.length();
						try{ nametemp += rec.substring(index+CLASS_SFATTR.length(), end).trim();}catch(Exception x){}
					}
					index = ucrs.indexOf(ID_SFATTR);
					if(index > 0){						
						end = rec.indexOf(";", index+ID_SFATTR.length());
						if(end < index) end = ucrs.length();
						try{ nametemp += rec.substring(index+ID_XPATTR.length(), end).trim();}catch(Exception x){}
					}else{
						index = ucrs.indexOf(NAME_SFATTR);
						if(index > 0){						
							end = rec.indexOf("'", index+NAME_SFATTR.length());
							if(end < index) end = ucrs.length();
							try{ nametemp += rec.substring(index+NAME_SFATTR.length(), end).trim();}catch(Exception x){}
						}
					}
				}catch(Exception np){
					
				}
			}			
		}
		if(nametemp.length()==0){
			try{
				rec = getDomain();				
				if(rec != null && rec.length()>0 && !rec.equals(SearchObject.DOMAIN_HTML)) 
					nametemp += rec;
			}catch(Exception x){}
			try{
				rec = getCompType();
				if(rec != null && rec.length()>0) nametemp += rec;
			}catch(Exception x){}
			if(nametemp.length()==0){
				try{
					rec = getSubType();
					if(rec != null && rec.length()>0) nametemp += rec;
				}catch(Exception x){}
			}
			if(nametemp.length()==0){
				try{
					rec = SPCUtilities.getRobotTag(getTag());
					nametemp += rec == null ? getTag().toUpperCase() : rec;
				}catch(Exception x){}
			}
		}
		nametemp.replace(" ", "");
		nametemp.replace("'", "");
		nametemp.replace("\"", "");
		if(nametemp.length()==0)nametemp = "CompName";
		nametemp = nametemp.toUpperCase();
		int nindex = 2;
		rec = nametemp;
		while(compNames.contains(rec)){
			rec = nametemp+String.valueOf(nindex++);
		}
		compNames.add(rec);
		return rec;
	}
	
	private static Vector<String> compNames = new Vector();
	/**
	 * reset the cache of compnames already generated to insure we start fresh with any indices.
	 */
	public static void resetCompNamesCache(){
		compNames.clear();
	}
	
	
}
