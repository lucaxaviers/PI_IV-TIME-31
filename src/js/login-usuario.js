/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Integracao real das telas de login e cadastro com a API REST (/api/auth/login e /api/auth/cadastro)
*/

document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = "http://localhost:8080/api/auth";

  const formLogin = document.querySelector("#abaLogin form");
  const formCadastro = document.querySelector("#abaCadastro form");

  function exibirFeedback(form, mensagem, sucesso) {
    let caixa = form.querySelector(".auth-feedback");
    if (!caixa) {
      caixa = document.createElement("p");
      caixa.className = "auth-feedback";
      caixa.style.padding = "10px 14px";
      caixa.style.borderRadius = "8px";
      caixa.style.marginBottom = "12px";
      caixa.style.fontSize = "0.95rem";
      form.prepend(caixa);
    }
    caixa.textContent = mensagem;
    caixa.style.backgroundColor = sucesso ? "#e8f5e9" : "#fdecea";
    caixa.style.color = sucesso ? "#1b5e20" : "#b71c1c";
    caixa.style.border = sucesso ? "1px solid #a5d6a7" : "1px solid #f5c6cb";
  }

  function salvarSessao(dados) {
    localStorage.setItem("alugamaquina_usuario", JSON.stringify({
      usuarioId: dados.usuarioId,
      nome: dados.nome,
      email: dados.email,
      perfil: dados.perfil
    }));
  }

  if (formLogin) {
    formLogin.addEventListener("submit", async function (event) {
      event.preventDefault();
      const identificador = document.getElementById("loginIdentificador").value.trim();
      const senha = document.getElementById("loginSenha").value;
      const botao = formLogin.querySelector('button[type="submit"]');

      botao.disabled = true;
      try {
        const resposta = await fetch(`${API_BASE}/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ identificador, senha })
        });
        const dados = await resposta.json();

        if (!resposta.ok) {
          exibirFeedback(formLogin, dados.erro || "Falha ao autenticar.", false);
          return;
        }

        salvarSessao(dados);
        exibirFeedback(formLogin, `Bem-vindo, ${dados.nome}! Redirecionando...`, true);
        setTimeout(() => { window.location.href = "busca-catalogo.html"; }, 900);
      } catch (erro) {
        exibirFeedback(formLogin, "Não foi possível conectar ao servidor (localhost:8080).", false);
      } finally {
        botao.disabled = false;
      }
    });
  }

  if (formCadastro) {
    formCadastro.addEventListener("submit", async function (event) {
      event.preventDefault();
      const senha = document.getElementById("cadSenha").value;
      const confirmaSenha = document.getElementById("cadConfirmaSenha").value;
      if (senha !== confirmaSenha) {
        exibirFeedback(formCadastro, "As senhas informadas não coincidem.", false);
        return;
      }

      const perfilSelecionado = formCadastro.querySelector('input[name="perfilUsuario"]:checked');
      const payload = {
        nome: document.getElementById("cadNome").value.trim(),
        documento: document.getElementById("cadDocumento").value.trim(),
        telefone: document.getElementById("cadTelefone").value.trim(),
        email: document.getElementById("cadEmail").value.trim(),
        municipio: document.getElementById("cadMunicipio").value,
        perfil: perfilSelecionado ? perfilSelecionado.value : "locatario",
        senha: senha
      };

      const botao = formCadastro.querySelector('button[type="submit"]');
      botao.disabled = true;
      try {
        const resposta = await fetch(`${API_BASE}/cadastro`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const dados = await resposta.json();

        if (!resposta.ok) {
          exibirFeedback(formCadastro, dados.erro || "Erro ao realizar cadastro.", false);
          return;
        }

        salvarSessao(dados);
        exibirFeedback(formCadastro, `Cadastro concluído para ${dados.nome}! Redirecionando...`, true);
        setTimeout(() => { window.location.href = "busca-catalogo.html"; }, 900);
      } catch (erro) {
        exibirFeedback(formCadastro, "Não foi possível conectar ao servidor (localhost:8080).", false);
      } finally {
        botao.disabled = false;
      }
    });
  }
});
