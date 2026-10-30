/*
  Autor: Leonardo Gambaroni Alves | 25003494
  Descrição: Validação e interatividade simples do formulário de chat
*/

document.addEventListener("DOMContentLoaded", function () {
  const chatForm = document.querySelector(".chat-input-area");
  const chatInput = chatForm ? chatForm.querySelector(".form-input") : null;
  const chatMessages = document.querySelector(".chat-messages");

  if (!chatForm || !chatInput || !chatMessages) return;

  chatForm.addEventListener("submit", function (event) {
    // Impede o envio padrão da página para simular o chat dinamicamente
    event.preventDefault();

    const mensagemTexto = chatInput.value.trim();

    // 1. Validação básica: se estiver vazio, alerta o usuário
    if (!mensagemTexto) {
      alert("Por favor, digite uma mensagem antes de enviar.");
      chatInput.focus();
      return;
    }

    // 2. Descobre a hora atual formatada (ex: 10:25)
    const agora = new Date();
    const horas = String(agora.getHours()).padStart(2, "0");
    const minutos = String(agora.getMinutes()).padStart(2, "0");
    const horarioFormatado = `${horas}:${minutos}`;

    // 3. Cria a estrutura HTML da mensagem enviada (balão verde)
    const novaMensagemDiv = document.createElement("div");
    novaMensagemDiv.classList.add("message", "sent");

    novaMensagemDiv.innerHTML = `
      <div class="message-bubble">
        ${escaparHTML(mensagemTexto)}
      </div>
      <span class="message-time">${horarioFormatado}</span>
    `;

    // 4. Adiciona a mensagem na área de chat
    chatMessages.appendChild(novaMensagemDiv);

    // 5. Limpa o input e rola o chat para o final
    chatInput.value = "";
    chatMessages.scrollTop = chatMessages.scrollHeight;

    // (Opcional) Simula uma resposta automática do locador após 2 segundos
    setTimeout(function () {
      simularRespostaLocador(chatMessages);
    }, 2000);
  });
});

/**
 * Função auxiliar simples para evitar injeção de texto malicioso (segurança básica)
 */
function escaparHTML(texto) {
  const div = document.createElement("div");
  div.textContent = texto;
  return div.innerHTML;
}

/**
 * Função opcional para simular o locador respondendo no chat
 */
function simularRespostaLocador(containerMensagens) {
  const agora = new Date();
  const horario = `${String(agora.getHours()).padStart(2, '0')}:${String(agora.getMinutes()).padStart(2, '0')}`;

  const respostaDiv = document.createElement("div");
  respostaDiv.classList.add("message", "received");
  respostaDiv.innerHTML = `
    <div class="message-bubble">
      Recebi sua mensagem! Vou verificar aqui na fazenda e já te retorno com os detalhes.
    </div>
    <span class="message-time">${horario}</span>
  `;

  containerMensagens.appendChild(respostaDiv);
  containerMensagens.scrollTop = containerMensagens.scrollHeight;
}