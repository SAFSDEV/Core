/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
package org.safs.control.swing;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import org.safs.StringUtils;
import org.safs.GetText;
import org.safs.control.ControlObservable;
import org.safs.control.ControlProcessing;
import org.safs.control.ControlParameters;
import org.safs.control.ControlProcessingUI1;

/**
 * Description   : ControlStepUI, used to control an engine by sending step commands.
 * This is the UI portion. We contain an Observable (instance of ControlObservable)
 * and we implement ControlProcessing (method processingComplete).
 * A controller for teststeps: this is the GUI.
 * When our buttons are pressed, we send commands to the Observer (Controller)
 * Implement ActionListener so that our actionPerformed method can
 * react to the buttons.  Our contained ControlObservable implements Runnable
 * so that it's 'run' method can be the result of
 * starting another Thread so that the controler/Observer can receive the notification
 * messages without blocking our GUI thread.
 * @author dbauman
 * @since   NOV 24, 2003
 *
 *   <br>   NOV 24, 2003    (DBauman) Original Release
 *   <br>   DEC 17, 2003    (DBauman) changed to contain an ControlObservable.
 * <p>
 */

public class ControlStepUI implements ActionListener, ControlProcessingUI1 {

  /** set the text of the 'statusCode' text field **/
  public void setStatusCode(String status) {this.statusCode.setText(status);}
  /** set the text of the 'testLogMsg' text field **/
  public void setTestLogMsg(String testLogMsg) {this.testLogMsg.setText(testLogMsg);}
  /** set the text of the 'appMapItem' text field **/
  public void setAppMapItem(String val) {this.appMapItem.setText(val);}
  /** set the text of the 'inputRecord' text field **/
  public void setInputRecord(String rec) {this.inputRecord.setText(rec);}
  /** set the text of the 'delim' text field **/
  public void setDelim(String d) {this.delim.setText(d);}

  /** startup the observable thread **/
  private ControlObservable controlObservable = new ControlObservable(this);

