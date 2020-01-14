package com.amdocs.challenge.monitoring.domain;

public class Status {
	
	private boolean isActive;
	private long lastAvailable;
	private long startTime;
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public long getLastAvailable() {
		return lastAvailable;
	}
	public void setLastAvailable(long lastAvailable) {
		this.lastAvailable = lastAvailable;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
}
