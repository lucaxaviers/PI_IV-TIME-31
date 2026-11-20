# Dicionário de Dados: Plataforma AlugaMáquina

> **Projeto Integrador IV (Time 31) — Engenharia de Software / PUC-Campinas**  
> Este documento consolida o dicionário de dados do sistema **AlugaMáquina**, descrevendo primeiro as entidades, atributos, tipos conceituais, exemplos aceitos/não aceitos e regras de domínio, e em seguida o desdobramento físico em coleções de documentos no **MongoDB** (além de consultas e índices previstos).

O domínio do AlugaMáquina contempla o cadastro de produtores rurais (locadores e locatários), anúncio e busca geoespacial de máquinas e equipamentos agrícolas, cálculo de frete e solicitação/gestão de locações, troca de mensagens em tempo real via chat, emissão e assinatura de contratos digitais em PDF, vistoria técnica assistida por checklist (retirada e devolução) e avaliação pós-locação com recálculo de reputação.

---

## 1. Histórico de versões

| Data | Autor | Versão | Comentários |
| :---: | :--- | :---: | :--- |
| 19/11/2026 | Leonardo Gambaroni Alves (RA 25003494) | 1.0.0 | Versão inicial do dicionário de dados do AlugaMáquina cobrindo as 7 entidades do sistema e desdobramento no MongoDB. |

---

## 2. Entidades e atributos conceituais

### 2.1. Usuário

Representa o produtor rural ou empresa cadastrada na plataforma, podendo atuar como locatário (quem aluga máquinas), locador (quem anuncia máquinas) ou ambos. Armazena dados pessoais, credenciais de acesso em hash e indicadores de reputação calculados a partir das avaliações recebidas.

#### Atributos de Usuário

| Atributo / Dado | Tipo conceitual | Obrigatório | Característica | Ex. aceitos | Ex. não aceitos | Comentários |
| :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| `id` | Identificador | Sim | Simples | `"usr-locador-01"`, `"6740a1..."` | vazio, nulo | Identificador único do usuário gerado pelo banco. |
| `nome` | Texto | Sim | Simples | `"Jose Carlos - Agricola Sao Jose"`, `"Ana"` | `""`, `"Ab"` (menos de 3 caracteres) | Nome completo ou razão social (mínimo 3 caracteres). |
| `documento` | Documento de identificação | Sim | Simples | `"11122233344"`, `"12345678000199"` | `"123"`, `"1234567890"` | CPF ou CNPJ armazenado apenas com dígitos numéricos (mínimo 11 dígitos); único na base. |
| `telefone` | Texto | Não | Simples | `"(19) 99123-4567"` | `—` | Telefone ou WhatsApp para contato entre as partes; opcional no cadastro. |
| `email` | Texto | Sim | Simples | `"antonio.pereira@alugamaquina.com.br"` | `"emailsemarroba"`, `""` | Endereço eletrônico normalizado em minúsculas; deve conter `@` e é único na base. |
| `municipio` | Texto | Não | Simples | `"paulinia"`, `"cosmopolis"` | `—` | Código do município base do usuário. |
| `perfil` | Texto enumerado | Sim | Simples | `"locatario"`, `"locador"`, `"ambos"` | `"admin"`, valor fora da lista | Papel do usuário na plataforma; padrão `"ambos"` se omitido. |
| `senhaHash` | Texto (Hash SHA-256) | Sim | Simples | Hash hexadecimal de 64 caracteres | senha em texto puro, string menor que 64 chars | Derivado de senha com no mínimo 6 caracteres; nunca exposto no `toString()` nem nas respostas da API. |
| `reputacaoMedia` | Número decimal | Não | Simples | `5.0`, `4.67` | `-1.0`, `6.0` | Média das avaliações recebidas como locador (0 a 5); inicia neutra em `5.0`. |
| `totalAvaliacoes` | Número inteiro | Não | Simples | `0`, `12` | `-1` | Quantidade acumulada de avaliações recebidas pelo locador; inicia em `0`. |

---

### 2.2. Máquina

Representa um equipamento agrícola ou implemento anunciado na plataforma para locação regional, contendo título, categoria, município de origem, valor da diária e coordenadas geográficas para busca por raio de distância.

#### Atributos de Máquina

