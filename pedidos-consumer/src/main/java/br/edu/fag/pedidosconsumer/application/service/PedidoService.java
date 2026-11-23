package br.edu.fag.pedidosconsumer.application.service;

import br.edu.fag.pedidosconsumer.application.ports.in.PedidoServicePort;
import br.edu.fag.pedidosconsumer.application.ports.out.PedidoRepositoryPort;
import br.edu.fag.pedidosconsumer.domain.Pedido;
import br.edu.fag.pedidosconsumer.domain.StatusPedido;
import br.edu.fag.pedidosconsumer.domain.TransicaoInvalidaException;
import br.edu.fag.pedidosconsumer.dto.PedidoEventoDTO;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PedidoService implements PedidoServicePort {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepositoryPort repository;

    public PedidoService(PedidoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void process(PedidoEventoDTO e) {
        switch (e.eventType()) {
            case "PedidoEfetuado" -> criar(e);
            case "PedidoPago" -> transicionar(e, p -> p.pagar(e.amount()));
            case "PedidoEnviado" -> transicionar(e, Pedido::enviar);
            case "PedidoCancelado" -> transicionar(e, Pedido::cancelar);
            default -> throw new IllegalArgumentException("eventType desconhecido: " + e.eventType());
        }
    }

    private void criar(PedidoEventoDTO e) {
        if (repository.buscar(e.orderId()).isPresent()) {
            log.info("Pedido {} ja existe, evento ignorado", e.orderId());
            return;
        }
        Pedido pedido = new Pedido(e.orderId(), e.total(), e.currency());
        repository.salvar(pedido);
        log.info("Pedido {}: criado ({}) | total: {} {}", pedido.getOrderId(), pedido.getStatus(),
                pedido.getTotal(), pedido.getCurrency());
    }

    private void transicionar(PedidoEventoDTO e, Consumer<Pedido> acao) {
        Pedido pedido = repository.buscar(e.orderId())
                .orElseThrow(() -> new TransicaoInvalidaException("Pedido nao encontrado: " + e.orderId()));
        StatusPedido de = pedido.getStatus();
        acao.accept(pedido);
        log.info("Pedido {}: {} -> {}", pedido.getOrderId(), de, pedido.getStatus());
    }
}
