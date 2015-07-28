/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

import java.util.Hashtable;

/**
 * 
 * @author canagl
 * @since Feb 17, 2005
 */
public class LocalAgentFactoryImpl implements LocalAgentFactory {

	private String agentpackage = DEFAULT_AGENT_PACKAGE;
	private String defaultagent = DEFAULT_AGENT_NAME;
	
	// critical cache of agents for performance.
	// if agents are not cached performance definitely is noticeably slower
	private Hashtable lagents = new Hashtable(20);
	
	/**
	 * Constructor for LocalAgentFactoryImpl.
	 */
	public LocalAgentFactoryImpl() {
		super();
	}

	/**
	 * Constructor for LocalAgentFactoryImpl.
	 */
	public LocalAgentFactoryImpl(String agentpackage) {
		super();
		setAgentPackage(agentpackage);
	}

	/**
	 * @see org.safs.jvmagent.LocalAgentFactory#setAgentPackage(String)
	 */
	public void setAgentPackage(String agentpackage) {
		this.agentpackage = agentpackage;
	}

	/**
	 * @see org.safs.jvmagent.LocalAgentFactory#setDefaultAgentName(String)
	 */
	public void setDefaultAgentName(String agentname) {
		this.defaultagent = agentname;
	}

	/**
	 * @see org.safs.jvmagent.LocalAgentFactory#getAgent(String)
	 */
	public LocalAgent getAgent(String agentname)  throws InvalidAgentException{
		String agentpkg = agentpackage +"."+ agentname;
		return getLocalAgent(agentpkg);
	}

	/**
	 * @see org.safs.jvmagent.LocalAgentFactory#getDerivedAgent(Object)
	 */
	public LocalAgent getDerivedAgent(Object testobject)  throws InvalidAgentException{
		Class clazz = null;
		String objectclass = null;
		String fullname = null;
		String name = null;
		
		try{ clazz = testobject.getClass();}
		catch(NullPointerException np) 
		{throw new InvalidAgentException(new NullPointerException("getDerivedAgent(null)"));}

		// check for cached agent already associated with this class
		objectclass = clazz.getName();
		Object lagent = lagents.get(objectclass);
		if (lagent instanceof LocalAgent) return (LocalAgent) lagent;
		
		int idot;
		LocalAgent agent = null;		
		do{
			try{
				fullname  = clazz.getName();
				idot = fullname.lastIndexOf('.');
				idot = (idot < 0) ? 0: idot;
				name = fullname.substring(idot+1);				
				agent = getAgent(name+"Agent");// either works or an exception is thrown

				// the cache is critical for performance
				// associates an agent with a specific gui object class
				// this is in addition to storing by agent name
				lagents.put(objectclass, agent);
				return agent;
			}
			catch(InvalidAgentException ia){ /* try next superclass */ }			
			clazz = clazz.getSuperclass(); // may get null			
		}while(clazz instanceof Class);
		
		// did not find a suitable agent 
		throw new InvalidAgentException("Could not locate an agent using:\""+ agentpackage +"\" for object "+ 
		                                 testobject.getClass().getName() + 
		                                 "\nContact SAFSDEV Development or your custom agent developer.");
	}

	/**
	 * By default we attempt to return a DEFAULT_AGENT_NAME instance from agentpackage.
	 * @see org.safs.jvmagent.LocalAgentFactory#getDefaultAgent()
	 */
	public LocalAgent getDefaultAgent()  throws InvalidAgentException{
		return getAgent(defaultagent);
	}

	/**
	 * Return the LocalAgent for the fully specified agentclass.
	 * @param agentclass the full proper class name for the desired LocalAgent.  The 
	 * internal agent package is ignored.
	 * @return LocalAgent
	 * @throws InvalidAgentException if agent cannot be found/created/returned.
	 */
	public LocalAgent getLocalAgent(String fullclassname) throws InvalidAgentException {

		try{
			Object lagent = lagents.get(fullclassname);
			if (lagent instanceof LocalAgent) return (LocalAgent) lagent;}
		catch(NullPointerException np){
			throw new InvalidAgentException(new NullPointerException("getLocalAgent(null)"));}

		Class agentclazz;
		try{ agentclazz = Class.forName(fullclassname);}
		catch(ClassNotFoundException cnf){ throw new InvalidAgentException (cnf);}
		catch(Throwable error){ throw new InvalidAgentException(error);}

		LocalAgent anagent = null;
		try{ anagent = (LocalAgent) agentclazz.newInstance();}
		catch(InstantiationException ie){ throw new InvalidAgentException (ie);}
		catch(IllegalAccessException ia){ throw new InvalidAgentException (ia);}
		
		if(anagent instanceof LocalAgentFactoryUser) 
		    ((LocalAgentFactoryUser)anagent).setLocalAgentFactory(this);

		lagents.put(fullclassname, anagent);
		return anagent;
	}
	
}