| Atributo / Dado | Tipo conceitual | Obrigatório | Característica | Ex. aceitos | Ex. não aceitos | Comentários |
| :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| `id` | Identificador | Sim | Simples | `"maq-001"`, `"6740b2..."` | vazio, nulo | Identificador único da máquina no catálogo. |
| `tituloAnuncio` | Texto | Sim | Simples | `"Trator John Deere 6115J Cabinado"` | `"abcd"` (< 5 chars), texto > 100 chars | Título exibido nos cards do catálogo (entre 5 e 100 caracteres no cadastro). |
| `tipoEquipamento` | Texto enumerado | Sim | Simples | `"tratores"`, `"colheitadeiras"`, `"pulverizadores"`, `"plantadeiras"`, `"implementos"` | `"caminhao"`, `""` | Categoria normalizada no plural conforme `TipoEquipamento` para filtros do catálogo. |
| `municipio` | Texto | Sim | Simples | `"Paulínia / SP"`, `"Cosmópolis / SP"` | `"Curitiba / PR"`, `""` | Nome do município atendido (`MunicipioAtendido`) onde o equipamento se encontra. |
| `valorDiaria` | Moeda (BRL) | Sim | Simples | `850.00`, `2400.00` | `0`, `-100.00`, `50001.00` | Valor da diária em reais com 2 casas decimais (> 0 e até R$ 50.000,00). |
| `localizacao` | Ponto Geoespacial (GeoJSON) | Sim | Composto (C) | `{ type: "Point", coordinates: [-47.1100, -22.7100] }` | coordenadas invertidas ou fora de `[-180,180] / [-90,90]` | Coordenada geográfica no formato `[longitude, latitude]` indexada com `2dsphere`. |

---

### 2.3. Locação

Representa o pedido de reserva e o ciclo de contratação de uma máquina entre um locatário e um locador em um período específico de datas, registrando os valores calculados de diárias e frete, o status atual e eventual motivo de recusa.

#### Atributos de Locação

| Atributo / Dado | Tipo conceitual | Obrigatório | Característica | Ex. aceitos | Ex. não aceitos | Comentários |
| :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| `id` | Identificador | Sim | Simples | `"LOC-2026-0901"`, `"6740c3..."` | vazio, nulo | Identificador único da solicitação de locação. |
| `locadorId` | Identificador (Ref. Usuário) | Sim | Simples | `"usr-locador-01"` | `""`, nulo | Referência ao `Usuario` proprietário da máquina. |
| `locatarioId` | Identificador (Ref. Usuário) | Sim | Simples | `"usr-locatario-01"` | `""`, nulo | Referência ao `Usuario` que solicitou o aluguel. |
| `maquinaId` | Identificador (Ref. Máquina) | Sim | Simples | `"maq-001"` | `""`, nulo | Referência à `Maquina` reservada. |
| `dataInicio` | Data | Sim | Simples | `2026-11-25` | `null` | Data de início da locação (contada como diária). |
| `dataFim` | Data | Sim | Simples | `2026-11-29` | data anterior a `dataInicio` | Data de término da locação (`dataFim >= dataInicio`; diárias = dias entre datas + 1). |
| `valorDiaria` | Moeda (BRL) | Sim | Simples | `850.00` | `0`, `-50.00` | Valor unitário da diária em reais, sempre normalizado com 2 casas decimais. |
| `valorFrete` | Moeda (BRL) | Sim | Simples | `0.00`, `280.00` | `-10.00` | Calculado como `distanciaKm * 2 * 6.50 + 150.00` (ou `0.00` para `0 km`). |
| `valorTotal` | Moeda (BRL) | Sim | Simples | `4530.00`, `7080.00` | `0`, valor negativo | Soma de `valorDiaria * diarias + valorFrete` com 2 casas decimais. |
| `status` | Texto enumerado | Sim | Simples | `"PENDENTE"`, `"ACEITO"`, `"RECUSADO"`, `"CANCELADO"`, `"FINALIZADO"` | `"EM_ANALISE"` | Nasce `"PENDENTE"`; apenas `"PENDENTE"` pode transitar para `"ACEITO"` ou `"RECUSADO"`. |
| `motivoRecusa` | Texto | Não | Simples | `"Equipamento em manutenção preventiva no período"` | `—` | Justificativa opcional preenchida pelo locador ao recusar o pedido. |
| `dataSolicitacao` | Data e Hora | Sim | Simples | `2026-11-18T09:15:00` | `—` | Carimbo temporal automático da criação do pedido. |

