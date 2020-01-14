package com.amdocs.challenge.monitoring.services;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.amdocs.challenge.monitoring.dbrepo.InMemoryDatabase;
import com.amdocs.challenge.monitoring.domain.Microservice;
import com.amdocs.challenge.monitoring.domain.Status;

/**
 * 
 * @author amit Class use to probe the register micro-service to get the
 *         liveness. Based on the response it calculated the total down time and
 *         save the status with start time and last available time in database.
 */
@Service
public class ProbingService {
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	InMemoryDatabase inMemoryDB;
	private static Logger logger = LoggerFactory.getLogger(ProbingService.class);

	/**
	 * Is used to validate the response code return from microservice rest call. if
	 * response code exist in this then it mean service is active and running
	 * otherwise service is down
	 */
	private static HashSet<HttpStatus> SUCCESS_STATUS_CODE = new HashSet<>();

	static {
		SUCCESS_STATUS_CODE.add(HttpStatus.ACCEPTED);
		SUCCESS_STATUS_CODE.add(HttpStatus.OK);
		SUCCESS_STATUS_CODE.add(HttpStatus.FOUND);
		SUCCESS_STATUS_CODE.add(HttpStatus.NO_CONTENT);
		SUCCESS_STATUS_CODE.add(HttpStatus.CREATED);
	}

	public List<Microservice> getMicroServicesStatus() throws Exception {
		logger.info("Retrieving Microservice status");
		List<Microservice> registeredMS = inMemoryDB.retrieveAll();
		if (registeredMS == null || registeredMS.isEmpty()) {
			throw new Exception("No microservice found");
		}
		// if DB was update 5 sec before by sheduler then no need to probe microservice
		if ((Instant.now().toEpochMilli() - inMemoryDB.getLastUpdatedTime()) > 5000) {
			registeredMS.stream().forEach(ms -> {
				callMicroService(ms);
				updateDB(ms);
			});
			inMemoryDB.setLastUpdateTime((Instant.now().toEpochMilli()));
		}
		return registeredMS;
	}

	public Microservice getMicroServiceStatus(String ms) throws Exception {
		try {
			Microservice registeredMS = inMemoryDB.retrieve(ms);
			if (registeredMS == null) {
				throw new Exception("Microservice not found");
			}
			callMicroService(registeredMS);
			updateDB(registeredMS);
			return registeredMS;
		} catch (Exception e) {
			logger.error("Requested service not registered");
			throw e;
		}

	}

	/**
	 * Method update or insert new microservice in im-memory db
	 * 
	 * @param ms
	 */
	private void updateDB(Microservice ms) {
		inMemoryDB.update(ms.getServiceName(), ms);
	}

	/**
	 * Method use to call the registered microservice in the system, It just call
	 * the and get the response form microservice and validate with success CODE
	 * list. Method also calculate the total downtime base on last available time.
	 * 
	 * @param ms
	 */
	private void callMicroService(Microservice ms) {
		Status st = ms.getStatus();
		try {
			logger.info("Probing microservice {} ", ms.getServiceName());
			ResponseEntity<String> resEntity = restTemplate.getForEntity(ms.getServiceUrl(), String.class);
			if (SUCCESS_STATUS_CODE.contains(resEntity.getStatusCode())) {
				logger.debug("Probing microservice {} successful", ms.getServiceName());
				Long currentTime = Instant.now().toEpochMilli();
				if (st == null) {
					st = new Status();
					st.setActive(true);
					st.setLastAvailable(currentTime);
					st.setStartTime(currentTime);
					ms.setStatus(st);
				} else {
					long totalInactiveTime = inMemoryDB.getMSInActiveTime(ms);
					if (!st.isActive()) {
						totalInactiveTime = totalInactiveTime + currentTime - st.getLastAvailable();
						inMemoryDB.setMSInActiveTime(ms, totalInactiveTime);
					}
					st.setLastAvailable(currentTime);
					st.setActive(true);
				}
				logger.info("Microsevice {} is Active", ms.getServiceName());
			}
		} catch (Exception e) {
			logger.info("Microsevice {} is Down", ms.getServiceName());
			if (st == null) {
				st = new Status();
				st.setActive(false);
				ms.setStatus(st);
			} else {
				logger.debug("Calculating total downtime for microsevice {}", ms.getServiceName());
				st.setActive(false);
				long totalInactiveTime = inMemoryDB.getMSInActiveTime(ms);
				if (totalInactiveTime != 0)
					totalInactiveTime = totalInactiveTime + Instant.now().toEpochMilli() - st.getLastAvailable();
				else {
					totalInactiveTime = st.getLastAvailable() != 0 ? Instant.now().toEpochMilli() - st.getLastAvailable() : -1;
				}
				inMemoryDB.setMSInActiveTime(ms, totalInactiveTime);
			}
		}
	}

	/**
	 * Retrieve the total down time for given service name
	 * 
	 * @param serviceName
	 * @return
	 * @throws Exception
	 */
	public long getMicroServiceTotalDownTime(String serviceName) throws Exception {
		logger.info("Retrieving Microservice {} total downtime ", serviceName);
		Microservice ms = inMemoryDB.retrieve(serviceName);
		if (ms == null) {
			throw new Exception("Microservice is not found");
		}
		callMicroService(ms);
		updateDB(ms);
		long totalInactiveTime = (inMemoryDB.getMSInActiveTime(ms));
		logger.debug("Total Down time {}", totalInactiveTime);

		return totalInactiveTime;
	}
}
