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
