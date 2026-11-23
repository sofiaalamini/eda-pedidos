package br.edu.fag.pedidosconsumer.application.ports.in;

import br.edu.fag.pedidosconsumer.dto.PedidoEventoDTO;

public interface PedidoServicePort {

    void process(PedidoEventoDTO evento);
}
