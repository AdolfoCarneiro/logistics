# Feature Briefs

Esta pasta expande linhas consolidadas do [`roadmap.md`](../roadmap.md) em **feature briefs** — um arquivo por feature, com detalhe suficiente para *começar o trabalho*. Cada brief é baseado no código real: nomeia as classes e padrões reais aos quais uma feature se conecta, para que um implementador não comece do zero.

Briefs ficam **entre** o "por quê" durável (os detalhamentos em [`mods/`](../mods/), [`vision.md`](../vision.md), [`principles.md`](../principles.md)) e o rastreamento de trabalho ao vivo (GitHub Project #4). O detalhamento diz *o que decidimos e por quê*; o brief diz *o que construir e como começar*; a issue do Project rastreia *o trabalho*. Quando um brief consolidar, decomponha no quadro (veja [Mapeamento para o quadro](../roadmap.md#mapeamento-para-o-quadro) no roadmap).

> Estes são **pontos de partida, não specs em pedra**. Todo brief tem uma seção "Questões em aberto" — as decisões ainda devidas antes ou durante a implementação. Resolva as bloqueadoras (geralmente via Discussion ou um spike curto) antes de agendar.

## Esquema de nomenclatura

Arquivos têm o prefixo **`PPSS-`** — dois dígitos de **fase**, dois de **passo** — para que se ordenem pelo build order dentro de sua fase e permaneçam agrupados conforme briefs de fases posteriores chegam nesta pasta compartilhada.

- `01xx` = Fase 1 (Núcleo de automação), `02xx` = Fase 2 (Forestry), `03xx` = Fase 3 (Transporte).
- O passo é a **ordem de build sugerida** dentro da fase, ciente de dependências (pré-requisitos de uma feature têm números de passo menores). É um guia, não um contrato — trilhas paralelas existem (veja abaixo).
- Novos briefs em um lote continuam a sequência (este lote termina em `0110`; os itens adiados bloqueados por fluidos se tornam `0111+`).

## Este lote: keystone da Fase 1 + prontos agora

O primeiro lote cobre a **[Fundação de Fluidos](0101-fluids-foundation.md)** (o keystone que desbloqueia grande parte das Fases 1–3) mais os itens da Fase 1 **sem bloqueadores** — o trabalho que pode começar imediatamente.

| # | Brief | Área | Decisão | Depende de |
|---|---|---|---|---|
| 0101 | [Fundação de Fluidos](0101-fluids-foundation.md) | 🔑 keystone | Port/Modernize | — (camada de plataforma já construída) |
| 0102 | [Saídas Secundárias do Macerador](0102-macerator-secondary-outputs.md) | Máquinas | Modernize | — |
| 0103 | [Hand Grinder](0103-hand-grinder.md) | Máquinas | Modernize | `0102` (receitas reutilizadas) |
| 0104 | [Serraria](0104-sawmill.md) | Máquinas | Port | `ChanceResult` (0102) |
| 0105 | [Alloy Smelter](0105-alloy-smelter.md) | Máquinas | Port | `ChanceResult` (0102), `0103` |
| 0106 | [Upgrades / Augments de Máquinas](0106-machine-upgrades.md) | Máquinas (transversal) | Modernize | as máquinas (0102–0105) |
| 0107 | [Baterias em Camadas](0107-tiered-batteries.md) | Energia | Modernize | — (estende a Bateria) |
| 0108 | [Tubo Vácuo de Obsidiana](0108-obsidian-vacuum-pipe.md) | Tubos | Port | — |
| 0109 | [Remote Orderer](0109-remote-orderer.md) | Logistics QoL | Modernize | — |
| 0110 | [Firewall Pipe](0110-firewall-pipe.md) | Logistics avançado | Port | — |

### Lendo a ordem

Os números de passo codificam dependências, mas vários briefs são independentes e podem rodar em paralelo:

- **Começar primeiro — `0101` Fluidos:** o keystone. Comece com seu spike de design (tubo de fluido paralelo vs. integrado à rede) pois desbloqueia o lote adiado inteiro.
- **Cadeia de máquinas — `0102` → `0103` → `0104`/`0105` → `0106`:** construa [Saídas Secundárias do Macerador](0102-macerator-secondary-outputs.md) primeiro; produz o mecanismo compartilhado `ChanceResult` que [Serraria](0104-sawmill.md) (serragem) e [Alloy Smelter](0105-alloy-smelter.md) (escória) reutilizam. [Hand Grinder](0103-hand-grinder.md) pode ser paralelo a 0102 (sem dependência de `ChanceResult`). Depois adicione [Upgrades de Máquinas](0106-machine-upgrades.md) em todas elas.
- **Independentes (qualquer hora) — `0107`/`0108`/`0109`:** [Baterias em Camadas](0107-tiered-batteries.md), [Tubo Vácuo de Obsidiana](0108-obsidian-vacuum-pipe.md), [Remote Orderer](0109-remote-orderer.md) não têm dependências no lote.
- **Spike antes de agendar — `0101` e `0110`:** a abordagem de tubo de [Fluidos](0101-fluids-foundation.md) e o [Firewall Pipe](0110-firewall-pipe.md) (routing-gate vs. segmentação de grafo, que toca código de rede estável) precisam de um spike curto para definir a abordagem.

### Adiados para um lote posterior (Fase 1, bloqueados por fluidos ou precisam de Discussion)

Ainda não escritos — esperam o keystone ou uma decisão em aberto, e ocuparão passos `0111+`:

- **Precisa de fluidos primeiro:** motor nível combustão, nível magmático/dínamo, Magma Crucible, Fluid Transposer, logística de fluidos (provider/supplier/request), Bomba (construída como a fatia de validação da Fundação de Fluidos), a cadeia de combustível de óleo/biofuel.
- **Adiado pós-1.0 (RFC, não é feature brief):** o sistema de automação programável / gates+circuitos (gates BuildCraft + augments programáveis TE + circuitos Forestry) — **não é um item de 1.0**; revisitar quando o Forestry precisar de circuit boards (Fase 2) ou depois. Veja [`../rfcs/0001-programmable-behavior.md`](../rfcs/0001-programmable-behavior.md). *(Distinto dos upgrades de modificador de máquina da Fase 1, [0106](0106-machine-upgrades.md).)*
- **Já feito:** gate de energia para operação de tubos (✅, #464/#465/#469).

## Template de brief

Todo brief usa o mesmo formato. Copie ao adicionar um (nomeie como `PPSS-<feature>.md`):

```markdown
# <Feature>

> **Status:** … · **Phase:** … · **Module:** …
> **Source:** <linha do detalhamento do mod> · **Depends on:** … · **Maps to (roadmap):** …

Resumo de um parágrafo.

## Problem & goal
## Requirements
### Functional
### Balance
## Design sketch        # baseado em classes/caminhos reais ao qual a feature se conecta
## Scope & non-goals
## Open questions       # decisões devidas antes/durante a implementação
## Done when            # critérios de aceitação
## References           # linha do roadmap, detalhamento do mod, precedentes de código
```

**Convenções:**
- O **Design sketch** deve referenciar código real (classes, pacotes, o padrão a espelhar) — isso é o que torna um brief "suficiente para começar."
- Mantenha números de equilíbrio como *âncoras* (relacione a constantes existentes — Macerador ~10k RF buffer / ~200-tick op, Bateria 100k / 1k RF-t, tiers de cabo 30/60/120 RF-t), não valores finais.
- **Questões em aberto** são de primeira classe: liste as bifurcações reais e uma inclinação, não as encubra.
- Linke briefs relacionados com links relativos (use o nome numerado); linke de volta à linha do roadmap e à justificativa em `mods/`.
