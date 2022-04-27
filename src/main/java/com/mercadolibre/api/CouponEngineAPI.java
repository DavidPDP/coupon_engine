package com.mercadolibre.api;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mercadolibre.core.CouponProcessor;
import com.mercadolibre.entities.RecommendedItems;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class CouponEngineAPI {

	private CouponProcessor couponProcessor;
	
	@Data
	public static class CouponRequest {
		@JsonAlias("items_id") private List<String> itemsId;
		@JsonAlias("amount") private Float couponAmount;
	}
	
	@PostMapping("/coupon")
	public Mono<RecommendedItems> recommendItemsByApplyingCoupon(@RequestBody CouponRequest request) {
		return couponProcessor.calculateItemsToRecommend(request.getItemsId(), request.getCouponAmount());
	}
	
}
