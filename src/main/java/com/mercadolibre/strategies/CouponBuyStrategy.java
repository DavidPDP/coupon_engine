package com.mercadolibre.strategies;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;

/**
 * Contract to implement new items recommendation strategies
 * by applying a coupon.
 * 
 * @author Johan Ballesteros
 * @since 1.0.0
 */
@FunctionalInterface
public interface CouponBuyStrategy {
	
	/**
	 * Given the prices of the items, implements a strategy to 
	 * prioritize a recommended purchase list.
	 * 
	 * @param items Map structure with the customer's favorite items. {k=itemId, v=itemPrice}.
	 * @return Purchase priority list.
	 */
	// Note: This method represents the contract defined by the client.
	List<String> calculate(Map<String, Float> items, Float amount);
	
	@AllArgsConstructor
	public enum CouponBuyStrategyType { 
		
		MAX_ITEM_QUANTITY("maxItemQuantityStrategy"), 
		MAX_SPEND("maxSpendStrategy");
		
		private String id;
		
		public String id() { return id; }
		
	};
	
}
