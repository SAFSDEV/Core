/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

/**
 * Instances of this class are intended to provide instances of LocalAgent objects that 
 * are appropriate for the requester and the testobject in question.
 * <p>
 * Ex: If we provide a java.awt.Component testobject we want to receive the LocalAgent 
 * that knows how to deal with this Component.  Subclasses can provide alternate LocalAgents 
 * that are specific for the tool (like Abbot) that is going to be playing with the 
 * testobject.
 * 
 * @author Carl Nagle
 * @since Feb 17, 2005
 */
public interface LocalAgentFactory {

	/**
	 * 'org.safs.jvmagent' 
	 * Default package used to instantiate local agent helpers.
	 * Ex: A ComponentAgent with this default package would be 'org.safs.jvmagent.ComponentAgent'
	 */
	public static final String DEFAULT_AGENT_PACKAGE = "org.safs.jvmagent.agents";
	
	/**
	 * 'ComponentAgent' 
	 * Default agent name used to instantiate local agent helpers.
	 * Ex: A ComponentAgent with this default package would be 'org.safs.jvmagent.ComponentAgent'
	 */
	public static final String DEFAULT_AGENT_NAME = "ComponentAgent";
	
	/**
	 * Override the DEFAULT_AGENT_PACKAGE setting for instantiating local agent helpers.
	 */
	public void setAgentPackage(String agentpackage);
	
	/**
	 * Override the DEFAULT_AGENT_NAME setting for instantiating the default agent helper.
	 */
	public void setDefaultAgentName(String agentname);
	
	/**
	 * Locate and return the LocalAgent using the stored agentpackage and provided agentname.
	 * Ex: If agentname = "ComponentAgent" and stored agentpackage = "org.safs.jvmagent" 
	 * then we will attempt to return "org.safs.jvmagent.ComponentAgent".
	 * @return LocalAgent 
	 * @throws InvalidAgentException if agent cannot be found/created/returned.
	 */
	public LocalAgent getAgent(String agentname) throws InvalidAgentException;

	/**
	 * Locate and return the LocalAgent most appropriate for the testobject target.
	 * For example, if the testobject is a Swing JFrame we would return the LocalAgent 
	 * from the current agent package that knows how to handle JFrames.
	 * @return LocalAgent
	 * @throws InvalidAgentException if agent cannot be found/created/returned.
	 */
	public LocalAgent getDerivedAgent(Object testobject) throws InvalidAgentException;

	/**
	 * Return the default LocalAgent for the current agent package.
	 * This is usually an agent with reduced capability like an ObjectAgent or a 
	 * ComponentAgent with no ability to deal with more complex testobjects.
	 * @return LocalAgent
	 * @throws InvalidAgentException if agent cannot be found/created/returned.
	 */
	public LocalAgent getDefaultAgent() throws InvalidAgentException;

	/**
	 * Return the LocalAgent for the fully specified agentclass.
	 * @param fullclassname the full proper class name for the desired LocalAgent.  The 
	 * internal agent package is ignored.
	 * @return LocalAgent
	 * @throws InvalidAgentException if agent cannot be found/created/returned.
	 */
	public LocalAgent getLocalAgent(String fullclassname) throws InvalidAgentException;
}
