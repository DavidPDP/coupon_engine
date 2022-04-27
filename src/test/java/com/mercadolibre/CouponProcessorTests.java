package com.mercadolibre;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mercadolibre.core.CouponProcessor;

@SpringBootTest
class CouponProcessorTests {

	@Autowired
	CouponProcessor couponProcessor;
	
	@Test
	void buy_with_max_item_quantity_strategy() {
		
		// Scenario 1: Positive residue.
		var items = Map.of("MLA1", 100F, "MLA2", 210F, "MLA3", 260F, "MLA4", 80F, "MLA5", 90F);
		var buyedItems = couponProcessor.calculate(items, 500F);
		var expectedBuyedItems = List.of("MLA4", "MLA5", "MLA1", "MLA2");
		assertEquals(expectedBuyedItems, buyedItems);
		
		// Scenario 2: Zero residue.
		items = Map.of("MLA1", 50.87F, "MLA2", 157.98F, "MLA3", 260.2F, "MLA4", 20.9F, "MLA5", 10.05F);
		buyedItems = couponProcessor.calculate(items, 500F);
		expectedBuyedItems = List.of("MLA5", "MLA4", "MLA1", "MLA2", "MLA3");
		assertEquals(expectedBuyedItems, buyedItems);
		
		// Scenario 3: No items to buy (price over amount limit).
		items = Map.of("MLA1", 501F, "MLA2", 1590F, "MLA3", 765.89F, "MLA4", 500.01F, "MLA5", 980F);
		buyedItems = couponProcessor.calculate(items, 500F);
		expectedBuyedItems = Collections.emptyList();
		assertEquals(expectedBuyedItems, buyedItems);
		
	}
	
}