  /** all of the GUI elements **/
  private JFrame frame;
  private JPanel top;
  private JTextField window;
  private JTextField component;
  private JTextField action;
  private JTextField param;
  private JTextField map;
  private JTextField section;
  private JTextField item;
  private JTextField appMapItem;
  private JTextField stepFile;
  private JTextField driver;
  private JTextField timeout;
  private JTextField statusCode;
  private JTextField testLogMsg;
  private JTextField inputRecord;
  private JTextField delim;
  private JLabel lab7;
  public static final String CENTER = "Center";
  public static final String RESOURCE_BUNDLE = "SAFSTextResourceBundle";
  private static final GetText strings = new GetText(RESOURCE_BUNDLE, Locale.getDefault());   
  private static final String BLANKSPACE = "                    ";
  private String BUSY = strings.translate("Please wait until the last command is finished.");
  private String lab7text = strings.translate("Processing Command");
  private String lab7tail= "..."+BLANKSPACE;
  /**Construct the Panel*/
  public ControlStepUI() {
    top = new JPanel();
    Dimension dim = new Dimension(588, 484);
    top.setSize(dim);
    top.setPreferredSize(dim);
    JPanel panA = new JPanel();
    panA.setBorder(BorderFactory.createTitledBorder(strings.translate("Test one AppMap entry")));
    JPanel panB = new JPanel();
    panB.setBorder(BorderFactory.createTitledBorder(strings.translate("Test using step file")));
    JPanel panC = new JPanel();
    panC.setBorder(BorderFactory.createTitledBorder(strings.translate("Application Map")));
    JPanel panD = new JPanel();
    panD.setBorder(BorderFactory.createTitledBorder(strings.translate("Results")));
    JPanel pan0 = new JPanel();
    JPanel pan1 = new JPanel();
    JPanel pan2 = new JPanel();
    JPanel pan3 = new JPanel();
    JPanel pan4 = new JPanel();
    JPanel pan5 = new JPanel();
    JPanel pan6 = new JPanel();
    JPanel pan7 = new JPanel();
    JPanel pan8 = new JPanel();
    JPanel pan9 = new JPanel();
    JPanel pan10 = new JPanel();
    JPanel pan11 = new JPanel();
    JPanel pan12 = new JPanel();
    JPanel pan13 = new JPanel();
    JPanel pan14 = new JPanel();
    JPanel pan15 = new JPanel();
    JLabel lab1 = new JLabel(strings.translate("Window")+": ");
    JLabel lab2 = new JLabel(strings.translate("Component")+": ");
    JLabel lab3 = new JLabel(strings.translate("Action")+": ");
    JLabel lab4 = new JLabel(strings.translate("Parameter")+": ");
    JLabel lab5 = new JLabel(strings.translate("Application Map")+": ");
    JLabel lab6 = new JLabel(strings.translate("Step File")+": ");
    lab7 = new JLabel(BLANKSPACE);
    JLabel lab8 = new JLabel(strings.translate("Driver")+": ");
    JLabel lab9 = new JLabel("  "+strings.translate("Timeout(sec)")+": ");
    JLabel lab10 = new JLabel(strings.translate("Status Code")+": ");
    JLabel lab11 = new JLabel(strings.translate("Section")+": ");
    JLabel lab12 = new JLabel(strings.translate("Item")+": ");
    JLabel lab13 = new JLabel(strings.translate("Item Value")+": ");
    JLabel lab14 = new JLabel(strings.translate("Input Record")+": ");
    JLabel lab15 = new JLabel("  "+strings.translate("Delim")+": ");
    JLabel lab16 = new JLabel(strings.translate("Test Log Msg")+": ");
    window = new JTextField();
    component = new JTextField();
    map = new JTextField();
    section = new JTextField();
    item = new JTextField();
    appMapItem = new JTextField();
    action = new JTextField("Click");
    param = new JTextField();
    stepFile = new JTextField();
    driver = new JTextField(org.safs.STAFHelper.SAFS_ROBOTJ_PROCESS);
    timeout = new JTextField("300");
    delim = new JTextField(",");
    statusCode = new JTextField();
    testLogMsg = new JTextField();
    inputRecord = new JTextField();
    window.setColumns(28);
    component.setColumns(28);
    map.setColumns(28);
    section.setColumns(28);
    item.setColumns(28);
    appMapItem.setColumns(28);
    action.setColumns(28);
    param.setColumns(28);
    stepFile.setColumns(28);
    driver.setColumns(28);
    timeout.setColumns(28);
    delim.setColumns(28);
    statusCode.setColumns(28);
    testLogMsg.setColumns(28);
    inputRecord.setColumns(28);
    loadIniValues();
    JButton b1 = new JButton(strings.translate("Submit"));
    JButton b2 = new JButton(strings.translate("OpenMap"));
    JButton b3 = new JButton(strings.translate("Clear"));
    JButton b4 = new JButton(strings.translate("Exit"));
    JButton b5 = new JButton(strings.translate("Submit Step File"));
    JButton b6 = new JButton(strings.translate("Get Item"));
    JButton b7 = new JButton(strings.translate("Shutdown Engine"));
    b1.setActionCommand("Submit");
    b1.addActionListener(this);
    b2.setActionCommand("OpenMap");
    b2.addActionListener(this);
    b3.setActionCommand("Clear");
    b3.addActionListener(this);
    b4.setActionCommand("Exit");
    b4.addActionListener(this);
    b5.setActionCommand("StepFile");
    b5.addActionListener(this);
    b6.setActionCommand("GetItem");
    b6.addActionListener(this);
    b7.setActionCommand("Shutdown");
    b7.addActionListener(this);
    panA.setLayout(new BoxLayout(panA, BoxLayout.Y_AXIS));
    panB.setLayout(new BoxLayout(panB, BoxLayout.Y_AXIS));
    panC.setLayout(new BoxLayout(panC, BoxLayout.Y_AXIS));
    panD.setLayout(new BoxLayout(panD, BoxLayout.Y_AXIS));
    pan0.setLayout(new BoxLayout(pan0, BoxLayout.X_AXIS));
    pan1.setLayout(new BoxLayout(pan1, BoxLayout.X_AXIS));
    pan2.setLayout(new BoxLayout(pan2, BoxLayout.X_AXIS));
    pan3.setLayout(new BoxLayout(pan3, BoxLayout.X_AXIS));
    pan4.setLayout(new BoxLayout(pan4, BoxLayout.X_AXIS));
    pan5.setLayout(new BoxLayout(pan5, BoxLayout.X_AXIS));
    pan6.setLayout(new BoxLayout(pan6, BoxLayout.X_AXIS));
    pan7.setLayout(new BoxLayout(pan7, BoxLayout.X_AXIS));
    pan8.setLayout(new BoxLayout(pan8, BoxLayout.X_AXIS));
    pan9.setLayout(new BoxLayout(pan9, BoxLayout.X_AXIS));
    pan10.setLayout(new BoxLayout(pan10, BoxLayout.X_AXIS));
    pan11.setLayout(new BoxLayout(pan11, BoxLayout.X_AXIS));
    pan12.setLayout(new BoxLayout(pan12, BoxLayout.X_AXIS));
    pan13.setLayout(new BoxLayout(pan13, BoxLayout.X_AXIS));
    pan14.setLayout(new BoxLayout(pan14, BoxLayout.X_AXIS));
    pan15.setLayout(new BoxLayout(pan15, BoxLayout.X_AXIS));
    pan0.add(lab8, CENTER);
    pan0.add(driver, CENTER);
    pan0.add(lab9, CENTER);
    pan0.add(timeout, CENTER);
    pan0.add(lab15, CENTER);
    pan0.add(delim, CENTER);
    pan1.add(lab1, CENTER);
    pan1.add(window, CENTER);
    pan2.add(lab2, CENTER);
    pan2.add(component, CENTER);
    pan3.add(lab3, CENTER);
    pan3.add(action, CENTER);
    pan4.add(lab4, CENTER);
    pan4.add(param, CENTER);
    pan5.add(lab5, CENTER);
    pan5.add(map, CENTER);
    pan6.add(b1, CENTER);
    pan6.add(b3, CENTER);
    pan7.add(lab6, CENTER);
    pan7.add(stepFile, CENTER);
    pan7.add(b5, CENTER);
    pan9.add(lab10, CENTER);
    pan9.add(statusCode, CENTER);
    pan10.add(lab11, CENTER);
    pan10.add(section, CENTER);
    pan11.add(lab12, CENTER);
    pan11.add(item, CENTER);
    pan12.add(b2, CENTER);
    pan12.add(b6, CENTER);
    pan13.add(lab13, CENTER);
    pan13.add(appMapItem, CENTER);
    pan14.add(lab14, CENTER);
    pan14.add(inputRecord, CENTER);
    pan15.add(lab16, CENTER);
    pan15.add(testLogMsg, CENTER);
    top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
    top.add(pan0);
    top.add(panA);
    top.add(panB);
    top.add(panC);
    top.add(panD);
    top.add(pan8);
    pan8.add(lab7, CENTER);
    pan8.add(b4, CENTER);
    pan8.add(b7, CENTER);
    panA.add(pan1);
    panA.add(pan2);
    panA.add(pan3);
    panA.add(pan4);
    panA.add(pan6);
    panB.add(pan7);
    panC.add(pan5);
    panC.add(pan10);
    panC.add(pan11);
    panC.add(pan12);
    panD.add(pan9);
    panD.add(pan14);
    panD.add(pan13);
    panD.add(pan15);
  }