---

### 2.4. Avaliação

Representa o feedback pós-locação registrado pelo locatário sobre a máquina e o atendimento do locador em cinco critérios objetivos (notas de 1 a 5 estrelas), além da média calculada, comentário textual e recomendação geral.

#### Atributos de Avaliação

| Atributo / Dado | Tipo conceitual | Obrigatório | Característica | Ex. aceitos | Ex. não aceitos | Comentários |
| :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| `id` | Identificador | Sim | Simples | `"aval-001"` | vazio, nulo | Identificador único da avaliação. |
| `locacaoId` | Identificador (Ref. Locação) | Sim | Simples | `"LOC-2026-0842"` | `""`, nulo | Referência única à locação avaliada (1 avaliação por locação). |
| `locadorId` | Identificador (Ref. Usuário) | Sim | Simples | `"usr-locador-01"` | `""`, nulo | Referência ao locador cuja reputação é recalculada. |
| `locatarioId` | Identificador (Ref. Usuário) | Não | Simples | `"usr-locatario-01"` | `—` | Referência ao locatário autor da avaliação. |
| `maquinaId` | Identificador (Ref. Máquina) | Não | Simples | `"maq-001"` | `—` | Referência à máquina avaliada. |
| `notaDesempenho` | Número inteiro | Sim | Simples | `1`, `3`, `5` | `0`, `6`, `-1` | Nota de desempenho e potência mecânica (escala inteira de `1` a `5`). |
| `notaConservacao` | Número inteiro | Sim | Simples | `1`, `4`, `5` | `0`, `6` | Nota de conservação e limpeza na entrega (escala inteira de `1` a `5`). |
| `notaFidelidade` | Número inteiro | Sim | Simples | `1`, `5` | `0`, `6` | Nota de fidelidade ao anúncio e horímetro (escala inteira de `1` a `5`). |
| `notaPontualidade` | Número inteiro | Sim | Simples | `1`, `4`, `5` | `0`, `6` | Nota de pontualidade no transporte e entrega (escala inteira de `1` a `5`). |
| `notaSuporte` | Número inteiro | Sim | Simples | `1`, `5` | `0`, `6` | Nota de comunicação e suporte técnico (escala inteira de `1` a `5`). |
| `mediaFinal` | Número decimal | Sim | Simples | `4.60`, `4.67` | `0.5`, `5.5` | Média aritmética das 5 notas arredondada para 2 casas decimais (`HALF_UP`). |
| `comentario` | Texto | Não | Simples | `"Equipamento em ótimo estado, atendeu perfeitamente ao plantio."` | `—` | Relato detalhado sobre a experiência de uso (texto aparado). |
| `recomenda` | Booleano | Não | Simples | `true`, `false` | `"sim"` | Indica se o locatário recomenda o equipamento e o locador. |
| `dataAvaliacao` | Data e Hora | Sim | Simples | `2026-10-19T15:00:00` | `—` | Data e hora do registro da avaliação. |

---

### 2.5. Mensagem de Chat

Representa uma mensagem trocada em tempo real entre locador e locatário durante a negociação, trafegada via WebSocket (STOMP/SockJS) e persistida para recuperação de histórico cronológico.

#### Atributos de Mensagem de Chat

| Atributo / Dado | Tipo conceitual | Obrigatório | Característica | Ex. aceitos | Ex. não aceitos | Comentários |
| :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| `id` | Identificador | Sim | Simples | `"msg-001"` | vazio, nulo | Identificador único da mensagem no banco. |
| `remetente` | Texto | Sim | Simples | `"Antonio Carlos Pereira"` | `""`, `"   "`, `null` | Nome ou identificador de quem enviou a mensagem; não pode ser igual ao destinatário. |
| `destinatario` | Texto | Não | Simples | `"Jose Carlos - Agricola Sao Jose"` | igual a `remetente` (ignorando maiúsculas) | Nome ou identificador de quem recebe a mensagem. |
| `conteudo` | Texto sanitizado | Sim | Simples | `"A"`, mensagem com até `1000` caracteres | `""`, `null`, texto com `1001` caracteres | Conteúdo entre 1 e 1000 caracteres; tags HTML (`<` e `>`) são convertidas para entidades seguras. |
| `horario` | Texto | Não | Simples | `"10:14"`, `"10:20"` | `—` | Horário formatado (`HH:mm`) para exibição rápida nas bolhas do chat. |
| `timestamp` | Data e Hora | Sim | Simples | `2026-11-19T10:14:00` | `—` | Data e hora completas usadas na ordenação cronológica; preenchido automaticamente se ausente. |

