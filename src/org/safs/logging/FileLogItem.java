package org.safs.logging;

import java.io.*;
import org.safs.tools.CaseInsensitiveFile; 

/**
 * This class is an abstract representation of SAFS log files. In addition to
 * attributes common to all <code>LogItem</code>s, <code>FileLogItem</code>
 * supports two more attributes: file spec and parent directory. File spec is 
 * the name (with or without path) of the file. It can be specified in absolute 
 * or relative form. When a relative path is specified, it is used together with 
 * parent directory to determine the full path of the file. If file spec is
 * absolute, the parent directory is ignored.
 */
public abstract class FileLogItem extends LogItem 
{
	private String fileSpec = "";
	private String parentDir = "";

	/**
	 * Creates a <code>FileLogItem</code>.
	 * <p>
	 * @param name		the name of this log item. If <code>null</code>, it is
	 * 					set to the file name.
	 * @param mode		the type of this log (<code>LOGMODE</code> constant
	 * 					defined by <code>AbstractLogFacility</code>).
	 * @param level		the log level for this log.
	 * @param enabled	<code>true</code> to enable this log; <code>false</code>
	 * 					to disable.
	 * @param parent	the parent directory of this log.
	 * @param file		the file spec of this log.
	 */
	public FileLogItem(String name, long mode, int level, boolean enabled, 
		String parent, String file)
	{
		super(name, mode, level, enabled);
		setParentDir(parent);
		if (file != null && file.length() > 0) 
		{
			fileSpec = file;
			// set name of this log to the file name.
			if (name == null) this.name = (new File(file)).getName();
		}
	}

	/**
	 * Creates a <code>FileLogItem</code> and sets its log level to
	 * <code>LOGLEVEL_INFO</code>.
	 * <p>
	 * @param name		the name of this log item. If <code>null</code>, it is
	 * 					set to the file name.
	 * @param mode		the type of this log (<code>LOGMODE</code> constant
	 * 					defined by <code>AbstractLogFacility</code>).
	 * @param enabled	<code>true</code> to enable this log; <code>false</code>
	 * 					to disable.
	 * @param parent	the parent directory of this log.
	 * @param file		the file spec of this log.
	 */
	public FileLogItem(String name, long mode, boolean enabled, String parent, 
		String file)
	{
		this(name, mode, AbstractLogFacility.LOGLEVEL_INFO, enabled, parent,
			file);
	}

	/**
	 * Creates a disabled <code>FileLogItem</code> with file name as the name, 
	 * and sets its log level to <code>LOGLEVEL_INFO</code>.
	 * <p>
	 * @param mode		the type of this log (<code>LOGMODE</code> constant
	 * 					defined by <code>AbstractLogFacility</code>).
	 * @param parent	the parent directory of this log.
	 * @param file		the file spec of this log.
	 */
	public FileLogItem(long mode, String parent, String file)
	{
		this(null, mode, AbstractLogFacility.LOGLEVEL_INFO, false, parent,
			file);
	}

	/**
	 * Returns the file spec for this log.
	 * <p>
	 * @return	the file spec of this log.
	 */
	public String getFileSpec()
	{
		return fileSpec;
	}

	/**
	 * Tests if the file spec of this log is absolute or relative.
	 * <p>
	 * @return	<code>true</code> if the file spec is absolute; 
	 * 			<code>false</code> if it is relative.
	 */
	public boolean isFileSpecAbsolute()
	{
		return (new CaseInsensitiveFile(fileSpec)).toFile().isAbsolute();
	}

	/**
	 * Returns the parent directory for this log.
	 * <p>
	 * @return	the full path to the parent directory.
	 */
	public String getParentDir()
	{
		return parentDir;
	}

	/**
	 * Sets the parent directory for this log.
	 * <p>
	 * @param dir	the new parent directory.
	 */
	public void setParentDir(String dir)
	{
		if( dir == null || dir.length() == 0 ) parentDir = "";
		else parentDir = (new CaseInsensitiveFile(dir)).toFile().getAbsolutePath();
	}

	/**
	 * Returns the full path to the file represented by this log item.
	 * <p>
	 * @return	the full path of this log file.
	 */
	public String getAbsolutePath()
	{
		if ((new CaseInsensitiveFile(fileSpec)).toFile().isAbsolute()) return fileSpec;
		return (new CaseInsensitiveFile(parentDir, fileSpec)).toFile().getAbsolutePath();
	}

	/**
	 * Tests if the file represented by this log item exists.
	 * <p>
	 * @return	<code>true</code> if <code>getAbsolutePath</code> points to an
	 * 			existing file; <code>false</code> otherwise.
	 */
	public boolean fileExists()
	{
		return (new CaseInsensitiveFile(getAbsolutePath())).toFile().isFile();
	}

}