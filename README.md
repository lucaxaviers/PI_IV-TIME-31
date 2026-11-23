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
├── .env.example
├── .gitignore
├── docker-compose.yml
├── README.md
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
│       │   │   ├── config/ (WebConfig, WebSocketConfig)
│       │   │   ├── controller/ (AuthController, AvaliacaoController, ChatController, ContratoController, LocacaoController, MaquinaBuscaController, MaquinaCadastroController, StatusController, VistoriaChecklistController)
│       │   │   ├── dto/ (AssinarContratoRequest, AuthResponse, AvaliacaoRequest, AvaliacaoResponse, CadastroRequest, ConcluirVistoriaRequest, ContratoResponse, CriarVistoriaRequest, GerarContratoRequest, ItemChecklistDto, LocacaoResponse, LoginRequest, MaquinaBuscaResponse, MaquinaCadastroRequest, MaquinaCadastroResponse, MaquinaDetalheResponse, RecusaLocacaoRequest, SolicitacaoLocacaoRequest, VistoriaResponse)
│       │   │   ├── exception/ (AssinaturaNaoPermitidaException, AutenticacaoException, ConflitoAgendaException, ContratoConflitoException, ContratoNaoEncontradoException, LocacaoNaoEncontradaException, MaquinaNaoEncontradaException, ParametroBuscaInvalidoException, TransicaoStatusInvalidaException, UsuarioJaCadastradoException, VistoriaConflitoException, VistoriaNaoEncontradaException)
│       │   │   ├── model/ (Avaliacao, ChatMessage, Contrato, ItemChecklist, Locacao, Maquina, MunicipioAtendido, StatusLocacao, StatusVistoria, TipoEquipamento, TipoVistoria, Usuario, VistoriaChecklist)
│       │   │   ├── repository/ (AvaliacaoRepository, ChatMessageRepository, ContratoRepository, LocacaoRepository, MaquinaRepository, UsuarioRepository, VistoriaChecklistRepository)
│       │   │   └── service/ (AuthService, AvaliacaoService, ChatMessageService, ContratoService, GeradorPdfContrato, LocacaoService, MaquinaBuscaService, MaquinaCadastroService, VistoriaChecklistService)
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/alugamaquina/backend/
│               ├── controller/ (LocacaoControllerConsultaTest, StatusControllerTest)
│               ├── dto/ (MaquinaCadastroRequestTest)
│               ├── model/ (AvaliacaoIntraclasseTest, AvaliacaoTest, CadastroEnumsTest, ChatMessageIntraclasseTest, ChatMessageModelTest, ContratoBvaTest, ContratoIntraclasseTest, ItemChecklistTest, LocacaoIntraclasseTest, LocacaoTest, MaquinaTest, UsuarioIntraclasseTest, UsuarioTest, VistoriaChecklistBvaTest, VistoriaChecklistIntraclasseTest)
│               ├── repository/ (LocacaoRepositoryListagemQueryTest, LocacaoRepositoryQueryTest)
│               └── service/ (AuthServiceBvaTest, AuthServiceTest, AvaliacaoInterclasseTest, AvaliacaoServiceBvaTest, AvaliacaoServiceTest, ChatInterclasseTest, ChatMessageServiceTest, ContratoInterclasseTest, ContratoServiceTest, GeradorPdfContratoTest, LocacaoConsultaServiceTest, LocacaoServiceTest, MaquinaBuscaServiceBuscaTest, MaquinaBuscaServiceBvaTest, MaquinaBuscaServiceTest, MaquinaCadastroInterclasseTest, MaquinaCadastroServiceTest, VistoriaChecklistServiceTest, VistoriaInterclasseTest)
├── docs/
│   ├── AlugaMaquina_Apresentacao.pptx
│   ├── DICIONARIO_DE_DADOS.md (Dicionário de dados completo e desdobramento NoSQL)
│   ├── MODELAGEM_MONGODB.md (Documentação detalhada das collections e índices)
│   ├── RELATORIO_TESTES_SERVIDOR.md (Resumo do relatório de testes do servidor Java)
│   ├── Relatorio_Ideacao_AlugaMaquina.docx
│   ├── Relatorio_Ideacao_AlugaMaquina.pdf
│   ├── Relatorio_Testes_Servidor_Ivan.docx (Relatório completo FTTVS com diagramas UML e JUnit 5)
│   └── TASKS_PI4_TIME_31.md (Quadro simples de tarefas e responsáveis)
├── mongodb/
│   └── init-mongo.js (Script de criação de schemas, índices e seed inicial)
├── scripts/
│   └── start-dev.ps1 (Inicialização unificada: Docker/MongoDB Atlas + Backend Spring Boot + Servidor TCP POO + Cliente TCP + Frontend)
├── servidor-poo/
│   ├── README.md (Documentação e comandos do servidor TCP puro)
│   ├── Cliente/ (Cliente.java, TratadoraDeComunicadoDeDesligamento.java e Comunicados)
│   └── Servidor/ (Servidor.java, AceitadoraDeConexao.java, SupervisoraDeConexao.java, Parceiro.java, MaquinaAgricola.java, RepositorioAlugaMaquina.java)
└── src/
    ├── css/
    │   ├── global.css
    │   ├── avaliacao-pos-locacao.css
    │   ├── busca-catalogo.css
    │   ├── cadastro-maquina.css
    │   ├── chat.css
    │   ├── contrato-digital.css
    │   ├── detalhe-maquina.css
    │   ├── login-usuario.css
    │   ├── meus-contratos.css
    │   └── vistoria-checklist.css
    ├── js/
    │   ├── auth-guard.js
    │   ├── avaliacao-pos-locacao.js
    │   ├── busca-catalogo.js
    │   ├── cadastro-maquina.js
    │   ├── chat.js
    │   ├── contrato-digital.js
    │   ├── detalhe-maquina.js
    │   ├── frete.js
    │   ├── login-usuario.js
    │   ├── meus-contratos.js
    │   └── vistoria-checklist.js
    └── pages/
        ├── avaliacao-pos-locacao.html
        ├── busca-catalogo.html
        ├── cadastro-maquina.html
        ├── chat.html
        ├── contrato-digital.html
        ├── detalhe-maquina.html
        ├── login-usuario.html
        ├── meus-contratos.html
        └── vistoria-checklist.html
