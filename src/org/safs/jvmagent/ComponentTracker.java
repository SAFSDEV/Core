/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

import java.util.*;
import java.awt.Component;
import java.awt.Container;

/**
 * This class is used to process component hierarchies into IDs and ArrayLists 
 * that do not pin down or hold references to actual component objects.  Instead, 
 * we capture and store hierarchies of Integer hashCodes making component lookup 
 * fast, but not dangerous.
 * 
 * @author CANAGL
 */
public class ComponentTracker {
	
	private Hashtable components = new Hashtable();

	/**
	 * Constructor for ComponentTracker.
	 */
	public ComponentTracker() {
		super();
	}

	/**
	 * Add a ComponentItem to those being tracked.
	 */
	public void putComponentItem(ComponentItem compItem){
		try{ components.put(compItem.getCompID(), compItem);}
		catch(Exception x){;}
	}
	
	/**
	 * Get a ComponentItem based on the stored Integer hashcode key or 
	 * null if not found or invalid key.
	 */
	public ComponentItem getComponentItem(Integer key){
		try{ return (ComponentItem) components.get(key);}
		catch(Exception x){ return null;}
	}
	

	/**
	 * Retrieve an ArrayList of ComponentItems representing the parent's children.
	 * @param Component parent from which to get child ComponentItems.
	 * <p>
	 * @param ArrayList Integer hashCode hierarchy of the parent component.  This 
	 *         is the same as the CompParents ArrayList within a ComponentItem.
	 *         A TopLevel Window should have an ArrayList of just one Integer hashCode: itself.
	 * <p>
	 * @return ArrayList of ComponentItems representing the parent's children.
	 *          Zero-length ArrayList if the parent is not a Container or has no children.
	 */
	public ArrayList getComponentItems(LocalAgentFactory lagents, Component parent, ArrayList parentpath){

		if (!(parent instanceof Container)) {	return new ArrayList(0);}

		Container container = (Container) parent;
		Component[] children;
		try{ children = (Component[]) lagents.getDerivedAgent(container).getChildren(container);}
		catch(InvalidAgentException ia){ children = container.getComponents();}
		ArrayList childItems = new ArrayList(children.length);

		Integer childhash = null;
		Component childcomp = null;
		
		for(int ichild=0; ichild < children.length; ichild++){
			ArrayList childpath = new ArrayList(parentpath.size()+1);
			childpath.addAll(0, parentpath);
			childcomp = children[ichild];
			childhash = new Integer(childcomp.hashCode());
			childpath.add(childhash);
			childItems.add(ichild, new ComponentItem(childhash, childpath));
		}
		return childItems;
	}
	
	
	/**
	 * Retrieve the component in the array that has the matching hashcode.
	 * @param components array to search for hashcode.
	 * @param hashcode to find in component array.
	 * @return Component matching hashcode or null.
	 */
	protected Component getMatchedComponent(Component[] components, int hashcode){
		try{
			Component comp = null;
			int childhash = 0;
			for(int compindex = 0; compindex < components.length; compindex++){
				comp = components[compindex];
				childhash = comp.hashCode();
				if (hashcode == childhash) return comp;
			}
			return null;
		}
		catch(NullPointerException np){ 
			np.printStackTrace();
			return null;}
	}

	
	/**
	 * Attempt to locate the real Component from the parent array using the child 
	 * ComponentItem information.  This is done by identifying Integer hashCode matches 
	 * found in the child ComponentItem as we walk the parent trees.
	 * @param Component[] parent array (typically) of top level windows.
	 * <p>
	 * @param ComponentItem child reference holding ArrayList of Integer hashCodes 
	 *         identifying the hierarchy to the target Component.
	 * <p>
	 * @return Component matched or null if no match found.
	 */
	public Component getChildComponent(LocalAgentFactory lagents, Component[] parent, ComponentItem child){
		try{
			ArrayList childpath = child.getCompParents();
			int pathindex = 0;
			Component comp = null;
			Component[] children = parent;
			int lastchild = childpath.size()-1;
			
			//System.out.println ("parent childpath.size():"+childpath.size());
			//System.out.println ("childpath:"+childpath.toString());
			
			for(;pathindex < childpath.size(); pathindex++){
				comp = getMatchedComponent(children, ((Integer)childpath.get(pathindex)).intValue());				
				if(comp==null) {
					//System.out.println("Failed to find parent "+ pathindex +" in parent hierarchy");
					return null;
				}
				// if last in list then this is the one
				if(pathindex == lastchild) {
					//System.out.println("Found Child at PATHINDEX:"+pathindex +": "+comp.getClass().getName());
					return comp;				
				}
				// if not a Container it can't have children to process
				if(!(comp instanceof Container)) {
					//System.out.println("Found parent "+pathindex+" is not a valid container!");
					return null;				
				}
				try{
					Object[] tkids = lagents.getDerivedAgent(comp).getChildren(comp);
					children = new Component[tkids.length];
					for(int i=0;i<tkids.length;i++) children[i]=(Component)tkids[i];
				}
				catch(InvalidAgentException ia) { 
					//System.out.println("Failed to find correct Agent for parent "+pathindex+"in parent hierarchy");
					return null; 
				}
			}
			return null;
		}
		catch(NullPointerException np){ return null;}
	}	
}
