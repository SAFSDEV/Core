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
package org.safs.projects.common.projects.callbacks;

import org.safs.projects.common.projects.pojo.POJOProject;

/**
 * This callback is expected to be used by projects such as SeleniumPlus.
 * The idea is that SeleniumPlus code will create this callback.
 * Then, at a later time, Core code will call createProject.
 * The SeleniumPlus code that implements createProjects will call the Eclipse API to
 * create the project.
 *
 */
public abstract class CreateProjectCallback {
	public abstract POJOProject createProject();
}
