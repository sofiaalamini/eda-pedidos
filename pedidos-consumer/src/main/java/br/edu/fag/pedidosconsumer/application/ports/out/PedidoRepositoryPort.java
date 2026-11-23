package br.edu.fag.pedidosconsumer.application.ports.out;

import br.edu.fag.pedidosconsumer.domain.Pedido;
import java.util.Optional;

public interface PedidoRepositoryPort {

    Optional<Pedido> buscar(String orderId);

    void salvar(Pedido pedido);
}
