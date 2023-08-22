package br.com.ph.orderservice.service;

import br.com.ph.orderservice.event.OrderPlacedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageProducer implements IMessageProducer {

    private final KafkaTemplate<String, OrderPlacedEvent> template;

    public KafkaMessageProducer(KafkaTemplate<String, OrderPlacedEvent> template) {
        this.template = template;
    }

    public void sendMessage(OrderPlacedEvent message){
        template.send("notification",message);
    }
}
