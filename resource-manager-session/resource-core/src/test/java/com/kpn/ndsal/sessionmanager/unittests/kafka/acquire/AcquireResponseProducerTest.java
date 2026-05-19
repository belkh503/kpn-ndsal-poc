package com.kpn.ndsal.sessionmanager.unittests.kafka.acquire;

import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {AcquireResponseProducer.class, KafkaProperties.class})
class AcquireResponseProducerTest {

    @Test
    void createAcquireResponseFailDtoTest() {
        var message = "errorMessage";
        var response = AcquireResponseProducer.createAcquireResponseFailDto(message);

        assertEquals(false, response.getSessionAcquired());

        assertNotNull(response.getErrorDto());
        assertNotNull(response.getErrorDto().getErrorMessage());
        assertEquals(message, response.getErrorDto().getErrorMessage());
    }
}