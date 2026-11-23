# pedidos-consumer

Consome a fila `pedidos-efetuados-processamento.fifo` e controla o status dos pedidos (`CRIADO → PAGO → ENVIADO`, ou `CANCELADO`), guardado em memória. Se a transição for inválida, lança `TransicaoInvalidaException` sem capturar, e a mensagem vai para a DLQ depois de 3 tentativas.

Estrutura (hexagonal): `infra/adapters/in/SqsPedidoAdapter` → `PedidoServicePort` → `PedidoService` → `PedidoRepositoryPort` → `infra/adapters/out/PedidoRepositoryMemoria`.

Como rodar (PowerShell):

```powershell
$env:AWS_ACCESS_KEY_ID = "<access key>"
$env:AWS_SECRET_ACCESS_KEY = "<secret key>"
.\mvnw.cmd spring-boot:run
```

Build e testes: `.\mvnw.cmd clean package`. Os testes do projeto estão no README da raiz.
