package br.com.ph.orderservice.dto.integration;

public record InventoryResponse(
    String skuCode, boolean inStock
) {
}
