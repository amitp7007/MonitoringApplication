package com.amdocs.challenge.monitoring.domain;

public class DownTimeResponse implements Response{
	
	public DownTimeResponse(String serviceName, long downtime, String state, String timeUnit) {
		super();
		this.serviceName = serviceName;
		this.totalDowntime = downtime;
		this.currentState = state;
		this.timUnit = timeUnit;
	}
	private String serviceName;
	private String currentState;
	private String timUnit;
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
	public String getTimUnit() {
		return timUnit;
	}
	public void setTimUnit(String timUnit) {
		this.timUnit = timUnit;
	}
	public long getTotalDowntime() {
		return totalDowntime;
	}
	public void setTotalDowntime(long totalDowntime) {
		this.totalDowntime = totalDowntime;
	}
}
