package org.safs.selenium.spc;

//import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
//import java.awt.Point;
//import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.WDLibrary;

/**
 * JAN 28, 2015     (CANAGL) Adding support for Frames.
 */ 
public class SPCTreePanel extends JPanel{
 
	protected SPCTreeNode rootNode;
	protected DefaultTreeModel treeModel;
	protected JTree tree;
	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	public ArrayList allNodes;
	public SPCTreePanel() {
		super(new GridLayout(1,0));
		allNodes = new ArrayList();
		rootNode = new SPCTreeNode(new String[]{"/Root","0","0","0","0"});
		treeModel = new DefaultTreeModel(rootNode);
		treeModel.addTreeModelListener(new SPCTreeModelListener());

		tree = new JTree(treeModel);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane);
	}

	/**
	 * 
	 * @param pframe the frame node this data belongs to.  Can be null.
	 * @param sdata
	 * @param boundsSep
	 * @author CANAGL JAN 27, 2015 Added Frame support
	 */
	public void setData(SPCTreeNode pframe, String [] sdata, String boundsSep){
		if(sdata == null || sdata[0].equalsIgnoreCase("INTERRUPTED")|| sdata[0].equalsIgnoreCase("true")){
			return;
		}
		String [] subtemp;
		clear();
		for(int i = 0; i < sdata.length; i++){
			subtemp = sdata[i].split(boundsSep);
			Log.debug(subtemp[0]);
			SPCTreeNode anode = addObject(subtemp, pframe);
			anode.frame = pframe;
		}
	}

	/**
	 * Add a single SPCTreeNode to the existing Tree Panel.
	 * All available info is extracted from the WebElement and stored in the SPCTreeNode object.
	 * @param item
	 * @param xpath
	 * @return
	 */
	public SPCTreeNode addSPCTreeNode(WebElement item, String xpath, SPCTreeNode parentTreeNode){
		if(item==null || xpath ==null || xpath.length()==0) return null;
		WebElement temp = item;
		Point p = null;
		Dimension d = null;		
		p = temp.getLocation();
		d = temp.getSize();
		ArrayList list = new ArrayList();
		list.add(xpath);
		list.add(p);
		list.add(d);
		list.add(parentTreeNode);
		
		SPCTreeNode node = addObject(list);
		if(node == null){
			IndependantLog.info("TreePanel.addSPCTreeNode did not addObject. Parent Node may not exist for XPATH: "+ xpath);

			String parentPath = StringUtils.getParentXpath(xpath);
			IndependantLog.info("TreePanel.addSPCTreeNode seeking parentNode for "+ parentPath);
			WebElement parent = SearchObject.getObject("XPATH="+parentPath);
			//once we have a good parent we should have the whole path
			if( parent!=null && addSPCTreeNode(parent, parentPath, parentTreeNode) != null){
				IndependantLog.info("TreePanel.addSPCTreeNode thru top parent should now be in place.");
				node = addObject(list);
			}
		}
		if(node == null){
			IndependantLog.info("TreePanel.addSPCTreeNode could not build parent path of nodes for XPATH: "+ xpath);
			return null;
		}		
		try{ node.setText(temp.getText());}catch(Exception n){}
		try{ node.setTitle(temp.getAttribute("title"));}catch(Exception n){}
		try{ node.setAttrClass(temp.getAttribute("class"));}catch(Exception n){}
		try{ node.setId(temp.getAttribute("id"));}catch(Exception n){}
		try{ node.setName(temp.getAttribute("name"));}catch(Exception n){}
		try{
			String tag = temp.getTagName();
			node.setTag(tag);
			if(tag != null && (tag.equalsIgnoreCase(SearchObject.TAG_FRAME) ||
					           tag.equalsIgnoreCase(SearchObject.TAG_IFRAME))){
				node.setRecognitionString(SearchObject.generateSAFSFrameRecognition(node.xpart));
			}
		}catch(Exception n){}
		try{ node.setSubType(temp.getAttribute("type"));}catch(Exception n){}
		try{
			if(SearchObject.isDojoDomain(temp)){
				node.setDomain(SearchObject.DOMAIN_DOJO);
				node.setDomainClass(SearchObject.DOJO.getDojoClassName(temp));
				node.setDomainRecognition(SearchObject.DOJO.getRecognition(temp));
			}else if(SearchObject.isSAPDomain(temp)){
				node.setDomain(SearchObject.DOMAIN_SAP);
				node.setDomainClass(SearchObject.SAP.getSAPClassName(temp));
				node.setDomainRecognition(SearchObject.SAP.getRecognition(temp));
			}else{
				node.setDomain(SearchObject.DOMAIN_HTML);
				node.setDomainClass(SearchObject.HTML.getHTMLClassName(temp));
				node.setDomainRecognition(SearchObject.HTML.getRecognition(temp));
			}
			node.setCompType(WebDriverGUIUtilities.getClassTypeMapping(node.getDomainClass()));
		}catch(Exception x){
			Log.debug("TreePanel.addSPCTreeNode "+ x.getClass().getSimpleName()+", "+ x.getMessage());
		}
		return node;
	}
	
	/**
	 * Clear the Tree Panel of all nodes and start a new tree with all elements provided.
	 * @param sdata
	 * @param xpaths
	 * @see #addSPCTreeNode(WebElement, String)
	 */
	public void setData(SPCTreeNode pframe, List<WebElement> sdata, List<String> xpaths){
		if(sdata == null || sdata.isEmpty()) return;		
		clear();
		WebElement temp = null;
		String xpath = null;
		for(int i = 0; i < sdata.size(); i++){
			temp = sdata.get(i);
			xpath = "";
			try{ xpath = xpaths.get(i);}catch(Exception ignore){}
			SPCTreeNode node = addSPCTreeNode(temp, xpath, pframe);
			node.frame = pframe;
		}
	}

	public Rectangle getNodeDimensions(SPCTreeNode anode){
		Rectangle compBounds = new Rectangle(anode.bounds);
		if(anode.frame != null){
			String frameRS = anode.frame.getRecognitionString() == null ? 
			        		 anode.frame.xpart : 
			        	     anode.frame.getRecognitionString();
			Log.info("TreePanel.getNodeDimensions relative to FRAME node RS: "+ 
		        frameRS +" @ "+
		        anode.frame.bounds.toString());
			compBounds.x += anode.frame.bounds.x;
			compBounds.y += anode.frame.bounds.y;
		}
		Log.info("TreePanel.getNodeDimensions for "+ anode.xpath +" returns "+ compBounds.toString());
		return compBounds;
	}
	
	public Rectangle getSelectedComponentDimensions() {
		SPCTreeNode node = getSelectedComponent();
		return getNodeDimensions(node);
	}
	
	public SPCTreeNode getSelectedComponent() {
		try{ return ((SPCTreeNode)tree.getSelectionPath().getLastPathComponent());}
		catch(NullPointerException np){
			Log.debug("SPCTreePanel.getSelectedComponent ignoring NullPointerException.");
		}
		return null;
	}

	public void setSelectedComponentByPoint(Point pt) {
		for(int i = allNodes.size()-1; i >=0; i--){
			if(getNodeDimensions((SPCTreeNode)allNodes.get(i)).contains(new java.awt.Point(pt.x, pt.y))){
				tree.setSelectionPath(new TreePath(((SPCTreeNode)(allNodes.get(i))).getPath()));
				tree.scrollRowToVisible(i+1);
				return;
			}
		}
	}
	
	/**
	 * Attempt to locate a node in the existing Tree by its full stored XPath.
	 * @param xpath
	 * @return
	 */
	public SPCTreeNode setSelectedComponentByXpath(String xpath){
		SPCTreeNode node = null;
		try{
			if(SearchObject.isValidFrameRS(xpath)){
				node = getFrameNode(xpath);
			}else{
				node = getNode(xpath, null);
			}
			tree.setSelectionPath(new TreePath(node.getPath()));
		}
		catch(NullPointerException np){
			Log.debug("TreeNode.setSelectedComponentByXpath may have no matching node.", np);
		}
		return node;
	}
	
	/** Remove all nodes except the root node. */
	public void clear() {
		rootNode.removeAllChildren();
		treeModel.reload();
		allNodes.clear();
	}

	/**
	 * @param object Expects a String[5] containing:
	 * <p><pre>
	 * object[0] = xpath
	 * object[1] = x
	 * object[2] = y
	 * object[3] = w
	 * object[4] = h
	 * </pre>
	 * @param parentFrame if one exists
	 * @return the newly created SPCTreeNode that has been added to the Tree.
	 */
	public SPCTreeNode addObject(String [] object, SPCTreeNode parentFrame) {
		SPCTreeNode node = new SPCTreeNode(object);
		node.frame = parentFrame; // might be null
		return addObject(node);
	}
	
	/**
	 * @param object Expects a List containing:
	 * <p><pre>
	 * object[0] = xpath
	 * object[1] = Point
	 * object[2] = Dimension
	 * object[3] = frame node, if any
	 * </pre>
     * @author JAN 28, 2015     (CANAGL) Adding support for Frames.
	 */
	public SPCTreeNode addObject(List object) {
		String[] info = new String[5];
		SPCTreeNode node = null;
		SPCTreeNode pnode = null;
		try{
			info[0] = object.get(0).toString();
			Point p = (Point)object.get(1);
			info[1] = String.valueOf(p.x);
			info[2] = String.valueOf(p.y);
			Dimension d = (Dimension) object.get(2);
			info[3] = String.valueOf(d.width);
			info[4] = String.valueOf(d.height);
			if(object.size()>3){
				pnode = (SPCTreeNode) object.get(3);
			}
			node = addObject(info, pnode);
		}catch(Exception x){
			Log.debug("TreeNode.addObject(List) ", x);
		}
		return node;
	}
	
	/**
	 * Add a node into the tree and treeModel.
	 * @param childNode
	 * @return the same childNode
	 */
	public SPCTreeNode addObject(SPCTreeNode childNode) {
		SPCTreeNode parentNode = getParentNode(childNode.getXpath(), childNode.frame);
		try{
			Log.debug("TreePanel.addObject(SPCTreeNode) using parentNode: "+parentNode.toString());
			treeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
		} catch (NullPointerException e){
			Log.debug("TreePanel.addObject(childNode) ignoring Exception: ", e);
			return null;
		}
		allNodes.add(childNode);
		tree.expandPath(new TreePath(parentNode.getPath()));
		return childNode;
	}
	
	/**
	 * Try to find the node in the tree with the matching xpath
	 * @param xpath
	 * @return
	 */
	public SPCTreeNode getNode(String xpath, SPCTreeNode theRoot){
		if(rootNode == null && theRoot == null) {
			Log.debug("TreePanel.getNode root node == null.  Cannot attempt to find "+xpath);
			return null;
		}
		if(xpath == null){
			Log.debug("TreePanel.getNode xpath == null.  Cannot attempt to find node with null xpath.");
			return null;
		}
		xpath = xpath.trim();
		if(xpath.startsWith("//")){
			xpath = xpath.substring(2);
		}else if(xpath.startsWith("/")){
			xpath = xpath.substring(1);
		}
		
//		String [] nodes = xpath.split("/");
		String [] nodes = StringUtils.breakXpath(xpath, false, true);
		SPCTreeNode cur = theRoot == null ? rootNode : theRoot;
		for(int i = 0; i < nodes.length && cur != null; i++){
			cur = cur.findChild(nodes[i]);
		}
		return cur;
	}
	
	/**
	 * Try to find the Frame node in the tree with the matching frameRS
	 * @param frameRS
	 * @return
	 */
	public SPCTreeNode getFrameNode(String frameRS){
		if(rootNode == null) {
			Log.debug("TreePanel.getFrameNode rootNode == null.  Cannot attempt to find "+frameRS);
			return null;
		}
		if(frameRS == null){
			Log.debug("TreePanel.getFrameNode frameRS == null.  Cannot attempt to find node with null frameRS.");
			return null;
		}
		frameRS = frameRS.trim();
		return rootNode.findFrame(frameRS);
	}
	
	public SPCTreeNode getParentNode(String xpath, SPCTreeNode topNode){
		xpath = xpath.trim();
		if(xpath.startsWith("//")){
			xpath = xpath.substring(2);
		}else if(xpath.startsWith("/")){
			xpath = xpath.substring(1);
		}
		
//		String [] nodes = xpath.split("/");
		String [] nodes = StringUtils.breakXpath(xpath, false, true);		
		SPCTreeNode cur = topNode == null ? rootNode : topNode;
		for(int i = 0; cur != null && i < nodes.length-1; i++){
			if(!nodes[i].equals(""))
				cur = cur.findChild(nodes[i]);
		}
		
		return cur;
	}

	class SPCTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(TreeModelEvent e) {
			SPCTreeNode node;
			node = (SPCTreeNode)(e.getTreePath().getLastPathComponent());

			/*
			 * If the event lists children, then the changed
			 * node is the child of the node we've already
			 * gotten.  Otherwise, the changed node and the
			 * specified node are the same.
			 */
			try {
				int index = e.getChildIndices()[0];
				node = (SPCTreeNode)(node.getChildAt(index));
			} catch (NullPointerException exc) {}

			Log.debug("The user has finished editing the node.");
			Log.debug("New value: " + node.getUserObject());
		}
		public void treeNodesInserted(TreeModelEvent e) {
		}
		public void treeNodesRemoved(TreeModelEvent e) {
		}
		public void treeStructureChanged(TreeModelEvent e) {
		}
	}

	public JTree getTree() {
		return tree;
	}


}
