package com.retail.ecommerce_backend.listener;

import com.retail.ecommerce_backend.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j // Pour les logs (conseil : toujours logger les envois Kafka)
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order-events";
    // 🔒 L'annotation magique : la méthode ne s'exécute que si la transaction est commitée avec succès
    // fallbackExecution = true signifie qu'elle s'exécute même si l'événement est publié hors transaction (pour sécurité)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // On envoie l'événement dans le topic Kafka.
        // La clé est l'ID de la commande (permet d'avoir le partitionnement par clé).
        log.info("🟢 Envoi de l'événement OrderPlacedEvent vers Kafka pour l'orderId : {}", event.orderId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event);
        // Note : Le send est asynchrone. En cas d'erreur, Spring Kafka retentera automatiquement 
        // grâce à 'retries: 10' dans la config.
    }
}
