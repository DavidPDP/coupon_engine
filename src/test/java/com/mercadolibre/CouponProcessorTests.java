package com.mercadolibre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.mercadolibre.core.CouponProcessor;
import com.mercadolibre.entities.RecommendedItems;
import com.mercadolibre.repositories.MeliItemRepo;
import com.mercadolibre.strategies.CouponBuyStrategy.CouponBuyStrategyType;

import reactor.core.publisher.Mono;

@SpringBootTest
class CouponProcessorTests {

	@Autowired
	private CouponProcessor couponProcessor;
	
	@SpyBean
	private MeliItemRepo meliItemRepo;
	
	@Test
	void buy_with_max_item_quantity_strategy() {
		
		// Stub.
		var items1 = Mono.just(Map.of("MLA1", 100F, "MLA2", 210F, "MLA3", 260F, "MLA4", 80F, "MLA5", 90F));
		var items2 = Mono.just(Map.of("MLA1", 50.87F, "MLA2", 157.98F, "MLA3", 260.2F, "MLA4", 20.9F, "MLA5", 10.05F));
		var items3 = Mono.just(Map.of("MLA1", 501F, "MLA2", 1590F, "MLA3", 765.89F, "MLA4", 500.01F, "MLA5", 980F));
		doReturn(items1, items2, items3).when(meliItemRepo).fecthMeliItems(anyList());
		
		// Test items.
		var itemIds = List.of("MLA1", "MLA2", "MLA3", "MLA4", "MLA5");
		
		// Scenario 1: Positive residue.
		var recommendedItems = couponProcessor.calculateItemsToRecommend(itemIds, 500F, CouponBuyStrategyType.MAX_ITEM_QUANTITY).block();
		var expectedRecommendedItems = RecommendedItems.buildSucessful(List.of("MLA4", "MLA5", "MLA1", "MLA2"), 480F);
		assertEquals(expectedRecommendedItems, recommendedItems);
				
		// Scenario 2: Zero residue.
		recommendedItems = couponProcessor.calculateItemsToRecommend(itemIds, 500F, CouponBuyStrategyType.MAX_ITEM_QUANTITY).block();
		expectedRecommendedItems = RecommendedItems.buildSucessful(List.of("MLA5", "MLA4", "MLA1", "MLA2", "MLA3"), 500F);
		assertEquals(expectedRecommendedItems, recommendedItems);
			
		// Scenario 3: No items to buy (price over amount limit).
		recommendedItems = couponProcessor.calculateItemsToRecommend(itemIds, 500F, CouponBuyStrategyType.MAX_ITEM_QUANTITY).block();
		expectedRecommendedItems = RecommendedItems.buildSucessful(Collections.emptyList(), 0.00F);
		assertEquals(expectedRecommendedItems, recommendedItems);
		
	}
	
	@Test
	void buy_with_max_spend_strategy() {
		
		// Stub.
		var items1 = Mono.just(Map.of("MLA1", 100F, "MLA2", 210F, "MLA3", 260F, "MLA4", 80F, "MLA5", 90F));
		var items2 = Mono.just(Map.of("MLA1", 50.87F, "MLA2", 157.98F, "MLA3", 260.2F, "MLA4", 20.9F, "MLA5", 10.05F));
		var items3 = Mono.just(Map.of("MLA1", 501F, "MLA2", 1590F, "MLA3", 765.89F, "MLA4", 500.01F, "MLA5", 980F));
		var items4 = Mono.just(Map.of("MLA1", 100F, "MLA2", 100F, "MLA3", 100F, "MLA4", 100F, "MLA5", 500F));
		doReturn(items1, items2, items3, items4).when(meliItemRepo).fecthMeliItems(anyList());
		
		// Test items.
		var itemIds = List.of("MLA1", "MLA2", "MLA3", "MLA4", "MLA5");
		
		// Scenario 1: Positive residue.
		var recommendedItems = couponProcessor.calculateItemsToRecommend(itemIds, 500F, CouponBuyStrategyType.MAX_SPEND).block();
		var expectedRecommendedItems = RecommendedItems.buildSucessful(List.of("MLA2", "MLA1", "MLA5", "MLA4"), 480F);
		assertEquals(expectedRecommendedItems.getItemsId().size(), recommendedItems.getItemsId().size());
		assertEquals(
			expectedRecommendedItems.getItemsId().stream().filter(recommendedItems.getItemsId()::contains).count(), 
			expectedRecommendedItems.getItemsId().size()
		);
		
		// Scenario 2: Zero residue.
		recommendedItems = couponProcessor.calculateItemsToRecommend(itemIds, 500F, CouponBuyStrategyType.MAX_SPEND).block();
		expectedRecommendedItems = RecommendedItems.buildSucessful(List.of("MLA5", "MLA4", "MLA1", "MLA2", "MLA3"), 500F);
		assertEquals(expectedRecommendedItems.getItemsId().size(), recommendedItems.getItemsId().size());
		assertEquals(
			expectedRecommendedItems.getItemsId().stream().filter(recommendedItems.getItemsId()::contains).count(), 
			expectedRecommendedItems.getItemsId().size()
		);
		
		// Scenario 3: No items to buy (price over amount limit).
		recommendedItems = couponProcessor.calculateItemsToRecommend(itemIds, 500F, CouponBuyStrategyType.MAX_SPEND).block();
		expectedRecommendedItems = RecommendedItems.buildSucessful(Collections.emptyList(), 0.00F);
		assertEquals(expectedRecommendedItems, recommendedItems);
		
		// Scenario 4: Only select max spend items.
		recommendedItems = couponProcessor.calculateItemsToRecommend(itemIds, 500F, CouponBuyStrategyType.MAX_SPEND).block();
		expectedRecommendedItems = RecommendedItems.buildSucessful(List.of("MLA5"), 500F);
		assertEquals(expectedRecommendedItems, recommendedItems);
		
	}
	
}
