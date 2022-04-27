package com.mercadolibre;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.api.CouponEngineAPI.CouponRequest;
import com.mercadolibre.entities.RecommendedItems;

import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
public class CouponEngineAPITests {

	@Autowired
	private WebTestClient webClient;
	
	@Autowired
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	void consume_coupon_engine_api() throws Exception {
		
		// Init.
		CouponRequest request = new CouponRequest();
		request.setItemsId(List.of("MCO808833794", "MCO808833795", "MCO808833796"));
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
		
		var expectedResponseBody = new RecommendedItems(List.of("MCO808833796"), 36950F);
		assertEquals(mapper.writeValueAsString(expectedResponseBody), responseBody);
		
	}
	
}
