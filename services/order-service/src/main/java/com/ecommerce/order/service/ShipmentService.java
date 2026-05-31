package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.Shipment;
import com.ecommerce.order.dto.ShipmentInfoDto;
import org.springframework.stereotype.Component;

@Component
public class ShipmentService {

    public ShipmentInfoDto toInfo(Shipment shipment) {
        if (shipment == null) {
            return null;
        }
        return new ShipmentInfoDto(
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getStatus().name(),
                shipment.getStatusUpdatedAt());
    }

    public ShipmentInfoDto getForOrder(Order order) {
        return toInfo(order.getShipment());
    }
}
