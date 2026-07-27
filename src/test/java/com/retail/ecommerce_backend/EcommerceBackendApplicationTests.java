package com.retail.ecommerce_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
class EcommerceBackendApplicationTests {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void contextLoads() {
        // En utilisant H2Dialect, Hibernate n'ajoutera plus 'returning id' à la fin du INSERT
    }
}