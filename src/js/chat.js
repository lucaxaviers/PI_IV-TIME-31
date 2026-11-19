/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descrição: Integracao do chat com o historico REST (/api/chat/historico) e envio de mensagens
*/

document.addEventListener("DOMContentLoaded", function () {
  const API_HISTORICO = "http://localhost:8080/api/chat/historico";
  const chatForm = document.querySelector(".chat-input-area");
  const chatInput = chatForm ? chatForm.querySelector(".form-input") : null;
  const chatMessages = document.querySelector(".chat-messages");

  if (!chatForm || !chatInput || !chatMessages) return;

  function escaparHTML(texto) {
    const div = document.createElement("div");
    div.textContent = texto;
    return div.innerHTML;
  }

  function adicionarBolha(texto, horario, enviada) {
    const div = document.createElement("div");
    div.classList.add("message", enviada ? "sent" : "received");
    div.innerHTML = `<div class="message-bubble">${escaparHTML(texto)}</div><span class="message-time">${escaparHTML(horario)}</span>`;
    chatMessages.appendChild(div);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  fetch(API_HISTORICO)
    .then((res) => (res.ok ? res.json() : []))
    .then((lista) => {
      if (Array.isArray(lista) && lista.length > 0) {
        const sessao = JSON.parse(localStorage.getItem("alugamaquina_usuario") || "{}");
        lista.forEach((m) => {
          adicionarBolha(m.conteudo || "", m.horario || "", m.remetente === sessao.nome);
        });
      }
    })
    .catch(() => { /* Servidor offline: mantem conversa exibida na tela */ });

  chatForm.addEventListener("submit", function (event) {
    event.preventDefault();
    const mensagemTexto = chatInput.value.trim();
    if (!mensagemTexto) {
      chatInput.focus();
      return;
    }

    const agora = new Date();
    const horarioFormatado = String(agora.getHours()).padStart(2, "0") + ":" + String(agora.getMinutes()).padStart(2, "0");
    adicionarBolha(mensagemTexto, horarioFormatado, true);
    chatInput.value = "";
    chatInput.focus();
  });
});