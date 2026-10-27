/*
  Autor: Lucas Rodrigues Xavier | 25000508
  Descricao: Validacoes do formulario de cadastro de maquinas
*/

document.addEventListener("DOMContentLoaded", function () {
  const form = document.querySelector("form");

  if (!form) return;

  form.addEventListener("submit", function (event) {
    // 1. Tipo de Equipamento
    const tipo = document.getElementById("tipoEquipamento");
    if (!tipo || !tipo.value.trim()) {
      alert("Por favor, selecione o tipo de equipamento.");
      tipo.focus();
      event.preventDefault();
      return;
    }

    // 2. Título do Anúncio
    const titulo = document.getElementById("tituloAnuncio");
    if (!titulo || titulo.value.trim().length < 5) {
      alert("Por favor, informe um título válido para o anúncio (mínimo de 5 caracteres).");
      titulo.focus();
      event.preventDefault();
      return;
    }

    // 3. Marca e Modelo
    const marca = document.getElementById("marca");
    const modelo = document.getElementById("modelo");
    if (!marca || !marca.value.trim()) {
      alert("Por favor, informe a marca do equipamento.");
      marca.focus();
      event.preventDefault();
      return;
    }
    if (!modelo || !modelo.value.trim()) {
      alert("Por favor, informe o modelo do equipamento.");
      modelo.focus();
      event.preventDefault();
      return;
    }

    // 4. Ano de Fabricação (se preenchido)
    const ano = document.getElementById("anoFabricacao");
    const anoAtual = new Date().getFullYear();
    if (ano && ano.value) {
      const valorAno = parseInt(ano.value, 10);
      if (valorAno < 1950 || valorAno > anoAtual + 1) {
        alert("Ano de fabricação inválido. Informe um ano entre 1950 e " + (anoAtual + 1) + ".");
        ano.focus();
        event.preventDefault();
        return;
      }
    }

    // 5. Horas de Uso (Horímetro)
    const horimetro = document.getElementById("horimetro");
    if (horimetro && horimetro.value) {
      const valorHoras = parseFloat(horimetro.value);
      if (valorHoras < 0) {
        alert("O horímetro não pode ter valor negativo.");
        horimetro.focus();
        event.preventDefault();
        return;
      }
    }

    // 6. Fotos do Equipamento
    const fotos = document.getElementById("fotos");
    if (fotos && fotos.files.length === 0) {
      const confirmarSemFoto = confirm("Nenhuma foto foi selecionada. Recomendamos anexar ao menos uma foto da máquina para aumentar a confiança dos produtores.\n\nDeseja continuar mesmo sem fotos?");
      if (!confirmarSemFoto) {
        fotos.focus();
        event.preventDefault();
        return;
      }
    }

    // 7. Município
    const municipio = document.getElementById("municipio");
    if (!municipio || !municipio.value.trim()) {
      alert("Por favor, selecione o município da propriedade.");
      municipio.focus();
      event.preventDefault();
      return;
    }

    // 8. Período de Disponibilidade
    const dataInicio = document.getElementById("dataInicio");
    const dataFim = document.getElementById("dataFim");

    if (!dataInicio || !dataInicio.value) {
      alert("Por favor, informe a data de início da disponibilidade.");
      dataInicio.focus();
      event.preventDefault();
      return;
    }

    if (!dataFim || !dataFim.value) {
      alert("Por favor, informe a data de término da disponibilidade.");
      dataFim.focus();
      event.preventDefault();
      return;
    }

    const dInicio = new Date(dataInicio.value);
    const dFim = new Date(dataFim.value);

    if (dFim < dInicio) {
      alert("A data de término da disponibilidade não pode ser anterior à data de início.");
      dataFim.focus();
      event.preventDefault();
      return;
    }

    // 9. Valor da Diária
    const valorDiaria = document.getElementById("valorDiaria");
    if (!valorDiaria || !valorDiaria.value.trim()) {
      alert("Por favor, informe o valor da diária.");
      valorDiaria.focus();
      event.preventDefault();
      return;
    }

    // Limpa máscara básica (R$, pontos e troca vírgula por ponto)
    const valorLimpo = valorDiaria.value.replace("R$", "").replace(/\./g, "").replace(",", ".").trim();
    const valorNumerico = parseFloat(valorLimpo);

    if (isNaN(valorNumerico) || valorNumerico <= 0) {
      alert("Por favor, informe um valor de diária válido e positivo (Ex: 850,00).");
      valorDiaria.focus();
      event.preventDefault();
      return;
    }

    // Sucesso na validação
    alert("Equipamento validado com sucesso! O anúncio será disponibilizado no catálogo.");
  });
});
