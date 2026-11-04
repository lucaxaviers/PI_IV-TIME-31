/*
  Autor: Tiago Medeiros | 25000845
  Descricao: Calculo de frete estimado e valor total da locacao

  Regra do calculo:
    frete = (distancia_km x 2 x VALOR_POR_KM) + TAXA_MOBILIZACAO
    (ida e volta, com distancia minima para cidades iguais)

  As distancias abaixo sao APROXIMADAS (rodoviarias, em km) entre os municipios
  disponiveis no cadastro de maquinas. Servem apenas para estimativa.
*/

(function () {
  'use strict';

  // ---- Parametros do frete (ajuste conforme a regra de negocio) ----
  var VALOR_POR_KM = 6.5;       // R$ por km rodado
  var TAXA_MOBILIZACAO = 150;   // R$ fixo de carga/descarga
  var DISTANCIA_MINIMA_KM = 10; // usada quando origem e destino sao a mesma cidade

  // ---- Distancias aproximadas entre municipios (km) ----
  var DISTANCIAS = {
    'paulinia':       { 'campinas': 25, 'cosmopolis': 18, 'sumare': 20, 'holambra': 25, 'jaguariuna': 22, 'artur-nogueira': 30 },
    'campinas':       { 'paulinia': 25, 'cosmopolis': 40, 'sumare': 20, 'holambra': 48, 'jaguariuna': 38, 'artur-nogueira': 52 },
    'cosmopolis':     { 'paulinia': 18, 'campinas': 40, 'sumare': 38, 'holambra': 12, 'jaguariuna': 28, 'artur-nogueira': 15 },
    'sumare':         { 'paulinia': 20, 'campinas': 20, 'cosmopolis': 38, 'holambra': 45, 'jaguariuna': 45, 'artur-nogueira': 40 },
    'holambra':       { 'paulinia': 25, 'campinas': 48, 'cosmopolis': 12, 'sumare': 45, 'jaguariuna': 20, 'artur-nogueira': 12 },
    'jaguariuna':     { 'paulinia': 22, 'campinas': 38, 'cosmopolis': 28, 'sumare': 45, 'holambra': 20, 'artur-nogueira': 25 },
    'artur-nogueira': { 'paulinia': 30, 'campinas': 52, 'cosmopolis': 15, 'sumare': 40, 'holambra': 12, 'jaguariuna': 25 }
  };

  /** Distancia estimada em km entre duas cidades (ou null se desconhecida). */
  function obterDistancia(origem, destino) {
    if (!origem || !destino) return null;
    if (origem === destino) return DISTANCIA_MINIMA_KM;
    if (DISTANCIAS[origem] && DISTANCIAS[origem][destino] !== undefined) {
      return DISTANCIAS[origem][destino];
    }
    return null;
  }

  /** Frete estimado (R$) para uma distancia em km. Ida e volta + taxa fixa. */
  function calcularFrete(distanciaKm) {
    return distanciaKm * 2 * VALOR_POR_KM + TAXA_MOBILIZACAO;
  }

  /** Numero de diarias entre duas datas (inclusive). Retorna 0 se invalido. */
  function calcularDias(inicio, fim) {
    if (!inicio || !fim) return 0;
    var d1 = new Date(inicio + 'T00:00:00');
    var d2 = new Date(fim + 'T00:00:00');
    if (isNaN(d1) || isNaN(d2) || d2 < d1) return 0;
    return Math.round((d2 - d1) / 86400000) + 1;
  }

  function formatarMoeda(valor) {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  // ---- Ligacao com a tela ----
  var card = document.getElementById('cardLocacao');
  var form = document.getElementById('formFrete');
  if (!card || !form) return;

  var origem = card.dataset.origem;
  var diaria = parseFloat(card.dataset.diaria);
  var inicioMin = card.dataset.inicioMin;
  var fimMax = card.dataset.fimMax;

  var campoInicio = document.getElementById('dataInicio');
  var campoFim = document.getElementById('dataFim');
  var campoCidade = document.getElementById('municipioEntrega');
  var campoLogistica = document.getElementById('tipoLogistica');
  var erro = document.getElementById('mensagemErro');
  var resumo = document.getElementById('resumoCalculo');

  // Limita o calendario ao periodo de disponibilidade da maquina
  campoInicio.min = campoFim.min = inicioMin;
  campoInicio.max = campoFim.max = fimMax;

  function mostrarErro(msg) {
    erro.textContent = msg;
    erro.hidden = false;
    resumo.hidden = true;
  }

  function limparErro() {
    erro.hidden = true;
    erro.textContent = '';
  }

  function calcular(evento) {
    if (evento) evento.preventDefault();
    limparErro();

    var inicio = campoInicio.value;
    var fim = campoFim.value;
    var retirada = campoLogistica.value === 'retirada';

    if (!inicio || !fim) {
      return mostrarErro('Informe as datas de início e término da locação.');
    }
    if (inicio < inicioMin || fim > fimMax) {
      return mostrarErro('As datas devem estar dentro do período de disponibilidade da máquina.');
    }
    var dias = calcularDias(inicio, fim);
    if (dias === 0) {
      return mostrarErro('A data de término deve ser igual ou posterior à data de início.');
    }

    var distancia = 0;
    var frete = 0;

    if (!retirada) {
      if (!campoCidade.value) {
        return mostrarErro('Selecione a cidade de entrega para estimar o frete.');
      }
      distancia = obterDistancia(origem, campoCidade.value);
      if (distancia === null) {
        return mostrarErro('Não foi possível estimar a distância para essa cidade.');
      }
      frete = calcularFrete(distancia);
    }

    var subtotal = dias * diaria;

    document.getElementById('resDias').textContent = dias + (dias === 1 ? ' diária' : ' diárias');
    document.getElementById('resSubtotal').textContent = formatarMoeda(subtotal);
    document.getElementById('resDistancia').textContent = retirada ? '—' : distancia + ' km';
    document.getElementById('resFrete').textContent = retirada ? 'Sem frete' : formatarMoeda(frete);
    document.getElementById('resTotal').textContent = formatarMoeda(subtotal + frete);
    resumo.hidden = false;
  }

  // Cidade de entrega so e obrigatoria quando ha frete
  campoLogistica.addEventListener('change', function () {
    var retirada = campoLogistica.value === 'retirada';
    campoCidade.disabled = retirada;
    campoCidade.required = !retirada;
    if (retirada) campoCidade.value = '';
  });

  // Garante que o termino nunca fique antes do inicio
  campoInicio.addEventListener('change', function () {
    if (campoInicio.value) campoFim.min = campoInicio.value;
  });

  form.addEventListener('submit', calcular);
})();