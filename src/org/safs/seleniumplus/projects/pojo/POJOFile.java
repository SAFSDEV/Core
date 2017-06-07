package org.safs.seleniumplus.projects.pojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class POJOFile {
	private File file;

	public POJOFile() {

	}
	public POJOFile(File file) {
		this.file = file;
	}
	public void create(InputStream testclassstream, boolean force, Object monitor) throws Exception {
		try {
			OutputStream out = new FileOutputStream(file);
			int b;
			while ((b = testclassstream.read()) != -1) {
				out.write(b);
			}
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
