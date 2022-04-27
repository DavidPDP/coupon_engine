package com.mercadolibre.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.mercadolibre.entities.RecommendedItems;
import com.mercadolibre.repositories.MeliItemRepo;
import com.mercadolibre.strategies.CouponBuyStrategy;
import com.mercadolibre.strategies.MaxItemQuantityStrategy;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class CouponProcessor {

	private static final CouponBuyStrategy DEFAULT_BUY_STRATEGY = new MaxItemQuantityStrategy();
	
	private MeliItemRepo meliItemRepo;
	
	public Mono<RecommendedItems> calculateItemsToRecommend(List<String> itemIds, Float couponAmount) {
		var items = meliItemRepo.fecthMeliItems(itemIds);
		return items.map(r -> buildRecommendedItems(r, couponAmount));
	}
	
	private RecommendedItems buildRecommendedItems(Map<String, Float> items, Float couponAmount) {
		var recommendedItems = calculate(items, couponAmount);
		var totalPriceRecommendedItems = recommendedItems.stream().map(i -> items.get(i)).reduce(0.00F, Float::sum);
		return new RecommendedItems(recommendedItems, totalPriceRecommendedItems);
	}
	
	private List<String> calculate(Map<String, Float> items, Float amount) {
	
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
	
}
