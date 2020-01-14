package com.amdocs.challenge.monitoring.domain;

public class DownTimeResponse implements Response {

	public DownTimeResponse(String serviceName, long downtime, String state, String timeUnit) {
		super();
		this.serviceName = serviceName;
		this.totalDowntime = downtime;
		this.currentState = state;
		this.unit = timeUnit;
	}

	private String serviceName;
	private String currentState;
	private String unit;
	private long totalDowntime;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String isCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public String getCurrentState() {
		return this.currentState;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String timUnit) {
		this.unit = timUnit;
	}

	public long getTotalDowntime() {
		return totalDowntime;
	}

	public void setTotalDowntime(long totalDowntime) {
		this.totalDowntime = totalDowntime;
	}
}
