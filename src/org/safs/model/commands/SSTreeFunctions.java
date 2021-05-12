
/******************************************************************************
 * SSTreeFunctions.java
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * !!! DO NOT EDIT THIS FILE !!!
 * This file is automatically generated from XML source.  Any changes you make 
 * here will be erased the next time the file is generated.
 *
 * The following assets are needed to generate this file:
 *
 *   SSTreeFunctions.xml
 *   keyword_library.dtd
 *   XSLJavaCommonFunctions.xsl
 *   XSLJavaCommandModel.xsl
 *
 * Example invocation to generate:
 *
 *   msxsl.exe SSTreeFunctions.xml XSLJavaCommandModel.xsl -o SSTreeFunctions.java
 *
 ******************************************************************************/ 
package org.safs.model.commands;


import org.safs.model.ComponentFunction;


public class SSTreeFunctions {

    /*****************
    Private Singleton Instance
    ****************/
    private static final SSTreeFunctions singleton = new SSTreeFunctions(); 

    /*****************
    Private Constructor
    Static class needing no instantiation.
    ****************/
    private SSTreeFunctions() {}

    /*****************
    public Singleton to access class static methods via instance
    ****************/
    public static SSTreeFunctions getInstance() { return singleton;}

    /** "CollapseNode" */
    static public final String COLLAPSENODE_KEYWORD = "CollapseNode";
    /** "ExpandNode" */
    static public final String EXPANDNODE_KEYWORD = "ExpandNode";
    /** "SelectNode" */
    static public final String SELECTNODE_KEYWORD = "SelectNode";
    /** "VerifyExpandedState" */
    static public final String VERIFYEXPANDEDSTATE_KEYWORD = "VerifyExpandedState";
    /** "VerifySelectedText" */
    static public final String VERIFYSELECTEDTEXT_KEYWORD = "VerifySelectedText";


    /*********** <pre> 
                Routine to DblClick a node according to its AppMap reference.
               
                Routine to DblClick a node according to its AppMap reference.  Because 
                the SSTree is unsupported, this reference is the x,y coordinate of a 
                GenericObject DblClick command in the form x,y (i.e. "25,10").
                Because of the nature of the reference, we cannot check whether or 
                not the node is collapsed/expanded before or after this routine or 
                even if we clicked on the right node.  Verify the selection and 
                expanded state of the node before and/or after to check.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param appMapSubkey  Optional:NO 
                The named reference of the coordinates to expand/collapse
              
     **********/
    static public ComponentFunction collapseNode (String winname, String compname, String appMapSubkey) {

        if ( winname == null ) throw new IllegalArgumentException ( "collapseNode.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "collapseNode.compname = null");

        if ( appMapSubkey == null ) throw new IllegalArgumentException ( "collapseNode.appMapSubkey = null");
        ComponentFunction cf = new ComponentFunction(COLLAPSENODE_KEYWORD, winname, compname);
        cf.addParameter(appMapSubkey);
        return cf;
    }


    /*********** <pre> 
                Routine to DblClick a node according to its AppMap reference.
               
                Routine to DblClick a node according to its AppMap reference.  Because 
                the SSTree is unsupported, this reference is the x,y coordinate of a 
                GenericObject DblClick command in the form x,y (i.e. "25,10").
                Because of the nature of the reference, we cannot check whether or 
                not the node is collapsed/expanded before or after this routine or 
                even if we clicked on the right node.  Verify the selection and 
                expanded state of the node before and/or after to check.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param appMapSubkey  Optional:NO 
                The named reference of the coordinates to expand/collapse
              
     **********/
    static public ComponentFunction expandNode (String winname, String compname, String appMapSubkey) {

        if ( winname == null ) throw new IllegalArgumentException ( "expandNode.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "expandNode.compname = null");

        if ( appMapSubkey == null ) throw new IllegalArgumentException ( "expandNode.appMapSubkey = null");
        ComponentFunction cf = new ComponentFunction(EXPANDNODE_KEYWORD, winname, compname);
        cf.addParameter(appMapSubkey);
        return cf;
    }


    /*********** <pre> 
                Routine to select a node according to its AppMap reference.
               
                Routine to select a node according to its AppMap reference.  Because 
                the SSTree is unsupported this reference is the x,y coordinate of a 
                GenericObject Click command in the form x,y (i.e. "25,10").
                Because of the nature of the reference, we cannot check whether or 
                not the node is collapsed/expanded before or after this routine or 
                even if we clicked on the right node.  Verify the selection and 
                expanded state of the node before and/or after to check.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param appMapSubkey  Optional:NO 
                The named reference of the coordinates to click
              
     **********/
    static public ComponentFunction selectNode (String winname, String compname, String appMapSubkey) {

        if ( winname == null ) throw new IllegalArgumentException ( "selectNode.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "selectNode.compname = null");

        if ( appMapSubkey == null ) throw new IllegalArgumentException ( "selectNode.appMapSubkey = null");
        ComponentFunction cf = new ComponentFunction(SELECTNODE_KEYWORD, winname, compname);
        cf.addParameter(appMapSubkey);
        return cf;
    }


    /*********** <pre> 
                Routine to verify the state of the Expanded property--True or False--
                for the node with the provided text.
               
                Routine to verify the state of the Expanded property--True or False--
                for the node with the provided text.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                Case-sensitive text of the node to check.
              
     @param expectedState  Optional:NO 
                Expected State: "True" or "False" (not case-sensitive)
              
     **********/
    static public ComponentFunction verifyExpandedState (String winname, String compname, String textValue, String expectedState) {

        if ( winname == null ) throw new IllegalArgumentException ( "verifyExpandedState.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "verifyExpandedState.compname = null");

        if ( expectedState == null ) throw new IllegalArgumentException ( "verifyExpandedState.expectedState = null");
        if ( textValue == null ) throw new IllegalArgumentException ( "verifyExpandedState.textValue = null");
        ComponentFunction cf = new ComponentFunction(VERIFYEXPANDEDSTATE_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        cf.addParameter(expectedState);
        return cf;
    }


    /*********** <pre> 
                Routine to verify the state of the Expanded property--True or False--
                for the node with the provided text.
               
                Routine to verify the state of the Expanded property--True or False--
                for the node with the provided text.
                  </pre>    
    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>        textValue -- Optional:NO 
                Case-sensitive text of the node to check.
              <BR/>        expectedState -- Optional:NO 
                Expected State: "True" or "False" (not case-sensitive)
              
    </UL>

     **********/
    static public ComponentFunction verifyExpandedState(String winname, String compname, String[] parameters) {

        if ( winname == null ) throw new IllegalArgumentException ( "verifyExpandedState.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "verifyExpandedState.compname = null");
        if ( parameters == null ) throw new IllegalArgumentException ( "verifyExpandedState.parameters = null");
        ComponentFunction cf = new ComponentFunction(VERIFYEXPANDEDSTATE_KEYWORD, winname, compname);
        cf.addParameters(parameters);
        return cf;
    }


    /*********** <pre> 
                Routine to verify the text of the currently selected node.
               
                Routine to verify the text of the currently selected node.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                Case-sensitive text expected in the selected node.
              
     **********/
    static public ComponentFunction verifySelectedText (String winname, String compname, String textValue) {

        if ( winname == null ) throw new IllegalArgumentException ( "verifySelectedText.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "verifySelectedText.compname = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "verifySelectedText.textValue = null");
        ComponentFunction cf = new ComponentFunction(VERIFYSELECTEDTEXT_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        return cf;
    }


}
