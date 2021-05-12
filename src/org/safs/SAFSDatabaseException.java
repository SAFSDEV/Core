/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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

@SuppressWarnings("serial")
public class SAFSDatabaseException extends SAFSException {

	public SAFSDatabaseException (String message){ super(message);}
	public SAFSDatabaseException (Object obj, String message){ super(obj, message);}
	public SAFSDatabaseException (Object obj, String method, String message){ super(obj, method, message);}

	public SAFSDatabaseException (Throwable th) { super(th); }
	public SAFSDatabaseException (String message, Throwable th) { super(message, th); }

}

