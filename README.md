<div align="center">
  <img src="assets/logo-AlugaMaquina.jpg" alt="Logo AlugaMáquina" width="220" />

  # AlugaMáquina 🚜🏗️
  
  > **Projeto Integrador IV (PI4) — PUC-Campinas**  
  > **Time:** PI_IV TIME 31

  *Plataforma inteligente para locação e gestão de máquinas e equipamentos para o agronegócio, construção civil e indústria.*
</div>

---

## 👥 Integrantes da Equipe (Time 31)

| Integrante | RA | Papel | GitHub |
| :--- | :---: | :--- | :--- |
| **Lucas Rodrigues Xavier** | 25000508 | Integrante | [@lucaxaviers](https://github.com/lucaxaviers) |
| **Leonardo Gambaroni Alves** | 25003494 | Integrante | [@LeonardoGambaroni](https://github.com/LeonardoGambaroni) |
| **Mateus Oliveira Rafael** | 25001369 | Integrante | [@mateusor](https://github.com/mateusor) |
| **Kauan Aurélio Lasmar Dias** | 25001590 | Integrante | [@KauanDias-Dev](https://github.com/KauanDias-Dev) |
| **Tiago Medeiros** | 25000845 | Integrante | [@tiago148](https://github.com/tiago148) |

**Docente / Orientação:** [Prof.ª Renata Arantes](https://github.com/RenataArantes) ([@RenataArantes](https://github.com/RenataArantes))

---

## 📁 Estrutura do Repositório

```text
├── assets/
│   ├── ico-AlugaMaquina.jpg
│   ├── logo-AlugaMaquina.jpg
│   ├── logo-header.png
│   └── maquinas/ (ilustrações SVG dos equipamentos)
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/alugamaquina/backend/
│       │   │   ├── BackendApplication.java
│       │   │   ├── config/ (WebSocketConfig)
│       │   │   ├── controller/ (AuthController, ChatController, MaquinaBuscaController)
│       │   │   ├── dto/ (AuthResponse, CadastroRequest, LoginRequest, MaquinaBuscaResponse)
│       │   │   ├── exception/ (AutenticacaoException, UsuarioJaCadastradoException, etc.)
│       │   │   ├── model/ (ChatMessage, Maquina, Usuario)
│       │   │   ├── repository/ (UsuarioRepository)
│       │   │   └── service/ (AuthService, MaquinaBuscaService)
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/alugamaquina/backend/service/ (AuthServiceTest, MaquinaBuscaServiceTest)
├── docs/
│   ├── AlugaMaquina_Apresentacao.pptx
│   ├── Relatorio_Ideacao_AlugaMaquina.docx
│   ├── Relatorio_Ideacao_AlugaMaquina.pdf
│   └── TASKS_PI4_TIME_31.md (Backlog oficial e mapeamento de tarefas)
├── src/
│   ├── css/
│   │   ├── global.css
│   │   ├── avaliacao-pos-locacao.css
│   │   ├── busca-catalogo.css
│   │   ├── cadastro-maquina.css
│   │   ├── chat.css
│   │   ├── contrato-digital.css
│   │   ├── detalhe-maquina.css
│   │   └── login-usuario.css
│   ├── js/
│   │   ├── busca-catalogo.js
│   │   ├── cadastro-maquina.js
│   │   ├── chat.js
│   │   └── frete.js
│   └── pages/
│       ├── avaliacao-pos-locacao.html
│       ├── busca-catalogo.html
│       ├── cadastro-maquina.html
│       ├── chat.html
│       ├── contrato-digital.html
│       ├── detalhe-maquina.html
│       └── login-usuario.html
├── .gitignore
└── README.md
```

---

## 📌 Documentação e Entregas (Etapa de Ideação & Planejamento)

Todos os artefatos oficiais de ideação e escopo encontram-se organizados na pasta [`docs/`](./docs/):
- **Backlog & Matriz de Tarefas do PI4 (`TASKS_PI4_TIME_31.md`)**: Planejamento oficial detalhado com as 9 funcionalidades obrigatórias, tecnologias exigidas e alocação por integrante.
- **Apresentação da Solução (`AlugaMaquina_Apresentacao.pptx`)**: Slides executivos com problema, proposta de valor, modelo de negócio, persona e roadmap do MVP.
- **Relatório Consolidado de Ideação (`Relatorio_Ideacao_AlugaMaquina.pdf` / `.docx`)**: Documento oficial completo reunindo contexto, personas, fundamentação quantitativa (IBGE/Embrapa/Cepea), Mapa de Empatia, Matriz SWOT e SWOT Cruzada, Business Model Canvas (BMC), Mapa Mental, 5W2H e wireframes do MVP.
- **Identidade Visual (`assets/`)**:
  - 🎨 **Logotipo:** [`assets/logo-AlugaMaquina.jpg`](./assets/logo-AlugaMaquina.jpg) — Marca visual oficial completa do AlugaMáquina.
  - 🚜 **Ícone / Favicon:** [`assets/ico-AlugaMaquina.jpg`](./assets/ico-AlugaMaquina.jpg) — Versão compacta para favicon, atalhos e avatar mobile.

---

## 🛠️ Tecnologias e Ferramentas (Stack Documentada)

Conforme formalizado no **Relatório de Ideação** e na **Ficha Oficial de Escopo do PI4**, a arquitetura do AlugaMáquina é estruturada nas seguintes tecnologias:

| Camada / Função | Tecnologia | Descrição no Projeto |
| :--- | :--- | :--- |
| **Frontend** | **HTML5, CSS3 & JavaScript (ES6+)** | Construção da estrutura semântica responsiva adaptada ao homem do campo, com design system unificado (`global.css`) e scripts client-side modulares. |
| **Backend** | **Java (Spring Boot 3.3)** | API REST estruturada com injeção de dependências, arquitetura em camadas (Controller, Service, Repository, DTO) e regras de negócio. |
| **Banco de Dados** | **MongoDB Atlas** | Banco de dados NoSQL gerenciado em nuvem com suporte geoespacial nativo (`2dsphere` / `geoNear`) para busca por raio de distância (km). |
| **Testes de Software** | **JUnit 5 & Mockito** | Cobertura obrigatória de testes unitários automatizados validando regras críticas de negócio e segurança. |
| **Gerenciador de Dependências** | **Apache Maven** | Automação de compilação, gestão de ciclo de vida e dependências do backend Java (`pom.xml`). |
| **Deploy & Nuvem (MVP)** | **Ambiente em Nuvem (Vercel / MongoDB Cloud)** | Hospedagem ágil e provisionamento da versão piloto do MVP. |

---

## 📋 Gestão do Projeto (GitHub Projects)

O acompanhamento das tarefas, sprints e entregas da equipe está centralizado no quadro oficial do projeto:  
🔗 **[Quadro de Tarefas: PI_IV TIME 31 - AlugaMáquina](https://github.com/users/lucaxaviers/projects/8)**

---

## 🎯 Próximos Passos (Ciclo de Desenvolvimento)

- [x] Criação e organização da pasta de documentação (`docs/`)
- [x] Criação do repositório remoto no GitHub (`PI_IV-TIME-31`)
- [x] Envio e aceite de convites de todos os colaboradores e docente
- [x] Configuração do [GitHub Projects](https://github.com/users/lucaxaviers/projects/8) vinculado ao repositório
- [x] Mapeamento oficial de tarefas do PI4 com base na devolutiva de escopo da orientação ([docs/TASKS_PI4_TIME_31.md](./docs/TASKS_PI4_TIME_31.md))
- [x] Implementação, estilização e interligação de todos os protótipos de interface HTML5/CSS3 do MVP
- [x] Inicialização da arquitetura Backend em Java (Spring Boot REST API)
- [x] Implementação dos módulos de Autenticação (`#35`) e Busca Geoespacial de Máquinas (`#36`)
- [x] Início da suíte de testes unitários com JUnit 5 e Mockito
- [ ] Integração completa dos endpoints REST com as páginas do front-end
- [ ] Módulos de Contrato Digital em PDF (`#44`) e Avaliação Pós-Locação (`#45`) no backend
