/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib;

import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.safs.GuiObjectRecognition;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.StringUtils;
import org.safs.selenium.util.JavaScriptFunctions.SAP;
import org.safs.selenium.webdriver.SeleniumPlus;
import org.safs.selenium.webdriver.SeleniumPlus.WDTimeOut;
import org.safs.selenium.webdriver.lib.model.AbstractTreeSelectable;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.ITreeSelectable;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;
import org.safs.selenium.webdriver.lib.model.TreeNode;
import org.safs.tools.stringutils.StringUtilities;

/**
 * 
 * History:<br>
 * 
 *  <br>   JAN 20, 2014    (DHARMESH4) Initial release.
 *  <br>   JUN 10, 2014    (Lei Wang) Implement keywords.
 */
public class Tree extends Component implements ITreeSelectable{
	ITreeSelectable treeListable = null;
	/**'data-expanded' attribute for tree http://demos.telerik.com/kendo-ui/treeview/index*/
	public static final String ATTRIBUTE_DATA_EXPANDED			= "data-expanded";

	public Tree(WebElement treeview) throws SeleniumPlusException{
		initialize(treeview);
	}

	protected void updateFields(){
		super.updateFields();
		treeListable = (ITreeSelectable) anOperableObject;
	}
	
	protected ITreeSelectable createOperable(WebElement treeview){
		String debugmsg = StringUtils.debugmsg(false);
		ITreeSelectable operable = null;
		try{
			if(WDLibrary.isDojoDomain(treeview)){
//				return new DojoSelectable_MultiSelect(this);
			}else if(WDLibrary.isSAPDomain(treeview)){
				operable = new SapSelectable_Tree(this);
			}else{
				operable = new DefaultSelectable_Tree(this);
			}
		}catch(Exception e){ IndependantLog.debug(debugmsg+" Met Exception ", e); }
		
		if(operable==null) IndependantLog.error("Can not create a proper Selectable object.");

		return operable;
	}
	
	public static boolean isTreeNodeExpanded(WebElement treenode){
		return SearchObject.getBoolean(treenode, ATTRIBUTE_ARIA_EXPANDED, ATTRIBUTE_DATA_EXPANDED);
	}
	
	public static boolean isTreeNodeSelected(WebElement treenode){
		return SearchObject.getBoolean(treenode, ATTRIBUTE_ARIA_SELECTED);
	}
	
	/**
	 * For treenode, WebElement.getText() may return also the text of its children.<br>
	 * These text are separated by '\n', this method tries to get the text before the first '\n', which<br>
	 * should be text of this tree node.
	 * 
	 * @param treenode WebElement, represents the tree node.
	 * @return String, the label of the treenode, or null if the treenode is null
	 */
	public static String parseTreeNodeText(WebElement treenode){
		if(treenode==null) return null;
		String text = TreeNode.parseWebElementText(treenode);
		return StringUtils.getFirstLine(text);
	}
	
	class DefaultSelectable_Tree extends AbstractTreeSelectable{
		/**
		 * @param parent
		 * @throws SeleniumPlusException
		 */
		public DefaultSelectable_Tree(Component parent) throws SeleniumPlusException {
			super(parent);
		}
		//Just return true for now, as getSupportedClassNames() cannot return an approperiate array.
		public boolean isSupported(WebElement element){
			return true;
		}
		public String[] getSupportedClassNames() {
			//TODO What kind of HTML-TAG/CSS-CLASS can be considered as a Tree??? Just let isSupported() return true for now.
			return null;
		}
		
		public TreeNode[] getCacheableContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			boolean setTimeoutLocally = false;

			if(!Tree.this.isAccessible()){
				throw new SeleniumPlusException("Cannot get content from a tree, whihc is not Web Accessible!");
			}
			
