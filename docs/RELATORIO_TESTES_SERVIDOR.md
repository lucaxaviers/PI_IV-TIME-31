# Atividade de FTTVS — Teste do Servidor JAVA PI IV
**Projeto AlugaMáquina — PI IV, Time 31**  
**Repositório:** `origin/main` | **Servidor:** 72 arquivos de produção e 39 arquivos de teste (662 testes JUnit 5 passando).

---

## Integrantes da Equipe

| RA | Nome | Papel Principal |
| :---: | :--- | :--- |
| **25000508** | **Lucas Rodrigues Xavier** | Usuários, cadastro de máquinas e avaliação pós-locação |
| **25003494** | **Leonardo Gambaroni Alves** | Chat em tempo real e infraestrutura de dados |
| **25001369** | **Mateus Oliveira Rafael** | Busca e catálogo regional geoespacial |
| **25001590** | **Kauan Aurélio Lasmar Dias** | Contrato digital (PDF) e vistoria técnica (checklist) |
| **25000845** | **Tiago Medeiros** | Detalhe da máquina, cálculo de frete e pedido de locação |

---

# Parte I - Propósito, Arquitetura e Estrutura das Classes do Servidor

## a) Nome e Descrição Sucinta do Projeto da Equipe

**Projeto:** AlugaMáquina (PI IV — Time 31)  
**Descrição:** Marketplace regional de economia compartilhada voltado ao agronegócio (região piloto de Paulínia/SP). Conecta proprietários de máquinas e implementos agrícolas ociosos a pequenos e médios produtores rurais que precisam alugar equipamentos por períodos pontuais da safra, cobrindo o fluxo completo:
- Cadastro e busca regional de máquinas com geolocalização;
- Solicitação e aceite de locação com cálculo de diárias e frete;
- Contrato digital com geração de PDF e assinaturas eletrônicas;
- Vistoria técnica (checklist) de entrega e devolução com horímetro;
- Chat em tempo real via WebSocket;
- Avaliação pós-locação com recálculo dinâmico da reputação do locador.

### Catálogo de Módulos e Endpoints Implementados (Spring Boot 3.3 / Java 17 + MongoDB Atlas)
O servidor conta com 72 arquivos Java em produção (13 tipos em `model`, 9 `controllers`, 9 `services`, 7 repositórios, 19 DTOs, 12 exceções de negócio, 2 classes de configuração e a classe principal) e 39 arquivos de teste.

| Módulo | Endpoints / Função Implementada no Servidor Java |
| :--- | :--- |
| **Status e Configuração** | `GET /api/status`; CORS global (`WebConfig`) e WebSocket/STOMP (`WebSocketConfig`). |
| **Usuários e Autenticação** | `POST /api/auth/cadastro` e `POST /api/auth/login` (normalização de e-mail/CPF/CNPJ e hash SHA-256). |
| **Cadastro de Máquinas** | `POST /api/maquinas` (validação de tipo de equipamento e município atendido com coordenadas `2dsphere`). |
| **Busca Geoespacial e Detalhe** | `GET /api/maquinas/busca` (latitude, longitude e raio até 500 km, índice `2dsphere`); `GET /api/maquinas/{id}`. |
| **Locações e Reservas** | `POST /api/locacoes/solicitar`; `PUT /api/locacoes/{id}/aceitar` e `/{id}/recusar`; `GET /api/locacoes/{id}` e `/usuario/{usuarioId}`. |
| **Contrato Digital e PDF** | `POST /api/contratos/gerar`; `GET /{id}`, `/locacao/{locacaoId}`, `/usuario/{usuarioId}`, `/{id}/pdf` (OpenPDF); `PUT /{id}/assinar`. |
| **Vistoria Técnica (Checklist)** | `POST /api/vistorias`; `GET /{id}` e `/locacao/{locacaoId}`; `PUT /{id}/concluir`. |
| **Chat em Tempo Real** | WebSocket `/ws-chat` (`/app/chat.enviar` → `/topic/mensagens`); `GET /api/chat/historico`. |
| **Avaliação Pós-Locação** | `POST /api/avaliacoes` (5 critérios de 1 a 5, média ponderada e recálculo da reputação do locador); `GET /api/avaliacoes/locador/{locadorId}`. |

---

## b) Arquitetura e Estrutura do Servidor Java

O servidor adota a arquitetura clássica em camadas do ecossistema Spring Boot:
- **Camada de Apresentação / Exposição:** `Controller` REST + Endpoints WebSocket STOMP.
- **Camada de Regras de Negócio e Serviços:** `Service`, com validações defensivas, cálculos de frete/diárias e geração de PDF.
- **Camada de Persistência / Acesso a Dados:** `Repository` (interfaces que estendem `MongoRepository`) e `MongoTemplate` (operações geoespaciais `$nearSphere`).
- **Camada de Dados:** MongoDB Atlas (7 coleções ativas: `usuarios`, `maquinas`, `locacoes`, `contratos`, `vistorias_checklist`, `chat_mensagens`, `avaliacoes`).
- **Pacotes Transversais:** `model` (entidades de domínio), `dto` (transferência de dados), `exception` (exceções de negócio com `@ExceptionHandler`), `config` (CORS e WebSocket).

---

## c) e d) Modelo UML de Classes e Domínio

