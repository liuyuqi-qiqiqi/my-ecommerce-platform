package com.ecommerce.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.cart.client.ProductServiceClient;
import com.ecommerce.cart.domain.Cart;
import com.ecommerce.cart.domain.CartItem;
import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.guest.GuestCartService;
import com.ecommerce.cart.guest.GuestCartService.GuestCartItem;
import com.ecommerce.cart.repository.CartRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private GuestCartService guestCartService;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartService cartService;

    @Test
    void mergeGuestCartCombinesQuantitiesForSameSku() {
        Long userId = 1L;
        String guestSession = "guest-1";
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setUpdatedAt(Instant.now());
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProductId(10L);
        existing.setProductName("Phone");
        existing.setUnitPrice(BigDecimal.TEN);
        existing.setQuantity(1);
        existing.setCreatedAt(Instant.now());
        cart.getItems().add(existing);

        when(guestCartService.getItems(guestSession))
                .thenReturn(List.of(new GuestCartItem(10L, "Phone", BigDecimal.TEN, 2)));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productServiceClient.getProductById(10L))
                .thenReturn(new ProductServiceClient.ProductDetailDto(
                        10L, "Phone", BigDecimal.TEN, "Brand", "img", true, "desc", "Cat", 5));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.mergeGuestCartIfNeeded(userId, guestSession);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
        verify(guestCartService).clear(guestSession);
    }

    @Test
    void addItemForGuestStoresItemsInGuestCart() {
        when(productServiceClient.getProductById(5L))
                .thenReturn(new ProductServiceClient.ProductDetailDto(
                        5L, "Watch", BigDecimal.valueOf(199), "Brand", "img", true, "desc", "Cat", 3));
        when(guestCartService.getItems("guest-2")).thenReturn(new ArrayList<>());

        CartDto cart = cartService.addItem(null, "guest-2", new AddCartItemRequest(5L, 1));

        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().get(0).productName()).isEqualTo("Watch");
        verify(guestCartService).saveItems(eq("guest-2"), any());
    }
}
