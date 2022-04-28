package com.mercadolibre.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

/**
 * Max Item Quantity Strategy.
 * 
 * @author Johan Ballesteros
 * @since 1.0.0
 */
@Component
public class MaxItemQuantityStrategy implements CouponBuyStrategy {
	
	@Override
	public List<String> calculate(Map<String, Float> items, Float amount) {
		
		List<String> result = new ArrayList<>();
		List<Entry<String, Float>> itemsList = new ArrayList<>(items.entrySet());
		itemsList.sort(Entry.comparingByValue()); // natural order.
		
		float totalPriceItemsBuyed = 0;
		for(Entry<String, Float> item : itemsList) {
			
			if(totalPriceItemsBuyed + item.getValue() > amount) break;
			
			result.add(item.getKey());
			totalPriceItemsBuyed += item.getValue();
			
		}
		
		return result;
		
	}

}
