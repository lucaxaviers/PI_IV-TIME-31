/**
 * AlugaMaquina - Inicializacao Oficial do MongoDB (PI4 - Time 31)
 *
 * Script idempotente: pode ser executado automaticamente pelo Docker
 * (/docker-entrypoint-initdb.d/01-init-mongo.js) ou manualmente via mongosh
 * em instancias locais ou no MongoDB Atlas:
 *
 *   mongosh "mongodb://localhost:27017/alugamaquina" mongodb/init-mongo.js
 *
 * Cria as 7 collections do projeto com validacao de schema ($jsonSchema),
 * todos os indices exigidos pelos repositories/services (incluindo 2dsphere)
 * e carga inicial de dados consistentes com as telas do MVP.
 */

const targetDbName = db.getName() && db.getName() !== "test" ? db.getName() : "alugamaquina";
const database = db.getSiblingDB(targetDbName);

function ensureCollection(name, validator) {
  const exists = database.getCollectionNames().includes(name);
  if (!exists) {
    database.createCollection(name, {
      validator,
      validationLevel: "moderate",
      validationAction: "error"
    });
    print(`[init-mongo] Collection criada: ${name}`);
  } else {
    try {
      database.runCommand({
        collMod: name,
        validator,
        validationLevel: "moderate",
        validationAction: "error"
      });
      print(`[init-mongo] Validator atualizado: ${name}`);
    } catch (err) {
      print(`[init-mongo] Aviso: sem permissao ou nao foi possivel aplicar collMod em ${name} (${err.codeName || err.message}); mantendo colecao existente.`);
    }
  }
}

function ensureIndex(collection, keys, options) {
  try {
    collection.createIndex(keys, options || {});
  } catch (err) {
    const code = err && err.codeName ? err.codeName : "";
    if (code === "IndexOptionsConflict" || code === "IndexKeySpecsConflict" || code === "IndexAlreadyExists") {
      print(`[init-mongo] Aviso: indice equivalente ja existe em ${collection.getName()} (${err.message})`);
    } else {
      throw err;
    }
  }
}

// -----------------------------------------------------------------------------
// 1. Collection: usuarios (Usuario.java)
// -----------------------------------------------------------------------------
ensureCollection("usuarios", {
  $jsonSchema: {
    bsonType: "object",
    required: ["nome", "documento", "email", "perfil", "senhaHash"],
    properties: {
      nome: { bsonType: "string", minLength: 3 },
      documento: { bsonType: "string", minLength: 11 },
      telefone: { bsonType: ["string", "null"] },
      email: { bsonType: "string", pattern: "^.+@.+$" },
      municipio: { bsonType: ["string", "null"] },
      perfil: { enum: ["locatario", "locador", "ambos"] },
      senhaHash: { bsonType: "string", minLength: 64, maxLength: 64 },
      reputacaoMedia: { bsonType: ["double", "int", "decimal", "null"], minimum: 0, maximum: 5 },
      totalAvaliacoes: { bsonType: ["int", "long", "null"], minimum: 0 }
    }
  }
});

ensureIndex(database.usuarios, { email: 1 }, { name: "email", unique: true });
ensureIndex(database.usuarios, { documento: 1 }, { name: "documento", unique: true });

// -----------------------------------------------------------------------------
// 2. Collection: maquinas (Maquina.java + MaquinaBuscaService.java)
// -----------------------------------------------------------------------------
ensureCollection("maquinas", {
  $jsonSchema: {
    bsonType: "object",
    required: ["tituloAnuncio", "tipoEquipamento", "municipio", "valorDiaria", "localizacao"],
    properties: {
      tituloAnuncio: { bsonType: "string", minLength: 3 },
      tipoEquipamento: { bsonType: "string", minLength: 2 },
      municipio: { bsonType: "string", minLength: 2 },
      valorDiaria: { bsonType: ["decimal", "double", "int", "long", "string"] },
      localizacao: {
        bsonType: "object",
        required: ["type", "coordinates"],
        properties: {
          type: { enum: ["Point"] },
          coordinates: {
            bsonType: "array",
            minItems: 2,
            maxItems: 2,
            items: { bsonType: ["double", "int", "decimal"] }
          }
        }
      }
    }
  }
});

