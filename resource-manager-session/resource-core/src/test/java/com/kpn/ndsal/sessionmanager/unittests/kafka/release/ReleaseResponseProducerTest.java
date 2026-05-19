package com.kpn.ndsal.sessionmanager.unittests.kafka.release;

import com.kpn.ndsal.sessionmanager.kafka.release.ReleaseResponseProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {ReleaseResponseProducer.class, KafkaProperties.class})
class ReleaseResponseProducerTest {

    @Test
    void createReleaseResponseFailDtoTest() {
        var message = "errorMessage";
        var response = ReleaseResponseProducer.createReleaseResponseFailDto(message);
        assertNotNull(response.getErrorDto());
        assertNotNull(response.getErrorDto().getErrorMessage());
        assertEquals(message, response.getErrorDto().getErrorMessage());
    }
}