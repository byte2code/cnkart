package com.cnkart.inventory.integration;

import com.cnkart.inventory.dto.InventoryReservationRequest;
import com.cnkart.inventory.dto.InventoryReservationResponse;
import com.cnkart.inventory.model.Inventory;
import com.cnkart.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class InventoryReservationIntegrationTest {

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

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
    }

    @Test
    void reservationSucceeds() {
        // save an Inventory row with skuCode="SKU1" quantity=10
        Inventory inv = new Inventory();
        inv.setSkuCode("SKU1");
        inv.setQuantity(10);
        inventoryRepository.save(inv);

        // POST an InventoryReservationRequest to /api/inventory/reservations with quantity 3
        InventoryReservationRequest request = new InventoryReservationRequest("ORD-1", "SKU1", 3);
        ResponseEntity<InventoryReservationResponse> response = restTemplate.postForEntity(
                "/api/inventory/reservations", request, InventoryReservationResponse.class);

        // assert response reserved=true
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isReserved());

        // assert DB quantity is now 7
        Inventory updatedInv = inventoryRepository.findBySkuCode("SKU1").orElseThrow(() -> new RuntimeException("Not found"));
        assertEquals(7, updatedInv.getQuantity());
    }

    @Test
    void reservationFailsWhenInsufficientStock() {
        // save Inventory with quantity=1
        Inventory inv = new Inventory();
        inv.setSkuCode("SKU2");
        inv.setQuantity(1);
        inventoryRepository.save(inv);

        // POST reservation with quantity=5
        InventoryReservationRequest request = new InventoryReservationRequest("ORD-2", "SKU2", 5);
        ResponseEntity<InventoryReservationResponse> response = restTemplate.postForEntity(
                "/api/inventory/reservations", request, InventoryReservationResponse.class);

        // assert reserved=false
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertFalse(response.getBody().isReserved());

        // assert DB quantity still 1
        Inventory updatedInv = inventoryRepository.findBySkuCode("SKU2").orElseThrow(() -> new RuntimeException("Not found"));
        assertEquals(1, updatedInv.getQuantity());
    }
}
