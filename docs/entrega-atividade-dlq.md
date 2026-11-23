# Entrega: Atividade 2 (Dead Letter Queue)

Arquitetura de Software (FAG) | Repositório: https://github.com/sofiaalamini/eda-pedidos

## Integrantes

- [PREENCHER]
- [PREENCHER]
- [PREENCHER]

## Condição de falha

`PedidoEnviado` de um pedido que ainda está `CRIADO` (sem `PedidoPago`) faz o `pedidos-consumer` lançar `TransicaoInvalidaException` (`CRIADO -> ENVIADO`).

É realista porque, em eventos de domínios diferentes (pagamento e logística), a ordem e a consistência não são garantidas. A regra (só se envia o que foi pago) é legítima e a falha é determinística: reprocessar não resolve. A mensagem precisa ser isolada na DLQ para análise, sem travar a fila.

## Configuração da DLQ

- Fila principal: `pedidos-efetuados-processamento.fifo` (assinada no tópico `pedidos-efetuados.fifo`, com raw message delivery).
- DLQ: `pedidos-efetuados-processamento-DLQ.fifo`.
- `maxReceiveCount` = 3 e visibility timeout = 30 s.
- O consumer não captura a exceção, então a mensagem não é confirmada e o SQS a reentrega até o limite.

## Evidências

### Configuração da DLQ (console do SQS)
Ver documento de entrega.

### Teste 1: sequência válida (pedido-4001)
Ver documento de entrega.

```
15:49:00 Mensagem recebida: PedidoEfetuado | pedido: pedido-4001 | evento: evt-4001-1 | tentativa: 1
15:49:01 Pedido pedido-4001: criado (CRIADO) | total: 259.8 BRL
15:49:17 Mensagem recebida: PedidoPago | pedido: pedido-4001 | evento: evt-4001-2 | tentativa: 1
15:49:17 Pedido pedido-4001: CRIADO -> PAGO
15:49:34 Mensagem recebida: PedidoEnviado | pedido: pedido-4001 | evento: evt-4001-3 | tentativa: 1
15:49:34 Pedido pedido-4001: PAGO -> ENVIADO
```

### Teste 2: 3 falhas (pedido-3001)
Ver documento de entrega.

```
15:49:54 Pedido pedido-3001: criado (CRIADO) | total: 89.9 BRL
15:50:11 Mensagem recebida: PedidoEnviado | pedido: pedido-3001 | evento: evt-3001-2 | tentativa: 1
         TransicaoInvalidaException: Pedido pedido-3001: transicao invalida CRIADO -> ENVIADO
15:50:41 ... evento: evt-3001-2 | tentativa: 2 -> mesma exceção
15:51:11 ... evento: evt-3001-2 | tentativa: 3 -> mesma exceção
```

### pedidos-dlq-consumer
Ver documento de entrega.

```
15:51:41 [DLQ] Mensagem recebida pela DLQ | evento: evt-3001-2 | tipo: PedidoEnviado | pedido: pedido-3001
15:51:41 [DLQ] Dados do evento: eventId=evt-3001-2, orderId=pedido-3001, carrier=Correios, trackingCode=BR000000000, occurredAt=2026-11-23T18:53:00Z
15:51:41 [DLQ] ApproximateReceiveCount: 4 | MessageGroupId: pedido-3001
```

### DLQ no console do SQS
Ver documento de entrega.

## Conclusão

A DLQ isolou a mensagem inválida sem parar o processamento dos outros pedidos: o SQS reentregou o `PedidoEnviado` do `pedido-3001` 3 vezes e o moveu para a DLQ, onde o `pedidos-dlq-consumer` o registrou. A sequência válida (`pedido-4001`) foi consumida normalmente. O `ApproximateReceiveCount` chegou a 4 na DLQ (3 tentativas mais o recebimento na DLQ). Como a DLQ só registra, uma mensagem assim exigiria análise e reprocessamento manual depois de corrigir a causa (por exemplo, publicar o `PedidoPago` que faltou).
