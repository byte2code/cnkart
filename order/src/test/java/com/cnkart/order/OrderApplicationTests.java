package com.cnkart.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:tc:mysql:8.0.32:///order_service",
		"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
		"spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false"
})
@org.testcontainers.junit.jupiter.Testcontainers
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
class OrderApplicationTests {

	@org.springframework.beans.factory.annotation.Autowired
	private org.springframework.test.web.servlet.MockMvc mockMvc;

	@org.springframework.beans.factory.annotation.Autowired
	private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

	@org.testcontainers.junit.jupiter.Container
	static org.testcontainers.containers.KafkaContainer kafka = 
		new org.testcontainers.containers.KafkaContainer(org.testcontainers.utility.DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

	@org.springframework.test.context.DynamicPropertySource
	static void kafkaProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}

	@Test
	void shouldFallbackOrder() throws Exception {
		com.cnkart.order.dto.OrderRequest request = new com.cnkart.order.dto.OrderRequest();
		request.setSkuCode("123");
		request.setPrice(java.math.BigDecimal.valueOf(1500));
		request.setQuantity(1);
		request.setIdempotencyKey("test-key");

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/order")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
	}

	@Test
	void contextLoads() {
	}

}
