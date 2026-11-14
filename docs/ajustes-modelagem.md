# FashionStore — Ajustes na modelagem original

Este arquivo registra as alterações feitas em relação ao PDF da documentação,
para que todo o grupo (Web, Mobile e Banco de Dados) use a mesma estrutura.

## Ajustes aprovados

| # | Tabela | Alteração | Motivo |
|---|--------|-----------|--------|
| 1 | `produto` | **Removida** a coluna `tamanho` | Um produto é vendido em vários tamanhos |
| 1 | `estoque` | **Nova** coluna `tamanho VARCHAR(10)`; `(id_produto, tamanho)` é `UNIQUE` | Estoque controlado por tamanho (ex.: P = 5, M = 3) |
| 1 | `item_pedido` | **Nova** coluna `tamanho VARCHAR(10)` | Saber qual tamanho o cliente comprou |
| 2 | `cliente` | **Nova** coluna `perfil VARCHAR(10)` (`CLIENTE` ou `ADMIN`), padrão `CLIENTE` | Controle de acesso à área administrativa |

## Ajustes menores

| Tabela | Alteração | Motivo |
|--------|-----------|--------|
| `cliente` | `senha VARCHAR(60)` ou maior | O hash BCrypt tem 60 caracteres |
| `cliente` | `email` e `cpf` com `UNIQUE` | Impedir cadastros duplicados |
| `pedido` | `data_pedido DATETIME` (era `DATE`) | Guardar também a hora do pedido |
| `pedido` | `status` aceita só: `PENDENTE`, `PAGO`, `ENVIADO`, `ENTREGUE`, `CANCELADO` | Mesmos valores na Web, no Flutter e no banco |
| `item_pedido` | FK para `produto` com `ON DELETE RESTRICT` | Não excluir produto que já foi vendido |

## Valores monetários

`preco`, `valor_total`, `preco_unitario` e `subtotal` usam `DECIMAL(10,2)`
no banco e `BigDecimal` no Java.

## Carrinho

O carrinho **não** tem tabela: fica na sessão do usuário (Web) ou no
próprio aplicativo (Flutter) até o pedido ser finalizado.
