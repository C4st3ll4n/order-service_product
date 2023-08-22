package br.com.ph.orderservice.config;

import br.com.ph.orderservice.event.OrderPlacedEvent;
import br.com.ph.orderservice.service.IMessageProducer;
import br.com.ph.orderservice.service.KafkaMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class MessagerConfig {

    private final KafkaTemplate<String, OrderPlacedEvent> template;

    public MessagerConfig(KafkaTemplate<String, OrderPlacedEvent> template) {
        this.template = template;
    }

    @Bean
    public IMessageProducer messageProducer(){
        return new KafkaMessageProducer(this.template);
    }
}
