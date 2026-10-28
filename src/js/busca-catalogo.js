/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Filtros interativos do catalogo de maquinas (busca, distancia, categoria e valor)
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

  const cards = Array.from(grade.querySelectorAll(".machine-card"));

  // Mensagem exibida quando nenhum card atende aos filtros
  const mensagemVazia = document.createElement("p");
  mensagemVazia.className = "empty-state";
  mensagemVazia.textContent = "Nenhuma máquina encontrada para os filtros selecionados.";
  grade.insertAdjacentElement("afterend", mensagemVazia);
  mensagemVazia.hidden = true;

  function aplicarFiltros() {
    const termo = busca ? busca.value.trim().toLowerCase() : "";
    const distanciaMax = raio ? parseInt(raio.value, 10) : Infinity;
    const categoriaSelecionada = categoria ? categoria.value : "";
    const valorMax = valor ? parseInt(valor.value, 10) : Infinity;

    let visiveis = 0;

    cards.forEach(function (card) {
      const titulo = card.querySelector(".machine-title");
      const nomeMaquina = titulo ? titulo.textContent.toLowerCase() : "";
      const distanciaMaquina = parseInt(card.dataset.distancia, 10);
      const categoriaMaquina = card.dataset.categoria;
      const precoMaquina = parseInt(card.dataset.preco, 10);

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

    mensagemVazia.hidden = visiveis !== 0;
  }

  function limparFiltros() {
    if (busca) busca.value = "";
    if (raio) raio.value = "9999";
    if (categoria) categoria.value = "";
    if (valor) valor.value = "9999999";
    aplicarFiltros();
  }

  if (busca) busca.addEventListener("input", aplicarFiltros);
  if (raio) raio.addEventListener("change", aplicarFiltros);
  if (categoria) categoria.addEventListener("change", aplicarFiltros);
  if (valor) valor.addEventListener("change", aplicarFiltros);
  if (limpar) limpar.addEventListener("click", limparFiltros);

  aplicarFiltros();
});