---

### 2.6. Contrato

Representa a minuta digital de locação gerada exclusivamente a partir de uma locação aceita, guardando cópia dos nomes das partes e da descrição da máquina no momento da emissão, o valor da caução, o código sequencial formal (`AM-ANO-00001`) e o registro de assinatura digital de ambas as partes para geração do PDF.

#### Atributos de Contrato

| Atributo / Dado | Tipo conceitual | Obrigatório | Característica | Ex. aceitos | Ex. não aceitos | Comentários |
| :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| `id` | Identificador | Sim | Simples | `"ctr-001"` | vazio, nulo | Identificador único do contrato. |
| `locacaoId` | Identificador (Ref. Locação) | Sim | Simples | `"LOC-2026-0901"` | `""`, locação já contratada | Único por contrato; somente locação com status `ACEITO` pode gerar contrato. |
| `numero` | Código formatado | Sim | Simples | `"AM-2026-00001"`, `"AM-2026-00002"` | `"2026-01"`, `"AM-26-1"` | Segue o padrão `^AM-\d{4}-\d{5}$`; único na base. |
| `locadorId` | Identificador (Ref. Usuário) | Sim | Simples | `"usr-locador-01"` | `""`, nulo | Referência ao locador autorizado a assinar. |
| `locatarioId` | Identificador (Ref. Usuário) | Sim | Simples | `"usr-locatario-01"` | `""`, nulo | Referência ao locatario autorizado a assinar. |
| `maquinaId` | Identificador (Ref. Máquina) | Sim | Simples | `"maq-001"` | `""`, nulo | Referência à máquina objeto do contrato. |
| `nomeLocador` | Texto | Sim | Simples | `"Jose Carlos - Agricola Sao Jose"` | `""`, nulo | Cópia do nome do locador na emissão (preserva o texto do documento assinado). |
| `nomeLocatario` | Texto | Sim | Simples | `"Antonio Carlos Pereira"` | `""`, nulo | Cópia do nome do locatário na emissão. |
| `descricaoMaquina` | Texto | Sim | Simples | `"Trator John Deere 6115J Cabinado - Paulínia / SP"` | `""`, nulo | Descrição da máquina congelada no contrato. |
| `dataInicio` | Data | Sim | Simples | `2026-11-25` | `null` | Data de início da vigência contratada. |
| `dataFim` | Data | Sim | Simples | `2026-11-29` | data anterior a `dataInicio` | Data de encerramento da locação (`dataFim >= dataInicio`). |
| `valorTotal` | Moeda (BRL) | Sim | Simples | `4530.00`, `7080.00` | `0.00`, `-100.00` | Valor total da locação (> 0), com 2 casas decimais. |
| `valorCaucao` | Moeda (BRL) | Sim | Simples | `1000.00`, `0.00` | `-50.00` | Valor da caução de segurança (>= 0; padrão da plataforma: R$ 1.000,00). |
| `assinadoPeloLocador` | Booleano | Sim | Simples | `true`, `false` | `null` | Indica se o locador já assinou (cada parte assina no máximo uma vez). |
| `assinadoPeloLocatario` | Booleano | Sim | Simples | `true`, `false` | `null` | Indica se o locatário já assinou; contrato fica vigente apenas quando ambos são `true`. |
| `dataAssinaturaLocador` | Data e Hora | Não | Simples | `2026-11-18T11:00:00` | `—` | Preenchida no momento em que o locador assina. |
| `dataAssinaturaLocatario` | Data e Hora | Não | Simples | `2026-10-06T11:30:00` | `—` | Preenchida no momento em que o locatário assina. |
| `dataEmissao` | Data e Hora | Sim | Simples | `2026-11-18T10:00:00` | `—` | Carimbo temporal de emissão da minuta. |

---

### 2.7. Vistoria Técnica (Checklist)

Representa a conferência técnica assistida realizada pelas partes na entrega (`RETIRADA`) ou devolução (`DEVOLUCAO`) do equipamento vinculado a uma locação aceita. Contém os 8 itens padronizados de inspeção, a leitura do horímetro do motor e o status final da vistoria.

