
/******************************************************************************
 * ToolBar.java
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
 *   ToolBarFunctions.xml
 *   keyword_library.dtd
 *   XSLJavaCommonFunctions.xsl
 *   XSLJavaComponentModel.xsl
 *
 * Example invocation to generate:
 *
 *   msxsl.exe ToolBarFunctions.xml XSLJavaComponentModel.xsl -o ToolBar.java
 *
 ******************************************************************************/ 
package org.safs.model.components;

import org.safs.model.commands.ToolBarFunctions;
import org.safs.model.ComponentFunction;
import org.safs.model.components.UIComponent;
import org.safs.model.StepTestTable;

public class ToolBar extends GenericObject {

    /*****************
    Constructor 

    Create an instance of pseudo-component representing 
    a specific component in a specific window.
    
    @param window  Optional:NO 
           Specifies which Window this component is 'in'.
    @param compname Optional:NO 
           Specifies the AppMap name of the component in the Window.
    ****************/
    public ToolBar(Window window, String compname) {

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
    public ToolBar(String winname, String compname) {

        this(new Window(winname), compname);
    }

    protected ToolBar(String compname) {

        super(compname);
    }



    /*********** <pre> 
                Deprecated.  Use ClickButtonText instead.
               
                Deprecated.  Use ClickButtonText instead.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param buttonTextValue  Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              
     @param caseInsensitive  Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
     **********/
    public ComponentFunction clickButton(String buttonTextValue, String caseInsensitive ) {

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "clickButton.buttonTextValue = null");
        return ToolBarFunctions.clickButton(getWindow().getName(), getName(), buttonTextValue, caseInsensitive);
    }

