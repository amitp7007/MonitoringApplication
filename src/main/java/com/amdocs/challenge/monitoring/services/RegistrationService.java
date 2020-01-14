package com.amdocs.challenge.monitoring.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amdocs.challenge.monitoring.dbrepo.InMemoryDatabase;
import com.amdocs.challenge.monitoring.domain.Microservice;
/**
 * Class used to intract with db service to store the microservice related information.
 * @author amit
 *
 */
@Service
public class RegistrationService {
	@Autowired
	InMemoryDatabase dbService;
	
	private static Logger logger = LoggerFactory.getLogger(RegistrationService.class);

	/**
	 * 
	 * @param ms
	 * @return
	 * @throws Exception 
	 */
	public Microservice register(Microservice ms) throws Exception {
		String serviceName = ms.getServiceName();
		return dbService.insert(serviceName, ms); 
	}
	
	/**
	 * 
	 * @param serviceName
	 * @return
	 */
	public Microservice  deregister(String serviceName) {
		return dbService.delete(serviceName);
	}
	
	public Microservice getMicroService(String msName) throws Exception {
		logger.info("Retrieving Microservice {}", msName);
		Microservice registeredMS = dbService.retrieve(msName);
		if (registeredMS == null) {
			throw new Exception("Microservice is not found");
		}
		return registeredMS;
	}

	
}
