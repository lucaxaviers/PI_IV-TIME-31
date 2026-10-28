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

| # | Título da Issue / Tarefa | Responsável(is) | Camada | Horas | Status |
|:---:|:---|:---|:---:|:---:|:---:|
| **#34** | Cadastro e login de usuários - Interface (HTML e CSS) | `@lucaxaviers` | Front | 3.5h | 🟣 CLOSED |
| **#35** | Autenticação e controle de acesso - Backend Java | `@lucaxaviers` | Back | 6.0h | 🟢 OPEN |
| **#16** | Cadastro de máquina - HTML | `@lucaxaviers` | Front | 2.5h | 🟣 CLOSED |
| **#17** | Cadastro de máquina - CSS | `@lucaxaviers` | Front | 2.5h | 🟣 CLOSED |
| **#18** | Validações dos campos do cadastro de máquina (JavaScript) | `@lucaxaviers` | Front | 3.5h | 🟣 CLOSED |
| **#14** | Busca e catálogo de máquinas - HTML | `@mateusor` | Front | 3.0h | 🟣 CLOSED |
| **#22** | Busca e catálogo de máquinas - CSS | `@mateusor` | Front | 3.0h | 🟣 CLOSED |
| **#37** | Filtros dinâmicos no catálogo de máquinas (JavaScript) | `@mateusor` | Front | 4.0h | 🟢 OPEN |
| **#36** | API REST de Busca Geoespacial de Máquinas - Backend Java | `@mateusor` | Back/DB | 7.0h | 🟢 OPEN |
| **#13** | Detalhe da máquina e frete - HTML | `@tiago148` | Front | 2.5h | 🟣 CLOSED |
| **#23** | Detalhe da máquina e frete - CSS | `@tiago148` | Front | 2.5h | 🟣 CLOSED |
| **#24** | Cálculo de frete estimado (JavaScript) | `@tiago148` | Front/Back | 4.0h | 🟢 OPEN |
| **#38** | Solicitação e aceite do pedido de locação - Interface e Fluxo | `@tiago148` | Front | 3.5h | 🟢 OPEN |
| **#39** | Solicitação e aceite do pedido de locação - Backend Java | `@tiago148` | Back | 6.0h | 🟢 OPEN |
| **#15** | Contrato digital simplificado - HTML | `@KauanDias-Dev` | Front | 2.5h | 🟣 CLOSED |
| **#27** | Contrato digital simplificado - CSS | `@KauanDias-Dev` | Front | 2.5h | 🟣 CLOSED |
| **#44** | Geração de PDF do Contrato Digital - Backend Java | `@KauanDias-Dev` | Back | 6.0h | 🟢 OPEN |
| **#42** | Verificação técnica assistida (Checklist) - Interface (HTML/CSS) | `@KauanDias-Dev` | Front | 3.0h | 🟢 OPEN |
| **#43** | Verificação técnica assistida (Checklist) - Backend Java | `@KauanDias-Dev` | Back | 5.0h | 🟢 OPEN |
| **#25** | Chat entre as partes - HTML | `@LeonardoGambaroni` | Front | 2.5h | 🟣 CLOSED |
| **#26** | Chat entre as partes - CSS | `@LeonardoGambaroni` | Front | 2.5h | 🟣 CLOSED |
| **#41** | Lógica de envio e renderização de mensagens no chat (JS) | `@LeonardoGambaroni` | Front | 4.0h | 🟢 OPEN |
| **#40** | Chat em tempo real e WebSockets - Backend Java | `@LeonardoGambaroni` | Back | 7.0h | 🟢 OPEN |
| **#28** | Avaliação pós-locação - HTML | `@lucaxaviers` | Front | 1.5h | 🟣 CLOSED |
| **#29** | Avaliação pós-locação - CSS | `@lucaxaviers` | Front | 1.5h | 🟣 CLOSED |
| **#45** | Avaliação pós-locação e recálculo de reputação - Backend Java | `@lucaxaviers` | Back | 4.0h | 🟢 OPEN |
| **#19** | Global - CSS & Design System | Todo o time | Front | 3.0h | 🟣 CLOSED |
| **#20** | Modelagem do Banco de Dados no MongoDB Atlas | Todo o time | DB | 3.0h | 🟢 OPEN |
| **#21** | Ambiente Java e servidor básico | Todo o time | Back | 3.0h | 🟢 OPEN |
| **#46** | Testes Unitários com JUnit 5 - Regras de Negócio e Cálculos Críticos | Todo o time | Testes | 4.0h | 🟢 OPEN |

