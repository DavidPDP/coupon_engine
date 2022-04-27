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

/**
 * Stateless service (stand-alone of model) that implements
 * the Coupon Engine business logic orchestration.
 * 
 * @author Johan Ballesteros
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class CouponProcessor {

	// Note: defined by the client.
	private static final CouponBuyStrategy DEFAULT_COUPON_STRATEGY = new MaxItemQuantityStrategy();
	
	private MeliItemRepo meliItemRepo;
	
	/**
	 * It is responsible for: 1. Finding the items prices and 
	 * 2. Building the list of recommended items, calculating with the
	 * {@link com.mercadolibre.strategies.MaxItemQuantityStrategy MaxItemQuantityStrategy}.
	 * 
	 * @param itemIds 
	 * @param couponAmount
	 * @return Future/Promise with RecommendedItems
	 */
	public Mono<RecommendedItems> calculateItemsToRecommend(List<String> itemIds, Float couponAmount) {
		var items = meliItemRepo.fecthMeliItems(itemIds);
		return items.map(fecthedItems -> buildRecommendedItems(fecthedItems, couponAmount));
	}
	
	/**
	 * Factory that is responsible for building the recommended items list.
	 * For this, it proceeds to calculate the recommended items list given 
	 * the prices of the customer's favorite products and the limit of the 
	 * coupon to be spent. Then calculates the total price to spend 
	 * (total price of recommended items). Finally, with the two previous data,
	 * create the {@link com.mercadolibre.entities.RecommendedItems RecommendedItems} entity. 
	 * 
	 * @param items Map structure with the customer's favorite items. {k=itemId, v=itemPrice}.
	 * @param couponAmount as the limit to spend.
	 * @return {@link com.mercadolibre.entities.RecommendedItems RecommendedItems}.
	 */
	private RecommendedItems buildRecommendedItems(Map<String, Float> items, Float couponAmount) {
		var recommendedItems = calculate(items, couponAmount);
		var totalPriceRecommendedItems = recommendedItems.stream().map(i -> items.get(i)).reduce(0.00F, Float::sum);
		return new RecommendedItems(recommendedItems, totalPriceRecommendedItems);
	}
	
	/**
	 * It is responsible for calculating the recommended items list. 
	 * For this, proceeds to order the items based on the default strategy.
	 * Subsequently, it is responsible for calculating the items that 
	 * reach the amount of the coupon to spend. Resulting in recommended items 
	 * list (item ids).
	 * 
	 * @param items Map structure with the customer's favorite items. {k=itemId, v=itemPrice}.
	 * @param amount as the limit to spend.
	 * @return recommended items list (item ids).
	 */
	// Note: This method represents the contract defined by the client.
	private List<String> calculate(Map<String, Float> items, Float amount) {
	
		List<String> result = new ArrayList<>(); 
		var itemsAvailableToBuy = DEFAULT_COUPON_STRATEGY.applyCouponStrategy(items);
		
		float totalPriceItemsBuyed = 0;
		for(Entry<String, Float> item : itemsAvailableToBuy) {
			
			if(totalPriceItemsBuyed + item.getValue() > amount) break;
			
			result.add(item.getKey());
			totalPriceItemsBuyed += item.getValue();
			
		}
		
		return result;
		
	}
	
}