As classes de domínio (`model`) implementam métodos canônicos por extenso sem abreviações (`getters`, `setters`, `equals`, `hashCode`, `toString`, `clone()` via `Cloneable` e `compareTo()` via `Comparable<T>`):
1. **Entidades Document:** `Usuario`, `Maquina`, `Locacao`, `Contrato`, `VistoriaChecklist`, `ItemChecklist`, `ChatMessage`, `Avaliacao`.
2. **Enumerations (5 enums):** `MunicipioAtendido`, `TipoEquipamento`, `StatusLocacao`, `StatusVistoria`, `TipoVistoria`.
3. **Exceções de Negócio (12 exceções):** Estendem `RuntimeException` (`AssinaturaNaoPermitidaException`, `AutenticacaoException`, `ConflitoAgendaException`, `ContratoConflitoException`, `ContratoNaoEncontradoException`, `LocacaoNaoEncontradaException`, `MaquinaNaoEncontradaException`, `ParametroBuscaInvalidoException`, `TransicaoStatusInvalidaException`, `UsuarioJaCadastradoException`, `VistoriaConflitoException`, `VistoriaNaoEncontradaException`).
4. **DTOs (19 classes):** Isolamento total entre requisições HTTP e entidades de banco.

---

# Parte II - Aplicação das Técnicas de Teste OO e Automação

## e) Testes Intraclasse em JUnit 5

Foram aplicadas as abordagens formais de particionamento recomendadas na disciplina:
1. **Particionamento por Estados (Locacao):** Avalia a máquina de estados através de `StatusLocacao` (`PENDENTE` → `ACEITO` → `FINALIZADO`, `PENDENTE` → `RECUSADO`, `PENDENTE` → `CANCELADO`).
2. **Particionamento por Atributos (Usuario):** Avalia operações que usam, modificam e não modificam atributos sensíveis (`reputacaoMedia`, `totalAvaliacoes`, `senhaHash`, `nome`, `documento`).
3. **Particionamento por Categorias de Métodos (Avaliacao):** Divide métodos em Inicialização/Cópia, Setup (Setters), Consultas (Getters), Predicativas (`isRecomenda`, `equals`) e Computação/Ordenação (`clone`, `hashCode`, `compareTo`).

### Falhas, Anomalias e Melhorias Identificadas nos Testes Intraclasse
| Falha / Anomalia | Descrição e Localização | Causa Raiz e Sugestão de Melhoria |
| :--- | :--- | :--- |
| **Transição de estado sem guarda na entidade** | `Locacao.java` (`setStatus`): permite regredir de `RECUSADO` para `ACEITO` ou atribuir `null`. | A máquina de estados é validada no `LocacaoService.exigirPendente()`. O setter da entidade é irrestrito. Sugestão: validar transições em `setStatus()`. |
| **Ausência de invariantes nas notas** | `Avaliacao.java` (construtor completo e setters): aceita notas 0 e 6 e média 9.5. | Validação da faixa 1..5 implementada apenas no `AvaliacaoService`. Sugestão: aplicar validação defensiva também na entidade. |
| **Atributos de reputação sem validação** | `Usuario.java` (`setReputacaoMedia`, `setTotalAvaliacoes`): aceita valores negativos. | Falta validação defensiva nos setters da entidade. Sugestão: assegurar `0.0 <= reputacaoMedia <= 5.0`. |
| **NullPointerException em `compareTo(null)`** | `Locacao`, `Usuario` e `Avaliacao`: chamadas a `compareTo(null)` lançam NPE bruto. | Falta checagem defensiva de argumento nulo antes de acessar campos da instância comparada. |

---

## f) Testes Interclasse em JUnit 5 (Serviço Principal: AvaliacaoService)

O teste simula a chamada completa partindo da camada Controller (`AvaliacaoController.registrarAvaliacao`), transitando pelo `AvaliacaoService`, validando notas, calculando média, persistindo no `AvaliacaoRepository`, recalculando a reputação agregada e atualizando o `Usuario` no `UsuarioRepository`.

### 1. Cenário NORMAL (HTTP 201 Created)
- **Fluxo:** Recebe `AvaliacaoRequest` com notas válidas (média 4,60), persiste a entidade `Avaliacao`, recalcula histórico do locador de 5,00 para 4,80, atualiza `Usuario` e retorna `ResponseEntity` HTTP 201 com `AvaliacaoResponse`.
- **Verificação Mockito:** `verify(avaliacaoRepository, times(1)).save(...)` e `verify(usuarioRepository, times(1)).save(...)`.

### 2. Cenário DE ERRO (HTTP 400 Bad Request)
- **Fluxo:** Recebe `AvaliacaoRequest` com nota inválida (`notaDesempenho = 0`), dispara `IllegalArgumentException` no Service, interceptada pelo `@ExceptionHandler` do Controller e convertida em HTTP 400 Bad Request.
- **Verificação Mockito:** `verify(avaliacaoRepository, never()).save(...)` e `verify(usuarioRepository, never()).save(...)`.

### Falhas e Anomalias Identificadas nos Testes Interclasse
| Falha / Anomalia | Descrição e Localização | Causa Raiz e Sugestão de Melhoria |
| :--- | :--- | :--- |
| **Avaliação duplicada permitida** | `AvaliacaoRepository.java` declara `existsByLocacaoId`, mas o Service não checa duplicidade. | Falta de chamada no `AvaliacaoService.registrarAvaliacao`. Sugestão: lançar conflito caso já exista avaliação. |
| **NullPointerException com DTO nulo** | `AvaliacaoService.java`: tenta acessar campos do request sem checar se o DTO é nulo. | Falta guarda `if (request == null)`. Gera erro 500 em vez de 400. |

---

## g) Resultados da Automação de Testes

- **Ferramentas:** JUnit 5 (Jupiter), Mockito 5.11, Spring Boot Test.
- **Resultado da Execução (`mvn test` na branch main):**
  - **Testes executados:** 662
  - **Falhas:** 0
  - **Erros:** 0
  - **Ignorados / Skipped:** 0
  - **Tempo total:** ~9.6 segundos
  - **Status:** **BUILD SUCCESS**
