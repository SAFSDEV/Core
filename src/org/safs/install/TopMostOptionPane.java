/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * MAY 05, 2016    (SBJLWA) Initial release.
 */
package org.safs.install;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * <p>
 * Show the dialog on top most level, which means it will be shown before other windows.
 * To achieve this goal, we tried to set the dialog's parent on the top most.
 * If the provided parent is null, then we create a JFrame object as parent 
 * and bring it to top most before showing dialog; after dialog is closed, we dispose the JFrame.
 * If the provided parent is not null, we will bring it to top most and set it to visible
 * before showing dialog; after dialog is closed, we set back the parent's status.
 * </p>
 * Current, this class supports the following methods:
 * <ul>
 * <li>showOptionDialog
 * <li>showInputDialog
 * <li>showConfirmDialog
 * </ul>
 * To implement other showXXXDialog() static method of JOptionPane, please refer to 
 * <ul>
 * <li>{@link #showInputDialog(Component, Object, String, int, Icon, Object[], Object)}
 * <li>{@link #showOptionDialog(Component, Object, String, int, int, Icon, Object[], Object)}
 * </ul>
 * @author sbjlwa
 */
public class TopMostOptionPane{
	
	private static final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	
	/**
	 * The dialog's Component parent object.
	 * If it is an instance of Window, it will also be kept in the class field 'window'.
	 */
	private Component parent = null;
	/**
	 * A convenient reference to field 'parent', not all Component is Window.
	 * This field keep its Window reference if the 'parent' Component is Window.
	 */
	private Window window = null;
	/**
	 * Keep the 'alwaysOnTop' original status of the dialog's parent.
	 */
	private boolean originalAlwaysOnTop = false;
	/**
	 * Keep the 'visible' original status of the dialog's parent.
	 */
	private boolean originalVisible = false;
	/**
	 * If the dialog's parent is created automatically.
	 */
	private boolean weCreatedParent = false;
	
	private TopMostOptionPane(Component parent){
		bringDialogParentToTop(parent);
	}
	
	/**
	 * Bring the dialog's parent to the top most level.
	 * @param parent
	 */
	private void bringDialogParentToTop(Component parent){
		if(parent==null){
			//Use a JFrame as dialog's parent, set this JFrame to top most.
			window = new JFrame();
			this.parent = window;
			window.setAlwaysOnTop(true);
			window.setVisible(true);
			window.setLocation(screen.width/2, screen.height/2);
			weCreatedParent = true;
			
		}else{
			this.parent = parent;

			if(parent instanceof Window){
				window = (Window) parent;
				originalAlwaysOnTop = window.isAlwaysOnTop();
				originalVisible = window.isVisible();
				window.setAlwaysOnTop(true);
				window.setVisible(true);
			}else{
				//TODO, don't know how to get the dialog to the top.
			}
		}
	}
	
	/**Set the 'visible' of dialog's parent window to original value */
	private void resetVisible(){
		if(window!=null) window.setVisible(originalVisible);
	}
	/**Set the 'alwaysOnTop' of dialog's parent window to original value */
	private void resetAlwaysOnTop(){
		if(window!=null) window.setAlwaysOnTop(originalAlwaysOnTop);
	}

	/**
	 * This should be called after the dialog is closed, For example after calling JOptionPane#showOptionDialog().<br>
	 * It will close the parent window if it is created automatically.<br>
	 * It will set the parent's status to original if the parent is a Window object.<br>
	 * It will do nothing if the parent object is not instance of Window.<br>
	 */
	public void cleanup(){
		if(window!=null){
			if(weCreatedParent){
				window.dispose();
			}else{
				resetAlwaysOnTop();
				resetVisible();
			}
		}
	}
	
	/**
	 * Show the Input Dialog on top most.<br>
	 * @see JOptionPane#showInputDialog(Component, Object, String, int, Icon, Object[], Object)
	 */
    public static Object showInputDialog(Component parentComponent,
            Object message, String title, int messageType, Icon icon,
            Object[] selectionValues, Object initialSelectionValue)
            throws HeadlessException {
    	Object result = null;
		
		TopMostOptionPane topPane = new TopMostOptionPane(parentComponent);
		result = JOptionPane.showInputDialog(topPane.parent, message, title, messageType, icon, selectionValues, initialSelectionValue);
		topPane.cleanup();
		
		return result;
    }
	
	/**
	 * Show the Option Dialog on top most.<br>
	 * @see JOptionPane#showOptionDialog(Component, Object, String, int, int, Icon, Object[], Object)
	 */
	public static int showOptionDialog(Component parentComponent,
			Object message, String title, int optionType, int messageType,
			Icon icon, Object[] options, Object initialValue)
					throws HeadlessException {
		int result = JOptionPane.CANCEL_OPTION;
		
		TopMostOptionPane topPane = new TopMostOptionPane(parentComponent);
		result = JOptionPane.showOptionDialog(topPane.parent, message, title, optionType, messageType, icon, options, initialValue);
		topPane.cleanup();
		
		return result;
	}
	
	/**
	 * @see #showOptionDialog(Component, Object, String, int, int, Icon, Object[], Object)
	 */
	public static int showConfirmDialog(Component parentComponent,
			Object message, String title, int optionType)
					throws HeadlessException {
		return showConfirmDialog(parentComponent, message, title, optionType,
				JOptionPane.QUESTION_MESSAGE);
	}
	
	/**
	 * @see #showOptionDialog(Component, Object, String, int, int, Icon, Object[], Object)
	 */
	public static int showConfirmDialog(Component parentComponent,
			Object message, String title, int optionType, int messageType)
					throws HeadlessException {
		return showConfirmDialog(parentComponent, message, title, optionType,
				messageType, null);
	}

	/**
	 * @see #showOptionDialog(Component, Object, String, int, int, Icon, Object[], Object)
	 */
	public static int showConfirmDialog(Component parentComponent,
			Object message, String title, int optionType,
			int messageType, Icon icon) throws HeadlessException {
		return showOptionDialog(parentComponent, message, title, optionType,
				messageType, icon, null, null);
	}
	
	/**
	 * Test.
	 * Directly change the JDialog's behavior to show on top. 
	 */
	private static void test_bring_dialog_to_top(){
		String msg = "Hi loooo  ... ";
		String title = "Testing Dialog shown as top most.";
		JOptionPane opane = new JOptionPane(msg, JOptionPane.CLOSED_OPTION);
		JDialog dialog = opane.createDialog(title);
		boolean originalAlwaysOnTop = dialog.isAlwaysOnTop();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		dialog.setAlwaysOnTop(originalAlwaysOnTop);
		dialog.dispose();
	}
	
	/**
	 * Test
	 * <ul>
	 * <li>{@link #showConfirmDialog(Component, Object, String, int)}
	 * <li>{@link #showOptionDialog(Component, Object, String, int, int, Icon, Object[], Object)}
	 * </ul>
	 */
	private static void test_bring_parent_to_top(){
		String msg = "Hi loooo  ... ";
		String title = "Testing Dialog shown as top most.";
		
		//The dialog will be shown at the center of the screen
		TopMostOptionPane.showConfirmDialog(null, msg, title, JOptionPane.CLOSED_OPTION);

		//Even the parent dialog is not visible, the dialog will also be shown on top most.
		JFrame parent = new JFrame();
		parent.setVisible(false);
		TopMostOptionPane.showOptionDialog(parent, msg, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		parent.dispose();
	}
	
	public static void main(String[] args){
		test_bring_dialog_to_top();
		
		test_bring_parent_to_top();
	}
}
