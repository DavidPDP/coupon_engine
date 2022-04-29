package com.mercadolibre.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Value;

/**
 * Entity (Aggregation Root) that represents the status of an items 
 * purchase recommendation.
 * 
 * @author Johan Ballesteros
 * @since 1.1.0
 */
@Value
public class RecommendedItems {
	
	private static final int SUCCESSFUL_CODE = 0;
	private static final int CONGESTION_ERROR_CODE = 1;
	private static final int BAD_PARAMS_ERROR_CODE = 2;
	
	private int code;
	
	private String message;

	@JsonAlias("items_id") 
	private List<String> itemsId;
	
	/** Total price of recommended items. */
	private Float total;
	
	public static RecommendedItems buildSucessful(List<String> itemsId, Float total) {
		return new RecommendedItems(SUCCESSFUL_CODE, "Successful", itemsId, total);
	}
	
	public static RecommendedItems buildCongestionError() {
		return new RecommendedItems(CONGESTION_ERROR_CODE, "Congestion", null, 0.00F);
	}
	
	public static RecommendedItems buildBadParams() {
		return new RecommendedItems(BAD_PARAMS_ERROR_CODE, 
			"Bad params: the item ids list cannot be null and the coupon amount cannot be 0 or negative", 
			null, 0.00F
		);
	}
	
}
