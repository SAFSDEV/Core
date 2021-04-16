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
package org.safs.jvmagent.agents;

import org.safs.SAFSException;
import org.safs.SAFSStringTokenizer;
import org.safs.Log;

import java.awt.Component;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;


/**
 * 
 * @author szucs
 * @since Feb 19, 2006
 * @TODO Swing menu handling except for popup menus through Java (no Abbot API used here).
 * See also {@link org.safs.abbot.jvmagent.agents.JMenuBarAgent}
 * and {@link org.safs.jvmagent.agents.JPopupMenuAgent}!
 */
public class JMenuBarAgent extends JComponentAgent {
    
    public static final String objectType = "JMenuBar";
    
    
    /**
     * Constructor for JMenuBarAgent.
     */
    public JMenuBarAgent( ) {
        super( );
        
        setAlternateAncestorClassname( org.safs.jvmagent.agents.JComponentAgent.class.getName( ) );
        Log.info( "org.safs.jvmagent.agents.JMenuBarAgent initialization complete" );
    }                       
    
    
    /**
     * does a click operation on the menu item relevant to the path
     * @param bar the Swing menubar control
     * @param path the menu path string
     * @param pressTime the time to press the menu buttons for
     */
    protected void doClick( JMenuBar bar, String path, int pressTime ) throws SAFSException {
        //looks like this operation does not update the GUI nicely...
        //otherwise it should work triggering the relevant menu action
        SAFSException exception = new SAFSException( "Menu path is invalid." );
        JComponent item = bar;
        SAFSStringTokenizer tokenizer = new SAFSStringTokenizer( path, "|" );
        
        mainwhile:
        while ( tokenizer.hasMoreTokens( ) ) {
            String token = tokenizer.nextToken( ).trim( );
            Log.info( "token: " + token );            
            if ( item instanceof JMenuBar ) {
                JMenuBar menuBar = ( JMenuBar )item;
                for ( int i = 0; i < menuBar.getMenuCount( ); i++ ) {
                        JMenuItem menuItem = null;
                        try {
                            menuItem = (JMenuItem) getSubItemAtIndex(menuBar, i);
                        } catch ( Exception ex ) {
                            Log.info( "getSubItemAtIndex() exception: " + ex.getMessage( ) );
                            //cannot get here, can we?
                        }
                    // equalsIgnoreCase() is good here?
                    String text = menuItem.getText( );
                    Log.info( "text: " + text );
                    if ( menuItem.getText( ).equalsIgnoreCase( token ) ) {
                        Log.info( "token ok inside JMenuBar analysis" );
                        menuItem.doClick( 68 );
                        menuItem.invalidate( );
                        try {
                            Thread.currentThread( ).sleep( pressTime );
                        } catch ( InterruptedException ie ) { ; }
                        item = menuItem;
                        continue mainwhile;
                    }                    
                }
                throw exception;
            } else if ( item instanceof JMenu ) {
                JMenu menu = ( JMenu )item;    
                Component[] items = menu.getMenuComponents( );
                for ( int i = 0; i < items.length; i++ ) {    
                    if ( !( items[ i ] instanceof JSeparator ) ) {
                        JMenuItem menuItem = ( JMenuItem )items[ i ];
                        String text = menuItem.getText( );
                        Log.info( "text: " + text );                        
                        if ( menuItem.getText( ).equalsIgnoreCase( token )  ) {
                            Log.info( "token ok inside JMenu analysis" );                            
                            menuItem.doClick( pressTime );       
                            menuItem.invalidate( );                            
                            try {
                                Thread.currentThread( ).sleep( pressTime );
                            } catch ( InterruptedException ie ) { ; }
                            item = menuItem;
                            continue mainwhile;
                        }
                    }
                }    
                throw exception;
            } else if ( item instanceof JMenuItem ) {
                throw exception;                
            }            
        }        
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
        Log.info( "org.safs.jvmagent.agents.JMenuBarAgent.getSubItemAtIndex( )" );
        //object is a Component type
        JMenuBar menuBar = ( JMenuBar ) object;
        if ( ( index < 0 ) || ( index > menuBar.getMenuCount( ) ) ) {
            throw new SAFSException( "MenuBar subitem index is out of range!" );
        }
        
        // ret can be any instance of JMenuItem (that is JMenuItem itself, JMenu, JCheckBoxMenuItem and JRadioButtonMenuItem)
        Object ret = menuBar.getMenu( index );
        // please consider that name is generally not a localized name and ofter equals to null
        //ret = menuBar.getMenu( index ).getName( );
        
        // for example for AbstractButton subcomponents...
        //ret = menuBar.getMenu( index ).getText( );
        
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
        Log.info( "org.safs.jvmagent.agents.JMenuBarAgent.getMatchingPathObject( )" );    
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
        Log.info( "org.safs.jvmagent.agents.JMenuBarAgent.isMatchingPath( )" );
        Log.info( "path is: " + path );
        Object ret = traverseMenu( object, path );
        if ( ret != null ) return true;
        else return false;
    }
    
    
    /**
     * returns a menu item control according to the path
     * @param object a menubar, a menu or a menu item control
     * @param path the path of the menu item on the menu hierarchy
     */    
    protected Object traverseMenu( Object object, String path ) {
        JComponent item = ( JComponent )object;
        SAFSStringTokenizer tokenizer = new SAFSStringTokenizer( path, "->" );
        
        mainwhile:
        while ( tokenizer.hasMoreTokens( ) ) {
            String token = tokenizer.nextToken( ).trim( );
            if ( item instanceof JMenuBar ) {
                JMenuBar menuBar = ( JMenuBar )item;
                for ( int i = 0; i < menuBar.getMenuCount( ); i++ ) {
                        JMenuItem menuItem = null;
                        try {
                            menuItem = (JMenuItem) getSubItemAtIndex(menuBar, i);
                        } catch ( Exception ex ) {
                            //cannot get here, can we?
                        }
                    // equalsIgnoreCase() is good here?
                    if ( menuItem.getText( ).equalsIgnoreCase( token ) ) {
                        item = menuItem;
                        continue mainwhile;
                    }                    
                }
                return null;
            } else if ( item instanceof JMenu ) {
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
