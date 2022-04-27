package com.mercadolibre.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Value;

/**
 * Entity (Aggregation Root) that represents the status of an items 
 * purchase recommendation.
 * 
 * @author Johan Ballesteros
 * @since 1.1.0
 */
@Value
public class RecommendedItems {

	@JsonAlias("items_id") 
	private List<String> itemsId;
	
	/** Total price of recommended items. */
	private Float total;
	
}
