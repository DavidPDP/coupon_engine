package com.mercadolibre.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MaxItemQuantityStrategy implements CouponBuyStrategy {

	@Override
	public List<Entry<String, Float>> applyCouponStrategy(Map<String, Float> items) {
		List<Entry<String, Float>> itemsList = new ArrayList<>(items.entrySet());
		itemsList.sort(Entry.comparingByValue()); // natural order.
		return itemsList;
	}

}
