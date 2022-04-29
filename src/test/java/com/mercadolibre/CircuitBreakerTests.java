package com.mercadolibre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.api.CouponEngineAPI.CouponRequest;
import com.mercadolibre.entities.RecommendedItems;

import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
public class CircuitBreakerTests {

	@Autowired
	private WebTestClient webClient;
	
	@Autowired
	private ObjectMapper mapper = new ObjectMapper();
	
	private static ClientAndServer mockServer;
	
	@BeforeAll
	static void startServer() {
		mockServer = ClientAndServer.startClientAndServer(9100);
	}
	
	@AfterAll
	static void stopServer() {
		mockServer.stop();
	}
	
	@DynamicPropertySource
    static void registerMeliAPIProperties(DynamicPropertyRegistry registry) {
        registry.add("meli.items.api.url", () -> "http://localhost:9100");
        registry.add("meli.items.api.paging", () -> "2");
        registry.add("resilience4j.timelimiter.configs.default.timeout-duration", () -> "1s");
    }
	
	@Test
	void consume_coupon_engine_api_with_back_pressure() throws Exception {
			
		var itemIds = List.of("MCO808833794","MCO808833795");	

		// Mock.
		mockServer.when(request().withMethod("GET").withQueryStringParameter("ids", String.join(",", itemIds)))
    		.respond(
				response().withStatusCode(200)
					.withBody("invalid", MediaType.APPLICATION_JSON).withDelay(TimeUnit.SECONDS, 10)
			);
		
		// Init.
		CouponRequest request = new CouponRequest();
		request.setItemsId(itemIds);
		request.setCouponAmount(50000F);
		
		// Scenario 1: verify response struct.
		var responseBody = webClient.post()
				.uri("/coupon")
				.body(Mono.just(request), CouponRequest.class)
				.exchange()
				.expectStatus().isOk()
				.returnResult(String.class)
				.getResponseBody()
				.blockFirst();
		
		var expectedResponseBody = RecommendedItems.buildCongestionError();
		assertEquals(mapper.writeValueAsString(expectedResponseBody), responseBody);

	}
	
}
