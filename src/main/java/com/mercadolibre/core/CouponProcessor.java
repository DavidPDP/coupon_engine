package com.mercadolibre.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.mercadolibre.strategies.CouponBuyStrategy;
import com.mercadolibre.strategies.MaxItemQuantityStrategy;

@Service
public class CouponProcessor {

	private static final CouponBuyStrategy DEFAULT_BUY_STRATEGY = new MaxItemQuantityStrategy();
	
	public List<String> calculate(Map<String, Float> items, Float amount) {
	
		List<String> result = new ArrayList<>(); 
		var itemsAvailableToBuy = DEFAULT_BUY_STRATEGY.applyCouponStrategy(items);
		
		float totalPriceItemsBuyed = 0;
		for(Entry<String, Float> item : itemsAvailableToBuy) {
			
			if(totalPriceItemsBuyed + item.getValue() > amount) break;
			
			result.add(item.getKey());
			totalPriceItemsBuyed += item.getValue();
			
		}
		
		return result;
		
	}
	
	public List<String> calculate(Map<String, Float> items, Float amount, CouponBuyStrategy strategy) {
		return null;
	}
	
}