			try{
				//If the tree's implementation follows the "Web Accessibilty Internet", we can use
				//role="tree", role="treeitem" to get the items, but we need to know the hierarchy!!!
				String searchCriteria = RS.XPATH.fromAttribute(ATTRIBUTE_WAI_ROLE, WAI_ROLE_TREEITEM, false, true);
				List<WebElement> childrenElement = webelement.findElements(By.xpath(searchCriteria));

				TreeNode node = null;
				TreeNode possibleParentNode = null;
				TreeNode tempNode = null;
				WebElement we = null;
				WebElement previousWe = null;
				List<WebElement> descendants = null;
				//childrenElement contains all the items in a tree, but it is flat, we need to turn it into hierarchical tree
				//Following algorithm has a pre-condition: selenium traverse the tree by 'depth-first-first' algorithm during searching
				
				//Create a map contain the pair (WebElement, TreeNode) Tree Node is created from WebElement
				//We are going to find the parent-child relationship between these TreeNode
				Map<Object, TreeNode> map = new HashMap<Object, TreeNode>(childrenElement.size());
				for(int i=childrenElement.size()-1;i>=0;i--){
					we = childrenElement.get(i);
					map.put(we, new TreeNode(we));
				}
				
				//we need to clear the implicit-wait timeout, otherwise findElements() will spend lot of time if there is no mathcing items.
				//If other thread is using this timeout, we will be blocked here until the other thread finish.
				if(!WDTimeOut.setImplicitlyWait(0, TimeUnit.SECONDS)){
					IndependantLog.warn(debugmsg+" Fail to thread-safely set implicit-wait timeout to 0. Going to clear it non-thread-safely.");
					//WARNING: But if we set, this will affect the other thread!!!
					SeleniumPlus.WebDriver().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
					setTimeoutLocally = true;
				}

				for(int i=childrenElement.size()-1;i>=0;i--){
					we = childrenElement.get(i);
					node = map.get(we);
					//Try to find the direct parent of node
					findDirectParent:
						for(int j=i-1;j>=0;j--){
							previousWe = childrenElement.get(j);
							possibleParentNode = map.get(previousWe);
							descendants = previousWe.findElements(By.xpath(searchCriteria));
							//if descendants contains we, which means the parent is found, then break; otherwise continue
							for(WebElement temp: descendants){
								//not sure if selenium.findElements() will return the same instances
								tempNode = map.get(temp);
								//if not, we cannot get the TreeNode from the Map and we have to create a new TreeNode to compare
								if(tempNode==null) tempNode = new TreeNode(temp);
								if(tempNode.equals(node)){
									possibleParentNode.addChild(node);
									node.setParent(possibleParentNode);
									IndependantLog.debug("'"+possibleParentNode.getLabel()+"' is direct parent of '"+node.getLabel()+"'");
									break findDirectParent;
								}
							}
						}
					//As node's children have been added from the last one to the first, we need to reverse the order.
					node.reverseChildren();
				}

				//if the node's parent is null, then they are top level node, put them in an array and return
				List<TreeNode> wholeTree = new ArrayList<TreeNode>();
				for(int i=0; i<childrenElement.size();i++){
					tempNode = map.get(childrenElement.get(i));
					if(tempNode.getParent()==null) wholeTree.add(tempNode);
				}

				return wholeTree.toArray(new TreeNode[0]);
			}catch(Exception e){
				IndependantLog.error(debugmsg+" Fail to get content from a tree.", e);
				throw new SeleniumPlusException("Fail to get content from a tree. Met "+StringUtils.debugmsg(e));
			}finally{
				long originalTimeout = Processor.getSecsWaitForWindow();
				if(!WDTimeOut.resetImplicitlyWait(originalTimeout, TimeUnit.SECONDS) && setTimeoutLocally){
					//we are going to reset the implicit-wait time non-thread-safely.
					SeleniumPlus.WebDriver().manage().timeouts().implicitlyWait(Processor.getSecsWaitForWindow(), TimeUnit.SECONDS);
				}
			}
		}
		
		/**
		 * This method override that of superclass. It will only try to find each node in the tree according to<br>
		 * the treepath provided as parameter, and then return a simple-chain of TreeNode which contains only<br>
		 * the nodes specified in the treepath. Not like the result return in superclass, which is a double-direction<br>
		 * chain and the whole tree can be accessed throught it.<br>
		 * 
		 * <b>Note:</b>The parameter matchIndex is not used yet here. Need to be considered. TODO<br>
		 */
		public TreeNode getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			TreeNode matchedNode = null;
			
