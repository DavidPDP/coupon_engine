package com.mercadolibre.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mercadolibre.entities.MeliItem;

@Component
public class MaxSpendStrategy implements CouponBuyStrategy {

	@Override
	public List<String> calculate(Map<String, Float> items, Float amount) {
		
		// Convert Map into Array.
		var itemsArray = items.entrySet().stream()
				.map(i -> { return MeliItem.buildItem(i.getKey(), i.getValue()); } )
				.toArray(MeliItem[]::new);
		
		// Execute with dynamic programming (NP problem). 
		var selectedItemsIndex = new boolean[items.size()];
		maxSpend(items.size(), itemsArray, amount, selectedItemsIndex);
		
		// Select result.
		List<String> result = new ArrayList<>();
		for(int i=0; i < selectedItemsIndex.length; i ++)
			if(selectedItemsIndex[i])
				result.add(itemsArray[i].getId());
		
		return result;
		
	}
	
	private float maxSpend(int currentIndex, MeliItem[] items, float couponLimit, boolean[] selected) {
		
		if(couponLimit <= 0 || currentIndex == 0)
	        return 0F;

		else if (items[currentIndex - 1].getPrice() > couponLimit)
	    	return maxSpend(currentIndex - 1, items, couponLimit, selected);

	    else {
	    	
	    	// Create selected copies to paths.
	    	boolean[] sl1 = new boolean[selected.length];
	        System.arraycopy(selected, 0, sl1, 0, sl1.length);
	        boolean[] sl2 = new boolean[selected.length];
	        System.arraycopy(selected, 0, sl2, 0, sl2.length);
	        sl1[currentIndex - 1] = true;
	    	
	    	// Right path = taken item.
	    	var rp = items[currentIndex - 1].getPrice() + maxSpend(currentIndex - 1, items, (couponLimit - items[currentIndex - 1].getPrice()), sl1);
	    	
	    	// Left Path = avoid item.
	    	var lp = maxSpend(currentIndex - 1, items, couponLimit, sl2);
	    	
	    	// Select max set item value (if equals select right path).
	    	if(rp > lp) {
	    		System.arraycopy(sl1, 0, selected, 0, sl1.length);
	    		return rp;
	    	} else {
	    		System.arraycopy(sl2, 0, selected, 0, sl2.length);
	    		return lp;
	    	}
	        
	    }
		
	}

	

}
