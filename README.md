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
| **Frontend** | **TypeScript / JavaScript** | Tipagem estática e lógica da aplicação web/mobile, reduzindo erros em tempo de desenvolvimento. |
| **Interface / Estilo** | **HTML e CSS** | Construção da estrutura semântica responsiva adaptada ao homem do campo, com design system unificado. |
| **Backend** | **Java** | Backend estruturado para implementação das regras de negócio, contratos e API REST consumida pelo frontend. |
| **Banco de Dados** | **MongoDB Atlas** | Banco de dados NoSQL gerenciado em nuvem com suporte geoespacial nativo (`2dsphere`) para busca por raio de distância (km). |
| **Testes de Software** | **JUnit 5** | Cobertura obrigatória de testes unitários validando as regras de negócio e cálculos críticos. |
| **Suporte & Comunicação** | **WhatsApp Business API** | Reforço de atendimento e validação rápida da comunicação direta entre locador e locatário. |
| **Deploy & Nuvem (MVP)** | **Ambiente em Nuvem (Vercel / Supabase)** | Hospedagem ágil e provisionamento da versão piloto do MVP. |

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
- [ ] Desenvolvimento da camada Backend em Java (API REST com Spring Boot)
- [ ] Configuração do banco de dados MongoDB Atlas (collections e índices geoespaciais `2dsphere`)
- [ ] Implementação da suíte de testes unitários com JUnit 5
