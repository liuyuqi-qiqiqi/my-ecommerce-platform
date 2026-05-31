package com.ecommerce.bff.web;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.bff.client.OrderServiceClient;
import com.ecommerce.bff.dto.OrderPageDto;
import com.ecommerce.bff.dto.OrderSummaryDto;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderController.class)
class OpenApiContractTest {

    private static String openApiSpecPath;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderServiceClient orderServiceClient;

    @BeforeAll
    static void resolveSpecPath() {
        openApiSpecPath = Path.of("..", "specs", "001-ecommerce-platform", "contracts", "shop-bff-api.yaml")
                .toAbsolutePath()
                .normalize()
                .toString();
    }

    @Test
    void listOrdersMatchesOpenApiContract() throws Exception {
        when(orderServiceClient.listOrders(eq(1L), anyInt(), anyInt()))
                .thenReturn(new OrderPageDto(
                        List.of(new OrderSummaryDto(
                                1L, "ORD-20260531-0001", "CONFIRMED", BigDecimal.valueOf(109), Instant.parse("2026-05-31T10:00:00Z"))),
                        1,
                        10,
                        1));

        mockMvc.perform(get("/orders").header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(openApiSpecPath));
    }
}
