package br.com.ph.orderservice.service;

import br.com.ph.orderservice.dto.OrderItemDTO;
import br.com.ph.orderservice.dto.OrderRequest;
import br.com.ph.orderservice.dto.integration.InventoryResponse;
import br.com.ph.orderservice.event.OrderPlacedEvent;
import br.com.ph.orderservice.model.Order;
import br.com.ph.orderservice.model.OrderItem;
import br.com.ph.orderservice.repository.OrderRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@Observed(name = "order-serivce")

public class OrderService {
    private final OrderRepository repository;
    private final WebClient.Builder webClient;
    private final IMessageProducer messageProducer;
    private final Environment environment;

    public boolean place(OrderRequest request) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderItem> orderList = request.orderItemDTOList().stream().map(
                this::mapToDTO
        ).toList();

        order.setOrdemItemList(orderList);

        // Chama o serviço de inventário and cria uma ordem se houver estoque

        String orderUrl = environment.getProperty("inventory.url");

        List<String> skus = order.getOrdemItemList().stream().map(OrderItem::getSkuCode).toList();
        log.info("Calling {} for stock", orderUrl);

        var inventoryResponse = webClient.build()
                .get()
                .uri(orderUrl + "/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skus).build())
                .retrieve().
                bodyToMono(InventoryResponse[].class).
                block();

        if (inventoryResponse.length == 0) {
            throw new IllegalArgumentException("Product not found in stock");
        }

        log.info("Response for /inventory:{}", Arrays.stream(inventoryResponse).toList());

        boolean allInStock = Arrays.stream(inventoryResponse).allMatch(InventoryResponse::inStock);

        if (allInStock) {
            repository.save(order);
            messageProducer.sendMessage(new OrderPlacedEvent(order.getOrderNumber()));
            return true;
        } else {
            throw new IllegalArgumentException("Product not in stock");
        }

    }

    private OrderItem mapToDTO(OrderItemDTO orderItemDTO) {
        OrderItem orderItem = new OrderItem();

        orderItem.setName(orderItemDTO.name());
        orderItem.setSkuCode(orderItemDTO.skuCode());
        orderItem.setQuantity(orderItemDTO.quantity());
        orderItem.setPrice(orderItemDTO.price());

        return orderItem;
    }
}