    /*********** <pre> 
                Deprecated.  Use ClickButtonText instead.
               
                Deprecated.  Use ClickButtonText instead.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param buttonTextValue  Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              
     @param caseInsensitive  Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
     **********/
    public void clickButton(StepTestTable table, String buttonTextValue, String caseInsensitive ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickButton.table = null");

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "clickButton.buttonTextValue = null");
        table.add( ToolBarFunctions.clickButton(getWindow().getName(), getName(), buttonTextValue, caseInsensitive));
    }

    /*********** <pre> 
                Deprecated.  Use ClickButtonText instead.
               
                Deprecated.  Use ClickButtonText instead.
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         buttonTextValue -- Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              <BR/>         caseInsensitive -- Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
    </UL>

     **********/
    public ComponentFunction clickButton(String[] parameters ) {

        if ( parameters == null ) throw new IllegalArgumentException ( "clickButton.parameters = null");
        return ToolBarFunctions.clickButton(getWindow().getName(), getName(), parameters);
    }

    /*********** <pre> 
                Deprecated.  Use ClickButtonText instead.
               
                Deprecated.  Use ClickButtonText instead.
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         buttonTextValue -- Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              <BR/>         caseInsensitive -- Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
    </UL>

     **********/
    public void clickButton(StepTestTable table, String[] parameters ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickButton.table = null");
        if ( parameters == null ) throw new IllegalArgumentException ( "clickButton.parameters = null");
        table.add( ToolBarFunctions.clickButton(getWindow().getName(), getName(), parameters));
    }

    /*********** <pre> 
                Click on the icon at a certain position on the ToolBar object, it is 1-based.
               
                Click on the icon at a certain position on the ToolBar object, it is 1-based.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param index  Optional:NO 
                 Index described which icon to be clicked in the toolbar.
              
     @param coordination  Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
     **********/
    public ComponentFunction clickButtonIndex(String index, String coordination ) {

        if ( index == null ) throw new IllegalArgumentException ( "clickButtonIndex.index = null");
        return ToolBarFunctions.clickButtonIndex(getWindow().getName(), getName(), index, coordination);
    }

    /*********** <pre> 
                Click on the icon at a certain position on the ToolBar object, it is 1-based.
               
                Click on the icon at a certain position on the ToolBar object, it is 1-based.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param index  Optional:NO 
                 Index described which icon to be clicked in the toolbar.
              
     @param coordination  Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
     **********/
    public void clickButtonIndex(StepTestTable table, String index, String coordination ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickButtonIndex.table = null");

        if ( index == null ) throw new IllegalArgumentException ( "clickButtonIndex.index = null");
        table.add( ToolBarFunctions.clickButtonIndex(getWindow().getName(), getName(), index, coordination));
    }

    /*********** <pre> 
                Click on the icon at a certain position on the ToolBar object, it is 1-based.
               
                Click on the icon at a certain position on the ToolBar object, it is 1-based.
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         index -- Optional:NO 
                 Index described which icon to be clicked in the toolbar.
              <BR/>         coordination -- Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
    </UL>

     **********/
    public ComponentFunction clickButtonIndex(String[] parameters ) {

        if ( parameters == null ) throw new IllegalArgumentException ( "clickButtonIndex.parameters = null");
        return ToolBarFunctions.clickButtonIndex(getWindow().getName(), getName(), parameters);
    }

    /*********** <pre> 
                Click on the icon at a certain position on the ToolBar object, it is 1-based.
               
                Click on the icon at a certain position on the ToolBar object, it is 1-based.
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         index -- Optional:NO 
                 Index described which icon to be clicked in the toolbar.
              <BR/>         coordination -- Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
    </UL>

     **********/
    public void clickButtonIndex(StepTestTable table, String[] parameters ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickButtonIndex.table = null");
        if ( parameters == null ) throw new IllegalArgumentException ( "clickButtonIndex.parameters = null");
        table.add( ToolBarFunctions.clickButtonIndex(getWindow().getName(), getName(), parameters));
    }

    /*********** <pre> 
                Routine to Click on the specified Button.Key (Text) value.
               
                Routine to Click on the specified Button.Key (Text) value.  
                The user has an optional parameter to specify that the provided 
                text is case-insensitive.  The routine will also verify that the 
                button is in the clicked or selected state upon completion.
                Use ClickUnverifiedButtonText if this post-click verification should 
                be skipped.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param buttonTextValue  Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              
     @param caseInsensitive  Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
     @param coordination  Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
     **********/
    public ComponentFunction clickButtonText(String buttonTextValue, String caseInsensitive, String coordination ) {

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "clickButtonText.buttonTextValue = null");
        return ToolBarFunctions.clickButtonText(getWindow().getName(), getName(), buttonTextValue, caseInsensitive, coordination);
    }

    /*********** <pre> 
                Routine to Click on the specified Button.Key (Text) value.
               
                Routine to Click on the specified Button.Key (Text) value.  
                The user has an optional parameter to specify that the provided 
                text is case-insensitive.  The routine will also verify that the 
                button is in the clicked or selected state upon completion.
                Use ClickUnverifiedButtonText if this post-click verification should 
                be skipped.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param buttonTextValue  Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              
     @param caseInsensitive  Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
     @param coordination  Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
     **********/
    public void clickButtonText(StepTestTable table, String buttonTextValue, String caseInsensitive, String coordination ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickButtonText.table = null");

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "clickButtonText.buttonTextValue = null");
        table.add( ToolBarFunctions.clickButtonText(getWindow().getName(), getName(), buttonTextValue, caseInsensitive, coordination));
    }

    /*********** <pre> 
                Routine to Click on the specified Button.Key (Text) value.
               
                Routine to Click on the specified Button.Key (Text) value.  
                The user has an optional parameter to specify that the provided 
                text is case-insensitive.  The routine will also verify that the 
                button is in the clicked or selected state upon completion.
                Use ClickUnverifiedButtonText if this post-click verification should 
                be skipped.
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         buttonTextValue -- Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              <BR/>         caseInsensitive -- Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              <BR/>         coordination -- Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
    </UL>

     **********/
    public ComponentFunction clickButtonText(String[] parameters ) {

        if ( parameters == null ) throw new IllegalArgumentException ( "clickButtonText.parameters = null");
        return ToolBarFunctions.clickButtonText(getWindow().getName(), getName(), parameters);
    }

    /*********** <pre> 
                Routine to Click on the specified Button.Key (Text) value.
               
                Routine to Click on the specified Button.Key (Text) value.  
                The user has an optional parameter to specify that the provided 
                text is case-insensitive.  The routine will also verify that the 
                button is in the clicked or selected state upon completion.
                Use ClickUnverifiedButtonText if this post-click verification should 
                be skipped.
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         buttonTextValue -- Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              <BR/>         caseInsensitive -- Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              <BR/>         coordination -- Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
    </UL>

     **********/
    public void clickButtonText(StepTestTable table, String[] parameters ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickButtonText.table = null");
        if ( parameters == null ) throw new IllegalArgumentException ( "clickButtonText.parameters = null");
        table.add( ToolBarFunctions.clickButtonText(getWindow().getName(), getName(), parameters));
    }

    /*********** <pre> 
                Click on the icon whose tooltip equals to the provided parameter
               
                Click on the icon whose tooltip equals to the provided parameter
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational RobotJ</LI>
    </UL>

     @param tooltip  Optional:NO 
                 Tooltip is description of the icon on ToolBar.
              
     **********/
    public ComponentFunction clickButtonTooltip(String tooltip ) {

        if ( tooltip == null ) throw new IllegalArgumentException ( "clickButtonTooltip.tooltip = null");
        return ToolBarFunctions.clickButtonTooltip(getWindow().getName(), getName(), tooltip);
    }

    /*********** <pre> 
                Click on the icon whose tooltip equals to the provided parameter
               
                Click on the icon whose tooltip equals to the provided parameter
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational RobotJ</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param tooltip  Optional:NO 
                 Tooltip is description of the icon on ToolBar.
              
     **********/
    public void clickButtonTooltip(StepTestTable table, String tooltip ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickButtonTooltip.table = null");

        if ( tooltip == null ) throw new IllegalArgumentException ( "clickButtonTooltip.tooltip = null");
        table.add( ToolBarFunctions.clickButtonTooltip(getWindow().getName(), getName(), tooltip));
    }

    /*********** <pre> 
                Routine to select a toolbar button using it's ID.
               
                Routine to select a toolbar button using it's ID.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param buttonID  Optional:NO 
                 ID for a particular button in the toolbar to select.
              
     **********/
    public ComponentFunction clickUnverifiedButtonID(String buttonID ) {

        if ( buttonID == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonID.buttonID = null");
        return ToolBarFunctions.clickUnverifiedButtonID(getWindow().getName(), getName(), buttonID);
    }

    /*********** <pre> 
                Routine to select a toolbar button using it's ID.
               
                Routine to select a toolbar button using it's ID.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param buttonID  Optional:NO 
                 ID for a particular button in the toolbar to select.
              
     **********/
    public void clickUnverifiedButtonID(StepTestTable table, String buttonID ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonID.table = null");

        if ( buttonID == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonID.buttonID = null");
        table.add( ToolBarFunctions.clickUnverifiedButtonID(getWindow().getName(), getName(), buttonID));
    }

    /*********** <pre> 
                Routine to Click on the specified Button.Key (Text) value.
               
                Routine to Click on the specified Button.Key (Text) value.
                The user has an optional parameter to specify that the provided 
                text is case-insensitive.  However, not all Toolbars and Buttons will be 
                able to support this case-insensitive capability.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param buttonTextValue  Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              
     @param caseInsensitive  Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
     @param coordination  Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
     **********/
    public ComponentFunction clickUnverifiedButtonText(String buttonTextValue, String caseInsensitive, String coordination ) {

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonText.buttonTextValue = null");
        return ToolBarFunctions.clickUnverifiedButtonText(getWindow().getName(), getName(), buttonTextValue, caseInsensitive, coordination);
    }

    /*********** <pre> 
                Routine to Click on the specified Button.Key (Text) value.
               
                Routine to Click on the specified Button.Key (Text) value.
                The user has an optional parameter to specify that the provided 
                text is case-insensitive.  However, not all Toolbars and Buttons will be 
                able to support this case-insensitive capability.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param buttonTextValue  Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              
     @param caseInsensitive  Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
     @param coordination  Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
     **********/
    public void clickUnverifiedButtonText(StepTestTable table, String buttonTextValue, String caseInsensitive, String coordination ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonText.table = null");

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonText.buttonTextValue = null");
        table.add( ToolBarFunctions.clickUnverifiedButtonText(getWindow().getName(), getName(), buttonTextValue, caseInsensitive, coordination));
    }

    /*********** <pre> 
                Routine to Click on the specified Button.Key (Text) value.
               
                Routine to Click on the specified Button.Key (Text) value.
                The user has an optional parameter to specify that the provided 
                text is case-insensitive.  However, not all Toolbars and Buttons will be 
                able to support this case-insensitive capability.
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         buttonTextValue -- Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              <BR/>         caseInsensitive -- Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              <BR/>         coordination -- Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
    </UL>

     **********/
    public ComponentFunction clickUnverifiedButtonText(String[] parameters ) {

        if ( parameters == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonText.parameters = null");
        return ToolBarFunctions.clickUnverifiedButtonText(getWindow().getName(), getName(), parameters);
    }

    /*********** <pre> 
                Routine to Click on the specified Button.Key (Text) value.
               
                Routine to Click on the specified Button.Key (Text) value.
                The user has an optional parameter to specify that the provided 
                text is case-insensitive.  However, not all Toolbars and Buttons will be 
                able to support this case-insensitive capability.
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    <LI>Rational RobotJ</LI>
    <LI>Automated QA TestComplete</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         buttonTextValue -- Optional:NO 
                 Case-sensitive text of the toolbar button to select.
              <BR/>         caseInsensitive -- Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              <BR/>         coordination -- Optional:YES 
				A position within the toolbar button, the mouse will be click there. 
				RJ Note: This parameter is special for RJ implementation.
              
    </UL>

     **********/
    public void clickUnverifiedButtonText(StepTestTable table, String[] parameters ) {

        if ( table == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonText.table = null");
        if ( parameters == null ) throw new IllegalArgumentException ( "clickUnverifiedButtonText.parameters = null");
        table.add( ToolBarFunctions.clickUnverifiedButtonText(getWindow().getName(), getName(), parameters));
    }

    /*********** <pre> 
                Verifies a particular case-sensitive button text item is selected
               
                Verifies a particular case-sensitive button text item is selected
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param buttonTextValue  Optional:NO 
                 Case-sensitive text(Button.Key) of button that will be verified as clicked
              
     **********/
    public ComponentFunction verifyButtonClicked(String buttonTextValue ) {

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "verifyButtonClicked.buttonTextValue = null");
        return ToolBarFunctions.verifyButtonClicked(getWindow().getName(), getName(), buttonTextValue);
    }

    /*********** <pre> 
                Verifies a particular case-sensitive button text item is selected
               
                Verifies a particular case-sensitive button text item is selected
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param buttonTextValue  Optional:NO 
                 Case-sensitive text(Button.Key) of button that will be verified as clicked
              
     **********/
    public void verifyButtonClicked(StepTestTable table, String buttonTextValue ) {

        if ( table == null ) throw new IllegalArgumentException ( "verifyButtonClicked.table = null");

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "verifyButtonClicked.buttonTextValue = null");
        table.add( ToolBarFunctions.verifyButtonClicked(getWindow().getName(), getName(), buttonTextValue));
    }

    /*********** <pre> 
                Verifies a total button presents in the Toolbar.
               
                Verifies a total button presents in the Toolbar.  Count includes buttons which are not visible as well.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param buttonCount  Optional:NO 
                 Numeric count value represents the total button counts in the Toolbar.
              
     **********/
    public ComponentFunction verifyButtonCount(String buttonCount ) {

        if ( buttonCount == null ) throw new IllegalArgumentException ( "verifyButtonCount.buttonCount = null");
        return ToolBarFunctions.verifyButtonCount(getWindow().getName(), getName(), buttonCount);
    }

    /*********** <pre> 
                Verifies a total button presents in the Toolbar.
               
                Verifies a total button presents in the Toolbar.  Count includes buttons which are not visible as well.
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param buttonCount  Optional:NO 
                 Numeric count value represents the total button counts in the Toolbar.
              
     **********/
    public void verifyButtonCount(StepTestTable table, String buttonCount ) {

        if ( table == null ) throw new IllegalArgumentException ( "verifyButtonCount.table = null");

        if ( buttonCount == null ) throw new IllegalArgumentException ( "verifyButtonCount.buttonCount = null");
        table.add( ToolBarFunctions.verifyButtonCount(getWindow().getName(), getName(), buttonCount));
    }

    /*********** <pre> 
                Verifies a particular case-sensitive text (button.key) item exists
               
                Verifies a particular case-sensitive text (button.key) item exists
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param buttonTextValue  Optional:NO 
                 Case-sensitive text (button.key) item that will be verified as exists
              
     @param caseInsensitive  Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
     **********/
    public ComponentFunction verifyButtonText(String buttonTextValue, String caseInsensitive ) {

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "verifyButtonText.buttonTextValue = null");
        return ToolBarFunctions.verifyButtonText(getWindow().getName(), getName(), buttonTextValue, caseInsensitive);
    }

    /*********** <pre> 
                Verifies a particular case-sensitive text (button.key) item exists
               
                Verifies a particular case-sensitive text (button.key) item exists
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param buttonTextValue  Optional:NO 
                 Case-sensitive text (button.key) item that will be verified as exists
              
     @param caseInsensitive  Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
     **********/
    public void verifyButtonText(StepTestTable table, String buttonTextValue, String caseInsensitive ) {

        if ( table == null ) throw new IllegalArgumentException ( "verifyButtonText.table = null");

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "verifyButtonText.buttonTextValue = null");
        table.add( ToolBarFunctions.verifyButtonText(getWindow().getName(), getName(), buttonTextValue, caseInsensitive));
    }

    /*********** <pre> 
                Verifies a particular case-sensitive text (button.key) item exists
               
                Verifies a particular case-sensitive text (button.key) item exists
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         buttonTextValue -- Optional:NO 
                 Case-sensitive text (button.key) item that will be verified as exists
              <BR/>         caseInsensitive -- Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
    </UL>

     **********/
    public ComponentFunction verifyButtonText(String[] parameters ) {

        if ( parameters == null ) throw new IllegalArgumentException ( "verifyButtonText.parameters = null");
        return ToolBarFunctions.verifyButtonText(getWindow().getName(), getName(), parameters);
    }

    /*********** <pre> 
                Verifies a particular case-sensitive text (button.key) item exists
               
                Verifies a particular case-sensitive text (button.key) item exists
                  </pre>    
    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param parameters  Optional:NO
            An array containing the following parameters:
    <UL>
<BR/>         buttonTextValue -- Optional:NO 
                 Case-sensitive text (button.key) item that will be verified as exists
              <BR/>         caseInsensitive -- Optional:YES 
                 "1", "CaseInsensitive", or "Case-Insensitive" to match button text in a case-insensitive manner.
              
    </UL>

     **********/
    public void verifyButtonText(StepTestTable table, String[] parameters ) {

        if ( table == null ) throw new IllegalArgumentException ( "verifyButtonText.table = null");
        if ( parameters == null ) throw new IllegalArgumentException ( "verifyButtonText.parameters = null");
        table.add( ToolBarFunctions.verifyButtonText(getWindow().getName(), getName(), parameters));
    }

    /*********** <pre> 
                Verifies a particular case-sensitive button text item is unselected
               
                Verifies a particular case-sensitive button text item is unselected
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param buttonTextValue  Optional:NO 
                 Case-sensitive text(Button.Key) of button that will be verified as unselected
              
     **********/
    public ComponentFunction verifyButtonUnClicked(String buttonTextValue ) {

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "verifyButtonUnClicked.buttonTextValue = null");
        return ToolBarFunctions.verifyButtonUnClicked(getWindow().getName(), getName(), buttonTextValue);
    }

    /*********** <pre> 
                Verifies a particular case-sensitive button text item is unselected
               
                Verifies a particular case-sensitive button text item is unselected
                  </pre>    Supporting Engines:
    <P/><UL>
    <LI>Rational Robot</LI>
    </UL>

     @param table  Optional:NO
            The table to add the record to.
     @param buttonTextValue  Optional:NO 
                 Case-sensitive text(Button.Key) of button that will be verified as unselected
              
     **********/
    public void verifyButtonUnClicked(StepTestTable table, String buttonTextValue ) {

        if ( table == null ) throw new IllegalArgumentException ( "verifyButtonUnClicked.table = null");

        if ( buttonTextValue == null ) throw new IllegalArgumentException ( "verifyButtonUnClicked.buttonTextValue = null");
        table.add( ToolBarFunctions.verifyButtonUnClicked(getWindow().getName(), getName(), buttonTextValue));
    }

}
