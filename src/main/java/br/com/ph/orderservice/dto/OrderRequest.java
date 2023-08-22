package br.com.ph.orderservice.dto;

import java.util.List;

public record OrderRequest(
        List<OrderItemDTO> orderItemDTOList
) {
}