#### Atributos de Vistoria Técnica

| Atributo / Dado | Tipo conceitual | Obrigatório | Característica | Ex. aceitos | Ex. não aceitos | Comentários |
| :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| `id` | Identificador | Sim | Simples | `"vst-001"` | vazio, nulo | Identificador único da vistoria. |
| `locacaoId` | Identificador (Ref. Locação) | Sim | Simples | `"LOC-2026-0901"` | `""`, nulo | Referência à locação aceita vistoriada. |
| `maquinaId` | Identificador (Ref. Máquina) | Sim | Simples | `"maq-001"` | `""`, nulo | Referência à máquina inspecionada (herdada da locação). |
| `tipo` | Texto enumerado | Sim | Simples | `"RETIRADA"`, `"DEVOLUCAO"` | `"MANUTENCAO"` | Momento da vistoria; o par `(locacaoId, tipo)` é único no banco. |
| `itens` | Lista de Item de Checklist | Sim | Multivalorado e Composto (MC) | Lista com os 8 itens padrão de `VistoriaChecklistService` | lista vazia, item sem resposta na conclusão | Itens inspecionados na vistoria; nunca pode ser vazia. |
| `horimetro` | Número decimal | Não* | Simples | `0.0`, `1200.5` | `-1.0`, `NaN` | Opcional na abertura (`PENDENTE`); obrigatório e `>= 0` para concluir a vistoria. |
| `responsavelId` | Identificador (Ref. Usuário) | Sim | Simples | `"usr-locador-01"` | usuário fora da locação | Deve ser o `locadorId` ou `locatarioId` da locação associada. |
| `dataVistoria` | Data e Hora | Sim | Simples | `2026-11-19T08:30:00` | `—` | Data e hora da abertura da vistoria. |
| `status` | Texto enumerado | Sim | Simples | `"PENDENTE"`, `"CONCLUIDA"`, `"COM_PENDENCIAS"` | `"FINALIZADA"` | Nasce `"PENDENTE"`; vira `"COM_PENDENCIAS"` se houver item não conforme, senão `"CONCLUIDA"`. |

---

## 3. Tipos especiais, compostos ou específicos

Neste projeto existem atributos compostos, multivalorados e listas enumeradas padronizadas. Como convenção documental, utiliza-se a seguinte notação:

| Padrões de atributos especiais |
| :--- |
| **(C)** = Composto (estrutura formada por partes internas que juntas representam o atributo) |
| **(M)** = Multivalorado (vários valores associados à mesma entidade) |
| **(MC)** = Multivalorado e Composto (lista/array de estruturas compostas) |

### 3.1. Localização Geoespacial (`GeoJsonPoint`) — Composto (C)

Representa a coordenada geográfica da máquina no formato padrão GeoJSON, exigido pelo MongoDB para consultas geoespaciais em esfera (`2dsphere`).

| Atributo / Dado | Tipo conceitual | Obrigatório | Comentários |
| :--- | :--- | :---: | :--- |
| `type` | Texto enumerado | Sim | Valor fixo `"Point"`. |
| `coordinates` | Array de 2 números decimais | Sim | Par ordenado `[longitude, latitude]`, com longitude em `[-180, 180]` e latitude em `[-90, 90]`. |

### 3.2. Item de Checklist (`ItemChecklist`) — Multivalorado e Composto (MC)

Representa cada item individual avaliado dentro da lista `itens` de uma `VistoriaChecklist`. Na abertura da vistoria são gerados 8 itens padrão (`Pneus, rodas ou esteiras sem avarias`, `Nivel de oleo do motor`, `Nivel de fluidos (arrefecimento e hidraulico)`, `Luzes e sinalizacao funcionando`, `Freios funcionando`, `Cabine, vidros e bancos em bom estado`, `Implementos acoplados conferidos` e `Avarias visiveis (riscos, amassados, vazamentos)`).

| Atributo / Dado | Tipo conceitual | Obrigatório | Comentários |
| :--- | :--- | :---: | :--- |
| `descricao` | Texto | Sim | Descrição do item inspecionado (não pode ser vazia). |
| `conforme` | Booleano (anulável) | Não* | `null` enquanto a vistoria está `PENDENTE`; `true` (conforme) ou `false` (não conforme) na conclusão. |
| `observacao` | Texto | Não* | Opcional se `conforme = true`; **obrigatória** e não vazia se `conforme = false`. |

