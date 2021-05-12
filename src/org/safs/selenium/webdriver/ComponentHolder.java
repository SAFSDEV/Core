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
package org.safs.selenium.webdriver;

import org.safs.model.Component;

import groovy.lang.GroovyObjectSupport;

public class ComponentHolder extends GroovyObjectSupport {
	private Component component;
	
	public ComponentHolder() {
		
	}
	
	private ComponentHolder(Component component) {
		this.component = component;
	}
	
	public Object propertyMissing(String name) {
		if (component == null) {
			component = new Component(name);
			return this;
		} else {
			Component comp = new Component(component, name);
			return new ComponentHolder(comp);
		}
	}
	
	public Component getComponent() {
		return component;
	}
	
	@Override
	public String toString() {
		return component.getName();
	}

}
