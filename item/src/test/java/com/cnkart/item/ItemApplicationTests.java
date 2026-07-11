package com.cnkart.item;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:tc:mysql:8.0.32:///item_service",
		"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
		"spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false"
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
class ItemApplicationTests {

	@org.springframework.beans.factory.annotation.Autowired
	private org.springframework.test.web.servlet.MockMvc mockMvc;

	@org.springframework.beans.factory.annotation.Autowired
	private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

	@Test
	void shouldCreateItem() throws Exception {
		com.cnkart.item.dto.ItemRequest request = com.cnkart.item.dto.ItemRequest.builder()
				.name("Laptop")
				.description("Gaming Laptop")
				.price(java.math.BigDecimal.valueOf(1500))
				.build();
				
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/item")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
	}

	@Test
	void contextLoads() {
	}

}
