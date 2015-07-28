package org.safs;

import java.util.*;
/**
 * @author Carl Nagle, SEP 02, 2004
 *         modified processChildren to skip checking children of Comboboxes
 * @author JunwuMa, NOV 03, 2008. Modified GuiChildIterator(Object,GuiObjectVector,List)
 *                  skipping the topmost Flex window("Type=FlexWindow") in Flex recognition strings.
 * @author Carl Nagle, APR 22, 2009
 *         Initial mods to support new (quicker?) FULLPATH_SEARCH_MODE.
 * @author Carl Nagle, MAY 01, 2009
 *         Complete FULLPATH_SEARCH_MODE implementation.
 */
public class GuiChildIterator {

	/** 
	 * 0=Classic Search Mode 
	 * The default mode which attempts to mimic the IBM Rational Robot search algorithm in order 
	 * to retain compatibility with IBM Rational Robot recognition strings.  Hierarchy and class/type 
	 * indices are incremented in the classic manner:
	 * <pre>
     *        1
     *        /\
     *       2  5
     *      /    \--\--\---\---\
     *     3      6  6  6  10? 10?
     *    /              \
     *   4                7
     *                     \
     *                      8
     *                       \
     *                        9
	 * </pre>
	 ***/
	public static final int CLASSIC_SEARCH_MODE = 0;
	/** 
	 * 1=Fullpath Search Mode (New, and incompatible with IBM Rational Robot) 
	 * A new search mode which requires recognition strings to contain info for each hierarchical 
	 * level in the parent/child ancestry tree.  This mode will NOT search children of a parent that 
	 * itself is not a match for the fullpath recognition string.  Hierarchy and class/type indices 
	 * are incremented uniquely (and reset?) at each hierarchy level.  This is expected to improve 
	 * performance (time) in locating matches at the expense of having longer recognition strings  
	 * enabling the match at each level in the hierarchy.  CLASSIC search mode would find the item #5 
	 * below after testing 14 objects.  FULLPATH search mode should find the same #5 item after testing 
	 * only 8 objects.
	 * <pre>
     *        1
     *        /\
     *       1  2
     *      /    \--\--\--\--\
     *     1      1  2  3  4  5
     *    /           \  \
     *   1             1  1
     *                     \--\
     *                      1  2
     * </pre>
	 ***/
	public static final int FULLPATH_SEARCH_MODE = 1;
	
	/** Default is CLASSIC_SEARCH_MODE **/
	protected static int SEARCH_MODE = CLASSIC_SEARCH_MODE;
	
	/**
	 * @return the SEARCH_MODE
	 */
	public static int getSearchMode() {
		return SEARCH_MODE;
	}

	/**
	 * @param search_mode the SEARCH_MODE to set
	 */
	public static void setSearchMode(int search_mode) {
		SEARCH_MODE = search_mode;
	}


	/** Stores object references that match pieces of our recognition strings. **/
	protected static Vector matches = null;	
	
	/** Flag indicating whether object searches should continue to iterate. **/
	protected static boolean notFound = true;
	
	/** Flag indicating a final match was achieved for the object vector. **/
	protected static boolean hasFinalMatch = false;
	
	/** Stores object references at absolute indices. */
    protected static Hashtable absindices = new Hashtable(38);
	
	/** Stores list of matching named items. */
    protected  List gather = null;
    
    /** Returns are stored List. */
    public List getGather () {return gather;}

	/** Returns stored absolute indices Hashtable. */
	public static Hashtable getAbsoluteIndexStore() { return absindices; }

	/** Reset\Clear stored absolute indices. */
	public static void resetAbsoluteIndexStore() { absindices.clear(); }

	/**
	 * Retrieves the final list of possible child matches.
	 * Each item in the Vector is an object of type MatchData.
	 * 
	 * @return the never-null Vector list of matched objects (if any).
	 */
	public static Vector getMatches(){ return matches; }
	
	  
	/**
	 * ONLY used internally by the initial GuiChildIterator for each subsequent 
	 * child level in the hierarchy search.
     * @param gather, List containing names matched, if null, then match first name
	 **/
	public GuiChildIterator(List gather) {
    	super();
        this.gather = gather;
    }

