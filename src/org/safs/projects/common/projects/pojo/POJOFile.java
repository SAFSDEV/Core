package org.safs.projects.common.projects.pojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is a POJO implementation if a File similar to the Eclipse IFile.
 * This implementation will be used by a project that does not use
 * something like Eclipse.
 * 
 * For projects that use something like Eclipse, they will likely use
 * a subclass that will hold an Eclipse IFile and will delegate calls to it.
 *
 */
public class POJOFile {
	private File file;

	/**
	 * This constructor will likely be used by a subclass that holds something
	 * like Eclipse's IFile and delegates to it.
	 */
	protected POJOFile() {

	}
	
	public POJOFile(File file) {
		this.file = file;
	}
	
	/**
	 * Writes the content of the InputStream to this POJOFile.
	 * 
	 * Subclasses are expected to overwrite this method and call a delegate's
	 * create method (such as Eclipse's IFile).
	 * 
	 * @param inputStream
	 * @param force only used by subclasses that overwrite this method.
	 * @param monitor only used by subclasses that overwrite this method.
	 * @throws Exception
	 */
	public void create(InputStream inputStream, boolean force, Object monitor) throws Exception {
		try {
			OutputStream out = new FileOutputStream(file);
			int b;
			while ((b = inputStream.read()) != -1) {
				out.write(b);
			}
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
