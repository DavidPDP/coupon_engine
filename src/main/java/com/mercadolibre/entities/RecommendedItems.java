package com.mercadolibre.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Value;

@Value
public class RecommendedItems {

	@JsonAlias("items_id") 
	private List<String> itemsId;
	
	private Float total;
	
}
