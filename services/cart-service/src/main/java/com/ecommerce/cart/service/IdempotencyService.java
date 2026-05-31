package com.ecommerce.cart.service;

import com.ecommerce.cart.domain.ProcessedEvent;
import com.ecommerce.cart.repository.ProcessedEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public boolean registerIfNew(String eventId) {
        if (processedEventRepository.existsById(eventId)) {
            return false;
        }
        try {
            ProcessedEvent event = new ProcessedEvent();
            event.setEventId(eventId);
            processedEventRepository.save(event);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }
}
