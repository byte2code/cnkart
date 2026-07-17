package com.cnkart.order.integration;

import com.cnkart.order.dto.InventoryReservationResponse;
import com.cnkart.order.dto.OrderRequest;
import com.cnkart.order.dto.OrderResponse;
import com.cnkart.order.feign.InventoryService;
import com.cnkart.order.model.Order;
import com.cnkart.order.model.OrderStatus;
import com.cnkart.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class OrderPlacementIntegrationTest {

    @Container
    static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");

    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("eureka.client.enabled", () -> false);
    }

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void orderConfirmedWhenInventoryReserved() {
        // mock inventoryService.reserveStock
        InventoryReservationResponse mockResponse = new InventoryReservationResponse();
        mockResponse.setReserved(true);
        when(inventoryService.reserveStock(any())).thenReturn(mockResponse);

        // POST an OrderRequest
        OrderRequest request = new OrderRequest("SKU-1", new BigDecimal("100.00"), 2, "idemp-key-1");
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/api/order", request, OrderResponse.class);

        // assert response status is CONFIRMED
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(OrderStatus.CONFIRMED, response.getBody().getStatus());

        // assert orderRepository has one row with status CONFIRMED
        List<Order> orders = orderRepository.findAll();
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.CONFIRMED, orders.get(0).getStatus());
    }

    @Test
    void idempotencyKeyPreventsDoubleOrder() {
        // mock inventoryService.reserveStock
        InventoryReservationResponse mockResponse = new InventoryReservationResponse();
        mockResponse.setReserved(true);
        when(inventoryService.reserveStock(any())).thenReturn(mockResponse);

        OrderRequest request = new OrderRequest("SKU-2", new BigDecimal("50.00"), 1, "idemp-key-2");
        
        // POST the same OrderRequest twice
        ResponseEntity<OrderResponse> response1 = restTemplate.postForEntity("/api/order", request, OrderResponse.class);
        ResponseEntity<OrderResponse> response2 = restTemplate.postForEntity("/api/order", request, OrderResponse.class);

        // assert both responses return the same orderReference
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertNotNull(response1.getBody().getOrderReference());
        assertEquals(response1.getBody().getOrderReference(), response2.getBody().getOrderReference());

        // assert only one row exists in orderRepository
        List<Order> orders = orderRepository.findAll();
        assertEquals(1, orders.size());
    }
}
