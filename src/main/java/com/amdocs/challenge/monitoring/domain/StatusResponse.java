package com.amdocs.challenge.monitoring.domain;

public class StatusResponse implements Response{
	public StatusResponse(String serviceName, String serviceUrl, boolean isActive) {
		super();
		this.serviceName = serviceName;
		this.serviceUrl = serviceUrl;
		this.isActive = isActive;
	}
	private String serviceName;
	private String serviceUrl;
	private boolean isActive;
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceUrl() {
		return serviceUrl;
	}
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
