package com.ecommerce.cart.service;

import com.ecommerce.cart.client.ProductServiceClient;
import com.ecommerce.cart.client.ProductServiceClient.ProductDetailDto;
import com.ecommerce.cart.domain.Cart;
import com.ecommerce.cart.domain.CartItem;
import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.cart.guest.GuestCartService;
import com.ecommerce.cart.guest.GuestCartService.GuestCartItem;
import com.ecommerce.cart.repository.CartRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final GuestCartService guestCartService;
    private final ProductServiceClient productServiceClient;

    public CartService(
            CartRepository cartRepository,
            GuestCartService guestCartService,
            ProductServiceClient productServiceClient) {
        this.cartRepository = cartRepository;
        this.guestCartService = guestCartService;
        this.productServiceClient = productServiceClient;
    }

    @Transactional
    public CartDto getCart(Long userId, String guestSessionId) {
        if (userId != null) {
            mergeGuestCartIfNeeded(userId, guestSessionId);
            return buildUserCart(getOrCreateUserCart(userId));
        }
        return buildGuestCart(guestSessionId);
    }

    @Transactional
    public CartDto addItem(Long userId, String guestSessionId, AddCartItemRequest request) {
        ProductDetailDto product = fetchProduct(request.productId());
        validateQuantity(request.quantity(), product.stockQuantity());

        if (userId != null) {
            mergeGuestCartIfNeeded(userId, guestSessionId);
            Cart cart = getOrCreateUserCart(userId);
            Optional<CartItem> existing = cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(request.productId()))
                    .findFirst();
            if (existing.isPresent()) {
                CartItem item = existing.get();
                int newQty = Math.min(item.getQuantity() + request.quantity(), product.stockQuantity());
                item.setQuantity(newQty);
                item.setUnitPrice(product.price());
                item.setProductName(product.name());
            } else {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProductId(product.id());
                item.setProductName(product.name());
                item.setUnitPrice(product.price());
                item.setQuantity(Math.min(request.quantity(), product.stockQuantity()));
                item.setCreatedAt(Instant.now());
                cart.getItems().add(item);
            }
            cart.setUpdatedAt(Instant.now());
            cartRepository.save(cart);
            return buildUserCart(cart);
        }

        List<GuestCartItem> items = new ArrayList<>(guestCartService.getItems(guestSessionId));
        Optional<GuestCartItem> existingGuest = items.stream()
                .filter(item -> item.productId().equals(request.productId()))
                .findFirst();
        if (existingGuest.isPresent()) {
            int index = items.indexOf(existingGuest.get());
            GuestCartItem current = existingGuest.get();
            int newQty = Math.min(current.quantity() + request.quantity(), product.stockQuantity());
            items.set(index, new GuestCartItem(product.id(), product.name(), product.price(), newQty));
        } else {
            items.add(new GuestCartItem(
                    product.id(), product.name(), product.price(), Math.min(request.quantity(), product.stockQuantity())));
        }
        guestCartService.saveItems(guestSessionId, items);
        return buildGuestCart(guestSessionId);
    }

    @Transactional
    public CartDto updateItem(Long userId, String guestSessionId, Long productId, UpdateCartItemRequest request) {
        ProductDetailDto product = fetchProduct(productId);
        validateQuantity(request.quantity(), product.stockQuantity());

        if (userId != null) {
            mergeGuestCartIfNeeded(userId, guestSessionId);
            Cart cart = getOrCreateUserCart(userId);
            CartItem item = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + productId));
            item.setQuantity(Math.min(request.quantity(), product.stockQuantity()));
            item.setUnitPrice(product.price());
            item.setProductName(product.name());
            cart.setUpdatedAt(Instant.now());
            cartRepository.save(cart);
            return buildUserCart(cart);
        }

        List<GuestCartItem> items = new ArrayList<>(guestCartService.getItems(guestSessionId));
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).productId().equals(productId)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("Cart item not found: " + productId);
        }
        items.set(
                index,
                new GuestCartItem(
                        product.id(),
                        product.name(),
                        product.price(),
                        Math.min(request.quantity(), product.stockQuantity())));
        guestCartService.saveItems(guestSessionId, items);
        return buildGuestCart(guestSessionId);
    }

    @Transactional
    public CartDto removeItem(Long userId, String guestSessionId, Long productId) {
        if (userId != null) {
            mergeGuestCartIfNeeded(userId, guestSessionId);
            Cart cart = getOrCreateUserCart(userId);
            cart.getItems().removeIf(item -> item.getProductId().equals(productId));
            cart.setUpdatedAt(Instant.now());
            cartRepository.save(cart);
            return buildUserCart(cart);
        }

        List<GuestCartItem> items = guestCartService.getItems(guestSessionId).stream()
                .filter(item -> !item.productId().equals(productId))
                .toList();
        guestCartService.saveItems(guestSessionId, items);
        return buildGuestCart(guestSessionId);
    }

    @Transactional
    public void mergeGuestCartIfNeeded(Long userId, String guestSessionId) {
        if (guestSessionId == null || guestSessionId.isBlank()) {
            return;
        }
        List<GuestCartItem> guestItems = guestCartService.getItems(guestSessionId);
        if (guestItems.isEmpty()) {
            return;
        }

        Cart cart = getOrCreateUserCart(userId);
        for (GuestCartItem guestItem : guestItems) {
            try {
                ProductDetailDto product = fetchProduct(guestItem.productId());
                Optional<CartItem> existing = cart.getItems().stream()
                        .filter(item -> item.getProductId().equals(guestItem.productId()))
                        .findFirst();
                int mergedQty = Math.min(
                        existing.map(i -> i.getQuantity() + guestItem.quantity()).orElse(guestItem.quantity()),
                        product.stockQuantity());
                if (existing.isPresent()) {
                    CartItem item = existing.get();
                    item.setQuantity(mergedQty);
                    item.setUnitPrice(product.price());
                    item.setProductName(product.name());
                } else if (mergedQty > 0) {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProductId(product.id());
                    item.setProductName(product.name());
                    item.setUnitPrice(product.price());
                    item.setQuantity(mergedQty);
                    item.setCreatedAt(Instant.now());
                    cart.getItems().add(item);
                }
            } catch (IllegalArgumentException ignored) {
                // skip invalid products during merge
            }
        }
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        guestCartService.clear(guestSessionId);
    }

    @Transactional
    public void clearUserCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setUpdatedAt(Instant.now());
            cartRepository.save(cart);
        });
    }

    private Cart getOrCreateUserCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setUpdatedAt(Instant.now());
            return cartRepository.save(cart);
        });
    }

    private CartDto buildUserCart(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream().map(this::toDtoWithAvailability).toList();
        return summarize(items);
    }

    private CartDto buildGuestCart(String guestSessionId) {
        List<CartItemDto> items = guestCartService.getItems(guestSessionId).stream()
                .map(this::guestToDtoWithAvailability)
                .toList();
        return summarize(items);
    }

    private CartItemDto toDtoWithAvailability(CartItem item) {
        return buildItemDto(item.getProductId(), item.getProductName(), item.getUnitPrice(), item.getQuantity());
    }

    private CartItemDto guestToDtoWithAvailability(GuestCartItem item) {
        return buildItemDto(item.productId(), item.productName(), item.unitPrice(), item.quantity());
    }

    private CartItemDto buildItemDto(Long productId, String name, BigDecimal unitPrice, int quantity) {
        boolean available = false;
        try {
            ProductDetailDto product = fetchProduct(productId);
            available = product.inStock() && product.stockQuantity() >= quantity;
            name = product.name();
            unitPrice = product.price();
        } catch (IllegalArgumentException ignored) {
            available = false;
        }
        BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return new CartItemDto(productId, name, unitPrice, quantity, lineSubtotal, available);
    }

    private CartDto summarize(List<CartItemDto> items) {
        BigDecimal subtotal =
                items.stream().map(CartItemDto::lineSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = items.stream().mapToInt(CartItemDto::quantity).sum();
        return new CartDto(items, subtotal, itemCount);
    }

    private ProductDetailDto fetchProduct(Long productId) {
        try {
            ProductDetailDto product = productServiceClient.getProductById(productId);
            if (!product.inStock() && product.stockQuantity() <= 0) {
                throw new IllegalArgumentException("Product out of stock: " + productId);
            }
            return product;
        } catch (FeignException.NotFound ex) {
            throw new IllegalArgumentException("Invalid product: " + productId);
        }
    }

    private void validateQuantity(int quantity, int stockQuantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (stockQuantity < 1) {
            throw new IllegalArgumentException("Product is out of stock");
        }
    }
}
