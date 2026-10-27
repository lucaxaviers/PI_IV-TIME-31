# 📋 Backlog de Tarefas & Issues — PI4 (Time 31 - AlugaMáquina)

> Baseado na **Ficha Oficial de Avaliação e Escopo do PI4** (Prof.ª Renata Arantes), no **Relatório de Ideação do AlugaMáquina** e sincronizado com o **[GitHub Projects](https://github.com/users/lucaxaviers/projects/8)**.

---

## 👥 Equipe e Distribuição de Papéis

| Integrante | GitHub | RA | Módulo Principal de Atuação |
| :--- | :--- | :---: | :--- |
| **Lucas Rodrigues Xavier** | [@lucaxaviers](https://github.com/lucaxaviers) | 25000508 | Autenticação & Usuários, Cadastro de Máquinas, Avaliação Pós-Locação |
| **Mateus Oliveira Rafael** | [@mateusor](https://github.com/mateusor) | 25001369 | Busca e Catálogo Regional, Filtros Geoespaciais (km / `2dsphere`) |
| **Tiago Medeiros** | [@tiago148](https://github.com/tiago148) | 25000845 | Detalhe da Máquina, Cálculo de Frete Estimado, Solicitação e Aceite de Reserva |
| **Leonardo Gambaroni Alves** | [@LeonardoGambaroni](https://github.com/LeonardoGambaroni) | 25003494 | Chat entre as Partes, Mensageria em Tempo Real e WebSockets |
| **Kauan Aurélio Lasmar Dias** | [@KauanDias-Dev](https://github.com/KauanDias-Dev) | 25001590 | Contrato Digital Simplificado (PDF), Verificação Técnica Assistida (Checklist) |

---

## 🏗️ Requisitos Tecnológicos Obrigatórios

| Camada | Tecnologia | Requisito / Critério de Aceite |
| :--- | :--- | :--- |
| **Front-end** | HTML5, CSS3, JavaScript / TypeScript | Interfaces responsivas, interligadas e adaptadas ao homem do campo. |
| **Servidor (Backend)** | Java (Spring Boot / Java EE) | API REST estruturada com injeção de dependências e regras de negócio. |
| **Banco de Dados** | MongoDB Atlas (NoSQL) | Modelagem em collections com índices geoespaciais (`2dsphere`). |
| **Testes de Software** | JUnit (versão 5) | Cobertura obrigatória de testes unitários nas regras críticas. |

---

## 📌 Tabela Geral de Issues no GitHub

| # | Título da Issue / Tarefa | Responsável(is) | Camada | Status |
|:---:|:---|:---|:---:|:---:|
| **#34** | Cadastro e login de usuários - Interface (HTML e CSS) | `@lucaxaviers` | Front | 🟣 CLOSED |
| **#35** | Autenticação e controle de acesso - Backend Java | `@lucaxaviers` | Back | 🟢 OPEN |
| **#16** | Cadastro de máquina - HTML | `@lucaxaviers` | Front | 🟣 CLOSED |
| **#17** | Cadastro de máquina - CSS | `@lucaxaviers` | Front | 🟣 CLOSED |
| **#18** | Validações dos campos do cadastro de máquina (JavaScript) | `@lucaxaviers` | Front | 🟢 OPEN |
| **#14** | Busca e catálogo de máquinas - HTML | `@mateusor` | Front | 🟣 CLOSED |
| **#22** | Busca e catálogo de máquinas - CSS | `@mateusor` | Front | 🟣 CLOSED |
| **#37** | Filtros dinâmicos no catálogo de máquinas (JavaScript) | `@mateusor` | Front | 🟢 OPEN |
| **#36** | API REST de Busca Geoespacial de Máquinas - Backend Java | `@mateusor` | Back/DB | 🟢 OPEN |
| **#13** | Detalhe da máquina e frete - HTML | `@tiago148` | Front | 🟣 CLOSED |
| **#23** | Detalhe da máquina e frete - CSS | `@tiago148` | Front | 🟣 CLOSED |
| **#24** | Cálculo de frete estimado (JavaScript) | `@tiago148` | Front/Back | 🟢 OPEN |
| **#38** | Solicitação e aceite do pedido de locação - Interface e Fluxo | `@tiago148` | Front | 🟢 OPEN |
| **#39** | Solicitação e aceite do pedido de locação - Backend Java | `@tiago148` | Back | 🟢 OPEN |
| **#15** | Contrato digital simplificado - HTML | `@KauanDias-Dev` | Front | 🟣 CLOSED |
| **#27** | Contrato digital simplificado - CSS | `@KauanDias-Dev` | Front | 🟣 CLOSED |
| **#44** | Geração de PDF do Contrato Digital - Backend Java | `@KauanDias-Dev` | Back | 🟢 OPEN |
| **#42** | Verificação técnica assistida (Checklist) - Interface (HTML/CSS) | `@KauanDias-Dev` | Front | 🟢 OPEN |
| **#43** | Verificação técnica assistida (Checklist) - Backend Java | `@KauanDias-Dev` | Back | 🟢 OPEN |
| **#25** | Chat entre as partes - HTML | `@LeonardoGambaroni` | Front | 🟣 CLOSED |
| **#26** | Chat entre as partes - CSS | `@LeonardoGambaroni` | Front | 🟣 CLOSED |
| **#41** | Lógica de envio e renderização de mensagens no chat (JS) | `@LeonardoGambaroni` | Front | 🟢 OPEN |
| **#40** | Chat em tempo real e WebSockets - Backend Java | `@LeonardoGambaroni` | Back | 🟢 OPEN |
| **#28** | Avaliação pós-locação - HTML | `@lucaxaviers` | Front | 🟣 CLOSED |
| **#29** | Avaliação pós-locação - CSS | `@lucaxaviers` | Front | 🟣 CLOSED |
| **#45** | Avaliação pós-locação e recálculo de reputação - Backend Java | `@lucaxaviers` | Back | 🟢 OPEN |
| **#19** | Global - CSS & Design System | Todo o time | Front | 🟣 CLOSED |
| **#20** | Modelagem do Banco de Dados no MongoDB Atlas | Todo o time | DB | 🟢 OPEN |
| **#21** | Ambiente Java e servidor básico | Todo o time | Back | 🟢 OPEN |
| **#46** | Testes Unitários com JUnit 5 - Regras de Negócio e Cálculos Críticos | Todo o time | Testes | 🟢 OPEN |

---

## 🎯 Escopo Excluído do PI4 (Evoluções Futuras)

Conforme acordado na ficha de avaliação e escopo do projeto, os seguintes itens **NÃO** serão implementados no PI4:
1. Verificação técnica presencial por perito físico.
2. Seguro integrado automatizado.
3. Pagamento e caução online (acerto combinado entre as partes no piloto).
4. Rastreamento GPS por hardware embarcado na máquina.
5. Central de mediação e disputas jurídicas.
6. Aplicativo mobile nativo (foco em Web responsivo).
7. Assinatura premium e planos de destaque.
8. Painel dedicado para cooperativas.
9. Módulo avançado de relatórios e analytics preditivo.
10. Contratação com operador incluso.
