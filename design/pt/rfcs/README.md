# RFCs — Decisões de Design Contestadas

Esta pasta contém **RFCs**: writeups das decisões de design genuinamente *não resolvidas e de alto impacto* — as que [`../principles.md`](../principles.md) diz marcar como **TBD** e encaminhar para uma **Discussion** em vez de deixar como padrão silencioso. Um RFC enquadra uma decisão; não assume uma.

**RFC ≠ feature brief.** Um [feature brief](../features/) descreve trabalho aceito, *pronto para começar* e baseado em código. Um RFC descreve uma **bifurcação no caminho** — a questão, as opções com trade-offs, uma inclinação e como decidiremos. Briefs são para construir; RFCs são para *escolher*, geralmente antes que um brief possa ser escrito.

## Modelo operacional

Consistente com o modelo operacional do [`../README.md`](../README.md) (**markdown primeiro, depois o quadro, feedback nas Discussions**):

1. **Escreva o RFC aqui** — capture a questão, opções e inclinação em markdown (revisável em PR).
2. **Abra uma GitHub Discussion** (Ideas, ou **Polls** quando sinal de demanda importa) linkando o RFC. É aqui que a decisão realmente é tomada.
3. **Registre o resultado** — mude as linhas **TBD** relevantes nos detalhamentos de [`../mods/`](../mods/) para `Port` / `Modernize` / `Skip`, e (se aceito) semeie um [feature brief](../features/). Atualize o status do RFC para ✅ Decidido com uma linha de resultado + link.

RFCs são numerados sequencialmente (`NNNN-`), independente da ordem de fase/passo usada pelos feature briefs — uma decisão não é um passo de build.

## RFCs em aberto

| # | RFC | Escopo | A questão | Inclinação |
|---|---|---|---|---|
| 0001 | [Comportamento Programável](0001-programmable-behavior.md) | Fase 2+ (pós-1.0) | Sistema unificado de lógica gates+circuitos, hooks voltados ao vanilla, ou pular para v1? | **Adiado pós-1.0** (mantenedor): não é item de 1.0; revisitar quando o Forestry precisar de circuit boards |
| 0002 | [Abelhas Forestry](0002-forestry-bees.md) | Fase 2 (Forestry) | Entregar abelhas de alguma forma — e estender vanilla ou construir genética paralela? | Pular para v1; estender-vanilla se perseguido depois |
| 0003 | [Multiblocos & Vapor do Railcraft](0003-railcraft-multiblocks.md) | Fase 3 (Transporte) | Bloco único / ladrilhável / multibloco por bloco assinatura; vapor é um nível de energia? | Máquinas de bloco único; tanques em massa ladrilháveis; vapor simplificado para v1 |

> A coluna de **inclinação** é uma posição inicial para debater, não uma decisão. Cada RFC traz as opções completas + trade-offs.

## Template de RFC

Copie ao adicionar um (nomeie como `NNNN-<topico>.md`):

```markdown
# RFC NNNN: <Título>

> **Status:** 🟡 Open — needs Discussion · **Scope:** … · **Decides:** …
> **Affects:** <linhas do detalhamento> · **Blocks:** <o que agendar bloqueia>

## Context
## The decision to make        # a questão objetiva
## Options                     # cada: o quê / prós / contras / esforço
## Recommendation / leaning
## Sub-questions still open
## How we'll decide            # critérios: equilíbrio, complexidade, poll da comunidade
## References
```

(RFCs de tabela de decisão — como 0003 — podem trocar a lista de Opções por uma tabela "Disposição recomendada" quando a decisão é por item em vez de uma bifurcação global.)
