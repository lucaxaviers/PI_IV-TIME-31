/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Tela de vistoria tecnica: abre ou carrega o checklist da locacao, valida as respostas e conclui pela API (/api/vistorias)
*/

document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = "http://localhost:8080/api/vistorias";

  // A tela recebe a locacao e o tipo na URL: vistoria-checklist.html?locacaoId=...&tipo=RETIRADA
  const params = new URLSearchParams(window.location.search);
  const locacaoId = params.get("locacaoId");
  const tipo = (params.get("tipo") || "RETIRADA").toUpperCase();

  const titulo = document.getElementById("tituloVistoria");
  const aviso = document.getElementById("avisoVistoria");
  const formulario = document.getElementById("formVistoria");
  const listaChecklist = document.getElementById("listaChecklist");
  const campoHorimetro = document.getElementById("horimetro");
  const botaoConcluir = document.getElementById("btnConcluir");

  const NOMES_DO_TIPO = { RETIRADA: "Retirada", DEVOLUCAO: "Devolução" };
  const NOMES_DO_STATUS = {
    PENDENTE: "Em preenchimento",
    CONCLUIDA: "Concluída",
    COM_PENDENCIAS: "Concluída com pendências"
  };

  let vistoriaAtual = null;

  // Link de volta para o contrato da mesma locacao
  if (locacaoId) {
    document.getElementById("linkContrato").href = "contrato-digital.html?locacaoId=" + encodeURIComponent(locacaoId);
  }

  function lerUsuarioLogado() {
    try {
      return JSON.parse(localStorage.getItem("alugamaquina_usuario") || "{}");
    } catch (erro) {
      return {};
    }
  }

  function mostrarAviso(mensagem, sucesso) {
    aviso.hidden = false;
    aviso.textContent = mensagem;
    aviso.className = "aviso " + (sucesso ? "aviso-sucesso" : "aviso-erro");
  }

  // Le a resposta da API; em erro devolve a mensagem que o servidor mandou
  async function chamarApi(url, opcoes) {
    const resposta = await fetch(url, opcoes);
    const dados = await resposta.json();
    if (!resposta.ok) {
      throw new Error(dados.erro || "Não foi possível concluir a operação.");
    }
    return dados;
  }

  // Procura a vistoria do tipo na locacao; se ainda nao existe, abre uma nova
  async function carregarVistoria(usuarioId) {
    const existentes = await chamarApi(API_BASE + "/locacao/" + encodeURIComponent(locacaoId));
    const doTipo = existentes.find(function (vistoria) { return vistoria.tipo === tipo; });
    if (doTipo) {
      return doTipo;
    }
    return await chamarApi(API_BASE, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ locacaoId: locacaoId, tipo: tipo, responsavelId: usuarioId })
    });
  }

  // Cria uma opcao (Conforme ou Nao conforme) como botao grande
  function criarOpcao(nomeDoGrupo, valor, rotulo, classeExtra) {
    const label = document.createElement("label");
    label.className = "opcao " + classeExtra;

    const radio = document.createElement("input");
    radio.type = "radio";
    radio.name = nomeDoGrupo;
    radio.value = valor;

    const texto = document.createElement("span");
    texto.textContent = rotulo;

    label.appendChild(radio);
    label.appendChild(texto);
    return label;
  }

  // Desenha um item do checklist
  function criarItem(item, indice, somenteLeitura) {
    const grupo = "item-" + indice;

    const fieldset = document.createElement("fieldset");
    fieldset.className = "item-vistoria";
    fieldset.dataset.descricao = item.descricao;

    const legenda = document.createElement("legend");
    legenda.textContent = (indice + 1) + ". " + item.descricao;
    fieldset.appendChild(legenda);

    const opcoes = document.createElement("div");
    opcoes.className = "opcoes";
    const opcaoSim = criarOpcao(grupo, "sim", "Conforme", "opcao-sim");
    const opcaoNao = criarOpcao(grupo, "nao", "Não conforme", "opcao-nao");
    opcoes.appendChild(opcaoSim);
    opcoes.appendChild(opcaoNao);
    fieldset.appendChild(opcoes);

    const blocoObservacao = document.createElement("div");
    blocoObservacao.className = "form-group observacao";
    blocoObservacao.hidden = true;

    const rotuloObservacao = document.createElement("label");
    rotuloObservacao.className = "form-label";
    rotuloObservacao.htmlFor = grupo + "-obs";
    rotuloObservacao.textContent = "Descreva o problema";

    const campoObservacao = document.createElement("textarea");
    campoObservacao.className = "form-textarea";
    campoObservacao.id = grupo + "-obs";
    campoObservacao.rows = 2;
    campoObservacao.maxLength = 300;
    campoObservacao.placeholder = "Ex.: pneu dianteiro furado";

    blocoObservacao.appendChild(rotuloObservacao);
    blocoObservacao.appendChild(campoObservacao);
    fieldset.appendChild(blocoObservacao);

    // Preenche o que ja foi respondido
    if (item.conforme === true) {
      opcaoSim.querySelector("input").checked = true;
    } else if (item.conforme === false) {
      opcaoNao.querySelector("input").checked = true;
      blocoObservacao.hidden = false;
      fieldset.classList.add("item-nao-conforme");
    }
    campoObservacao.value = item.observacao || "";

    if (somenteLeitura) {
      fieldset.disabled = true;
    }

    // Ao mudar a resposta: mostra ou esconde a observacao e atualiza o resumo
    fieldset.addEventListener("change", function () {
      const naoConforme = opcaoNao.querySelector("input").checked;
      blocoObservacao.hidden = !naoConforme;
      fieldset.classList.toggle("item-nao-conforme", naoConforme);
      atualizarResumo();
    });

    return fieldset;
  }

  function lerRespostas() {
    return Array.from(listaChecklist.querySelectorAll(".item-vistoria")).map(function (fieldset) {
      const marcado = fieldset.querySelector("input[type=radio]:checked");
      return {
        descricao: fieldset.dataset.descricao,
        conforme: marcado ? marcado.value === "sim" : null,
        observacao: fieldset.querySelector("textarea").value.trim()
      };
    });
  }

  function atualizarResumo() {
    const respostas = lerRespostas();
    const marcados = respostas.filter(function (r) { return r.conforme !== null; }).length;
    const pendencias = respostas.filter(function (r) { return r.conforme === false; }).length;

    document.getElementById("resumoProgresso").textContent = marcados + " de " + respostas.length;
    document.getElementById("resumoPendencias").textContent = String(pendencias);
  }

  function preencherTela(vistoria) {
    const nomeDoTipo = NOMES_DO_TIPO[vistoria.tipo] || vistoria.tipo;
    const concluida = vistoria.status !== "PENDENTE";

    titulo.textContent = "Vistoria Técnica - " + nomeDoTipo;
    document.getElementById("resumoTipo").textContent = nomeDoTipo;
    document.getElementById("resumoStatus").textContent = NOMES_DO_STATUS[vistoria.status] || vistoria.status;

    listaChecklist.textContent = "";
    vistoria.itens.forEach(function (item, indice) {
      listaChecklist.appendChild(criarItem(item, indice, concluida));
    });

    if (vistoria.horimetro !== null && vistoria.horimetro !== undefined) {
      campoHorimetro.value = vistoria.horimetro;
    }
    campoHorimetro.disabled = concluida;
    botaoConcluir.disabled = concluida;
    botaoConcluir.textContent = concluida ? "Vistoria concluída" : "Concluir Vistoria";

    atualizarResumo();
  }

  // Validacao no navegador, antes de enviar (o servidor valida de novo)
  function validar(respostas, horimetro) {
    const semResposta = respostas.filter(function (r) { return r.conforme === null; });
    if (semResposta.length > 0) {
      return "Marque Conforme ou Não conforme em todos os itens. Faltam " + semResposta.length + ".";
    }
    const semObservacao = respostas.filter(function (r) { return r.conforme === false && !r.observacao; });
    if (semObservacao.length > 0) {
      return "Descreva o problema de cada item marcado como Não conforme.";
    }
    if (campoHorimetro.value.trim() === "" || isNaN(horimetro)) {
      return "Informe o horímetro da máquina.";
    }
    if (horimetro < 0) {
      return "O horímetro não pode ser negativo.";
    }
    return null;
  }

  async function concluir(evento) {
    evento.preventDefault();
    if (!vistoriaAtual) {
      return;
    }

    const respostas = lerRespostas();
    const horimetro = parseFloat(campoHorimetro.value.replace(",", "."));

    const erro = validar(respostas, horimetro);
    if (erro) {
      mostrarAviso(erro, false);
      return;
    }

    botaoConcluir.disabled = true;
    try {
      vistoriaAtual = await chamarApi(API_BASE + "/" + encodeURIComponent(vistoriaAtual.id) + "/concluir", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ horimetro: horimetro, itens: respostas })
      });
      preencherTela(vistoriaAtual);
      mostrarAviso(vistoriaAtual.mensagem, !vistoriaAtual.temPendencias);
    } catch (falha) {
      botaoConcluir.disabled = false;
      mostrarAviso(falha.message, false);
    }
  }

  async function iniciar() {
    if (!locacaoId) {
      listaChecklist.textContent = "";
      botaoConcluir.disabled = true;
      mostrarAviso("Abra a vistoria a partir do contrato da locação.", false);
      return;
    }

    const usuario = lerUsuarioLogado();
    if (!usuario.usuarioId) {
      botaoConcluir.disabled = true;
      mostrarAviso("Faça login para preencher a vistoria.", false);
      return;
    }

    try {
      vistoriaAtual = await carregarVistoria(usuario.usuarioId);
      preencherTela(vistoriaAtual);
      formulario.addEventListener("submit", concluir);
    } catch (erro) {
      listaChecklist.textContent = "";
      botaoConcluir.disabled = true;
      mostrarAviso(erro.message || "Servidor offline no momento.", false);
    }
  }

  iniciar();
});
