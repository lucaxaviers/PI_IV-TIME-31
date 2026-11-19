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

## 🎨 Protótipo e Design System (Figma)

O projeto de interface completo do **AlugaMáquina** foi desenvolvido no Figma, cobrindo todas as etapas de fidelidade visual e adaptação responsiva:

🔗 **[Acessar Projeto no Figma: AlugaMáquina - PI IV (Telas)](https://www.figma.com/design/l0OPhWxDRSCT7qE8l8vm8s/AlugaMaquina---PI-IV--Telas-?node-id=0-1&t=WScTFMf1Kxlq3QCb-1)**

O arquivo é estruturado em três camadas principais:
1. **Baixa Fidelidade (Desktop - 1440px):** 9 wireframes em tons de cinza para validação de fluxos, ergonomia e hierarquia da informação.
2. **Alta Fidelidade (Desktop - 1440px):** 9 telas estilizadas com Design System unificado, tokens de cores (`#1A5632`, `#F9FAFB`), tipografia padronizada e componentes reutilizáveis.
3. **Mobile - Alta Fidelidade + Fluxo (375px):** 9 telas responsivas em formato mobile acompanhadas de marcadores visuais do fluxo de navegação da jornada do usuário.

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
│       │   │   ├── config/ (CorsConfig, WebSocketConfig)
│       │   │   ├── controller/ (AuthController, AvaliacaoController, ChatController, LocacaoController, MaquinaBuscaController, StatusController, ContratoController, VistoriaChecklistController)
│       │   │   ├── dto/ (AuthResponse, AvaliacaoRequest, AvaliacaoResponse, CadastroRequest, LocacaoResponse, LoginRequest, MaquinaBuscaResponse, RecusaLocacaoRequest, SolicitacaoLocacaoRequest, GerarContratoRequest, AssinarContratoRequest, ContratoResponse, CriarVistoriaRequest, ConcluirVistoriaRequest, ItemChecklistDto, VistoriaResponse)
│       │   │   ├── exception/ (AutenticacaoException, ConflitoAgendaException, LocacaoNaoEncontradaException, ParametroBuscaInvalidoException, TransicaoStatusInvalidaException, UsuarioJaCadastradoException, ContratoNaoEncontradoException, ContratoConflitoException, AssinaturaNaoPermitidaException, VistoriaNaoEncontradaException, VistoriaConflitoException)
│       │   │   ├── model/ (Avaliacao, ChatMessage, Contrato, ItemChecklist, Locacao, Maquina, StatusLocacao, StatusVistoria, TipoVistoria, Usuario, VistoriaChecklist)
│       │   │   ├── repository/ (AvaliacaoRepository, ContratoRepository, LocacaoRepository, MaquinaRepository, UsuarioRepository, VistoriaChecklistRepository)
│       │   │   └── service/ (AuthService, AvaliacaoService, ContratoService, GeradorPdfContrato, LocacaoService, MaquinaBuscaService, VistoriaChecklistService)
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/alugamaquina/backend/
│               ├── controller/ (StatusControllerTest)
│               ├── model/ (AvaliacaoTest, ContratoBvaTest, ContratoIntraclasseTest, ItemChecklistTest, LocacaoTest, UsuarioTest, VistoriaChecklistBvaTest, VistoriaChecklistIntraclasseTest)
│               ├── repository/ (LocacaoRepositoryQueryTest)
│               └── service/ (AuthServiceBvaTest, AuthServiceTest, AvaliacaoServiceBvaTest, AvaliacaoServiceTest, LocacaoServiceTest, MaquinaBuscaServiceBvaTest, MaquinaBuscaServiceTest, ContratoServiceTest, ContratoInterclasseTest, GeradorPdfContratoTest, VistoriaChecklistServiceTest, VistoriaInterclasseTest)
├── docs/
│   ├── AlugaMaquina_Apresentacao.pptx
│   ├── MODELAGEM_MONGODB.md (Documentação detalhada das collections e índices)
│   ├── Relatorio_Ideacao_AlugaMaquina.docx
│   ├── Relatorio_Ideacao_AlugaMaquina.pdf
│   └── TASKS_PI4_TIME_31.md (Quadro simples de tarefas e responsáveis)
├── src/
│   ├── css/
│   │   ├── global.css
│   │   ├── avaliacao-pos-locacao.css
│   │   ├── busca-catalogo.css
│   │   ├── cadastro-maquina.css
│   │   ├── chat.css
│   │   ├── contrato-digital.css
│   │   ├── detalhe-maquina.css
│   │   ├── login-usuario.css
│   │   └── vistoria-checklist.css
│   ├── js/
│   │   ├── avaliacao-pos-locacao.js
│   │   ├── busca-catalogo.js
│   │   ├── cadastro-maquina.js
│   │   ├── chat.js
│   │   ├── contrato-digital.js
│   │   ├── frete.js
│   │   ├── login-usuario.js
│   │   └── vistoria-checklist.js
│   └── pages/
│       ├── avaliacao-pos-locacao.html
│       ├── busca-catalogo.html
│       ├── cadastro-maquina.html
│       ├── chat.html
│       ├── contrato-digital.html
│       ├── detalhe-maquina.html
│       ├── login-usuario.html
│       └── vistoria-checklist.html
├── .gitignore
└── README.md
```

---

## 📌 Documentação e Entregas (Etapa de Ideação & Planejamento)

Todos os artefatos oficiais de ideação e escopo encontram-se organizados na pasta [`docs/`](./docs/):
- **Tarefas do PI4 (`TASKS_PI4_TIME_31.md`):** Quadro simples com as tarefas e os responsáveis de cada integrante.
- **Modelagem do Banco de Dados MongoDB (`MODELAGEM_MONGODB.md`):** Documentação técnica das collections, índices NoSQL, validação de schemas e regras de integridade.
- **Apresentação da Solução (`AlugaMaquina_Apresentacao.pptx`):** Slides executivos com problema, proposta de valor, modelo de negócio, persona e roadmap do MVP.
- **Relatório Consolidado de Ideação (`Relatorio_Ideacao_AlugaMaquina.pdf` / `.docx`):** Documento oficial completo reunindo contexto, personas, fundamentação quantitativa (IBGE/Embrapa/Cepea), Mapa de Empatia, Matriz SWOT e SWOT Cruzada, Business Model Canvas (BMC), Mapa Mental, 5W2H e wireframes do MVP.
- **Identidade Visual (`assets/`):**
  - 🎨 **Logotipo:** [`assets/logo-AlugaMaquina.jpg`](./assets/logo-AlugaMaquina.jpg) — Marca visual oficial completa do AlugaMáquina.
  - 🚜 **Ícone / Favicon:** [`assets/ico-AlugaMaquina.jpg`](./assets/ico-AlugaMaquina.jpg) — Versão compacta para favicon, atalhos e avatar mobile.

---

## 🛠️ Tecnologias e Ferramentas (Stack Documentada)

Conforme formalizado no **Relatório de Ideação** e na **Ficha Oficial de Escopo do PI4**, a arquitetura do AlugaMáquina é estruturada nas seguintes tecnologias:

| Camada / Função | Tecnologia | Descrição no Projeto |
| :--- | :--- | :--- |
| **Prototipação & UX** | **Figma** | Prototipação em Baixa Fidelidade, Alta Fidelidade e Mobile com fluxo de navegação documentado. |
| **Frontend** | **HTML5, CSS3 & JavaScript (ES6+)** | Construção da estrutura semântica responsiva adaptada ao homem do campo, com design system unificado (`global.css`) e scripts client-side modulares. |
| **Backend** | **Java (Spring Boot 3.3)** | API REST estruturada com injeção de dependências, arquitetura em camadas (Controller, Service, Repository, DTO) e regras de negócio. |
| **Banco de Dados** | **MongoDB Atlas** | Banco de dados NoSQL gerenciado em nuvem com suporte geoespacial nativo (`2dsphere` / `geoNear`) para busca por raio de distância (km). |
| **Testes de Software** | **JUnit 5 & Mockito** | Cobertura abrangente de testes unitários automatizados cobrindo métodos canônicos e regras críticas de negócio (AAA + BVA + Particionamento de Equivalência). |
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
- [x] Prototipação completa no Figma (Baixa Fidelidade, Alta Fidelidade e Mobile com Fluxo)
- [x] Implementação, estilização e interligação de todos os protótipos de interface HTML5/CSS3 do MVP
- [x] Inicialização da arquitetura Backend em Java (Spring Boot REST API)
- [x] Implementação dos módulos de Autenticação (`#35`) e Busca Geoespacial de Máquinas (`#36`)
- [x] Módulo de Solicitação e Aceite de Locação com Cálculo de Frete (`#39`)
- [x] Módulo de Avaliação Pós-Locação e recálculo de reputação no backend (`#45`)
- [x] Suíte de testes unitários automatizados com JUnit 5 e Mockito
- [x] Documentação formal da modelagem NoSQL MongoDB (`docs/MODELAGEM_MONGODB.md`)
- [x] Integração das páginas de Login, Cadastro e Avaliação Pós-Locação com a API REST
- [ ] Integração completa dos demais endpoints REST com o front-end e conexão final com MongoDB Atlas
<!-- Kauan Aurélio Lasmar Dias | 25001590 - itens abaixo: contrato digital em PDF e vistoria técnica (checklist) -->
- [x] Módulo de Contrato Digital em PDF (`#44`) no backend e integração da tela de contrato com a API
- [x] Módulo de Vistoria Técnica com checklist (`#43` backend e `#42` tela)