### 3.3. Enumerações de domínio

| Enumeração / Domínio | Valores aceitos | Uso no sistema |
| :--- | :--- | :--- |
| `PerfilUsuario` | `locatario`, `locador`, `ambos` | Campo `Usuario.perfil` no cadastro e controle de atuação. |
| `TipoEquipamento` | `trator` (`tratores`), `colheitadeira` (`colheitadeiras`), `pulverizador` (`pulverizadores`), `plantadeira` (`plantadeiras`), `implemento` (`implementos`) | Mapeia o código selecionado no formulário para a chave de catálogo gravada em `Maquina.tipoEquipamento`. |
| `MunicipioAtendido` | `paulinia` (`-47.1100, -22.7100`), `campinas` (`-47.0608, -22.9056`), `cosmopolis` (`-47.1960, -22.6458`), `sumare` (`-47.2667, -22.8219`), `holambra` (`-47.0547, -22.6333`), `jaguariuna` (`-46.9858, -22.7055`), `artur-nogueira` (`-47.1725, -22.5731`) | Fornece nome de exibição e coordenadas geográficas para `Maquina.localizacao`. |
| `StatusLocacao` | `PENDENTE`, `ACEITO`, `RECUSADO`, `CANCELADO`, `FINALIZADO` | Ciclo de vida da entidade `Locacao`. |
| `TipoVistoria` | `RETIRADA`, `DEVOLUCAO` | Identifica o momento da `VistoriaChecklist`. |
| `StatusVistoria` | `PENDENTE`, `CONCLUIDA`, `COM_PENDENCIAS` | Resultado da conclusão da `VistoriaChecklist`. |

---

## 4. Desdobramento físico (Banco Orientado a Documentos — MongoDB)

O sistema utiliza o **MongoDB** (banco `alugamaquina`), mapeado via **Spring Data MongoDB**. Abaixo está a correspondência entre as entidades conceituais e as 7 coleções do banco.

### 4.1. Mapeamento Entidade Conceitual → Coleção

| Entidade conceitual | Coleção | Estratégia de modelagem |
| :--- | :--- | :--- |
| Usuário | `usuarios` | Coleção independente; referenciada por `_id` nas demais coleções. |
| Máquina | `maquinas` | Coleção independente; embute o objeto composto `localizacao` (`GeoJsonPoint`). |
| Locação | `locacoes` | Coleção independente; referencia `locadorId`, `locatarioId` e `maquinaId` por ID. |
| Avaliação | `avaliacoes` | Coleção independente; referencia `locacaoId`, `locadorId`, `locatarioId` e `maquinaId` por ID. |
| Mensagem de Chat | `chat_mensagens` | Coleção independente; cada mensagem é um documento para suportar crescimento contínuo e ordenação por `timestamp`. |
| Contrato | `contratos` | Coleção independente; referencia os IDs da locação/partes/máquina e embute cópia resumida dos nomes e da descrição no momento da emissão. |
| Vistoria Técnica | `vistorias_checklist` | Coleção independente; embute o array composto `itens` (8 itens do checklist) dentro do documento da vistoria. |

### 4.2. Exemplos de documentos JSON por coleção

#### Coleção `usuarios`
```json
{
  "_id": "usr-locador-01",
  "nome": "Jose Carlos - Agricola Sao Jose",
  "documento": "11122233344",
  "telefone": "(19) 99123-4567",
  "email": "jose.carlos@alugamaquina.com.br",
  "municipio": "paulinia",
  "perfil": "locador",
  "senhaHash": "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
  "reputacaoMedia": 5.0,
  "totalAvaliacoes": 0
}
```

#### Coleção `maquinas`
```json
{
  "_id": "maq-001",
  "tituloAnuncio": "Trator John Deere 6115J Cabinado",
  "tipoEquipamento": "tratores",
  "municipio": "Paulínia / SP",
  "valorDiaria": "850.00",
  "localizacao": {
    "type": "Point",
    "coordinates": [-47.1100, -22.7100]
  }
}
```

