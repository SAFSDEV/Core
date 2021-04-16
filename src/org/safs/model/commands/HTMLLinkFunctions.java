
/******************************************************************************
 * HTMLLinkFunctions.java
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
 *   HTMLLinkFunctions.xml
 *   keyword_library.dtd
 *   XSLJavaCommonFunctions.xsl
 *   XSLJavaCommandModel.xsl
 *
 * Example invocation to generate:
 *
 *   msxsl.exe HTMLLinkFunctions.xml XSLJavaCommandModel.xsl -o HTMLLinkFunctions.java
 *
 ******************************************************************************/ 
package org.safs.model.commands;


import org.safs.model.ComponentFunction;


public class HTMLLinkFunctions {

    /*****************
    Private Singleton Instance
    ****************/
    private static final HTMLLinkFunctions singleton = new HTMLLinkFunctions(); 

    /*****************
    Private Constructor
    Static class needing no instantiation.
    ****************/
    private HTMLLinkFunctions() {}

    /*****************
    public Singleton to access class static methods via instance
    ****************/
    public static HTMLLinkFunctions getInstance() { return singleton;}

    /** "Click" */
    static public final String CLICK_KEYWORD = "Click";
    /** "CompareStoredData" */
    static public final String COMPARESTOREDDATA_KEYWORD = "CompareStoredData";
    /** "CompareStoredProperties" */
    static public final String COMPARESTOREDPROPERTIES_KEYWORD = "CompareStoredProperties";


    /*********** <pre> 
                 Use Generic CLICK where possible.
               
                Attempts to perform a standard Click on an HTMLLink on a webpage.
                
                Example:
                
                BrowserWin SomeLink Click
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     **********/
    static public ComponentFunction click (String winname, String compname) {

        if ( winname == null ) throw new IllegalArgumentException ( "click.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "click.compname = null");

        ComponentFunction cf = new ComponentFunction(CLICK_KEYWORD, winname, compname);
        return cf;
    }


    /*********** <pre> 
                Performs a HTMLLinkVP CompareData on an HTMLLink object.
               
                Performs a HTMLLinkVP CompareData on an HTMLLink object.
                
                THE BENCHMARK VP MUST ALREADY EXIST AND BE AN ASSET OF THE CURRENTLY
                RUNNING SCRIPT.
                 
                Modified VP parameter information can be added to the standard
                VP=VPName by including the VPName reference in the application map
                in a section defined for the HTMLLink.  If this is done, the value 
                retrieved from the application map will be appended to VP=VPName.
                The required semicolon for this append will be provided by this routine.
                
                Example 1: Perform a standard HTMLLink CompareData.  To perform
                a basic CompareData the name "StoredVP" will not exist in the app map:
                
                The Step File call:
                
                BrowserWindow AnHTMLLink CompareStoredData StoredVP
                
                This will produce a CompareData VP with "VP=StoredVP;Wait=2,10".
                
                The StoredVP baseline MUST already exist as an asset of the
                currently running script.
                
                Example 2: Perform a HTMLLink CompareData providing addition 
                parameter information (such as ExpectedResult=FAIL).  To do this the
                HTMLLink object must have its own section in the app map and an item
                with the same name as the StoredVP.  The value of that item will be
                appended to the standard VP argument with a semicolon.
                
                Part of App Map:
                
                [BrowserWindow]
                BrowserWindow=WindowTag=WEBBrowser
                AnHTMLLink=<snipped for brevity>;\;Type=HTMLLink;HTMLID=LinkID
                ...
                [AnHTMLLink]
                StoredVP=ExpectedResult=FAIL;Wait=3,30
                 
                The Step File call:
                
                BrowserWindow AnHTMLLink CompareStoredData StoredVP
                
                This will produce a CompareData VP with all the parameters appended
                like this: "VP=StoredVP;ExpectedResult=FAIL;Wait=3,30".
                NOTE:When stored parameters are found in the app map then the default Wait= 
                parameter used in the standard compare is no longer provided.  If you still 
                need a Wait= parameter, then it must be included in the stored parameters.
                
                The StoredVP baseline MUST already exist as an asset of the
                currently running script.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param vPAsset  Optional:NO 
                Name of the pre-existing VP asset stored in the currently running script.
              
     **********/
    static public ComponentFunction compareStoredData (String winname, String compname, String vPAsset) {

        if ( winname == null ) throw new IllegalArgumentException ( "compareStoredData.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "compareStoredData.compname = null");

        if ( vPAsset == null ) throw new IllegalArgumentException ( "compareStoredData.vPAsset = null");
        ComponentFunction cf = new ComponentFunction(COMPARESTOREDDATA_KEYWORD, winname, compname);
        cf.addParameter(vPAsset);
        return cf;
    }


    /*********** <pre> 
                Performs a HTMLLinkVP CompareProperties on an HTMLLink object.
               
                Performs a HTMLLinkVP CompareProperties on an HTMLLink object.
                
                THE BENCHMARK VP MUST ALREADY EXIST AND BE AN ASSET OF THE CURRENTLY
                RUNNING SCRIPT.
                
                Modified VP parameter information can be added to the standard
                VP=VPName by including the VPName reference in the application map
                in a section defined for the HTMLLink.  If this is done, the value 
                retrieved from the application map will be appended to VP=VPName.
                The required semicolon for this append will be provided by this routine.
                
                Example 1: Perform a standard HTMLLink CompareProperties.  To perform
                a basic CompareProperties the name "StoredVP" will not exist in the app map:
                
                The Step File call:
                    
                BrowserWindow AnHTMLLink CompareStoredProperties StoredVP
                   
                This will produce a CompareProperties VP with "VP=StoredVP;Wait=2,10".
                
                The StoredVP baseline MUST already exist as an asset of the
                currently running script.
                  
                Example 2: Perform a HTMLLink CompareProperties providing addition 
                parameter information (such as ExpectedResult=FAIL).  To do this the
                HTMLLink object must have its own section in the app map and an item
                with the same name as the StoredVP.  The value of that item will be
                appended to the standard VP argument with a semicolon.
                
                Part of App Map:
                
                [BrowserWindow]
                BrowserWindow=WindowTag=WEBBrowser
                AnHTMLLink=<snipped for brevity>;\;Type=HTMLLink;HTMLID=LinkID
                ...
                [AnHTMLLink]
                StoredVP=ExpectedResult=FAIL;Wait=3,30
                
                The Step File call:
                 
                BrowserWindow AnHTMLLink CompareStoredProperties StoredVP
                
                This will produce a CompareProperties VP with all the parameters appended
                like this: "VP=StoredVP;ExpectedResult=FAIL;Wait=3,30".
                NOTE:When stored parameters are found in the app map then the default Wait= 
                parameter used in the standard compare is no longer provided.  If you still 
                need a Wait= parameter, then it must be included in the stored parameters.
                
                The StoredVP baseline MUST already exist as an asset of the
                currently running script.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param vPAsset  Optional:NO 
                Name of the pre-existing VP asset stored in the currently running script.
              
     **********/
    static public ComponentFunction compareStoredProperties (String winname, String compname, String vPAsset) {

        if ( winname == null ) throw new IllegalArgumentException ( "compareStoredProperties.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "compareStoredProperties.compname = null");

        if ( vPAsset == null ) throw new IllegalArgumentException ( "compareStoredProperties.vPAsset = null");
        ComponentFunction cf = new ComponentFunction(COMPARESTOREDPROPERTIES_KEYWORD, winname, compname);
        cf.addParameter(vPAsset);
        return cf;
    }


}
