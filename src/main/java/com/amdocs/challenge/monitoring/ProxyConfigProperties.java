package com.amdocs.challenge.monitoring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "http.proxy")
public class ProxyConfigProperties {
	private boolean overrideDefault;
	private String proxyHost;
	private int proxyPort;
	private String noProxyHosts;
	private String userName;
	private String password;

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public boolean isOverrideDefault() {
		return overrideDefault;
	}

	public void setOverrideDefault(boolean overrideDefault) {
		this.overrideDefault = overrideDefault;
	}

	public String getNoProxyHosts() {
		return noProxyHosts;
	}

	public void setNoProxyHosts(String noProxyHosts) {
		this.noProxyHosts = noProxyHosts;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
