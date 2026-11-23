# pedidos-dlq-consumer

Consome a DLQ `pedidos-efetuados-processamento-DLQ.fifo` e só registra no console, com o prefixo `[DLQ]`, o `eventId`, o tipo, o pedido, os dados do evento, o `ApproximateReceiveCount` e o `MessageGroupId`. Não reenvia nada e o listener nunca lança exceção.

O `ApproximateReceiveCount` na DLQ é 4: as 3 tentativas da fila principal mais o recebimento na DLQ.

Como rodar (PowerShell):

```powershell
$env:AWS_ACCESS_KEY_ID = "<access key>"
$env:AWS_SECRET_ACCESS_KEY = "<secret key>"
.\mvnw.cmd spring-boot:run
```

Build e testes: `.\mvnw.cmd clean package`.
