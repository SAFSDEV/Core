package org.safs.projects.common.projects.pojo;

/**
 * This class is meant to be a POJO implementation of a Path that is similar to the Eclipse
 * IPath.
 * 
 * For projects that use something like Eclipse, they are expected to use a subclass
 * that will hold something like Eclipse's IPath and delegate calls to it.
 *
 */
public class POJOPath {
	private String pathStr;

	/**
	 * This constructor will most likely be used from a subclass that will hold
	 * something like Eclipse's IPath.
	 */
	protected POJOPath() {

	}

	/**
	 * This constructor will be used with a project that does not use Eclipse.
	 * @param pathStr
	 */
	public POJOPath(String pathStr) {
		this.pathStr = pathStr;
	}

	@Override
	public String toString() {
		return pathStr;
	}

}