			try{
				matchedNode = super.getMatchedElement(criterion);
				if(matchedNode!=null) return matchedNode;
			}catch(Exception e){
				IndependantLog.debug(debugmsg+"Fail to find tree node ", e);
			}
			
			String treepath = criterion.getText();
			boolean partialMatch = criterion.isPartialMatch();
			
			String[] pathNodes = StringUtils.getTokenArray(treepath, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR, null);
			WebElement parentElement = webelement;
			List<WebElement> childrenElement = null;
			WebElement childElement = null;
			TreeNode parentNode = null;
			String searchCriteria = null;

			boolean isAccessible = Tree.this.isAccessible();
			String tempText = null;
			int level = 0;

			for(String nodeText: pathNodes){
				if(isAccessible){
					//Treate as a Tree, which follows the rules of 'Web Accessible Internet'
					//Find the all nodes containing attribute role='treeitem', the result will include
					//direct children, grand-children, grand-grand-children ..., is there a way to 
					//get only the direct children???
//					searchCriteria = "xpath=.//*[role='treeitem']";
					searchCriteria = RS.XPATH.fromAttribute(ATTRIBUTE_WAI_ROLE, WAI_ROLE_TREEITEM, false, true);
					childrenElement = parentElement.findElements(By.xpath(searchCriteria));
					for(int i=0;i<childrenElement.size();i++){
						childElement = childrenElement.get(i);
						tempText = parseTreeNodeText(childElement);
//						if(criterion.matchText(tempText)){
						if(criterion.matchText(tempText, level)){
							parentElement = childElement;
							break;
						}
					}
				}else{
					//Treate as a normal tree.
					searchCriteria = RS.text(nodeText, partialMatch, true);
					//'Using parentElement' risks find NO treenode, as parentElement is just a SPAN TAG containing only 'Text'
					//if we create TreeNode from it, the TreeNode will not contain correct value for 'expanded' 'selected', then showOnPage(), expandItem() will not work.
					parentElement = SearchObject.getObject(webelement, searchCriteria);
				}
				level++;
				
				if(parentElement==null){
					IndependantLog.error(debugmsg+"node '"+nodeText + "' not found. " + searchCriteria);
					matchedNode = null;
					break;
				}else{
					matchedNode = new TreeNode(parentElement);
					matchedNode.setParent(parentNode);
					parentNode = matchedNode;
				}
			}
			
