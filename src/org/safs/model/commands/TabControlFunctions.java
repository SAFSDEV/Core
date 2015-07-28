
/******************************************************************************
 * TabControlFunctions.java
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
 *   TabControlFunctions.xml
 *   keyword_library.dtd
 *   XSLJavaCommonFunctions.xsl
 *   XSLJavaCommandModel.xsl
 *
 * Example invocation to generate:
 *
 *   msxsl.exe TabControlFunctions.xml XSLJavaCommandModel.xsl -o TabControlFunctions.java
 *
 ******************************************************************************/ 
package org.safs.model.commands;


import org.safs.model.ComponentFunction;


public class TabControlFunctions {

    /*****************
    Private Singleton Instance
    ****************/
    private static final TabControlFunctions singleton = new TabControlFunctions(); 

    /*****************
    Private Constructor
    Static class needing no instantiation.
    ****************/
    private TabControlFunctions() {}

    /*****************
    public Singleton to access class static methods via instance
    ****************/
    public static TabControlFunctions getInstance() { return singleton;}

    /** "Click" */
    static public final String CLICK_KEYWORD = "Click";
    /** "ClickTab" */
    static public final String CLICKTAB_KEYWORD = "ClickTab";
    /** "ClickTabContains" */
    static public final String CLICKTABCONTAINS_KEYWORD = "ClickTabContains";
    /** "MakeSelection" */
    static public final String MAKESELECTION_KEYWORD = "MakeSelection";
    /** "SelectTab" */
    static public final String SELECTTAB_KEYWORD = "SelectTab";
    /** "SelectTabIndex" */
    static public final String SELECTTABINDEX_KEYWORD = "SelectTabIndex";
    /** "UnverifiedClick" */
    static public final String UNVERIFIEDCLICK_KEYWORD = "UnverifiedClick";
    /** "UnverifiedClickTab" */
    static public final String UNVERIFIEDCLICKTAB_KEYWORD = "UnverifiedClickTab";


