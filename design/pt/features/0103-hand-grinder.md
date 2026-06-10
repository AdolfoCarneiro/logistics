# Hand Grinder

> **Status:** 🚧 Planned — **design resolvido, pronto para construir** · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (domínio `core` — par do Macerador)
> **Source:** modernização (sem fonte direta) — contraparte manual e sem energia do Macerador (sabor mortar/quern) · **Depends on:** [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md) (reutiliza receitas do Macerador) · **Required by:** [`0105-alloy-smelter.md`](0105-alloy-smelter.md) (o caminho "Bronze não bloqueado por energia")
> **Maps to (roadmap):** Fase 1 — ramp de início de jogo · **Build order:** `0103` — antes do Alloy Smelter (`0105`), cujo caminho de Bronze sem energia depende disso.

Uma estação-manivela colocável que tritura minério → pó **à mão, sem RF**. A rampa de entrada sem energia que permite ao jogador chegar à economia de pó → ligas (Bronze) antes de ter motores e um Macerador. O Macerador com energia permanece o upgrade: é mais rápido *e* ganha os subprodutos por chance (0102) que o hand grinder não tem.

## Problem & goal

O caminho de ligas via crafting de pó ([`0105-alloy-smelter.md`](0105-alloy-smelter.md)) passa por pós (ex.: pó de cobre + pó de estanho → pó de bronze). Mas os pós vêm do Macerador, que precisa de energia — então sem isso, **Bronze (e toda a tier de ligas) ficaria hard-gated atrás de energia**. O Hand Grinder quebra esse gate com esforço manual.

**Goal:** um bloco barato, inicial e sem energia que transforma minério em pó base via manivela manual repetida — tedioso o suficiente para que o Macerador seja um upgrade real, mas suficiente para fazer o bootstrap do tier.

## Requirements

### Functional
- **Bloco colocável**, sem energia. Armazena um input carregado + um contador de progresso de manivela; expõe o pó finalizado como output.
- **Interação de manivela** (estilo vanilla Composter, sem GUI):
  - *Vazio* + jogador segurando item moível → carrega um item, avança para moagem.
  - *Carregado* + clique direito → **manivela**: avança o progresso um passo, com som de moagem + partículas de esmagamento e um estágio de block-state visível.
  - *Completo* → o pó é produzido para o output; clique direito coleta (ou ele cai). Depois vazio novamente.
- **Reutiliza receitas do Macerador**, restrito a uma tag `#logistics:hand_grindable` (minérios + metais brutos), produzindo **apenas o resultado primário** — *sem* secundária/subproduto (essa é a recompensa do Macerador) e sem XP.
- **~8 manivelas por moagem** (ajustável) — alguns segundos de cliques por minério.
- **Tier único, sem upgrades.** É a ferramenta manual da base da escada (Crude/Basic), não uma linha com tiers.
- **Interop com hopper/pipe, mas a manivela fica manual:** o block entity pode expor seu output (e opcionalmente input) como `HasItemStorage` para que pipes/hoppers alimentem minério e puxem pó — mas **a manivela não pode ser automatizada**, então nunca vira um auto-Macerador grátis. Esse passo manual é o gate.

### Balance
- Mais lento e mais tedioso que o Macerador por design — o trabalho manual é o custo de pular energia.
- **Sem chance de subproduto** — o Macerador com energia ganha o pó bônus ([`0102`](0102-macerator-secondary-outputs.md)); o hand grinder dá apenas o output base.
- **Craft inicial barato** (tier pedra, sem máquina) — ex.: pedra + componente de ferro. Fica na base da escada canônica ([`../progression-tiers.md`](../progression-tiers.md)) como a rampa de entrada pré-energia.

## Design sketch

Não segue o padrão de máquina com energia (sem energia, sem menu). Modela-se no **`ComposterBlock` vanilla** (progresso por block-state + interação de clique direito) mais o **`LecternBlock`** (um BE que armazena um item), reutilizando o tipo de receita do Macerador para o mapeamento real minério→pó.

