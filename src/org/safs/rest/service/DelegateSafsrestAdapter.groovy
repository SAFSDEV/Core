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
package org.safs.rest.service

import org.apache.hc.client5.http.testframework.HttpClientPOJOAdapter
import org.safs.rest.service.models.consumers.SafsrestAdapter

/**
 *
 * Because this class is instantiated by SAFS Core internally, there is a
 * complication.  SAFS Core requires the adapters be of type
 * org.apache.hc.client5.http.testframework.HttpClientPOJOAdapter which is
 * in the SAFS Core project (temporarily hopefully).
 * However, the SafsrestAdapter is not of that type (it is of type
 * org.apache.hc.core5.testing.framework.ClientPOJOAdapter which is in
 * HttpCore5.  Though these two ClientPOJOAdapter types
 * are in different packages, they have the same API.  So, this
 * class acts as a delegate effectively changing the type.
 *
 * TODO:  This complication can be resolved by changing SAS Core so it uses
 * ClientPOJOAdapter from Apache HTTPCore5 instead of HttpClientPOJOAdapter.
 *
 */
class DelegateSafsrestAdapter extends HttpClientPOJOAdapter {

	@Delegate
	SafsrestAdapter adapter = new SafsrestAdapter()

	public SafsrestAdapter getAdapter() {
		adapter
	}

	@Override
	public String getClientName(){
		//give an implementation to avoid the build failure.
		if(adapter!=null){
			adapter.getClientName();
		}
	}

	@Override
	public Map<String, Object> execute(String defaultURI, Map<String, Object> request) throws Exception{
		//give an implementation to avoid the build failure.
		if(adapter!=null){
			return adapter.execute(defaultURI, request);
		}
	}
}