    /*********** <pre> 
                 Use ClickTab instead (where available).
               
                Attempts to perform a standard Click on a particular Tab on the TabControl.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                 Case-sensitive text* on the tab to identify which tab to click.
              
     @param process  Optional:YES 
                 Indicator for unverified click.
              
     **********/
    static public ComponentFunction click (String winname, String compname, String textValue, String process) {

        if ( winname == null ) throw new IllegalArgumentException ( "click.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "click.compname = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "click.textValue = null");
        ComponentFunction cf = new ComponentFunction(CLICK_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        cf.addParameter(process);
        return cf;
    }


    /*********** <pre> 
                 Use ClickTab instead (where available).
               
                Attempts to perform a standard Click on a particular Tab on the TabControl.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                  </pre>    
    Supporting Engines:
    <P/><UL>
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
                 Case-sensitive text* on the tab to identify which tab to click.
              <BR/>        process -- Optional:YES 
                 Indicator for unverified click.
              
    </UL>

     **********/
    static public ComponentFunction click(String winname, String compname, String[] parameters) {

        if ( winname == null ) throw new IllegalArgumentException ( "click.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "click.compname = null");
        if ( parameters == null ) throw new IllegalArgumentException ( "click.parameters = null");
        ComponentFunction cf = new ComponentFunction(CLICK_KEYWORD, winname, compname);
        cf.addParameters(parameters);
        return cf;
    }


    /*********** <pre> 
                 Attempts to perform a standard Click on a particular Tab on the TabControl
               
                Attempts to perform a standard Click on a particular Tab on the TabControl.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Rational RobotJ</LI>
        <LI>Mercury Interactive WinRunner</LI>
        <LI>Automated QA TestComplete</LI>
        <LI>Google Android</LI>
        <LI>OpenQA Selenium</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                 Case-sensitive text on the tab to identify which tab to click.
              
     **********/
    static public ComponentFunction clickTab (String winname, String compname, String textValue) {

        if ( winname == null ) throw new IllegalArgumentException ( "clickTab.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "clickTab.compname = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "clickTab.textValue = null");
        ComponentFunction cf = new ComponentFunction(CLICKTAB_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        return cf;
    }


    /*********** <pre> 
                Attempts to perform a Click on a Tab according to a partial match of its text value.
               
                Attempts to perform a Click on a Tab according to a partial match of its text value.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                Note: this keyword used to be UnverifiedClickTabContains, and it was renamed on 01/26/2011 due to conflict.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational RobotJ</LI>
        <LI>Automated QA TestComplete</LI>
        <LI>Google Android</LI>
        <LI>OpenQA Selenium</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                 Partial case-sensitive text on the tab to identify which tab to click.
              
     **********/
    static public ComponentFunction clickTabContains (String winname, String compname, String textValue) {

        if ( winname == null ) throw new IllegalArgumentException ( "clickTabContains.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "clickTabContains.compname = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "clickTabContains.textValue = null");
        ComponentFunction cf = new ComponentFunction(CLICKTABCONTAINS_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        return cf;
    }


    /*********** <pre> 
                 Attempts to perform a standard Click on a particular Tab on the TabControl
               
                Attempts to perform a standard Click on a particular Tab on the TabControl.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Rational RobotJ</LI>
        <LI>Mercury Interactive WinRunner</LI>
        <LI>Automated QA TestComplete</LI>
        <LI>Google Android</LI>
        <LI>OpenQA Selenium</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                 Case-sensitive text on the tab to identify which tab to click.
              
     **********/
    static public ComponentFunction makeSelection (String winname, String compname, String textValue) {

        if ( winname == null ) throw new IllegalArgumentException ( "makeSelection.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "makeSelection.compname = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "makeSelection.textValue = null");
        ComponentFunction cf = new ComponentFunction(MAKESELECTION_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        return cf;
    }


    /*********** <pre> 
                 Attempts to perform a standard SelectTab on a particular Tab on the TabControl
               
                Attempts to perform a standard Selection on a particular Tab on the TabControl.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                Note, this routine will call the CLICK function to try 
                default CLICK processing if it is not able to locate all 
                the component properties it needs for success.
                This command has been provided for times when it might 
                work and the other TabControl CLICK alternatives do not.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Rational RobotJ</LI>
        <LI>Mercury Interactive WinRunner</LI>
        <LI>Automated QA TestComplete</LI>
        <LI>Google Android</LI>
        <LI>OpenQA Selenium</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                 Case-sensitive text on the tab to identify which tab to click.
              
     **********/
    static public ComponentFunction selectTab (String winname, String compname, String textValue) {

        if ( winname == null ) throw new IllegalArgumentException ( "selectTab.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "selectTab.compname = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "selectTab.textValue = null");
        ComponentFunction cf = new ComponentFunction(SELECTTAB_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        return cf;
    }


    /*********** <pre> 
                 Attempts to select a particular Tab by Index.
              
                Attempts to select a particular Tab by Index.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                
                For some engines, like Rational Robot, the user must know whether the indices of 
                the TabControl are 0-based or 1-based and use the raw index number.  
                
                
                For Rational Functional Tester we attempt to enforce that all indices are 
                1-based whenever possible.  Thus, the first tab would be specified as TabIndex=1 
                even if the control itself uses 0-based tab indices.
                
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Rational RobotJ</LI>
        <LI>Automated QA TestComplete</LI>
        <LI>Google Android</LI>
        <LI>OpenQA Selenium</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param tabIndex  Optional:NO 
                 Index of the tab to click.
              
     **********/
    static public ComponentFunction selectTabIndex (String winname, String compname, String tabIndex) {

        if ( winname == null ) throw new IllegalArgumentException ( "selectTabIndex.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "selectTabIndex.compname = null");

        if ( tabIndex == null ) throw new IllegalArgumentException ( "selectTabIndex.tabIndex = null");
        ComponentFunction cf = new ComponentFunction(SELECTTABINDEX_KEYWORD, winname, compname);
        cf.addParameter(tabIndex);
        return cf;
    }


    /*********** <pre> 
                 Use UnverifiedClickTab instead (where available).
               
                Attempts to perform an unverified Click on a particular Tab on the TabControl.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Mercury Interactive WinRunner</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                 Case-sensitive text* on the tab to identify which tab to click.
              
     **********/
    static public ComponentFunction unverifiedClick (String winname, String compname, String textValue) {

        if ( winname == null ) throw new IllegalArgumentException ( "unverifiedClick.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "unverifiedClick.compname = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "unverifiedClick.textValue = null");
        ComponentFunction cf = new ComponentFunction(UNVERIFIEDCLICK_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        return cf;
    }


    /*********** <pre> 
                 Attempts to perform an unverified Click on a particular Tab on the TabControl
               
                Attempts to perform an unverified Click on a particular Tab on the TabControl.
                The routine will set the StepDriverTestInfo.statuscode and
                log any pass/warning/fail info using the StepDriverTestInfo.fac LogFacility.
                  </pre>    Supporting Engines:
    <P/><UL>
        <LI>Rational Robot</LI>
        <LI>Rational RobotJ</LI>
        <LI>Automated QA TestComplete</LI>
        <LI>Google Android</LI>
        <LI>OpenQA Selenium</LI>
    </UL>

     @param winname  Optional:NO
            The name of the window to act upon.
     @param compname  Optional:NO
            The name of the component to act upon.
     @param textValue  Optional:NO 
                 Case-sensitive text* on the tab to identify which tab to click.
              
     **********/
    static public ComponentFunction unverifiedClickTab (String winname, String compname, String textValue) {

        if ( winname == null ) throw new IllegalArgumentException ( "unverifiedClickTab.winname = null");
        if ( compname == null ) throw new IllegalArgumentException ( "unverifiedClickTab.compname = null");

        if ( textValue == null ) throw new IllegalArgumentException ( "unverifiedClickTab.textValue = null");
        ComponentFunction cf = new ComponentFunction(UNVERIFIEDCLICKTAB_KEYWORD, winname, compname);
        cf.addParameter(textValue);
        return cf;
    }


}
