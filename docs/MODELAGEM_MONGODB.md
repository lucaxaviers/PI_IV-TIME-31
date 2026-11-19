# Modelagem do Banco de Dados - MongoDB Atlas (Issue #20)

Autor da parte de Usuarios e Avaliacoes: Lucas Rodrigues Xavier | 25000508

Autor da parte de Locacoes: Tiago Medeiros | 25000845 (Issue #39)

Autor da parte de Maquinas: Mateus Oliveira Rafael | 25001369 (Issue #20 / #36)

Autor da parte de Chat: Leonardo Gambaroni Alves | 25003494 (Issues #20 / #52)

Autor da parte de Contratos e Vistorias: Kauan Aurelio Lasmar Dias | 25001590 (Issues #44 e #43)

A conexao vem da variavel de ambiente `MONGODB_URI` (veja `application.properties`).

## Collections

| Collection | Classe | Dono | Indices |
| :--- | :--- | :--- | :--- |
| `usuarios` | `Usuario` | Lucas | `email` (unico), `documento` (unico) |
| `maquinas` | `Maquina` | Mateus | `localizacao` (`2dsphere`), `tipoEquipamento`, `municipio` |
| `avaliacoes` | `Avaliacao` | Lucas | `locacaoId` (unico), `locadorId`, `maquinaId` |
| `locacoes` | `Locacao` | Tiago | conflito de agenda por `maquinaId` + `status` + `dataInicio`/`dataFim` |
| `chat_mensagens` | `ChatMessage` | Leonardo | `remetente` + `destinatario` + `timestamp`, `timestamp` |
| `contratos` | `Contrato` | Kauan | `locacaoId` (unico), `numero` (unico) |
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

## Collection `chat_mensagens` (Issues #20 / #52)

| Campo | Tipo | Descricao |
| :--- | :--- | :--- |
| `id` | `String` | Identificador gerado pelo MongoDB |
| `remetente` | `String` | Nome ou identificador de quem enviou a mensagem |
| `destinatario` | `String` | Nome ou identificador de quem recebe a mensagem |
| `conteudo` | `String` | Texto sanitizado da mensagem (de 1 ate 1000 caracteres) |
| `horario` | `String` | Horario formatado para exibicao na tela (ex.: `10:15`) |
| `timestamp` | `LocalDateTime` | Data e hora exatas usadas na ordenacao cronologica |

Regras de negocio (`ChatMessageService`):

- Remetente e conteudo sao obrigatorios e nao podem ser vazios.
- Remetente e destinatario nao podem ser iguais.
- O conteudo aceita no maximo 1000 caracteres e passa por sanitizacao de tags HTML (`<` e `>`) antes de gravar no banco.
- Quando `timestamp` nao e informado, o servico preenche automaticamente com `LocalDateTime.now()`.

Indices: indice composto `{ remetente: 1, destinatario: 1, timestamp: 1 }` para filtrar conversas em ordem cronologica e indice `{ timestamp: 1 }` para o historico geral.

Endpoints: WebSocket STOMP `/ws-chat` (envio em `/app/chat.enviar` e broadcast em `/topic/mensagens`) e `GET /api/chat/historico` (com filtros opcionais `remetente` e `destinatario`).

## Collection `contratos` (Issue #44)

Autor: Kauan Aurelio Lasmar Dias | 25001590 - guarda o contrato digital de cada locacao aceita.

| Campo | Tipo | Descricao |
| :--- | :--- | :--- |
| `id` | `String` | Identificador gerado pelo MongoDB |
| `locacaoId` | `String` | Locacao do contrato (unico: um contrato por locacao) |
| `numero` | `String` | Numero no formato `AM-ANO-00123` (unico) |
| `locadorId`, `locatarioId`, `maquinaId` | `String` | Ids das partes e da maquina |
| `nomeLocador`, `nomeLocatario`, `descricaoMaquina` | `String` | Copia dos nomes e do equipamento no momento da emissao, como em um documento assinado |
| `dataInicio`, `dataFim` | `LocalDate` | Periodo da locacao |
| `valorTotal`, `valorCaucao` | `BigDecimal` | Valores em reais, sempre com 2 casas |
| `assinadoPeloLocador`, `assinadoPeloLocatario` | `boolean` | Quem ja assinou |
| `dataAssinaturaLocador`, `dataAssinaturaLocatario` | `LocalDateTime` | Quando cada parte assinou |
| `dataEmissao` | `LocalDateTime` | Quando o contrato foi gerado |

Indices: `locacaoId` e `numero` sao unicos. O unico em `numero` evita dois contratos com o mesmo numero se duas geracoes ocorrerem ao mesmo tempo.

Regras de negocio (`ContratoService`):

- So locacao `ACEITO` gera contrato, e cada locacao tem no maximo um (409 nos outros casos).
- O numero segue a contagem de contratos: `AM-2026-00001`, `AM-2026-00002`, e assim por diante.
- A caucao padrao e de R$ 1.000,00, a mesma da tela.
- So o locador e o locatario do contrato podem assinar (403 para os demais), e cada um assina uma vez (409).
- O contrato so fica vigente quando as duas partes assinaram.
- O PDF e gerado na hora com OpenPDF, com as 6 clausulas e as assinaturas registradas.

Endpoints: `POST /api/contratos/gerar`, `GET /api/contratos/{id}`, `GET /api/contratos/locacao/{locacaoId}`, `GET /api/contratos/{id}/pdf` e `PUT /api/contratos/{id}/assinar`.

## Collection `vistorias_checklist` (Issue #43)

Autor: Kauan Aurelio Lasmar Dias | 25001590 - guarda a vistoria tecnica (checklist de retirada e devolucao) de cada locacao.

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

## Ambiente local com MongoDB

Para rodar localmente com todas as collections, validadores, indices e dados iniciais:

```bash
docker compose up -d mongodb
```

O script `mongodb/init-mongo.js` inicializa as 7 collections do banco (incluindo `contratos` e `vistorias_checklist`) com validadores, indices e carga inicial, podendo tambem ser rodado direto no MongoDB Atlas via `mongosh`.

Para o detalhamento conceitual completo de entidades, atributos, exemplos e desdobramento fisico, consulte o [Dicionario de Dados](./DICIONARIO_DE_DADOS.md).

## Padrao das classes

As entidades `Usuario`, `Maquina`, `Avaliacao`, `Locacao`, `ChatMessage`, `Contrato`, `VistoriaChecklist` e `ItemChecklist` seguem o padrao da disciplina: `equals(Object)` em 4 passos, `hashCode()` com todos os campos do `equals`, `compareTo`, `toString`, construtor de copia (erro `Modelo ausente` se nulo) e `clone()`. Em `VistoriaChecklist` a lista de itens tem copia profunda: o construtor, o de copia e `getItens()` clonam cada item. As classes `Contrato`, `VistoriaChecklist` e `ItemChecklist` foram acrescentadas por Kauan Aurelio Lasmar Dias | 25001590.
