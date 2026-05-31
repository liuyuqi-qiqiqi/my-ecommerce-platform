package com.ecommerce.user.service;

import com.ecommerce.user.domain.Address;
import com.ecommerce.user.dto.AddressDto;
import com.ecommerce.user.dto.AddressRequest;
import com.ecommerce.user.exception.AddressNotFoundException;
import com.ecommerce.user.repository.AddressRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<AddressDto> listAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public AddressDto getAddress(Long userId, Long addressId) {
        return toDto(findOwnedAddress(userId, addressId));
    }

    @Transactional
    public AddressDto createAddress(Long userId, AddressRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        applyRequest(address, request);
        address.setCreatedAt(Instant.now());
        if (Boolean.TRUE.equals(request.isDefault()) || addressRepository.countByUserId(userId) == 0) {
            clearDefault(userId);
            address.setIsDefault(true);
        }
        Address saved = addressRepository.save(address);
        log.info("AUDIT address_created userId={} addressId={}", userId, saved.getId());
        return toDto(saved);
    }

    @Transactional
    public AddressDto updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = findOwnedAddress(userId, addressId);
        applyRequest(address, request);
        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefault(userId);
            address.setIsDefault(true);
        }
        Address saved = addressRepository.save(address);
        log.info("AUDIT address_updated userId={} addressId={}", userId, saved.getId());
        return toDto(saved);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = findOwnedAddress(userId, addressId);
        addressRepository.delete(address);
        log.info("AUDIT address_deleted userId={} addressId={}", userId, addressId);
    }

    private Address findOwnedAddress(Long userId, Long addressId) {
        return addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
    }

    private void applyRequest(Address address, AddressRequest request) {
        address.setRecipientName(request.recipientName().trim());
        address.setPhone(request.phone().trim());
        address.setProvince(request.province().trim());
        address.setCity(request.city().trim());
        address.setDistrict(request.district().trim());
        address.setStreet(request.street().trim());
        address.setPostalCode(request.postalCode().trim());
    }

    private void clearDefault(Long userId) {
        addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).forEach(address -> {
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                address.setIsDefault(false);
                addressRepository.save(address);
            }
        });
    }

    private AddressDto toDto(Address address) {
        return new AddressDto(
                address.getId(),
                address.getRecipientName(),
                address.getPhone(),
                address.getProvince(),
                address.getCity(),
                address.getDistrict(),
                address.getStreet(),
                address.getPostalCode(),
                Boolean.TRUE.equals(address.getIsDefault()));
    }
}