  /** starts up the frame and adds the 'top' component to it;
   ** also starts up the observable thread (ControlObservable)
   **/
  public void start () {
    frame = new JFrame(strings.translate("ControlStepUI"));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          saveIniValues();
          //notifyOurObserver("Exit");
          System.exit(1); // for the impatient user, otherwise would have to wait for cmd
        }
      });
    frame.getContentPane().add(top, CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  /** pass along to 'controlObservable'**/
  public void addObserver(Observer observer) {
    controlObservable.addObserver(observer);
  }

  /** show an information popup dialog using JOptionPane
   ** @param msg, String
   **/
  public void popupMessage (String msg) {
    JOptionPane.showMessageDialog(frame, msg,
                                  msg, JOptionPane.INFORMATION_MESSAGE);
  }

  /** processing complete will be called by ControlObservable class run thread **/
  public void processingComplete (boolean pc) {
    if (lab7 != null) {
      if (pc) {
        lab7.setText(BLANKSPACE);
      } else {
        lab7.setText(lab7text + lab7tail);
      }
    }
  }

  /** notify the observers by placing 'cmd' on the 'cmds' queue
   ** @param cmd, String
   **/
  private void notifyOurObserver (ControlParameters cmd) {
    lab7.setText(lab7text + lab7tail);
    controlObservable.addCommand(cmd);
  }
  /** notify the observers of the event.getActionCommand()
   ** with the params
   **/
  public void actionPerformed(ActionEvent event) {
    ControlParameters buf = new ControlParameters();
    buf.setCommand(event.getActionCommand());
    if (event.getActionCommand().equalsIgnoreCase("Shutdown")) {
      if (!controlObservable.isEmpty()) {popupMessage(BUSY); return;}
      ControlParameters buf2 = new ControlParameters();
      buf2.setCommand("driver");
      buf2.addParam(driver.getText());
      buf2.addParam(timeout.getText());
      buf2.addParam(delim.getText());
      notifyOurObserver(buf2);
    } else if (event.getActionCommand().equalsIgnoreCase("Submit")) {
      if (!controlObservable.isEmpty()) {popupMessage(BUSY); return;}
      ControlParameters buf2 = new ControlParameters();
      buf2.setCommand("driver");
      buf2.addParam(driver.getText());
      buf2.addParam(timeout.getText());
      buf2.addParam(delim.getText());
      notifyOurObserver(buf2);
      buf.addParam(window.getText());
      buf.addParam(component.getText());
      buf.addParam(action.getText());
      buf.addParam(param.getText());
    } else if (event.getActionCommand().equalsIgnoreCase("OpenMap")) {
      if (!controlObservable.isEmpty()) {popupMessage(BUSY); return;}
      buf.addParam(map.getText());
    } else if (event.getActionCommand().equalsIgnoreCase("GetItem")) {
      if (!controlObservable.isEmpty()) {popupMessage(BUSY); return;}
      buf.addParam(map.getText());
      buf.addParam(section.getText());
      buf.addParam(item.getText());
    } else if (event.getActionCommand().equalsIgnoreCase("StepFile")) {
      if (!controlObservable.isEmpty()) {popupMessage(BUSY); return;}
      ControlParameters buf2 = new ControlParameters();
      buf2.setCommand("driver");
      buf2.addParam(driver.getText());
      buf2.addParam(timeout.getText());
      buf2.addParam(delim.getText());
      notifyOurObserver(buf2);
      buf.addParam(stepFile.getText());
    } else if (event.getActionCommand().equalsIgnoreCase("Clear")) {
      window.setText("");
      component.setText("");
      action.setText("");
      param.setText("");
      map.setText("");
      section.setText("");
      item.setText("");
      stepFile.setText("");
      statusCode.setText("");
      testLogMsg.setText("");
      appMapItem.setText("");
      return;
    } else if (event.getActionCommand().equalsIgnoreCase("Exit")) {
      if (!controlObservable.isEmpty()) {popupMessage(BUSY); return;}
      saveIniValues();
    } 
    notifyOurObserver(buf);
  }

  private String loadIniFile = "c:/.ControlStepUI.ini";
  /** load initial values into text boxes **/
  private void loadIniValues () {
    try {
      Iterator c = StringUtils.readfile(loadIniFile).iterator();
      window.setText((String)c.next());
      component.setText((String)c.next());
      action.setText((String)c.next());
      param.setText((String)c.next());
      map.setText((String)c.next());
      stepFile.setText((String)c.next());
      driver.setText((String)c.next());
      timeout.setText((String)c.next());
      section.setText((String)c.next());
      item.setText((String)c.next());
    } catch (Exception ex) { // ignore
    }
  }
  /** save initial values of text boxes **/
  private void saveIniValues () {
    try {
      Collection c = new ArrayList();
      c.add(window.getText());
      c.add(component.getText());
      c.add(action.getText());
      c.add(param.getText());
      c.add(map.getText());
      c.add(stepFile.getText());
      c.add(driver.getText());
      c.add(timeout.getText());
      c.add(section.getText());
      c.add(item.getText());
      StringUtils.writefile(loadIniFile, c);
    } catch (Exception ex) { // ignore
    }
  }
}