```text
common/src/main/java/com/logistics/core/handgrinder/
├── HandGrinderBlock.java        # extends BaseEntityBlock; STAGE IntegerProperty (0=vazio..N=concluído);
│                                #   useItemOn() carrega um item moível; use() dá manivela / coleta
├── HandGrinderBlockEntity.java  # extends BaseBlockEntity implements HasItemStorage (sem HasEnergyStorage)
│                                #   campos: ItemStack de input carregado, crankProgress, ItemStack de output
└── (cliente) som de manivela + partículas de esmagamento; modelos de block-state por STAGE (estilo Composter)
```

- **Lookup de receita:** resolve o item carregado contra o `RecipeType` `logistics:macerator` (o `MaceratorRecipeWrapper` existente), filtrado pela tag de item `#logistics:hand_grindable`; pega apenas `result()`, ignora `grindingtime`/secundária/experiência. (Sem novo tipo de receita — DRY com o Macerador.)
- **Tag:** `data/logistics/tags/item/hand_grindable.json` listando os minérios/metais brutos alcançáveis pré-energia (cobre, ferro bruto, estanho bruto, …).
- **Máquina de estado de interação** no bloco:
  - `STAGE == 0` (vazio) + `useItemOn` com item `#hand_grindable` que tem receita de macerador → consome um, armazena no BE, `STAGE = 1`.
  - `STAGE in 1..N-1` + `use` (manivela) → `STAGE++`, toca som + partículas. Em `STAGE == N` → produz o pó para o output do BE, reseta o input carregado.
  - output presente + `use` → dá o pó ao jogador / derruba, `STAGE = 0`.
- **Registro:** em `LogisticsCore` ao lado do Macerador (bloco + item + tipo de BE). Wiring de modelo/partícula cliente por loader.

## Scope & non-goals

- **In:** o bloco, a máquina de estado de manivela, minérios→pós via receitas reutilizadas do Macerador + a tag `hand_grindable` (apenas primário), output por pipe/hopper, a receita de craft inicial.
- **Out:** qualquer RF; um GUI; **upgrades/tiers** (tier único); chance de subproduto; manivela automática; inputs de Macerador fora de minério (a tag `hand_grindable` limita a minérios/metais brutos); recipe-book/JEI além do que o Macerador já mostra.
- **Out:** substituir o Macerador — é estritamente o predecessor mais lento, sem bônus e sem energia.

## Decisions

- **Forma & interação** — **estação-manivela colocável**: carrega um minério, **clique direito repetidamente para dar manivela** até concluir (progresso por block-state estilo Composter, sem GUI).
- **Escopo** — **minérios → pós apenas, tier único sem escada**; a rampa de entrada sem energia, nada mais.
- **Receitas** — **reutiliza o tipo de receita do Macerador**, filtrado por `#logistics:hand_grindable`, **apenas resultado primário** (sem subproduto, sem XP). *(Confirmar a abordagem de reutilização de tag vs. um pequeno conjunto de receitas dedicado.)*
- **Automação** — pipes/hoppers podem mover itens para dentro/fora, mas **a manivela é apenas manual**, preservando o gate.

> Detalhes de implementação/balanceamento restantes: contagem exata de manivelas, a receita de craft, o conteúdo da tag `hand_grindable`, e se o pó cai automaticamente ou é coletado por clique.

## Done when

- Um Hand Grinder colocado aceita um minério `#hand_grindable`, avança um estágio por clique direito com som/partículas, e produz o pó base após ~8 manivelas — **sem energia** — em ambos os loaders.
- Produz **apenas** o pó primário (sem subproduto), confirmando que o Macerador mantém o bônus.
- Um jogador pode chegar ao Bronze sem motores: triturar cobre + estanho à mão → pós → craftar pó de bronze → fundir → lingote de bronze.
- Um hopper/pipe pode alimentar minério e puxar pó, mas o grinder ainda requer manivelas manuais para processar.

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 (rampa de processamento início de jogo)
- Par com: [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md) (receitas compartilhadas; upgrade com energia e subprodutos), [`0105-alloy-smelter.md`](0105-alloy-smelter.md) (o caminho de Bronze sem energia que este habilita)
- Posicionamento de tier: [`../progression-tiers.md`](../progression-tiers.md) (ferramenta manual na base da escada)
- Precedente de código: `ComposterBlock` vanilla (progresso por block-state + clique direito), `LecternBlock` (BE armazena item); `core/macerator/{MaceratorRecipeWrapper,MaceratorRecipeSerializer}` (receitas reutilizadas); registro em `LogisticsCore.java`
