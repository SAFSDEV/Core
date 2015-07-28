/**
 ** Copyright (C) Continental Teves Hungary Ltd., All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent.agents;

import org.safs.SAFSException;
import org.safs.SAFSStringTokenizer;
import org.safs.Log;

import java.awt.Component;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;


/**
 * 
 * @author Szucs
 * @since Feb 20, 2006
 */
public class JMenuAgent extends JComponentAgent {
    
    public static final String objectType = "JMenu";
    
    
    /** Creates a new instance of JMenuAgent */
    public JMenuAgent( ) {
        super( );
        
        setAlternateAncestorClassname( org.safs.jvmagent.agents.JComponentAgent.class.getName( ) );
        Log.info( "org.safs.jvmagent.agents.JMenuAgent initialization complete" );
        
    }
    
    
    /**
     * Return the subitem at the specified index from the given object.
     * This may be a ComboBox item, a List item, or a Tree node, etc...
     * The returned item may be a component object or perhaps a String representing the
     * text of the item.  The return type is object specific.
     * @param object reference from which to locate the subitem.
     * @param index of the subitem to retrieve.
     * @return subitem object or String
     * @throws an Exception if the subitem index is invalid or subitem is unobtainable.
     */
    public Object getSubItemAtIndex(Object object, int index) throws Exception {
        Log.info( "org.safs.jvmagent.agents.JMenuAgent.getSubItemAtIndex( )" );
        //object is a Component type
        JMenu menu = ( JMenu ) object;
        if ( ( index < 0 ) || ( index > menu.getMenuComponentCount( ) ) ) {
            throw new SAFSException( "Menu subitem index is out of range!" );
        }
        
        // ret can be any instance of JSeparator(!) or JMenuItem
        // (that is JMenuItem itself, JMenu, JCheckBoxMenuItem and JRadioButtonMenuItem)
        Object ret = menu.getMenuComponent( index );
        // please consider that name is generally not a localized name and often equals to null
        // ret = menu.getMenuComponent( index ).getName( );
        
        /*
        // for example for JMenu subcomponents...
        Component item = menu.getMenuComponent( index );
        if ( item instanceof JSeparator ) {
            // ret = ""; ???
            ret = null;            
        } else {
            ret = ( ( JMenuItem )item ).getText( );
        }
        return ret;*/
        
        return ret;
    }
    
    /**
     * Return the subitem at the specified string path from the given object.
     * This is a hierarchical path of parent->child relationships separated by "->".
     * The returned item may be a component object or perhaps a String representing the
     * text of the item.  The return type is object specific.  This is often used
     * to identify items in Menus and Trees.
     * <p>
     * Ex:
     * <p>
     *     File->Exit<br/>
     *     Root->Branch->Leaf
     *
     * @param object
     * @param path to desired subitem using item->subitem->subitem format.
     * @return subitem object or String
     */
    public Object getMatchingPathObject(Object object, String path) throws Exception {
        Log.info( "org.safs.jvmagent.agents.JMenuAgent.getMatchingPathObject( )" );
        return traverseMenu( object, path );
    }
    
    /**
     * Determine if the object contains a subitem/object identified
     * by the provided Path.  Path is hierarchical information showing parent->child
     * relationships separated by '->'.  This is often used in Menus and Trees.
     * <p>
     * Ex:
     * <p>
     *     File->Exit<br/>
     *     Root->Branch->Leaf
     *
     * @param object--Object proxy for the object to be evaluated.
     *
     * @param path information to locate another object or subitem relative to object.
     *        this is usually something like a menuitem or tree node where supported.
     *
     * @return true if the child sub-object was found relative to object.
     **/
    public boolean isMatchingPath(Object object, String path) throws Exception {
        Log.info( "org.safs.jvmagent.agents.JMenuAgent.isMatchingPath( )" );
        Log.info( "path is: " + path );
        Object ret = traverseMenu( object, path );
        if ( ret != null ) return true;
        else return false;
    }
    
    
    /**
     * returns a menu item control according to the path
     * @param object a menu or menu item control
     * @param path the path of the menu item on the menu hierarchy
     */
    protected Object traverseMenu( Object object, String path ) {
        JComponent item = ( JComponent )object;
        SAFSStringTokenizer tokenizer = new SAFSStringTokenizer( path, "->" );
        
        mainwhile:
            while ( tokenizer.hasMoreTokens( ) ) {
            String token = tokenizer.nextToken( ).trim( );
            if ( item instanceof JMenu ) {
                JMenu menu = ( JMenu )item;
                Component[] items = menu.getMenuComponents( );
                for ( int i = 0; i < items.length; i++ ) {
                    if ( !( items[ i ] instanceof JSeparator )
                    && ( ( ( JMenuItem )items[ i ] ).getText( ).equalsIgnoreCase( token ) ) ) {
                        item = ( JMenuItem )items[ i ];
                        continue mainwhile;
                    }
                }
                return null;
            } else if ( item instanceof JMenuItem ) {                
                return null;
            }
            }
        
        return item;
    }
}
