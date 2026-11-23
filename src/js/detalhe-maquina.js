/*
  Autor: Mateus Oliveira Rafael | 25001369
  Descricao: Tela de detalhe da maquina integrada a API REST (GET /api/maquinas/{id}).
             Preenche titulo, preco, tipo, localizacao e o data-diaria/data-origem do
             card de locacao antes de chamar window.AlugaMaquinaFrete.iniciar() (frete.js).
*/

document.addEventListener("DOMContentLoaded", function () {
  const statusMaquina = document.getElementById("statusMaquina");
  const btnTentarDeNovo = document.getElementById("btnTentarDeNovoMaquina");
  const introMaquina = document.getElementById("introMaquina");
  const layoutMaquina = document.getElementById("layoutMaquina");
  const breadcrumbCategoria = document.getElementById("breadcrumbCategoria");
  const breadcrumbTitulo = document.getElementById("breadcrumbTitulo");
  const tituloMaquina = document.getElementById("tituloMaquina");
  const localizacaoMaquina = document.getElementById("localizacaoMaquina");
  const specTipo = document.getElementById("specTipo");
  const specLocalizacao = document.getElementById("specLocalizacao");
  const precoMaquina = document.getElementById("precoMaquina");
  const fotoMaquina = document.getElementById("fotoMaquina");
  const cardLocacao = document.getElementById("cardLocacao");

  if (!statusMaquina || !cardLocacao) return;

  const API_MAQUINAS = "http://localhost:8080/api/maquinas/";

  const IMAGEM_POR_CATEGORIA = {
    trator: "../../assets/maquinas/trator-agricola.jpg",
    tratores: "../../assets/maquinas/trator-agricola.jpg",
    colheitadeira: "../../assets/maquinas/colheitadeira-graos.jpg",
    colheitadeiras: "../../assets/maquinas/colheitadeira-graos.jpg",
    implemento: "../../assets/maquinas/grade-aradora.jpg",
    implementos: "../../assets/maquinas/grade-aradora.jpg",
    pulverizador: "../../assets/maquinas/pulverizador-autopropelido.jpg",
    pulverizadores: "../../assets/maquinas/pulverizador-autopropelido.jpg",
    plantadeira: "../../assets/maquinas/plantadeira-agricola.jpg",
    plantadeiras: "../../assets/maquinas/plantadeira-agricola.jpg",
  };

  // Rotulo de exibicao por categoria, para o breadcrumb
  const LABEL_POR_CATEGORIA = {
    trator: "Tratores",
    tratores: "Tratores",
    colheitadeira: "Colheitadeiras",
    colheitadeiras: "Colheitadeiras",
    implemento: "Implementos",
    implementos: "Implementos",
    pulverizador: "Pulverizadores",
    pulverizadores: "Pulverizadores",
    plantadeira: "Plantadeiras",
    plantadeiras: "Plantadeiras",
  };

  // Nome de exibicao do municipio (API) -> codigo usado pelo frete.js e pelo select de cidade
  const CODIGO_POR_MUNICIPIO = {
    "Paulínia / SP": "paulinia",
    "Campinas / SP": "campinas",
    "Cosmópolis / SP": "cosmopolis",
    "Sumaré / SP": "sumare",
    "Holambra / SP": "holambra",
    "Jaguariúna / SP": "jaguariuna",
    "Artur Nogueira / SP": "artur-nogueira",
  };

  function obterIdDaUrl() {
    const parametros = new URLSearchParams(window.location.search);
    return parametros.get("id");
  }

  function formatarValor(valorDiaria) {
    const numero = Number(valorDiaria) || 0;
    return numero.toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function mostrarStatus(texto, comBotaoTentarDeNovo) {
    statusMaquina.textContent = texto;
    statusMaquina.hidden = false;
    btnTentarDeNovo.hidden = !comBotaoTentarDeNovo;
    introMaquina.hidden = true;
    layoutMaquina.hidden = true;
  }

  function esconderStatus() {
    statusMaquina.hidden = true;
    btnTentarDeNovo.hidden = true;
  }

  function preencherMaquina(maquina) {
    const categoriaChave = (maquina.tipoEquipamento || "").trim().toLowerCase();
    const categoriaLabel = LABEL_POR_CATEGORIA[categoriaChave] || "Categoria";
    const codigoMunicipio = CODIGO_POR_MUNICIPIO[maquina.municipio] || "";

    breadcrumbCategoria.textContent = categoriaLabel;
    breadcrumbTitulo.textContent = maquina.tituloAnuncio || "Equipamento sem titulo";
    tituloMaquina.textContent = maquina.tituloAnuncio || "Equipamento sem titulo";
    localizacaoMaquina.textContent = maquina.municipio || "Não informado";
    specTipo.textContent = maquina.tipoEquipamento || "Não informado";
    specLocalizacao.textContent = maquina.municipio || "Não informado";
    precoMaquina.textContent = "R$ " + formatarValor(maquina.valorDiaria);

    if (fotoMaquina) {
      fotoMaquina.src = IMAGEM_POR_CATEGORIA[categoriaChave] || "../../assets/maquinas/trator-agricola.jpg";
      fotoMaquina.alt = "Foto de " + (maquina.tituloAnuncio || "equipamento agrícola");
      fotoMaquina.hidden = false;
    }

    cardLocacao.dataset.diaria = Number(maquina.valorDiaria) || 0;
    cardLocacao.dataset.origem = codigoMunicipio;

    esconderStatus();
    introMaquina.hidden = false;
    layoutMaquina.hidden = false;

    // So agora os data-diaria/data-origem estao corretos: o frete.js pode ler o card
    if (window.AlugaMaquinaFrete) {
      window.AlugaMaquinaFrete.iniciar();
    }
  }

  function carregarMaquina() {
    const id = obterIdDaUrl();

    if (!id) {
      mostrarStatus("Máquina não encontrada.", false);
      return;
    }

    mostrarStatus("Carregando dados da máquina...", false);

    fetch(API_MAQUINAS + encodeURIComponent(id))
      .then(function (resposta) {
        if (resposta.status === 404) {
          mostrarStatus("Máquina não encontrada.", false);
          return null;
        }
        if (!resposta.ok) throw new Error("Resposta invalida da API: " + resposta.status);
        return resposta.json();
      })
      .then(function (maquina) {
        if (maquina) preencherMaquina(maquina);
      })
      .catch(function () {
        // Servidor Java offline ou MongoDB indisponivel
        mostrarStatus("Não foi possível carregar a máquina agora. Verifique se o servidor está online.", true);
      });
  }

  if (btnTentarDeNovo) btnTentarDeNovo.addEventListener("click", carregarMaquina);

  carregarMaquina();
});
