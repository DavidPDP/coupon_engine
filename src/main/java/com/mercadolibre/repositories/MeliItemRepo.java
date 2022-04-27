package com.mercadolibre.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MeliItemRepo {

	private WebClient meliItemPort;
	private int pagingLimit;
	
	public MeliItemRepo(WebClient.Builder webClientBuilder, Environment env) {
		meliItemPort = webClientBuilder
				.baseUrl(env.getRequiredProperty("meli.items.api.url"))
				.build();
		pagingLimit = env.getRequiredProperty("meli.items.api.paging", Integer.class);
	}
	
	@Data
	public static class MeliItemWrapper {
		private MeliItem body;
	}
	
	@Data
	public static class MeliItem {
		private String id;
		private Float price;
	}
	
	public Mono<Map<String, Float>> fecthMeliItems(List<String> itemIds) {
		
		List<Mono<List<MeliItemWrapper>>> resultPubs = new ArrayList<>();
		for(int i=0, j=pagingLimit; i < itemIds.size(); i+=pagingLimit, j+=pagingLimit) {
			
			if(j > itemIds.size()) j = itemIds.size();
			var itemIdsChunk = itemIds.subList(i, j);
			
			var fetchedItems = meliItemPort.get()
					.uri(uri -> uri.queryParam("ids", String.join(",", itemIdsChunk)).build())
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<MeliItemWrapper>>() {});
		
			resultPubs.add(fetchedItems);
			
		}
		
		return Flux.merge(resultPubs)
				.flatMapIterable(Function.identity())
				.filter(item -> item.getBody().getPrice() != null)
				.collectMap(item -> item.getBody().getId(), item -> item.getBody().getPrice());
		
	}
	
}
