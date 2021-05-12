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
 * @date 2018-03-22    (Lei Wang) Initial release.
 * @date 2018-03-30    (Lei Wang) Added constant 'REST_BASE_PATH'.
 */
package org.safs.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author Lei Wang
 *
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames = {"name", "ip"}) })
public class Machine extends UpdatableDefault<Machine> implements RestModel{
	/** 'machines' the base path to access entity */
	public static final String REST_BASE_PATH = "machines";
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	//The length is 255 by default, and mysql will refuse to create constraint, so set the length explicitly
	//alter table machine add constraint UKl2w0326a9aakpop55m113b4u8 unique (name, ip)	Error Code: 1071. Specified key was too long; max key length is 1000 bytes	0.000 sec
	@Column(length=50)
	private String name;
	@Column(length=15)
	private String ip;
	private String platform;

	//The default constructor is needed by hibernate when querying database.
	public Machine() {
		super();
	}

	/**
	 * @param name
	 * @param ip
	 * @param platform
	 */
	public Machine(String name, String ip, String platform) {
		super();
		this.name = name;
		this.ip = ip;
		this.platform = platform;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the platform
	 */
	public String getPlatform() {
		return platform;
	}

	/**
	 * @param platform the platform to set
	 */
	public void setPlatform(String platform) {
		this.platform = platform;
	}

	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}
}
