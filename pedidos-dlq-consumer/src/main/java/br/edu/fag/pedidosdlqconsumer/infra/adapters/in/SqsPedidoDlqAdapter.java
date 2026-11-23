package br.edu.fag.pedidosdlqconsumer.infra.adapters.in;

import br.edu.fag.pedidosdlqconsumer.dto.PedidoEventoDTO;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.SqsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class SqsPedidoDlqAdapter {

    private static final Logger log = LoggerFactory.getLogger(SqsPedidoDlqAdapter.class);

    @SqsListener("${queue.order-events-dlq}")
    public void listen(PedidoEventoDTO evento,
                       @Header(value = SqsHeaders.MessageSystemAttributes.SQS_APPROXIMATE_RECEIVE_COUNT,
                               required = false) String receiveCount,
                       @Header(value = SqsHeaders.MessageSystemAttributes.SQS_MESSAGE_GROUP_ID_HEADER,
                               required = false) String groupId) {
        try {
            log.warn("[DLQ] Mensagem recebida pela DLQ | evento: {} | tipo: {} | pedido: {}",
                    evento.eventId(), evento.eventType(), evento.orderId());
            log.warn("[DLQ] Dados do evento: {}", evento);
            log.warn("[DLQ] ApproximateReceiveCount: {} | MessageGroupId: {}", receiveCount, groupId);
        } catch (RuntimeException e) {
            log.error("[DLQ] Falha ao registrar a mensagem da DLQ: {}", e.getMessage());
        }
    }
}
