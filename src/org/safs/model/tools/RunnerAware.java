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
package org.safs.model.tools;

/**
 * Classes implementing this interface that get automatically configured by a 
 * Runner should automatically receive an instance of the Runner at runtime.
 * @author Carl Nagle
 * @see org.safs.model.annotations.AutoConfigureJSAFS
 * @see Runner#autorun(String[])
 * @see org.safs.model.annotations.Utilities#autoConfigure(String, org.safs.model.annotations.JSAFSConfiguredClassStore)
 */
public interface RunnerAware {
	public abstract void setRunner(Runner runner);
}
