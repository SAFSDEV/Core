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
 * @date 2018-03-23    (Lei Wang) Initial release.
 * @date 2018-04-04    (Lei Wang) Add unique constraint for ("productName", "platform", "track", "branch").
 *                                Change field name 'product_name' to 'productName': use camel format.
 */
package org.safs.data.model;

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
@Table(uniqueConstraints={ @UniqueConstraint(columnNames = {"productName", "platform", "track", "branch"}) })
public class Orderable extends UpdatableDefault<Orderable> implements RestModel{
	/** 'orderables' the base path to access entity */
	public static final String REST_BASE_PATH = "orderables";
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String productName;
	private String platform;
	private String track;
	private String branch;
	/**
	 *
	 */
	public Orderable() {
		super();
	}
	/**
	 * @param productName
	 * @param platform
	 * @param track
	 * @param branch
	 */
	public Orderable(String productName, String platform, String track, String branch) {
		super();
		this.productName = productName;
		this.platform = platform;
		this.track = track;
		this.branch = branch;
	}
	/**
	 * @return the id
	 */
	@Override
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
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}
	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
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
	/**
	 * @return the track
	 */
	public String getTrack() {
		return track;
	}
	/**
	 * @param track the track to set
	 */
	public void setTrack(String track) {
		this.track = track;
	}
	/**
	 * @return the branch
	 */
	public String getBranch() {
		return branch;
	}
	/**
	 * @param branch the branch to set
	 */
	public void setBranch(String branch) {
		this.branch = branch;
	}
	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}
}

