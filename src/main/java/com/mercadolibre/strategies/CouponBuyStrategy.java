package com.mercadolibre.strategies;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	 * organize a purchase priority list.
	 * 
	 * @param items Map structure with the customer's favorite items. {k=itemId, v=itemPrice}.
	 * @return Purchase priority list.
	 */
	List<Entry<String, Float>> applyCouponStrategy(Map<String, Float> items);
	
}
