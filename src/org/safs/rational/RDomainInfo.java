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
package org.safs.rational;
/**
 * A simple class trying to consolidate info available from a DomainTestObject and some of its
 * child objects.
 */
import com.rational.test.ft.object.interfaces.DomainTestObject;

public class RDomainInfo {
	private DomainTestObject domain = null;
	private String domainname = null;
	private String mailslot = null;
	public RDomainInfo(DomainTestObject adomain){
		domain = adomain;
		domainname = adomain.getName().toString();
		mailslot = adomain.getTestContextReference().getMailslotName();
	}
	public DomainTestObject getDomain() {
		return domain;
	}
	public String getDomainname() {
		return domainname;
	}
	public String getMailslot() {
		return mailslot;
	}
}
