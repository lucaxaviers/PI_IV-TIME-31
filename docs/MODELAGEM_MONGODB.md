# Modelagem do Banco de Dados - MongoDB Atlas (Issue #20)

Autor da parte de Usuarios e Avaliacoes: Lucas Rodrigues Xavier | 25000508

Autor da parte de Locacoes: Tiago Medeiros | 25000845 (Issue #39)

Autor da parte de Vistorias: Kauan Aurelio Lasmar Dias | 25001590 (Issue #43)

A conexao vem da variavel de ambiente `MONGODB_URI` (veja `application.properties`).

## Collections

| Collection | Classe | Dono | Indices |
| :--- | :--- | :--- | :--- |
| `usuarios` | `Usuario` | Lucas | `email` (unico), `documento` (unico) |
| `maquinas` | `Maquina` | Mateus | `localizacao` (`2dsphere`) |
| `avaliacoes` | `Avaliacao` | Lucas | consulta por `locadorId`, `maquinaId` e `locacaoId` |
| `locacoes` | `Locacao` | Tiago | conflito de agenda por `maquinaId` + `status` + `dataInicio`/`dataFim` |
| `chat_messages` | `ChatMessage` | Leonardo | - |
| `vistorias_checklist` | `VistoriaChecklist` | Kauan | `locacaoId` + `tipo` (unico) |

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

A classe `Maquina` segue o mesmo padrao das demais entidades (abaixo), com `compareTo` ordenando primeiro por `valorDiaria` e depois por `tituloAnuncio`.

## Collection `vistorias_checklist` (Issue #43)

| Campo | Tipo | Descricao |
| :--- | :--- | :--- |
| `id` | `String` | Identificador gerado pelo MongoDB |
| `locacaoId` | `String` | Locacao a que a vistoria pertence |
| `maquinaId` | `String` | Maquina vistoriada (vem da locacao) |
| `tipo` | `TipoVistoria` | `RETIRADA` ou `DEVOLUCAO` |
| `itens` | `List<ItemChecklist>` | Itens conferidos, cada um com `descricao`, `conforme` (`true`, `false` ou nulo se ainda nao respondido) e `observacao` |
| `horimetro` | `Double` | Horas de uso do motor, informadas ao concluir (zero ou maior) |
| `responsavelId` | `String` | Locador ou locatario que abriu a vistoria |
| `dataVistoria` | `LocalDateTime` | Quando a vistoria foi aberta |
| `status` | `StatusVistoria` | `PENDENTE`, `CONCLUIDA` ou `COM_PENDENCIAS` |

Indice: `locacaoId` + `tipo` e unico (`locacao_tipo_unico`), entao cada locacao tem no maximo uma vistoria de retirada e uma de devolucao.

Regras de negocio (`VistoriaChecklistService`):

- So locacao `ACEITO` pode ter vistoria, e quem abre precisa ser o locador ou o locatario da locacao.
- A vistoria nasce `PENDENTE` com 8 itens padrao sem resposta (pneus, oleo, fluidos, luzes, freios, cabine, implementos e avarias visiveis).
- Item `nao conforme` exige observacao.
- Para concluir, todos os itens precisam estar respondidos e o horimetro deve ser zero ou maior.
- Se algum item esta `nao conforme`, o status final e `COM_PENDENCIAS`; senao e `CONCLUIDA`.
- Vistoria concluida nao pode ser alterada nem concluida de novo (409).
- Se uma resposta for invalida, nada e salvo (as respostas sao aplicadas primeiro em uma copia).

Endpoints: `POST /api/vistorias`, `GET /api/vistorias/{id}`, `GET /api/vistorias/locacao/{locacaoId}` e `PUT /api/vistorias/{id}/concluir`.

## Padrao das classes

As entidades `Usuario`, `Maquina`, `Avaliacao`, `Locacao`, `VistoriaChecklist` e `ItemChecklist` seguem o padrao da disciplina: `equals(Object)` em 4 passos, `hashCode()` com todos os campos do `equals`, `compareTo`, `toString`, construtor de copia (erro `Modelo ausente` se nulo) e `clone()`. Em `VistoriaChecklist` a lista de itens tem copia profunda: o construtor, o de copia e `getItens()` clonam cada item.
