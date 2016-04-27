/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs.rest.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and maintains a set of active Service sessions by serviceId.
 * @author canagl
 */
public class Services {
	
	private static Map<String, Service> services = Collections.synchronizedMap(new HashMap<String, Service>());
	
	/**
	 * Normally not called directly unless a Service has been created outside the control of this Collection.
	 * @param service instance with a sessionId that must NOT already exist in this Collection.
	 * @throws IllegalArgumentException if the provided Service is null or has a serviceId already in the Collection.
	 * @see #createService(String)
	 * @see #deleteService(String)
	 */
	public static void addService(Service service) throws IllegalArgumentException{
		if(service == null) throw new IllegalArgumentException("Service instance cannot be null for Services.addService.");
		if(! services.containsKey(service.getServiceId()))
			services.put(service.getServiceId(), service);
		else throw new IllegalArgumentException("service "+ service.getServiceId()+" already exists.");
	}
	
	/**
	 * Create a new Service session using the provided unique serviceId.
	 * @param serviceId
	 * @return Service
	 * @throws IllegalArgumentException if the provided serviceId is null 
	 * or if the Collection already contains a Service with the given serviceId.
	 * @see #deleteService(String)
	 */
	public static Service createService(String serviceId)throws IllegalArgumentException{
		if(serviceId == null) throw new IllegalArgumentException("ServiceId name cannot be null for Services.createService.");
		if(! services.containsKey(serviceId))
			return services.put(serviceId, new Service(serviceId));
		else throw new IllegalArgumentException("Service "+ serviceId+" already exists.");
	}
	
	/**
	 * Get an existing Service session using the provided unique serviceId.
	 * @param serviceId
	 * @return Service
	 * @throws IllegalArgumentException if the provided serviceId is null 
	 * or if the Collection does not contain a Service with the given serviceId.
	 * @see #createService(String)
	 */
	public static Service getService(String serviceId)throws IllegalArgumentException{
		if(serviceId == null) throw new IllegalArgumentException("ServiceId name cannot be null for Services.getService.");
		if(services.containsKey(serviceId))
			return services.get(serviceId);
		else throw new IllegalArgumentException("Service "+ serviceId+" does NOT exist in Services.");
	}
	
	/**
	 * Delete a Service session matching the provided unique serviceId.
	 * @param serviceId
	 * @throws IllegalArgumentException if the provided serviceId is null 
	 * or if the Collection does NOT contain a Service with the given serviceId.
	 * @see #createService(String)
	 */
	public static void deleteService(String serviceId)throws IllegalArgumentException{
		if(serviceId == null) throw new IllegalArgumentException("Service name cannot be null for Services.deleteService.");
		if(services.containsKey(serviceId))
			services.remove(serviceId);
		else throw new IllegalArgumentException("Service "+ serviceId+" does NOT exist in Services.");
	}

	/**
	 * Normally not called directly.
	 * @param service
	 * @throws IllegalArgumentException if the provided Service is null or has a serviceId NOT in the Collection.
	 * @see #createService(String)
	 * @see #deleteService(String)
	 */
	public static void deleteService(Service service) throws IllegalArgumentException{
		if(service == null) throw new IllegalArgumentException("Service instance cannot be null for Services.deleteService.");
		if(services.containsKey(service.getServiceId()))
				services.remove(service.getServiceId());
		else throw new IllegalArgumentException("Service "+ service.getServiceId()+" does NOT exist in Services.");
	}	
}
