package br.edu.fag.pedidosconsumer.infra.adapters.in;

import br.edu.fag.pedidosconsumer.application.ports.in.PedidoServicePort;
import br.edu.fag.pedidosconsumer.dto.PedidoEventoDTO;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.SqsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class SqsPedidoAdapter {

    private static final Logger log = LoggerFactory.getLogger(SqsPedidoAdapter.class);

    private final PedidoServicePort pedidoServicePort;

    public SqsPedidoAdapter(PedidoServicePort pedidoServicePort) {
        this.pedidoServicePort = pedidoServicePort;
    }

    // Nao captura excecao: a mensagem volta para a fila e, apos 3 tentativas, vai para a DLQ.
    @SqsListener("${queue.order-events}")
    public void listen(PedidoEventoDTO evento,
                       @Header(value = SqsHeaders.MessageSystemAttributes.SQS_APPROXIMATE_RECEIVE_COUNT,
                               required = false) String tentativa) {
        log.info("Mensagem recebida: {} | pedido: {} | evento: {} | tentativa: {}",
                evento.eventType(), evento.orderId(), evento.eventId(), tentativa);
        pedidoServicePort.process(evento);
        log.info("Mensagem consumida com sucesso. | evento: {}", evento.eventId());
    }
}
