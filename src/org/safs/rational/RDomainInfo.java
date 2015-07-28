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
