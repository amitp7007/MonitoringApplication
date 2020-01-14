package com.amdocs.challenge.monitoring.dbrepo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.amdocs.challenge.monitoring.domain.Microservice;

/**
 * This class act as in memory database to store all the information temporarily
 * It provide necessary DB operations
 * 
 * @author amit
 *
 */
@Service
public class InMemoryDatabase {
	private Map<String, Microservice> cachedMap = new ConcurrentHashMap<>();
	private Map<String, Long> cachedMapTotalUnavailable = new ConcurrentHashMap<>();

	private long lastUpdatedTime;

	public Microservice insert(String IDHashed, Microservice ms) throws Exception {
		if (cachedMap.containsKey(IDHashed)) {
			throw new Exception("Microservice already registered");
		}
		cachedMap.put(IDHashed, ms);
		return ms;
	}

	public Microservice retrieve(String serviceName) {
		return cachedMap.get(serviceName);
	}

	public Microservice delete(String serviceName) {
		return cachedMap.remove(serviceName);
	}

	public List<Microservice> retrieveAll() {
		return cachedMap.values().parallelStream().collect(Collectors.toList());
	}

	public long getMSInActiveTime(Microservice ms) {
		return cachedMapTotalUnavailable.containsKey(ms.getServiceName())
				? cachedMapTotalUnavailable.get(ms.getServiceName())
				: 0L;
	}

	public void setMSInActiveTime(Microservice ms, long totalInactiveTime) {
		cachedMapTotalUnavailable.put(ms.getServiceName(), totalInactiveTime);
	}

	public long getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	public void setLastUpdateTime(long time) {
		lastUpdatedTime = time;
	}

	public void update(String serviceName, Microservice ms) {
		cachedMap.put(serviceName, ms);
	}
}
