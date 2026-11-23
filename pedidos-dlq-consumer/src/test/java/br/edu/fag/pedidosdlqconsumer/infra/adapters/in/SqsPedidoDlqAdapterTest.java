package br.edu.fag.pedidosdlqconsumer.infra.adapters.in;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.fag.pedidosdlqconsumer.dto.PedidoEventoDTO;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class SqsPedidoDlqAdapterTest {

    private final SqsPedidoDlqAdapter adapter = new SqsPedidoDlqAdapter();
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(SqsPedidoDlqAdapter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    void registraEventoDaDlqSemLancarExcecao() {
        var evento = new PedidoEventoDTO("evt-3001-2", "PedidoEnviado", "pedido-3001", Instant.now(),
                null, null, null, null, null, "Correios", "BR000000000", null);

        assertDoesNotThrow(() -> adapter.listen(evento, "3", "pedido-3001"));

        String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .reduce("", (a, b) -> a + "\n" + b);
        assertTrue(logs.contains("DLQ"));
        assertTrue(logs.contains("evt-3001-2"));
        assertTrue(logs.contains("PedidoEnviado"));
        assertTrue(logs.contains("pedido-3001"));
    }

    @Test
    void nuncaLancaExcecaoMesmoComDadosIncompletos() {
        var vazio = new PedidoEventoDTO(null, null, null, null, null, null, null, null, null, null, null, null);
        assertDoesNotThrow(() -> adapter.listen(vazio, null, null));
        assertDoesNotThrow(() -> adapter.listen(null, null, null));
    }
}
