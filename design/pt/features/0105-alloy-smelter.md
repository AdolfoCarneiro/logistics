# Alloy Smelter

> **Status:** 🚧 Planned — **majoritariamente resolvido** (um item em aberto: confirmar o uso do Invar) · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (domínio `automation`) + materiais em `core`
> **Source:** [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (Induction Smelter) · **Depends on:** [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md) (Nickel como subproduto do Macerador de minério de ferro) · **Companion:** [`0103-hand-grinder.md`](0103-hand-grinder.md) — Hand Grinder: manual, sem energia, minério→pó; desacopla a economia de pós/ligas de energia
> **Maps to (roadmap):** Fase 1 — Alloy Smelter; linhas de materiais em [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md)

Uma máquina RF de duas entradas que combina dois metais em uma liga. Esta feature é **máquina + uma pequena expansão de materiais**: adiciona **Nickel + Invar** (o primeiro novo metal/liga desde o Bronze), introduz um **caminho de crafting via pó** para que ligas pareçam *ligas* e não sejam bloqueadas por energia, e dá às ligas uma máquina + custo de energia como o tier de automação. Agora o Bronze existe mas não tem máquina de produção; isso torna o set de ligas coerente. Combina com as linhas de materiais em [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md).

## Problem & goal

A mid-game clássica rodava em ligas, mas liar metais deve *parecer* combinação de metais — não uma receita plana de crafting, e não algo que bloqueia uma liga inicial (Bronze) por precisar de energia primeiro.

O modelo (decidido): **ligas são feitas combinando dois *pós* componentes.**
- **À mão:** `pó-componente-A + pó-componente-B → pó de liga` na grade de crafting (sem atalho lingote+lingote), depois fundir o pó de liga → lingote. Pós vêm do Macerador (com energia) **ou do Hand Grinder (manual, sem energia)** — então Bronze é alcançável sem energia.
- **Automatizado:** o **Alloy Smelter** faz a combinação diretamente e continuamente, aceitando tanto o par de pós quanto o par de lingotes → liga.

**Goal:** uma máquina RF de bloco único e duas entradas (o tier de automação) mais o caminho de crafting via pó e os materiais **Nickel + Invar** que dão ao set de ligas espaço para crescer.

## Requirements

### Functional
- **Dois slots de entrada + um inventário de saída compartilhado de 3 slots.** As saídas (a liga *e* qualquer secundária por chance) preenchem a região de saída compartilhada onde houver espaço — *não* uma divisão rígida um-primário/um-secundário. Justificativa de tamanho: uma carga de entrada completa (ex.: 64 + 64) roda muitas operações e produz **duas pilhas de liga** (≈128), então dois slots fazem buffer da primária e o terceiro deixa espaço para um subproduto por chance. Usa o **`ChanceResult` + pausa-até-limpar** do 0102 (uma op só completa quando o inventário de saída tem espaço para seus resultados; caso contrário, o progresso se mantém, nada perdido), generalizado de um slot dedicado para o buffer compartilhado. **v1 não entrega nenhuma receita que produz uma secundária** (a capacidade é construída/testada, depois dormente; veja abaixo). Acesso por lado: entradas pelo topo/lados; baixo → o inventário de saída inteiro.
- **Tipo de receita personalizado** `logistics:alloy_smelter`: dois ingredientes (cada um com uma contagem), um resultado, uma **`ChanceResult` secundária opcional** (schema presente, não usada por nenhuma receita v1), um tempo de fundição, custo de energia. O matching de receita é **independente de ordem** para as duas entradas.
- **Aceita pós ou lingotes:** o smelter tem receitas para `pó + pó → pó de liga` e `lingote + lingote → lingote de liga` (saída em espécie). A grade suporta apenas a forma de pó (abaixo).
- RF-alimentado com buffer interno; consome energia por tick enquanto funde (mesmo shape que `ENERGY_PER_TICK` do Macerador).
- **Caminho manual (sem máquina):** receitas de grade `pó-componente-A + pó-componente-B → pó de liga` (ex.: pó de cobre + pó de estanho → pó de bronze; pó de ferro + pó de nickel → pó de invar). **Sem receita de grade lingote+lingote.** Pó de liga funde para lingote em uma fornalha/Kiln.
- **Novos materiais:** adicionar **Nickel** (`nickel_dust`, `nickel_ingot`, `nickel_nugget`) e **Invar** (`invar_dust`, `invar_ingot`, `invar_nugget`). Nickel é obtido como **subproduto do Macerador de minério de ferro** (usa `ChanceResult` do 0102 — sem novo worldgen). Invar = Ferro + Nickel. **Sem item de escória na v1** (adiado com seu uso).
- **Uso do Invar** (estende escadas existentes): **Invar Gear** + **Invar Valve** (o próximo tier acima de `bronze_gear` / `bronze_valve`), com o sabor de estabilidade dimensional / "válvula de motor" apontando para futuros frames de máquinas. *(Confirmar — veja Decisões.)*
- **Saída secundária está dormente:** o buffer de saída compartilhado + schema de receita existem para que um futuro subproduto (escória — um loop de reciclagem/Rich-Slag estilo TE) seja uma **mudança puro-JSON + adicionar-o-item**. Sem item de escória, sem receita produtora de escória, na v1.
- Categoria JEI (entrada A + entrada B → resultado, mais a chance secundária *quando uma receita a define*); GUI de recipe-book com barras de progresso + energia (reutilizar layout da tela do Macerador, alargado para o inventário de saída de 3 slots).

### Balance
- **Bronze não é bloqueado por energia:** Hand Grinder → pós → craftar pó de bronze na grade → fundir. O Alloy Smelter é o tier de automação/eficiência, não a única porta.
- **Invar é (aceitavelmente) bloqueado por energia:** Nickel vem do subproduto do Macerador com energia de minério de ferro, então Invar — uma liga de mid-tier — fica por trás de ter energia. Ok para seu tier.
- Energia/tempo ancorados no Macerador (buffer 10.000 RF, intake ~128 RF/t, draw ~10 RF/t, op ~200 ticks) mas um pouco mais custoso — liar metais é um passo acima de moer.
- **Set de ligas mantido enxuto:** Bronze (existe) + Invar (novo). Electrum/Silver, Constantan e ligas de alto tier (Signalum/Lumium/Enderium) são adiados/fora para v1.
- Proporções (âncoras; **aproximadas — TE não tem fonte pública**, ajustar em playtest): Bronze = 3 cobre : 1 estanho; Invar ≈ 2 ferro : 1 nickel.

## Design sketch

Seguir o "shape" da máquina exatamente como Macerador/Kiln (padrão verificado):

```text
common/src/main/java/com/logistics/automation/alloysmelter/
├── AlloySmelterBlock.java            # extends MachineBlock; FACING + LIT; partículas quando ativo
├── AlloySmelterBlockEntity.java      # extends BaseBlockEntity
│                                     #   implements HasItemStorage, HasEnergyStorage,
│                                     #   WorldlyContainer, MenuBehavior.HasMenu
│                                     #   campos: ItemInventoryComponent (2 entradas + 3-slot saída compartilhada),
│                                     #   EnergyComponent, ContainerData
├── AlloySmelterRecipe.java           # implements Recipe<…>; dois ingredientes, resultado, ChanceResult secundária opcional, tempo
├── AlloySmelterRecipeSerializer.java # MapCodec + StreamCodec
├── AlloySmelterProcessingPlan.java   # lógica pura (advance/consume/complete) — testada com testes de unidade
├── AlloySmelterScreenHandler.java    # extends RecipeBookMenu; getters de ContainerData
└── (cliente) AlloySmelterScreen.java + jei/{Category,Plugin}
```

- Registrar bloco/item/BE/menu/tipo-de-receita/serializer em **`LogisticsAutomation`**; registrar os novos **itens Nickel/Invar** (+ gear/valve de Invar) em `LogisticsCore` ao lado dos itens tin/bronze existentes. Usar `registerBlockWithItem` / `registerBlockEntity` / `registerMenuType` / `registerItem`.
- A entrada de receita de duas entradas é um `RecipeInput` personalizado (não o `SingleRecipeInput` vanilla) contendo ambos os slots; `matches()` testa ambas as ordens de entrada.
- **Saída secundária** reutiliza o `ChanceResult` + pausa-até-limpar compartilhados de [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md), generalizado para o inventário de saída compartilhado de 3 slots — construir em lockstep. (O `ChanceResult` do 0102 também é usado para o subproduto nickel do minério de ferro no Macerador.) O campo `secondary` opcional é suportado pelo codec mas não é usado por receitas v1.
- **Receitas do smelter** (ambas as formas; saída em espécie; sem `secondary` na v1):
  ```json
  { "type": "logistics:alloy_smelter",
    "inputs": [ { "ingredient": "#c:ingots/copper", "count": 3 },
                { "ingredient": "logistics:core/tin_ingot", "count": 1 } ],
    "result": { "id": "logistics:core/bronze_ingot", "count": 4 }, "smelttime": 200 }
  { "type": "logistics:alloy_smelter",
    "inputs": [ { "ingredient": "logistics:core/copper_dust", "count": 3 },
                { "ingredient": "logistics:core/tin_dust", "count": 1 } ],
    "result": { "id": "logistics:core/bronze_dust", "count": 4 }, "smelttime": 200 }
  // "secondary" opcional: { "id": "...", "count": 1, "chance": 0.25 } suportado mas não usado na v1
  ```
- **Receita de grade manual** (`crafting_shapeless`): `pó de cobre ×3 + pó de estanho ×1 → pó de bronze ×4`; `pó de ferro ×2 + pó de nickel ×1 → pó de invar ×3`. Sem receita lingote+lingote.
- **Subproduto Nickel** (Macerador, usa 0102): minério de ferro → pó de ferro + pequena chance `nickel_dust`. Fundição: `nickel_dust → nickel_ingot`, `invar_dust → invar_ingot` (vanilla/Kiln). Esta é a primeira entrada concreta no mapa de subprodutos que 0102 adiou aqui.

## Scope & non-goals

- **In:** o Alloy Smelter (2 entradas + **inventário de saída compartilhado de 3 slots**, com a *capacidade* `ChanceResult` + pausa-até-limpar construída e testada mas sem receita v1 usando-a); os materiais **Nickel + Invar**; **Invar gear + valve**; as receitas de grade de liga-via-pó; as receitas de forma-pó e forma-lingote do smelter; o subproduto nickel de minério de ferro no Macerador; JEI + GUI.
- **Companion (brief separado, necessário para o objetivo de sem-gate-de-energia):** o **Hand Grinder** — um bloco manual e sem energia para minério→pó (o equivalente manual do Macerador; sem chance de subproduto, já que o Macerador com energia é o que ganha o bônus). Especificar no próprio brief; esta feature assume que existe.
- **Out:** o **item de escória + qualquer receita produtora de escória + uso da escória** (todos adiados juntos — a capacidade de saída é construída mas dormente); Electrum/Silver, Constantan, ligas de alto tier (Signalum/Lumium/Enderium); um **minério de Nickel + worldgen** dedicado (obtido via subproduto por ora; adicionar um minério apenas se o supply for muito escasso); I/O de fluido; tiers de máquina ([`0106-machine-upgrades.md`](0106-machine-upgrades.md)); **crafting de grade lingote+lingote** (intencionalmente excluído para que o caminho manual de ligas passe pelos pós).

## Decisions

- **Set de liga/metal** — **Bronze (existe) + Invar (novo)**, via **Nickel**. Electrum/Silver e o resto são adiados. *Em aberto: confirmar **uso do Invar** — proposto como Invar gear + Invar valve (próximo tier nas escadas existentes, sabor "válvula de motor"/precisão, futuros frames de máquinas). Se não for um uso satisfatório, adiar Nickel/Invar até uma feature de tier de máquinas dar um lar a ele.*
- **Fonte de liga** — caminho de crafting via pó (grade: `pó + pó → pó de liga`, depois fundir → lingote) **+** o Alloy Smelter (automatizado, aceita par de pós ou par de lingotes → liga). **Sem receita de grade lingote+lingote.** Bronze permanece alcançável sem energia **via o Hand Grinder**.
- **Inventário de saída e escória** — construir o **inventário de saída compartilhado de 3 slots** + a *capacidade* `ChanceResult`/pausa-até-limpar agora, mas entregar **sem item de escória e sem receita produtora de escória**. Os segundo/terceiro slots fazem buffer de runs de liga multi-stack hoje e ficam prontos para um futuro subproduto; habilitar escória depois é puro JSON + adicionar o item. O uso eventual da escória = um loop de reciclagem/Rich-Slag estilo TE.
- **Fonte de Nickel** — **subproduto do Macerador de minério de ferro** (`ChanceResult` do 0102), sem novo worldgen; seu próprio minério apenas como fast-follow se o supply parecer muito escasso.

> As escolhas restantes são implementação/balanceamento: proporções exatas e % de subproduto de nickel, tempo de fundição, consumidores de receita do Invar, e se o Invar também recebe um core (tem precedente cobre/bronze).

## Done when

- Bloco coloca, rotaciona, funde duas entradas → liga em ambos os loaders, persiste através de save/load.
- O smelter aceita **tanto** pares de pó (→ pó de liga) quanto pares de lingote (→ lingote de liga).
- Caminho manual funciona: `pó de cobre + pó de estanho → pó de bronze` na grade → fundir → lingote de bronze, alcançável **sem energia** (via o Hand Grinder).
- Minério de ferro macera para pó de ferro + uma chance de `nickel_dust`; `pó de ferro + pó de nickel → pó de invar` → fundir → lingote de invar.
- Uma carga de entrada completa roda até a conclusão, fazendo buffer de **duas pilhas de liga** pelo inventário de saída sem shuffling manual de slots.
- A **capacidade de saída secundária** (roll `ChanceResult` ambas as branches + pausa-até-limpar quando o inventário de saída está cheio) é **testada com testes de unidade via uma receita sintética**, mesmo que nenhuma receita v1 entregue produzindo uma secundária.
- Invar gear + Invar valve são craftáveis.
- JEI lista receitas de liga; GUI mostra progresso + energia + o inventário de saída de 3 slots.

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → Alloy Smelter; linhas de materiais em [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md)
- Padrão de código: `automation/kiln/*` e `core/macerator/*` (bloco, BE, receita, serializer, plano de processamento, tela, JEI); registro em `LogisticsAutomation.java`; novos itens de metal em `LogisticsCore.java` (ao lado de `TIN_*`/`BRONZE_*`)
- Mecanismo compartilhado: `ChanceResult` de [`0102-macerator-secondary-outputs.md`](0102-macerator-secondary-outputs.md) (generalizado aqui para o inventário de saída compartilhado)
- **Nota de fonte TE:** TE **não tem fonte pública** — comportamento de escória/subprodutos e proporções são baseados em wiki/conhecimento, aproximados; ajustar em playtest. Itens de metal existentes confirmados em `LogisticsCore.java`: `TIN_*`, `BRONZE_*`, `*_DUST`, `*_GEAR`, `COPPER/BRONZE_VALVE`, `COPPER/BRONZE_CORE` (Nickel/Invar/Slag **ainda não existem**).
- **Brief companion (a escrever):** Hand Grinder — manual, sem energia, minério→pó; desacopla a economia de pós/ligas de energia. Listado em [`README.md`](README.md).