ensureIndex(database.maquinas, { localizacao: "2dsphere" }, { name: "localizacao" });
ensureIndex(database.maquinas, { tipoEquipamento: 1 }, { name: "idx_maquinas_tipo" });
ensureIndex(database.maquinas, { municipio: 1 }, { name: "idx_maquinas_municipio" });

// -----------------------------------------------------------------------------
// 3. Collection: locacoes (Locacao.java + LocacaoRepository.java)
// -----------------------------------------------------------------------------
ensureCollection("locacoes", {
  $jsonSchema: {
    bsonType: "object",
    required: [
      "locadorId",
      "locatarioId",
      "maquinaId",
      "dataInicio",
      "dataFim",
      "valorDiaria",
      "valorFrete",
      "valorTotal",
      "status"
    ],
    properties: {
      locadorId: { bsonType: "string" },
      locatarioId: { bsonType: "string" },
      maquinaId: { bsonType: "string" },
      dataInicio: { bsonType: ["date", "string"] },
      dataFim: { bsonType: ["date", "string"] },
      valorDiaria: { bsonType: ["decimal", "double", "int", "long", "string"] },
      valorFrete: { bsonType: ["decimal", "double", "int", "long", "string"] },
      valorTotal: { bsonType: ["decimal", "double", "int", "long", "string"] },
      status: { enum: ["PENDENTE", "ACEITO", "RECUSADO", "CANCELADO", "FINALIZADO"] },
      motivoRecusa: { bsonType: ["string", "null"] },
      dataSolicitacao: { bsonType: ["date", "string", "null"] }
    }
  }
});

ensureIndex(
  database.locacoes,
  { maquinaId: 1, status: 1, dataInicio: 1, dataFim: 1 },
  { name: "idx_locacoes_conflito_agenda" }
);
ensureIndex(database.locacoes, { locadorId: 1 }, { name: "idx_locacoes_locador" });
ensureIndex(database.locacoes, { locatarioId: 1 }, { name: "idx_locacoes_locatario" });

// -----------------------------------------------------------------------------
// 4. Collection: avaliacoes (Avaliacao.java + AvaliacaoRepository.java)
// -----------------------------------------------------------------------------
ensureCollection("avaliacoes", {
  $jsonSchema: {
    bsonType: "object",
    required: [
      "locacaoId",
      "locadorId",
      "notaDesempenho",
      "notaConservacao",
      "notaFidelidade",
      "notaPontualidade",
      "notaSuporte",
      "mediaFinal"
    ],
    properties: {
      locacaoId: { bsonType: "string" },
      locadorId: { bsonType: "string" },
      locatarioId: { bsonType: ["string", "null"] },
      maquinaId: { bsonType: ["string", "null"] },
      notaDesempenho: { bsonType: "int", minimum: 1, maximum: 5 },
      notaConservacao: { bsonType: "int", minimum: 1, maximum: 5 },
      notaFidelidade: { bsonType: "int", minimum: 1, maximum: 5 },
      notaPontualidade: { bsonType: "int", minimum: 1, maximum: 5 },
      notaSuporte: { bsonType: "int", minimum: 1, maximum: 5 },
      mediaFinal: { bsonType: ["double", "int", "decimal"], minimum: 1, maximum: 5 },
      comentario: { bsonType: ["string", "null"] },
      recomenda: { bsonType: ["bool", "null"] },
      dataAvaliacao: { bsonType: ["date", "string", "null"] }
    }
  }
});

ensureIndex(database.avaliacoes, { locacaoId: 1 }, { name: "locacaoId", unique: true });
ensureIndex(database.avaliacoes, { locadorId: 1 }, { name: "locadorId" });
ensureIndex(database.avaliacoes, { maquinaId: 1 }, { name: "maquinaId" });

