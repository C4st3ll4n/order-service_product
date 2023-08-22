package br.com.ph.orderservice.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
        Long id, String skuCode, String name, Integer quantity, BigDecimal price
) {
}
