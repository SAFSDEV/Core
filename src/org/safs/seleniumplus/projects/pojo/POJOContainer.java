package org.safs.seleniumplus.projects.pojo;

import java.io.File;

public class POJOContainer {
	File file;

	public POJOContainer() {

	}

	public POJOContainer(File file) {
		this.file = file;
	}

	public POJOFile getFile(POJOPath path) {
		File tempFile = new File(file, path.toString());
		return new POJOFile(tempFile);
	}
}
