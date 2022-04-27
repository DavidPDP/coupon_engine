package com.mercadolibre.strategies;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@FunctionalInterface
public interface CouponBuyStrategy {

	List<Entry<String, Float>> applyCouponStrategy(Map<String, Float> items);
	
}