#### Coleção `locacoes`
```json
{
  "_id": "LOC-2026-0901",
  "locadorId": "usr-locador-01",
  "locatarioId": "usr-locatario-01",
  "maquinaId": "maq-001",
  "dataInicio": "2026-11-25T03:00:00Z",
  "dataFim": "2026-11-29T03:00:00Z",
  "valorDiaria": "850.00",
  "valorFrete": "280.00",
  "valorTotal": "4530.00",
  "status": "ACEITO",
  "motivoRecusa": null,
  "dataSolicitacao": "2026-11-18T09:15:00Z"
}
```

#### Coleção `avaliacoes`
```json
{
  "_id": "aval-001",
  "locacaoId": "LOC-2026-0842",
  "locadorId": "usr-locador-01",
  "locatarioId": "usr-locatario-01",
  "maquinaId": "maq-001",
  "notaDesempenho": 5,
  "notaConservacao": 4,
  "notaFidelidade": 5,
  "notaPontualidade": 4,
  "notaSuporte": 5,
  "mediaFinal": 4.6,
  "comentario": "Equipamento em ótimo estado, atendeu perfeitamente ao plantio.",
  "recomenda": true,
  "dataAvaliacao": "2026-10-19T15:00:00Z"
}
```

#### Coleção `chat_mensagens`
```json
{
  "_id": "msg-001",
  "remetente": "Jose Carlos - Agricola Sao Jose",
  "destinatario": "Antonio Carlos Pereira",
  "conteudo": "Ola! Vi que voce tem interesse no Trator John Deere 6115J para a proxima semana. A maquina esta disponivel sim!",
  "horario": "10:14",
  "timestamp": "2026-11-19T10:14:00Z"
}
```

#### Coleção `contratos`
```json
{
  "_id": "ctr-002",
  "locacaoId": "LOC-2026-0901",
  "numero": "AM-2026-00002",
  "locadorId": "usr-locador-01",
  "locatarioId": "usr-locatario-01",
  "maquinaId": "maq-001",
  "nomeLocador": "Jose Carlos - Agricola Sao Jose",
  "nomeLocatario": "Antonio Carlos Pereira",
  "descricaoMaquina": "Trator John Deere 6115J Cabinado - Paulínia / SP",
  "dataInicio": "2026-11-25T03:00:00Z",
  "dataFim": "2026-11-29T03:00:00Z",
  "valorTotal": "4530.00",
  "valorCaucao": "1000.00",
  "assinadoPeloLocador": true,
  "assinadoPeloLocatario": false,
  "dataAssinaturaLocador": "2026-11-18T11:00:00Z",
  "dataAssinaturaLocatario": null,
  "dataEmissao": "2026-11-18T10:00:00Z"
}
```

#### Coleção `vistorias_checklist`
```json
{
  "_id": "vst-001",
  "locacaoId": "LOC-2026-0901",
  "maquinaId": "maq-001",
  "tipo": "RETIRADA",
  "itens": [
    {
      "descricao": "Pneus, rodas ou esteiras sem avarias",
      "conforme": true,
      "observacao": null
    },
    {
      "descricao": "Nivel de oleo do motor",
      "conforme": true,
      "observacao": null
    }
  ],
  "horimetro": 1200.0,
  "responsavelId": "usr-locador-01",
  "dataVistoria": "2026-11-19T08:30:00Z",
  "status": "CONCLUIDA"
}
```

### 4.3. Justificativa das decisões arquiteturais

- **Embutimento de `localizacao` em `maquinas`:** o operador `$geoNear` do MongoDB exige que o campo GeoJSON com índice `2dsphere` resida diretamente na coleção consultada, garantindo ordenação imediata por distância em quilômetros.
- **Embutimento de `itens` em `vistorias_checklist`:** cada vistoria possui exatamente 8 itens de tamanho pequeno e ciclo de vida inseparável da vistoria; embuti-los permite carregar e concluir o checklist inteiro em uma única leitura e escrita atômica.
- **Duplicação controlada (snapshot) em `contratos`:** os campos `nomeLocador`, `nomeLocatario` e `descricaoMaquina` são copiados no momento da emissão para preservar a integridade histórica e jurídica do contrato e do PDF mesmo que o usuário ou anúncio mude depois.
- **Separação por referência de ID entre as coleções principais:** `usuarios`, `maquinas`, `locacoes`, `avaliacoes`, `chat_mensagens`, `contratos` e `vistorias_checklist` possuem consultas independentes, diferentes padrões de crescimento (ex.: crescimento contínuo de mensagens no chat) e regras de unicidade próprias, evitando documentos inchados ou acoplamento excessivo.

