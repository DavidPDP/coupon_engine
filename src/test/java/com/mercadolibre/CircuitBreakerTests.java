package com.mercadolibre;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
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
@AutoConfigureWebTestClient(timeout = "36000")
public class CircuitBreakerTests {

	@Autowired
	private WebTestClient webClient;
	
	@Autowired
	private ObjectMapper mapper = new ObjectMapper();
	
	@DynamicPropertySource
    static void registerMeliAPIProperties(DynamicPropertyRegistry registry) {
        registry.add("meli.items.api.url", () -> "https://api.mercadolibre.com/items");
        registry.add("meli.items.api.paging", () -> "20");
        registry.add("resilience4j.timelimiter.configs.default.timeout-duration", () -> "5s");
    }
	
	@Test
	void consume_coupon_engine_api() throws Exception {
		
		// Init.
		CouponRequest request = new CouponRequest();
		request.setItemsId(List.of("MCO808833794","MCO808833795"));
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
		
		var expectedResponseBody = RecommendedItems.buildError();
		assertEquals(mapper.writeValueAsString(expectedResponseBody), responseBody);

	}
	
}
