/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Integracao real da tela de avaliacao pos-locacao com a API REST (GET /api/locacoes e POST /api/avaliacoes)
*/

document.addEventListener("DOMContentLoaded", function () {
  const API_AVALIACOES = "http://localhost:8080/api/avaliacoes";
  const API_LOCACOES = "http://localhost:8080/api/locacoes";
  const form = document.querySelector("main form");
  if (!form) return;

  // Dados da locacao carregada (por id na URL ou pela lista de locacoes do usuario)
  let locacaoCarregada = null;

  function lerSessao() {
    try {
      const bruto = localStorage.getItem("alugamaquina_usuario");
      return bruto ? JSON.parse(bruto) : {};
    } catch (erro) {
      return {};
    }
  }

  function formatarData(iso) {
    if (!iso) return "";
    const partes = String(iso).split("-");
    return partes.length === 3 ? partes[2] + "/" + partes[1] + "/" + partes[0] : iso;
  }

  function formatarMoeda(valor) {
    return Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  }

  function preencher(id, texto) {
    const elemento = document.getElementById(id);
    if (elemento && texto) elemento.textContent = texto;
  }

  // Atualiza os campos visuais do card de resumo a partir da locacao retornada pela API
  function preencherResumoLocacao(l) {
    if (!l) return;
    locacaoCarregada = l;

    preencher("resumoMaquina", l.tituloMaquina);
    preencher("resumoContrato", l.id ? "LOC-" + String(l.id).slice(-6).toUpperCase() : "");
    preencher("resumoStatus", l.status);
    preencher("resumoLocador", l.nomeLocador);
    if (l.dataInicio && l.dataFim) {
      preencher(
        "resumoPeriodo",
        formatarData(l.dataInicio) + " a " + formatarData(l.dataFim)
          + " (" + l.diarias + (l.diarias === 1 ? " diaria)" : " diarias)")
      );
    }
    if (l.valorTotal != null) preencher("resumoTotal", formatarMoeda(l.valorTotal));

    const linkContrato = document.getElementById("resumoContrato");
    if (linkContrato && l.id) linkContrato.href = "contrato-digital.html?locacaoId=" + encodeURIComponent(l.id);
    const linkMaquina = document.getElementById("resumoMaquina");
    if (linkMaquina && l.maquinaId) linkMaquina.href = "detalhe-maquina.html?id=" + encodeURIComponent(l.maquinaId);
  }

  // Prioriza locacao FINALIZADA, depois ACEITA, depois a primeira da lista do usuario
  function selecionarLocacaoParaAvaliar(lista) {
    if (!Array.isArray(lista) || lista.length === 0) return null;
    return (
      lista.find((item) => item.status === "FINALIZADO")
      || lista.find((item) => item.status === "ACEITO")
      || lista[0]
    );
  }

  // Carrega a locacao pela URL (?locacaoId=...) ou busca automaticamente a mais recente do usuario logado
  async function carregarResumo() {
    const locacaoId = new URLSearchParams(window.location.search).get("locacaoId");

    try {
      if (locacaoId) {
        const resposta = await fetch(API_LOCACOES + "/" + encodeURIComponent(locacaoId));
        if (!resposta.ok) return;
        const locacao = await resposta.json();
        preencherResumoLocacao(locacao);
        return;
      }

      const sessao = lerSessao();
      if (!sessao.usuarioId) return;

      const respostaLista = await fetch(API_LOCACOES + "/usuario/" + encodeURIComponent(sessao.usuarioId));
      if (!respostaLista.ok) return;
      const lista = await respostaLista.json();
      const escolhida = selecionarLocacaoParaAvaliar(lista);
      preencherResumoLocacao(escolhida);
    } catch (erro) {
      // Sem servidor ativo: mantem o resumo exibido na tela sem quebrar o carregamento
    }
  }

  carregarResumo();

  // Le a nota selecionada (1 a 5) de cada criterio de avaliacao
  function lerNota(nomeGrupo) {
    const marcado = form.querySelector(`input[name="${nomeGrupo}"]:checked`);
    return marcado ? parseInt(marcado.value, 10) : 0;
  }

  function exibirFeedback(mensagem, sucesso) {
    let caixa = form.querySelector(".avaliacao-feedback");
    if (!caixa) {
      caixa = document.createElement("p");
      caixa.className = "avaliacao-feedback";
      caixa.setAttribute("role", "status");
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
    caixa.scrollIntoView({ behavior: "smooth", block: "center" });
  }

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    const params = new URLSearchParams(window.location.search);
    const sessao = lerSessao();

    const locacaoId = params.get("locacaoId") || (locacaoCarregada && locacaoCarregada.id) || "";
    const locadorId = params.get("locadorId") || (locacaoCarregada && locacaoCarregada.locadorId) || "";
    const maquinaId = params.get("maquinaId") || (locacaoCarregada && locacaoCarregada.maquinaId) || "";
    const locatarioId = params.get("locatarioId") || (locacaoCarregada && locacaoCarregada.locatarioId) || sessao.usuarioId || "";

    if (!locacaoId || !locadorId || !locatarioId || !maquinaId) {
      exibirFeedback("Nenhuma locação vinculada ao seu usuário foi encontrada para avaliar.", false);
      return;
    }

    const notaDesempenho = lerNota("rating-desempenho");
    const notaConservacao = lerNota("rating-conservacao");
    const notaFidelidade = lerNota("rating-fidelidade");
    const notaPontualidade = lerNota("rating-pontualidade");
    const notaSuporte = lerNota("rating-suporte");

    if ([notaDesempenho, notaConservacao, notaFidelidade, notaPontualidade, notaSuporte].some((n) => n < 1 || n > 5)) {
      exibirFeedback("Selecione uma nota de 1 a 5 estrelas para todos os critérios.", false);
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
      notaDesempenho,
      notaConservacao,
      notaFidelidade,
      notaPontualidade,
      notaSuporte,
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

      // Usa os nomes exatos de AvaliacaoResponse.java (mediaFinalAvaliacao e novaReputacaoLocador)
      const mediaNum = dados.mediaFinalAvaliacao ?? dados.mediaAvaliacao;
      const media = typeof mediaNum === "number" ? mediaNum.toFixed(1) : "";
      const novaReputacao = typeof dados.novaReputacaoLocador === "number"
        ? ` Nova reputação do locador: ${dados.novaReputacaoLocador.toFixed(2)}.`
        : "";
      exibirFeedback(`Avaliação registrada com sucesso! Média desta locação: ${media} estrelas.${novaReputacao}`, true);
      form.reset();
    } catch (erro) {
      exibirFeedback("Não foi possível conectar ao servidor (localhost:8080).", false);
    } finally {
      botao.disabled = false;
    }
  });
});
