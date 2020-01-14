package com.amdocs.challenge.monitoring.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amdocs.challenge.monitoring.domain.DownTimeResponse;
import com.amdocs.challenge.monitoring.domain.ErrorResponse;
import com.amdocs.challenge.monitoring.domain.Microservice;
import com.amdocs.challenge.monitoring.domain.Response;
import com.amdocs.challenge.monitoring.domain.StatusResponse;
import com.amdocs.challenge.monitoring.services.ProbingService;
import com.amdocs.challenge.monitoring.services.RegistrationService;

/**
 * Controller handle all the incoming requests for momitoring the microserices.
 * Based on the request type it route the request to intended service.
 * 
 * @author amit
 *
 */
@RestController
@RequestMapping("monitoring")
public class MonitoringController {

	private static Logger logger = LoggerFactory.getLogger(MonitoringController.class);
	@Autowired
	ProbingService availibiltyService;

	@Autowired
	RegistrationService registrationService;

	/**
	 * Return the response list of all register microservices with status, If no
	 * service is registered it returns empty list.
	 * 
	 * @return
	 */
	@RequestMapping("/microservices")
	public ResponseEntity<List<Response>> getServices() {
		try {
			List<Response> result = availibiltyService.getMicroServicesStatus().stream().map(ms -> {
				return new StatusResponse(ms.getServiceName(), ms.getServiceUrl(), ms.getStatus().isActive());
			}).collect(Collectors.toList());
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return ResponseEntity.ok(new ArrayList<>());
	}

	/**
	 * Returns the response for request microservice name with current status of
	 * availability . If given service is not registered, it respond with https
	 * status code 404 Not Found
	 * 
	 * @param serviceName
	 * @return
	 */
	@RequestMapping("/microservice/{serviceName}")
	public ResponseEntity<Response> getService(@PathVariable("serviceName") String serviceName) {
		Microservice ms;
		try {
			ms = availibiltyService.getMicroServiceStatus(serviceName);
			return ResponseEntity.ok()
					.body(new StatusResponse(ms.getServiceName(), ms.getServiceUrl(), ms.getStatus().isActive()));
		} catch (Exception e) {
			return new ResponseEntity<Response>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()),
					HttpStatus.NOT_FOUND);
			// return ResponseEntity.status(HttpStatus.NOT_FOUND)
			// .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
		}
	}

	/**
	 * Return the total downtime of a microservice
	 * 
	 * @param serviceName
	 * @return
	 */
	@GetMapping("/microservice/downtime/{serviceName}")
	public ResponseEntity<Response> getActiveTime(@PathVariable("serviceName") String serviceName) {
		try {
			long time = availibiltyService.getMicroServiceTotalDownTime(serviceName);
			String currentState = availibiltyService.getMicroServiceStatus((serviceName)).getStatus().isActive()
					? "Active"
					: "Down";
			DownTimeResponse res = new DownTimeResponse(serviceName, (time / 1000), currentState, "SECOND");
			return ResponseEntity.ok(res);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
		}
	}

}
