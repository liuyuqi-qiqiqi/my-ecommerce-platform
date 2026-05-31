package com.ecommerce.order.web;

import com.ecommerce.order.dto.ShipmentInfoDto;
import com.ecommerce.order.dto.UpdateShipmentRequest;
import com.ecommerce.order.service.ShipmentUpdateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/admin/shipments")
public class InternalShipmentController {

    private final ShipmentUpdateService shipmentUpdateService;

    public InternalShipmentController(ShipmentUpdateService shipmentUpdateService) {
        this.shipmentUpdateService = shipmentUpdateService;
    }

    @PutMapping("/{orderId}")
    public ShipmentInfoDto updateShipment(
            @PathVariable Long orderId, @Valid @RequestBody UpdateShipmentRequest request) {
        return shipmentUpdateService.updateShipment(orderId, request);
    }
}
