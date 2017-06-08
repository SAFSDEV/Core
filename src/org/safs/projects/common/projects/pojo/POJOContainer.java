package org.safs.projects.common.projects.pojo;

import java.io.File;

/**
 * This is a POJO implementation if a Container similar to the Eclipse IContainer.
 * This implementation will be used by a project that does not use
 * something like Eclipse.
 * 
 * For projects that use something like Eclipse, they will likely use
 * a subclass that will hold an Eclipse IContainer and will delegate calls to it.
 *
 */
public class POJOContainer {
	File file;

	/**
	 * This constructor will likely be used by a subclass that holds something
	 * like Eclipse's IContainer and delegates to it.
	 */
	protected POJOContainer() {

	}

	public POJOContainer(File file) {
		this.file = file;
	}

	/**
	 * Gets the file that corresponds to a path relative to the location
	 * of this POJOContainer.
	 * 
	 * Subclasses are expected to overwrite this method and call a delegate's
	 * getFile method (such as Eclipse's IContainer).
	 * 
	 * @param path a relative path under this POJOContainer.
	 * @return the POJOFile that corresponds to the relative path.
	 */
	public POJOFile getFile(POJOPath path) {
		File tempFile = new File(file, path.toString());
		return new POJOFile(tempFile);
	}
}
