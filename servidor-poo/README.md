# AlugaMáquina — Servidor Java TCP Puro (POO/Java)

Implementação dedicada do **Servidor e Cliente em Java puro (Sockets TCP + `ObjectInputStream`/`ObjectOutputStream`)**, seguindo fielmente a arquitetura do *Fazedor de Continhas* apresentada na disciplina de Programação Orientada a Objetos (PUC-Campinas).

> **Zero HTTP / Zero Spring Boot / Zero Maven:** utiliza apenas bibliotecas nativas do JDK (`java.net.*`, `java.io.*`, `java.util.*`, `java.util.concurrent.Semaphore`, `java.math.*`).
>
> A API Web Spring Boot que atende o front-end HTML/CSS/JS e sustenta os testes de FTTVS permanece preservada em `backend/`.

---

## Arquitetura Cliente-Servidor (TCP)

- **Servidor (`Servidor/`)**
  - `Servidor`: classe principal (`public static void main`), inicia a aceitação de conexões na porta `3000` (ou informada via `args[0]`) e aguarda o comando `"desativar"` no console para notificar e desconectar clientes ativos.
  - `AceitadoraDeConexao`: `Thread` com `ServerSocket` que aceita conexões e cria uma `SupervisoraDeConexao` por cliente.
  - `SupervisoraDeConexao`: `Thread` dedicada por cliente que recebe instâncias de `Comunicado`, executa a operação no `RepositorioAlugaMaquina` (`synchronized`) e devolve `ResultadoDaOperacao` ou `ResultadoDaSimulacao`.
  - `Parceiro`: encapsula `Socket`, `ObjectInputStream`, `ObjectOutputStream` e controle com `Semaphore` (`receba`, `espie`, `envie`, `adeus`).
  - `MaquinaAgricola`: entidade de domínio com todos os métodos canônicos de POO (`construtor de cópia`, `clone`, `toString`, `equals`, `hashCode`, `compareTo`).
- **Cliente (`Cliente/`)**
  - `Cliente`: conecta via `Socket`, abre os streams de objetos, inicia `TratadoraDeComunicadoDeDesligamento` e exibe menu interativo no console:
    1. Buscar / listar máquinas disponíveis
    2. Cadastrar nova máquina agrícola
    3. Simular locação e frete (`distanciaKm * 2 * R$ 6,50 + R$ 150,00`, mais caução padrão `R$ 1.000,00`)
    4. Registrar avaliação pós-locação (5 critérios de 1 a 5 e recálculo da reputação)
    5. Consultar reputação de locador
    6. Terminar (`PedidoParaSair`)

---

## Responsáveis pelas Classes (Time 31)

| Integrante (Nome \| RA) | Arquivos de Responsabilidade |
| :--- | :--- |
| **Lucas Rodrigues Xavier \| 25000508** | `Servidor.java`, `MaquinaAgricola.java`, `PedidoDeCadastroDeMaquina.java`, `PedidoDeAvaliacao.java`, `PedidoDeConsultaDeReputacao.java` |
| **Leonardo Gambaroni Alves \| 25003494** | `AceitadoraDeConexao.java`, `SupervisoraDeConexao.java`, `Parceiro.java`, `Comunicado.java`, `ComunicadoDeDesligamento.java`, `PedidoParaSair.java` |
| **Mateus Oliveira Rafael \| 25001369** | `RepositorioAlugaMaquina.java`, `PedidoDeBuscaDeMaquinas.java` |
| **Tiago Medeiros \| 25000845** | `PedidoDeSimulacaoDeLocacao.java`, `ResultadoDaSimulacao.java` |
| **Kauan Aurélio Lasmar Dias \| 25001590** | `Cliente.java`, `TratadoraDeComunicadoDeDesligamento.java`, `ResultadoDaOperacao.java`, `Teclado.java` |

---

## Como Compilar e Executar

### Comando Único (Sobe tudo automaticamente)

Na raiz do repositório (`AlugaMaquina/`), execute:

```powershell
.\scripts\start-dev.ps1
```

Isso inicia em um único comando o **Docker / MongoDB** (ou conecta direto ao **MongoDB Atlas** se `MONGODB_URI` estiver configurado no `.env`), a **API Spring Boot (`8080`)**, o **Servidor TCP POO (`3000`)**, o **Cliente TCP POO** e o **Frontend Web (`5500`)**.

*(Se quiser subir apenas os servidores sem abrir a janela do Cliente TCP, use `.\scripts\start-dev.ps1 -SemClienteTcp`.)*

---

### Execução Manual em 2 Terminais (Apenas Servidor e Cliente TCP)

Abra **dois terminais** na raiz do repositório:

**Terminal 1 — Iniciar o Servidor (porta 3000):**
```powershell
cd servidor-poo\Servidor
javac -encoding UTF-8 *.java
java Servidor
```
*(Para encerrar o servidor com segurança, digite `desativar` no terminal do Servidor.)*

**Terminal 2 — Iniciar o Cliente:**
```powershell
cd servidor-poo\Cliente
javac -encoding UTF-8 *.java
java Cliente
```
*(Opcionalmente informe host e porta: `java Cliente localhost 3000`.)*