			if(matchedNode==null){
				IndependantLog.error(debugmsg+"Fail to find tree node matching '"+criterion.toString()+"'");
				throw new SeleniumPlusException("Fail to find tree node '"+treepath+"'.");
			}
			return matchedNode;
		}
		
		protected void showOnPage(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			WDLibrary.checkNotNull(element);
			
			TreeNode node = convertTo(element);
			
			try {
				expandItem(node, true);

				//TODO Show the node, if the node is out of the scroll area.
				
			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to show node '"+node.getLabel()+"'. due to '"+e.getMessage()+"'");
			}
		}

		/**
		 * Expand the nodes (by double-click) from the root level by level.<br>
		 * <b>Note:</b>The second parameter expandChildren is not used yet here.<br>
		 */
		protected void expandItem(TreeNode node, boolean expandChildren) throws SeleniumPlusException {
			Stack<TreeNode> stack = new Stack<TreeNode>();
			TreeNode parent = node;
			while(parent!=null){
				stack.push(parent);
				parent = parent.getParent();
			}

			TreeNode item = null;
			while(!stack.isEmpty()){
				item = stack.pop();
				item.refresh(true);
				if(item!=null && !item.isExpanded()){
					Actions action = new Actions(SearchObject.getWebDriver());
					Action doubleClick = action.doubleClick(item.getClickableWebElement()).build();
					doubleClick.perform();
					StringUtilities.sleep(500);//slowdown,correct way?
					//TODO if 'double-click' cannot expand the tree node
//					item.refresh(false);
				}
			}
		}

		/**
		 * Collapse the nodes (by double-click).<br>
		 * <b>Note:</b>The second parameter collpaseChildren is not used here.<br>
		 */
		protected void collapseItem(TreeNode node, boolean collpaseChildren) throws SeleniumPlusException {
			if(node.isExpanded()){
				Actions action = new Actions(SearchObject.getWebDriver());
				Action doubleClick = action.doubleClick(node.getClickableWebElement()).build();
				doubleClick.perform();
				StringUtilities.sleep(500);//slowdown,correct way?
				//TODO if 'double-click' cannot collapse the tree node
			}
		}
		
		protected boolean verifyItemExpanded(TreeNode node) throws SeleniumPlusException {
			//As default tree doesn't have an ID, during refresh 'css class' will be used to
			//find the WebElement, but it very probably find a WRONG one.
			//Temprary fix: pass 'true' to refresh and hope it will not refresh the WebElement
//			node.refresh(false);
			node.refresh(true);
			return super.verifyItemExpanded(node);
		}
		
		protected void verifyItemSelected(Element element) throws SeleniumPlusException {		
			//As default tree doesn't have an ID, during refresh 'css class' will be used to
			//find the WebElement, but it very probably find a WRONG one.
			//Temprary fix: pass 'true' to refresh and hope it will not refresh the WebElement
//			element.refresh(false);
			element.refresh(true);
			super.verifyItemSelected(element);
		}
	}
	
	class SapSelectable_Tree extends AbstractTreeSelectable{
		public static final String CLASS_NAME_TREE = "sap.ui.commons.Tree";

		public SapSelectable_Tree(Component component) throws SeleniumPlusException {
			super(component);
		}

		public String[] getSupportedClassNames() {
			String[] clazzes = {CLASS_NAME_TREE};
			return clazzes;
		}

		public TreeNode[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			TreeNode root = null;

			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_Tree_getNodes(true));
				jsScript.append(" return sap_ui_commons_Tree_getNodes(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				
				if(result instanceof AbstractMap){
					root = new TreeNode(result);
					return root.getChildren();
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return null;
		}
		
		protected void expandItem(TreeNode node, boolean expandChildren) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_TreeNode_expand(true));
				jsScript.append(" return sap_ui_commons_TreeNode_expand(arguments[0], arguments[1]);");
				
				//We need to expand from the top node level by level.
				List<String> nodeIds = new ArrayList<String>();//a list of nodeID, from this node to top node.
				nodeIds.add(node.getId());
				TreeNode parent = node.getParent();
				while(parent!=null){//if parent is null, which means the node is a Tree not a TreeNode
					if(parent.getId()==null) throw new SeleniumPlusException("TreeNode's id is null, cannot expand it.");
					nodeIds.add(parent.getId());
					parent = parent.getParent();
				}
				
				for(int i=nodeIds.size()-1;i>0;i--){
					//expand the ancestor nodes, do NOT expand their children
					WDLibrary.executeScript(jsScript.toString(), nodeIds.get(i), false);
				}
				//expand this node
				WDLibrary.executeScript(jsScript.toString(), nodeIds.get(0), expandChildren);
				
			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to expand node '"+node.getLabel()+"'. due to '"+e.getMessage()+"'");
			}	
		}

		protected void collapseItem(TreeNode node, boolean collpaseChildren) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			
			String nodeId = node.getId();
			
			if(nodeId==null) throw new SeleniumPlusException("TreeNode's id is null, cannot collapse it.");
			
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_TreeNode_collapse(true));
				jsScript.append(" return sap_ui_commons_TreeNode_collapse(arguments[0], arguments[1]);");
				WDLibrary.executeScript(jsScript.toString(), nodeId, collpaseChildren);
				
			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to collapse node '"+node.getLabel()+"'. due to '"+e.getMessage()+"'");
			}
		}

		protected boolean verifyItemExpanded(TreeNode node) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			WDLibrary.checkNotNull(node);
			String nodeId = node.getId();
			
			if(nodeId==null) throw new SeleniumPlusException("TreeNode's id is null, cannot verify its status.");
			
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_TreeNode_getExpanded(true));
				jsScript.append(" return sap_ui_commons_TreeNode_getExpanded(arguments[0]);");
				Object result = WDLibrary.executeScript(jsScript.toString(), nodeId);

				IndependantLog.debug(debugmsg+"node "+node.getFullPath()+" expanded="+result);
				node.setExpanded(StringUtilities.convertBool(result));
				
				if(!node.isExpanded()){
					WebElement treeNode = SearchObject.getObject(webelement(), "id=" + nodeId);
					node.setExpanded(Tree.isTreeNodeExpanded(treeNode));
				}
				
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return super.verifyItemExpanded(node);
		}

		/**
		 * As we call SAP API TreeNode.select() to show a node on page, which will set the 'selected'
		 * property to true. If we click that node again, this property 'selected' will be set to false,
		 * which will cause the verification fail. So we just test the value in parameter element, which
		 * is set before clicking the node.
		 * 
		 * @see #selectItem(String, boolean, int, boolean, Keys, Point, int)
		 * @see #clickElement(Element, Keys, Point, int, int)
		 * @see #showOnPage(Element)
		 */
		protected void verifyItemSelected(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			WDLibrary.checkNotNull(element);
			//TODO if the element's property 'selected' is true, we will not try to get the value from the
			//real SAP TreeNode. The real SAP TreeNode very probably has a 'false' value for that property!!!
			//"TreeNode.select() then click on node" cause this, we may need to modify method showOnPage()-> 
			//don't call TreeNode.select() to show the node on page.
			if(element.isSelected()) return;

			TreeNode node = convertTo(element);
			String nodeId = node.getId();
			
			if(nodeId==null) throw new SeleniumPlusException("TreeNode's id is null, cannot verify its status.");
			
			try {
//				StringBuffer jsScript = new StringBuffer();
//				jsScript.append(SAP.sap_ui_commons_TreeNode_refresh(true));
//				jsScript.append(" return sap_ui_commons_TreeNode_refresh(arguments[0]);");
//				Object result = WDLibrary.executeScript(jsScript.toString(), nodeId);
//				
//				if(result instanceof AbstractMap){
//					node = new TreeNode(result);
//				}else if(result!=null){
//					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
//					IndependantLog.warn("javascript result:\n"+result);
//				}
				
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_TreeNode_getIsSelected(true));
				jsScript.append(" return sap_ui_commons_TreeNode_getIsSelected(arguments[0]);");
				Object result = WDLibrary.executeScript(jsScript.toString(), nodeId);

				IndependantLog.debug(debugmsg+"node "+node.getFullPath()+" selected="+result);
				node.setSelected(StringUtilities.convertBool(result));
				
				//This may happen if we call TreeNode.select(), then click on treenode.
				//Try to get the attribute 'aria-selected'
				if(!node.isSelected()){
					WebElement treeNode = SearchObject.getObject(webelement(), "id=" + nodeId);
					node.setSelected(Tree.isTreeNodeSelected(treeNode));
				}
				
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			super.verifyItemSelected(node);
		}
		
		/**
		 * In this implementation, we call node.select() to show the node on page.
		 * This API will also select the node of the tree, the 'selected' property will be
		 * set to true; but if we click the node again by mouse, this property 'selected'
		 * will be set to false!!! So we set the value of 'selected' to the element parameter,
		 * and in method {@link #verifyItemSelected(Element)}, if the element isSelected() then
		 * we will not try to get the value for the real SAP TreeNode object.
		 * 
		 * @see #selectItem(String, boolean, int, boolean, Keys, Point, int)
		 * @see #clickElement(Element, Keys, Point, int, int)
		 * @see #verifyItemSelected(Element)
		 */
		protected void showOnPage(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			WDLibrary.checkNotNull(element);
			
			TreeNode node = convertTo(element);
			String treeId = node.getRootId();
			String nodeId = node.getId();
			
			if(treeId==null) throw new SeleniumPlusException("Tree's id is null, cannot show node on page.");
			if(nodeId==null) throw new SeleniumPlusException("TreeNode's id is null, cannot show node on page.");
			
			try {
				//Expand all nodes
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_Tree_expandAll(true));
				jsScript.append(" return sap_ui_commons_Tree_expandAll(arguments[0]);");
				WDLibrary.executeScript(jsScript.toString(), treeId);
				StringUtilities.sleep(500);

				//Show the node
				jsScript.delete(0, jsScript.length());
				jsScript.append(SAP.sap_ui_commons_TreeNode_showOnPage(true));
				jsScript.append(" return sap_ui_commons_TreeNode_showOnPage(arguments[0]);");
				WDLibrary.executeScript(jsScript.toString(), nodeId);
				StringUtilities.sleep(500);
				
				//Get the value of property 'selected' 
				jsScript.delete(0, jsScript.length());
				jsScript.append(SAP.sap_ui_commons_TreeNode_getIsSelected(true));
				jsScript.append(" return sap_ui_commons_TreeNode_getIsSelected(arguments[0]);");
				Object selected = WDLibrary.executeScript(jsScript.toString(), nodeId);
				
				IndependantLog.debug(debugmsg+"node "+node.getFullPath()+" selected="+selected);
				element.setSelected((Boolean)selected);
				
			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to show node '"+node.getLabel()+"'. due to '"+e.getMessage()+"'");
			}
		}

	}

	public void selectItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		
		try{
			treeListable.selectItem(criterion, verify, key, offset, mouseButtonNumber);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "selectItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void selectItem(int index, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	public void activateItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		
		try{
			treeListable.activateItem(criterion, verify, key, offset);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "activateItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void activateItem(int index, boolean verify, Keys key, Point offset) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	public void verifyItemSelection(TextMatchingCriterion criterion, boolean expectSelected) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "verifyItemSelection");
		
		try{
			treeListable.verifyItemSelection(criterion, expectSelected);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifyItemSelection", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void verifyItemSelection(int index, boolean expectSelected) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	public void verifyContains(TextMatchingCriterion criterion) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		
		try{
			treeListable.verifyContains(criterion);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifyContains", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public TreeNode[] getContent() throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		
		try{
			return treeListable.getContent();
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "getContent", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void expandItem(TextMatchingCriterion criterion, boolean expandChildren, boolean verify) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		
		try{
			treeListable.expandItem(criterion, expandChildren, verify);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "expandItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void collapseItem(TextMatchingCriterion criterion, boolean collpaseChildren, boolean verify) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		
		try{
			treeListable.collapseItem(criterion, collpaseChildren, verify);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "collapseItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public TreeNode getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		
		try{
			return treeListable.getMatchedElement(criterion);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedElement", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	//======================================= Some deprecated methods ===================================================//
	/**
	 * @param fullnode
	 * @throws SeleniumPlusException
	 * @deprecated due to deprecation of {@link #node(String, boolean)}
	 */
	public void SelectTextNode(String fullnode) throws SeleniumPlusException{
		node(fullnode,true);
	}
	
	/**
	 * @param fullnode
	 * @throws SeleniumPlusException
	 * @deprecated due to deprecation of {@link #node(String, boolean)}
	 */
	public void ExpandTextNode(String fullnode) throws SeleniumPlusException{
		node(fullnode,false);
	}
	
	/**
	 * @param fullnode
	 * @param isSelect
	 * @throws SeleniumPlusException
	 * @deprecated merged to the implementation of AbstractTreeSelectable, see DefaultSelectable_Tree
	 */
	private void node(String fullnode, boolean isSelect) throws SeleniumPlusException{

		StringTokenizer st = new StringTokenizer(fullnode, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR);		
		String node = null;		
		while (st.hasMoreElements()) {
			node = (String) st.nextElement();			
			WebElement item = null; 			
			try{
				item = SearchObject.getObject(webelement, SearchObject.SEARCH_CRITERIA_TEXT+SearchObject.assignSeparator + node);
			
				if (item == null){
					throw new SeleniumPlusException(node + " node not found" + "(text=" + node +")");					
				}
				
				if (isSelect){
					if (st.countTokens() == 0){
						item.click();
						break;
					}
				}
				if (item.getAttribute(ATTRIBUTE_ARIA_EXPANDED).equals("false")){
					Actions action = new Actions(SearchObject.getWebDriver());
					Action doubleClick = action.doubleClick(item).build();
					doubleClick.perform();					
					try {
						Thread.sleep(500); //slowdown,correct way?
					} catch (InterruptedException e) {
						// never happen ?
					}					
				}				
			} catch (SeleniumPlusException spe){
				throw spe;
			} catch (Exception msee) {
				throw new SeleniumPlusException("Click action failed on node " + node);				
			}			
		}		
	}
}
