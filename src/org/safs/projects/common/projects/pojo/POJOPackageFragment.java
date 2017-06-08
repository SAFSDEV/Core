package org.safs.projects.common.projects.pojo;

/**
 * This is a POJO implementation if a PackageFragment similar to the Eclipse PackageFragment.
 * This implementation will be used by a project that does not use
 * something like Eclipse.
 * 
 * For projects that use something like Eclipse, they will likely use
 * a subclass that will hold an Eclipse PackageFragment and will delegate calls to it.
 *
 */
public class POJOPackageFragment {
	private String elementName;

	/**
	 * This constructor will likely be used by a subclass that holds something
	 * like Eclipse's PackageFragment and delegates to it.
	 */
	protected POJOPackageFragment() {

	}

	public POJOPackageFragment(String elementName) {
		this.elementName = elementName;
	}

	/**
	 * Gets the element name of this POJOPackageFragment.
	 * 
	 * Subclasses are expected to overwrite this method and call a delegate's
	 * getElementName method (such as Eclipse's PackageFragment).
	 * 
	 * @return the element name
	 */
	public String getElementName() {
		return elementName;
	}
}
