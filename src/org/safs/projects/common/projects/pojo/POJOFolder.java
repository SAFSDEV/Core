package org.safs.projects.common.projects.pojo;

import java.io.File;

public class POJOFolder {
	File dir;

	public POJOFolder() {

	}

	public POJOFolder(File dir) {
		this.dir = dir;
	}
	public boolean exists() {
		return dir.exists();
	}
	public POJOFile getFile(String path) {
		File file = new File(dir, path);
		return new POJOFile(file);
	}

	public POJOContainer getParent() {
		File parentFile = dir.getParentFile();
		return new POJOContainer(parentFile);
	}
}
