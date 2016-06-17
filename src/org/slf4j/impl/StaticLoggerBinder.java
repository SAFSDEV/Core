/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.slf4j.impl;

import org.safs.logging.slf4j.SAFSLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * @author canagl
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

	private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
	private final SAFSLoggerFactory factory;
	
	private StaticLoggerBinder() {
		factory = new SAFSLoggerFactory();
	}

	public static final StaticLoggerBinder getSingleton(){
		return SINGLETON;
	}
	
	@Override
	public ILoggerFactory getLoggerFactory() {
		return factory;
	}

	@Override
	public String getLoggerFactoryClassStr() {
		return SAFSLoggerFactory.class.getName();
	}

}