// -----------------------------------------------------------------------------
// 5. Collection: chat_mensagens (ChatMessage.java + ChatMessageRepository.java)
// -----------------------------------------------------------------------------
ensureCollection("chat_mensagens", {
  $jsonSchema: {
    bsonType: "object",
    required: ["remetente", "conteudo"],
    properties: {
      remetente: { bsonType: "string", minLength: 1 },
      destinatario: { bsonType: ["string", "null"] },
      conteudo: { bsonType: "string", minLength: 1, maxLength: 1000 },
      horario: { bsonType: ["string", "null"] },
      timestamp: { bsonType: ["date", "string", "null"] }
    }
  }
});

ensureIndex(
  database.chat_mensagens,
  { remetente: 1, destinatario: 1, timestamp: 1 },
  { name: "idx_chat_conversa_timestamp" }
);
ensureIndex(database.chat_mensagens, { timestamp: 1 }, { name: "timestamp" });

// -----------------------------------------------------------------------------
// 6. Collection: contratos (Contrato.java + ContratoRepository.java)
// -----------------------------------------------------------------------------
ensureCollection("contratos", {
  $jsonSchema: {
    bsonType: "object",
    required: [
      "locacaoId",
      "numero",
      "locadorId",
      "locatarioId",
      "maquinaId",
      "nomeLocador",
      "nomeLocatario",
      "descricaoMaquina",
      "dataInicio",
      "dataFim",
      "valorTotal",
      "valorCaucao"
    ],
    properties: {
      locacaoId: { bsonType: "string" },
      numero: { bsonType: "string", pattern: "^AM-\\d{4}-\\d{5}$" },
      locadorId: { bsonType: "string" },
      locatarioId: { bsonType: "string" },
      maquinaId: { bsonType: "string" },
      nomeLocador: { bsonType: "string", minLength: 1 },
      nomeLocatario: { bsonType: "string", minLength: 1 },
      descricaoMaquina: { bsonType: "string", minLength: 1 },
      dataInicio: { bsonType: ["date", "string"] },
      dataFim: { bsonType: ["date", "string"] },
      valorTotal: { bsonType: ["decimal", "double", "int", "long", "string"] },
      valorCaucao: { bsonType: ["decimal", "double", "int", "long", "string"] },
      assinadoPeloLocador: { bsonType: "bool" },
      assinadoPeloLocatario: { bsonType: "bool" },
      dataAssinaturaLocador: { bsonType: ["date", "string", "null"] },
      dataAssinaturaLocatario: { bsonType: ["date", "string", "null"] },
      dataEmissao: { bsonType: ["date", "string", "null"] }
    }
  }
});

ensureIndex(database.contratos, { locacaoId: 1 }, { name: "locacaoId", unique: true });
ensureIndex(database.contratos, { numero: 1 }, { name: "numero", unique: true });
ensureIndex(database.contratos, { locadorId: 1 }, { name: "idx_contratos_locador" });
ensureIndex(database.contratos, { locatarioId: 1 }, { name: "idx_contratos_locatario" });

// -----------------------------------------------------------------------------
// 7. Collection: vistorias_checklist (VistoriaChecklist.java + Repository)
// -----------------------------------------------------------------------------
ensureCollection("vistorias_checklist", {
  $jsonSchema: {
    bsonType: "object",
    required: ["locacaoId", "maquinaId", "tipo", "itens", "responsavelId", "status"],
    properties: {
      locacaoId: { bsonType: "string" },
      maquinaId: { bsonType: "string" },
      tipo: { enum: ["RETIRADA", "DEVOLUCAO"] },
      itens: {
        bsonType: "array",
        minItems: 1,
        items: {
          bsonType: "object",
          required: ["descricao"],
          properties: {
            descricao: { bsonType: "string", minLength: 1 },
            conforme: { bsonType: ["bool", "null"] },
            observacao: { bsonType: ["string", "null"] }
          }
        }
      },
      horimetro: { bsonType: ["double", "int", "decimal", "long", "null"], minimum: 0 },
      responsavelId: { bsonType: "string" },
      dataVistoria: { bsonType: ["date", "string", "null"] },
      status: { enum: ["PENDENTE", "CONCLUIDA", "COM_PENDENCIAS"] }
    }
  }
});