---

## 5. Consultas, índices e observações de arquitetura

### 5.1. Consultas esperadas e índices

| Consulta esperada | Coleção | Campos envolvidos | Índice utilizado | Observações |
| :--- | :--- | :--- | :--- | :--- |
| Autenticação por e-mail / verificação de duplicidade | `usuarios` | `email` | `email` (único) | Garante unicidade e login rápido por e-mail. |
| Autenticação por CPF/CNPJ / verificação de duplicidade | `usuarios` | `documento` | `documento` (único) | Garante unicidade de CPF/CNPJ sem máscara. |
| Busca regional de máquinas por raio em km (`geoNear`) | `maquinas` | `localizacao` | `localizacao` (`2dsphere`) | Obrigatório para consulta geoespacial esférica. |
| Filtro auxiliar por categoria ou município | `maquinas` | `tipoEquipamento`, `municipio` | `idx_maquinas_tipo`, `idx_maquinas_municipio` | Apoia consultas segmentadas no catálogo. |
| Verificação de conflito de agenda em reservas | `locacoes` | `maquinaId`, `status`, `dataInicio`, `dataFim` | `idx_locacoes_conflito_agenda` | Índice composto para detectar sobreposição de datas em reservas `ACEITO`. |
| Listagem de locações por locador ou locatário | `locacoes` | `locadorId`, `locatarioId` | `idx_locacoes_locador`, `idx_locacoes_locatario` | Apoia histórico e acompanhamento de pedidos. |
| Impedir avaliação duplicada da mesma locação | `avaliacoes` | `locacaoId` | `locacaoId` (único) | Garante no máximo uma avaliação por locação. |
| Recálculo da reputação média do locador | `avaliacoes` | `locadorId` | `locadorId` | Usado em `findByLocadorId` após cada nova avaliação. |
| Consulta de avaliações por máquina | `avaliacoes` | `maquinaId` | `maquinaId` | Suporta listagem por equipamento. |
| Histórico de conversa entre duas partes | `chat_mensagens` | `remetente`, `destinatario`, `timestamp` | `idx_chat_conversa_timestamp` | Filtro direto por conversa em ordem cronológica. |
| Histórico geral do chat ordenado por data | `chat_mensagens` | `timestamp` | `timestamp` | Garante ordenação ascendente no carregamento inicial. |
| Busca e unicidade de contrato por locação | `contratos` | `locacaoId` | `locacaoId` (único) | Garante no máximo um contrato por locação. |
| Unicidade do número formal do contrato | `contratos` | `numero` | `numero` (único) | Previne colisão no código sequencial `AM-ANO-00001`. |
| Listagem de contratos pendentes e histórico do usuário | `contratos` | `locadorId`, `locatarioId` | `idx_contratos_locador`, `idx_contratos_locatario` | Usado na tela *Meus Contratos* (`findByLocadorIdOrLocatarioId`). |
| Unicidade e listagem de vistorias da locação | `vistorias_checklist` | `locacaoId`, `tipo` | `locacao_tipo_unico` (único), `idx_vistorias_locacao` | Garante no máximo 1 vistoria de `RETIRADA` e 1 de `DEVOLUCAO` por locação. |

### 5.2. Regras de integridade e validação

- Todo usuário nasce com `reputacaoMedia = 5.0` e `totalAvaliacoes = 0`, e a senha nunca é armazenada em texto puro (`senhaHash` SHA-256).
- As coordenadas de `maquinas.localizacao` seguem obrigatoriamente a ordem `[longitude, latitude]` do GeoJSON.
- Locações nascem como `PENDENTE` e só podem ser aceitas se não houver reserva `ACEITO` cruzando o período (`dataInicioExistente <= dataFimNovo` e `dataFimExistente >= dataInicioNovo`).
- Contratos e vistorias só podem ser abertos para locações no status `ACEITO`, sendo que apenas o locador ou o locatário vinculados à locação podem assinar o contrato ou abrir a vistoria.
- Nenhuma vistoria pode ser alterada após sair do status `PENDENTE`; itens marcados como não conformes exigem observação escrita antes da conclusão.
- Todas as entidades de domínio seguem o padrão da disciplina com métodos canônicos (`equals`, `hashCode`, `compareTo`, `toString`, construtor de cópia e `clone`), com cópia profunda em listas mutáveis como `VistoriaChecklist.itens`.
