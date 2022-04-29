package com.mercadolibre.core;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mercadolibre.entities.RecommendedItems;
import com.mercadolibre.repositories.MeliItemRepo;
import com.mercadolibre.strategies.CouponBuyStrategy;
import com.mercadolibre.strategies.CouponBuyStrategy.CouponBuyStrategyType;

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

	private Map<String, CouponBuyStrategy> couponStrategies;
	
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
	public Mono<RecommendedItems> calculateItemsToRecommend(List<String> itemIds, Float couponAmount, CouponBuyStrategyType strategyType) {
		
		if(itemIds == null || couponAmount < 1) 
			return Mono.just(RecommendedItems.buildBadParams());  
		else {
			var couponBuyStrategy = strategyType == null ? 
				couponStrategies.get(CouponBuyStrategyType.MAX_SPEND.id()) : couponStrategies.get(strategyType.id());
			var items = meliItemRepo.fecthMeliItems(itemIds);
			return items.map(fecthedItems -> buildRecommendedItems(fecthedItems, couponAmount, couponBuyStrategy));
		}
		
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
	private RecommendedItems buildRecommendedItems(Map<String, Float> items, Float couponAmount, CouponBuyStrategy strategy) {
		
		if(items.size() == 1 && items.containsKey("")) { // only have empty item
			return RecommendedItems.buildCongestionError();
		} else {
			var recommendedItems = strategy.calculate(items, couponAmount);
			var totalPriceRecommendedItems = recommendedItems.stream().map(i -> items.get(i)).reduce(0.00F, Float::sum);
			return RecommendedItems.buildSucessful(recommendedItems, totalPriceRecommendedItems);
		}
	
	}
	
}
