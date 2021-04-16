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
package org.safs.tools.data;

import java.awt.Point;
import java.util.List;

import org.safs.Log;
import org.safs.StringUtils;

public class DataUtilities {
	
	/** <br><em>Purpose:</em> Sarch through a 2D Array for a fully qualified node.  Return a PathNode object
	 *  that contains the information for the located cell (if found).
	 * @param                     2Dobj, the 2D array to search
	 * @param                     fullpath, the fully qualified path of the node to be located
	 * @param                     delim, the node delimiter
	 * @param                     partialmatch, whether to perform partial or exact match search for node
	 * @return                    node, null if node is not located, else a PathNode object containing the 
	 *                             information about the node
	 * @author Bob Lawler				-Added 09.02.2005 (RJL)
	 **/
	public static NodeInfo getObjectDataNodeInfo (String[][] a2Dobj, String fullpath, String delim, boolean partialmatch) {
		String searchnode = null;
		String matchpath = new String("");
		int searchrow = 0;
		int searchcol = 0;
		int searchpos = 0;
		int delimptr = fullpath.indexOf(delim);
		if (delimptr > 0)
			searchnode = fullpath.substring(searchpos, delimptr);	//up to delim
		else
			searchnode = fullpath.substring(searchpos);				//fullpath without delim
			
		while (searchrow < a2Dobj.length && searchpos < fullpath.length()) {
			//initialize search
			boolean match = false;
			if (a2Dobj[searchrow][searchcol] != null) {
				if (partialmatch)
					match = a2Dobj[searchrow][searchcol].toLowerCase().indexOf(searchnode.toLowerCase()) >= 0;
				else
					match = a2Dobj[searchrow][searchcol].equals(searchnode);
				//if (a2Dobj[searchrow][searchcol].equals(searchnode)) {
				if (match) {
					Log.debug(searchnode+" found at ("+searchrow+","+searchcol+")");
					
					if(delimptr > 0){
						//There are still some paths to match, we should check if this matched node has children.
						//If it does not have child, we should CONTINUE to find next matched node at this column;
						//Else if it does have children, we should check if one of its children can match the next path;
						//If none of its children can match, we should CONTINUE to find next matched node at this column.
						//Else if one of its children match, we should add this node to matchpath, and process next path.
						try{
							int tmprow = searchrow+1;
							int tmpcol = searchcol+1;
							String nodeValue = a2Dobj[tmprow][tmpcol];
							String nextparent = null;
							//If it does not have child, we should continue to find next matched node at this column;
							if(nodeValue==null){
								searchrow++;
								continue;
							}else{
								boolean childMatch = false;
								//get the next path to compare
								String childPath = "";
	
								if (delimptr > 0) {
									int tmpSearchpos = delimptr + delim.length();
									int tmpDelimptr = fullpath.indexOf(delim, tmpSearchpos);
									if (tmpDelimptr > 0)
										childPath = fullpath.substring(tmpSearchpos, tmpDelimptr);
									else
										childPath = fullpath.substring(tmpSearchpos);
								}
								
								//To check if one of the children can match the next path.
								do{
									//nodeValue maybe null
									if(nodeValue!=null){
										if (partialmatch)
											childMatch = nodeValue.toLowerCase().indexOf(childPath.toLowerCase()) >= 0;
										else
											childMatch = nodeValue.equals(childPath);
										
										if(childMatch)
											break;
									}
									//Get the next child
									nodeValue = a2Dobj[++tmprow][tmpcol];
									//We need to check if this nodeValue is the child of node that
									//we are checking, We check the previous column's value
									nextparent = a2Dobj[tmprow][tmpcol-1];
									//If it is null, nodeValue is child;
									//Otherwise, is not child, we need get out this loop and 
									//find another matched parent.
								}while(tmprow<a2Dobj.length && nextparent==null);
								
								//If none of its children can match, we should continue to find next matched node at this column.
								if(!childMatch){
									searchrow++;
									continue;
								}
							}
						}catch(IndexOutOfBoundsException e){
							Log.debug(" Can not find a matched path.");
							return null;
						}
					}
					
					matchpath = matchpath + a2Dobj[searchrow][searchcol];
					//searchnode found, are there more nodes in fullpath?
					if (delimptr > 0) {
						//get next searchnode in fullpath
						searchpos = delimptr + delim.length();
						delimptr = fullpath.indexOf(delim, searchpos);
						if (delimptr > 0)
							searchnode = fullpath.substring(searchpos, delimptr);	//...up to next delim
						else
							searchnode = fullpath.substring(searchpos);				//...remainder of fullpath
						
						if (searchnode.equals(""))
							searchpos = fullpath.length();	//no more nodes to be found, exit loop
						else {
							//increment to new cell search location
							matchpath = matchpath + delim;
							searchcol++;
							searchrow++;
						}
					}
					else 
						searchpos = fullpath.length();	//no more nodes to be found, exit loop
				}
				else
					searchrow++;	//searchnode not found, incr searchrow
			}
			else
				searchrow++;		//searchnode not found, incr searchrow
		}
		
		//return location of located cell
		NodeInfo node = new NodeInfo();
		if (searchrow == a2Dobj.length)
			return null;							//cell was not found in a2Dobj
		else {
			//cell was found, return node (w/ its located information)
			node.setPath(matchpath);
			node.setPoint(new Point(searchrow, searchcol));
			return node;
		}
	}

	
	/** <br><em>Purpose:</em> Sarch through a 2D Array for a fully qualified node.  Return a PathNode object
	 *  that contains the information for the located cell (if found).
	 * @param                     2Dobj, the 2D array to search
	 * @param                     fullpath, the fully qualified path of the node to be located
	 * @param                     delim, the node delimiter
	 * @param                     partialmatch, whether to perform partial or exact match search for node
	 * @param					  index,an int indicating the Nth duplicate item to match
	 * @return                    node, null if node is not located, else a PathNode object containing the 
	 *                             information about the node
	 * @author Lei Wang			  Overloaded method, add a new parameter	
	 **/
	public static NodeInfo getObjectDataNodeInfo (String[][] a2Dobj, String fullpath, String delim, boolean partialmatch,int index) {
		String debugmsg = DataUtilities.class.getName()+".getObjectDataNodeInfo(): ";
		String searchnode = null;
		String matchpath = new String("");
		int searchrow = 0;
		int searchcol = 0;
		int searchpos = 0;
		int level = StringUtils.getTokenList(fullpath, delim).size();
		
		int delimptr = fullpath.indexOf(delim);		
		if (delimptr > 0)
			searchnode = fullpath.substring(searchpos, delimptr);	//up to delim
		else
			searchnode = fullpath.substring(searchpos);				//fullpath without delim
		
		//Convert index from String to int
		int matchedTimes = 0;
		
		while (searchrow < a2Dobj.length && searchpos < fullpath.length()) {
			//initialize search
			boolean match = false;
			if (a2Dobj[searchrow][searchcol] != null) {
				if (partialmatch)
					match = a2Dobj[searchrow][searchcol].toLowerCase().indexOf(searchnode.toLowerCase()) >= 0;
				else
					match = a2Dobj[searchrow][searchcol].equals(searchnode);
				//if (a2Dobj[searchrow][searchcol].equals(searchnode)) {
				if (match) {
					Log.debug(searchnode+" found at ("+searchrow+","+searchcol+")");
					Log.info(debugmsg+" column: "+(searchcol+1)+" = level: "+level+" ? If equal, then it is last path.");
					//When match the last node in the path, should the check the matchedTimes
					if(searchcol+1==level){
						matchedTimes++;
						Log.debug(debugmsg+" matchedTimes="+matchedTimes+" $$ index="+index);
						if(matchedTimes<index){
							searchrow++;
							continue;
						}
					}else if(searchcol+1<level){
						//There are still some paths to match, we should check if this matched node has children.
						//If it does not have child, we should CONTINUE to find next matched node at this column;
						//Else if it does have children, we should check if one of its children can match the next path;
						//If none of its children can match, we should CONTINUE to find next matched node at this column.
						//Else if one of its children match, we should add this node to matchpath, and process next path.
						try{
							int tmprow = searchrow+1;
							int tmpcol = searchcol+1;
							String nodeValue = a2Dobj[tmprow][tmpcol];
							String nextparent = null;
							//If it does not have child, we should continue to find next matched node at this column;
							if(nodeValue==null){
								searchrow++;
								continue;
							}else{
								boolean childMatch = false;
								//get the next path to compare
								String childPath = "";
	
								if (delimptr > 0) {
									int tmpSearchpos = delimptr + delim.length();
									int tmpDelimptr = fullpath.indexOf(delim, tmpSearchpos);
									if (tmpDelimptr > 0)
										childPath = fullpath.substring(tmpSearchpos, tmpDelimptr);
									else
										childPath = fullpath.substring(tmpSearchpos);
								}
								
								//To check if one of the children can match the next path.
								do{
									//nodeValue maybe null
									if(nodeValue!=null){
										Log.info(debugmsg+" row: "+tmprow+" col: "+tmpcol);
										Log.info(debugmsg+" nodeValue: "+nodeValue+" = childPath: "+childPath+" ?");
										if (partialmatch)
											childMatch = nodeValue.toLowerCase().indexOf(childPath.toLowerCase()) >= 0;
										else
											childMatch = nodeValue.equals(childPath);
										
										if(childMatch){
											Log.info(debugmsg+"Find child '"+childPath+"' under path "+matchpath);
											break;
										}
									}
									//Get the next child
									nodeValue = a2Dobj[++tmprow][tmpcol];
									//We need to check if this nodeValue is the child of node that
									//we are checking, We check the previous column's value
									nextparent = a2Dobj[tmprow][tmpcol-1];
									//If it is null, nodeValue is child;
									//Otherwise, is not child, we need get out this loop and 
									//find another matched parent.
									Log.info(debugmsg+" next parent : "+nextparent);
								}while(tmprow<a2Dobj.length && nextparent==null);
								
								//If none of its children can match, we should continue to find next matched node at this column.
								if(!childMatch){
									Log.info(debugmsg+"No child '"+childPath+"' can be found under path "+matchpath);
									searchrow++;
									continue;
								}
							}
						}catch(IndexOutOfBoundsException e){
							Log.debug(debugmsg+" Can not find a matched path.");
							return null;
						}
					}
					
					//We add this node to matchpath, and process next path.
					matchpath = matchpath + a2Dobj[searchrow][searchcol];
					Log.index(debugmsg+"match path is "+matchpath);
					//searchnode found, are there more nodes in fullpath?
					if (delimptr > 0) {
						//get next searchnode in fullpath
						searchpos = delimptr + delim.length();
						delimptr = fullpath.indexOf(delim, searchpos);
						if (delimptr > 0)
							searchnode = fullpath.substring(searchpos, delimptr);	//...up to next delim
						else
							searchnode = fullpath.substring(searchpos);				//...remainder of fullpath
						
						if (searchnode.equals(""))
							searchpos = fullpath.length();	//no more nodes to be found, exit loop
						else {
							//increment to new cell search location
							matchpath = matchpath + delim;
							searchcol++;
							searchrow++;
						}
					}
					else 
						searchpos = fullpath.length();	//no more nodes to be found, exit loop
				}
				else
					searchrow++;	//searchnode not found, incr searchrow
			}
			else
				searchrow++;		//searchnode not found, incr searchrow
		}
		
		//return location of located cell
		NodeInfo node = new NodeInfo();
		if (searchrow == a2Dobj.length)
			return null;							//cell was not found in a2Dobj
		else {
			//cell was found, return node (w/ its located information)
			node.setPath(matchpath);
			node.setPoint(new Point(searchrow, searchcol));
			return node;
		}
	}
}