	/**
	 * Called only once by some external routine kicking off a TestObject search.
	 * The govLevel and objLevel in the ObjectVector will be assumed to be 0.
	 * The routine will install an initial class and type indices for the provided parent.
	 * <p>
	 * If we can deduce that the parent actually exists in the path vector then that 
	 * govLevel of the vector will be matched and skipped. If we find that the parent info 
	 * is NOT in the provided vector then we will not skip the govLevel.
	 * <p>
	 * We can assume the parent info is in the path if the path begins with "\;".  The 
	 * next item in the path is assumed to be the topmost parent.
	 * <p>
	 * We can ignore the first path info if it instead begins with ".\;".  The next item 
	 * would be considered to be the first child of the parent.
	 * <p>
	 * @param aparent the topmost parent to search.
	 * <p>
	 * @param agovVector the govVector (recognition string) to satisfy with the search.
	 * <p>
     * @param gather, List containing names matched, if null, then match first name
	 */
	public GuiChildIterator (Object aparent, GuiObjectVector agovVector,
                                  java.util.List gather) {
        this(gather);
        if(agovVector.isFullPathSearchMode()) {
        	setSearchMode(FULLPATH_SEARCH_MODE);
        }else{
        	setSearchMode(CLASSIC_SEARCH_MODE);
        }
		Hashtable classindices = new Hashtable(8);
		Hashtable typeindices = new Hashtable(8);
		matches = new Vector(10, 3);
		notFound = true;
		hasFinalMatch = false;
		
		// class (ex: JFrame) and Type (ex: Window) counters are complimentary
		// for each class that is a known type BOTH counters get incremented.
		
		// always initialize class index counters for the retrieved class
		
		//it is possible this "parent" info is actually info for the first child govLevel 0
		GuiObjectRecognition gorParent = agovVector.getParentGuiObjectRecognition();
		GuiClassData         classdata = agovVector.getGuiClassData();
		
		String classname = gorParent.getObjectClassName(aparent);
		Log.info("GCI: processing children of parent:"+ classname);
		classindices.put(classname, new Integer(1));

		// always initialize the Type index counter if it is equivalent to a known type
		String typeclass = classdata.getMappedClassType(classname, aparent);		
		Log.info("GCI: processing parent of type:"+ typeclass);
		if (typeclass instanceof String) {
			StringTokenizer toker = new StringTokenizer(typeclass, classdata.DEFAULT_TYPE_SEPARATOR);
			String atoken = null;
			while (toker.hasMoreTokens()){
				atoken = toker.nextToken().trim();
                typeindices.put(atoken, new Integer(1));		
			}
        }
		
		// add our entry govVector and parent object as the first match
		// CANAGL - modifying to only store finalMatches
		//MatchData adata = new MatchData(agovVector, 0, 0, aparent);
		//matches.addElement(adata);
		
		int agovDepth = agovVector.getRecognitionDepth();
		Log.info("GCI: processing GOV with depth of:"+ agovDepth +", path="+ agovVector.getPathVector());

		// begin the reentrant search for the matching child
		int startlevel = (agovDepth > 1) ? 1 : 0;
		if ((gorParent.getGovLevel()==0)&&
			(!(gorParent.pathInfo.equals(GuiObjectVector.ACTIVE_WINDOW_REFERENCE)))){

			// Robot Java recognition strings often contain a JavaWindow reference 
			// to the parent window that needs to be skipped.	Same with Flex recognition strings(JunwuMa).		
			if(! gorParent.getClassRecognition().equalsIgnoreCase("Type=JavaWindow")&&
			   ! gorParent.getClassRecognition().equalsIgnoreCase("Type=FlexWindow")) { 
				startlevel = 0;
			}else{
				Log.info("GCI: bypassing govLevel=0 for child 'Type=JavaWindow'. Assuming duplicate parent info.");
			}
		}
		//Try to match the parent for some popupmenu
		Hashtable save_classindices = (Hashtable) classindices.clone();
		Hashtable save_typeindices = (Hashtable) typeindices.clone();
		
		processParent(aparent, agovVector, startlevel, 1, classindices, typeindices, typeclass);
		Object matchedObject = this.getMatchedGuiObject();
		
		if(matchedObject==null){
			if(SEARCH_MODE != FULLPATH_SEARCH_MODE){
				Log.info("GCI: CLASSIC_SEARCH_MODE calling processChildren...");
				processChildren(aparent, agovVector, startlevel, 1, classindices, typeindices, typeclass);
			}else{
				Log.info("GCI: FULLPATH_SEARCH_MODE calling processChildren...");
				processChildren(aparent, agovVector, startlevel, 1, save_classindices, save_typeindices, typeclass);
			}
		}
	}

