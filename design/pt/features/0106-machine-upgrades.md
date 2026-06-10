# Upgrades / Augments de Máquinas

> **Status:** 🚧 Planned · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (transversal às máquinas; contrato em `core.lib`)
> **Source:** [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (Augments) · **Depends on:** o padrão de máquina (Macerador/Kiln); combina com [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md) (augment secundário)
> **Maps to (roadmap):** Fase 1 — upgrades de máquinas

Um sistema de modificadores por slot aplicado em todas as máquinas RF (Macerador, Kiln, Serraria, Alloy Smelter): augments de velocidade, eficiência, rendimento-secundário e saída-automática. A feature transversal de "fazer as máquinas parecerem um sistema, não blocos isolados". Hoje não há sistema de upgrade — este define um.

## Problem & goal

Cada máquina é atualmente um bloco fixo: velocidade fixa, custo de energia fixo, extração de saída manual. O sistema de augment do TE é o que transformou suas máquinas em uma progressão — você atualizava a máquina que tinha em vez de substituí-la (o princípio de equilíbrio: *cada tier torna a moagem obsoleta, não o gameplay*).

**Goal:** um mecanismo de upgrade único reutilizável, definido uma vez em `core.lib` e adotado por cada máquina, com um set pequeno e legível de augments.

## Requirements

### Functional
- Cada máquina suportada ganha um pequeno set de **slots de upgrade** (ex.: 3–4), acessível no GUI, contendo **itens de augment**.
- Augments modificam a matemática de processamento da máquina a cada tick. Tipos de augment para v1:
  - **Speed** — processamento mais rápido (menos ticks por operação), a maior custo de energia/tick.
  - **Efficiency** — menor energia por operação (e/ou buffer maior).
  - **Secondary yield** — aumenta a chance de secundária/subproduto onde a receita define uma (conecta [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md); sem efeito em receitas sem secundária).
  - **Auto-output** — empurra saídas concluídas para um inventário/pipe adjacente via `IItemStorage`, sem extrator necessário.
- Augments se acumulam dentro de limites sensatos; os efeitos são computados no **`ProcessingPlan`** da máquina para permanecerem testáveis por unidade.
- O estado do augment persiste (os slots são parte do inventário / NBT do BE) e cai com a máquina.

### Balance
- Augments são um **trade**, não poder puro: Speed custa mais energia; os efeitos fortes (velocidade máxima + secundário) demandam as baterias em camadas / motores melhores para alimentá-los — conecta o trabalho de [`0107-tiered-batteries.md`](0107-tiered-batteries.md) e motores em um loop.
- Manter o set pequeno e os limites modestos; evitar a sobrecarga de augments tardios do TE.
- Auto-output é conveniência, não substituto de logística — alvo adjacente único, não roteamento por rede.

## Design sketch

Definir o contrato em `core.lib`, aplicar no plano de processamento de cada máquina.

- **`core/lib/machine/MachineUpgrades`** (novo): contém os itens de augment para uma máquina e expõe modificadores derivados — ex.: `speedMultiplier()`, `energyPerOpMultiplier()`, `secondaryChanceBonus()`, `autoOutput()`. Apoiado por um pequeno `ItemInventoryComponent` de slots de upgrade.
- **Itens de augment** no domínio `core` (ex.: `SpeedUpgradeItem`/em tiers, ou um tipo de item único com um componente de tier). O tier poderia ser um data component para evitar proliferação de itens.
- **Integração com `ProcessingPlan`:** o `advance(...)` de cada plano já recebe parâmetros de progresso/energia — estender para receber os modificadores derivados (um pequeno record `MachineModifiers`) para que velocidade/eficiência sejam inputs de função-pura e ambas as branches permaneçam testáveis. Essa é a razão chave para rotear augments pelo plano, não ad-hoc no loop de tick.
- **Auto-output:** no tick do BE, após `complete`, se `upgrades.autoOutput()` empurrar saídas para o inventário adjacente via `ItemStorageLookup.find(...)` (precedente do lado de item existe).
- **GUI:** adicionar slots de upgrade ao `ScreenHandler` + `Screen` de cada máquina. Um sub-layout compartilhado (faixa de slots + tooltips) mantém as quatro telas consistentes.

**Ordem de adoção:** construir o contrato + integração com o Macerador primeiro (o Macerador já tem trabalho de saída secundária em andamento), depois adicionar os slots ao Kiln/Serraria/Alloy Smelter — eles compartilham o shape do `ProcessingPlan`, então a adoção é mecânica.

## Scope & non-goals

- **In:** o contrato de upgrade `core.lib`, quatro tipos de augment, slots GUI, adoção nas quatro máquinas da Fase 1.
- **Out:** **tiers / frames de máquinas** (corpos de máquinas craftados Basic→Resonant — separado, veja detalhamento TE "Machine frames / tiers"); config de augment por lado; augments que mudam *o que* uma máquina faz (apenas como rápido/barato/auto); augments para máquinas de fluido (até que essas máquinas existam).
- **Out:** augments de motor — motores não são máquinas de processamento; revisitar separadamente se desejado.

## Open questions

- **Itens de augment vs. tiers de máquina** — entregamos augments por slot (este doc) *e* tiers de máquina craftados, ou dobramos o tiering em augments? O detalhamento TE lista tiers como uma linha separada "—". **Inclinação: augments primeiro (mais flexível, menos proliferação de blocos); decidir tiers depois.**
- Um item de augment com componente de tier, ou itens distintos por tier? (O data component mantém a lista de itens curta.)
- Quantos slots por máquina, e limites por tipo de augment, para evitar acúmulo em trivialização?
- Speed e Efficiency deveriam ser mutuamente restritivos (uma curva) em vez de multiplicadores independentes?

## Done when

- Uma máquina com augment Speed processa mais rápido a maior energia/tick; com Efficiency, mais barato por operação — ambos verificados nos testes de unidade do `ProcessingPlan`.
- Um augment Secondary aumenta a chance de subproduto apenas onde uma secundária existe.
- Auto-output empurra itens concluídos para um inventário/pipe adjacente em ambos os loaders.
- Os mesmos itens de augment funcionam no Macerador, Kiln, Serraria e Alloy Smelter.

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → upgrades de máquinas
- Detalhamento: [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) → linhas "Augments" e "Machine frames / tiers"
- Código: `*ProcessingPlan` + `*ScreenHandler` das quatro máquinas; `core/lib/items/ItemInventoryComponent`; `core/lib/storage/ItemStorageLookup` (auto-output); precedente de data-component em `pipe/data/PipeDataComponents`
- Relacionados: [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md), [`0107-tiered-batteries.md`](0107-tiered-batteries.md)
