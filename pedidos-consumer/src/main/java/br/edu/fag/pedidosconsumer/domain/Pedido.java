package br.edu.fag.pedidosconsumer.domain;

import java.math.BigDecimal;

public class Pedido {

    private final String orderId;
    private final BigDecimal total;
    private final String currency;
    private StatusPedido status = StatusPedido.CRIADO;

    public Pedido(String orderId, BigDecimal total, String currency) {
        this.orderId = orderId;
        this.total = total;
        this.currency = currency;
    }

    public void pagar(BigDecimal amount) {
        exigir(StatusPedido.PAGO, StatusPedido.CRIADO);
        if (amount == null || amount.compareTo(total) != 0) {
            throw new TransicaoInvalidaException(
                    "Pedido " + orderId + ": amount " + amount + " diferente do total " + total);
        }
        status = StatusPedido.PAGO;
    }

    public void enviar() {
        exigir(StatusPedido.ENVIADO, StatusPedido.PAGO);
        status = StatusPedido.ENVIADO;
    }

    public void cancelar() {
        exigir(StatusPedido.CANCELADO, StatusPedido.CRIADO, StatusPedido.PAGO);
        status = StatusPedido.CANCELADO;
    }

    private void exigir(StatusPedido destino, StatusPedido... permitidos) {
        for (StatusPedido s : permitidos) {
            if (status == s) {
                return;
            }
        }
        throw new TransicaoInvalidaException(
                "Pedido " + orderId + ": transicao invalida " + status + " -> " + destino);
    }

    public String getOrderId() { return orderId; }
    public BigDecimal getTotal() { return total; }
    public String getCurrency() { return currency; }
    public StatusPedido getStatus() { return status; }
}