```

---

## 📌 Documentação e Entregas (Etapa de Ideação & Planejamento)

Todos os artefatos oficiais de ideação e escopo encontram-se organizados na pasta [`docs/`](./docs/):
- **Tarefas do PI4 (`TASKS_PI4_TIME_31.md`):** Quadro simples com as tarefas e os responsáveis de cada integrante.
- **Dicionário de Dados (`DICIONARIO_DE_DADOS.md`):** Dicionário completo das 7 entidades do sistema, regras de domínio, exemplos aceitos/não aceitos e mapeamento físico para o MongoDB.
- **Modelagem do Banco de Dados MongoDB (`MODELAGEM_MONGODB.md`):** Documentação técnica das collections, índices NoSQL, validação de schemas e regras de integridade.
- **Ambiente Local e Seed (`docker-compose.yml`, `mongodb/init-mongo.js`, `scripts/start-dev.ps1`):** Provisionamento containerizado do MongoDB, criação de índices/schemas, carga inicial das 7 coleções e inicialização unificada dos servidores e frontend.
- **Relatório de Testes do Servidor (`docs/Relatorio_Testes_Servidor_Ivan.docx` / `docs/RELATORIO_TESTES_SERVIDOR.md`):** Arquitetura em camadas, diagramas UML completos (classes, DTE e sequência), testes Intraclasse/Interclasse e suíte de 662 testes JUnit 5 aprovados.
- **Servidor Java TCP Puro (`servidor-poo/`):** Implementação dedicada sem HTTP (`ServerSocket`, `Thread`, `ObjectInputStream`/`ObjectOutputStream`, `Parceiro`, `Comunicado`), documentada em [`servidor-poo/README.md`](./servidor-poo/README.md).
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
| **Backend (API Web)** | **Java (Spring Boot 3.3)** | API REST estruturada com injeção de dependências, arquitetura em camadas (Controller, Service, Repository, DTO) e regras de negócio. |
| **Servidor TCP (POO/Java)** | **Java Sockets Puro (Zero HTTP)** | Servidor e Cliente dedicados em `servidor-poo/` usando `ServerSocket`, `Thread`, `ObjectInputStream`/`ObjectOutputStream` e `Semaphore`. |
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
- [x] Documentação formal da modelagem NoSQL MongoDB (`docs/MODELAGEM_MONGODB.md`), Dicionário de Dados (`docs/DICIONARIO_DE_DADOS.md`) e script de seed (`mongodb/init-mongo.js`)
- [x] Módulo de Chat em Tempo Real via WebSocket com persistência e histórico REST (`#40`, `#41`, `#52`)
- [x] Integração das páginas de Login, Cadastro de Usuário, Cadastro de Máquinas, Catálogo, Detalhe da Máquina, Avaliação Pós-Locação, Meus Contratos e Consulta de Locações com a API REST
<!-- Kauan Aurélio Lasmar Dias | 25001590 - itens abaixo: contrato digital em PDF e vistoria técnica (checklist) -->
- [x] Módulo de Contrato Digital em PDF (`#44`) no backend e integração da tela de contrato com a API
- [x] Módulo de Vistoria Técnica com checklist (`#43` backend e `#42` tela)
- [x] Conexão final com o cluster MongoDB Atlas e população das 7 coleções via `mongodb/init-mongo.js`
- [x] Servidor TCP dedicado em Java puro no padrão POO/Fazedor de Continhas (`servidor-poo/`) e inicialização unificada via `scripts/start-dev.ps1`
- [ ] Integração do envio de locação em `detalhe-maquina.html` (`POST /api/locacoes/solicitar`)

