# Serraria

> **Status:** 🚧 Planned — **design resolvido, pronto para construir** · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (domínio `automation`)
> **Source:** [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (Sawmill) · **Depends on:** [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md) (`ChanceResult` para serragem)
> **Maps to (roadmap):** Fase 1 — Serraria

Uma máquina RF de entrada única: toras → pranchas extras + uma chance de serragem. A máquina nova de menor complexidade na Fase 1 — efetivamente o padrão do Macerador com um set de receitas com tema de madeira. Os pós Wood Pulp / Flour já existem; isso dá a eles uma máquina de produção primária.

## Problem & goal

O vanilla dá 4 pranchas por tora. A Serraria clássica dava *mais* (≈6) mais serragem, tornando-a a máquina de eficiência de madeira no início de jogo e o ponto de entrada para uma cadeia de subprodutos de madeira (serragem → papel/blocos comprimidos/etc. depois).

**Goal:** uma máquina RF barata e inicial que melhora o rendimento tora→prancha e produz serragem, reutilizando os mecanismos de máquina e saída secundária existentes com mínima superfície nova.

## Requirements

### Functional
- **Slots (herda 0102):** uma entrada (uma tora) + **saída primária (pranchas)** + **saída secundária dedicada (Wood Pulp)** = 3 slots. Mesma semântica `ChanceResult` + **pausa-até-limpar** que o Macerador — um slot de Wood Pulp cheio paralisa o processamento, nenhum subproduto perdido. Topo/lados → entrada (slot 0); baixo → ambas as saídas (slots 1 e 2).
- **Tipo de receita personalizado** `logistics:sawmill`: um ingrediente, um resultado primário (pranchas, contagem configurável), subproduto opcional `ChanceResult` **Wood Pulp**, tempo de serra, custo de energia.
- **Uma receita por madeira vanilla** — JSON explícito chaveado na tag de tora de cada madeira (`#minecraft:oak_logs` → pranchas de carvalho, etc.), cobrindo carvalho…cerejeira mais talos crimson/warped (~9 receitas). Usar a *tag* de tora por madeira como ingrediente cobre elegantemente variantes descascadas/madeira. Madeiras modadas funcionam apenas se aquele mod/pack entregar sua própria receita `logistics:sawmill` — é apenas dado.
- **O subproduto reutiliza o item Wood Pulp existente** (`logistics:core/wood_pulp`) — nenhum novo item de serragem.
- RF-alimentada com buffer interno; categoria JEI; GUI de progresso + energia (reutilizar tela do Macerador).

### Balance
- Âncora de rendimento: tora → **6 pranchas** (vs vanilla 4) + uma **chance modesta de Wood Pulp (~25–50%)**, ajustada para valer a pena o consumo mas não ser uma impressora de pranchas. *Números aproximados — TE não tem fonte pública (wiki/conhecimento); ajustar em playtest.* O escalonamento de upgrade é trabalho do [`0105-machine-upgrades.md`](0105-machine-upgrades.md).
- Máquina nova mais barata de construir e operar — é um unlock de eficiência no início de jogo. Consumo de energia igual ou abaixo do Macerador.

## Design sketch

Estruturalmente a **instância mais simples do padrão de máquina** — irmão mais próximo do Kiln (entrada única) mas com receita personalizada como o Macerador.

```text
common/src/main/java/com/logistics/automation/sawmill/
├── SawmillBlock.java               # extends MachineBlock; FACING + LIT
├── SawmillBlockEntity.java         # extends BaseBlockEntity; HasItemStorage, HasEnergyStorage,
│                                   #   WorldlyContainer, MenuBehavior.HasMenu; 3 slots (entrada/pranchas/wood-pulp)
├── SawmillRecipe.java              # Recipe<SingleRecipeInput>; ingrediente, resultado, ChanceResult wood pulp, tempo
├── SawmillRecipeSerializer.java
├── SawmillProcessingPlan.java      # lógica pura, testada com testes de unidade
├── SawmillScreenHandler.java
└── (cliente) SawmillScreen.java + jei/{Category,Plugin}
```

- Registrar em `LogisticsAutomation` ao lado do Kiln. Adotar o mesmo shape de 3 slots + gate de conclusão resolvido para o Macerador.
- Reutilizar o `ChanceResult` compartilhado (de [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md)) para o subproduto Wood Pulp.
- Recipe JSON — um por madeira vanilla, chaveado na tag de tora da madeira:
  ```json
  {
    "type": "logistics:sawmill",
    "ingredient": "#minecraft:oak_logs",
    "result": { "id": "minecraft:oak_planks", "count": 6 },
    "byproduct": { "id": "logistics:core/wood_pulp", "count": 1, "chance": 0.5 },
    "sawtime": 160
  }
  ```

## Scope & non-goals

- **In:** a máquina, uma receita tora→prancha por madeira vanilla, o subproduto Wood Pulp (slot dedicado + pausa-até-limpar), JEI, GUI.
- **Out:** descascar/processar madeira que não é tora, madeira tratada / creosoto (isso é Railcraft, Fase 3 — embora Wood Pulp possa alimentar isso), upgrades (doc separado), cobertura automática de madeiras modadas (madeiras modadas precisam de sua própria receita de data-pack).

## Decisions

Todas as questões bloqueadoras de início estão resolvidas:

- **Modelo de receita** — **um JSON explícito por madeira vanilla**, chaveado na tag de tora da madeira (cobre variantes descascadas/madeira). Madeiras modadas são cobertas apenas por suas próprias receitas de data-pack — sem derivação em runtime ou datamap na v1.
- **Item de subproduto** — **reutilizar o Wood Pulp existente** (`logistics:core/wood_pulp`); nenhum novo item de serragem.
- **Mecânica de saída secundária** — **herdar 0102 verbatim**: slot secundário dedicado + `ChanceResult` + pausa-até-limpar.
- **Rendimento** — âncora **6 pranchas + ~25–50% Wood Pulp**, aproximado (wiki/conhecimento TE), ajustar em playtest; escalonamento de upgrade tratado por `0105`.

> As escolhas restantes são detalhes de implementação: a contagem final de pranchas por madeira / % de Wood Pulp, o tempo de serra, e o nome do campo de receita (`byproduct`) compartilhado com o helper `ChanceResult`.

## Done when

- Toras de cada madeira vanilla serram nas pranchas configuradas + chance de Wood Pulp em ambos os loaders.
- O roll de Wood Pulp (ambas as branches) e a **pausa-até-limpar** (slot de subproduto cheio paralisa, nada perdido) são testados com testes de unidade no plano de processamento.
- JEI lista receitas da serraria; GUI mostra progresso + energia e o slot secundário + chance.

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → Serraria
- Detalhamento: [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) → linha Sawmill (nota os itens existentes Wood Pulp/Flour)
- **Nota de fonte TE:** TE **não tem fonte pública** — rendimentos são baseados em wiki/conhecimento, aproximados; ajustar em playtest. Itens existentes confirmados em `LogisticsCore.java`: `WOOD_PULP` (`logistics:core/wood_pulp`), `FLOUR`.
- Padrão de código: `automation/kiln/*`, `core/macerator/*`; registro em `LogisticsAutomation.java`
- Mecanismo compartilhado: `ChanceResult` de [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md)
