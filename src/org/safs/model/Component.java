/**
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.model;

public class Component {
	
   private String _name;
   private Component _parent;
   
   public Component(String name) {
      super();
      _name = name;
   }

   public Component(Component parent, String name) {
	      this(name);
	      _parent = parent;
	   }

   public String getName() {
      return _name;
   }

   /**
    * @return the parent Component or null if no parent is available.
    */
   public Component getParent() {
	      return _parent;
   }
   
   /**
    * @return the name of the parent or null if no parent is available.
    */
   public String getParentName() {
	      return _parent == null ? null:_parent.getName();
   }
   
   /**
    * Convenience routine to Utilities.quote.
    * @see org.safs.model.Utils#quote(String)
    */
   public static String quote(String val){
   	  return Utils.quote(val);
   }
}
