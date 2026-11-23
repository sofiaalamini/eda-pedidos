package br.edu.fag.pedidosconsumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoEventoDTO(
        String eventId,
        String eventType,
        String orderId,
        Instant occurredAt,
        String customerId,
        BigDecimal total,
        String currency,
        String paymentMethod,
        BigDecimal amount,
        String carrier,
        String trackingCode,
        String reason) {
}
