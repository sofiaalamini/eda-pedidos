# eda-pedidos

Trabalho de Arquitetura Orientada a Eventos (Arquitetura de Software, FAG). Região AWS: `us-east-1`.

## Fluxo

```
SNS FIFO pedidos-efetuados.fifo
  └─► SQS FIFO pedidos-efetuados-processamento.fifo ─► pedidos-consumer
                 │ falhou 3x (maxReceiveCount = 3)
                 ▼
      SQS FIFO pedidos-efetuados-processamento-DLQ.fifo ─► pedidos-dlq-consumer
```

- [`pedidos-consumer`](pedidos-consumer): processa os eventos do pedido.
- [`pedidos-dlq-consumer`](pedidos-dlq-consumer): registra no console as mensagens da DLQ.

## Condição de falha

`PedidoEnviado` de um pedido que ainda está `CRIADO` (sem pagamento). O consumer lança `TransicaoInvalidaException` (`CRIADO -> ENVIADO`), a mensagem não é confirmada e volta à fila. Na 3ª falha o SQS a move para a DLQ.

## Como rodar

Em cada projeto, em terminais separados (PowerShell):

```powershell
$env:AWS_ACCESS_KEY_ID = "<access key>"
$env:AWS_SECRET_ACCESS_KEY = "<secret key>"
.\mvnw.cmd spring-boot:run
```

As chaves vêm só das variáveis de ambiente e nunca são versionadas.

## Testes

No tópico `pedidos-efetuados.fifo`, use como **Message group ID** o `orderId` e um **Message deduplication ID** novo a cada mensagem (pode ser o `eventId`).

### Teste 1: sequência válida (pedido-4001)

```json
{ "eventId": "evt-4001-1", "eventType": "PedidoEfetuado", "orderId": "pedido-4001", "customerId": "cliente-42", "total": 149.90, "currency": "BRL" }
{ "eventId": "evt-4001-2", "eventType": "PedidoPago", "orderId": "pedido-4001", "paymentMethod": "PIX", "amount": 149.90 }
{ "eventId": "evt-4001-3", "eventType": "PedidoEnviado", "orderId": "pedido-4001", "carrier": "Correios", "trackingCode": "BR123456789" }
```

Esperado: `CRIADO`, `CRIADO -> PAGO`, `PAGO -> ENVIADO`. Nada chega à DLQ.

### Teste 2: falha 3 vezes e vai para a DLQ (pedido-3001)

```json
{ "eventId": "evt-3001-1", "eventType": "PedidoEfetuado", "orderId": "pedido-3001", "customerId": "cliente-42", "total": 99.90, "currency": "BRL" }
{ "eventId": "evt-3001-2", "eventType": "PedidoEnviado", "orderId": "pedido-3001", "carrier": "Correios", "trackingCode": "BR000000000" }
```

Esperado: o `pedidos-consumer` mostra a 2ª mensagem com `tentativa: 1`, `2` e `3` (a cada 30 s, o visibility timeout) e a `TransicaoInvalidaException` em cada uma. Depois, o `pedidos-dlq-consumer` mostra `[DLQ] Mensagem recebida pela DLQ | evento: evt-3001-2 ...` com `ApproximateReceiveCount: 4`.

Documento de entrega: [`docs/entrega-atividade-dlq.md`](docs/entrega-atividade-dlq.md).
