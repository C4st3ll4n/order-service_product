package br.com.ph.orderservice.event;

public record OrderPlacedEvent(
        String orderNumber
) {
}
