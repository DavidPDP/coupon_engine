package com.mercadolibre.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

import com.mercadolibre.entities.MeliItem;

import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository (port) that encapsulates the complexity of 
 * obtaining the MELI Items.
 *  
 * @author Johan Ballesteros
 * @since 1.1.0
 */
@Repository
public class MeliItemRepo {
	
	private WebClient meliItemAdapter;
	private ReactiveCircuitBreaker fecthMeliItemsCB;
	
	/** Pagination, represents the limit of items that can be queried in a single request. */
	private int pagingLimit;
	
	public MeliItemRepo(WebClient.Builder webClientBuilder, 
			ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> cBFactory, 
			Environment env) {
		meliItemAdapter = webClientBuilder
				.baseUrl(env.getRequiredProperty("meli.items.api.url"))
				.build();
		pagingLimit = env.getRequiredProperty("meli.items.api.paging", Integer.class);
		fecthMeliItemsCB = cBFactory.create("meliapi");
	}
	
	// Note: Representation of the data to deserialize.
	// Json response sintax: [ body: { id: string, price: numeric } ].
	@Data
	public static class MeliItemWrapper {
		
		private MeliItem body;
		
		public static MeliItemWrapper buildEmpty() {
			var entity = new MeliItemWrapper();
			entity.setBody(MeliItem.buildEmpty());
			return entity;
		}
		
	}
	
	
	/**
	 * Given the list of item ids, it proceeds to make requests asynchronously 
	 * (fetch MELI Item) by chunks. Merges all responses and turns them into 
	 * a Map: {k=itemId, v=itemPrice}.
	 * 
	 * @param itemIds
	 * @return Future/Promise with Map.
	 */
	public Mono<Map<String, Float>> fecthMeliItems(List<String> itemIds) {
		
		List<Flux<MeliItemWrapper>> resultPubs = new ArrayList<>();
		
		// Iteration per chunk. Chunk = pagingLimit.
		for(int i=0, j=pagingLimit; i < itemIds.size(); i+=pagingLimit, j+=pagingLimit) {
			
			if(j > itemIds.size()) j = itemIds.size(); // odd amount adjustment.
			var itemIdsChunk = itemIds.subList(i, j); // create items chunk to request.
			
			// Consume MELI items API.
			var fetchedItems = fecthMeliItemsCB.run( // circuit breaker.
				meliItemAdapter.get()
					.uri(uri -> uri.queryParam("ids", String.join(",", itemIdsChunk)).build())
					.retrieve()
					.bodyToFlux(MeliItemWrapper.class),
				throwable -> {
					System.out.println("Cogestion/Error: " + throwable.getMessage());
					return Flux.just(MeliItemWrapper.buildEmpty());
				}
			);
			
			// Merge responses.
			resultPubs.add(fetchedItems);
			
		}
		
		// Stream = merge all responses + filter null prices + collect in map
		return Flux.merge(resultPubs)
				.filter(item -> item.getBody().getPrice() != null)
				.collectMap(item -> item.getBody().getId(), item -> item.getBody().getPrice());
		
	}
	
}
