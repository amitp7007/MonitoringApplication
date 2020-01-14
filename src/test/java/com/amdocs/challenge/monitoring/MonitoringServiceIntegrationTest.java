package com.amdocs.challenge.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;

import com.amdocs.challenge.monitoring.dbrepo.InMemoryDatabase;
import com.amdocs.challenge.monitoring.domain.DownTimeResponse;
import com.amdocs.challenge.monitoring.domain.ErrorResponse;
import com.amdocs.challenge.monitoring.domain.Microservice;
import com.amdocs.challenge.monitoring.domain.Response;
import com.amdocs.challenge.monitoring.domain.StatusResponse;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MonitoringServiceIntegrationTest {

	@LocalServerPort
	private int localPort;

	@Autowired
	private TestRestTemplate restTemplate;

	@MockBean
	private InMemoryDatabase dbS;
	@Autowired
	private RestTemplate restT;

	private MockRestServiceServer mockService1;

	List<Microservice> listOfServiceRegistered = new ArrayList<Microservice>();
	Map<String, Boolean> expectedStatusOfService = new HashMap<>();

	@BeforeEach
	void setUp() {
		RestGatewaySupport gateWay = new RestGatewaySupport();
		gateWay.setRestTemplate(restT);
		mockService1 = MockRestServiceServer.createServer(gateWay);		
		Microservice ms = new Microservice();
		ms.setServiceName("MS1");
		ms.setServiceUrl("http://localhost:9090/health");
		listOfServiceRegistered.add(ms);
		expectedStatusOfService.put(ms.getServiceName(), true);

		ms = new Microservice();
		ms.setServiceName("MS2");
		ms.setServiceUrl("http://localhost:9091/health");
		listOfServiceRegistered.add(ms);
		expectedStatusOfService.put(ms.getServiceName(), false);

	}

	@AfterEach
	void tearDown() {
		listOfServiceRegistered.clear();
		mockService1 = null;
	}

	@Test
	public void getStatusOfAllActiveNDownMicroservices() throws Exception {
		mockService1.expect(ExpectedCount.min(2), requestTo("http://localhost:9090/health"))
				.andRespond(withStatus(HttpStatus.OK));
		mockService1.expect(ExpectedCount.min(2), requestTo("http://localhost:9091/health"))
				.andRespond(withStatus(HttpStatus.NOT_ACCEPTABLE));
		Mockito.when(dbS.retrieveAll()).thenReturn(listOfServiceRegistered);
		List<StatusResponse> expectedResult = listOfServiceRegistered.stream().map(m -> {
			return new StatusResponse(m.getServiceName(), m.getServiceUrl(), true);
		}).collect(Collectors.toList());
		ResponseEntity<List<StatusResponse>> response = this.restTemplate.exchange(
				"http://localhost:" + localPort + "/monitoring/microservices", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<StatusResponse>>() {});
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody()).hasSameSizeAs(expectedResult);
		response.getBody().stream().forEach(s -> {
			assertThat(s.isActive()).isEqualTo(expectedStatusOfService.get(s.getServiceName()));
		});
	}

	@Test
	public void getAllMicroServices() throws Exception {
		mockService1.expect(ExpectedCount.min(2), requestTo("http://localhost:9090/health"))
				.andRespond(withStatus(HttpStatus.OK));
		mockService1.expect(ExpectedCount.min(2), requestTo("http://localhost:9091/health"))
				.andRespond(withStatus(HttpStatus.NOT_ACCEPTABLE));
		Mockito.when(dbS.retrieveAll()).thenReturn(listOfServiceRegistered);
		ResponseEntity<List<StatusResponse>> response = this.restTemplate.exchange(
				"http://localhost:" + localPort + "/monitoring/microservices", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<StatusResponse>>() {
				});
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody()).hasSameSizeAs(listOfServiceRegistered);
	}

	@Test
	public void getStatusOfActiveMicroservice() throws Exception {
		mockService1.expect(ExpectedCount.min(2), requestTo("http://localhost:9091/health")).andRespond(withStatus(HttpStatus.OK));
		Microservice ms = new Microservice();
		ms.setServiceName("MS1");
		ms.setServiceUrl("http://localhost:9091/health");
		Mockito.when(dbS.retrieve(ms.getServiceName())).thenReturn(ms);
		ResponseEntity<StatusResponse> response = this.restTemplate.exchange(
				"http://localhost:" + localPort + "/monitoring/microservice/" + ms.getServiceName(), HttpMethod.GET,
				null, StatusResponse.class);
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getServiceName().equals(ms.getServiceName()) && response.getBody().isActive())
				.isTrue();
	}

	@Test
	public void getStatusOfDownMicroservice() throws Exception {
		mockService1.expect(ExpectedCount.min(2), requestTo("http://localhost:9091/health"))
				.andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT));
		Microservice ms = new Microservice();
		ms.setServiceName("MS1");
		ms.setServiceUrl("http://localhost:9091/health");
		Mockito.when(dbS.retrieve(ms.getServiceName())).thenReturn(ms);
		ResponseEntity<StatusResponse> response = this.restTemplate.exchange(
				"http://localhost:" + localPort + "/monitoring/microservice/" + ms.getServiceName(), HttpMethod.GET,
				null, StatusResponse.class);
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getServiceName().equals(ms.getServiceName()) && !response.getBody().isActive())
				.isTrue();

	}

	@Test
	public void registerMicroService() throws Exception {
		Microservice ms = new Microservice();
		ms.setServiceName("MS1");
		ms.setServiceUrl("http://localhost:9091/health");
		Mockito.when(dbS.insert(ms.getServiceName(), ms)).thenReturn(ms);
		ResponseEntity<Response> response = this.restTemplate
				.postForEntity("http://localhost:" + localPort + "/monitoring/microservice/register", ms, Response.class);
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
	}
	
	@Test
	public void getMicroServiceDowntime() throws Exception {
		mockService1.expect(ExpectedCount.min(2), requestTo("http://localhost:9091/health")).andRespond(withStatus(HttpStatus.OK));
		Microservice ms = new Microservice();
		ms.setServiceName("MS1");
		ms.setServiceUrl("http://localhost:9091/health");
		long expectedDowntime = 0;
		Mockito.when(dbS.retrieve(ms.getServiceName())).thenReturn(ms);
		ResponseEntity<DownTimeResponse> responseDownTime = this.restTemplate.exchange(
				"http://localhost:" + localPort + "/monitoring/microservice/" + ms.getServiceName(), HttpMethod.GET,
				null, DownTimeResponse.class);
		
		assertThat(responseDownTime.getStatusCodeValue()).isEqualTo(200);
		assertThat(responseDownTime.getBody().getServiceName().equals(ms.getServiceName()) && 
				responseDownTime.getBody().getTotalDowntime() == expectedDowntime)
				.isTrue();
	}

	@Test
	public void deleteMicroService() throws Exception {
		Microservice ms = new Microservice();
		ms.setServiceName("MS1");
		ms.setServiceUrl("http://localhost:9091/health");
		Mockito.when(dbS.insert(ms.getServiceName(), ms)).thenReturn(ms);
		int resp = this.restTemplate
				.postForEntity("http://localhost:" + localPort + "/monitoring/microservice/register", ms,
						ResponseEntity.class)
				.getStatusCodeValue();
		assertThat(resp).isEqualTo(200);

		mockService1.expect(requestTo("http://localhost:9091/health")).andRespond(withStatus(HttpStatus.OK));
		Mockito.when(dbS.retrieve(ms.getServiceName())).thenReturn(null);
		ResponseEntity<ErrorResponse> errorResponse = this.restTemplate.exchange(
				"http://localhost:" + localPort + "/monitoring/microservice/" + ms.getServiceName(), HttpMethod.GET,
				null, ErrorResponse.class);
		assertThat(errorResponse.getStatusCodeValue()).isEqualTo(404);
		assertThat(errorResponse.getBody().getMessage()).isEqualTo("Microservice not found");
	}

}
