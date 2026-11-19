/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Integracao real da tela de avaliacao pos-locacao com a API REST (GET /api/locacoes/{id} para o resumo e POST /api/avaliacoes)
*/

document.addEventListener("DOMContentLoaded", function () {
  const API_AVALIACOES = "http://localhost:8080/api/avaliacoes";
  const API_LOCACOES = "http://localhost:8080/api/locacoes";
  const form = document.querySelector("main form");
  if (!form) return;

  // Ids vindos da locacao carregada (usados no envio se nao estiverem na URL)
  let locacaoCarregada = null;

  function formatarData(iso) {
    if (!iso) return "";
    const partes = iso.split("-");
    return partes.length === 3 ? partes[2] + "/" + partes[1] + "/" + partes[0] : iso;
  }

  function formatarMoeda(valor) {
    return Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  }

  function preencher(id, texto) {
    const elemento = document.getElementById(id);
    if (elemento && texto) elemento.textContent = texto;
  }

  // Carrega o resumo da locacao (GET /api/locacoes/{id}); campos que a API ainda nao envia ficam como estao
  async function carregarResumo() {
    const locacaoId = new URLSearchParams(window.location.search).get("locacaoId");
    if (!locacaoId) return;

    try {
      const resposta = await fetch(API_LOCACOES + "/" + encodeURIComponent(locacaoId));
      if (!resposta.ok) return;
      const l = await resposta.json();
      locacaoCarregada = l;

      preencher("resumoMaquina", l.tituloMaquina);
      preencher("resumoContrato", l.id ? "LOC-" + String(l.id).slice(-6).toUpperCase() : "");
      preencher("resumoStatus", l.status);
      preencher("resumoLocador", l.nomeLocador);
      if (l.dataInicio && l.dataFim) {
        preencher("resumoPeriodo", formatarData(l.dataInicio) + " a " + formatarData(l.dataFim)
          + " (" + l.diarias + (l.diarias === 1 ? " diaria)" : " diarias)"));
      }
      if (l.valorTotal != null) preencher("resumoTotal", formatarMoeda(l.valorTotal));

      const linkContrato = document.getElementById("resumoContrato");
      if (linkContrato) linkContrato.href = "contrato-digital.html?locacaoId=" + encodeURIComponent(l.id);
      const linkMaquina = document.getElementById("resumoMaquina");
      if (linkMaquina && l.maquinaId) linkMaquina.href = "detalhe-maquina.html?id=" + encodeURIComponent(l.maquinaId);
    } catch (erro) {
      // sem servidor: o resumo continua com o texto da tela
    }
  }

  carregarResumo();

  function lerNota(nomeGrupo) {
    const marcado = form.querySelector(`input[name="${nomeGrupo}"]:checked`);
    return marcado ? parseInt(marcado.value, 10) : 0;
  }

  function exibirFeedback(mensagem, sucesso) {
    let caixa = form.querySelector(".avaliacao-feedback");
    if (!caixa) {
      caixa = document.createElement("p");
      caixa.className = "avaliacao-feedback";
      caixa.style.padding = "12px 16px";
      caixa.style.borderRadius = "8px";
      caixa.style.marginBottom = "16px";
      caixa.style.fontSize = "0.95rem";
      form.prepend(caixa);
    }
    caixa.textContent = mensagem;
    caixa.style.backgroundColor = sucesso ? "#e8f5e9" : "#fdecea";
    caixa.style.color = sucesso ? "#1b5e20" : "#b71c1c";
    caixa.style.border = sucesso ? "1px solid #a5d6a7" : "1px solid #f5c6cb";
  }

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    const params = new URLSearchParams(window.location.search);
    const sessaoRaw = localStorage.getItem("alugamaquina_usuario");
    const sessao = sessaoRaw ? JSON.parse(sessaoRaw) : {};

    const locacaoId = params.get("locacaoId") || "";
    const locadorId = params.get("locadorId") || (locacaoCarregada && locacaoCarregada.locadorId) || "";
    const maquinaId = params.get("maquinaId") || (locacaoCarregada && locacaoCarregada.maquinaId) || "";
    const locatarioId = params.get("locatarioId") || (locacaoCarregada && locacaoCarregada.locatarioId) || sessao.usuarioId || "";

    if (!locacaoId || !locadorId || !locatarioId || !maquinaId) {
      exibirFeedback("Informe locacaoId, locadorId, locatarioId e maquinaId na URL ou faça login antes de avaliar.", false);
      return;
    }

    const comentario = document.getElementById("comentario").value.trim();
    if (comentario.length < 10) {
      exibirFeedback("Escreva um relato detalhado com pelo menos 10 caracteres.", false);
      return;
    }

    const recomendacao = form.querySelector('input[name="recomendacao"]:checked');
    const payload = {
      locacaoId,
      locadorId,
      locatarioId,
      maquinaId,
      notaDesempenho: lerNota("rating-desempenho"),
      notaConservacao: lerNota("rating-conservacao"),
      notaFidelidade: lerNota("rating-fidelidade"),
      notaPontualidade: lerNota("rating-pontualidade"),
      notaSuporte: lerNota("rating-suporte"),
      comentario,
      recomenda: recomendacao ? recomendacao.value === "sim" : false
    };

    const botao = form.querySelector('button[type="submit"]');
    botao.disabled = true;
    try {
      const resposta = await fetch(API_AVALIACOES, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      const dados = await resposta.json();

      if (!resposta.ok) {
        exibirFeedback(dados.erro || "Não foi possível registrar a avaliação.", false);
        return;
      }

      const media = typeof dados.mediaAvaliacao === "number" ? dados.mediaAvaliacao.toFixed(1) : "";
      exibirFeedback(`Avaliação registrada com sucesso! Média desta locação: ${media} estrelas.`, true);
      form.reset();
    } catch (erro) {
      exibirFeedback("Não foi possível conectar ao servidor (localhost:8080).", false);
    } finally {
      botao.disabled = false;
    }
  });
});
