/* $Id: MenuPath.java,v 1.1.2.2 2014/04/17 02:06:51 lei_wang Exp $ */
package org.safs.model.components;



public class MenuPath {
   
   private static final String PATH_SEPARATOR = "->";
   
   private String _lastMenuName;
   private MenuPath _parentPath;
   
   public MenuPath(String menuName) {
      this((MenuPath)null, menuName);
   }
   
   public MenuPath(String[] menuNames) {
      if (menuNames == null || menuNames.length == 0)
         throw new IllegalArgumentException("menuNames must be non-null and non-zero length.");

      _lastMenuName = menuNames[menuNames.length - 1];
      if (menuNames.length > 1)
         _parentPath = new MenuPath(menuNames, menuNames.length - 1);
   }
   
   protected MenuPath(String[] menuNames, int length) {
      _lastMenuName = menuNames[length - 1];
      if (length > 1)
         _parentPath = new MenuPath(menuNames, length - 1);
   }
   
   public MenuPath(MenuPath parentPath, String menuName) {
      if (menuName == null || menuName.length() == 0)
         throw new IllegalArgumentException("menuName must be non-null and non-zero length.");
         
      _lastMenuName = menuName;
      _parentPath = parentPath;
   }
   
   public MenuPath(String menuName1, String menuName2) {
      this(new String[] {menuName1, menuName2});
   }
   
   
   public MenuPath(String menuName1, String menuName2, String menuName3) {
      this(new String[] {menuName1, menuName2, menuName3});
   }
   
   public final String getLastMenuName() {
      return _lastMenuName;
   }
   
   public final MenuPath getParentPath() {
      return _parentPath;
   }
   
   public final String getPath() {
      StringBuffer buffer = new StringBuffer(_lastMenuName);
      MenuPath parent = _parentPath;
      while (parent != null) {
         buffer.insert(0, MenuPath.PATH_SEPARATOR);
         buffer.insert(0, parent.getLastMenuName());
         parent = parent.getParentPath();
      }
      
      // Quote the string
      buffer.insert(0, "\"");
      buffer.append("\"");
      
      return buffer.toString();
   }
   
   public final MenuPath pathByAddingChild(String menuName) {
      return new MenuPath(this, menuName);
   }
}
