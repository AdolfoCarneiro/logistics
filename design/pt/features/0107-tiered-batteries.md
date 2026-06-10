# Linha de Armazenamento de Energia em Camadas (Baterias)

> **Status:** 🚧 Planned · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (domínio `power`)
> **Source:** [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (Energy Cells: Leadstone→Resonant) · **Depends on:** nada (estende a Bateria existente)
> **Maps to (roadmap):** Fase 1 — Bateria → linha de armazenamento de energia em camadas

Expandir a única Bateria existente em uma linha de 3 tiers (Cobre → Ouro → Ender, per a escada canônica em [`../progression-tiers.md`](../progression-tiers.md)) com capacidade crescente e taxas de I/O configuráveis. O codebase já tem a abstração (`AbstractBatteryBlockEntity`) e um precedente de tiering (os tiers de cabo), então isso é majoritariamente conteúdo + UX de config de I/O.

## Problem & goal

Há uma Bateria (100.000 RF, 1.000 RF/t). O armazenamento de energia atualmente é plano — sem progressão, sem controle de throttle. A linha clássica de Energy Cell dava *tiers* de armazenamento e, crucialmente, **taxas de input/output configuráveis** para que uma célula pudesse agir como buffer, limitador ou fonte de burst.

**Goal:** uma linha de bateria de 3 tiers com capacidade/throughput crescentes e I/O configurável por bloco (idealmente por lado), reutilizando a base de bateria existente e o padrão de tier de cabo.

## Requirements

### Functional
- **Três tiers** nomeados da escada canônica ([`../progression-tiers.md`](../progression-tiers.md)) — **Cobre · Ouro · Ender** (sabor de armazenamento) — com capacidade e I/O máximo crescentes. A Bateria atual mapeia para um deles (preferir Cobre ou Ouro para que baterias salvas mantenham seus stats de 100k/1k).
- Cada tier: bloco + item distintos, `BlockEntityType` compartilhado, capacidade + I/O máximo definidos por tier (estilo tier de cabo).
- **Taxa de I/O configurável** — o jogador pode definir input e output máximos (independentemente), pelo menos por bloco; por lado é o stretch goal. Persistido e mostrado em um GUI pequeno ou via ciclo de wrench.
- Nível de carga visível no mundo (a propriedade de block-state `CHARGE` existente, 0–10) e no item (a renderização de barra de durabilidade existente em `BatteryBlockItem`).
- Energia persiste através de quebra/colocação via o caminho existente do componente `block_entity_data`.
- Interopera com tanto o lado push (motores/baterias → máquinas) quanto o lado pull de rede (`ILogisticsNetwork.consumeEnergy`) exatamente como a Bateria atual faz.

### Balance
- Capacidade/throughput ancorados na Bateria atual (cap 100k, **1k RF/t** de I/O por lado) como o **meio** da linha; ex.: base ≈ 50k / 0,5k, top ≈ 1M / 5k. **Nota:** o I/O de bateria é o limite de transferência *direta por face* e já excede o throughput de cabo — cabos são um gargalo de rede separado, muito menor (`CableTier`: cobre 30 / ouro 60 / **ender 120 RF/t**). Então não dimensione baterias "para saturar um cabo"; através de uma rede de cabos, o cabo é sempre o tubo menor. O I/O alto de bateria só importa para adjacência direta bateria↔máquina e draw de múltiplas conexões.
- Tiers mais altos bloqueados por ligas/componentes do tier [`0105-alloy-smelter.md`](0105-alloy-smelter.md) para que a progressão de armazenamento se acople à curva de materiais.
- A config de I/O não pode exceder o máximo de hardware do tier — throttle para baixo, nunca para cima.

### Alinhamento de taxas vs dutos de energia do TE

Verificamos nossas taxas de distribuição de energia (cabos + I/O de bateria) contra os **Fluxducts** do Thermal Expansion (Thermal Dynamics; números da wiki do Team CoFH — TE não tem fonte pública, tratar como referência):

| | Logistics | TE Fluxducts |
|---|---|---|
| Tiers | 3 — Cobre / Ouro / Ender | 5 + ilimitado — Leadstone / Hardened / Redstone / Signalum / Resonant / Cryo |
| Taxa (RF/t) | 30 / 60 / 120 | 200 / 800 / 8.000 / … / 32.000 / ∞ |
| Razão por tier | ×2 | ~×4–×10 |
| Spread total | 4× | ~160× (+∞) |
| Perda / buffer | sem perda, sem buffer | sem perda, sem buffer |

**Veredicto: alinhados em *filosofia*, deliberadamente *não* em taxas absolutas — corretamente.** Ambos são condutos em tiers sem perda, sem buffer, com taxa por segmento limitada. Mas nossa economia de RF é ~2 ordens de magnitude menor (Macerador consome ~10 RF/t com cap de intake de 128; Stirling produz 3–10 RF/t), então os dutos 200–32.000 RF/t do TE seriam selvagemente superdimensionados aqui. **Copiar o shape, não os números** — o Ender com 120 RF/t já alimenta nossas máquinas confortavelmente.

**Escada de cabos (decidido).** Cabos adotam a escada canônica ([`../progression-tiers.md`](../progression-tiers.md)) → **Cobre · Ouro · Ametista · Ender** a **30 / 60 / 120 / 240 RF/t** (mantém a escada ×2; Ametista é o novo tier "ressonante" na antiga taxa do Ender, Ender sobe para 240 para margem no late-game). Números ficam ajustáveis contra a curva de RF — **revisitar uma vez que o output do motor de tier combustão e os maiores intakes de máquina estiverem definidos**, dimensionando o topo para a maior demanda esperada de linha única (a abordagem "amarrar taxas ao consumo" de [`0101-fluids-foundation.md`](0101-fluids-foundation.md)). Esta é uma tarefa de retrofit (adiciona um tier `amethyst_cable` + re-taxa o Ender) — veja o plano de retrofit em [`../progression-tiers.md`](../progression-tiers.md).

**Tiers de bateria** também derivam da escada canônica — ex.: Cobre · Ouro · Ender (sabor de armazenamento) — em vez de inventar nomes; escolher os números de I/O em relação à demanda de máquinas, não aos valores das células do TE.

## Design sketch

A base já suporta tiering via parâmetros de construtor — verificado: `AbstractBatteryBlockEntity` recebe `(type, pos, state, capacity, maxInsert, maxExtract)` e `BatteryBlockEntity` passa constantes. Espelhar a abordagem de **tier de cabo** (enum `CableTier` + instâncias de `Block` por tier, tipo de BE compartilhado).

```text
common/src/main/java/com/logistics/power/block/
├── BatteryTier.java          # enum {COPPER, GOLD, ENDER} → capacity(), maxIo()
├── BatteryBlock.java         # contém um campo BatteryTier (como CableBlock contém CableTier)
└── entity/BatteryBlockEntity.java  # lê tier do bloco; passa tier.capacity()/maxIo() para super
```

- Registrar três blocos em `LogisticsPower.BLOCK` (`registerBlockWithItem`), um `BlockEntityType` compartilhado cobrindo os três (`registerBlockEntity(type, factory, BASIC, REINFORCED, RESONANT)`) — exatamente o shape de registro de cabo.
- **Armazenamento de config de I/O:** duas opções —
  - **(A) `IntegerProperties` de block-state** `INPUT_RATE` / `OUTPUT_RATE` ciclados por wrench. Simples, sem GUI, mas grosseiro (poucos passos discretos) e incha os blockstates.
  - **(B) Data component / NBT do BE** contendo `maxInput`/`maxOutput` (e mapas por lado para o stretch), editado em um GUI pequeno. Controle mais fino, segue o precedente de `PipeDataComponents`, copia para o item dropado.
  - **Inclinação: (B)** para controle real de I/O, caindo de volta para ciclo-por-wrench de presets se um GUI for demais para v1.
- O `EnergyComponent` já aceita max-insert/max-extract; os valores configurados alimentam um wrapper para que `insert/extract` clampeiem para o menor entre (máx do tier, configurado).

## Scope & non-goals

- **In:** três tiers, capacidade/throughput por tier, I/O configurável (por bloco mín; por lado stretch), exibição de carga no mundo + no item.
- **Out:** transferência sem fio/entre dimensões (Tesseract — fora de escopo per detalhamento TE), variantes cosméticas de "frames" de célula de energia, modos de saída controlados por redstone (poderia ser um augment posterior), toggles de *enable* de input/output por lado se a config de taxa por lado já cobrir.

## Open questions

- **Armazenamento de config de I/O: block-state vs data-component+GUI** (acima). Define se esta entrega uma tela.
- **I/O por lado vs por bloco inteiro** para v1 — por lado é o comportamento clássico da Energy Cell mas mais trabalho de UX/render. **Inclinação: por bloco inteiro na v1, por lado como fast-follow.**
- Nomes de tier + números exatos de capacidade/throughput (escolher pelos próprios méritos — *não* "para saturar um cabo", pois cabos topam em 120 RF/t enquanto até a bateria base faz mais).
- A Bateria **existente** se torna BASIC ou REINFORCED? (Afeta se as baterias de mundos existentes mudam stats — prefere mapear para que baterias salvas mantenham seus 100k/1k atuais.)

## Done when

- Três tiers de bateria colocam, armazenam e transferem energia com capacidade/throughput distintos em ambos os loaders.
- Taxas de I/O são configuráveis e clampam corretamente (nunca excedem o máximo do tier), persistidos através de save/load e quebra/colocação.
- Carga mostra no mundo e no item para todos os tiers.
- As taxas de I/O de bateria são independentes do throughput de cabo; uma bateria alimentando por uma rede de cabos é corretamente limitada pelo cabo (≤120 RF/t no Ender), enquanto adjacência direta usa a taxa total por face da bateria.

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → Bateria (tiers); [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) → linha "Energy Cells"
- Código: `core/lib/power/AbstractBatteryBlockEntity` (construtor pronto para tiering), `power/block/{BatteryBlock,BatteryBlockItem}`, `power/block/entity/BatteryBlockEntity`; precedente de tier `power/cable/{CableTier,CableBlock,CableBlockEntity,CableNetwork}` (taxas atuais 30/60/120 RF/t, sem perda, sem buffer); precedente de data-component `pipe/data/PipeDataComponents`; registro em `LogisticsPower.java`
- Referência Fluxduct TE (wiki — TE não tem fonte pública): [Team CoFH — Fluxducts](https://teamcofh.com/docs/1.12/thermal-dynamics/fluxducts/) (Leadstone 200 / Hardened 800 / Redstone 8.000 / Resonant 32.000 RF/t; [Cryo-Stabilized](https://teamcofh.com/docs/1.12/thermal-dynamics/cryo-stabilized-fluxduct/) = ilimitado)
- Relacionados: [`0105-alloy-smelter.md`](0105-alloy-smelter.md) (gate de materiais), [`0106-machine-upgrades.md`](0106-machine-upgrades.md) (cria a demanda por mais armazenamento)
