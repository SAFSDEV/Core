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
/** 
 * @author  Carl Nagle
 * @since   NOV 10, 2003
 *   <br>   NOV 10, 2003    (Carl Nagle) Original Release
 *
 **/
package org.safs;

/** 
 * Flags the ommission of proper initialization of a ProcessRequester.
 * 
 * ProcessRequester initialization errors are considered a catastrophic engine failure.
 **/
public class SAFSProcessorInitializationException extends RuntimeException {
	
	public SAFSProcessorInitializationException (String message){ super(message);}
}