	/**
	 * See if the provided object reference has any children that may help satisfy the 
	 * recognition string.  This is essentially how this Iterator is "reentrant".  
	 * Objects matching pieces of our ObjectVector are added to the statically shared 
	 * matches collection until the final Object satisfying the entire ObjectVector 
	 * is found.
	 * <p>
	 * Throughout the process we must maintain an accurate count of each class and its 
	 * associated object type in each child branch.  In this way, we keep track of when 
	 * we have found the nth Index of a class or an associated object type in the branch.  
	 * Thus, for each object encountered, we must increment both the class count, and t
	 * he object type count for that class in this branch.
	 * <p>
	 * Note, as we go down an object hierarchy branch, the counts increment.  
	 * However, when we reset back up to a higher level in the object hierarchy to go down 
	 * a new branch, the counts must be reset to those counts that were valid on 
	 * entry at that level. 
	 * <p>
	 * This function is only called internally.
	 * <p>
	 * @param achild the Object to process for children.
	 * <p>
	 * @param agovVector the currently active GuiObjectVector.
	 * <p>
	 * @param agovLevel the hierarchy level in the GuiObjectVector we are trying 
	 *                  to satisfy.
	 * <p>
	 * @param aobjLevel the level in the actual object hierarchy we are searching.
	 * <p>
	 * @param classindices the storage of current class counts.
	 * <p>
	 * @param typeindices the storage of current object type counts.
	 * 
	 * @author Carl Nagle, SEP 02, 2004
	 *         modified processChildren to skip checking children of Comboboxes
	 */
	protected void processChildren(Object achild,  GuiObjectVector agovVector,
                                       int agovLevel, int aobjLevel,
                                       Hashtable classindices, Hashtable typeindices,
                                       String typeclass) {
          
          GuiObjectRecognition agor = agovVector.getParentGuiObjectRecognition();
          String classname = agor.getObjectClassName(achild);

          // CANAGL -- do not seek children in comboboxes.  messes pushbutton counts!
          try{ 
        	  if (GuiClassData.classtypeContainsClassType(typeclass,"ComboBox")){
        		  Log.debug("GCI: No children sought for ComboBoxes");
        		  return;
        	  }
        	  // if Domain = HTML and typeclass = PushButton
        	  if ((classname.toLowerCase().startsWith("html"))&&(GuiClassData.classtypeContainsClassType(typeclass,"PushButton"))){
        		  Log.debug("GCI: No children sought for HTML PushButtons");
        		  return;
        	  }
          }catch(Exception x){;}                            	

          //may be cached keys to proxies during EXTERNAL_PROCESSING
          Object[] childsChildren = agovVector.getChildObjects(achild);
          
          if ( (childsChildren instanceof Object) &&
               (childsChildren.length > 0) ){

            Log.info("GCI: "+childsChildren.length +" children in "+ classname);
            Log.info("..........Child:"+ classname +", typeclass: "+typeclass);
            GuiChildIterator it = agovVector.createGuiChildIterator(gather);
            
            boolean isTab = (GuiClassData.classtypeContainsClassType(typeclass,"TabControl"))||
            				(GuiClassData.classtypeContainsClassType(classname,"Html.HtmlBrowser"));
            
            //childsChildren may be cached keys during EXTERNAL_PROCESSING
            it.processNextLevel(childsChildren, agovVector,
                                agovLevel, aobjLevel, classindices, typeindices, isTab);
          } else {
            Log.debug("GCI: No children found for "+ classname);
          }
	}

	protected void processParent(Object parent, GuiObjectVector agovVector,
			int agovLevel, int aobjLevel, Hashtable classindices,
			Hashtable typeindices, String typeclass) {

		GuiObjectRecognition agor = agovVector.getParentGuiObjectRecognition();
		String classname = agor.getObjectClassName(parent);

		Log.debug("GCI popup class name is "+classname);
		// may be cached keys to proxies during EXTERNAL_PROCESSING
		Object[] childsChildren = {parent};
		GuiChildIterator it = agovVector.createGuiChildIterator(gather);

		it.processNextLevel(childsChildren, agovVector, agovLevel,
					aobjLevel, classindices, typeindices, false);

	}

