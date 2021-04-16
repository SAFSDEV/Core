/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.slf4j.impl;

import org.safs.logging.slf4j.SAFSLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * Required class implementation for SLF4J to find and use the SAFSLoggerFactory.
 * @author Carl Nagle
 * @see org.slf4j.spi.LoggerFactoryBinder
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
