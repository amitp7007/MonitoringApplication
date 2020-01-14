package com.amdocs.challenge.monitoring.controller;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amdocs.challenge.monitoring.domain.ErrorResponse;
import com.amdocs.challenge.monitoring.domain.Microservice;
import com.amdocs.challenge.monitoring.domain.Response;
import com.amdocs.challenge.monitoring.services.RegistrationService;

@RestController
@RequestMapping("monitoring")
public class RegistrationController {
	private static Logger logger = LoggerFactory.getLogger(MonitoringController.class);
	@Autowired
	RegistrationService registrationService;
	
	/**
	 * Register the given microservice with monitoring service.
	 * @param ms
	 * @return
	 * @throws URISyntaxException 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "microservice/register")
	public ResponseEntity<Response> registration(@RequestBody Microservice ms) {
		try {
			registrationService.register(ms);
			logger.debug("Microservice Regsitration succesfull...");
		} catch (Exception e) {	
			logger.error(e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage()));
		}
		return ResponseEntity.ok().build();
	}

	/**
	 * 
	 * @param serviceName
	 * @return
	 */
	@DeleteMapping("microservice/deregister/{serviceName}")
	public ResponseEntity<Microservice> deRegistration(@PathVariable("serviceName") String serviceName) {
		Microservice ms = registrationService.deregister(serviceName);
		return ResponseEntity.ok().body(ms);
	}

}
