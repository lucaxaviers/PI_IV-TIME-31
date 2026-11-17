# Modelagem do Banco de Dados - MongoDB Atlas (Issue #20)

Autor da parte de Usuarios e Avaliacoes: Lucas Rodrigues Xavier | 25000508

A conexao vem da variavel de ambiente `MONGODB_URI` (veja `application.properties`).

## Collections

| Collection | Classe | Dono | Indices |
| :--- | :--- | :--- | :--- |
| `usuarios` | `Usuario` | Lucas | `email` (unico), `documento` (unico) |
| `maquinas` | `Maquina` | Equipe | `localizacao` (`2dsphere`) |
| `avaliacoes` | `Avaliacao` | Lucas | consulta por `locadorId`, `maquinaId` e `locacaoId` |
| `chat_messages` | `ChatMessage` | Leonardo | - |

## Relacionamentos (por referencia de id)

- `Avaliacao.locadorId` aponta para `Usuario.id`. Cada avaliacao atualiza `reputacaoMedia` e `totalAvaliacoes` do locador.
- `Avaliacao.locatarioId` aponta para `Usuario.id`.
- `Avaliacao.maquinaId` aponta para `Maquina.id`.
- `Avaliacao.locacaoId` e unico por regra de negocio (`existsByLocacaoId`): uma avaliacao por locacao.

## Regras dos dados

- Notas de 1 a 5 em cinco criterios. `mediaFinal` fica com 2 casas decimais.
- `reputacaoMedia` comeca em 5.0 e `totalAvaliacoes` em 0.
- A senha fica so como hash SHA-256 em `senhaHash`. O `toString()` de `Usuario` nao mostra a senha.

## Padrao das classes

As entidades `Usuario` e `Avaliacao` seguem o padrao do Prof. Francisco: `equals(Object)` em 4 passos, `hashCode()` com todos os campos do `equals`, `compareTo`, `toString`, construtor de copia (erro `Modelo ausente` se nulo) e `clone()`.
