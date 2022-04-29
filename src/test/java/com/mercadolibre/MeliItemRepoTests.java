package com.mercadolibre;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.mercadolibre.repositories.MeliItemRepo;

@SpringBootTest
public class MeliItemRepoTests {

	@Autowired
	private MeliItemRepo meliItemRepo;
	
	private static ClientAndServer mockServer;
	
	@BeforeAll
	static void startServer() {
		mockServer = ClientAndServer.startClientAndServer(9100);
	}
	
	@AfterAll
	static void stopServer() {
		mockServer.stop();
	}
	
	@DynamicPropertySource
    static void registerMeliAPIProperties(DynamicPropertyRegistry registry) {
        registry.add("meli.items.api.url", () -> "http://localhost:9100");
        registry.add("meli.items.api.paging", () -> "2");
        registry.add("resilience4j.timelimiter.configs.default.timeout-duration", () -> "20s");
    }
	
	@Test
	void fetch_meli_items_and_transform_in_map() throws Exception {
		
		// Mock.
		var itemIds1 = List.of("MCO613846300", "MCO808833794");
		var responseBody1 = Files.readString(Path.of("src/test/resources/meli_items_p1.json"));
		mockServer.when(request().withMethod("GET").withQueryStringParameter("ids", String.join(",", itemIds1)))
	    	.respond(response().withStatusCode(200).withBody(responseBody1, MediaType.APPLICATION_JSON));
		
		var itemIds2 = List.of("MCO808833795", "MCO808833796");
		var responseBody2 = Files.readString(Path.of("src/test/resources/meli_items_p2.json"));
		mockServer.when(request().withMethod("GET").withQueryStringParameter("ids", String.join(",", itemIds2)))
	    	.respond(response().withStatusCode(200).withBody(responseBody2, MediaType.APPLICATION_JSON));
		
		var itemIds3 = List.of("MCO808833797", "MCO808833798");
		var responseBody3 = Files.readString(Path.of("src/test/resources/meli_items_p3.json"));
		mockServer.when(request().withMethod("GET").withQueryStringParameter("ids", String.join(",", itemIds3)))
    		.respond(response().withStatusCode(200).withBody(responseBody3, MediaType.APPLICATION_JSON));
		
		var responseBody4 = Files.readString(Path.of("src/test/resources/meli_items_p4.json"));
		mockServer.when(request().withMethod("GET").withQueryStringParameter("ids", "MCO808833797"))
    		.respond(response().withStatusCode(200).withBody(responseBody4, MediaType.APPLICATION_JSON));
		
		// Scenario 1: Fetch 2 items (happy path).
		var fetchedItems = meliItemRepo.fecthMeliItems(itemIds1);
		var expectedItems = Map.of("MCO613846300", 9238F, "MCO808833794", 348900F);
		assertTrue(expectedItems.equals(fetchedItems.block()));
		System.out.println(fetchedItems);
		
		// Scenario 2: Fetch more than paging limit (current 2).
		var itemIds = List.of("MCO613846300", "MCO808833794", "MCO808833795", "MCO808833796", "MCO808833797", "MCO808833798");
		fetchedItems = meliItemRepo.fecthMeliItems(itemIds);
		expectedItems = Map.of("MCO808833798", 86900F, "MCO808833796", 36950F, "MCO808833795", 1277000F, "MCO808833794", 348900F, "MCO613846300", 9238F);
		assertTrue(expectedItems.equals(fetchedItems.block()));
		
		// Scenario 3: Fetch with odd paging limit
		itemIds = List.of("MCO613846300", "MCO808833794", "MCO808833795", "MCO808833796", "MCO808833797");
		fetchedItems = meliItemRepo.fecthMeliItems(itemIds);
		expectedItems = Map.of("MCO808833796", 36950F, "MCO808833795", 1277000F, "MCO808833794", 348900F, "MCO613846300", 9238F);
		assertTrue(expectedItems.equals(fetchedItems.block()));
		
	}
	
}