ensureIndex(
  database.vistorias_checklist,
  { locacaoId: 1, tipo: 1 },
  { name: "locacao_tipo_unico", unique: true }
);
ensureIndex(database.vistorias_checklist, { locacaoId: 1 }, { name: "idx_vistorias_locacao" });

// -----------------------------------------------------------------------------
// Dados iniciais (Seed idempotente alinhado com o MVP do AlugaMaquina)
// Senha padrao dos usuarios de demonstracao: 123456
// SHA-256("123456") = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
// -----------------------------------------------------------------------------
const SENHA_DEMO_HASH = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

const usuariosSeed = [
  {
    _id: "usr-locador-01",
    nome: "Jose Carlos - Agricola Sao Jose",
    documento: "11122233344",
    telefone: "(19) 99123-4567",
    email: "jose.carlos@alugamaquina.com.br",
    municipio: "paulinia",
    perfil: "locador",
    senhaHash: SENHA_DEMO_HASH,
    reputacaoMedia: 4.8,
    totalAvaliacoes: Int32(2),
    _class: "com.alugamaquina.backend.model.Usuario"
  },
  {
    _id: "usr-locatario-01",
    nome: "Antonio Carlos Pereira",
    documento: "55566677788",
    telefone: "(19) 99234-5678",
    email: "antonio.pereira@alugamaquina.com.br",
    municipio: "paulinia",
    perfil: "locatario",
    senhaHash: SENHA_DEMO_HASH,
    reputacaoMedia: 5.0,
    totalAvaliacoes: 0,
    _class: "com.alugamaquina.backend.model.Usuario"
  },
  {
    _id: "usr-ambos-01",
    nome: "Fazenda Santa Maria",
    documento: "12345678000199",
    telefone: "(19) 99345-6789",
    email: "contato@fazendasantamaria.com.br",
    municipio: "cosmopolis",
    perfil: "ambos",
    senhaHash: SENHA_DEMO_HASH,
    reputacaoMedia: 4.6,
    totalAvaliacoes: Int32(1),
    _class: "com.alugamaquina.backend.model.Usuario"
  }
];

usuariosSeed.forEach((doc) => {
  database.usuarios.updateOne({ _id: doc._id }, { $setOnInsert: doc }, { upsert: true });
});

const maquinasSeed = [
  {
    _id: "maq-001",
    tituloAnuncio: "Trator John Deere 6115J Cabinado",
    tipoEquipamento: "tratores",
    municipio: "Paulínia / SP",
    valorDiaria: NumberDecimal("850.00"),
    localizacao: { type: "Point", coordinates: [-47.1100, -22.7100] },
    _class: "com.alugamaquina.backend.model.Maquina"
  },
  {
    _id: "maq-002",
    tituloAnuncio: "Colheitadeira Case IH 7130",
    tipoEquipamento: "colheitadeiras",
    municipio: "Cosmópolis / SP",
    valorDiaria: NumberDecimal("2400.00"),
    localizacao: { type: "Point", coordinates: [-47.1960, -22.6458] },
    _class: "com.alugamaquina.backend.model.Maquina"
  },
  {
    _id: "maq-003",
    tituloAnuncio: "Pulverizador Jacto Uniport 3030",
    tipoEquipamento: "pulverizadores",
    municipio: "Jaguariúna / SP",
    valorDiaria: NumberDecimal("1300.00"),
    localizacao: { type: "Point", coordinates: [-46.9858, -22.7055] },
    _class: "com.alugamaquina.backend.model.Maquina"
  },
  {
    _id: "maq-004",
    tituloAnuncio: "Trator New Holland TL75E 4x4",
    tipoEquipamento: "tratores",
    municipio: "Campinas / SP",
    valorDiaria: NumberDecimal("600.00"),
    localizacao: { type: "Point", coordinates: [-47.0608, -22.9056] },
    _class: "com.alugamaquina.backend.model.Maquina"
  },
  {
    _id: "maq-005",
    tituloAnuncio: "Grade Aradora Baldan 14 discos",
    tipoEquipamento: "implementos",
    municipio: "Holambra / SP",
    valorDiaria: NumberDecimal("280.00"),
    localizacao: { type: "Point", coordinates: [-47.0547, -22.6333] },
    _class: "com.alugamaquina.backend.model.Maquina"
  },
  {
    _id: "maq-006",
    tituloAnuncio: "Colheitadeira New Holland TC5090",
    tipoEquipamento: "colheitadeiras",
    municipio: "Artur Nogueira / SP",
    valorDiaria: NumberDecimal("1900.00"),
    localizacao: { type: "Point", coordinates: [-47.1725, -22.5731] },
    _class: "com.alugamaquina.backend.model.Maquina"
  }
];

