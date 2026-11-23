package br.edu.fag.pedidosconsumer.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.fag.pedidosconsumer.domain.StatusPedido;
import br.edu.fag.pedidosconsumer.domain.TransicaoInvalidaException;
import br.edu.fag.pedidosconsumer.dto.PedidoEventoDTO;
import br.edu.fag.pedidosconsumer.infra.adapters.out.PedidoRepositoryMemoria;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PedidoServiceTest {

    private PedidoRepositoryMemoria repo;
    private PedidoService service;

    @BeforeEach
    void setUp() {
        repo = new PedidoRepositoryMemoria();
        service = new PedidoService(repo);
    }

    private static PedidoEventoDTO evento(String tipo, String amount) {
        return new PedidoEventoDTO("e", tipo, "p1", null, "cliente-42", new BigDecimal("100.00"), "BRL",
                "PIX", amount == null ? null : new BigDecimal(amount), "Correios", "BR1", "motivo");
    }

    private StatusPedido status() {
        return repo.buscar("p1").orElseThrow().getStatus();
    }

    @Test
    void sequenciaValidaChegaEmEnviado() {
        service.process(evento("PedidoEfetuado", null));
        assertEquals(StatusPedido.CRIADO, status());
        service.process(evento("PedidoPago", "100.00"));
        assertEquals(StatusPedido.PAGO, status());
        service.process(evento("PedidoEnviado", null));
        assertEquals(StatusPedido.ENVIADO, status());
    }

    @Test
    void enviarPedidoSoCriadoLancaTransicaoInvalida() {
        service.process(evento("PedidoEfetuado", null));
        var ex = assertThrows(TransicaoInvalidaException.class,
                () -> service.process(evento("PedidoEnviado", null)));
        assertEquals("Pedido p1: transicao invalida CRIADO -> ENVIADO", ex.getMessage());
    }

    @Test
    void pagamentoComValorDiferenteFalha() {
        service.process(evento("PedidoEfetuado", null));
        assertThrows(TransicaoInvalidaException.class, () -> service.process(evento("PedidoPago", "10.00")));
        assertEquals(StatusPedido.CRIADO, status());
    }

    @Test
    void naoCancelaPedidoEnviado() {
        service.process(evento("PedidoEfetuado", null));
        service.process(evento("PedidoPago", "100.00"));
        service.process(evento("PedidoEnviado", null));
        assertThrows(TransicaoInvalidaException.class, () -> service.process(evento("PedidoCancelado", null)));
    }

    @Test
    void pedidoInexistenteLancaExcecao() {
        assertThrows(TransicaoInvalidaException.class, () -> service.process(evento("PedidoPago", "100.00")));
    }
}
