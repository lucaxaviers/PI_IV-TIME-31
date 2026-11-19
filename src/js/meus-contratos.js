/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Lista os contratos do usuario logado (/api/contratos/usuario/{id}) em duas abas: pendentes e historico
*/

document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = "http://localhost:8080/api/contratos";

  const aviso = document.getElementById("avisoContratos");
  const listaPendentes = document.getElementById("listaPendentes");
  const listaHistorico = document.getElementById("listaHistorico");
  const abaPendentes = document.getElementById("abaPendentes");
  const abaHistorico = document.getElementById("abaHistorico");

  function lerUsuarioLogado() {
    try {
      return JSON.parse(localStorage.getItem("alugamaquina_usuario") || "{}");
    } catch (erro) {
      return {};
    }
  }

  function mostrarAviso(mensagem) {
    aviso.hidden = false;
    aviso.textContent = mensagem;
  }

  function formatarReais(valor) {
    return Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  }

  function formatarData(dataIso) {
    const partes = dataIso.split("-");
    return partes[2] + "/" + partes[1] + "/" + partes[0];
  }

  function criarTexto(tag, classe, texto) {
    const elemento = document.createElement(tag);
    elemento.className = classe;
    elemento.textContent = texto;
    return elemento;
  }

  // Selo que mostra se a parte ja assinou
  function criarSelo(papel, assinou) {
    return criarTexto("span", "selo " + (assinou ? "selo-ok" : "selo-falta"),
      papel + (assinou ? ": assinou" : ": falta assinar"));
  }

  async function baixarPdf(contrato, evento) {
    evento.preventDefault();
    try {
      const resposta = await fetch(API_BASE + "/" + encodeURIComponent(contrato.id) + "/pdf");
      if (!resposta.ok) {
        throw new Error("Não foi possível baixar o PDF.");
      }
      const arquivo = await resposta.blob();
      const link = document.createElement("a");
      link.href = URL.createObjectURL(arquivo);
      link.download = "contrato-" + contrato.numero + ".pdf";
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(link.href);
    } catch (erro) {
      mostrarAviso(erro.message);
    }
  }

  function criarCard(contrato, pendente) {
    const card = document.createElement("article");
    card.className = "contrato-card";

    const dados = document.createElement("div");
    dados.appendChild(criarTexto("div", "contrato-numero", "Nº " + contrato.numero));
    dados.appendChild(criarTexto("h2", "contrato-titulo", contrato.descricaoMaquina));
    dados.appendChild(criarTexto("p", "contrato-detalhe",
      contrato.nomeLocador + " (locador) e " + contrato.nomeLocatario + " (locatário)"));
    dados.appendChild(criarTexto("p", "contrato-detalhe",
      formatarData(contrato.dataInicio) + " a " + formatarData(contrato.dataFim) + " · " + formatarReais(contrato.valorTotal)));
    dados.appendChild(criarSelo("Locador", contrato.assinadoPeloLocador));
    dados.appendChild(criarSelo("Locatário", contrato.assinadoPeloLocatario));
    card.appendChild(dados);

    const acoes = document.createElement("div");
    acoes.className = "contrato-acoes";

    const abrir = criarTexto("a", "btn " + (pendente ? "btn-primary" : "btn-secondary"),
      pendente ? "Revisar e Assinar" : "Abrir Contrato");
    abrir.href = "contrato-digital.html?locacaoId=" + encodeURIComponent(contrato.locacaoId);
    acoes.appendChild(abrir);

    const pdf = criarTexto("a", "btn btn-secondary", "Baixar PDF");
    pdf.href = "#";
    pdf.addEventListener("click", function (evento) { baixarPdf(contrato, evento); });
    acoes.appendChild(pdf);

    card.appendChild(acoes);
    return card;
  }

  function preencherLista(lista, contratos, pendente, mensagemVazia) {
    lista.textContent = "";
    if (contratos.length === 0) {
      lista.appendChild(criarTexto("p", "vazio", mensagemVazia));
      return;
    }
    contratos.forEach(function (contrato) {
      lista.appendChild(criarCard(contrato, pendente));
    });
  }

  function mostrarAba(pendentes) {
    abaPendentes.classList.toggle("ativa", pendentes);
    abaHistorico.classList.toggle("ativa", !pendentes);
    abaPendentes.setAttribute("aria-selected", String(pendentes));
    abaHistorico.setAttribute("aria-selected", String(!pendentes));
    listaPendentes.hidden = !pendentes;
    listaHistorico.hidden = pendentes;
  }

  abaPendentes.addEventListener("click", function () { mostrarAba(true); });
  abaHistorico.addEventListener("click", function () { mostrarAba(false); });

  async function iniciar() {
    const usuario = lerUsuarioLogado();
    if (!usuario.usuarioId) {
      listaPendentes.textContent = "";
      mostrarAviso("Faça login para ver os seus contratos.");
      return;
    }

    try {
      const resposta = await fetch(API_BASE + "/usuario/" + encodeURIComponent(usuario.usuarioId));
      const contratos = await resposta.json();
      if (!resposta.ok) {
        throw new Error(contratos.erro || "Não foi possível carregar os contratos.");
      }

      // Pendente = ainda falta alguma assinatura; historico = as duas partes assinaram
      const pendentes = contratos.filter(function (c) { return !c.vigente; });
      const historico = contratos.filter(function (c) { return c.vigente; });

      document.getElementById("contadorPendentes").textContent = String(pendentes.length);
      document.getElementById("contadorHistorico").textContent = String(historico.length);

      preencherLista(listaPendentes, pendentes, true, "Você não tem contratos aguardando assinatura.");
      preencherLista(listaHistorico, historico, false, "Você ainda não tem contratos assinados.");
    } catch (erro) {
      listaPendentes.textContent = "";
      mostrarAviso(erro.message || "Servidor offline no momento.");
    }
  }

  iniciar();
});
