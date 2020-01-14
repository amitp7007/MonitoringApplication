package com.amdocs.challenge.monitoring;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 
 * @author amit
 *
 */
@Component
@ConditionalOnProperty(value = "proxyEnabled", havingValue = "true")
public class ApplicationPropertiesInitalizer implements InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(ApplicationPropertiesInitalizer.class);
	@Autowired
	ProxyConfigProperties proxyConfig;

	@Override
	public void afterPropertiesSet() throws Exception {
		proxySetting();
		
	}

	private void proxySetting() {
		
		if (proxyConfig.isOverrideDefault()) {
			final Set<String> noProxyHostSet = new LinkedHashSet<>();
			if (proxyConfig.getNoProxyHosts() != null && !proxyConfig.getNoProxyHosts().isEmpty()) {
				noProxyHostSet.addAll(Arrays.asList(proxyConfig.getNoProxyHosts().split("\\|")));
			}
			ProxySelector.setDefault(new ProxySelector() {

				@Override
				public List<Proxy> select(URI uri) {
					final List<Proxy> proxyList = new ArrayList<Proxy>();
					String host = uri.getHost();
					if (!noProxyHostSet.contains(host)) {
						final Proxy proxy = new Proxy(Proxy.Type.HTTP,
								new InetSocketAddress(proxyConfig.getProxyHost(), proxyConfig.getProxyPort()));
						proxyList.add(proxy);
					} else {
						proxyList.add(Proxy.NO_PROXY);
					}
					return proxyList;
				}

				@Override
				public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
					logger.error("Unable to connect proxy");

				}
			});
			String userName = proxyConfig.getUserName();
			String password = proxyConfig.getPassword();
			Authenticator.setDefault(new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password.toCharArray());
				}
			});
			logger.info("Proxy configuration loaded");
		}
	}
}