	/**
	 * 
	 * @param child
	 * @param govVector
	 * @param objLevel
	 * @param govLevel
	 * @param classindices
	 * @param typeindices
	 * @param classname
	 * @return NULL if error occurs relating to gorInfo creation, or 
	 * NULL if object is NOT to be processed.  Otherwise, return a valid ClassTypeInfo 
	 * object with all appropriate settings.
	 */
	protected ClassTypeInfo incrementClassTypeIndices(Object child, GuiObjectVector govVector, int govLevel, int objLevel, Hashtable classindices, Hashtable typeindices, String classname){
        Log.info("GCI: getting child GOR info. GOV level:"+govLevel);               
        ClassTypeInfo cti = new ClassTypeInfo();
        final String TOOLTIP_SUFFIX = "TOOLTIP";
        cti.classname = classname;
        try{
        	cti.gorInfo = govVector.getChildGuiObjectRecognition(govLevel);
        }catch(ArrayIndexOutOfBoundsException x){
        	Log.debug("GCI: sought govLevel:"+ govLevel +", exceeds 0-based recognition path information:"+ govVector.getPathVector());
        	return null;
        }
	    GuiClassData classdata = govVector.getGuiClassData();
	    cti.typeclass = classdata.getMappedClassType(classname, child);
	    
	    //bypass panels that harbor tooltip(s)
	    Object tooltip = null;
	    String tooltipclass = null;
	    String tooltipmapped = null;
	    String gortypeclass = cti.gorInfo.getClassValue();
	    //only perform check if we are NOT looking for a tooltip
	    if((gortypeclass==null)||(gortypeclass!=null && ! gortypeclass.toUpperCase().endsWith(TOOLTIP_SUFFIX))){
		    try{
		    	Log.info("GCI.incrementClassTypeIndices evaluating possible TOOLTIP container...");
		    	if(classdata.isToolTipContainerType(cti.typeclass)){
		    		Object[] childsChildren = govVector.getChildObjects(child);
	            	if(childsChildren!=null && childsChildren.length > 0){
	            		tooltip = childsChildren[0];
	            		tooltipclass = cti.gorInfo.getObjectClassName(tooltip);
	            		try{ 
	            			if(tooltipclass.toUpperCase().endsWith(TOOLTIP_SUFFIX)){
		            	    	Log.info("GCI.incrementClassTypeIndices bypassing active TOOLTIP class!");
	            				return null; //skip panel with tooltip
	            			}
	                   		tooltipmapped = classdata.getMappedClassType(tooltipclass, tooltip);
	                		if (tooltipmapped.toUpperCase().endsWith(TOOLTIP_SUFFIX)){ 
		            	    	Log.info("GCI.incrementClassTypeIndices bypassing active TOOLTIP type!");
	                			return null; //skip panel with tooltip
	                		}
	            		}catch(Exception x){
	            	    	Log.debug("GCI.incrementClassTypeIndices ToolTip test IGNORING "+ x.getClass().getSimpleName());
	            		}
	            	}
		    	}
		    	Log.info("GCI.incrementClassTypeIndices container NOT a TOOLTIP container.");
		    }catch(Exception x){
		    	Log.debug("GCI.incrementClassTypeIndices ToolTip Container test IGNORING "+ x.getClass().getSimpleName());
		    }
		}
	    // increment class index counters for the retrieved class?
	    Log.info("GCI: incrementing class indices for:"+classname);                        	
	    Integer classindex = (Integer) classindices.get(classname);
	    classindex = (classindex == null) ? new Integer(1) : new Integer(classindex.intValue() +1);
	    classindices.put(classname, classindex);    
	    cti.classindex = classindex.intValue();	    
	    Log.info("GCI: classindices.put("+classname+", "+classindex+")");
	
	    Integer absclassindex = (Integer) absindices.get(classname);
	    absclassindex = (absclassindex == null) ? new Integer(1) : new Integer(absclassindex.intValue() +1);
	    absindices.put(classname, absclassindex);
	    cti.absoluteclassindex = absclassindex.intValue();
	    Log.info("GCI: absindices.put("+classname+", "+absclassindex+")");
	
	    // also increment the Type index counter if it is equivalent to a known type
		String gorClassType = cti.gorInfo.getClassValue();
	    Log.info("GCI: "+ classname +" mappedClassType: "+ cti.typeclass );
	    Integer typeindex = new Integer(0);
	    Integer abstypeindex = new Integer(0);
	
	    if (cti.typeclass instanceof String){
	      StringTokenizer toker = new StringTokenizer(cti.typeclass, classdata.DEFAULT_TYPE_SEPARATOR);
	      String atoken = null;
		  Integer tmptypeindex = null;
		  Integer tmpabstypeindex = null;
	
	      while(toker.hasMoreTokens()){
	      	  atoken = toker.nextToken().toUpperCase().trim();
	          tmptypeindex = (Integer) typeindices.get(atoken);
	          Log.info("GCI: getting: "+atoken+", "+tmptypeindex);
	          tmptypeindex = (tmptypeindex == null) ?
	              new Integer(1) : new Integer(tmptypeindex.intValue()+1);
	          Log.index("GCI: ... "+StringUtils.getSpaces(new Integer(objLevel))+
	                    atoken+":"+tmptypeindex);
	          typeindices.put(atoken, tmptypeindex);	          
	          tmpabstypeindex = (Integer) absindices.get(atoken);
	          tmpabstypeindex = (tmpabstypeindex == null) ?
	              new Integer(1) : new Integer(tmpabstypeindex.intValue()+1);
	          absindices.put(atoken, tmpabstypeindex);
	          Log.info("TYPE:absindices.put("+atoken+", "+tmpabstypeindex+")");
	
	          // use the correct typeindex for the classtype that matches recognition string
	          if(atoken.equalsIgnoreCase(gorClassType)){
	          	  typeindex = tmptypeindex;
	          	  cti.typeindex = typeindex.intValue();
	          	  abstypeindex = tmpabstypeindex;
	          	  cti.absolutetypeindex = abstypeindex.intValue();
	          }
	      }
	    }
	    return cti;
	}

	
	/**
	 * Called internally by processChildren only.
	 * <p>
	 * @param children -- array of child objects to examine to see if any one of them 
	 *                    satisfies the piece of the recognition string for the current 
	 *                    object vector level--the current substring of the full recognition 
	 *                    string.  May be array of cached keys if GuiObjectVector is in 
	 *                    EXTERNAL_PROCESSING_MODE.
	 * <p>
	 * @param govVector -- the currently active GuiObjectVector.
	 * <p>
	 * @param govLevel -- the hierarchy level (current substring)in the GuiObjectVector 
	 *                    we are trying to satisfy.
	 * <p>
	 * @param objLevel -- the depth in the actual application's object hierarchy currently 
	 *                    being searched.
	 * <p>
	 * @param entry_classindices -- the storage for class counts.
	 * <p>
	 * @param entry_typeindices -- the storage for object type counts.
	 * <p>
	 * @param onlyOneChildVisible -- true if parent is a TabControl-like component,
	 **/	
	protected void processNextLevel ( Object[] children,	  // children\key array to process
                                      GuiObjectVector govVector, // the full recognition vector
                                      int govLevel, // depth within the vector to process
                                      int objLevel, // depth within obj hierarchy being processed
                                      Hashtable entry_classindices, // class=count storage for class indices
                                      Hashtable entry_typeindices, // class=count storage for object indices
                                      boolean onlyOneChildVisible) { // if true, parent is tab
          boolean notDone = true;

          Hashtable classindices = entry_classindices;
          Hashtable typeindices = entry_typeindices;
          Hashtable save_classindices = (Hashtable) entry_classindices.clone();
          Hashtable save_typeindices = (Hashtable) entry_typeindices.clone();
          
          for (int i = 0; ((notFound)&&(notDone)&&(i < children.length)); i++){

          	Object _child = null;
            Object child = null;
            Object[] childsChildren = new Object[0];

            // reset indices for all but the last level of searching:
            // when dealing with things like TabControls with non-visible panels 
            // and in CLASSIC_SEARCH_MODE.
            // In FULLPATH_SEARCH_MODE these hidden panels get unique indices
            if( (govLevel  < govVector.getRecognitionDepth() -1 && onlyOneChildVisible) &&
            	(SEARCH_MODE != FULLPATH_SEARCH_MODE)){ 
              if (i>0) {
                Log.info(".....class/type indices reset for all but the last level of searching");
                classindices = (Hashtable) save_classindices.clone();
                typeindices = (Hashtable) save_typeindices.clone();
              }
            }

            // get next child and reset index counters
            // only play with GuiTestObjects
            Log.info("GCI: Seeking child("+i+")outof("+children.length+")...");
            _child = children[i];
            child = _child;
            if( ! govVector.isValidGuiObject(_child)) {
                Log.debug("GCI: skipping invalid Gui Object found in child array.");
                continue;
            }
            Log.info("GCI: child("+i+") is a valid GUI object.");

            if(govVector.getProcessMode()==GuiObjectVector.MODE_EXTERNAL_PROCESSING){
				try{child = govVector.getCachedItem(_child);}catch(Exception e){}
			}
			GuiObjectRecognition gor = govVector.getParentGuiObjectRecognition();
			String classname = gor.getObjectClassName(child);
			ClassTypeInfo typeinfo = null;
			
            // **** if (! classname.equalsIgnoreCase("Html.!")) { // this kills IE
            Log.info("GCI: child classname is:"+ classname);                        	

            // check to see if it is visible
            if (! gor.isObjectShowing(child)){

            	// if not, skip it if it is a container (like a TabControl Panel)
                Log.info("GCI: child is not showing.");                        	
            	if (govVector.isValidGuiContainer(_child)){
            		if(govVector.isFullPathSearchMode()){
            			incrementClassTypeIndices(child, govVector, govLevel, objLevel, classindices, typeindices, classname);
            		}
                    Log.info("GCI: skipping non-visible Gui Container: "+ classname);
                    continue;
            	}
            }
            //} end if classname == "Html.!"
           	typeinfo = incrementClassTypeIndices(child, govVector, govLevel, objLevel, classindices, typeindices, classname);
            if (typeinfo==null) continue; // invalid gorInfo at this level
            
            int classindex = typeinfo.classindex;
            int typeindex = typeinfo.typeindex;
            int abstypeindex = typeinfo.absolutetypeindex;
            String typeclass = typeinfo.typeclass;
            GuiObjectRecognition gorInfo = typeinfo.gorInfo;
            
            // classname  will ALWAYS have a class value
            // classindex will ALWAYS be at least 1
            // typeclass MAY BE null if we can't match the type
            // typeindex MAY BE  0   if we can't match the type
            int passindex, abspassindex;
            if (gorInfo.getClassCategoryID() == gorInfo.CATEGORY_CLASS_ID){
              passindex = classindex;
              abspassindex = typeinfo.absoluteclassindex;
            }
            // TYPE is only alternative to CLASS right now
            else{
              passindex = typeindex;
              abspassindex = abstypeindex;
            }
            // see if we match the object at this level
            boolean classMatch;
            try{
              String [] tmpName = null;
              if (gather != null) tmpName = new String[1];
              classMatch = gorInfo.isMatchingObject(child, passindex,
                                                    abspassindex, tmpName);

              // this is a special case test when we are doing this algorithm
              // but we want to gather *ALL* of the names which match, rather
              // than just the first name.  In that event, the parameter
              // 'gather' is used.  If not null, then don't let 'classMatch' get
              // beyond this place as 'true', but instead, stuff the name
              // into the List 'gather' and use 'false' for the classMatch...
              if (gather != null) {
                if (tmpName != null && tmpName[0] != null) {
                  Log.info(" .....  ADD TO GATHER: "+tmpName[0]);
                  gather.add(tmpName[0]);
                } else { // maybe use the index
                  Log.info(" .....  GATHER INDEX?? passindex, abspassindex: "+
                           passindex + ", "+abspassindex+", classmatch would be: "+
                           classMatch);
                }
                // if we are gathering then lets also gather matched child objects
                if(classMatch){
                    // for recognitions with Path=, we must get that object
                    // this is primarily used when we want ALL possible objects of 
                    // a given type.  For example, all HTMLLinks on a page.
	                Object matchObject = gorInfo.getMatchingObject(child);
	                MatchData adata = new MatchData(govVector, govLevel, objLevel, matchObject);
	                matches.addElement(adata);
                }
                classMatch = false;
              }
            }
            catch(SAFSObjectRecognitionException ore){
              classMatch = false;
              Log.debug( ore.getMessage());
            }

            if(classMatch){
              // see if we have matched to the last object vector recognition level
              notDone = ( govLevel  < (govVector.getRecognitionDepth() -1));
              Log.debug("GCI: notDone matching vector: "+ notDone +" :govLevel="+ govLevel +", maxLevel="+ (govVector.getRecognitionDepth()-1) +", path="+ govVector.getPathVector());
              if (notDone) {
                // see if this child has some children
                try{
                  int lenchildren = (typeinfo.childsChildren!=null) ? 
                		typeinfo.childsChildren.length :
                		govVector.getChildObjects(child).length;
                  if (lenchildren > 0) {
                    //Log.index("notDone: processChildren: "+(objLevel+1));
                	  if(SEARCH_MODE == FULLPATH_SEARCH_MODE){
                		  processChildren(child, govVector, govLevel + 1,
                                  objLevel + 1, save_classindices, save_typeindices, typeclass);
                	  }
                	  else { //assume CLASSIC_SEARCH_MODE
                		  processChildren(child, govVector, govLevel + 1,
                                  objLevel + 1, classindices, typeindices, typeclass);
                	  }
                  }
                  // if there are no children, then we are done going down this branch
                  // and this path will not be a match for our recognition string
                  else{
                                // signal this branch is done
                                // should this just be a continue?
                    Log.debug("GCI: Branch completed. 0 children for " + gorInfo);
                    notDone = false;
                  }
                  // signal this branch is done if no children (exception thrown for .length)
                }catch(Exception np){ 
                  Log.debug("GCI: Branch completed. NullPointer/No children for "
                            + gorInfo, np);
                  notDone = false; 
                }						
              }
              // **************************************************************
              // **** see if this is THEE child that completes our search! ****
              // **************************************************************
              else{
                hasFinalMatch = gorInfo.isFinalMatch(child);
                notFound = ! hasFinalMatch;
                // for recognitions with Path=, we must get that object
                Object matchObject = gorInfo.getMatchingObject(child);
                // removing this unregister because currently unregistering any one reference 
                // to an object seems to unregister ALL stored references to an object, 
                // including cached references in our AppMaps.
                // // must unregister child if a subitem was actually the finalObject
                // if( !(matchObject == child)) {
                //   Log.debug("RGCI: Unregistering parent "+ child.getObjectClassName() +" from JVM as needed.");
                //   child.unregister();
                // }
                //
                MatchData adata = new MatchData(govVector, govLevel, objLevel, matchObject);
                matches.addElement(adata);
              }
            }
            // not a class match for this object
            // we have to handle looking for the nth match inside a container
            // if not complete match, see if the match is farther down the hierarchy
            // but only for the CLASSIC_SEARCH_MODE.
            // We do not go down hierarchy paths for FULLPATH_SEARCH_MODE if the parent did not match
            else if(SEARCH_MODE!=FULLPATH_SEARCH_MODE){ // will be CLASSIC_SEARCH_MODE (or anything else) by default
              //Log.index("processChildren: "+(objLevel+1));
              processChildren(child, govVector, govLevel, objLevel + 1,
                              classindices, typeindices, typeclass);
            }
          }// end FOR LOOP
	}

	
	/**
	 * Retrieves the final Object that satisfies the entire GuiObjectVector recognition string.
	 * 
	 * @return valid Object if the recognition string was entirely satisfied.<br>
	 *         null if it was not.
	 */
	public Object getMatchedGuiObject(){
		
		int matchCount = matches.size();
		
		if (! hasFinalMatch){
			 // we will need to add code to resolve when multiple possible matches 
			 // exist but none were found to be "showing".  This is usually only 
			 // the case for items found by Index.
			 // If only 1 match exists, we will use it, though.
			 if (matchCount == 0) {
			 	Log.debug("GCI: No matching Object was found.");
			 	return null;
			 }
			 	
			 if (matchCount > 1) {
			 	Log.debug("Could not resolve multiple matches for the recognition string!");
			 	return null;
			 }
			 // fall out if matchCount == 1
		}

		MatchData match = (MatchData) matches.lastElement();
		return match.getGuiTestObject();
	}	
	
	public class ClassTypeInfo{
		GuiObjectRecognition gorInfo = null;
		String classname = null;
		int classindex = 0;
		int absoluteclassindex = 0;
		String typeclass = null;
		int typeindex= 0;
		int absolutetypeindex = 0;
		Object[] childsChildren = null;
	}
}

