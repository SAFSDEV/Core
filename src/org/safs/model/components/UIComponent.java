/* $Id: UIComponent.java,v 1.1.2.2 2014/04/17 02:06:51 lei_wang Exp $ */
/* Copyright (c) 2005 by SAS Institute Inc., Cary, NC 27513 */
package org.safs.model.components;

import org.safs.model.Component;


public class UIComponent extends Component {
   private Window _window;

   protected UIComponent(String compname) {
      super(compname);
   }

   public UIComponent(Window window, String compname) {
      this(compname);
      setWindow(window);
   }
   
   public UIComponent(String winname, String compname) {
    this(new Window(winname), compname);
 }
 
   protected void setWindow(Window window){
   	  _window=window;
   }
   
   public Window getWindow() {
      return _window;
   }
}
