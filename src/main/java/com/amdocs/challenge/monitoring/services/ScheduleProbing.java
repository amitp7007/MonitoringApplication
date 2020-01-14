package com.amdocs.challenge.monitoring.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleProbing {
	private static final Logger log = LoggerFactory.getLogger(ScheduleProbing.class);
	@Autowired
	ProbingService service;

	@Scheduled(fixedRate = 15000, initialDelay = 2000)
	public void scheduleProbing() {
		try {
			service.getMicroServicesStatus();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e.getMessage());
		}
	}

}
