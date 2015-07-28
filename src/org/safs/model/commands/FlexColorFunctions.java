
/******************************************************************************
 * FlexColorFunctions.java
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
 *   FlexColorFunctions.xml
 *   keyword_library.dtd
 *   XSLJavaCommonFunctions.xsl
 *   XSLJavaCommandModel.xsl
 *
 * Example invocation to generate:
 *
 *   msxsl.exe FlexColorFunctions.xml XSLJavaCommandModel.xsl -o FlexColorFunctions.java
 *
 ******************************************************************************/ 
package org.safs.model.commands;


import org.safs.model.ComponentFunction;


public class FlexColorFunctions {

    /*****************
    Private Singleton Instance
    ****************/
    private static final FlexColorFunctions singleton = new FlexColorFunctions(); 

    /*****************
    Private Constructor
    Static class needing no instantiation.
    ****************/
    private FlexColorFunctions() {}

    /*****************
    public Singleton to access class static methods via instance
    ****************/
    public static FlexColorFunctions getInstance() { return singleton;}

    /** "SetColor" */
    static public final String SETCOLOR_KEYWORD = "SetColor";


    /*********** <pre> 
                 Action to set a color according to its color value. Currently, this component function is for Flex ColorPicker only.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational RobotJ</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param colorValue  Optional:NO 
                 ColorValue to set.
              
     **********/
    static public ComponentFunction setColor (String winname, String compname, String colorValue) {

        if ( winname == null ) throw new IllegalArgumentException ( "setColor.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "setColor.compname = null");

        if ( colorValue == null ) throw new IllegalArgumentException ( "setColor.colorValue = null");
        ComponentFunction cf = new ComponentFunction(SETCOLOR_KEYWORD, winname, compname);
        cf.addParameter(colorValue);
        return cf;
    }


}
