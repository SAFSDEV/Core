
/******************************************************************************
 * HTMLLink.java
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
 *   HTMLLinkFunctions.xml
 *   keyword_library.dtd
 *   XSLJavaCommonFunctions.xsl
 *   XSLJavaComponentModel.xsl
 *
 * Example invocation to generate:
 *
 *   msxsl.exe HTMLLinkFunctions.xml XSLJavaComponentModel.xsl -o HTMLLink.java
 *
 ******************************************************************************/ 
package org.safs.model.components;

import org.safs.model.commands.HTMLLinkFunctions;
import org.safs.model.ComponentFunction;
import org.safs.model.components.UIComponent;
import org.safs.model.StepTestTable;

public class HTMLLink extends HTML {

    /*****************
    Constructor 

    Create an instance of pseudo-component representing 
    a specific component in a specific window.
    
    @param window  Optional:NO 
           Specifies which Window this component is 'in'.
    @param compname Optional:NO 
           Specifies the AppMap name of the component in the Window.
    ****************/
    public HTMLLink(Window window, String compname) {

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
    public HTMLLink(String winname, String compname) {

        this(new Window(winname), compname);
    }

    protected HTMLLink(String compname) {

        super(compname);
    }



    /*********** <pre> 
                 Use Generic CLICK where possible.
               
                Attempts to perform a standard Click on an HTMLLink on a webpage.
                
                Example:
                
                BrowserWin SomeLink Click
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Mercury Interactive WinRunner</LI>
    </UL>

     **********/
    public ComponentFunction click( ) {

        return HTMLLinkFunctions.click(getWindow().getName(), getName());
    }

    /*********** <pre> 
                 Use Generic CLICK where possible.
               
                Attempts to perform a standard Click on an HTMLLink on a webpage.
                
                Example:
                
                BrowserWin SomeLink Click
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     **********/
    public void click(StepTestTable table ) {

        if ( table == null ) throw new IllegalArgumentException ( "click.table = null");

        table.add( HTMLLinkFunctions.click(getWindow().getName(), getName()));
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

     @param vPAsset  Optional:NO 
                Name of the pre-existing VP asset stored in the currently running script.
              
     **********/
    public ComponentFunction compareStoredData(String vPAsset ) {

        if ( vPAsset == null ) throw new IllegalArgumentException ( "compareStoredData.vPAsset = null");
        return HTMLLinkFunctions.compareStoredData(getWindow().getName(), getName(), vPAsset);
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

     @param table  Optional:NO
            The table to add the record to.
     @param vPAsset  Optional:NO 
                Name of the pre-existing VP asset stored in the currently running script.
              
     **********/
    public void compareStoredData(StepTestTable table, String vPAsset ) {

        if ( table == null ) throw new IllegalArgumentException ( "compareStoredData.table = null");

        if ( vPAsset == null ) throw new IllegalArgumentException ( "compareStoredData.vPAsset = null");
        table.add( HTMLLinkFunctions.compareStoredData(getWindow().getName(), getName(), vPAsset));
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

     @param vPAsset  Optional:NO 
                Name of the pre-existing VP asset stored in the currently running script.
              
     **********/
    public ComponentFunction compareStoredProperties(String vPAsset ) {

        if ( vPAsset == null ) throw new IllegalArgumentException ( "compareStoredProperties.vPAsset = null");
        return HTMLLinkFunctions.compareStoredProperties(getWindow().getName(), getName(), vPAsset);
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

     @param table  Optional:NO
            The table to add the record to.
     @param vPAsset  Optional:NO 
                Name of the pre-existing VP asset stored in the currently running script.
              
     **********/
    public void compareStoredProperties(StepTestTable table, String vPAsset ) {

        if ( table == null ) throw new IllegalArgumentException ( "compareStoredProperties.table = null");

        if ( vPAsset == null ) throw new IllegalArgumentException ( "compareStoredProperties.vPAsset = null");
        table.add( HTMLLinkFunctions.compareStoredProperties(getWindow().getName(), getName(), vPAsset));
    }

}