maquinasSeed.forEach((doc) => {
  database.maquinas.updateOne({ _id: doc._id }, { $setOnInsert: doc }, { upsert: true });
});

const locacoesSeed = [
  {
    _id: "LOC-2026-0710",
    locadorId: "usr-locador-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-002",
    dataInicio: ISODate("2026-09-10T03:00:00Z"),
    dataFim: ISODate("2026-09-12T03:00:00Z"),
    valorDiaria: NumberDecimal("2400.00"),
    valorFrete: NumberDecimal("384.00"),
    valorTotal: NumberDecimal("7584.00"),
    status: "FINALIZADO",
    motivoRecusa: null,
    dataSolicitacao: ISODate("2026-09-05T10:00:00Z"),
    _class: "com.alugamaquina.backend.model.Locacao"
  },
  {
    _id: "LOC-2026-0755",
    locadorId: "usr-ambos-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-004",
    dataInicio: ISODate("2026-09-20T03:00:00Z"),
    dataFim: ISODate("2026-09-23T03:00:00Z"),
    valorDiaria: NumberDecimal("600.00"),
    valorFrete: NumberDecimal("215.00"),
    valorTotal: NumberDecimal("2615.00"),
    status: "FINALIZADO",
    motivoRecusa: null,
    dataSolicitacao: ISODate("2026-09-15T14:20:00Z"),
    _class: "com.alugamaquina.backend.model.Locacao"
  },
  {
    _id: "LOC-2026-0790",
    locadorId: "usr-locador-01",
    locatarioId: "usr-ambos-01",
    maquinaId: "maq-003",
    dataInicio: ISODate("2026-09-29T03:00:00Z"),
    dataFim: ISODate("2026-10-01T03:00:00Z"),
    valorDiaria: NumberDecimal("1300.00"),
    valorFrete: NumberDecimal("358.00"),
    valorTotal: NumberDecimal("4258.00"),
    status: "FINALIZADO",
    motivoRecusa: null,
    dataSolicitacao: ISODate("2026-09-25T09:45:00Z"),
    _class: "com.alugamaquina.backend.model.Locacao"
  },
  {
    _id: "LOC-2026-0842",
    locadorId: "usr-locador-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-001",
    dataInicio: ISODate("2026-10-10T03:00:00Z"),
    dataFim: ISODate("2026-10-17T03:00:00Z"),
    valorDiaria: NumberDecimal("850.00"),
    valorFrete: NumberDecimal("280.00"),
    valorTotal: NumberDecimal("7080.00"),
    status: "FINALIZADO",
    motivoRecusa: null,
    dataSolicitacao: ISODate("2026-10-05T14:30:00Z"),
    _class: "com.alugamaquina.backend.model.Locacao"
  },
  {
    _id: "LOC-2026-0901",
    locadorId: "usr-locador-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-001",
    dataInicio: ISODate("2026-11-25T03:00:00Z"),
    dataFim: ISODate("2026-11-29T03:00:00Z"),
    valorDiaria: NumberDecimal("850.00"),
    valorFrete: NumberDecimal("280.00"),
    valorTotal: NumberDecimal("4530.00"),
    status: "ACEITO",
    motivoRecusa: null,
    dataSolicitacao: ISODate("2026-11-18T09:15:00Z"),
    _class: "com.alugamaquina.backend.model.Locacao"
  },
  {
    _id: "LOC-2026-0910",
    locadorId: "usr-ambos-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-002",
    dataInicio: ISODate("2026-12-05T03:00:00Z"),
    dataFim: ISODate("2026-12-08T03:00:00Z"),
    valorDiaria: NumberDecimal("2400.00"),
    valorFrete: NumberDecimal("384.00"),
    valorTotal: NumberDecimal("9984.00"),
    status: "PENDENTE",
    motivoRecusa: null,
    dataSolicitacao: ISODate("2026-11-19T11:00:00Z"),
    _class: "com.alugamaquina.backend.model.Locacao"
  }
];

