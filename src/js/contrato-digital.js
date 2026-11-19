/*
  Autor: Kauan Aurélio Lasmar Dias | 25001590
  Descricao: Liga a tela de contrato digital a API (/api/contratos): gera o contrato, preenche a tela,
             registra a assinatura e baixa o PDF. Sem locacaoId na URL a tela continua como prototipo.
*/

document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = "http://localhost:8080/api/contratos";

  // A locacao vem na URL: contrato-digital.html?locacaoId=...
  const params = new URLSearchParams(window.location.search);
  const locacaoId = params.get("locacaoId");

  // Sem locacao informada, nada a fazer: a tela fica como prototipo
  if (!locacaoId) {
    return;
  }

  const botaoAssinar = document.getElementById("btnAssinar");
  const botoesPdf = [document.getElementById("btnBaixarPdf"), document.getElementById("btnBaixarPdfLateral")];
  const feedback = document.getElementById("feedbackContrato");
  const aceite = document.getElementById("aceiteContrato");
  const campoNome = document.getElementById("nomeAssinatura");

  let contratoAtual = null;

  function lerUsuarioLogado() {
    try {
      return JSON.parse(localStorage.getItem("alugamaquina_usuario") || "{}");
    } catch (erro) {
      return {};
    }
  }

  function formatarReais(valor) {
    return Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  }

  function formatarData(dataIso) {
    const partes = dataIso.split("-");
    return partes[2] + "/" + partes[1] + "/" + partes[0];
  }

  function escrever(id, texto) {
    const elemento = document.getElementById(id);
    if (elemento) {
      elemento.textContent = texto;
    }
  }

  function mostrarAviso(mensagem, sucesso) {
    feedback.hidden = false;
    feedback.style.display = "block";
    feedback.textContent = mensagem;
    feedback.style.color = sucesso ? "#1b5e20" : "#b71c1c";
  }

  // Preenche resumo, minuta e total com os dados do contrato
  function preencherTela(contrato) {
    const periodo = formatarData(contrato.dataInicio) + " a " + formatarData(contrato.dataFim);
    const diarias = contrato.diarias + (contrato.diarias === 1 ? " diária" : " diárias");

    escrever("nomeLocador", contrato.nomeLocador);
    escrever("nomeLocatario", contrato.nomeLocatario);
    escrever("resumoEquipamento", contrato.descricaoMaquina);
    escrever("resumoPeriodo", periodo + " (" + diarias + ")");
    escrever("resumoTotal", formatarReais(contrato.valorTotal));
    escrever("resumoCaucao", formatarReais(contrato.valorCaucao));
    escrever("totalAssinatura", formatarReais(contrato.valorTotal));
    escrever("codigoContrato", "Nº " + contrato.numero + " · Emitido via AlugaMáquina");

    // O contrato guarda so o total, entao as linhas de diaria e frete saem da tela
    ["linhaDiarias", "linhaFrete"].forEach(function (id) {
      const linha = document.getElementById(id);
      if (linha) {
        linha.hidden = true;
      }
    });

    escrever("clausulaObjeto", "O LOCADOR cede ao LOCATÁRIO, em caráter temporário, o equipamento "
      + contrato.descricaoMaquina + ", em perfeito estado de funcionamento.");
    escrever("clausulaPrazo", "A locação vigora de " + periodo + " (" + diarias
      + "), devendo o equipamento ser devolvido no último dia, no mesmo local da retirada.");
    escrever("clausulaValor", "O valor total é de " + formatarReais(contrato.valorTotal)
      + ", já incluindo as diárias e o frete estimado, pago conforme combinado na plataforma.");
    escrever("clausulaCaucao", "Foi retida caução de " + formatarReais(contrato.valorCaucao)
      + ". Em caso de danos não decorrentes do desgaste normal, apurados no checklist de devolução, "
      + "o valor do reparo será abatido da caução, cabendo ao LOCATÁRIO complementar eventual diferença.");
    escrever("clausulaForo", "As partes elegem o foro da comarca do LOCADOR para dirimir quaisquer dúvidas deste contrato.");
  }

  // Mostra quem ja assinou no botao e no aviso
  function atualizarEstadoDasAssinaturas(contrato) {
    if (contrato.vigente) {
      mostrarAviso("Contrato assinado pelas duas partes.", true);
      botaoAssinar.textContent = "Contrato assinado";
    } else if (contrato.assinadoPeloLocador || contrato.assinadoPeloLocatario) {
      const quem = contrato.assinadoPeloLocador ? "locador" : "locatário";
      mostrarAviso("Já assinou o " + quem + ". Falta a assinatura da outra parte.", true);
    }
  }

  // Le a resposta da API; em erro, devolve a mensagem que o servidor mandou
  async function chamarApi(url, opcoes) {
    const resposta = await fetch(url, opcoes);
    const dados = await resposta.json();
    if (!resposta.ok) {
      throw new Error(dados.erro || "Não foi possível concluir a operação.");
    }
    return dados;
  }

  // Procura o contrato da locacao; se ainda nao existe, gera
  async function carregarContrato() {
    try {
      return await chamarApi(API_BASE + "/locacao/" + encodeURIComponent(locacaoId));
    } catch (erro) {
      return await chamarApi(API_BASE + "/gerar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ locacaoId: locacaoId })
      });
    }
  }

  async function baixarPdf(evento) {
    evento.preventDefault();
    if (!contratoAtual) {
      return;
    }
    try {
      const resposta = await fetch(API_BASE + "/" + encodeURIComponent(contratoAtual.id) + "/pdf");
      if (!resposta.ok) {
        throw new Error("Não foi possível baixar o PDF.");
      }
      const arquivo = await resposta.blob();
      const link = document.createElement("a");
      link.href = URL.createObjectURL(arquivo);
      link.download = "contrato-" + contratoAtual.numero + ".pdf";
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(link.href);
    } catch (erro) {
      mostrarAviso(erro.message, false);
    }
  }

  async function assinar(evento) {
    evento.preventDefault();

    const usuario = lerUsuarioLogado();
    if (!usuario.usuarioId) {
      mostrarAviso("Faça login para assinar o contrato.", false);
      return;
    }
    if (!campoNome.value.trim()) {
      mostrarAviso("Digite seu nome completo para assinar.", false);
      return;
    }
    if (!aceite.checked) {
      mostrarAviso("Marque que leu e concorda com os termos do contrato.", false);
      return;
    }

    botaoAssinar.setAttribute("aria-disabled", "true");
    try {
      contratoAtual = await chamarApi(API_BASE + "/" + encodeURIComponent(contratoAtual.id) + "/assinar", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ usuarioId: usuario.usuarioId })
      });
      atualizarEstadoDasAssinaturas(contratoAtual);

      // Com as duas assinaturas, segue para a avaliacao como no fluxo original
      if (contratoAtual.vigente) {
        setTimeout(function () { window.location.href = "avaliacao-pos-locacao.html"; }, 1500);
      }
    } catch (erro) {
      mostrarAviso(erro.message, false);
    } finally {
      botaoAssinar.removeAttribute("aria-disabled");
    }
  }

  async function iniciar() {
    try {
      contratoAtual = await carregarContrato();
      preencherTela(contratoAtual);
      atualizarEstadoDasAssinaturas(contratoAtual);
    } catch (erro) {
      mostrarAviso(erro.message || "Servidor offline no momento.", false);
      return;
    }

    botaoAssinar.addEventListener("click", assinar);
    botoesPdf.forEach(function (botao) {
      if (botao) {
        botao.removeAttribute("onclick");
        botao.addEventListener("click", baixarPdf);
      }
    });
  }

  iniciar();
});
