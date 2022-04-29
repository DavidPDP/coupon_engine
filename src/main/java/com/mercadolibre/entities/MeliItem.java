package com.mercadolibre.entities;

import lombok.Data;

@Data
public class MeliItem {

	private String id;
	private Float price;
	
	public static MeliItem buildEmpty() {
		var item = new MeliItem();
		item.setId("");
		item.setPrice(0F);
		return item;
	}
	
	public static MeliItem buildItem(String id, Float price) {
		var item = new MeliItem();
		item.setId(id);
		item.setPrice(price);
		return item;
	}
	
}
