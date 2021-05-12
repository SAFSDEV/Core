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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-03-30    (Lei Wang) Initial release.
 */
package org.safs.data.model;

/**
 * @author Lei Wang
 *
 */
public class ConstantPath {
	/** 'chart' */
	public static final String CHART = "chart";
	/** '/chart' */
	public static final String CHART_WITH_SEPARATOR = "/"+CHART;

	/** 'table' */
	public static final String TABLE = "table";
	/** '/table' */
	public static final String TABLE_WITH_SEPARATOR = "/"+TABLE;

	/** 'statistics' */
	public static final String STATISTIC = "statistics";
	/** '/statistics' */
	public static final String STATISTIC_WITH_SEPARATOR = "/"+STATISTIC;

	/** 'error' path for error page */
	public static final String ERROR = "error";
	/** '/error' path for error page */
	public static final String ERROR_WITH_SEPARATOR = "/"+ERROR;

}
