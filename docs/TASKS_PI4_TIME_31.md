# 📋 Backlog de Tarefas — PI4 (Time 31 - AlugaMáquina)

> Baseado na **Ficha Oficial de Avaliação e Escopo do PI4** (Prof.ª Renata Arantes) e no **Relatório de Ideação do AlugaMáquina**.

---

## 👥 Equipe e Distribuição de Papéis

| Integrante | GitHub | RA | Módulo Principal de Atuação |
| :--- | :--- | :---: | :--- |
| **Lucas Rodrigues Xavier** | [@lucaxaviers](https://github.com/lucaxaviers) | 25000508 | Autenticação & Usuários, Cadastro de Máquinas, Avaliação Pós-Locação |
| **Mateus Oliveira Rafael** | [@mateusor](https://github.com/mateusor) | 25001369 | Busca e Catálogo Regional, Filtros Geoespaciais (km) |
| **Tiago Medeiros** | [@tiago148](https://github.com/tiago148) | 25000845 | Detalhe da Máquina, Cálculo de Frete Estimado, Fluxo de Solicitação e Aceite |
| **Leonardo Gambaroni Alves** | [@LeonardoGambaroni](https://github.com/LeonardoGambaroni) | 25003494 | Chat entre as Partes, Mensageria em Tempo Real, Suporte |
| **Kauan Aurélio Lasmar Dias** | [@KauanDias-Dev](https://github.com/KauanDias-Dev) | 25001590 | Contrato Digital Simplificado (PDF), Verificação Técnica Assistida (Checklist) |

---

## 🏗️ Requisitos Tecnológicos Obrigatórios

| Camada | Tecnologia | Requisito / Critério de Aceite |
| :--- | :--- | :--- |
| **Front-end** | HTML5, CSS3, JavaScript / TypeScript | Interfaces responsivas, integradas e acessíveis ao homem do campo. |
| **Servidor (Backend)** | Java (Spring Boot / Java EE) | API REST estruturada com injeção de dependências e regras de negócio. |
| **Banco de Dados** | MongoDB Atlas (NoSQL) | Modelagem em collections com índices geoespaciais (`2dsphere`). |
| **Testes de Software** | JUnit (versão 5) | Cobertura de testes unitários nas regras de negócio e cálculos críticos. |

---

## 📦 Matriz de Tarefas por Funcionalidade (PI4)

### 1. Cadastro e Login de Usuários (Locador e Locatário)
*Item identificado na devolutiva como necessário para a sustentação do sistema.*
* **Responsável Principal:** Lucas Xavier ([@lucaxaviers](https://github.com/lucaxaviers)) | **Apoio:** Leonardo Gambaroni ([@LeonardoGambaroni](https://github.com/LeonardoGambaroni))
* **Tasks:**
  - [ ] **[FRONT]** Criar tela de login e cadastro de usuários com distinção ou unificação de perfil (Locador / Locatário).
  - [ ] **[FRONT]** Validação de formulário no cliente (CPF/CNPJ, telefone, senha e endereço em Paulínia e região).
  - [ ] **[BACK]** Criar endpoints `POST /api/auth/cadastro` e `POST /api/auth/login` em Java.
  - [ ] **[BACK]** Implementar hash de senha seguro (BCrypt) e controle de sessão ou token JWT.
  - [ ] **[DB]** Criar collection `usuarios` no MongoDB Atlas com validação de esquema e índice único por e-mail/CPF.
  - [ ] **[TESTS]** Testes unitários com JUnit para validação de dados de entrada e autenticação.

---

### 2. Cadastro de Máquinas Agrícolas
* **Responsável Principal:** Lucas Xavier ([@lucaxaviers](https://github.com/lucaxaviers))
* **Tasks:**
  - [x] **[FRONT]** Interface HTML/CSS de cadastro de máquinas com upload de fotos e dados operacionais *(Protótipo entregue)*.
  - [ ] **[FRONT]** Validação de campos obrigatórios via JavaScript (tipo, potência, horímetro, diária, localização).
  - [ ] **[BACK]** Criar endpoints `POST /api/maquinas`, `GET /api/maquinas/{id}` e `PUT /api/maquinas/{id}` em Java.
  - [ ] **[BACK]** Regras de validação de valores de diária e vinculação com o ID do proprietário logado.
  - [ ] **[DB]** Criar collection `maquinas` com coordenadas de geolocalização (`location: { type: "Point", coordinates: [lng, lat] }`).
  - [ ] **[TESTS]** Testes unitários JUnit para validação dos limites de valores e consistência dos dados do anúncio.

---

### 3. Busca e Filtro Regional (Vitrine & Catálogo)
* **Responsável Principal:** Mateus Oliveira ([@mateusor](https://github.com/mateusor))
* **Tasks:**
  - [x] **[FRONT]** Interface HTML/CSS da vitrine de máquinas com badges de verificação e distância *(Protótipo entregue)*.
  - [ ] **[FRONT]** Lógica de filtros interativos no frontend (categoria, raio de distância máxima em km e faixa de preço).
  - [ ] **[BACK]** Criar endpoint `GET /api/maquinas/busca` com parâmetros `categoria`, `raioKm`, `latitude`, `longitude`.
  - [ ] **[DB]** Implementar índice geoespacial `2dsphere` na collection `maquinas` e consulta usando operadores `$nearSphere` ou `$geoWithin`.
  - [ ] **[TESTS]** Testes unitários JUnit validando a lógica de filtragem e cálculo de raio de distância.

---

### 4. Detalhe da Máquina e Cálculo de Frete Estimado
* **Responsável Principal:** Tiago Medeiros ([@tiago148](https://github.com/tiago148))
* **Tasks:**
  - [x] **[FRONT]** Interface HTML/CSS da página de detalhe da máquina com especificações e simulador *(Protótipo entregue)*.
  - [ ] **[FRONT]** Implementação do script de cálculo estimado de frete baseado na distância (Paulínia, Cosmópolis, Campinas, etc.).
  - [ ] **[BACK]** Endpoint `POST /api/frete/calcular` recebendo CEP/origem e destino, retornando km estimado e valor (ida e volta).
  - [ ] **[BACK]** Tabela de precificação de frete por categoria de maquinário (peso e dimensões do implemento/trator).
  - [ ] **[TESTS]** Testes unitários JUnit para todas as faixas de distância e modalidades de frete (entrega pela plataforma vs. retirada própria).

---

### 5. Solicitação e Aceite do Pedido de Locação
*Item identificado na devolutiva ("Aparece no protótipo: Solicitar aluguel").*
* **Responsável Principal:** Tiago Medeiros ([@tiago148](https://github.com/tiago148)) | **Apoio:** Lucas Xavier ([@lucaxaviers](https://github.com/lucaxaviers))
* **Tasks:**
  - [x] **[FRONT]** Botão e gatilho de solicitação de aluguel conectado à reserva *(Protótipo interligado)*.
  - [ ] **[FRONT]** Painel de gestão de pedidos de locação do locador (Aprovar / Recusar solicitação).
  - [ ] **[BACK]** Endpoint `POST /api/locacoes` para criação do pedido com status `PENDENTE`.
  - [ ] **[BACK]** Endpoint `PATCH /api/locacoes/{id}/status` para transições (`APROVADO`, `RECUSADO`, `EM_ANDAMENTO`, `FINALIZADO`).
  - [ ] **[DB]** Collection `locacoes` relacionando `locadorId`, `locatarioId`, `maquinaId`, datas, diárias e valores.
  - [ ] **[TESTS]** Testes unitários JUnit para a máquina de estados da locação (garantindo que locações canceladas ou recusadas não avancem).

---

### 6. Contrato Digital Simplificado (Geração de PDF)
* **Responsável Principal:** Kauan Dias ([@KauanDias-Dev](https://github.com/KauanDias-Dev))
* **Tasks:**
  - [x] **[FRONT]** Interface HTML/CSS da minuta de contrato com etapas da locação e termos de aceite *(Protótipo entregue)*.
  - [x] **[FRONT]** Ação de impressão/exportação para PDF (`window.print()`).
  - [ ] **[BACK]** Serviço em Java para geração automática de documento PDF formal com os dados da locação (usando OpenPDF / iText).
  - [ ] **[BACK]** Endpoint `GET /api/locacoes/{id}/contrato/pdf` retornando o arquivo gerado para download.
  - [ ] **[DB]** Registro do aceite digital (data/hora, IP, dados informados pelo locatário) no registro da locação.
  - [ ] **[TESTS]** Testes unitários JUnit validando a composição das cláusulas essenciais e integridade das variáveis da minuta.

---

### 7. Verificação Técnica Assistida (Checklist no Sistema)
*Destaque da devolutiva: "Checklist registrado no sistema".*
* **Responsável Principal:** Kauan Dias ([@KauanDias-Dev](https://github.com/KauanDias-Dev)) | **Apoio:** Tiago Medeiros ([@tiago148](https://github.com/tiago148))
* **Tasks:**
  - [ ] **[FRONT]** Criar componente ou modal de Checklist Digital de Entrega e Devolução (itens: horímetro, combustível/óleo, limpeza, integridade dos pneus/esteiras e implementos).
  - [ ] **[FRONT]** Upload de fotos de comprovação de vistoria inicial e final.
  - [ ] **[BACK]** Endpoints `POST /api/locacoes/{id}/checklist/entrega` e `POST /api/locacoes/{id}/checklist/devolucao`.
  - [ ] **[DB]** Subdocumento `checklist` incorporado ao contrato/locação com dados de antes e depois.
  - [ ] **[TESTS]** Testes unitários JUnit para validação de discrepância de horímetro e validação dos itens obrigatórios do checklist.

---

### 8. Chat entre as Partes (Mensageria)
* **Responsável Principal:** Leonardo Gambaroni ([@LeonardoGambaroni](https://github.com/LeonardoGambaroni))
* **Tasks:**
  - [x] **[FRONT]** Interface HTML/CSS da central de mensagens responsiva com histórico *(Protótipo entregue)*.
  - [ ] **[FRONT]** Lógica de envio e renderização de novas mensagens em tempo real na tela.
  - [ ] **[BACK]** Endpoints REST para histórico de conversas: `GET /api/chat/conversas` e `GET /api/chat/{conversaId}/mensagens`.
  - [ ] **[BACK]** Implementação de WebSocket (Spring WebSocket / STOMP) para mensageria bidirecional em tempo real.
  - [ ] **[DB]** Collection `mensagens` com indexação por `conversaId` e ordenação cronológica por `timestamp`.
  - [ ] **[TESTS]** Testes unitários JUnit para envio, persistência e sanitização das mensagens do chat.

---

### 9. Avaliação Pós-Locação
* **Responsável Principal:** Lucas Xavier ([@lucaxaviers](https://github.com/lucaxaviers))
* **Tasks:**
  - [x] **[FRONT]** Interface HTML/CSS de avaliação mútua com sistema de estrelas, critérios e comentários *(Protótipo entregue)*.
  - [ ] **[FRONT]** Lógica interativa de seleção de estrelas e bloqueio de envio sem preenchimento obrigatório.
  - [ ] **[BACK]** Endpoint `POST /api/avaliacoes` associado à locação finalizada.
  - [ ] **[BACK]** Regra de negócio: recálculo automático da reputação média do produtor e da máquina após cada avaliação.
  - [ ] **[DB]** Collection `avaliacoes` e atualização atômica de score na collection `maquinas` e `usuarios`.
  - [ ] **[TESTS]** Testes unitários JUnit para cálculo ponderado de notas e restrição contra avaliações duplicadas.

---

## 🎯 Escopo Excluído do PI4 (Evoluções Futuras)

Conforme acordado na ficha e devolutiva do projeto, os seguintes itens **NÃO** serão implementados nesta etapa acadêmica:
1. Verificação técnica presencial por perito físico.
2. Seguro integrado automatizado.
3. Gateway de pagamento e caução online (o acerto financeiro é combinado entre as partes no piloto).
4. Rastreamento GPS por hardware embarcado na máquina.
5. Central de mediação e disputas jurídicas.
6. Aplicativo mobile nativo (foco em Web responsivo).
7. Assinatura premium e planos de destaque.
8. Painel dedicado para cooperativas.
9. Módulo avançado de relatórios e analytics preditivo.
10. Contratação com operador incluso.
