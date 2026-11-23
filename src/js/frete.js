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
  // Nao roda sozinho: so depois que o detalhe-maquina.js preenche data-diaria/data-origem
  // com a resposta da API (GET /api/maquinas/{id}) e chama window.AlugaMaquinaFrete.iniciar()
  var API_LOCACOES = 'http://localhost:8080/api/locacoes';
  var card, form, origem, diaria, inicioMin, fimMax;
  var campoInicio, campoFim, campoCidade, campoLogistica, erro, resumo;
  var btnSolicitar, feedbackLocacao;
  var eventosLigados = false;

  function mostrarErro(msg) {
    erro.textContent = msg;
    erro.hidden = false;
    resumo.hidden = true;
    return null;
  }

  function limparErro() {
    erro.hidden = true;
    erro.textContent = '';
  }

  function mostrarFeedbackLocacao(msg, sucesso) {
    if (!feedbackLocacao) return;
    feedbackLocacao.textContent = msg;
    feedbackLocacao.className = 'form-feedback ' + (sucesso ? 'sucesso' : 'erro');
    feedbackLocacao.hidden = false;
  }

  function limparFeedbackLocacao() {
    if (!feedbackLocacao) return;
    feedbackLocacao.hidden = true;
    feedbackLocacao.textContent = '';
    feedbackLocacao.className = 'form-feedback';
  }

  function lerUsuarioLogado() {
    try {
      return JSON.parse(localStorage.getItem('alugamaquina_usuario') || '{}');
    } catch (e) {
      return {};
    }
  }

  function resolverLocadorId(maquinaId, locatarioId) {
    var locadorApi = card && card.dataset ? (card.dataset.locadorId || '').trim() : '';
    var locador = locadorApi || ((maquinaId === 'maq-002' || maquinaId === 'maq-004') ? 'usr-ambos-01' : 'usr-locador-01');
    if (locador === locatarioId) {
      locador = locador === 'usr-locador-01' ? 'usr-ambos-01' : 'usr-locador-01';
    }
    return locador;
  }

  function calcular(evento) {
    if (evento) evento.preventDefault();
    limparErro();
    limparFeedbackLocacao();

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

    return {
      inicio: inicio,
      fim: fim,
      dias: dias,
      distancia: distancia,
      frete: frete,
      subtotal: subtotal,
      total: subtotal + frete
    };
  }

  async function solicitarReserva() {
    limparFeedbackLocacao();

    var usuario = lerUsuarioLogado();
    if (!usuario || !usuario.usuarioId) {
      mostrarFeedbackLocacao('Faça login para solicitar a reserva e gerar o contrato digital.', false);
      return;
    }

    var maquinaId = card && card.dataset ? (card.dataset.maquinaId || '').trim() : '';
    if (!maquinaId) {
      mostrarFeedbackLocacao('Não foi possível identificar a máquina selecionada.', false);
      return;
    }

    var dados = calcular();
    if (!dados) {
      return;
    }

    var textoOriginal = btnSolicitar ? btnSolicitar.textContent : 'Solicitar Reserva & Contrato';
    if (btnSolicitar) {
      btnSolicitar.disabled = true;
      btnSolicitar.textContent = 'Solicitando reserva...';
    }

    try {
      var respSolicitar = await fetch(API_LOCACOES + '/solicitar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          locadorId: resolverLocadorId(maquinaId, usuario.usuarioId),
          locatarioId: usuario.usuarioId,
          maquinaId: maquinaId,
          dataInicio: dados.inicio,
          dataFim: dados.fim,
          valorDiaria: diaria,
          distanciaKm: dados.distancia
        })
      });
      var locacao = await respSolicitar.json().catch(function () { return {}; });
      if (!respSolicitar.ok) {
        throw new Error(locacao.erro || 'Não foi possível enviar a solicitação de reserva.');
      }

      // Confirma a reserva como ACEITO para liberar a emissao do contrato e a vistoria
      var respAceitar = await fetch(API_LOCACOES + '/' + encodeURIComponent(locacao.id) + '/aceitar', {
        method: 'PUT'
      });
      var aceita = await respAceitar.json().catch(function () { return {}; });
      if (!respAceitar.ok) {
        throw new Error(aceita.erro || 'Não foi possível confirmar a reserva para gerar o contrato.');
      }

      mostrarFeedbackLocacao('Reserva confirmada! Abrindo contrato digital...', true);
      window.location.href = 'contrato-digital.html?locacaoId=' + encodeURIComponent(locacao.id);
    } catch (e) {
      mostrarFeedbackLocacao(e.message || 'Servidor offline no momento.', false);
      if (btnSolicitar) {
        btnSolicitar.disabled = false;
        btnSolicitar.textContent = textoOriginal;
      }
    }
  }

  // Le data-diaria/data-origem do card e liga os eventos do formulario.
  // Chamada pelo detalhe-maquina.js assim que a API devolve a maquina.
  function iniciar() {
    card = document.getElementById('cardLocacao');
    form = document.getElementById('formFrete');
    if (!card || !form) return;

    origem = card.dataset.origem;
    diaria = parseFloat(card.dataset.diaria);
    inicioMin = card.dataset.inicioMin;
    fimMax = card.dataset.fimMax;

    campoInicio = document.getElementById('dataInicio');
    campoFim = document.getElementById('dataFim');
    campoCidade = document.getElementById('municipioEntrega');
    campoLogistica = document.getElementById('tipoLogistica');
    erro = document.getElementById('mensagemErro');
    resumo = document.getElementById('resumoCalculo');
    btnSolicitar = document.getElementById('btnSolicitarLocacao');
    feedbackLocacao = document.getElementById('feedbackLocacao');

    // Limita o calendario ao periodo de disponibilidade da maquina
    campoInicio.min = campoFim.min = inicioMin;
    campoInicio.max = campoFim.max = fimMax;

    if (!eventosLigados) {
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
      if (btnSolicitar) {
        btnSolicitar.addEventListener('click', solicitarReserva);
      }
      eventosLigados = true;
    }
  }

  window.AlugaMaquinaFrete = { iniciar: iniciar };
})();