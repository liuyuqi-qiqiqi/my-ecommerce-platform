package com.ecommerce.order.service;

import com.ecommerce.order.client.dto.AddressDto;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShippingFeeCalculator {

    private final BigDecimal flatFee;
    private final BigDecimal remoteSurcharge;

    public ShippingFeeCalculator(
            @Value("${ecommerce.shipping.flat-fee:10.00}") BigDecimal flatFee,
            @Value("${ecommerce.shipping.remote-surcharge:5.00}") BigDecimal remoteSurcharge) {
        this.flatFee = flatFee;
        this.remoteSurcharge = remoteSurcharge;
    }

    public BigDecimal calculate(AddressDto address) {
        if (address.province() != null
                && (address.province().contains("新疆")
                        || address.province().contains("西藏")
                        || address.province().contains("内蒙古"))) {
            return flatFee.add(remoteSurcharge);
        }
        return flatFee;
    }
}
