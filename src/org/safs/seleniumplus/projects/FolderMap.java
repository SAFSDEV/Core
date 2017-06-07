package org.safs.seleniumplus.projects;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FolderMap {
	private final Map<File, Object> folderMap = new HashMap<File, Object>();

	public Object get(File file) {
		return folderMap.get(file);
	}
	
	public void put(File file, Object folder) {
		folderMap.put(file, folder);
	}
}
