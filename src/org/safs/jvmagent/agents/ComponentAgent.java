/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent.agents;

import java.awt.*;
import javax.accessibility.AccessibleContext;

import org.safs.SAFSRuntimeException;
import org.safs.StatusCodes;
import org.safs.TestRecordData;
import org.safs.jvmagent.NoSuchPropertyException;
import org.safs.jvmagent.SAFSInvalidActionRuntimeException;
import org.safs.jvmagent.SAFSInvalidComponentRuntimeException;
import org.safs.jvmagent.SAFSObjectNotFoundRuntimeException;

import org.safs.Log;


/**
 * @author canagl
 * <br/>MAY 26, 2005	(CANAGL) Standardized property names
 *
 * Feb 16, 2006 (Szucs) adding the converCoord( ) method
 */
public class ComponentAgent extends ObjectAgent {

	/** 
	 * "Component"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "Component";

	/**
	 * Constructor for ComponentAgent.
	 */
	public ComponentAgent() {
		super();		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.ObjectAgent.class.getName());
	}

	/**
	 * Verify testRecordData.getCommand() is not null or 0-length.
	 * @return the retrieved action command.
	 * @throws SAFSInvalidActionRuntimeException("Invalid Action") if action is null or 0-length.
	 */
	protected String validateActionCommand(TestRecordData testRecordData){
		String action = testRecordData.getCommand();
		if (action == null) throw new SAFSInvalidActionRuntimeException("Invalid Action");
		if (action.length() == 0) throw new SAFSInvalidActionRuntimeException("Invalid Action");
		return action;
	}
	
	/**
	 * Verify the object is an instanceof java.awt.Component.
	 * @return the cast Component on success.
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if component is null.
	 * @throws SAFSInvalidComponentRuntimeException(object.getClass().getName()) if component is not a java.awt.Component.
	 */
	protected Component validateComponent(Object object){
		if (object instanceof java.awt.Component) return (java.awt.Component)object;
		if(object == null) throw new SAFSObjectNotFoundRuntimeException("Invalid object");
		throw new SAFSInvalidComponentRuntimeException(object.getClass().getName());
	}
        
        
        /**
         * Convert coordinates string of the formats:
         * <ul>
         * <li>"x;y"
         * <li>"x,y"
         * <li>"Coords=x;y"
         * <li>"Coords=x,y"
         * </ul>
         * into a java.awt.Point object.
         * <p>
         * Subclasses may override to convert alternative values, such
         * as Row and Col values as is done in org.safs.rational.CFTable
         *
         * @param   coords, String x;y or x,y or Coords=x;y  or Coords=x,y
         * @return  Point if successfull, null otherwise
         * @author CANAGL OCT 21, 2005 modified to work as required for
         *                              keywords as documented.
         **/
        public java.awt.Point convertCoords(String coords) {
            // CANAGL OCT 21, 2005 This function previously did NOT support the
            // "Coords=" prefix and used to decrement 1 for all provided values.
            // It also did not accept coords of x or y < 0.  And it allowed the
            // y value to be left off.
            // The routine has been modified to leave the provided values "as-is"
            // and to support the "Coords=" prefix.  The y value
            Log.info("convertCoords...");
            try {
                int coordsindex = coords.indexOf("=");
                int sindex = coords.indexOf(";");
                if (sindex < 0) sindex = coords.indexOf(",");
                if ((sindex < 1)||(sindex < coordsindex)) return null;
                
                // properly handles case where coordsindex = -1 (not found)
                String xS = coords.substring(coordsindex+1, sindex).trim();
                String yS = coords.substring(sindex+1).trim();
                if ((xS.length()==0)||(yS.length()==0)) return null; // assumption
                
                Log.info("x: "+xS);
                Log.info("y: "+yS);
                
                int y = Integer.parseInt(yS);
                int x = Integer.parseInt(xS);
                
                Log.debug("converted coords: x: "+x+", y: "+y);
                return new java.awt.Point(x, y);
                
            } catch (Exception ee) {
                Log.debug( "bad coords format: "+ coords, ee);
                return null;
            }
        }
}
