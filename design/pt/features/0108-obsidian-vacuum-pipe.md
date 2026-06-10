# Tubo Vácuo de Obsidiana

> **Status:** 🚧 Planned · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (domínio `pipe`)
> **Source:** [`../mods/buildcraft.md`](../mods/buildcraft.md) (tubo de obsidiana — coleta do mundo) · **Depends on:** nada
> **Maps to (roadmap):** Fase 1 — pipes (Tubo Vácuo de Obsidiana)

Um tubo que aspira entidades de item largadas no mundo ao redor e as injeta na rede de tubos. Um Port fiel e de baixo risco — o sistema pipe/module já tem tudo necessário; isto é um novo módulo + registro de pipe.

## Problem & goal

Não há como ingerir *drops do mundo* (loot de mobs, itens de blocos quebrados, detritos de mineração, itens jogados no chão) na rede logística. O tubo de Obsidiana do BuildCraft era a solução canônica e um bloco clássico amado.

**Goal:** um tubo que puxa drops de `ItemEntity` próximos para a rede, com alcance de coleta balanceado e (opcionalmente) custo de energia, reutilizando o padrão de módulo existente.

## Requirements

### Functional
- Um novo bloco pipe que, a cada tick (ou a cada N ticks), escaneia um AABB ao redor de si por instâncias de `ItemEntity` e as puxa.
- Stacks coletadas se tornam `TravelingItem`s injetados no tubo, depois roteiam normalmente (para sinks/providers/a rede) — sem roteamento especial.
- Respeita delay de coleta (não re-pega itens que o tubo acabou de largar) e verificação de sanidade de TTL/age.
- **Alcance** configurável/balanceado (pequeno por padrão; veja Equilíbrio).
- Conecta à rede como qualquer tubo; direção de injeção é para o core do tubo.
- Opcional: filtro (apenas aspira itens correspondentes) — **adiar para v2** a menos que seja trivial.

### Balance
- Alcance padrão **modesto** (≈ raio de 1–2 blocos ao redor do tubo) — um coletor, não um ímã de toda a base. Alcance maior é candidato a uma variante com upgrade ou um augment depois.
- Considerar **custo em RF por coleta** para encaixar na direção de tubos com gate de energia (operações de tubo já consomem RF neste código). **Inclinação: custo pequeno ou zero em v1, revisitar com o modelo de energia.**
- Não deve brigar com hoppers/jogadores pelos mesmos itens de forma perturbadora; delay de coleta cuida do pior caso.

## Design sketch

O precedente mais próximo é o **Void pipe** (`VoidModule`) para "pipe de roteamento especial", mais o **drop-collection do Laser Quarry** para o scan de entidade do mundo — ambos verificados no código.

- **`ObsidianVacuumModule`** em `pipe/modules/`, implementando `TickingModule` (o scan) e `RoutingModule` (pass-through):
  - `onTick(PipeContext ctx)`: construir um AABB ao redor de `ctx.pos()`, `level.getEntitiesOfClass(ItemEntity.class, box, predicate)`, e para cada: pegar `itemEntity.getItem()`, criar um `TravelingItem`, injetar via `addItem(...)` do pipe, depois `itemEntity.remove(DISCARDED)`.
  - `route(...)`: retornar `RoutePlan.pass()` para que a rede roteie o item capturado.
- **Registrar o pipe** em `PipeTypes` e bloco/item em `LogisticsPipe`.
- Identidade de material: textura estilo obsidiana (o nome carrega a linhagem).
- Opcional: partículas de sucção no `randomDisplayTick` do cliente (Void pipe tem o precedente).

## Scope & non-goals

- **In:** o tubo vácuo, coleta de `ItemEntity` do mundo, roteamento normal dos itens capturados, alcance padrão sensato.
- **Out (v2):** coleta de XP/entidade, filtragem de itens, alcance grande/"ímã", tiers de upgrade dedicados.

## Open questions

- Padrão de **alcance** e se é fixo, dirigido por config, ou upgradável. **Inclinação: padrão fixo pequeno em v1, valor de config.**
- **Custo RF por coleta** — acoplar ao gate de energia de tubo existente ou manter gratuito em v1?
- Frequência de scan (todo tick vs. a cada N ticks) para performance com muitos tubos vácuo. **Inclinação: a cada poucos ticks.**
- Deve puxar apenas por uma face do bloco (BuildCraft apontava a face aberta) ou omnidirecionalmente? **Inclinação: raio omnidirecional por simplicidade.**

## Done when

- Itens largados dentro do alcance são puxados para o tubo e roteados para a rede em ambos os loaders.
- Itens que a rede acabou de largar não são re-pegos instantaneamente (delay de coleta respeitado).
- Performance ok com vários tubos vácuo carregados (scan limitado).

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → pipes; [`../mods/buildcraft.md`](../mods/buildcraft.md) → linha do tubo de Obsidiana
- Código: `pipe/modules/VoidModule`, `core/lib/pipe/{Module,TickingModule,RoutingModule,TravelingItem}`, `pipe/block/entity/PipeBlockEntity#dropItem`, `automation/laserquarry/.../QuarryBlockBreaker`, `pipe/PipeTypes`, `LogisticsPipe.java`
