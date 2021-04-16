
/******************************************************************************
 * FlexColor.java
 *
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
 * !!! DO NOT EDIT THIS FILE !!!
 * This file is automatically generated from XML source.  Any changes you make 
 * here will be erased the next time the file is generated.
 *
 * The following assets are needed to generate this file:
 *
 *   FlexColorFunctions.xml
 *   keyword_library.dtd
 *   XSLJavaCommonFunctions.xsl
 *   XSLJavaComponentModel.xsl
 *
 * Example invocation to generate:
 *
 *   msxsl.exe FlexColorFunctions.xml XSLJavaComponentModel.xsl -o FlexColor.java
 *
 ******************************************************************************/ 
package org.safs.model.components;

import org.safs.model.commands.FlexColorFunctions;
import org.safs.model.ComponentFunction;
import org.safs.model.components.UIComponent;
import org.safs.model.StepTestTable;

public class FlexColor extends GenericObject {

    /*****************
    Constructor 

    Create an instance of pseudo-component representing 
    a specific component in a specific window.
    
    @param window  Optional:NO 
           Specifies which Window this component is 'in'.
    @param compname Optional:NO 
           Specifies the AppMap name of the component in the Window.
    ****************/
    public FlexColor(Window window, String compname) {

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
    public FlexColor(String winname, String compname) {

        this(new Window(winname), compname);
    }

    protected FlexColor(String compname) {

        super(compname);
    }



    /*********** <pre> 
                 Action to set a color according to its color value. Currently, this component function is for Flex ColorPicker only.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational RobotJ</LI>
    </UL>

     @param colorValue  Optional:NO 
                 ColorValue to set.
              
     **********/
    public ComponentFunction setColor(String colorValue ) {

        if ( colorValue == null ) throw new IllegalArgumentException ( "setColor.colorValue = null");
        return FlexColorFunctions.setColor(getWindow().getName(), getName(), colorValue);
    }

    /*********** <pre> 
                 Action to set a color according to its color value. Currently, this component function is for Flex ColorPicker only.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational RobotJ</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param colorValue  Optional:NO 
                 ColorValue to set.
              
     **********/
    public void setColor(StepTestTable table, String colorValue ) {

        if ( table == null ) throw new IllegalArgumentException ( "setColor.table = null");

        if ( colorValue == null ) throw new IllegalArgumentException ( "setColor.colorValue = null");
        table.add( FlexColorFunctions.setColor(getWindow().getName(), getName(), colorValue));
    }

}
