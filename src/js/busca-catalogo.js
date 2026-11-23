/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Catalogo de maquinas integrado a API REST de busca geoespacial (GET /api/maquinas/busca),
             com filtros combinados de texto, distancia, categoria e valor
*/

document.addEventListener("DOMContentLoaded", function () {
  const busca = document.getElementById("busca");
  const raio = document.getElementById("raio");
  const categoria = document.getElementById("categoria");
  const valor = document.getElementById("valor");
  const limpar = document.getElementById("limpar");
  const grade = document.getElementById("grade");
  const contagem = document.getElementById("contagem");

  if (!grade) return;

  // Coordenadas de referencia da busca (Paulinia / SP), conforme o prototipo visual
  const API_BUSCA = "http://localhost:8080/api/maquinas/busca";
  const LAT_REFERENCIA = -22.7611;
  const LNG_REFERENCIA = -47.1544;
  const RAIO_MAXIMO_API = 500; // limite aceito pelo backend (MaquinaBuscaService)

  // Foto representativa por categoria dos equipamentos
  const IMAGEM_POR_CATEGORIA = {
    tratores: "../../assets/maquinas/trator-agricola.jpg",
    colheitadeiras: "../../assets/maquinas/colheitadeira-graos.jpg",
    pulverizadores: "../../assets/maquinas/pulverizador-autopropelido.jpg",
    implementos: "../../assets/maquinas/grade-aradora.jpg",
    plantadeiras: "../../assets/maquinas/plantadeira-agricola.jpg",
  };

  // Normaliza o tipoEquipamento vindo da API para as mesmas chaves do filtro #categoria
  const MAPA_CATEGORIA = {
    trator: "tratores",
    tratores: "tratores",
    colheitadeira: "colheitadeiras",
    colheitadeiras: "colheitadeiras",
    implemento: "implementos",
    implementos: "implementos",
    pulverizador: "pulverizadores",
    pulverizadores: "pulverizadores",
    plantadeira: "plantadeiras",
    plantadeiras: "plantadeiras",
  };

  let cards = [];
  let temporizadorDebounce = null;

  // Mensagem exibida quando nao ha cards (carregando, vazio ou servidor offline)
  const mensagemStatus = document.createElement("p");
  mensagemStatus.className = "empty-state";
  grade.insertAdjacentElement("afterend", mensagemStatus);
  mensagemStatus.hidden = true;

  // Botao de nova tentativa, usado so no estado de servidor offline
  const botaoTentarDeNovo = document.createElement("button");
  botaoTentarDeNovo.type = "button";
  botaoTentarDeNovo.className = "btn btn-secondary";
  botaoTentarDeNovo.textContent = "Tentar de novo";
  mensagemStatus.insertAdjacentElement("afterend", botaoTentarDeNovo);
  botaoTentarDeNovo.hidden = true;
  botaoTentarDeNovo.addEventListener("click", buscarMaquinas);

  function normalizarCategoria(tipoEquipamento) {
    if (!tipoEquipamento) return "";
    const chave = tipoEquipamento.trim().toLowerCase();
    return MAPA_CATEGORIA[chave] || chave;
  }

  function formatarValor(valorDiaria) {
    const numero = Number(valorDiaria) || 0;
    return numero.toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function formatarDistancia(distanciaKm) {
    const numero = Number(distanciaKm) || 0;
    return numero.toLocaleString("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 });
  }

  function montarUrlBusca(raioKm) {
    const parametros = new URLSearchParams({
      lat: LAT_REFERENCIA,
      lng: LNG_REFERENCIA,
      raioKm: raioKm,
    });
    return API_BUSCA + "?" + parametros.toString();
  }

  // Cria um card no mesmo formato visual usado no prototipo estatico
  function criarCard(maquina) {
    const categoriaNormalizada = normalizarCategoria(maquina.tipoEquipamento);
    const imagem = IMAGEM_POR_CATEGORIA[categoriaNormalizada] || "../../assets/maquinas/trator-agricola.jpg";

    const card = document.createElement("article");
    card.className = "machine-card";
    card.dataset.categoria = categoriaNormalizada;
    card.dataset.distancia = Math.round(Number(maquina.distanciaKm) || 0);
    card.dataset.preco = Math.round(Number(maquina.valorDiaria) || 0);

    const link = "detalhe-maquina.html?id=" + encodeURIComponent(maquina.id);

    card.innerHTML =
      '<div class="machine-photo"><img src="' + imagem + '" alt="Foto do equipamento agrícola" /></div>' +
      '<div class="machine-body">' +
      '<span class="verified-badge">Verificada</span>' +
      '<h2 class="machine-title"></h2>' +
      '<p class="machine-meta"></p>' +
      '<p class="machine-distance"></p>' +
      '<p class="machine-price"></p>' +
      '<a href="' + link + '" class="btn btn-primary btn-card">Ver detalhes</a>' +
      "</div>";

    const imgEl = card.querySelector(".machine-photo img");
    if (imgEl) {
      imgEl.alt = "Foto de " + (maquina.tituloAnuncio || "equipamento agrícola");
    }
    card.querySelector(".machine-title").textContent = maquina.tituloAnuncio || "Equipamento sem titulo";
    card.querySelector(".machine-meta").textContent = maquina.tipoEquipamento + " · " + maquina.municipio;
    card.querySelector(".machine-distance").textContent = "a " + formatarDistancia(maquina.distanciaKm) + " km de você";

    const precoEl = card.querySelector(".machine-price");
    precoEl.textContent = "R$ " + formatarValor(maquina.valorDiaria) + " ";
    const sufixo = document.createElement("span");
    sufixo.textContent = "/ dia";
    precoEl.appendChild(sufixo);

    return card;
  }

  function mostrarStatus(texto, comBotaoTentarDeNovo) {
    mensagemStatus.textContent = texto;
    mensagemStatus.hidden = false;
    botaoTentarDeNovo.hidden = !comBotaoTentarDeNovo;
  }

  function esconderStatus() {
    mensagemStatus.hidden = true;
    botaoTentarDeNovo.hidden = true;
  }

  function aplicarFiltros() {
    const termo = busca ? busca.value.trim().toLowerCase() : "";
    const distanciaMax = raio ? parseFloat(raio.value) : Infinity;
    const categoriaSelecionada = categoria ? categoria.value : "";
    const valorMax = valor ? parseFloat(valor.value) : Infinity;

    let visiveis = 0;

    cards.forEach(function (card) {
      const titulo = card.querySelector(".machine-title");
      const nomeMaquina = titulo ? titulo.textContent.toLowerCase() : "";
      const distanciaMaquina = parseFloat(card.dataset.distancia);
      const categoriaMaquina = card.dataset.categoria;
      const precoMaquina = parseFloat(card.dataset.preco);

      const correspondeBusca = termo === "" || nomeMaquina.includes(termo);
      const correspondeDistancia = distanciaMaquina <= distanciaMax;
      const correspondeCategoria = categoriaSelecionada === "" || categoriaMaquina === categoriaSelecionada;
      const correspondeValor = precoMaquina <= valorMax;

      const exibir = correspondeBusca && correspondeDistancia && correspondeCategoria && correspondeValor;
      card.hidden = !exibir;

      if (exibir) visiveis++;
    });

    if (contagem) {
      contagem.textContent = visiveis + (visiveis === 1 ? " máquina encontrada" : " máquinas encontradas");
    }

    if (visiveis === 0) {
      const mensagem = cards.length === 0
        ? "Nenhuma máquina encontrada."
        : "Nenhuma máquina encontrada para os filtros selecionados.";
      mostrarStatus(mensagem, false);
    } else {
      esconderStatus();
    }
  }

  function renderizarMaquinas(maquinas) {
    grade.innerHTML = "";
    maquinas.forEach(function (maquina) {
      grade.appendChild(criarCard(maquina));
    });
    cards = Array.from(grade.querySelectorAll(".machine-card"));
    aplicarFiltros();
  }

  // Busca as maquinas na API Java (lat/lng fixos + raio escolhido no filtro) e renderiza os cards
  function buscarMaquinas() {
    const raioSelecionado = raio ? parseFloat(raio.value) : RAIO_MAXIMO_API;
    const raioEnviado = Math.min(raioSelecionado > 0 ? raioSelecionado : RAIO_MAXIMO_API, RAIO_MAXIMO_API);

    grade.innerHTML = "";
    cards = [];
    if (contagem) contagem.textContent = "Carregando máquinas...";
    mostrarStatus("Carregando máquinas...", false);

    return fetch(montarUrlBusca(raioEnviado))
      .then(function (resposta) {
        if (!resposta.ok) throw new Error("Resposta invalida da API: " + resposta.status);
        return resposta.json();
      })
      .then(function (maquinas) {
        renderizarMaquinas(Array.isArray(maquinas) ? maquinas : []);
      })
      .catch(function () {
        // Servidor Java offline ou MongoDB indisponivel: nao sobra nenhum card fixo
        grade.innerHTML = "";
        cards = [];
        if (contagem) contagem.textContent = "0 máquinas encontradas";
        mostrarStatus("Não foi possível carregar o catálogo agora. Verifique se o servidor está online.", true);
      });
  }

  function comDebounce(funcao, atrasoMs) {
    return function () {
      clearTimeout(temporizadorDebounce);
      temporizadorDebounce = setTimeout(funcao, atrasoMs);
    };
  }

  function limparFiltros() {
    if (busca) busca.value = "";
    if (raio) raio.value = "9999";
    if (categoria) categoria.value = "";
    if (valor) valor.value = "9999999";
    buscarMaquinas();
  }

  if (busca) busca.addEventListener("input", comDebounce(aplicarFiltros, 300));
  if (raio) raio.addEventListener("change", buscarMaquinas);
  if (categoria) categoria.addEventListener("change", aplicarFiltros);
  if (valor) valor.addEventListener("change", aplicarFiltros);
  if (limpar) limpar.addEventListener("click", limparFiltros);

  buscarMaquinas();
});
