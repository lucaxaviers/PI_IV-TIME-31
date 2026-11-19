# Modelagem do Banco de Dados - MongoDB Atlas (Issue #20)

Autor da parte de Usuarios e Avaliacoes: Lucas Rodrigues Xavier | 25000508

Autor da parte de Locacoes: Tiago Medeiros | 25000845 (Issue #39)

A conexao vem da variavel de ambiente `MONGODB_URI` (veja `application.properties`).

## Collections

| Collection | Classe | Dono | Indices |
| :--- | :--- | :--- | :--- |
| `usuarios` | `Usuario` | Lucas | `email` (unico), `documento` (unico) |
| `maquinas` | `Maquina` | Mateus | `localizacao` (`2dsphere`) |
| `avaliacoes` | `Avaliacao` | Lucas | consulta por `locadorId`, `maquinaId` e `locacaoId` |
| `locacoes` | `Locacao` | Tiago | conflito de agenda por `maquinaId` + `status` + `dataInicio`/`dataFim` |
| `chat_messages` | `ChatMessage` | Leonardo | - |

## Relacionamentos (por referencia de id)

- `Avaliacao.locadorId` aponta para `Usuario.id`. Cada avaliacao atualiza `reputacaoMedia` e `totalAvaliacoes` do locador.
- `Avaliacao.locatarioId` aponta para `Usuario.id`.
- `Avaliacao.maquinaId` aponta para `Maquina.id`.
- `Avaliacao.locacaoId` e unico por regra de negocio (`existsByLocacaoId`): uma avaliacao por locacao.
- `Locacao.locadorId` e `Locacao.locatarioId` apontam para `Usuario.id`.
- `Locacao.maquinaId` aponta para `Maquina.id`.
- `Avaliacao.locacaoId` aponta para `Locacao.id`.

## Regras dos dados

- Notas de 1 a 5 em cinco criterios. `mediaFinal` fica com 2 casas decimais.
- `reputacaoMedia` comeca em 5.0 e `totalAvaliacoes` em 0.
- A senha fica so como hash SHA-256 em `senhaHash`. O `toString()` de `Usuario` nao mostra a senha.

## Collection `locacoes` (Issue #39)

| Campo | Tipo | Descricao |
| :--- | :--- | :--- |
| `id` | `String` | Identificador gerado pelo MongoDB |
| `locadorId` | `String` | Dono da maquina |
| `locatarioId` | `String` | Quem pediu a locacao |
| `maquinaId` | `String` | Maquina reservada |
| `dataInicio`, `dataFim` | `LocalDate` | Periodo da reserva (os dois dias contam como diaria) |
| `valorDiaria`, `valorFrete`, `valorTotal` | `BigDecimal` | Valores em reais, sempre com 2 casas |
| `status` | `StatusLocacao` | `PENDENTE`, `ACEITO`, `RECUSADO`, `CANCELADO` ou `FINALIZADO` |
| `motivoRecusa` | `String` | Texto opcional informado pelo locador ao recusar |
| `dataSolicitacao` | `LocalDateTime` | Quando o pedido foi criado |

Regras de negocio (`LocacaoService`):

- `dataFim` nao pode ser anterior a `dataInicio`. Diarias = dias entre as datas + 1 (10/11 a 14/11 = 5 diarias), igual ao front-end.
- Frete = `distanciaKm` x 2 (ida e volta) x R$ 6,50 + taxa fixa de R$ 150,00. Com 0 km (retirada no local) o frete e zero.
- `valorTotal` = `valorDiaria` x diarias + `valorFrete`.
- Toda solicitacao nasce `PENDENTE`. Se a maquina ja tem reserva `ACEITO` que cruza o periodo, a solicitacao e bloqueada (409).
- So `PENDENTE` pode virar `ACEITO` ou `RECUSADO`. Qualquer outra transicao e invalida (409). Ao aceitar, a agenda e conferida de novo.
- Dois periodos se cruzam quando `dataInicio` existente <= `dataFim` novo e `dataFim` existente >= `dataInicio` novo.

Endpoints: `POST /api/locacoes/solicitar`, `PUT /api/locacoes/{id}/aceitar` e `PUT /api/locacoes/{id}/recusar` (corpo opcional com `motivo`).

## Collection `maquinas` (Issue #20)

| Campo | Tipo | Descricao |
| :--- | :--- | :--- |
| `id` | `String` | Identificador gerado pelo MongoDB |
| `tituloAnuncio` | `String` | Titulo do anuncio do equipamento |
| `tipoEquipamento` | `String` | Categoria (trator, colheitadeira, pulverizador, implemento, etc.) |
| `municipio` | `String` | Municipio onde a maquina esta disponivel |
| `valorDiaria` | `BigDecimal` | Valor da diaria em reais |
| `localizacao` | `GeoJsonPoint` | Coordenadas (longitude, latitude) com indice `2dsphere` |

Indice geoespacial: `localizacao` tem `@GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)`, o que permite a consulta `geoNear` usada em `MaquinaBuscaService.buscarPorRaio` (Issue #36) para encontrar maquinas dentro de um raio em quilometros a partir de uma coordenada `lat`/`lng`.

A classe `Maquina` segue o mesmo padrao do Prof. Francisco das demais entidades (abaixo), com `compareTo` ordenando primeiro por `valorDiaria` e depois por `tituloAnuncio`.

## Padrao das classes

As entidades `Usuario`, `Maquina`, `Avaliacao` e `Locacao` seguem o padrao do Prof. Francisco: `equals(Object)` em 4 passos, `hashCode()` com todos os campos do `equals`, `compareTo`, `toString`, construtor de copia (erro `Modelo ausente` se nulo) e `clone()`.
