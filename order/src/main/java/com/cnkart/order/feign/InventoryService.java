package com.cnkart.order.feign;

import com.cnkart.order.dto.InventoryReservationRequest;
import com.cnkart.order.dto.InventoryReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="INVENTORY-SERVICE")
public interface InventoryService {

	
	 	@GetMapping("/api/inventory?skuCode={skuCode}&qty={qty}")
	    public boolean isInStock(@RequestParam Long skuCode,@RequestParam Integer qty);

        @PostMapping("/api/inventory/reservations")
        InventoryReservationResponse reserveStock(@RequestBody InventoryReservationRequest request);
}
