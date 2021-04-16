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
package org.safs;

import java.util.ArrayList;
import java.util.List;

/**
 * <br><em>Purpose:</em> generic tree object
 *
 **/
public class Arbre<T> implements java.io.Serializable {
	private static final long serialVersionUID = 1554972744112182888L;

	protected T userObject;
	public T getUserObject () {return userObject;}
	public void setUserObject (T userObject) {this.userObject = userObject;}

	protected Arbre<T> parent = null;
	public Arbre<T> getParent() {
		return parent;
	}
	public void setParent(Arbre<T> parent) {
		this.parent = parent;
	}

	protected List<Arbre<T>> children = new ArrayList<Arbre<T>>();
	public List<Arbre<T>> getChildren() {
		return children;
	}
	public void addChild(Arbre<T> child){
		child.setParent(this);
		children.add(child);
	}

	public Arbre() {
	}

}
