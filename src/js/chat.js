/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descrição: Integracao do chat com WebSocket (STOMP/SockJS), historico REST (/api/chat/historico),
             reconexao automatica, status online/offline e rolagem automatica sem dados mockados.
*/

document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = "http://localhost:8080";
  const API_HISTORICO = `${API_BASE}/api/chat/historico`;
  const WS_ENDPOINT = `${API_BASE}/ws-chat`;
  const AVATAR_PADRAO = "../../assets/ico-AlugaMaquina.jpg";

  const chatForm = document.querySelector(".chat-input-area");
  const chatInput = chatForm ? chatForm.querySelector(".form-input") : null;
  const chatMessages = document.getElementById("chatMessages") || document.querySelector(".chat-messages");
  const conversationsList = document.getElementById("conversationsList") || document.querySelector(".conversations-list");
  const partnerNameEl = document.getElementById("chatPartnerName");
  const statusEl = document.getElementById("chatStatus");

  if (!chatForm || !chatInput || !chatMessages) return;

  const sessao = JSON.parse(localStorage.getItem("alugamaquina_usuario") || "{}");
  const remetenteAtual = (sessao.nome || "").trim() || "Produtor";
  const paramsUrl = new URLSearchParams(window.location.search);
  const destinatarioParam = (paramsUrl.get("destinatario") || "").trim();

  let todasMensagens = [];
  let destinatarioAtual =
    destinatarioParam && destinatarioParam.toLowerCase() !== remetenteAtual.toLowerCase()
      ? destinatarioParam
      : remetenteAtual.toLowerCase() === "jose carlos - agricola sao jose"
        ? "Antonio Carlos Pereira"
        : "Jose Carlos - Agricola Sao Jose";

  let stompClient = null;
  let conectado = false;
  let tentativaReconexao = 0;

  function atualizarStatus(texto, cor) {
    if (!statusEl) return;
    statusEl.textContent = texto;
    statusEl.style.color = cor;
  }

  function escaparHTML(texto) {
    const div = document.createElement("div");
    div.textContent = texto || "";
    return div.innerHTML;
  }

  function normalizarNome(texto) {
    return (texto || "").trim().toLowerCase();
  }

  function horarioAtual() {
    const agora = new Date();
    return String(agora.getHours()).padStart(2, "0") + ":" + String(agora.getMinutes()).padStart(2, "0");
  }

  function usuarioParticipaDaMensagem(msg) {
    const eu = normalizarNome(remetenteAtual);
    const rem = normalizarNome(msg.remetente);
    const dest = normalizarNome(msg.destinatario);
    return rem === eu || dest === eu;
  }

  function obterOutroParticipante(msg) {
    if (!usuarioParticipaDaMensagem(msg)) {
      return null;
    }
    const eu = normalizarNome(remetenteAtual);
    const rem = (msg.remetente || "").trim();
    const dest = (msg.destinatario || "").trim();
    if (normalizarNome(rem) === eu) {
      return dest || null;
    }
    return rem || null;
  }

  function mostrarEstadoVazio(texto) {
    chatMessages.innerHTML = `<p style="text-align: center; color: #666; margin: auto;">${escaparHTML(texto)}</p>`;
  }

  function adicionarBolha(texto, horario, enviada, nomeRemetente) {
    const vazio = chatMessages.querySelector("p");
    if (vazio && !chatMessages.querySelector(".message")) {
      chatMessages.innerHTML = "";
    }

    const div = document.createElement("div");
    div.classList.add("message", enviada ? "sent" : "received");
    const remetenteHtml =
      !enviada && nomeRemetente
        ? `<span class="message-sender">${escaparHTML(nomeRemetente)}</span>`
        : "";
    div.innerHTML =
      remetenteHtml +
      `<div class="message-bubble">${escaparHTML(texto)}</div>` +
      `<span class="message-time">${escaparHTML(horario)}</span>`;
    chatMessages.appendChild(div);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function renderizarConversas() {
    if (!conversationsList) return;

    const eu = normalizarNome(remetenteAtual);
    const mapa = new Map();
    todasMensagens.forEach((m) => {
      const contato = obterOutroParticipante(m);
      const chaveContato = normalizarNome(contato);
      if (!chaveContato || chaveContato === eu) return;
      mapa.set(chaveContato, { nome: contato, ultimaMsg: m });
    });

    if (destinatarioParam && !mapa.has(normalizarNome(destinatarioParam))) {
      mapa.set(normalizarNome(destinatarioParam), { nome: destinatarioParam, ultimaMsg: null });
    }

    const chaveAtual = normalizarNome(destinatarioAtual);
    if (mapa.size > 0 && !mapa.has(chaveAtual)) {
      destinatarioAtual = Array.from(mapa.values())[0].nome;
    }

    if (partnerNameEl) {
      partnerNameEl.textContent = destinatarioAtual;
    }

    if (mapa.size === 0) {
      conversationsList.innerHTML =
        `<p class="last-message" style="padding: 16px; white-space: normal;">` +
        `Nenhuma conversa iniciada ainda. Envie uma mensagem para iniciar com ${escaparHTML(destinatarioAtual)}.</p>`;
      return;
    }

    const chaveSelecionada = normalizarNome(destinatarioAtual);
    conversationsList.innerHTML = "";
    mapa.forEach((entrada, chaveContato) => {
      const contato = entrada.nome;
      const ultimaMsg = entrada.ultimaMsg;
      const item = document.createElement("div");
      item.className = "conversation-item" + (chaveContato === chaveSelecionada ? " active" : "");

      const resumo = ultimaMsg && ultimaMsg.conteudo ? ultimaMsg.conteudo : "Nenhuma mensagem ainda";
      item.innerHTML =
        `<div class="user-avatar"><img src="${AVATAR_PADRAO}" alt="Avatar" /></div>` +
        `<div class="conversation-info">` +
        `<strong class="user-name">${escaparHTML(contato)}</strong>` +
        `<p class="last-message">${escaparHTML(resumo)}</p>` +
        `</div>`;

      item.addEventListener("click", function () {
        destinatarioAtual = contato;
        renderizarConversas();
        renderizarMensagensAtivas();
      });

      conversationsList.appendChild(item);
    });
  }

  function renderizarMensagensAtivas() {
    const eu = normalizarNome(remetenteAtual);
    const outro = normalizarNome(destinatarioAtual);
    const filtradas = todasMensagens.filter((m) => {
      const rem = normalizarNome(m.remetente);
      const dest = normalizarNome(m.destinatario);
      return (rem === eu && dest === outro) || (rem === outro && dest === eu);
    });

    if (filtradas.length === 0) {
      mostrarEstadoVazio("Nenhuma mensagem nesta conversa ainda. Envie a primeira mensagem abaixo.");
      return;
    }

    chatMessages.innerHTML = "";
    filtradas.forEach((m) => {
      const remetenteMsg = (m.remetente || "").trim();
      const enviadaPorMim = normalizarNome(remetenteMsg) === eu;
      adicionarBolha(m.conteudo || "", m.horario || "", enviadaPorMim, remetenteMsg);
    });
  }

  function carregarHistorico() {
    fetch(API_HISTORICO)
      .then((res) => {
        if (!res.ok) throw new Error("Erro ao consultar historico");
        return res.json();
      })
      .then((lista) => {
        todasMensagens = Array.isArray(lista) ? lista : [];
        renderizarConversas();
        renderizarMensagensAtivas();
      })
      .catch(() => {
        todasMensagens = [];
        renderizarConversas();
        mostrarEstadoVazio("Não foi possível carregar o histórico do servidor (localhost:8080).");
      });
  }

  function conectarWebSocket() {
    if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
      atualizarStatus("Offline", "#b71c1c");
      return;
    }

    atualizarStatus("Conectando...", "#8a6d3b");

    const socket = new SockJS(WS_ENDPOINT);
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect(
      {},
      function () {
        conectado = true;
        tentativaReconexao = 0;
        atualizarStatus("Online", "#1A5632");

        stompClient.subscribe("/topic/mensagens", function (frame) {
          const msg = JSON.parse(frame.body);
          if (!usuarioParticipaDaMensagem(msg)) return;

          todasMensagens.push(msg);
          renderizarConversas();

          const contatoDaMsg = obterOutroParticipante(msg);
          if (contatoDaMsg && normalizarNome(contatoDaMsg) === normalizarNome(destinatarioAtual)) {
            const remetenteMsg = (msg.remetente || "").trim();
            const enviadaPorMim = normalizarNome(remetenteMsg) === normalizarNome(remetenteAtual);
            adicionarBolha(msg.conteudo || "", msg.horario || horarioAtual(), enviadaPorMim, remetenteMsg);
          }
        });
      },
      function () {
        conectado = false;
        tentativaReconexao++;
        const atraso = Math.min(3000 * tentativaReconexao, 15000);
        atualizarStatus("Reconectando...", "#b71c1c");
        setTimeout(conectarWebSocket, atraso);
      }
    );
  }

  carregarHistorico();
  conectarWebSocket();

  chatForm.addEventListener("submit", function (event) {
    event.preventDefault();
    const mensagemTexto = chatInput.value.trim();
    if (!mensagemTexto) {
      chatInput.focus();
      return;
    }

    if (!conectado || !stompClient) {
      atualizarStatus("Offline — aguardando servidor para enviar", "#b71c1c");
      return;
    }

    stompClient.send(
      "/app/chat.enviar",
      {},
      JSON.stringify({
        remetente: remetenteAtual,
        destinatario: destinatarioAtual,
        conteudo: mensagemTexto,
        horario: horarioAtual()
      })
    );

    chatInput.value = "";
    chatInput.focus();
  });
});
