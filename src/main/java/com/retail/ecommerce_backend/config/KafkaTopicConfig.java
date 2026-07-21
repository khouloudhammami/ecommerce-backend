package com.retail.ecommerce_backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean //Exécute cette méthode au démarrage de l'application
    public NewTopic orderTopic() {
        // On crée un topic nommé "order-events" avec 3 partitions (pour distribuer la charge)
        // et 1 réplica (car nous sommes en développement local).
        return TopicBuilder.name("order-events")
                .partitions(3)
                .replicas(1) // replica = Broker
                .build();
    }

}
