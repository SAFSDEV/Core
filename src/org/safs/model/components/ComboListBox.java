
/******************************************************************************
 * ComboListBox.java
 *
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 *
 * !!! DO NOT EDIT THIS FILE !!!
 * This file is automatically generated from XML source.  Any changes you make 
 * here will be erased the next time the file is generated.
 *
 * The following assets are needed to generate this file:
 *
 *   ComboListBoxFunctions.xml
 *   keyword_library.dtd
 *   XSLJavaCommonFunctions.xsl
 *   XSLJavaComponentModel.xsl
 *
 * Example invocation to generate:
 *
 *   msxsl.exe ComboListBoxFunctions.xml XSLJavaComponentModel.xsl -o ComboListBox.java
 *
 ******************************************************************************/ 
package org.safs.model.components;

import org.safs.model.commands.ComboListBoxFunctions;
import org.safs.model.ComponentFunction;
import org.safs.model.components.UIComponent;
import org.safs.model.StepTestTable;

public class ComboListBox extends GenericObject {

    /*****************
    Constructor 

    Create an instance of pseudo-component representing 
    a specific component in a specific window.
    
    @param window  Optional:NO 
           Specifies which Window this component is 'in'.
    @param compname Optional:NO 
           Specifies the AppMap name of the component in the Window.
    ****************/
    public ComboListBox(Window window, String compname) {

        super(window, compname);
    }

    /*****************
    Constructor 

    Create an instance of pseudo-component representing 
    a specific component in a specific window.
    
    This convenience routine will create the requisite Window component.
    
    @param winname  Optional:NO 
           Specifies the AppMap name of the window.
    @param compname Optional:NO 
           Specifies the AppMap name of the component in the Window.
    ****************/
    public ComboListBox(String winname, String compname) {

        this(new Window(winname), compname);
    }

    protected ComboListBox(String compname) {

        super(compname);
    }



    /*********** <pre> 
                 Select an item by its text value from an exposed combobox dropdown list.
               
                 Select an item by its text value from an exposed combobox dropdown list.  
                    This method requires that the list already be exposed via a ComboBox 
                    Click command.  To combine both the Click and the Selection in a single
                    command use the ComboBox Select command.
                 
                 We first test to make sure the item to select is actually in the list.
                 If it is not, we report the failure and issue an ESCAPE character to 
                 close the list. 
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Mercury Interactive WinRunner</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param textValue  Optional:NO 
                 The case-sensitive text item to select
              
     **********/
    public ComponentFunction select(String textValue ) {

        if ( textValue == null ) throw new IllegalArgumentException ( "select.textValue = null");
        return ComboListBoxFunctions.select(getWindow().getName(), getName(), textValue);
    }

    /*********** <pre> 
                 Select an item by its text value from an exposed combobox dropdown list.
               
                 Select an item by its text value from an exposed combobox dropdown list.  
                    This method requires that the list already be exposed via a ComboBox 
                    Click command.  To combine both the Click and the Selection in a single
                    command use the ComboBox Select command.
                 
                 We first test to make sure the item to select is actually in the list.
                 If it is not, we report the failure and issue an ESCAPE character to 
                 close the list. 
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Mercury Interactive WinRunner</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param textValue  Optional:NO 
                 The case-sensitive text item to select
              
     **********/
    public void select(StepTestTable table, String textValue ) {

        if ( table == null ) throw new IllegalArgumentException ( "select.table = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "select.textValue = null");
        table.add( ComboListBoxFunctions.select(getWindow().getName(), getName(), textValue));
    }

    /*********** <pre> 
                 Select an item by its index from a combolistbox.
               
                Select an item by its index from a combolistbox.  
                The list must already be exposed via a preceeding ComboBox Click.
                
                The routine will attempt to see if the index exists in the list. 
                It will fail if it cannot validate the index or cannot 
                read the array of items.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Mercury Interactive WinRunner</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param indexValue  Optional:NO 
                 The item index in the exposed list to select.
              
     **********/
    public ComponentFunction selectIndex(String indexValue ) {

        if ( indexValue == null ) throw new IllegalArgumentException ( "selectIndex.indexValue = null");
        return ComboListBoxFunctions.selectIndex(getWindow().getName(), getName(), indexValue);
    }

    /*********** <pre> 
                 Select an item by its index from a combolistbox.
               
                Select an item by its index from a combolistbox.  
                The list must already be exposed via a preceeding ComboBox Click.
                
                The routine will attempt to see if the index exists in the list. 
                It will fail if it cannot validate the index or cannot 
                read the array of items.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Mercury Interactive WinRunner</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param indexValue  Optional:NO 
                 The item index in the exposed list to select.
              
     **********/
    public void selectIndex(StepTestTable table, String indexValue ) {

        if ( table == null ) throw new IllegalArgumentException ( "selectIndex.table = null");

        if ( indexValue == null ) throw new IllegalArgumentException ( "selectIndex.indexValue = null");
        table.add( ComboListBoxFunctions.selectIndex(getWindow().getName(), getName(), indexValue));
    }

}
