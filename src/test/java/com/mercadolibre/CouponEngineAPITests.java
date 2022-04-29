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
public class CouponEngineAPITests {

	@Autowired
	private WebTestClient webClient;
	
	@Autowired
	private ObjectMapper mapper = new ObjectMapper();
	
	@DynamicPropertySource
    static void registerMeliAPIProperties(DynamicPropertyRegistry registry) {
        registry.add("meli.items.api.url", () -> "https://api.mercadolibre.com/items");
        registry.add("meli.items.api.paging", () -> "20");
        registry.add("resilience4j.timelimiter.configs.default.timeout-duration", () -> "20s");
    }
	
	@Test
	void consume_coupon_engine_api() throws Exception {
		
		// Init.
		var request = new CouponRequest();
		request.setItemsId(List.of("MCO808833794","MCO808833795","MCO808833796","MCO808833797",
				"MCO808833798", "MCO808833799","MCO808833800","MCO808833801","MCO808833802",
				"MCO808833803", "MCO808833804","MCO808833805","MCO808833806","MCO808833807", 
				"MCO808833808", "MCO808833809","MCO808833810","MCO808833811","MCO808833812", 
				"MCO808833813","MCO808833814", "MCO808833815", "MCO808833816","MCO808833817",
				"MCO808833818","MCO808833819", "MCO808833820"));
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
		
		var expectedResponseBody = RecommendedItems.buildSucessful(List.of("MCO808833807"), 45400F);
		assertEquals(mapper.writeValueAsString(expectedResponseBody), responseBody);
		
	}
	
	@Test
	void consume_coupon_engine_api_with_bad_params() throws Exception {
		
		// Init.
		var request = new CouponRequest();
		request.setItemsId(null);
		
		// Scenario 1: verify response struct.
		var responseBody = webClient.post()
				.uri("/coupon")
				.body(Mono.just(request), CouponRequest.class)
				.exchange()
				.expectStatus().isOk()
				.returnResult(String.class)
				.getResponseBody()
				.blockFirst();
		
		assertEquals(mapper.writeValueAsString(RecommendedItems.buildBadParams()), responseBody);
		
		
	}
	
}