locacoesSeed.forEach((doc) => {
  database.locacoes.updateOne({ _id: doc._id }, { $setOnInsert: doc }, { upsert: true });
});

const avaliacoesSeed = [
  {
    _id: "aval-001",
    locacaoId: "LOC-2026-0710",
    locadorId: "usr-locador-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-002",
    notaDesempenho: Int32(5),
    notaConservacao: Int32(5),
    notaFidelidade: Int32(5),
    notaPontualidade: Int32(4),
    notaSuporte: Int32(5),
    mediaFinal: 4.8,
    comentario: "Colheitadeira muito bem revisada, entregou excelente rendimento na colheita.",
    recomenda: true,
    dataAvaliacao: ISODate("2026-09-14T16:20:00Z"),
    _class: "com.alugamaquina.backend.model.Avaliacao"
  },
  {
    _id: "aval-002",
    locacaoId: "LOC-2026-0755",
    locadorId: "usr-ambos-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-004",
    notaDesempenho: Int32(4),
    notaConservacao: Int32(5),
    notaFidelidade: Int32(5),
    notaPontualidade: Int32(5),
    notaSuporte: Int32(4),
    mediaFinal: 4.6,
    comentario: "Trator entregue limpo e no horario combinado. Atendimento rapido pelo chat.",
    recomenda: true,
    dataAvaliacao: ISODate("2026-09-25T11:10:00Z"),
    _class: "com.alugamaquina.backend.model.Avaliacao"
  },
  {
    _id: "aval-003",
    locacaoId: "LOC-2026-0790",
    locadorId: "usr-locador-01",
    locatarioId: "usr-ambos-01",
    maquinaId: "maq-003",
    notaDesempenho: Int32(5),
    notaConservacao: Int32(4),
    notaFidelidade: Int32(5),
    notaPontualidade: Int32(5),
    notaSuporte: Int32(5),
    mediaFinal: 4.8,
    comentario: "Pulverizador calibrado e pronto para o trabalho, recomendo o locador.",
    recomenda: true,
    dataAvaliacao: ISODate("2026-10-03T18:05:00Z"),
    _class: "com.alugamaquina.backend.model.Avaliacao"
  }
];

avaliacoesSeed.forEach((doc) => {
  database.avaliacoes.updateOne({ _id: doc._id }, { $setOnInsert: doc }, { upsert: true });
});

const mensagensSeed = [
  {
    _id: "msg-001",
    remetente: "Jose Carlos - Agricola Sao Jose",
    destinatario: "Antonio Carlos Pereira",
    conteudo: "Ola! Vi que voce tem interesse no Trator John Deere 6115J para a proxima semana. A maquina esta disponivel sim!",
    horario: "10:14",
    timestamp: ISODate("2026-11-19T10:14:00Z"),
    _class: "com.alugamaquina.backend.model.ChatMessage"
  },
  {
    _id: "msg-002",
    remetente: "Antonio Carlos Pereira",
    destinatario: "Jose Carlos - Agricola Sao Jose",
    conteudo: "Ola Jose, bom dia! Preciso para 8 diarias. Consegue entregar em Paulinia?",
    horario: "10:16",
    timestamp: ISODate("2026-11-19T10:16:00Z"),
    _class: "com.alugamaquina.backend.model.ChatMessage"
  },
  {
    _id: "msg-003",
    remetente: "Jose Carlos - Agricola Sao Jose",
    destinatario: "Antonio Carlos Pereira",
    conteudo: "Com certeza! O frete estimado fica em R$ 280,00. Podemos fechar o contrato digital pela plataforma?",
    horario: "10:20",
    timestamp: ISODate("2026-11-19T10:20:00Z"),
    _class: "com.alugamaquina.backend.model.ChatMessage"
  }
];

