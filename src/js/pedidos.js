/*
  Autor: Tiago Medeiros | 25000845
  Descricao: Fluxo de solicitacao e aceite do pedido de locacao

  Os pedidos ficam salvos no localStorage do navegador (simulacao do fluxo).
  Quando o backend estiver pronto, basta trocar lerPedidos() e salvarPedidos()
  por chamadas a API.

  Status do pedido: 'pendente' -> 'aceito' ou 'recusado'
*/

(function () {
  'use strict';

  var CHAVE = 'alugamaquina_pedidos';

  var CIDADES = {
    'paulinia': 'Paulínia / SP',
    'campinas': 'Campinas / SP',
    'cosmopolis': 'Cosmópolis / SP',
    'sumare': 'Sumaré / SP',
    'holambra': 'Holambra / SP',
    'jaguariuna': 'Jaguariúna / SP',
    'artur-nogueira': 'Artur Nogueira / SP'
  };

  var STATUS = {
    'pendente': 'Aguardando resposta',
    'aceito': 'Aceito',
    'recusado': 'Recusado'
  };

  // ---------- Utilitarios ----------
  function lerPedidos() {
    try {
      var bruto = localStorage.getItem(CHAVE);
      return bruto ? JSON.parse(bruto) : [];
    } catch (e) {
      return [];
    }
  }

  function salvarPedidos(lista) {
    try {
      localStorage.setItem(CHAVE, JSON.stringify(lista));
    } catch (e) {
      // sem armazenamento disponivel: o fluxo continua, mas nao persiste
    }
  }

  function formatarMoeda(valor) {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  function formatarData(iso) {
    var partes = iso.split('-');
    return partes[2] + '/' + partes[1] + '/' + partes[0];
  }

  function calcularDias(inicio, fim) {
    if (!inicio || !fim) return 0;
    var d1 = new Date(inicio + 'T00:00:00');
    var d2 = new Date(fim + 'T00:00:00');
    if (isNaN(d1) || isNaN(d2) || d2 < d1) return 0;
    return Math.round((d2 - d1) / 86400000) + 1;
  }

  function gerarProtocolo() {
    return 'PED-' + String(Date.now()).slice(-6);
  }

  function criar(tag, classe, texto) {
    var no = document.createElement(tag);
    if (classe) no.className = classe;
    if (texto !== undefined) no.textContent = texto;
    return no;
  }

  // ---------- Tela: solicitar locacao ----------
  function iniciarSolicitacao() {
    var form = document.getElementById('formSolicitacao');
    if (!form) return;

    var maquina = form.dataset.maquina;
    var origem = form.dataset.origem;
    var diaria = parseFloat(form.dataset.diaria);
    var inicioMin = form.dataset.inicioMin;
    var fimMax = form.dataset.fimMax;

    var campoInicio = document.getElementById('dataInicio');
    var campoFim = document.getElementById('dataFim');
    var campoCidade = document.getElementById('cidade');
    var campoLogistica = document.getElementById('logistica');
    var erro = document.getElementById('mensagemErro');

    campoInicio.min = campoFim.min = inicioMin;
    campoInicio.max = campoFim.max = fimMax;

    function atualizarResumo() {
      var dias = calcularDias(campoInicio.value, campoFim.value);
      document.getElementById('resDias').textContent = dias;
      document.getElementById('resSubtotal').textContent = formatarMoeda(dias * diaria);
    }

    function mostrarErro(msg) {
      erro.textContent = msg;
      erro.hidden = false;
    }

    campoInicio.addEventListener('change', function () {
      if (campoInicio.value) campoFim.min = campoInicio.value;
      atualizarResumo();
    });
    campoFim.addEventListener('change', atualizarResumo);

    campoLogistica.addEventListener('change', function () {
      var retirada = campoLogistica.value === 'retirada';
      campoCidade.disabled = retirada;
      campoCidade.required = !retirada;
      if (retirada) campoCidade.value = '';
    });

    form.addEventListener('reset', function () {
      erro.hidden = true;
      campoCidade.disabled = false;
      setTimeout(atualizarResumo, 0);
    });

    form.addEventListener('submit', function (evento) {
      evento.preventDefault();
      erro.hidden = true;

      var nome = form.nome.value.trim();
      var telefone = form.telefone.value.trim();
      var inicio = campoInicio.value;
      var fim = campoFim.value;
      var retirada = campoLogistica.value === 'retirada';

      if (nome.length < 3) {
        return mostrarErro('Informe seu nome completo.');
      }
      if (telefone.replace(/\D/g, '').length < 10) {
        return mostrarErro('Informe um telefone válido com DDD.');
      }
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
      if (!retirada && !campoCidade.value) {
        return mostrarErro('Selecione a cidade de entrega.');
      }

      var pedido = {
        id: gerarProtocolo(),
        maquina: maquina,
        origem: origem,
        diaria: diaria,
        solicitante: nome,
        telefone: telefone,
        inicio: inicio,
        fim: fim,
        dias: dias,
        entrega: retirada ? 'Retirada na propriedade' : CIDADES[campoCidade.value],
        mensagem: form.mensagem.value.trim(),
        status: 'pendente',
        motivo: '',
        criadoEm: new Date().toISOString()
      };

      var lista = lerPedidos();
      lista.push(pedido);
      salvarPedidos(lista);

      document.getElementById('protocolo').textContent = '#' + pedido.id;
      document.getElementById('cartaoFormulario').hidden = true;
      document.getElementById('confirmacao').hidden = false;
    });
  }

  // ---------- Tela: painel do locador ----------
  function iniciarPainel() {
    var container = document.getElementById('listaPedidos');
    if (!container) return;

    var vazio = document.getElementById('estadoVazio');
    var abas = document.querySelectorAll('.tab');
    var filtroAtual = 'pendente';

    function atualizarContadores(lista) {
      var contagem = { pendente: 0, aceito: 0, recusado: 0, todos: lista.length };
      lista.forEach(function (p) { contagem[p.status]++; });
      document.querySelectorAll('[data-contador]').forEach(function (no) {
        no.textContent = contagem[no.dataset.contador];
      });
    }

    function responder(id, status, motivo) {
      var lista = lerPedidos();
      lista.forEach(function (p) {
        if (p.id === id) {
          p.status = status;
          p.motivo = motivo || '';
          p.respondidoEm = new Date().toISOString();
        }
      });
      salvarPedidos(lista);
      renderizar();
    }

    function campo(rotulo, valor) {
      var div = criar('div');
      div.appendChild(criar('span', 'order-field-label', rotulo));
      div.appendChild(criar('span', 'order-field-value', valor));
      return div;
    }

    function criarCard(p) {
      var card = criar('article', 'order-card');

      var cabecalho = criar('div', 'order-head');
      var titulos = criar('div');
      titulos.appendChild(criar('h2', 'order-title', p.maquina));
      titulos.appendChild(criar('span', 'order-protocol', 'Pedido #' + p.id));
      cabecalho.appendChild(titulos);
      cabecalho.appendChild(criar('span', 'badge badge-' + p.status, STATUS[p.status]));
      card.appendChild(cabecalho);

      var grade = criar('div', 'order-grid');
      grade.appendChild(campo('Solicitante', p.solicitante));
      grade.appendChild(campo('Telefone', p.telefone));
      grade.appendChild(campo('Entrega', p.entrega));
      grade.appendChild(campo('Período', formatarData(p.inicio) + ' a ' + formatarData(p.fim)));
      grade.appendChild(campo('Diárias', p.dias + (p.dias === 1 ? ' diária' : ' diárias')));
      grade.appendChild(campo('Valor das diárias', formatarMoeda(p.dias * p.diaria)));
      card.appendChild(grade);

      if (p.mensagem) {
        card.appendChild(criar('p', 'order-message', p.mensagem));
      }

      if (p.status === 'recusado' && p.motivo) {
        card.appendChild(criar('p', 'order-reason', 'Motivo da recusa: ' + p.motivo));
      }

      if (p.status === 'pendente') {
        var caixa = criar('div', 'refuse-box');
        caixa.hidden = true;
        caixa.appendChild(criar('label', 'form-label', 'Motivo da recusa (opcional)'));
        var texto = criar('textarea', 'form-textarea');
        texto.rows = 2;
        texto.placeholder = 'Ex: máquina indisponível nessas datas.';
        caixa.appendChild(texto);
        var acoesCaixa = criar('div', 'order-actions');
        var cancelar = criar('button', 'btn btn-secondary', 'Cancelar');
        cancelar.type = 'button';
        var confirmar = criar('button', 'btn btn-danger-solid', 'Confirmar recusa');
        confirmar.type = 'button';
        acoesCaixa.appendChild(cancelar);
        acoesCaixa.appendChild(confirmar);
        caixa.appendChild(acoesCaixa);

        var acoes = criar('div', 'order-actions');
        var recusar = criar('button', 'btn btn-danger', 'Recusar');
        recusar.type = 'button';
        var aceitar = criar('button', 'btn btn-primary', 'Aceitar pedido');
        aceitar.type = 'button';
        acoes.appendChild(recusar);
        acoes.appendChild(aceitar);

        recusar.addEventListener('click', function () {
          caixa.hidden = false;
          acoes.hidden = true;
        });
        cancelar.addEventListener('click', function () {
          caixa.hidden = true;
          acoes.hidden = false;
        });
        confirmar.addEventListener('click', function () {
          responder(p.id, 'recusado', texto.value.trim());
        });
        aceitar.addEventListener('click', function () {
          responder(p.id, 'aceito');
        });

        card.appendChild(acoes);
        card.appendChild(caixa);
      }

      return card;
    }

    function renderizar() {
      var lista = lerPedidos();
      atualizarContadores(lista);

      var visiveis = lista.filter(function (p) {
        return filtroAtual === 'todos' || p.status === filtroAtual;
      }).sort(function (a, b) {
        return b.criadoEm.localeCompare(a.criadoEm);
      });

      container.textContent = '';
      visiveis.forEach(function (p) {
        container.appendChild(criarCard(p));
      });

      // O estado vazio so aparece quando nao ha nenhum pedido neste filtro
      vazio.hidden = visiveis.length > 0;
    }

    abas.forEach(function (aba) {
      aba.addEventListener('click', function () {
        abas.forEach(function (a) { a.classList.remove('active'); });
        aba.classList.add('active');
        filtroAtual = aba.dataset.filtro;
        renderizar();
      });
    });

    // Pedidos de exemplo para testar o painel sem preencher o formulario
    document.getElementById('btnExemplos').addEventListener('click', function () {
      var agora = Date.now();
      var exemplos = [
        {
          id: 'PED-' + String(agora).slice(-6),
          maquina: 'Trator John Deere 6115J Cabinado', origem: 'Paulínia / SP', diaria: 850,
          solicitante: 'João da Silva', telefone: '(19) 99999-1111',
          inicio: '2026-11-10', fim: '2026-11-14', dias: 5, entrega: 'Campinas / SP',
          mensagem: 'Preciso do trator para preparo de solo em 20 hectares.',
          status: 'pendente', motivo: '', criadoEm: new Date(agora).toISOString()
        },
        {
          id: 'PED-' + String(agora + 1).slice(-6),
          maquina: 'Trator John Deere 6115J Cabinado', origem: 'Paulínia / SP', diaria: 850,
          solicitante: 'Maria Souza', telefone: '(19) 98888-2222',
          inicio: '2026-11-20', fim: '2026-11-22', dias: 3, entrega: 'Retirada na propriedade',
          mensagem: '',
          status: 'pendente', motivo: '', criadoEm: new Date(agora - 60000).toISOString()
        }
      ];
      salvarPedidos(lerPedidos().concat(exemplos));
      renderizar();
    });

    renderizar();
  }

  iniciarSolicitacao();
  iniciarPainel();
})();
