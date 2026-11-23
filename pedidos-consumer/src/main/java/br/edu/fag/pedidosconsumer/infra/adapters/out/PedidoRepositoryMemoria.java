package br.edu.fag.pedidosconsumer.infra.adapters.out;

import br.edu.fag.pedidosconsumer.application.ports.out.PedidoRepositoryPort;
import br.edu.fag.pedidosconsumer.domain.Pedido;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PedidoRepositoryMemoria implements PedidoRepositoryPort {

    private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();

    @Override
    public Optional<Pedido> buscar(String orderId) {
        return Optional.ofNullable(pedidos.get(orderId));
    }

    @Override
    public void salvar(Pedido pedido) {
        pedidos.put(pedido.getOrderId(), pedido);
    }
}