---

## ⏱️ Calibração de Horas Individual — Lucas Rodrigues Xavier (@lucaxaviers)
> **Carga Horária Obrigatória:** 38,0 horas | **Total Calibrado:** **38,0 horas cravadas (sem inflar)**

### 1. Etapa de Front-end, UX e Estrutura (Já Executado): **18,0 horas**
| Issue / Atividade | Descrição Real e Justificativa Técnica | Horas | Status |
| :--- | :--- | :---: | :---: |
| **#16 - Cadastro de Máquina (HTML)** | Estruturação de formulário extenso com upload de fotos, especificações mecânicas, precificação de diária e disponibilidade. | **2,5h** | Concluído |
| **#17 - Cadastro de Máquina (CSS)** | Estilização responsiva em Grid/Flexbox, cards de upload interativos e acessibilidade mobile para uso em campo. | **2,5h** | Concluído |
| **#18 - Validações do Formulário (JS)** | Validação de campos obrigatórios, regras de fotos mínimas, datas válidas e bloqueio de submissão incorreta. | **3,5h** | Concluído |
| **#34 - Login e Cadastro de Usuários (HTML/CSS)** | Interface dupla com tabs dinâmicas, alternância de perfil (Produtor vs Proprietário), máscaras e feedback visual. | **3,5h** | Concluído |
| **#28 & #29 - Avaliação Pós-Locação (HTML/CSS)** | Sistema visual de avaliação com estrelas, critérios de estado do maquinário, pontualidade e depoimento. | **3,0h** | Concluído |
| **#11, #12 & #19 - Setup Git, README & Design System** | Padronização global de estilos (cores, tipografia, header/footer unificado), branches e matriz 5W2H do relatório. | **3,0h** | Concluído |
| **Subtotal Front-end & Planejamento:** | | **18,0h** | |

### 2. Etapa de Backend Java, Banco, Testes e Integração (A Executar): **20,0 horas**
| Issue / Atividade | Descrição Real e Justificativa Técnica | Horas | Status |
| :--- | :--- | :---: | :---: |
| **#35 - Autenticação & Usuários (Backend Java)** | Controllers (`/auth/register`, `/auth/login`), regras de negócio em Service, hash de senhas (BCrypt) e token JWT. | **6,0h** | A Fazer |
| **#45 - Avaliação Pós-Locação (Backend Java)** | Endpoint de registro de avaliação e algoritmo de recálculo da nota média da reputação do usuário no MongoDB. | **4,0h** | A Fazer |
| **#20 & #21 - MongoDB Atlas & Setup Spring Boot** | Modelagem das collections de usuários/avaliações, injeção de dependências e configuração de conexão segura. | **3,0h** | A Fazer |
| **#46 - Testes Automatizados com JUnit 5** | Testes unitários das regras críticas de autenticação, validação de campos e cálculo ponderado da reputação. | **4,0h** | A Fazer |
| **Integração Front-Back & Validação Piloto** | Conexão dos formulários via `fetch()`, testes de integração do fluxo completo de usuário e documentação final. | **3,0h** | A Fazer |
| **Subtotal Backend & Validação:** | | **20,0h** | |

$$\mathbf{18,0\text{h (Front-end Concluído)} + 20,0\text{h (Backend / Testes Futuros)} = \mathbf{38,0\text{ HORAS}}}$$


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
