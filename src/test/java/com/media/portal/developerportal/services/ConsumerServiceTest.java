package com.media.portal.developerportal.services;

import com.media.portal.developerportal.model.Consumer;
import com.media.portal.developerportal.repositories.ConsumerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @InjectMocks
    private ConsumerService consumerService;

    @Test
    void createConsumer_returnsSavedId() {
        var consumer = new Consumer();
        consumer.setName("Ada Lovelace");
        consumer.setEmail("ada@example.com");
        consumer.setOrganisation("Analytical Engines Inc");

        var saved = mock(Consumer.class);
        when(saved.getId()).thenReturn(42L);
        when(consumerRepository.save(consumer)).thenReturn(saved);

        var id = consumerService.createConsumer(consumer);

        assertThat(id).isEqualTo(42L);
        verify(consumerRepository).save(consumer);
    }

    @Test
    void getConsumer_returnsConsumer_whenFound() {
        var consumer = new Consumer();
        consumer.setName("Ada Lovelace");
        when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));

        var result = consumerService.getConsumer(1L);

        assertThat(result).isSameAs(consumer);
    }

    @Test
    void getConsumer_throwsResourceNotFoundException_whenMissing() {
        when(consumerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> consumerService.getConsumer(99L));
    }
}