---

## ☁️ Configuração do MongoDB Atlas

Por padrão local, o `backend/src/main/resources/application.properties` usa `MONGODB_URI` apontando para `mongodb://localhost:27017/alugamaquina`. Para executar o projeto conectado ao **MongoDB Atlas**:

1. **Cluster, usuário e Network Access no Atlas:**
   - Confirme o cluster ativo, o banco da aplicação e um usuário em **Database Access** com permissão de leitura e escrita (`readWriteAnyDatabase` ou `readWrite` + `dbAdmin` no banco).
   - Em **Network Access**, libere o IP de quem executará o backend (`0.0.0.0/0` é aceitável para testes em ambiente acadêmico).
2. **Definir a `MONGODB_URI` apenas na sessão local (sem versionar segredos):**
   - No PowerShell, defina a variável somente para a sessão atual usando sua string SRV real no lugar dos placeholders (observando que o nome do banco no MongoDB diferencia maiúsculas e minúsculas, como `AlugaMaquina` ou `alugamaquina`):
     ```powershell
     $env:MONGODB_URI = "mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/<banco>?retryWrites=true&w=majority"
     ```
   - Como alternativa local, copie `.env.example` para `.env` (já incluído no `.gitignore`) e preencha `MONGODB_URI`. **Nunca commite `.env`, senhas ou URIs reais.**
3. **Popular as coleções e criar os índices (incluindo `2dsphere`):**
   ```powershell
   mongosh "$env:MONGODB_URI" mongodb/init-mongo.js
   ```
4. **Iniciar o backend e validar:**
   - Execute `.\scripts\start-dev.ps1` (quando `MONGODB_URI` aponta para um host remoto/Atlas, o script pula automaticamente o Docker local) ou `mvn spring-boot:run` na pasta `backend/`.
   - Valide `GET http://localhost:8080/api/maquinas/busca?lat=-22.7611&lng=-47.1544&raioKm=50` e `POST http://localhost:8080/api/auth/login` com um usuário do seed.
