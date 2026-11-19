/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Bloqueio simples de paginas privadas verificando se existe usuario logado no localStorage
*/

(function () {
  var paginasPrivadas = [
    "cadastro-maquina.html",
    "chat.html",
    "avaliacao-pos-locacao.html",
    "contrato-digital.html",
    // Kauan Aurélio Lasmar Dias | 25001590 - pagina de vistoria tecnica, so para quem esta logado
    "vistoria-checklist.html"
  ];

  var paginaAtual = window.location.pathname.split("/").pop();
  var usuarioSalvo = localStorage.getItem("alugamaquina_usuario");

  // Se for pagina privada e nao estiver logado, manda direto para o login com aviso na tela
  if (paginasPrivadas.indexOf(paginaAtual) !== -1 && !usuarioSalvo) {
    window.location.href = "login-usuario.html?aviso=login";
    return;
  }

  // Atualiza o menu com "Sair" (se logado) ou "Entrar" (se deslogado)
  document.addEventListener("DOMContentLoaded", function () {
    var menu = document.querySelector(".nav-menu");
    if (!menu) return;

    var jaTemLogin = menu.querySelector('a[href="login-usuario.html"]');

    if (usuarioSalvo) {
      var usuario = JSON.parse(usuarioSalvo);
      if (jaTemLogin) {
        jaTemLogin.parentElement.remove();
      }

      var itemSair = document.createElement("li");
      var linkSair = document.createElement("a");
      linkSair.href = "#";
      linkSair.className = "nav-link";
      linkSair.textContent = "Sair (" + (usuario.nome ? usuario.nome.split(" ")[0] : "Conta") + ")";
      linkSair.addEventListener("click", function (e) {
        e.preventDefault();
        localStorage.removeItem("alugamaquina_usuario");
        window.location.href = "login-usuario.html";
      });

      itemSair.appendChild(linkSair);
      menu.appendChild(itemSair);
    } else if (!jaTemLogin) {
      var itemEntrar = document.createElement("li");
      var linkEntrar = document.createElement("a");
      linkEntrar.href = "login-usuario.html";
      linkEntrar.className = "nav-link";
      linkEntrar.textContent = "Entrar";

      itemEntrar.appendChild(linkEntrar);
      menu.appendChild(itemEntrar);
    }
  });
})();
