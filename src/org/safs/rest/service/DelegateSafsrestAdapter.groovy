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
		//give a void implementation to avoid the build failure.
	}
}
