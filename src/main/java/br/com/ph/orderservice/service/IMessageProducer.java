package br.com.ph.orderservice.service;

import br.com.ph.orderservice.event.OrderPlacedEvent;

public interface IMessageProducer {

    public void sendMessage(OrderPlacedEvent message);

}
