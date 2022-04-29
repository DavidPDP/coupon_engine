package com.mercadolibre.api;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mercadolibre.core.CouponProcessor;
import com.mercadolibre.entities.RecommendedItems;
import com.mercadolibre.strategies.CouponBuyStrategy.CouponBuyStrategyType;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Mono;

/**
 * HTTP API that exposes coupon engine system flows. Represents an output port 
 * (whose adapter is implemented via Spring MVC Rest Controllers).
 * 
 * @author Johan Ballesteros
 * @since 1.1.0
 */
@RestController
@AllArgsConstructor
public class CouponEngineAPI {

	private CouponProcessor couponProcessor;
	
	/** Request payload body of the Query POST /coupon. */
	@Data
	public static class CouponRequest {
		@JsonAlias("items_id") private List<String> itemsId;
		@JsonAlias("amount") private Float couponAmount;
		private CouponBuyStrategyType strategy;
	}
	
	/**
	 * Given the item ids list and the coupon amount (limit to spend), 
	 * it proceeds to calculate the list of recommended items to buy. 
	 * Uses the strategy of maximum quantity of products to buy. 
	 * 
	 * @param request {@link com.mercadolibre.api.CouponRequest CouponRequest}
	 * @return Future/Promise with RecommendedItems.
	 * @see com.mercadolibre.core.CouponProcessor
	 * @see com.mercadolibre.strategies.MaxItemQuantityStrategy
	 */
	// Note: DDR = favorites items size, coupon amount, recommended items size, total to spend, timestamp.
	@PostMapping("/coupon")
	public Mono<RecommendedItems> recommendItemsByApplyingCoupon(@RequestBody CouponRequest request) {
		
		var response = couponProcessor.calculateItemsToRecommend(
			request.getItemsId(), request.getCouponAmount(), request.getStrategy()
		);
		
		// DDR print.
		response.subscribe(r -> { 
			int reqSize = request.getItemsId() != null ? request.getItemsId().size() : 0;
			int resSize = r.getItemsId() != null ? r.getItemsId().size() : 0;
			System.out.format("%s,%s,%s,%s,%s,%s'\n", 
					reqSize, request.getCouponAmount(),
					resSize, r.getTotal(),
					Instant.now().toString(), request.getStrategy()
			);
		});
		
		return response;
		
	}
	
}