mensagensSeed.forEach((doc) => {
  database.chat_mensagens.updateOne({ _id: doc._id }, { $setOnInsert: doc }, { upsert: true });
});

const contratosSeed = [
  {
    _id: "ctr-001",
    locacaoId: "LOC-2026-0842",
    numero: "AM-2026-00001",
    locadorId: "usr-locador-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-001",
    nomeLocador: "Jose Carlos - Agricola Sao Jose",
    nomeLocatario: "Antonio Carlos Pereira",
    descricaoMaquina: "Trator John Deere 6115J Cabinado - Paulínia / SP",
    dataInicio: ISODate("2026-10-10T03:00:00Z"),
    dataFim: ISODate("2026-10-17T03:00:00Z"),
    valorTotal: NumberDecimal("7080.00"),
    valorCaucao: NumberDecimal("1000.00"),
    assinadoPeloLocador: true,
    assinadoPeloLocatario: true,
    dataAssinaturaLocador: ISODate("2026-10-06T10:00:00Z"),
    dataAssinaturaLocatario: ISODate("2026-10-06T11:30:00Z"),
    dataEmissao: ISODate("2026-10-06T09:00:00Z"),
    _class: "com.alugamaquina.backend.model.Contrato"
  },
  {
    _id: "ctr-002",
    locacaoId: "LOC-2026-0901",
    numero: "AM-2026-00002",
    locadorId: "usr-locador-01",
    locatarioId: "usr-locatario-01",
    maquinaId: "maq-001",
    nomeLocador: "Jose Carlos - Agricola Sao Jose",
    nomeLocatario: "Antonio Carlos Pereira",
    descricaoMaquina: "Trator John Deere 6115J Cabinado - Paulínia / SP",
    dataInicio: ISODate("2026-11-25T03:00:00Z"),
    dataFim: ISODate("2026-11-29T03:00:00Z"),
    valorTotal: NumberDecimal("4530.00"),
    valorCaucao: NumberDecimal("1000.00"),
    assinadoPeloLocador: true,
    assinadoPeloLocatario: false,
    dataAssinaturaLocador: ISODate("2026-11-18T11:00:00Z"),
    dataAssinaturaLocatario: null,
    dataEmissao: ISODate("2026-11-18T10:00:00Z"),
    _class: "com.alugamaquina.backend.model.Contrato"
  }
];

contratosSeed.forEach((doc) => {
  database.contratos.updateOne({ _id: doc._id }, { $setOnInsert: doc }, { upsert: true });
});

const vistoriasSeed = [
  {
    _id: "vst-001",
    locacaoId: "LOC-2026-0901",
    maquinaId: "maq-001",
    tipo: "RETIRADA",
    itens: [
      {
        descricao: "Pneus, rodas ou esteiras sem avarias",
        conforme: true,
        observacao: null
      },
      {
        descricao: "Nivel de oleo do motor",
        conforme: true,
        observacao: null
      },
      {
        descricao: "Nivel de fluidos (arrefecimento e hidraulico)",
        conforme: true,
        observacao: null
      },
      {
        descricao: "Luzes e sinalizacao funcionando",
        conforme: true,
        observacao: null
      },
      {
        descricao: "Freios funcionando",
        conforme: true,
        observacao: null
      },
      {
        descricao: "Cabine, vidros e bancos em bom estado",
        conforme: true,
        observacao: null
      },
      {
        descricao: "Implementos acoplados conferidos",
        conforme: true,
        observacao: null
      },
      {
        descricao: "Avarias visiveis (riscos, amassados, vazamentos)",
        conforme: true,
        observacao: null
      }
    ],
    horimetro: 1200.0,
    responsavelId: "usr-locador-01",
    dataVistoria: ISODate("2026-11-19T08:30:00Z"),
    status: "CONCLUIDA",
    _class: "com.alugamaquina.backend.model.VistoriaChecklist"
  }
];

vistoriasSeed.forEach((doc) => {
  database.vistorias_checklist.updateOne({ _id: doc._id }, { $setOnInsert: doc }, { upsert: true });
});

print(`[init-mongo] Banco '${targetDbName}' inicializado com sucesso!`);
