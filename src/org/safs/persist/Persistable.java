/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 02, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.util.Map;
import java.util.Set;

/**
 * Represent an object that can be persisted.
 * @author Lei Wang
 */
public interface Persistable {

	/**
	 * @return Map<String, Object>, a Map containing a pair(persistKey, value) to persist.
	 */
	public Map<String /*persistKey*/, Object/*content*/> getContents();

	/**
	 * This Map contains a pair, the key is the 'class field name' telling us which field needs
	 * to be persisted; the value is a 'unique string' representing this class field in the
	 * persistence. The 'class field name' and the 'persist key' can be same or different.<br>
	 * When persisting a class, not all the fields need to be persisted; Only those fields
	 * which need to be persisted will be put into this Map.<br>
	 *
	 * @return Map<String, String>, a Map containing a pair(fieldName, persistKey) to persist.
	 */
	public Map<String/*fieldName*/, String/*persistKey*/> getPersitableFields();

	/**
	 * Tell us if this object will be persisted or not.
	 * @return boolean, if true, this object will be persisted; otherwise, will not.
	 */
	public boolean isEnabled();

	/**
	 * Sometimes, we don't want to persist an object, we disable it.
	 * @param enabled boolean, if true, this object will be persisted; otherwise, will not.
	 */
	public void setEnabled(boolean enabled);

	public void setParent(Persistable parent);

	public Persistable getParent();

	/**
	 * "CONTAINER_ELEMENT", The general key name representing the container element.
	 * The the container element is like <b>Response</b> in XML
	 * <pre>
	 * &lt;Response&gt;
	 *   &lt;StatusCode&gt;200&lt;/StatusCode&gt;
	 * &lt;/Response&gt;
	 * </pre>
	 */
	public static final String CONTAINER_ELEMENT = "CONTAINER_ELEMENT";

	/**
	 * A Persistable object can contain other Persistable objects, it is an hierarchical structure.
	 * This flat-key is supposed to represent each Persistable object uniquely.<br/>
	 *
	 * @return String, the flat key representing this Persistable object.<br/>
	 * @see #getContents(Map, Set)
	 */
	public String getFlatKey();

	/**
	 * Turn the Persistable hierarchical contents Map ({@link #getContents()}) into a flat-key Map.
	 *
	 * @param elementAlternativeValues Map<String,String>,<b>in</b>, contains pairs(elementName, alternativeValue), which
	 * will be used to set the value for a certain element.
	 *
	 * @param ignoredFields Set<String>, <b>out</b>, it will hold the fields (of Persistable and its children) to be ignored.
	 * <b>This Set should be initialized an provided from outside.</b>
	 * The field is expressed as a flat key<br/>
	 * <pre>
	 * If this Set contains a field 'Response.Request', then all its children will
	 * be ignored, such as:
	 * Response.Request.Headers
	 * Response.Request.MessageBody
	 * ...
	 * </pre>
	 *
	 * @param includeContainer boolean, <b>in</b>, if the Persistable Object itself should be considered as part of the whole contents.<br/>
	 *                                  For example, if we have a Persistable Response object, it contains some fields as its contents, these
	 *                                  fields could be Response.Headers, Response.ID etc. But does "Response" itself should be put into
	 *                                  the contents Map as a key? The answer depends on the needs.
	 *
	 * @return Map<String, Object>, the actual contents of Persistable and those of its children.
	 * They are pairs of (flatKey, content) such as
	 * <pre>
	 * (Response.ID, "FFE3543545JLFS")
	 * (Response.Headers, "{Date=Tue, 06 DEC 2016 03:08:12 GMT}")
	 * (Response.Request.Headers, "{Content-Length=4574, Via=1.1 inetgw38 (squid)}")
	 * </pre>
	 */
	public Map<String/* flatKey */, Object /* content */> getContents(Map<String,String> elementAlternativeValues, Set<String> ignoredFields, boolean includeContainer);

	/**
	 * Set the field's value of this Persistable object.
	 * @param tag String, the tag name or the persist-key. refer to {@link #getPersitableFields()}.
	 * @param value Object, the value to set
	 * @return boolean true if successful.
	 */
	public boolean setField(String tag, Object value);
}
